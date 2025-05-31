package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.annotation.OptionalEncoding;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.annotation.StructTypeSelector;
import net.deanly.structlayout.codec.decode.StructDecodeResult;
import net.deanly.structlayout.codec.decode.StructDecoder;
import net.deanly.structlayout.dispatcher.StructTypeResolver;
import net.deanly.structlayout.exception.LayoutInitializationException;

import java.lang.annotation.Annotation;

public class StructObjectFieldHandler extends BaseFieldHandler {

    @Override
    public <T> int handleField(T instance, java.lang.reflect.Field field, byte[] data, int offset) throws IllegalAccessException {
        StructObjectField annotation = field.getAnnotation(StructObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @StructObjectField", field.getName())
            );
        }

        OptionalEncoding optionalEncoding = annotation.optional();
        int consumed = 0;

        if (optionalEncoding == OptionalEncoding.BORSH) {
            boolean isPresent = isValuePresent(data, offset, optionalEncoding);
            consumed += 1; // consume 1 byte for prefix

            if (!isPresent) {
                field.setAccessible(true);
                field.set(instance, null);
                return consumed;
            }

            offset += 1;
        }

        Class<?> nestedType = field.getType();
        StructDecodeResult<?> result;

        Annotation nestedTypeAnnotation = nestedType.getAnnotation(StructTypeSelector.class);
        if (nestedTypeAnnotation != null && nestedType.isInterface()) {
            // 인터페이스일 경우 `@StructTypeSelector` 로 생성
            try {
                if (data.length - offset == 0 && StructTypeResolver.resolveNoDataSpan(nestedType) == 0) {
                    return 0;
                } else {
                    Class<?> implClazz = StructTypeResolver.resolveClass(data, nestedType, offset);
                    result = StructDecoder.decode(implClazz, data, offset);
                }

            } catch(Exception e) {
                throw new LayoutInitializationException("Failed to dispatch interface: `" + nestedType.getName() + "` => " + e.getMessage(), e);
            }
        } else {
            result = StructDecoder.decode(nestedType, data, offset);
        }

        field.setAccessible(true);
        field.set(instance, result.getValue());

        return consumed + result.getSize();
    }


}
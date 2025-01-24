package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.codec.decode.StructDecodeResult;
import net.deanly.structlayout.codec.decode.StructDecoder;
import net.deanly.structlayout.annotation.StructSequenceObjectField;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.exception.InvalidSequenceTypeException;
import net.deanly.structlayout.exception.LayoutInitializationException;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Decoder handler for fields annotated with @StructSequenceObjectField.
 */
public class StructSequenceObjectFieldHandler extends BaseFieldHandler {

    @SuppressWarnings("unchecked")
    @Override
    public <T> int handleField(T instance, java.lang.reflect.Field field, byte[] data, int offset) throws IllegalAccessException {
        // 1. 어노테이션 확인
        StructSequenceObjectField annotation = field.getAnnotation(StructSequenceObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @StructSequenceObjectField", field.getName())
            );
        }

        // 2. Layout 인스턴스 가져오기
        Field<?> lengthField = resolveLayout(annotation.lengthType());

        // 3. 길이 정보 디코딩
        Object lengthRawValue = lengthField.decode(data, offset);
        int length = (int) TypeConverterHelper.convertToType(lengthRawValue, Integer.class);
        int currentOffset = offset + lengthField.getSpan();

        // 4. 배열 또는 컬렉션 타입 확인
        Class<?> fieldType = field.getType();
        Object result;
        Class<?> elementType;

        if (fieldType.isArray()) {
            // 배열인 경우
            elementType = fieldType.getComponentType();
            result = Array.newInstance(elementType, length); // Create an array to store values
        } else if (Collection.class.isAssignableFrom(fieldType)) {
            // 컬렉션인 경우
            elementType = resolveCollectionElementType(field);
            result = createCollectionInstance(fieldType);
        } else {
            throw new InvalidSequenceTypeException(field.getName(), fieldType, "Only Array or Collection types are supported for @StructSequenceObjectField");
        }

        // 5. 개별 요소 디코드
        for (int i = 0; i < length; i++) {
            StructDecodeResult<?> decodeResult = StructDecoder.decode(elementType, data, currentOffset);
            Object decodedValue = decodeResult.getValue();
            int decodedSize = decodeResult.getSize();

            if (fieldType.isArray()) {
                Array.set(result, i, decodedValue);
            } else {
                ((Collection<Object>) result).add(decodedValue);
            }

            currentOffset += decodedSize;
        }

        // 6. 필드 값 설정
        field.setAccessible(true);
        field.set(instance, result);

        return currentOffset - offset;
    }

    private Class<?> resolveCollectionElementType(java.lang.reflect.Field field) {
        // Resolve the generic type of the Collection
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            if (parameterizedType.getActualTypeArguments().length > 0) {
                return (Class<?>) parameterizedType.getActualTypeArguments()[0];
            }
        }
        return Object.class;
    }

    private Collection<Object> createCollectionInstance(Class<?> fieldType) {
        if (List.class.isAssignableFrom(fieldType)) {
            return new ArrayList<>();
        } else {
            throw new IllegalArgumentException(
                    String.format("Unsupported collection type '%s'. Only List is currently supported.", fieldType.getName())
            );
        }
    }
}
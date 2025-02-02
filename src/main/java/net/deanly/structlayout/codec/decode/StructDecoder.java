package net.deanly.structlayout.codec.decode;

import net.deanly.structlayout.annotation.StructTypeSelector;
import net.deanly.structlayout.codec.helpers.FieldHelper;
import net.deanly.structlayout.dispatcher.StructTypeResolver;
import net.deanly.structlayout.factory.ClassFactory;
import net.deanly.structlayout.exception.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;

public class StructDecoder {
    public static <T> StructDecodeResult<T> decode(Class<T> type, byte[] data, int startOffset) {
        if (startOffset < 0 || startOffset >= data.length) {
            throw new InvalidDataOffsetException(startOffset, data.length);
        }

        // 0. 디코딩 클래스 리졸버가 정의된 경우
        Annotation annotation = type.getAnnotation(StructTypeSelector.class);
        if (annotation != null) {
            if (type.isInterface()) {
                try {
                    // 인터페이스일 경우 `@StructTypeSelector` 로 생성
                    Class<? extends T> implClazz = StructTypeResolver.resolve(data, type);
                    StructDecodeResult<? extends T> result = StructDecoder.decode(implClazz, data, startOffset);
                    return StructDecodeResult.of((T) result.getValue(), result.getSize());

                } catch(Exception e) {
                    throw new LayoutInitializationException("Failed to dispatch interface: `" + type.getName() + "` => " + e.getMessage(), e);
                }
            }
        }

        // 1. 디코딩할 객체의 인스턴스 생성
        T instance = ClassFactory.createNoArgumentsInstance(type);

        // 2. 상속 계층의 모든 필드 수집 및 정렬
        List<Field> allFields = FieldHelper.getAllDeclaredFieldsIncludingSuperclasses(type);
        List<Field> orderedFields = FieldHelper.getOrderedFields(allFields);

        // 3. 디코딩: 필드 순서대로 Byte 데이터를 객체 필드에 매핑
        int offset = startOffset;
        for (Field field : orderedFields) {
            offset += FieldProcessor.processField(instance, field, data, offset);
        }

        // 4. 디코딩 결과 반환
        return StructDecodeResult.of(instance, offset - startOffset);
    }
}
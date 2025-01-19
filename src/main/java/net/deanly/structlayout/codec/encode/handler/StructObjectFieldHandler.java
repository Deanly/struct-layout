package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.codec.encode.StructEncoder;
import net.deanly.structlayout.type.DataType;

import java.lang.reflect.Field;

public class StructObjectFieldHandler extends BaseFieldHandler {

    @Override
    protected DataType resolveDataType(Field field) {
        StructObjectField annotation = field.getAnnotation(StructObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @StructObjectField", field.getName())
            );
        }

        // StructObjectField는 데이터 타입이 중첩 구조에 의해 정의
        return null; // Nested 구조는 명시적 DataType 정의를 요구하지 않음
    }

    @Override
    public <T> byte[] handleField(T instance, Field field) throws IllegalAccessException {
        // 1. 필드 값 추출
        Object fieldValue = extractFieldValue(instance, field);

        // 2. @StructObjectField 어노테이션 확인
        StructObjectField annotation = field.getAnnotation(StructObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @StructObjectField", field.getName())
            );
        }

        // 3. 필드 값이 null인지 확인
        if (fieldValue == null) {
            return new byte[0]; // Null 처리: 빈 배열
        }

        // 4. StructEncoder를 사용하여 재귀적으로 인코딩 처리
        return StructEncoder.encode(fieldValue);
    }
}
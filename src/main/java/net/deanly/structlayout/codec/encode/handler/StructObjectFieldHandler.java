package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.codec.encode.StructEncoder;

import java.lang.reflect.Field;

public class StructObjectFieldHandler extends BaseFieldHandler {
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
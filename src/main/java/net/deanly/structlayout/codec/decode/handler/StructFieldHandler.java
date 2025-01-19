package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;

import java.lang.reflect.Field;

public class StructFieldHandler extends BaseFieldHandler {
    @Override
    public <T> int handleField(T instance, Field field, byte[] data, int offset) throws IllegalAccessException {
        // 1. StructField 어노테이션 가져오기
        StructField annotation = field.getAnnotation(StructField.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Field is not annotated with @StructField: " + field.getName());
        }

        // 2. Layout 가져오기
        Layout<Object> layout = resolveLayout(annotation.dataType());

        // 3. 값 디코딩
        Object rawValue = layout.decode(data, offset);
        if (rawValue == null) {
            throw new IllegalArgumentException("Decoded value is null for field: " + field.getName());
        }

        // 4. 필드 타입에 맞는 값으로 변환 (TypeConverter 적용)
        Class<?> fieldType = field.getType();
        Object convertedValue = TypeConverterHelper.convertToType(rawValue, fieldType);

        // 5. 디코딩된 값을 필드에 설정
        field.setAccessible(true);
        field.set(instance, convertedValue);

        // 6. 사용된 바이트 수 반환
        return layout.getSpan();
    }
}
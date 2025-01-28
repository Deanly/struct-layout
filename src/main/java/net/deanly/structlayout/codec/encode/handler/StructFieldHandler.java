package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.analysis.FieldDebugInfo;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.codec.helpers.FieldHelper;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.exception.CustomLayoutInstantiationException;

import java.util.List;

public class StructFieldHandler extends BaseFieldHandler {

    @Override
    public <T> byte[] handleField(T instance, java.lang.reflect.Field field) throws IllegalAccessException {
        // 1. 필드 값 추출
        Object fieldValue = extractFieldValue(instance, field);

        // 2. @CustomLayoutField 어노테이션 확인
        StructField annotation = field.getAnnotation(StructField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @CustomLayoutField", field.getName())
            );
        }

        // 3. Layout 클래스에서 Layout 인스턴스를 생성
        Class<? extends Field<?>> layoutClass = annotation.type();
        Field<Object> fieldInstance = createLayoutInstance(layoutClass, field.getName());

        // 4. Layout의 제네릭 타입 추출
        Class<?> genericType = FieldHelper.extractLayoutGenericType(layoutClass);

        // 5. TypeConverter를 통해 값 변환
        Object convertedValue = TypeConverterHelper.convertToType(fieldValue, genericType);

        // 6. Layout을 이용해 값 인코딩
        return fieldInstance.encode(convertedValue);
    }

    @Override
    public <T> List<FieldDebugInfo.Builder> handleDebug(T instance, java.lang.reflect.Field field) throws IllegalAccessException {
        byte[] encodedBytes = this.handleField(instance, field);
        FieldDebugInfo.Builder builder = FieldDebugInfo.builder();
        builder.fieldName(field.getName());
        builder.encodedBytes(encodedBytes);
        return List.of(builder);
    }

    /**
     * 사용자 정의 Layout 인스턴스 생성
     */
    @SuppressWarnings("unchecked")
    private Field<Object> createLayoutInstance(Class<? extends Field<?>> layoutClass, String fieldName) {
        try {
            return (Field<Object>) layoutClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new CustomLayoutInstantiationException(
                    String.format("Failed to instantiate custom layout '%s' for field '%s'", layoutClass.getName(), fieldName),
                    e
            );
        }
    }
}
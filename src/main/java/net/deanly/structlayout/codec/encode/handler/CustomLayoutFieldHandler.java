package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.annotation.CustomLayoutField;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.exception.CustomLayoutInstantiationException;
import net.deanly.structlayout.type.DataType;

import java.lang.reflect.Field;

public class CustomLayoutFieldHandler extends BaseFieldHandler {

    @Override
    protected DataType resolveDataType(Field field) {
        CustomLayoutField annotation = field.getAnnotation(CustomLayoutField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @CustomLayoutField", field.getName())
            );
        }

        // 사용자 정의 Layout이 자체적으로 DataType을 관리하지 않는다고 가정
        return null; // CustomLayoutField 자체에는 DataType이 명시되지 않음.
    }

    @Override
    public <T> byte[] handleField(T instance, Field field) throws IllegalAccessException {
        // 1. 필드 값 추출
        Object fieldValue = extractFieldValue(instance, field);

        // 2. @CustomLayoutField 어노테이션 확인
        CustomLayoutField annotation = field.getAnnotation(CustomLayoutField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @CustomLayoutField", field.getName())
            );
        }

        // 3. Layout 클래스에서 Layout 인스턴스를 생성
        Class<? extends Layout<?>> layoutClass = annotation.layout();
        Layout<Object> layoutInstance = createLayoutInstance(layoutClass, field.getName());

        // 4. Layout의 제네릭 타입 추출
        Class<?> genericType = extractLayoutGenericType(layoutClass);

        // 5. TypeConverter를 통해 값 변환
        Object convertedValue = TypeConverterHelper.convertToType(fieldValue, genericType);

        // 6. Layout을 이용해 값 인코딩
        return layoutInstance.encode(convertedValue);
    }

    /**
     * Extract the generic type defined by the Layout class.
     */
    private Class<?> extractLayoutGenericType(Class<? extends Layout<?>> layoutClass) {
        // 기본적인 제네릭 추출
        try {
            return (Class<?>) ((java.lang.reflect.ParameterizedType) layoutClass
                    .getGenericSuperclass()).getActualTypeArguments()[0];
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Unable to extract generic type from Layout class '%s'", layoutClass.getName()), e
            );
        }
    }

    /**
     * 사용자 정의 Layout 인스턴스 생성
     */
    @SuppressWarnings("unchecked")
    private Layout<Object> createLayoutInstance(Class<? extends Layout<?>> layoutClass, String fieldName) {
        try {
            return (Layout<Object>) layoutClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new CustomLayoutInstantiationException(
                    String.format("Failed to instantiate custom layout '%s' for field '%s'", layoutClass.getName(), fieldName),
                    e
            );
        }
    }
}
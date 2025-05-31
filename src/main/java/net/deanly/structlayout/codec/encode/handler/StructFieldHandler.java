package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.analysis.FieldDebugInfo;
import net.deanly.structlayout.annotation.OptionalEncoding;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.codec.helpers.FieldHelper;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.exception.CustomLayoutInstantiationException;
import net.deanly.structlayout.type.DynamicSpanField;

import java.util.List;

public class StructFieldHandler extends BaseFieldHandler {


    @Override
    public <T> byte[] handleField(T instance, java.lang.reflect.Field field) throws IllegalAccessException {
        StructField annotation = field.getAnnotation(StructField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @StructField", field.getName())
            );
        }

        // Layout 클래스
        Class<? extends Field<?>> layoutClass = annotation.type();
        Field<Object> layout = createLayoutInstance(layoutClass, field.getName());
        Object value = extractFieldValue(instance, field);

        OptionalEncoding opt = annotation.optional();

        if (opt == OptionalEncoding.BORSH) {
            if (value == null) {
                return new byte[]{0x00}; // None
            } else {
                Object converted = TypeConverterHelper.convertToLayoutType(value, layoutClass);
                byte[] encoded = layout.encode(converted);
                byte[] result = new byte[1 + encoded.length];
                result[0] = 0x01; // Some
                System.arraycopy(encoded, 0, result, 1, encoded.length);
                return result;
            }
        }

        // NONE (기존 로직)
        if (value == null) {
            if (layout instanceof DynamicSpanField) {
                return new byte[((DynamicSpanField) layout).getNoDataSpan()];
            } else {
                return new byte[layout.getSpan()];
            }
        } else {
            Object converted = TypeConverterHelper.convertToLayoutType(value, layoutClass);
            return layout.encode(converted);
        }
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
//    @SuppressWarnings("unchecked")
    private Field<Object> createLayoutInstance(Class<? extends Field<?>> layoutClass, String fieldName) {
        return resolveLayout(layoutClass);
//        try {
//            return (Field<Object>) layoutClass.getDeclaredConstructor().newInstance();
//        } catch (Exception e) {
//            throw new CustomLayoutInstantiationException(
//                    String.format("Failed to instantiate custom layout '%s' for field '%s'", layoutClass.getName(), fieldName),
//                    e
//            );
//        }
    }
}
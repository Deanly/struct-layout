package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.analysis.CachedLayoutProvider;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.type.DataType;

import java.lang.reflect.Field;

public abstract class BaseFieldHandler {

    /**
     * Main handling logic for a field.
     * Handles common steps like validation, type conversion, and layout encoding.
     */
    public <T> byte[] handleField(T instance, Field field) throws IllegalAccessException {
        // 1. 어노테이션으로부터 DataType 가져오기
        DataType dataType = resolveDataType(field);

        // 2. Field 값 읽기
        Object fieldValue = extractFieldValue(instance, field);

        // 3. Field 값 검증 및 변환
        Object convertedValue = validateAndConvert(fieldValue, dataType, field);

        // 4. Layout에 태워 무조건 byte[]로 변환
        Layout<Object> layout = resolveLayout(dataType);
        return layout.encode(convertedValue);
    }

    /**
     * Resolves the DataType associated with a field's annotation.
     */
    protected abstract DataType resolveDataType(Field field);

    /**
     * Extracts the layout instance for the specified DataType.
     */
    protected Layout<Object> resolveLayout(DataType dataType) {
        return CachedLayoutProvider.getLayout(dataType); // 공통 Layout 제공자 사용
    }

    /**
     * Extracts the raw field value from the given instance and field.
     */
    protected Object extractFieldValue(Object instance, Field field) throws IllegalAccessException {
        return field.get(instance);
    }

    /**
     * Validates and converts the field value to the target type, if necessary.
     */
    protected Object validateAndConvert(Object value, DataType dataType, Field field) {
        try {
            return TypeConverterHelper.convertToLayoutType(value, dataType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid value for field " + field.getName() + ": " + e.getMessage(), e
            );
        }
    }

    /**
     * Handles null values based on the target DataType.
     */
    protected Object handleNullValue(DataType dataType) {
        if (dataType.isNumeric()) {
            return 0; // Default for numeric types
        } else if (dataType.isStringType()) {
            return ""; // Default for strings
        }
        throw new IllegalArgumentException("Field value cannot be null for DataType: " + dataType);
    }
}

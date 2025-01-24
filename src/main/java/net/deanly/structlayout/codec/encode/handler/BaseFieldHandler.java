package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.analysis.CachedLayoutProvider;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.type.FieldBase;

public abstract class BaseFieldHandler {

    /**
     * Main handling logic for a field.
     * Handles common steps like validation, type conversion, and layout encoding.
     */
    public abstract <T> byte[] handleField(T instance, java.lang.reflect.Field field) throws IllegalAccessException;

    /**
     * Extracts the layout instance for the specified DataType.
     */
    protected Field<Object> resolveLayout(Class<? extends Field<?>> basicType) {
        return CachedLayoutProvider.getLayout(basicType);
    }

    /**
     * Extracts the raw field value from the given instance and field.
     */
    protected Object extractFieldValue(Object instance, java.lang.reflect.Field field) throws IllegalAccessException {
        return field.get(instance);
    }

    /**
     * Validates and converts the field value to the target type, if necessary.
     */
    protected Object validateAndConvert(Object value, Class<? extends Field<?>> basicType, java.lang.reflect.Field field) {
        try {
            return TypeConverterHelper.convertToLayoutType(value, basicType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid value for field " + field.getName() + ": " + e.getMessage(), e
            );
        }
    }

    /**
     * Handles null values based on the target DataType.
     */
    protected Object handleNullValue(Class<? extends Field<?>> basicType) {
        if (FieldBase.isNumericType(basicType)) {
            return 0; // Default for numeric types
        } else if (FieldBase.isStringType(basicType)) {
            return ""; // Default for strings
        }
        throw new IllegalArgumentException("Field value cannot be null for Field: " + basicType);
    }
}

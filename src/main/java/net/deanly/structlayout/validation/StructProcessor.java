package net.deanly.structlayout.validation;

import net.deanly.structlayout.type.DataType;

public class StructProcessor {

    public static void validateFieldType(Object fieldValue, DataType dataType, String fieldName) {
        if (fieldValue == null) {
            throw new IllegalArgumentException("Field '" + fieldName + "' is null.");
        }

        Class<?> expectedType = dataType.getFieldType(); // DataType에 정의된 fieldType 사용

        if (expectedType == null) {
            throw new IllegalStateException(
                    "Unsupported DataType '" + dataType + "' for field '" + fieldName + "'.");
        }

        if (!expectedType.isInstance(fieldValue)) {
            throw new ClassCastException(
                    String.format(
                            "Field '%s' has type '%s' but expected type is '%s' based on DataType '%s'.",
                            fieldName, fieldValue.getClass().getName(),
                            expectedType.getName(), dataType
                    )
            );
        }
    }
}
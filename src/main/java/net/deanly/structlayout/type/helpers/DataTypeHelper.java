package net.deanly.structlayout.type.helpers;

import net.deanly.structlayout.type.DataType;

public class DataTypeHelper {

    public static boolean isIntegerType(DataType dataType) {
        Class<?> type = dataType.getFieldType();
        return type == Integer.class || type == Short.class || type == Byte.class || type == Long.class;
    }

    public static boolean isFloatType(DataType dataType) {
        Class<?> type = dataType.getFieldType();
        return type == Float.class || type == Double.class;
    }

    public static boolean isStringType(DataType dataType) {
        Class<?> type = dataType.getFieldType();
        return type == String.class;
    }

    public static boolean matches(DataType dataType, Class<?> fieldType) {
        return dataType.getFieldType().isAssignableFrom(fieldType);
    }
}
package net.deanly.structlayout.type.helpers;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.type.FieldBase;

import java.util.Objects;

public class DataTypeHelper {

    public static boolean isIntegerType(Class<? extends Field<?>> fieldType) {
        Class<?> type = FieldBase.getGenericTypeAsObject(fieldType);
        return type == Integer.class || type == Short.class || type == Byte.class || type == Long.class;
    }

    public static boolean isFloatType(Class<? extends Field<?>> fieldType) {
        Class<?> type = FieldBase.getGenericTypeAsObject(fieldType);
        return type == Float.class || type == Double.class;
    }

    public static boolean isStringType(Class<? extends Field<?>> fieldType) {
        Class<?> type = FieldBase.getGenericTypeAsObject(fieldType);
        return type == String.class;
    }

    public static boolean matches(Class<? extends Field<?>> fieldType, Class<?> targetType) {
        return Objects.requireNonNull(FieldBase.getGenericTypeAsObject(fieldType)).isAssignableFrom(targetType);
    }
}
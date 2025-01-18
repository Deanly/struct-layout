package net.deanly.structlayout.codec.helpers;

import net.deanly.structlayout.exception.StructParsingException;

import java.util.List;

public class NumberConversionHelper {

    /**
     * Converts a given value to the expected numeric type while performing validations.
     * Supports Int8 (Byte), Int16 (Short), Int32 (Integer), and Int64 (Long).
     * Excludes Float32, Float64, and any unsupported types by throwing an exception.
     *
     * @param value        The input numeric value to convert.
     * @param expectedType The target type for conversion (e.g., Byte, Short, Integer, Long).
     * @param fieldName    The name of the field being processed (for error messages).
     * @return The value converted to the requested type.
     * @throws IllegalArgumentException If the type is unsupported or the value is out of range.
     */
    public static Object convertToExpectedNumericType(Object value, Class<?> expectedType, String fieldName) {
        if (expectedType == null) {
            throw new IllegalArgumentException("Expected type cannot be null for field: " + fieldName);
        }

        // Allowed value types (Int8, Int16, Int32, Int64).
        if (!(expectedType == Byte.class ||
                expectedType == Short.class ||
                expectedType == Integer.class ||
                expectedType == Long.class)) {
            throw new IllegalArgumentException(
                    String.format("Field '%s': Unsupported expected type %s. Only Byte, Short, Integer, or Long are allowed.",
                            fieldName, expectedType.getSimpleName())
            );
        }

        // Validate to ensure that values of types Float adn Double are net allowed.
        if (value instanceof Float || value instanceof Double) {
            throw new IllegalArgumentException(
                    String.format("Field '%s': Float or Double types are not supported for numeric conversion.", fieldName)
            );
        }

        // Values are not allowed unless they are of type Number.
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(
                    String.format("Field '%s': Unsupported value type %s. Only integers (Int8, Int16, Int32, Int64) are allowed.",
                            fieldName, value.getClass().getSimpleName())
            );
        }

        // If the value already matches the expected type, return it as is.
        if (expectedType.isInstance(value)) {
            return value;
        }

        // Handle Int8 (Byte).
        if (expectedType == Byte.class) {
            long longValue = ((Number) value).longValue();
            if (longValue < Byte.MIN_VALUE || longValue > Byte.MAX_VALUE) {
                throw new IllegalArgumentException(
                        String.format("Field '%s': Value %d is out of range for Int8 (byte).", fieldName, longValue)
                );
            }
            return (byte) longValue;
        }

        // Handle Int16 (Short).
        if (expectedType == Short.class) {
            long longValue = ((Number) value).longValue();
            if (longValue < Short.MIN_VALUE || longValue > Short.MAX_VALUE) {
                throw new IllegalArgumentException(
                        String.format("Field '%s': Value %d is out of range for Int16 (short).", fieldName, longValue)
                );
            }
            return (short) longValue;
        }

        // Handle Int32 (Integer).
        if (expectedType == Integer.class) {
            long longValue = ((Number) value).longValue();
            if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        String.format("Field '%s': Value %d is out of range for Int32 (int).", fieldName, longValue)
                );
            }
            return (int) longValue;
        }

        // Handle Int64 (Long).
        if (expectedType == Long.class) {
            return ((Number) value).longValue();
        }

        // If unsupported type, throw an exception.
        throw new IllegalArgumentException(
                String.format(
                        "Field '%s': Cannot convert value of type %s to expected type %s.",
                        fieldName, value.getClass().getName(), expectedType.getName()
                )
        );
    }

    public static int convertToInt(Object rawLength, String fieldName) {
        if (rawLength instanceof Number) {
            return ((Number) rawLength).intValue();
        }
        throw new StructParsingException(
                String.format("Field '%s': Unable to decode length. Expected a numeric type but got %s.",
                        fieldName, rawLength.getClass().getName())
        );
    }


    /**
     * Converts a List<Object> to a primitive array (e.g., float[], int[], etc.).
     * Supports primitive types only.
     *
     * @param values      The list of values to convert.
     * @param componentType The type of the primitive array (e.g., float, int).
     * @return A primitive array of the desired type.
     */
    public static Object convertToPrimitiveArray(List<Object> values, Class<?> componentType) {
        for (Object value : values) {
            if ((componentType != boolean.class && !(value instanceof Number)) ||
                    (componentType == boolean.class && !(value instanceof Boolean))) {
                throw new UnsupportedOperationException(
                        "Unsupported value type in list: " + value.getClass().getName() +
                                ". Expected type matching componentType: " + componentType.getSimpleName()
                );
            }
        }

        if (componentType == float.class) {
            float[] array = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = ((Number) values.get(i)).floatValue();
            }
            return array;
        } else if (componentType == int.class) {
            int[] array = new int[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = ((Number) values.get(i)).intValue();
            }
            return array;
        } else if (componentType == double.class) {
            double[] array = new double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = ((Number) values.get(i)).doubleValue();
            }
            return array;
        } else if (componentType == long.class) {
            long[] array = new long[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = ((Number) values.get(i)).longValue();
            }
            return array;
        } else if (componentType == short.class) {
            short[] array = new short[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = ((Number) values.get(i)).shortValue();
            }
            return array;
        } else if (componentType == byte.class) {
            byte[] array = new byte[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = ((Number) values.get(i)).byteValue();
            }
            return array;
        } else if (componentType == char.class) {
            char[] array = new char[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = (char) ((Number) values.get(i)).intValue();
            }
            return array;
        } else if (componentType == boolean.class) {
            boolean[] array = new boolean[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = (Boolean) values.get(i);
            }
            return array;
        }

        throw new UnsupportedOperationException("Unsupported primitive array type: " + componentType);
    }
}
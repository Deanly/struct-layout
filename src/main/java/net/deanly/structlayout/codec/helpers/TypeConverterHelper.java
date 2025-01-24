package net.deanly.structlayout.codec.helpers;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.Field;
import net.deanly.structlayout.exception.TypeConversionException;
import net.deanly.structlayout.type.FieldBase;

/**
 * Utility class for converting values of one type to another while supporting validation,
 * handling special cases, and dealing with primitive types, null values, and exceptions.
 */
public class TypeConverterHelper {

    /**
     * Converts a value to the specified target type and validates the converted value
     * using the provided validator predicate. If the value is valid, it returns the
     * converted value. If the value is invalid according to the validator, it throws
     * an IllegalArgumentException.
     *
     * @param value       The input value to be converted.
     * @param targetType  The target type to which the value is to be converted.
     * @param validator   A predicate to validate the converted value.
     * @return The converted and validated value.
     * @throws IllegalArgumentException If the value is invalid based on the validator.
     */
    public static Object convertWithValidation(Object value, Class<?> targetType, java.util.function.Predicate<Object> validator) {
        Object result = convertToType(value, targetType);
        if (!validator.test(result)) {
            throw new IllegalArgumentException("Value is invalid: " + value);
        }
        return result;
    }

    /**
     * Converts a value from the Struct Type to the Layout Type.
     * Throws exceptions if conversion is not possible or value is incompatible.
     *
     * @param value      The value to be converted.
     * @param targetType The Field defining the target type.
     * @return The converted value.
     */
    public static Object convertToLayoutType(Object value, Class<? extends Field<?>> targetType) {
        if (value == null) {
            return handleNullValue(FieldBase.getGenericTypeAsObject(targetType));
        }
        return convertToType(value, FieldBase.getGenericTypeAsObject(targetType));
    }

    /**
     * Converts a value from the original type to the target layout type.
     * Throws exceptions if conversion is not possible or value is incompatible.
     *
     * @param value      The value to be converted.
     * @param targetType The target type represented by Class.
     * @return The converted value.
     */
    public static Object convertToType(Object value, Class<?> targetType) {
        if (value == null) {
            return handleNullValue(targetType);
        }

        // 숫자형 변환: targetType이 숫자형 타입인지 확인
        if (Number.class.isAssignableFrom(targetType) || isPrimitiveNumberType(targetType)) {
            if (value instanceof Number) {
                return convertNumber((Number) value, targetType);
            } else if (value instanceof String) {
                return convertFromString((String) value, targetType);
            } else {
                throw new TypeConversionException("Cannot convert non-numeric value to numeric type: " + targetType);
            }
        }

        // 문자열 변환: targetType이 String인지 확인
        if (targetType == String.class) {
            if (value instanceof String) {
                return value; // 이미 String인 경우 변환 필요 없음
            } else {
                return value.toString(); // 다른 타입은 toString으로 변환
            }
        }

        // 불리언 반환: targetType이 Boolean인지 확인
        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof String) {
                return Boolean.valueOf((String) value);
            }
        }

        // 문자 반환: targetType이 Char인지 확인
        if (targetType == Character.class || targetType == char.class) {
            if (value instanceof String) {
                return convertFromString((String) value, targetType);
            }
        }

        // 동일 타입인지 확인
        if (targetType.isInstance(value)) {
            return value; // 변환 필요 없음
        }

        // 특별한 타입에 대한 변환 규칙 확인
        Object specialTypeResult = convertToSpecialTypes(value, targetType);
        if (specialTypeResult != null) {
            return specialTypeResult;
        }

        // 변환 불가능한 경우 예외 발생
        throw new TypeConversionException("Cannot convert value of type " + value.getClass()
                + " to target type " + targetType);
    }

    // 기본 자료형 숫자 타입인지 확인하는 메서드
    private static boolean isPrimitiveNumberType(Class<?> type) {
        return type == byte.class || type == short.class || type == int.class || type == long.class
                || type == float.class || type == double.class;
    }

    private static Object convertNumber(Number value, Class<?> targetClass) {
        if (value instanceof Float && ((Float) value).isNaN()) {
            return handleNaN(targetClass);
        }
        if (value instanceof Double && ((Double) value).isNaN()) {
            return handleNaN(targetClass);
        }
        if (Double.isInfinite(value.doubleValue())) {
            return handleInfinity(targetClass, value.doubleValue());
        }

        // 값 범위 검사 (각 타입에 대한 최소, 최대값 제한 추가)
        if (targetClass == Byte.class || targetClass == byte.class) {
            checkRange(value.longValue(), Byte.MIN_VALUE, Byte.MAX_VALUE, targetClass);
            return value.byteValue();
        }
        if (targetClass == Short.class || targetClass == short.class) {
            checkRange(value.longValue(), Short.MIN_VALUE, Short.MAX_VALUE, targetClass);
            return value.shortValue();
        }
        if (targetClass == Integer.class || targetClass == int.class) {
            checkRange(value.longValue(), Integer.MIN_VALUE, Integer.MAX_VALUE, targetClass);
            return value.intValue();
        }
        if (targetClass == Long.class || targetClass == long.class) {
            // `long`은 Java가 제공하는 최대 범위이므로 제한 없음
            return value.longValue();
        }
        if (targetClass == Float.class || targetClass == float.class) {
            checkFloatRange(value.doubleValue(), Float.MIN_VALUE, Float.MAX_VALUE, targetClass);
            return value.floatValue();
        }
        if (targetClass == Double.class || targetClass == double.class) {
            // `double`은 Java가 제공하는 최대 범위이므로 제한 없음
            return value.doubleValue();
        }
        if (targetClass == java.math.BigInteger.class) {
            return java.math.BigInteger.valueOf(value.longValue());
        }

        throw new TypeConversionException("Unsupported number conversion to type: " + targetClass);
    }

    private static void checkRange(long value, long min, long max, Class<?> targetClass) {
        if (value < min || value > max) {
            throw new TypeConversionException("Value " + value + " is out of range for type: " + targetClass.getSimpleName());
        }
    }

    private static void checkFloatRange(double value, double min, double max, Class<?> targetClass) {
        if (value < -max || value > max) { // `min`과 `max`의 절대값이 필요
            throw new TypeConversionException("Value " + value + " is out of range for type: " + targetClass.getSimpleName());
        }
    }

    private static Object convertFromString(String value, Class<?> targetClass) {
        try {
            if (targetClass == String.class) {
                return value;
            }
            if (targetClass == Short.class || targetClass == short.class) {
                return Short.valueOf(value);
            }
            if (targetClass == Byte.class || targetClass == byte.class) {
                return Byte.valueOf(value);
            }
            if (targetClass == Integer.class || targetClass == int.class) {
                return Integer.valueOf(value);
            }
            if (targetClass == Long.class || targetClass == long.class) {
                return Long.valueOf(value);
            }
            if (targetClass == Float.class || targetClass == float.class) {
                return Float.valueOf(value);
            }
            if (targetClass == Double.class || targetClass == double.class) {
                return Double.valueOf(value);
            }
            if (targetClass == java.math.BigInteger.class) {
                return new java.math.BigInteger(value);
            }
            if (targetClass == java.math.BigDecimal.class) {
                return new java.math.BigDecimal(value); // 추가된 BigDecimal 처리
            }
            if (targetClass == Boolean.class || targetClass == boolean.class) {
                return Boolean.valueOf(value); // Boolean 처리 추가
            }
            if (targetClass == Character.class || targetClass == char.class) {
                if (value.length() != 1) {
                    throw new TypeConversionException("Cannot convert String to Character: " + value);
                }
                return value.charAt(0); // Character 처리 추가
            }
        } catch (NumberFormatException e) {
            throw new TypeConversionException("Cannot convert String to " + targetClass + ": " + value, e);
        }

        throw new TypeConversionException("Unsupported String conversion to type: " + targetClass);
    }

    @SuppressWarnings("unchecked")
    private static Object convertToSpecialTypes(Object value, Class<?> targetClass) {
        if (targetClass == java.math.BigDecimal.class) {
            return new java.math.BigDecimal(value.toString());
        }
        if (targetClass == java.math.BigInteger.class) {
            return new java.math.BigInteger(value.toString());
        }
        if (targetClass == java.time.LocalDate.class) {
            return java.time.LocalDate.parse(value.toString()); // ISO-8601 형식 기본 지원
        }
        if (targetClass == java.time.LocalDateTime.class) {
            return java.time.LocalDateTime.parse(value.toString());
        }
        if (targetClass == java.time.ZonedDateTime.class) {
            return java.time.ZonedDateTime.parse(value.toString());
        }
        if (targetClass.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) targetClass, value.toString());
        }
        if (targetClass == java.util.UUID.class) {
            return java.util.UUID.fromString(value.toString());
        }
        return null; // 다른 경우에 null 반환
    }

    private static Object handleNullValue(Class<?> targetType) {
        // 숫자형 타입(원시 타입 및 참조 타입 모두 동일 처리)
        if (Number.class.isAssignableFrom(targetType) || targetType.isPrimitive()) {
            if (targetType == Integer.class || targetType == int.class) {
                return 0;
            }
            if (targetType == Long.class || targetType == long.class) {
                return 0L;
            }
            if (targetType == Float.class || targetType == float.class) {
                return 0.0f;
            }
            if (targetType == Double.class || targetType == double.class) {
                return 0.0;
            }
            if (targetType == Short.class || targetType == short.class) {
                return (short) 0;
            }
            if (targetType == Byte.class || targetType == byte.class) {
                return (byte) 0;
            }
        }

        // Boolean 타입(원시 및 참조 타입 동일 처리)
        if (targetType == Boolean.class || targetType == boolean.class) {
            return false; // 기본값 false
        }

        // Character 타입(원시 및 참조 타입 동일 처리)
        if (targetType == Character.class || targetType == char.class) {
            return '\u0000'; // 기본 char 값 (null character)
        }

        // 문자열 타입
        if (targetType == String.class) {
            return ""; // 빈 문자열 반환
        }

        // 그 외 타입
        return null;
    }

    private static Object handleNaN(Class<?> targetClass) {
        // NaN 예외 처리 로직 (필요 시 기본값 설정 가능)
        if (targetClass == Float.class || targetClass == float.class) {
            return 0.0f; // Float에서 NaN은 기본적으로 0.0f로 변환
        }
        if (targetClass == Double.class || targetClass == double.class) {
            return 0.0; // Double에서 NaN은 기본적으로 0.0로 변환
        }
        throw new TypeConversionException("Cannot convert NaN to non-floating-point type: " + targetClass);
    }

    private static Object handleInfinity(Class<?> targetClass, double value) {
        if (targetClass == Float.class || targetClass == float.class) {
            return value > 0 ? Float.MAX_VALUE : -Float.MAX_VALUE;
        }
        if (targetClass == Double.class || targetClass == double.class) {
            return value > 0 ? Double.MAX_VALUE : -Double.MAX_VALUE;
        }
        throw new TypeConversionException("Cannot convert Infinity to non-floating-point type: " + targetClass);
    }
}
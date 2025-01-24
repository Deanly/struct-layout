package net.deanly.structlayout.type;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.deanly.structlayout.Field;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Abstract class providing encoding and decoding functionality.
 * @param <T> The type of the value being encoded/decoded.
 */
@Slf4j
@Getter
public abstract class FieldBase<T> implements Field<T> {
    private final int span; // Number of bytes for this layout
    private final String property; // Optional associated property name

    /**
     * Constructs a Layout with a specified span and an optional property.
     *
     * @param span Number of bytes that this Layout will process.
     *             **This value MUST be explicitly defined by the implementer.**
     *             It is critical to define the span correctly, as it determines:
     *               - The number of bytes this layout processes when encoding/decoding.
     *               - Validation offsets during data processing.
     * @param property Optional property name associated with the Layout.
     *                 For example, this can be used for debugging or mapping.
     */
    public FieldBase(int span, String property) {
        this.span = span;
        this.property = property;
    }

    /**
     * Constructs a Layout with a specified span.
     *
     * @param span Number of bytes that this Layout will process.
     *             **This value MUST be explicitly defined by the implementer.**
     */
    public FieldBase(int span) {
        this(span, null);
    }

    public abstract byte[] encode(T value);
    public abstract T decode(byte[] bytes, int offset);

    /**
     * Ensure the offset and length are valid for decoding data.
     *
     * @param data   The data array.
     * @param offset The offset to start decoding.
     * @throws IllegalArgumentException if data or offset is invalid.
     */
    protected void validateLength(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (offset < 0 || offset + span > data.length) {
            String fullHex = bytesToHex(data);

            throw new IllegalArgumentException(
                    String.format(
                            "Illegal decoding request. Data is insufficient for span: %d, offset: %d, data length: %d. Full data in HEX: [%s]",
                            span, offset, data.length, fullHex
                    )
            );
        }
    }

    /**
     * Converts the entire byte array to a hexadecimal string.
     *
     * @param data The byte array to convert.
     * @return A string representing the hex values of the entire byte array.
     */
    public String bytesToHex(byte[] data) {
        if (data == null || data.length == 0) {
            return "Empty or null data";
        }

        StringBuilder hexBuilder = new StringBuilder(data.length * 2); // Each byte = 2 HEX chars
        for (int i = 0; i < data.length; i++) {
            // Convert each byte to HEX and append to StringBuilder
            hexBuilder.append(String.format("%02X", data[i]));
            if (i < data.length - 1) {
                hexBuilder.append(" "); // Optional: Space between HEX values
            }
        }
        return hexBuilder.toString();
    }

    /**
     * Converts the given byte array to a hexadecimal string for debugging purposes.
     *
     * @param data   The byte array to convert.
     * @param offset The starting offset in the data array.
     * @return A string representing the hex values of the selected bytes.
     */
    public String bytesToHex(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Cannot convert null data to hex.");
        }
        if (offset < 0 || offset + getSpan() > data.length) {
            throw new IllegalArgumentException(
                    "Invalid offset or length for hex conversion. Data length: " + data.length +
                            ", offset: " + offset + ", length: " + getSpan());
        }

        StringBuilder hexBuilder = new StringBuilder(getSpan() * 2); // Each byte = 2 HEX chars
        for (int i = 0; i < getSpan(); i++) {
            // Convert each byte to HEX and append to StringBuilder
            hexBuilder.append(String.format("%02X", data[offset + i]));
            if (i < getSpan() - 1) {
                hexBuilder.append(" "); // Optional: Adds space between HEX values for readability
            }
        }
        return hexBuilder.toString();
    }

    /**
     * Retrieves the generic type parameter of the class.
     * This method assumes that the class extends a generic superclass
     * and extracts the actual type parameter at runtime.
     *
     * @return The {@code Class<T>} representing the generic type parameter,
     *         or {@code null} if the generic type cannot be determined.
     */
    @SuppressWarnings("unchecked")
    public Class<T> getGenericType() {
        Type superClass = this.getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType parameterized) {
            return (Class<T>) parameterized.getActualTypeArguments()[0];
        }
        return null;
    }

    /**
     * Retrieves the generic type parameter of the given class.
     * This method assumes that the class extends a generic superclass
     * and attempts to extract the actual type parameter at runtime.
     *
     * @param fieldClass The {@code Class<? extends Field>} to analyze.
     * @return The {@code Class<T>} representing the generic type parameter,
     *         or {@code null} if the generic type cannot be determined.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getGenericType(Class<? extends Field<T>> fieldClass) {
        Type superClass = fieldClass.getGenericSuperclass();
        if (superClass instanceof ParameterizedType parameterizedType) {
            Type typeArgument = parameterizedType.getActualTypeArguments()[0];

            if (typeArgument instanceof Class<?>) {
                return (Class<T>) typeArgument;
            }
        }
        return null;
    }

    /**
     * Extracts and returns the generic type of the first parameterized type argument
     * used in the specified class that extends {@code Field<?>}.
     *
     * @param fieldClass The class that extends {@code Field<?>}, for which the first generic type is to be determined.
     * @return The {@code Class<?>} object representing the first generic type,
     *         or {@code null} if the generic type is not available or cannot be resolved.
     */
    public static Class<?> getGenericTypeAsObject(Class<? extends Field<?>> fieldClass) {
        Type superClass = fieldClass.getGenericSuperclass();
        if (superClass instanceof ParameterizedType parameterizedType) {
            Type typeArgument = parameterizedType.getActualTypeArguments()[0];
            if (typeArgument instanceof Class<?>) {
                return (Class<?>) typeArgument;
            }
        }
        return null;
    }

    /**
     * Determines whether a given class represents a numeric type.
     * Numeric types include subclasses of {@link Number} or primitive types such as
     * byte, short, int, long, float, and double.
     *
     * @param type The class to be checked.
     * @return {@code true} if the specified class is a numeric type; otherwise {@code false}.
     */
    private static boolean isNumericClass(Class<?> type) {
        return Number.class.isAssignableFrom(type) || type.isPrimitive() && (
                type == byte.class || type == short.class || type == int.class ||
                        type == long.class || type == float.class || type == double.class);
    }

    /**
     * Determines if a given class is of type {@code String}.
     *
     * @param type The class to be evaluated.
     * @return {@code true} if the specified class is {@code String}; {@code false} otherwise.
     */
    private static boolean isStringClass(Class<?> type) {
        return type == String.class;
    }

    /**
     * Determines if the provided class represents a numeric field type.
     *
     * @param fieldClass The class object representing a specific type of field.
     * @return true if the field type is a numeric type; otherwise, false.
     */
    public static boolean isNumericType(Class<? extends Field<?>> fieldClass) {
        // Field 클래스의 제네릭 타입 가져오기
        Class<?> genericType = getGenericTypeAsObject(fieldClass);
        return genericType != null && isNumericClass(genericType);
    }

    /**
     * Determines if the given field class represents a string type.
     *
     * @param fieldClass The class type of the field to check.
     * @return True if the field is of a string type, otherwise false.
     */
    public static boolean isStringType(Class<? extends Field<?>> fieldClass) {
        Class<?> genericType = getGenericTypeAsObject(fieldClass);
        return genericType != null && isStringClass(genericType);
    }

    public void printDebug(byte[] data, int offset, java.lang.reflect.Field field) {
        if (isTestEnvironment()) {
            if (offset > 0 && offset + getSpan() <= data.length) {
                try {
                    log.debug("[Field: {}.{}] Bytes: [{}] ({} bytes), Value: {}",
                            field.getDeclaringClass().getSimpleName(), field.getName(),
                            bytesToHex(data, offset), getSpan(), decode(data, offset));
                } catch (RuntimeException ex) {
                    log.debug("[Field: {}.{}] {}", field.getDeclaringClass().getSimpleName(), field.getName(), ex.getMessage());
                }
            }
        }
    }

    private static Boolean isTestMode = null;
    public boolean isTestEnvironment() {
        if (isTestMode == null) {
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                if (element.getClassName().startsWith("org.junit.") || element.getClassName().startsWith("org.testng.")) {
                    isTestMode = true;
                    break;
                }
            }
            if (isTestMode == null) {
                isTestMode = false;
            }
        }
        return isTestMode;
    }
}
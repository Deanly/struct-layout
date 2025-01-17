package net.deanly.structlayout;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.deanly.structlayout.codec.Encoder;
import net.deanly.structlayout.codec.Decoder;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * Abstract class providing encoding and decoding functionality.
 * @param <T> The type of the value being encoded/decoded.
 */
@Slf4j
@Getter
public abstract class Layout<T> implements Encoder<T>, Decoder<T> {
    @Setter(AccessLevel.PROTECTED)
    private int span; // Number of bytes for this layout
    private final String property; // Optional associated property name

    public Layout(int span, String property) {
        this.span = span;
        this.property = property;
    }

    public Layout(int span) {
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

    public void printDebug(byte[] data, int offset, Field field) {
        if (isTestEnvironment()) {
            if (offset > 0 && offset + getSpan() <= data.length) {
                log.debug("[Field: {}.{}] Bytes: [{}] ({} bytes), Value: {}",
                        field.getDeclaringClass().getSimpleName(), field.getName(),
                        bytesToHex(data, offset), getSpan(), decode(data, offset));
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
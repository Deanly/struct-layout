package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Abstract class for FixedStringField implementations.
 * Makes it easy to define fields with a fixed-length string representation.
 */
public abstract class AbstractFixedStringField extends FieldBase<String> {

    /**
     * Constructs an AbstractFixedStringField with the specified fixed span.
     *
     * @param fixedSpan The fixed length of the string this field will handle.
     */
    protected AbstractFixedStringField(int fixedSpan) {
        super(fixedSpan, String.class);
    }

    @Override
    public String decode(byte[] data, int offset) {
        validateLength(data, offset);

        // Extract fixed-length bytes and convert to a string
        byte[] fixedBytes = Arrays.copyOfRange(data, offset, offset + getSpan());
        return new String(fixedBytes, StandardCharsets.UTF_8).trim(); // Trim padding
    }

    @Override
    public byte[] encode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        byte[] stringBytes = value.getBytes(StandardCharsets.UTF_8);

        if (stringBytes.length > getSpan()) {
            throw new IllegalArgumentException("String length exceeds fixed span of " + getSpan() + " bytes.");
        }

        // Create byte array with fixed span and copy the string bytes into it
        byte[] fixedBytes = new byte[getSpan()];
        System.arraycopy(stringBytes, 0, fixedBytes, 0, stringBytes.length);

        // If necessary, fill the rest of the array with padding (e.g., 0x00)
        Arrays.fill(fixedBytes, stringBytes.length, getSpan(), (byte) 0);
        return fixedBytes;
    }
}
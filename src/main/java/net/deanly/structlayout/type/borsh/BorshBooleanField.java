package net.deanly.structlayout.type.borsh;

import net.deanly.structlayout.type.FieldBase;

/**
 * Represents a Borsh boolean field.
 * Encodes a boolean value into a single byte:
 * - true -> 0x01
 * - false -> 0x00
 */
public class BorshBooleanField extends FieldBase<Boolean> {

    public BorshBooleanField() {
        super(1, Boolean.class); // 1 byte for boolean
    }

    /**
     * Decodes a single byte from the data array into a boolean value.
     *
     * @param data   The byte array to decode from.
     * @param offset The offset in the array to start decoding.
     * @return The decoded boolean value.
     * @throws IllegalArgumentException If the data is null or the byte is not a valid boolean representation.
     */
    @Override
    public Boolean decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (data.length <= offset) {
            throw new IllegalArgumentException("Offset is out of bounds for the given data array.");
        }

        byte value = data[offset];
        if (value == 0x00) {
            return false;
        } else if (value == 0x01) {
            return true;
        } else {
            throw new IllegalArgumentException("Invalid boolean value: " + value);
        }
    }

    /**
     * Encodes a boolean value into a single byte.
     *
     * @param value The boolean value to encode.
     * @return A byte array representing the boolean value.
     * @throws IllegalArgumentException If the value is null.
     */
    @Override
    public byte[] encode(Boolean value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        return new byte[]{(byte) (value ? 0x01 : 0x00)};
    }

    /**
     * Returns the size (in bytes) of the field.
     * For Borsh boolean, this is always 1 byte.
     *
     * @return The size of the field.
     */
    @Override
    public int getSpan() {
        return 1;
    }

}
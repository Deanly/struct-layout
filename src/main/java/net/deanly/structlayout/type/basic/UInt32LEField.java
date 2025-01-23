package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.Field;

public class UInt32LEField extends Field<Long> implements CountableType {

    public UInt32LEField(String property) {
        super(4, property); // 4 bytes
    }

    public UInt32LEField() {
        this(null);
    }

    /**
     * Decodes an unsigned 32-bit integer from a byte array in little-endian format.
     *
     * @param data   The byte array containing the data.
     * @param offset The offset where decoding starts.
     * @return The decoded unsigned 32-bit integer as a Long.
     */
    @Override
    public Long decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (offset < 0 || offset + getSpan() > data.length) {
            throw new IllegalArgumentException("Invalid offset or insufficient data length.");
        }

        // Combine bytes in little-endian order
        long result = 0;
        for (int i = 0; i < getSpan(); i++) {
            result |= (long) (data[offset + i] & 0xFF) << (8 * i);
        }

        if (result > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("Decoded value exceeds allowed range for UInt32.");
        }

        return result;
    }

    /**
     * Encodes an unsigned 32-bit integer into a byte array in little-endian format.
     *
     * @param value The unsigned 32-bit integer as Long to encode.
     * @return The encoded byte array.
     */
    @Override
    public byte[] encode(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        if (value < 0 || value > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("Value must be in range 0 to 4294967295.");
        }

        byte[] data = new byte[getSpan()]; // Allocate memory for 4 bytes.

        // Write bytes in little-endian order
        for (int i = 0; i < getSpan(); i++) {
            data[i] = (byte) ((value >> (8 * i)) & 0xFF);
        }

        return data;
    }

}
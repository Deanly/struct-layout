package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.CountableField;

public class UInt32BEField extends FieldBase<Long> implements CountableField<Long> {

    public UInt32BEField() {
        super(4, Long.class);
    }

    /**
     * Decodes an unsigned 32-bit integer from a byte array in big-endian format.
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

        // Combine bytes in big-endian order
        long result = 0;
        for (int i = 0; i < getSpan(); i++) {
            result = (result << 8) | (data[offset + i] & 0xFF);
        }

        if (result > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("Decoded value exceeds allowed range for UInt32.");
        }

        return result;
    }

    /**
     * Encodes an unsigned 32-bit integer into a byte array in big-endian format.
     *
     * @param value The unsigned 32-bit integer as Long to encode.
     * @return The encoded byte array.
     */
    @Override
    public byte[] encode(Long value) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("Cannot encode negative values for UInt32.");
        }

        byte[] data = new byte[getSpan()]; // Allocate memory for 4 bytes.

        // Write bytes in big-endian order
        for (int i = getSpan() - 1; i >= 0; i--) {
            data[i] = (byte) (value & 0xFF);
            value >>>= 8;
        }

        return data;
    }

}
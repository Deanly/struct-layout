package net.deanly.structlayout.type.impl;

import net.deanly.structlayout.Layout;

public class UInt32LELayout extends Layout<Integer> {

    public UInt32LELayout(String property) {
        super(4, property); // 4 bytes
    }

    public UInt32LELayout() {
        this(null);
    }

    /**
     * Decodes an unsigned 32-bit integer from a byte array in little-endian format.
     *
     * @param data   The byte array containing the data.
     * @param offset The offset where decoding starts.
     * @return The decoded unsigned 32-bit integer as an Integer.
     */
    @Override
    public Integer decode(byte[] data, int offset) {
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

        return (int) result;
    }

    /**
     * Encodes an unsigned 32-bit integer into a byte array in little-endian format.
     *
     * @param value The unsigned 32-bit integer to encode.
     * @return The encoded byte array.
     */
    @Override
    public byte[] encode(Integer value) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("Cannot encode negative values for UInt32.");
        }

        byte[] data = new byte[getSpan()]; // Allocate memory for 4 bytes.

        // Write bytes in little-endian order
        for (int i = 0; i < getSpan(); i++) {
            data[i] = (byte) ((value >> (8 * i)) & 0xFF);
        }

        return data;
    }
}
package net.deanly.structlayout.type.impl;

import net.deanly.structlayout.Layout;

public class UInt64BELayout extends Layout<Long> {

    private static final long MAX_SAFE_INTEGER = 1L << 52; // 2^52
    private static final long UINT32_MASK = 0xFFFFFFFFL;

    /**
     * Constructs a UInt64BE layout for unsigned 64-bit integers in big-endian format.
     *
     * @param property (Optional) Property name associated with this layout.
     */
    public UInt64BELayout(String property) {
        super(8, property);
    }

    public UInt64BELayout() {
        this(null);
    }

    /**
     * Decodes an unsigned 64-bit integer from a byte array in big-endian format.
     *
     * @param data   The byte array containing the data.
     * @param offset The offset where decoding starts.
     * @return The decoded unsigned integer as a long.
     */
    @Override
    public Long decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (offset < 0 || offset + getSpan() > data.length) {
            throw new IllegalArgumentException("Invalid offset or insufficient data length.");
        }

        // Read high and low 32 bits (unsigned) in big-endian order
        long hi32 = 0;
        long lo32 = 0;
        for (int i = 0; i < 4; i++) {
            hi32 = (hi32 << 8) | (data[offset + i] & 0xFF);
        }
        for (int i = 4; i < 8; i++) {
            lo32 = (lo32 << 8) | (data[offset + i] & 0xFF);
        }

        // Combine the high and low parts
        long result = (hi32 << 32) | lo32;

        // Warning: Magnitude greater than `2^52` may not be accurately represented in JavaScript
        if (result > MAX_SAFE_INTEGER) {
            System.out.println("Warning: The value exceeds JavaScript's maximum safe integer representation (2^52).");
        }

        return result;
    }

    /**
     * Encodes an unsigned 64-bit integer into a byte array in big-endian format.
     *
     * @param value The unsigned 64-bit integer to encode.
     * @return The encoded byte array.
     */
    @Override
    public byte[] encode(Long value) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("Value cannot be null or negative for unsigned encoding.");
        }

        byte[] data = new byte[8]; // Allocate memory for a 64-bit integer.

        // Split into high and low parts
        long hi32 = (value >> 32) & UINT32_MASK; // High 32 bits
        long lo32 = value & UINT32_MASK;         // Low 32 bits

        // Write high 32 bits into the array in big-endian order
        for (int i = 0; i < 4; i++) {
            data[i] = (byte) ((hi32 >> (24 - (i * 8))) & 0xFF);
        }

        // Write low 32 bits into the array in big-endian order
        for (int i = 4; i < 8; i++) {
            data[i] = (byte) ((lo32 >> (24 - ((i - 4) * 8))) & 0xFF);
        }

        return data;
    }
}
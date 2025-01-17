package net.deanly.structlayout.type.impl;

import net.deanly.structlayout.Layout;

public class UInt64LELayout extends Layout<Long> {

    private static final long MAX_SAFE_INTEGER = 1L << 52; // 2^52
    private static final long UINT32_MASK = 0xFFFFFFFFL;

    /**
     * Constructs a UInt64 layout for unsigned 64-bit integers in little-endian format.
     *
     * @param property (Optional) Property name associated with this layout.
     */
    public UInt64LELayout(String property) {
        super(8, property);
    }

    public UInt64LELayout() {
        this(null);
    }

    /**
     * Decodes an unsigned 64-bit integer from a byte array in little-endian format.
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

        // Read lower and higher 32 bits (unsigned) in little-endian order
        long lo32 = 0;
        long hi32 = 0;
        for (int i = 0; i < 4; i++) {
            lo32 |= (long) (data[offset + i] & 0xFF) << (8 * i);
        }
        for (int i = 4; i < 8; i++) {
            hi32 |= (long) (data[offset + i] & 0xFF) << (8 * (i - 4));
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
     * Encodes an unsigned 64-bit integer into a byte array in little-endian format.
     *
     * @param value The unsigned 64-bit integer to encode.
     * @return The encoded byte array.
     */
    @Override
    public byte[] encode(Long value) {
        if (value == null || value < 0) { // Unsigned integers cannot be negative
            throw new IllegalArgumentException("Value cannot be null or negative for unsigned encoding.");
        }

        byte[] data = new byte[8]; // Always 8 bytes for 64-bit encoding.

        // Split into high and low parts
        long lo32 = value & UINT32_MASK;   // Low 32 bits
        long hi32 = (value >> 32) & UINT32_MASK; // High 32 bits

        // Write low 32 bits into the array in little-endian order
        for (int i = 0; i < 4; i++) {
            data[i] = (byte) (lo32 & 0xFF);
            lo32 >>= 8;
        }

        // Write high 32 bits into the array in little-endian order
        for (int i = 4; i < 8; i++) {
            data[i] = (byte) (hi32 & 0xFF);
            hi32 >>= 8;
        }

        return data;
    }
}
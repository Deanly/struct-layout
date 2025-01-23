package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.Field;

public class Int64BEField extends Field<Long> implements CountableType {

    private static final long MAX_SAFE_INTEGER = 1L << 52; // 2^52
    private static final long UINT32_MASK = 0xFFFFFFFFL;

    /**
     * Constructs a Int64BE layout for signed 64-bit integers in big-endian format.
     *
     * @param property (Optional) Property name associated with this layout.
     */
    public Int64BEField(String property) {
        super(8, property);
    }

    public Int64BEField() {
        this(null);
    }

    /**
     * Decode a signed 64-bit integer from the given byte array in big-endian format.
     *
     * @param data   The byte array containing the data.
     * @param offset The offset where decoding starts.
     * @return The decoded signed integer as a long.
     */
    @Override
    public Long decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (offset < 0 || offset + 8 > data.length) {
            throw new IllegalArgumentException("Invalid offset or insufficient data length.");
        }

        // Combine the higher 32 bits and lower 32 bits in big-endian order
        int hi32 =
                ((data[offset] & 0xFF) << 24) |
                        ((data[offset + 1] & 0xFF) << 16) |
                        ((data[offset + 2] & 0xFF) << 8) |
                        (data[offset + 3] & 0xFF); // High 32 bits (signed)

        long lo32 =
                ((long)(data[offset + 4] & 0xFF) << 24) |
                        ((long)(data[offset + 5] & 0xFF) << 16) |
                        ((long)(data[offset + 6] & 0xFF) << 8) |
                        (long)(data[offset + 7] & 0xFF); // Low 32 bits (unsigned)

        // Combine high and low bits into a signed 64-bit integer
        long result = ((long) hi32 << 32) | lo32;

        // Warning if magnitude exceeds the JavaScript safe integer limit
        if (Math.abs(result) > MAX_SAFE_INTEGER) {
            System.out.println("Warning: The value exceeds JavaScript's maximum safe integer representation (2^52).");
        }
        return result;
    }

    /**
     * Encodes a signed 64-bit integer into a byte array in big-endian format.
     *
     * @param value The signed 64-bit integer to encode.
     * @return The encoded byte array.
     */
    @Override
    public byte[] encode(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }

        byte[] data = new byte[8]; // Allocate 8 bytes for the integer.

        // Write bytes in big-endian order
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (value & 0xFF);
            value >>= 8;
        }

        return data;
    }

}
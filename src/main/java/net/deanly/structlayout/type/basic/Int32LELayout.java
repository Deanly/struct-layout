package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.Layout;

public class Int32LELayout extends Layout<Integer> {

    public Int32LELayout(String property) {
        super(4, property); // 4 bytes
    }

    public Int32LELayout() {
        this(null);
    }

    @Override
    public Integer decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative. Given offset: " + offset);
        }
        if (data.length - offset < getSpan()) {
            throw new IllegalArgumentException(
                    "Data length is insufficient for decoding. Required: " + getSpan() + " bytes, but available: " + (data.length - offset) + " bytes from offset " + offset
            );
        }

        int result = 0;
        for (int i = 0; i < getSpan(); i++) {
            result |= (data[offset + i] & 0xFF) << (8 * i); // Little-Endian 처리
        }
        return result;
    }

    /**
     * Encode a 32-bit signed integer into a byte array in little-endian format.
     *
     * @param value The integer value to encode.
     * @return The encoded byte array.
     */
    @Override
    public byte[] encode(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }

        byte[] data = new byte[getSpan()];

        for (int i = 0; i < getSpan(); i++) {
            data[i] = (byte) ((value >> (8 * i)) & 0xFF); // Little-endian 처리
        }
        return data;
    }
}
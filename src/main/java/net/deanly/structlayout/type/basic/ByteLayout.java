package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.Layout;

/**
 * Represents a layout for encoding and decoding a single byte value.
 * This layout operates on 1-byte values, maintaining their signed representation.
 */
public class ByteLayout extends Layout<Byte> {

    public ByteLayout(String property) {
        super(1, property); // 1 바이트
    }

    public ByteLayout() {
        this(null);
    }

    @Override
    public Byte decode(byte[] data, int offset) {
        if (data == null || offset < 0 || offset >= data.length) {
            throw new IllegalArgumentException("Invalid offset or data array.");
        }
        return data[offset]; // 부호를 유지한 Byte 반환
    }

    @Override
    public byte[] encode(Byte value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        return new byte[]{value}; // 그대로 1 바이트
    }
}
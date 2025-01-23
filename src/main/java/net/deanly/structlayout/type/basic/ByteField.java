package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.Field;

/**
 * Represents a layout for encoding and decoding a single byte value.
 * This layout operates on 1-byte values, maintaining their signed representation.
 */
public class ByteField extends Field<Byte> implements BasicType {
    public static final Class<Byte> GENETIC_CLASS = Byte.class;

    public ByteField(String property) {
        super(1, property); // 1 바이트
    }

    public ByteField() {
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
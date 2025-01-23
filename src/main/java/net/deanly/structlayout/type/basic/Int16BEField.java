package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.Field;

public class Int16BEField extends Field<Short> implements CountableType {

    public Int16BEField(String property) {
        super(2, property); // 2 bytes
    }

    public Int16BEField() {
        this(null);
    }

    @Override
    public Short decode(byte[] data, int offset) {
        if (data == null || offset + getSpan() > data.length) {
            throw new IllegalArgumentException("Data is invalid or offset exceeds length.");
        }
        return (short) (((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF)); // Big-endian
    }

    @Override
    public byte[] encode(Short value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        return new byte[]{
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        }; // Big-endian
    }
}
package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.Layout;

public class Int16BELayout extends Layout<Short> {

    public Int16BELayout(String property) {
        super(2, property); // 2 bytes
    }

    public Int16BELayout() {
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
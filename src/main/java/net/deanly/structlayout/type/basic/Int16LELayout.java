package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.Layout;

public class Int16LELayout extends Layout<Short> {

    public Int16LELayout(String property) {
        super(2, property); // 2 bytes
    }

    public Int16LELayout() {
        this(null);
    }

    @Override
    public Short decode(byte[] data, int offset) {
        if (data == null || offset + getSpan() > data.length) {
            throw new IllegalArgumentException("Data is invalid or offset exceeds length.");
        }
        return (short) ((data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8)); // Little-endian
    }

    @Override
    public byte[] encode(Short value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        return new byte[]{
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF)
        }; // Little-endian
    }
}
package net.deanly.structlayout.type.impl;

import net.deanly.structlayout.Layout;

public class UInt16LELayout extends Layout<Integer> {

    public UInt16LELayout(String property) {
        super(2, property); // 2 bytes
    }

    public UInt16LELayout() {
        this(null);
    }

    @Override
    public Integer decode(byte[] data, int offset) {
        if (data == null || offset + getSpan() > data.length) {
            throw new IllegalArgumentException("Data is invalid or offset exceeds length.");
        }
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8); // Little-endian
    }

    @Override
    public byte[] encode(Integer value) {
        if (value == null || value < 0 || value > 65535) {
            throw new IllegalArgumentException("Value must be in range 0 to 65535.");
        }
        return new byte[]{
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF)
        };
    }
}
package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.Layout;

public class UCharCLayout extends Layout<Integer> {

    public UCharCLayout(String property) {
        super(1, property);
    }

    public UCharCLayout() {
        this(null);
    }

    @Override
    public Integer decode(byte[] data, int offset) {
        return data[offset] & 0xFF;
    }

    @Override
    public byte[] encode(Integer value) {
        if (value == null || value < 0 || value > 255) {
            throw new IllegalArgumentException("Value must be in the range 0 to 255.");
        }
        return new byte[]{value.byteValue()};
    }
}
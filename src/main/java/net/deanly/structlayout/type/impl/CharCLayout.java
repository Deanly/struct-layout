package net.deanly.structlayout.type.impl;

import net.deanly.structlayout.Layout;

public class CharCLayout extends Layout<Byte> {

    public CharCLayout(String property) {
        super(1, property);
    }

    public CharCLayout() {
        this(null);
    }

    @Override
    public Byte decode(byte[] data, int offset) {
        return data[offset];
    }

    @Override
    public byte[] encode(Byte value) {
        return new byte[]{value};
    }
}
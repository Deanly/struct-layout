package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;

public class CharCField extends FieldBase<Byte> implements BasicType {

    public CharCField() {
        super(1, Byte.class);
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
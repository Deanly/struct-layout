package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;

public class CharCField extends FieldBase<Byte> implements BasicType {

    public CharCField(String property) {
        super(1, property);
    }

    public CharCField() {
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
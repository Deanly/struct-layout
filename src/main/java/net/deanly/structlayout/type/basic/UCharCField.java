package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;

public class UCharCField extends FieldBase<Integer> implements BasicType {

    public UCharCField() {
        super(1, Integer.class);
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
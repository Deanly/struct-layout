package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.CountableField;

public class Int8Field extends FieldBase<Short> implements CountableField<Short> {

    public Int8Field() {
        super(1, Short.class); // 1 byte
    }

    @Override
    public Short decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        return (short) data[offset]; // 데이터 자체가 부호 있는 8비트 정수
    }

    @Override
    public byte[] encode(Short value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        return new byte[]{(byte) (value & 0xFF)};
    }

}
package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.CountableField;

public class UInt8Field extends FieldBase<Short> implements CountableField<Short> {

    public UInt8Field() {
        super(1, Short.class); // 1 byte
    }

    @Override
    public Short decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        return (short) (data[offset] & 0xFF); // 부호 없는 8비트 정수
    }

    @Override
    public byte[] encode(Short value) {
        if (value == null || value < 0 || value > 255) {
            throw new IllegalArgumentException("Value must be in the range 0 to 255.");
        }
        return new byte[]{value.byteValue()}; // 1바이트로 인코딩
    }

}
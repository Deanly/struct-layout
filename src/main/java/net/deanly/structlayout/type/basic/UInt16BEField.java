package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.CountableField;

public class UInt16BEField extends FieldBase<Integer> implements CountableField<Integer> {

    public UInt16BEField() {
        super(2, Integer.class); // 2 bytes
    }

    @Override
    public Integer decode(byte[] data, int offset) {
        if (data == null || offset + getSpan() > data.length) {
            throw new IllegalArgumentException("Data is invalid or offset exceeds length.");
        }
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF); // Big-endian
    }

    @Override
    public byte[] encode(Integer value) {
        if (value == null || value < 0 || value > 65535) {
            throw new IllegalArgumentException("Value must be in range 0 to 65535.");
        }
        return new byte[]{
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

}
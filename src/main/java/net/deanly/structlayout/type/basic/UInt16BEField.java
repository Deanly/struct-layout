package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.Field;

public class UInt16BEField extends Field<Integer> implements CountableType {

    public UInt16BEField(String property) {
        super(2, property); // 2 bytes
    }

    public UInt16BEField() {
        this(null);
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
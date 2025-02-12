package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.CountableField;

import net.deanly.structlayout.type.guava.UnsignedLong;

public class UInt64LEField extends FieldBase<UnsignedLong> implements CountableField<UnsignedLong> {

    public static final UnsignedLong UINT64_MAX = UnsignedLong.MAX_VALUE;

    public UInt64LEField() {
        super(8, UnsignedLong.class);
    }

    @Override
    public UnsignedLong decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (offset < 0 || offset + getSpan() > data.length) {
            throw new IllegalArgumentException("Invalid offset or insufficient data length.");
        }

        long result = 0;
        for (int i = 0; i < 8; i++) {
            result |= (long) (data[offset + i] & 0xFF) << (i * 8);
        }

        return UnsignedLong.fromLongBits(result);
    }

    @Override
    public byte[] encode(UnsignedLong value) {
        if (value == null || value.compareTo(UnsignedLong.ZERO) < 0 || value.compareTo(UINT64_MAX) > 0) {
            throw new IllegalArgumentException("Value must not be negative or exceed unsigned 64-bit integer range. " + value);
        }

        byte[] data = new byte[8];
        long temp = value.longValue();
        for (int i = 0; i < 8; i++) {
            data[i] = (byte) (temp & 0xFF);
            temp >>= 8;
        }

        return data;
    }
}

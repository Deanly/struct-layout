package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.CountableField;

public class Int16LEField extends FieldBase<Short> implements CountableField<Short> {

    public Int16LEField() {
        super(2, Short.class); // 2 bytes
    }

    @Override
    public Short decode(byte[] data, int offset) {
        if (data == null || offset + getSpan() > data.length) {
            throw new IllegalArgumentException("Data is invalid or offset exceeds length.");
        }
        return (short) ((data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8)); // Little-endian
    }

    @Override
    public byte[] encode(Short value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        return new byte[]{
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF)
        }; // Little-endian
    }

}
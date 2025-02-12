package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;

import java.util.Arrays;

/**
 * Represents a field that handles serialization and deserialization of 5-byte arrays.
 * This class ensures that the data being processed is exactly 5 bytes in length.
 * It extends the functionality of the FieldBase class for specialized handling of 5-byte fields.
 */
public class Bytes5Field extends FieldBase<byte[]> {
    public Bytes5Field() {
        super(5, byte[].class); // 5 바이트 처리
    }

    @Override
    public byte[] decode(byte[] data, int offset) {
        validateLength(data, offset);
        return Arrays.copyOfRange(data, offset, offset + 5);
    }

    @Override
    public byte[] encode(byte[] value) {
        if (value == null || value.length != 5) {
            throw new IllegalArgumentException("Value must be exactly 5 bytes.");
        }
        return value;
    }
}

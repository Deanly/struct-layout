package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;

import java.util.Arrays;

/**
 * A field class designed to handle 4-byte serialization and deserialization.
 * This class provides methods to encode and decode 4-byte arrays, ensuring the
 * input and output values conform to the expected fixed-length 4 bytes.
 */
public class Bytes4Field extends FieldBase<byte[]> {
    public Bytes4Field() {
        super(4, byte[].class); // 4 바이트 처리
    }

    @Override
    public byte[] decode(byte[] data, int offset) {
        validateLength(data, offset);
        return Arrays.copyOfRange(data, offset, offset + 4);
    }

    @Override
    public byte[] encode(byte[] value) {
        if (value == null || value.length != 4) {
            throw new IllegalArgumentException("Value must be exactly 4 bytes.");
        }
        return value;
    }
}

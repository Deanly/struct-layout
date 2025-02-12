package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;

import java.util.Arrays;

/**
 * A field class designed to handle 3-byte serialization and deserialization.
 * This class provides methods to encode and decode 3-byte arrays, ensuring the
 * input and output values conform to the expected fixed-length 3 bytes.
 */
public class Bytes3Field extends FieldBase<byte[]> {
    public Bytes3Field() {
        super(3, byte[].class); // 3 바이트 처리
    }

    @Override
    public byte[] decode(byte[] data, int offset) {
        validateLength(data, offset);
        return Arrays.copyOfRange(data, offset, offset + 3);
    }

    @Override
    public byte[] encode(byte[] value) {
        if (value == null || value.length != 3) {
            throw new IllegalArgumentException("Value must be exactly 3 bytes.");
        }
        return value;
    }
}
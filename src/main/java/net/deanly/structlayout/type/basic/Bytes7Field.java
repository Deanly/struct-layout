package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;

import java.util.Arrays;

/**
 * A field class designed to handle 7-byte serialization and deserialization.
 * This class provides methods to encode and decode 7-byte arrays, ensuring the
 * input and output values conform to the expected fixed-length 7 bytes.
 */
public class Bytes7Field extends FieldBase<byte[]> {
    public Bytes7Field() {
        super(7, byte[].class); // 7 바이트 처리
    }

    @Override
    public byte[] decode(byte[] data, int offset) {
        validateLength(data, offset);
        return Arrays.copyOfRange(data, offset, offset + 7);
    }

    @Override
    public byte[] encode(byte[] value) {
        if (value == null || value.length != 7) {
            throw new IllegalArgumentException("Value must be exactly 7 bytes.");
        }
        return value;
    }
}

package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;

import java.util.Arrays;

/**
 * A field class designed to handle 2-byte serialization and deserialization.
 * This class provides methods to encode and decode 2-byte arrays, ensuring the
 * input and output values conform to the expected fixed-length 2 bytes.
 */
public class Bytes2Field extends FieldBase<byte[]> {
    public Bytes2Field() {
        super(2, byte[].class); // 2 바이트 처리
    }

    @Override
    public byte[] decode(byte[] data, int offset) {
        validateLength(data, offset);
        return Arrays.copyOfRange(data, offset, offset + 2);
    }

    @Override
    public byte[] encode(byte[] value) {
        if (value == null || value.length != 2) {
            throw new IllegalArgumentException("Value must be exactly 2 bytes.");
        }
        return value;
    }
}
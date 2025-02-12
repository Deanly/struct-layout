package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;

import java.util.Arrays;

/**
 * A field class designed to handle 8-byte serialization and deserialization.
 * This class provides methods to encode and decode 8-byte arrays, ensuring the
 * input and output values conform to the expected fixed-length 8 bytes.
 */
public class Bytes8Field extends FieldBase<byte[]> {
    public Bytes8Field() {
        super(8, byte[].class); // 8 바이트 처리
    }

    @Override
    public byte[] decode(byte[] data, int offset) {
        validateLength(data, offset);
        return Arrays.copyOfRange(data, offset, offset + 8);
    }

    @Override
    public byte[] encode(byte[] value) {
        if (value == null || value.length != 8) {
            throw new IllegalArgumentException("Value must be exactly 8 bytes.");
        }
        return value;
    }
}

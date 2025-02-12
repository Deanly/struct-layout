package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;

import java.util.Arrays;

/**
 * The Bytes6Field class represents a field that handles a fixed size of 6 bytes.
 * It provides methods to decode and encode byte array values specifically constrained to 6 bytes.
 * This class extends FieldBase to enforce the behavior of binary data handling.
 */
public class Bytes6Field extends FieldBase<byte[]> {
    public Bytes6Field() {
        super(6, byte[].class); // 6 바이트 처리
    }

    @Override
    public byte[] decode(byte[] data, int offset) {
        validateLength(data, offset);
        return Arrays.copyOfRange(data, offset, offset + 6);
    }

    @Override
    public byte[] encode(byte[] value) {
        if (value == null || value.length != 6) {
            throw new IllegalArgumentException("Value must be exactly 6 bytes.");
        }
        return value;
    }
}

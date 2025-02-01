package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.CountableField;

import java.math.BigInteger;

public class UInt64LEField extends FieldBase<BigInteger> implements CountableField<BigInteger> {

    public static final BigInteger UINT64_MAX = new BigInteger("18446744073709551615");

    public UInt64LEField() {
        super(8, BigInteger.class);
    }

    @Override
    public BigInteger decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (offset < 0 || offset + getSpan() > data.length) {
            throw new IllegalArgumentException("Invalid offset or insufficient data length.");
        }

        // Read 8 bytes in little-endian order into BigInteger
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < 8; i++) {
            result = result.or(BigInteger.valueOf(data[offset + i] & 0xFF).shiftLeft(i * 8));
        }

        return result;
    }

    @Override
    public byte[] encode(BigInteger value) {
        if (value == null || value.compareTo(BigInteger.ZERO) < 0 || value.compareTo(UINT64_MAX) > 0) {
            throw new IllegalArgumentException("Value must not be negative or exceed unsigned 64-bit integer range." + value);
        }

        byte[] data = new byte[8]; // Allocate memory for a 64-bit integer

        // Encode in little-endian order
        BigInteger temp = value;
        for (int i = 0; i < 8; i++) {
            data[i] = temp.byteValue();
            temp = temp.shiftRight(8);
        }

        return data;
    }

}
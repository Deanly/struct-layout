package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.Layout;
import java.math.BigInteger;

public class UInt64BELayout extends Layout<BigInteger> {

    private static final BigInteger UINT64_MAX = new BigInteger("18446744073709551615");

    public UInt64BELayout(String property) {
        super(8, property);
    }

    public UInt64BELayout() {
        this(null);
    }

    @Override
    public BigInteger decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (offset < 0 || offset + getSpan() > data.length) {
            throw new IllegalArgumentException("Invalid offset or insufficient data length.");
        }

        // Read 8 bytes in big-endian order into BigInteger
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < 8; i++) {
            result = result.shiftLeft(8).or(BigInteger.valueOf(data[offset + i] & 0xFF));
        }

        return result;
    }

    @Override
    public byte[] encode(BigInteger value) {
        if (value == null || value.compareTo(BigInteger.ZERO) < 0 || value.compareTo(UINT64_MAX) > 0) {
            throw new IllegalArgumentException("Value must not be negative or exceed unsigned 64-bit integer range.");
        }

        byte[] data = new byte[8];

        // Encode in big-endian order
        BigInteger temp = value;
        for (int i = 7; i >= 0; i--) {
            data[i] = temp.byteValue();
            temp = temp.shiftRight(8);
        }

        return data;
    }
}
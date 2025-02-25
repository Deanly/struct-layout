package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.CountableField;

public class Int32BEField extends FieldBase<Integer> implements CountableField<Integer> {

    public Int32BEField() {
        super(4, Integer.class); // 4 bytes
    }

    @Override
    public Integer decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative. Given offset: " + offset);
        }
        if (data.length - offset < getSpan()) {
            throw new IllegalArgumentException(
                    "Data length is insufficient for decoding. Required: " + getSpan() + " bytes, but available: " + (data.length - offset) + " bytes from offset " + offset
            );
        }

        int result = 0;
        for (int i = 0; i < getSpan(); i++) {
            result = (result << 8) | (data[offset + i] & 0xFF); // Big-Endian 처리
        }
        return result;
    }

    /**
     * Encodes a 32-bit signed integer into a byte array in big-endian format.
     *
     * @param value The integer value to encode.
     * @return The encoded byte array.
     */
    @Override
    public byte[] encode(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }

        byte[] data = new byte[getSpan()]; // Enforces size of 4 bytes for 32-bit integer.

        for (int i = getSpan() - 1; i >= 0; i--) {
            data[i] = (byte) (value & 0xFF); // Write each byte in big-endian order.
            value >>= 8;                     // Shift value by 8 bits for the next byte.
        }
        return data;
    }

}
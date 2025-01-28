package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;

public class Float64BEField extends FieldBase<Double> implements BasicType {

    /**
     * Constructs a layout for a 64-bit floating-point number in big-endian format.
     */
    public Float64BEField() {
        super(8, Double.class);
    }

    /**
     * Decode a 64-bit floating-point number from the given byte array in big-endian format.
     *
     * @param data   The byte array containing the data.
     * @param offset The offset where decoding starts.
     * @return The decoded double value.
     */
    @Override
    public Double decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (offset < 0 || offset + 8 > data.length) {
            throw new IllegalArgumentException("Invalid offset or insufficient data length.");
        }

        // Combine the bytes in big-endian order to create a long representation of the double
        long longBits =
                ((long) (data[offset] & 0xFF) << 56) |
                        ((long) (data[offset + 1] & 0xFF) << 48) |
                        ((long) (data[offset + 2] & 0xFF) << 40) |
                        ((long) (data[offset + 3] & 0xFF) << 32) |
                        ((long) (data[offset + 4] & 0xFF) << 24) |
                        ((long) (data[offset + 5] & 0xFF) << 16) |
                        ((long) (data[offset + 6] & 0xFF) << 8) |
                        ((long) (data[offset + 7] & 0xFF));

        // Convert the long bits to a double
        return Double.longBitsToDouble(longBits);
    }

    /**
     * Encodes a 64-bit floating-point number into a byte array in big-endian format.
     *
     * @param value The double value to encode.
     * @return The encoded byte array.
     */
    @Override
    public byte[] encode(Double value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }

        byte[] data = new byte[8]; // Allocate 8 bytes for the 64-bit double.

        // Convert the double to its raw long bits.
        long longBits = Double.doubleToLongBits(value);

        // Write each byte of the long into the array in big-endian order.
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (longBits & 0xFF); // Extract the least significant byte.
            longBits >>= 8;                     // Shift the bits for the next byte.
        }

        return data;
    }

}
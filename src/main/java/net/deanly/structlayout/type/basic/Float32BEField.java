package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.type.FieldBase;

public class Float32BEField extends FieldBase<Float> implements BasicType {

    /**
     * Constructs a layout for a 32-bit floating-point number in big-endian format.
     *
     * @param property (Optional) The property name associated with this layout.
     */
    public Float32BEField(String property) {
        super(4, property);
    }

    public Float32BEField() {
        this(null);
    }

    /**
     * Decode a 32-bit floating-point number from the given byte array in big-endian format.
     *
     * @param data   The byte array containing the data.
     * @param offset The offset where decoding starts.
     * @return The decoded float value.
     */
    @Override
    public Float decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (offset < 0 || offset + 4 > data.length) {
            throw new IllegalArgumentException("Invalid offset or insufficient data length.");
        }

        // Combine the bytes in big-endian order to create an integer representation of the float
        int intBits =
                ((data[offset] & 0xFF) << 24) |
                        ((data[offset + 1] & 0xFF) << 16) |
                        ((data[offset + 2] & 0xFF) << 8) |
                        (data[offset + 3] & 0xFF);

        // Convert the integer bits to a float
        return Float.intBitsToFloat(intBits);
    }

    /**
     * Encode a 32-bit floating-point number into a byte array in big-endian format.
     *
     * @param value The float value to encode.
     * @return The encoded byte array.
     */
    @Override
    public byte[] encode(Float value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        if (value > Float.MAX_VALUE || value < -Float.MAX_VALUE) {
            throw new IllegalArgumentException(
                    String.format("Value %s exceeds the range of Float32 (Valid range: [%s, %s])",
                            value, -Float.MAX_VALUE, Float.MAX_VALUE)
            );
        }

        byte[] data = new byte[4];

        // Convert the float to its raw integer bits
        int intBits = Float.floatToIntBits(value);

        // Write the integer bits into the byte array in big-endian order
        data[0] = (byte) ((intBits >> 24) & 0xFF);
        data[1] = (byte) ((intBits >> 16) & 0xFF);
        data[2] = (byte) ((intBits >> 8) & 0xFF);
        data[3] = (byte) (intBits & 0xFF);

        return data;
    }

}
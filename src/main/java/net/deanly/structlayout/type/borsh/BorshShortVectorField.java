package net.deanly.structlayout.type.borsh;

import net.deanly.structlayout.type.DynamicSpanField;
import net.deanly.structlayout.type.CountableField;
import net.deanly.structlayout.type.FieldBase;

/**
 * Represents a Borsh ShortVector field, used to encode the length of an array
 * or vector in variable-length encoding (VLE) format. This is a standard
 * method in the Borsh specification for prefixing array data with its length.
 * <p>
 * Key Characteristics:
 * - Uses 1 to 5 bytes to encode an integer length.
 * - Efficient for small lengths, as values <= 127 use only 1 byte.
 * - Commonly used in Solana and Anchor for handling array lengths.
 */
public class BorshShortVectorField extends FieldBase<Integer> implements DynamicSpanField, CountableField<Integer> {

    private int dynamicSpan;

    public BorshShortVectorField() {
        super(0, Integer.class); // Default span is dynamically calculated.
    }

    /**
     * Encodes an integer length into variable-length encoding (VLE) format.
     *
     * @param value The length to encode.
     * @return The encoded length as a byte array.
     */
    @Override
    public byte[] encode(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null for VLE encoding.");
        }
        byte[] encoded = this.encodeLength(value);
        this.dynamicSpan = encoded.length;
        return encoded;
    }

    /**
     * Decodes a variable-length encoded length from a byte array.
     *
     * @param bytes  The data array containing the encoded length.
     * @param offset The offset to start decoding from.
     * @return The decoded length as an integer.
     */
    @Override
    public Integer decode(byte[] bytes, int offset) {
        if (bytes == null) {
            throw new IllegalArgumentException("Data cannot be null for VLE decoding.");
        }
        if (offset < 0 || offset >= bytes.length) {
            throw new IllegalArgumentException("Invalid offset for VLE decoding.");
        }

        int length = this.decodeLength(bytes, offset);

        int byteCount = calculateEncodedLength(bytes, offset);
        this.dynamicSpan = byteCount;

        return length;
    }

    /**
     * Calculates the number of bytes used to encode the length.
     *
     * @param bytes  The data array.
     * @param offset The offset to start calculating from.
     * @return The number of bytes used to encode the length.
     */
    private int calculateEncodedLength(byte[] bytes, int offset) {
        int span = 0;
        while (offset + span < bytes.length) {
            if ((bytes[offset + span] & 0x80) == 0) {
                span++;
                break;
            }
            span++;
        }
        return span;
    }

    /**
     * Calculates the dynamic span (in bytes) of the encoded length.
     *
     * @param data   The data array.
     * @param offset The offset to start calculating from.
     * @return The span of the encoded length.
     */
    @Override
    public int calculateSpan(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null for span calculation.");
        }
        if (offset < 0 || offset >= data.length) {
            throw new IllegalArgumentException("Invalid offset for span calculation.");
        }
        return calculateEncodedLength(data, offset);
    }

    /**
     * Returns the span (in bytes) of the encoded length when no data is provided.
     *
     * @return The span of an encoded length (minimum 1 byte).
     */
    @Override
    public int getNoDataSpan() {
        return encodeLength(0).length;
    }

    /**
     * Returns the dynamically calculated span of the last encoded/decoded value.
     *
     * @return The span of the last encoded/decoded value.
     */
    @Override
    public int getSpan() {
        return this.dynamicSpan;
    }

    /**
     * Encodes an integer length into variable-length encoding (VLE) format.
     *
     * @param length The length to encode.
     * @return The encoded length as a byte array.
     */
    private byte[] encodeLength(int length) {
        byte[] buffer = new byte[5];
        int i = 0;
        while (length > 127) {
            buffer[i++] = (byte) ((length & 0x7F) | 0x80);
            length >>>= 7;
        }
        buffer[i++] = (byte) (length & 0x7F);
        byte[] result = new byte[i];
        System.arraycopy(buffer, 0, result, 0, i);
        return result;
    }

    /**
     * Decodes a variable-length encoded length from a byte array.
     *
     * @param bytes  The data array.
     * @param offset The offset to start decoding from.
     * @return The decoded length as an integer.
     */
    private int decodeLength(byte[] bytes, int offset) {
        int length = 0;
        int shift = 0;
        for (int i = offset; i < bytes.length; i++) {
            byte b = bytes[i];
            length |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                break;
            }
            shift += 7;
        }
        return length;
    }
}
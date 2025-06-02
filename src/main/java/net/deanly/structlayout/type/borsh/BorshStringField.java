package net.deanly.structlayout.type.borsh;

import net.deanly.structlayout.type.DynamicSpanField;
import net.deanly.structlayout.type.FieldBase;

import java.nio.charset.StandardCharsets;

/**
 * The {@code BorshStringField} class represents a string field that encodes and decodes
 * string data according to the Borsh (Binary Object Representation Serializer for Hashing) specification.
 * This class implements the {@link DynamicSpanField} interface, allowing for the handling
 * of dynamically sized string fields where the string length is determined at runtime.
 *
 * <p>The encoded string format consists of:
 * <ul>
 *   <li>A 4-byte little-endian integer representing the length of the string.</li>
 *   <li>The UTF-8 encoded string bytes.</li>
 * </ul>
 *
 * <p>This class extends {@link FieldBase} with a type parameter of {@code String}
 * and provides methods to encode strings into byte data and decode byte data back into strings.
 * It also supports calculating the variable span (length) of the encoded string.
 */
public class BorshStringField extends FieldBase<String> implements DynamicSpanField {
    public BorshStringField() {
        super(-1, String.class);
    }

    @Override
    public String decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (offset < 0 || offset + 4 > data.length) {
            throw new IllegalArgumentException("Offset out of bounds. offset=" + offset + ", data.length=" + data.length);
        }

        // 문자열 길이 읽기 (Little-Endian u32)
        int length = ((data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8) |
                ((data[offset + 2] & 0xFF) << 16) | ((data[offset + 3] & 0xFF) << 24));
        offset += 4;

        if (length < 0 || length > data.length - offset) {
            throw new IllegalArgumentException(String.format(
                    "Invalid string length in Borsh decoding. length=%d, offset(after header)=%d, data.length=%d, headerOffset=%d",
                    length, offset, data.length, offset - 4
            ));
        }

        // 문자열 데이터 읽기
        if (length == 0) {
            return "";
        }

        return new String(data, offset, length, StandardCharsets.UTF_8).replaceAll("\u0000+$", "");
    }

    @Override
    public byte[] encode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("String value cannot be null");
        }

        byte[] stringBytes = value.getBytes(StandardCharsets.UTF_8);
        int length = stringBytes.length;

        byte[] result = new byte[4 + length];

        // 문자열 길이 기록 (Little-Endian u32)
        result[0] = (byte) (length & 0xFF);
        result[1] = (byte) ((length >> 8) & 0xFF);
        result[2] = (byte) ((length >> 16) & 0xFF);
        result[3] = (byte) ((length >> 24) & 0xFF);

        // 문자열 데이터 기록
        System.arraycopy(stringBytes, 0, result, 4, length);

        return result;
    }

    @Override
    public int getSpan() {
        // Dynamic span: -1을 반환하면 안되므로 실제 길이를 계산해야 함
        throw new UnsupportedOperationException("DynamicStringField requires data to calculate span.");
    }

    @Override
    public int calculateSpan(byte[] data, int offset) {
        // 문자열 길이 읽기 (Little-Endian u32)
        int length = ((data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8) |
                ((data[offset + 2] & 0xFF) << 16) | ((data[offset + 3] & 0xFF) << 24));
        return 4 + length; // 길이 필드(4 bytes) + 문자열 데이터 길이
    }

    @Override
    public int getNoDataSpan() {
        return 4; // 0x00000000
    }
}

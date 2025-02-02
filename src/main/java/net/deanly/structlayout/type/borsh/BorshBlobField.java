package net.deanly.structlayout.type.borsh;

import net.deanly.structlayout.type.DynamicSpanField;
import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.basic.BasicType;

public class BorshBlobField extends FieldBase<byte[]> implements BasicType, DynamicSpanField {

    public BorshBlobField() {
        super(-1, byte[].class);
    }

    @Override
    public byte[] encode(byte[] value) {
        if (value == null) {
            value = new byte[0];
        }
        var length = value.length;

        // Encode TLV 구조: [Length][Value]
        byte[] result = new byte[length + 4]; // 4 bytes for length (VarInt)

        // Length를 먼저 기록 (Big-endian)
        result[0] = (byte) (length >> 24);
        result[1] = (byte) (length >> 16);
        result[2] = (byte) (length >> 8);
        result[3] = (byte) length;

        // Value 복사
        System.arraycopy(value, 0, result, 4, length);

        return result;
    }

    @Override
    public byte[] decode(byte[] bytes, int offset) {
        if (bytes == null || bytes.length < offset + 4) {
            throw new IllegalArgumentException("Invalid TLV structure: insufficient data");
        }
        // Length 읽기 (Big-endian)
        var length = ((bytes[offset] & 0xFF) << 24) |
                ((bytes[offset + 1] & 0xFF) << 16) |
                ((bytes[offset + 2] & 0xFF) << 8) |
                (bytes[offset + 3] & 0xFF);

        // Value 데이터가 충분한지 확인
        if (bytes.length < offset + 4 + length) {
            throw new IllegalArgumentException("Invalid TLV structure: insufficient data for value");
        }

        // 빈 데이터 처리
        if (bytes.length == offset + 4 && length == 0) {
            return new byte[0]; // 빈 값 반환
        }

        // Value 읽기
        byte[] value = new byte[length];
        System.arraycopy(bytes, offset + 4, value, 0, length);

        return value;
    }

    @Override
    public int calculateSpan(byte[] data, int offset) {
        return data.length - offset;
    }

    @Override
    public int getNoDataSpan() {
        return 4;
    }

    @Override
    public int getSpan() {
        throw new UnsupportedOperationException("Span cannot be calculated for BorshBlobField");
    }

}
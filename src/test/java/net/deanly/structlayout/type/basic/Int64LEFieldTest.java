package net.deanly.structlayout.type.basic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Int64LEFieldTest {

    @Test
    void testEncodeValidValue() {
        Int64LEField layout = new Int64LEField();

        long value = 1234567890123456789L; // 테스트 값
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded);
        assertEquals(8, encoded.length); // 64비트(8바이트)여야 함
        assertArrayEquals(new byte[]{
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 32) & 0xFF),
                (byte) ((value >> 40) & 0xFF),
                (byte) ((value >> 48) & 0xFF),
                (byte) ((value >> 56) & 0xFF)
        }, encoded); // 리틀 엔디안으로 인코딩 확인
    }

    @Test
    void testDecodeValidData() {
        Int64LEField layout = new Int64LEField();

        long expectedValue = -1234567890123456789L;
        byte[] encodedData = new byte[]{
                (byte) (expectedValue & 0xFF),
                (byte) ((expectedValue >> 8) & 0xFF),
                (byte) ((expectedValue >> 16) & 0xFF),
                (byte) ((expectedValue >> 24) & 0xFF),
                (byte) ((expectedValue >> 32) & 0xFF),
                (byte) ((expectedValue >> 40) & 0xFF),
                (byte) ((expectedValue >> 48) & 0xFF),
                (byte) ((expectedValue >> 56) & 0xFF)
        };

        Long decoded = layout.decode(encodedData, 0);

        assertNotNull(decoded);
        assertEquals(expectedValue, decoded);
    }

    @Test
    void testEncodeAndDecodeConsistency() {
        Int64LEField layout = new Int64LEField();

        long originalValue = 9223372036854775807L; // Long의 최대값
        byte[] encoded = layout.encode(originalValue);
        Long decoded = layout.decode(encoded, 0);

        assertNotNull(decoded); // 결과가 null이 아니어야 함
        assertEquals(originalValue, decoded); // 원래 값과 일치해야 함
    }

    @Test
    void testDecodeWithOffset() {
        Int64LEField layout = new Int64LEField();

        long expectedValue = 1357913579135791L;
        byte[] data = new byte[]{
                0x00, 0x00, 0x00, 0x00, // 더미 데이터
                (byte) (expectedValue & 0xFF),
                (byte) ((expectedValue >> 8) & 0xFF),
                (byte) ((expectedValue >> 16) & 0xFF),
                (byte) ((expectedValue >> 24) & 0xFF),
                (byte) ((expectedValue >> 32) & 0xFF),
                (byte) ((expectedValue >> 40) & 0xFF),
                (byte) ((expectedValue >> 48) & 0xFF),
                (byte) ((expectedValue >> 56) & 0xFF)
        };

        Long decoded = layout.decode(data, 4); // 오프셋 4로 디코딩

        assertNotNull(decoded);
        assertEquals(expectedValue, decoded);
    }

    @Test
    void testEncodeNullValue() {
        Int64LEField layout = new Int64LEField();

        assertThrows(IllegalArgumentException.class, () -> layout.encode(null)); // null 값 예외 처리 확인
    }

    @Test
    void testDecodeInvalidData() {
        Int64LEField layout = new Int64LEField();

        byte[] shortData = new byte[]{0x01, 0x02, 0x03}; // 유효하지 않은 길이 (8바이트 미만)
        assertThrows(IllegalArgumentException.class, () -> layout.decode(shortData, 0)); // 예외 발생 확인
    }
}
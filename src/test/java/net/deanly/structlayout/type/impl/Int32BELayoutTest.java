package net.deanly.structlayout.type.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Int32BELayoutTest {

    @Test
    void testEncodeValidValue() {
        Int32BELayout layout = new Int32BELayout();

        int value = -123456789; // 테스트 값
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded);
        assertEquals(4, encoded.length); // 4바이트여야 함
        assertArrayEquals(new byte[]{
                (byte) ((value >> 24) & 0xFF),      // MSB
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)               // LSB
        }, encoded); // 빅 엔디안으로 인코딩 확인
    }

    @Test
    void testDecodeValidData() {
        Int32BELayout layout = new Int32BELayout();

        int expectedValue = -123456789;
        byte[] encodedData = new byte[]{
                (byte) ((expectedValue >> 24) & 0xFF), // MSB
                (byte) ((expectedValue >> 16) & 0xFF),
                (byte) ((expectedValue >> 8) & 0xFF),
                (byte) (expectedValue & 0xFF)          // LSB
        };

        Integer decoded = layout.decode(encodedData, 0);

        assertNotNull(decoded);
        assertEquals(expectedValue, decoded);
    }

    @Test
    void testEncodeAndDecodeConsistency() {
        Int32BELayout layout = new Int32BELayout();

        int originalValue = 987654321; // 임의의 값
        byte[] encoded = layout.encode(originalValue);
        Integer decoded = layout.decode(encoded, 0);

        assertNotNull(decoded); // 결과가 null이 아니어야 함
        assertEquals(originalValue, decoded); // 원래 값과 일치해야 함
    }

    @Test
    void testDecodeWithOffset() {
        Int32BELayout layout = new Int32BELayout();

        int expectedValue = 24680;
        byte[] data = new byte[]{
                0x00, 0x00, 0x11, 0x22,         // 더미 데이터
                (byte) ((expectedValue >> 24) & 0xFF),
                (byte) ((expectedValue >> 16) & 0xFF),
                (byte) ((expectedValue >> 8) & 0xFF),
                (byte) (expectedValue & 0xFF)
        };

        Integer decoded = layout.decode(data, 4); // 오프셋 4로 디코딩

        assertNotNull(decoded);
        assertEquals(expectedValue, decoded); // 결과 값 확인
    }

    @Test
    void testEncodeNullValue() {
        Int32BELayout layout = new Int32BELayout();

        assertThrows(IllegalArgumentException.class, () -> layout.encode(null)); // null 값 예외 처리 확인
    }

    @Test
    void testDecodeInvalidData() {
        Int32BELayout layout = new Int32BELayout();

        byte[] shortData = new byte[]{0x01, 0x02}; // 유효하지 않은 길이 (4바이트 미만)
        assertThrows(IllegalArgumentException.class, () -> layout.decode(shortData, 0)); // 예외 발생 확인
    }
}
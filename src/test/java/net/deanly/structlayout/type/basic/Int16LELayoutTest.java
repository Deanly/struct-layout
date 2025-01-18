package net.deanly.structlayout.type.basic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Int16LELayoutTest {

    @Test
    void testEncodeValidValue() {
        Int16LELayout layout = new Int16LELayout();

        short value = 32767; // 리틀 엔디안 테스트 값 (16비트 Short)
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded); // 인코딩 결과가 null이 아니어야 함
        assertEquals(2, encoded.length); // 2바이트여야 함
        assertArrayEquals(new byte[]{
                (byte) (value & 0xFF),         // LSB
                (byte) ((value >> 8) & 0xFF)  // MSB
        }, encoded); // 리틀 엔디안 형식 검증
    }

    @Test
    void testDecodeValidData() {
        Int16LELayout layout = new Int16LELayout();

        short expectedValue = 32767;
        byte[] encodedData = new byte[]{
                (byte) (expectedValue & 0xFF),         // LSB
                (byte) ((expectedValue >> 8) & 0xFF)  // MSB
        };

        Short decodedValue = layout.decode(encodedData, 0);

        assertNotNull(decodedValue); // 디코딩 결과가 null이 아니어야 함
        assertEquals(expectedValue, decodedValue); // 값이 일치해야 함
    }

    @Test
    void testEncodeAndDecodeConsistency() {
        Int16LELayout layout = new Int16LELayout();

        short originalValue = -12345;
        byte[] encoded = layout.encode(originalValue);
        Short decoded = layout.decode(encoded, 0);

        assertNotNull(decoded); // 디코딩 결과 확인
        assertEquals(originalValue, decoded); // 인코딩 및 디코딩 후 값이 동일해야 함
    }

    @Test
    void testDecodeWithOffset() {
        Int16LELayout layout = new Int16LELayout();

        short expectedValue = 12345;
        byte[] data = new byte[]{
                0x00, 0x00,                    // Offset 0~1: Dummy bytes
                (byte) (expectedValue & 0xFF), // LSB
                (byte) ((expectedValue >> 8) & 0xFF) // MSB
        };

        Short decodedValue = layout.decode(data, 2);

        assertNotNull(decodedValue); // null이 아니어야 함
        assertEquals(expectedValue, decodedValue); // 값이 일치해야 함
    }

    @Test
    void testEncodeNullValue() {
        Int16LELayout layout = new Int16LELayout();

        assertThrows(IllegalArgumentException.class, () -> layout.encode(null)); // null 입력 테스트
    }

    @Test
    void testDecodeInvalidData() {
        Int16LELayout layout = new Int16LELayout();

        byte[] shortData = new byte[]{0x01}; // 데이터가 너무 짧음
        assertThrows(IllegalArgumentException.class, () -> layout.decode(shortData, 0)); // 예외 발생해야 함
    }
}
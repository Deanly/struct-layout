package net.deanly.structlayout.type.basic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Int8FieldTest {

    @Test
    void testEncodeValidValue() {
        Int8Field layout = new Int8Field();

        Short value = -128; // 부호 있는 8비트 정수 값 (-128 ~ 127)
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded); // 인코딩 결과가 null이 아님
        assertEquals(1, encoded.length); // 크기가 1바이트여야 함
        assertEquals((byte) (value & 0xFF), encoded[0]); // 결과 값 일치 확인
    }

    @Test
    void testDecodeValidData() {
        Int8Field layout = new Int8Field();

        byte[] encodedData = new byte[]{-128}; // -128
        Short decodedValue = layout.decode(encodedData, 0);

        assertNotNull(decodedValue); // 디코딩 결과가 null이 아니어야 함
        assertEquals((short) -128, decodedValue); // 기대 값 확인
    }

    @Test
    void testEncodeAndDecodeConsistency() {
        Int8Field layout = new Int8Field();

        Short originalValue = 127; // 테스트할 값
        byte[] encoded = layout.encode(originalValue);
        Short decoded = layout.decode(encoded, 0);

        assertNotNull(decoded); // 디코딩 결과 확인
        assertEquals(originalValue, decoded); // 원래 값과 동일 확인
    }

    @Test
    void testEncodeNullValue() {
        Int8Field layout = new Int8Field();

        assertThrows(IllegalArgumentException.class, () -> layout.encode(null)); // null 입력
    }

    @Test
    void testDecodeWithOffset() {
        Int8Field layout = new Int8Field();

        byte[] data = new byte[]{0x00, 0x7F, (byte) 0x80}; // 데이터 배열 (0, 127, -128)
        Short decodedValue = layout.decode(data, 2); // 오프셋 2

        assertNotNull(decodedValue); // 디코딩 결과가 null이 아니어야 함
        assertEquals((short) -128, decodedValue); // 기대 값 확인
    }
}
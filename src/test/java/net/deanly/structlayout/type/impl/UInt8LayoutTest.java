package net.deanly.structlayout.type.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UInt8LayoutTest {

    @Test
    void testEncodeValidValue() {
        UInt8Layout layout = new UInt8Layout();

        int value = 200; // 부호 없는 8비트 정수 값 (0~255)
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded); // 인코딩 결과가 null이 아님
        assertEquals(1, encoded.length); // 크기가 1바이트여야 함
        assertEquals((byte) value, encoded[0]); // 결과 값이 정확해야 함
    }

    @Test
    void testDecodeValidData() {
        UInt8Layout layout = new UInt8Layout();

        byte[] encodedData = new byte[]{-56}; // -56은 부호 없는 8비트에서 200
        Integer decodedValue = layout.decode(encodedData, 0);

        assertNotNull(decodedValue); // 디코딩 결과가 null이 아니어야 함
        assertEquals(200, decodedValue); // 기대 값과 동일해야 함
    }

    @Test
    void testEncodeAndDecodeConsistency() {
        UInt8Layout layout = new UInt8Layout();

        int originalValue = 128; // 테스트할 값
        byte[] encoded = layout.encode(originalValue);
        Integer decoded = layout.decode(encoded, 0);

        assertNotNull(decoded); // 디코딩 결과가 null이 아니어야 함
        assertEquals(originalValue, decoded); // 원래 값과 동일해야 함
    }

    @Test
    void testEncodeOutOfRangeValue() {
        UInt8Layout layout = new UInt8Layout();

        assertThrows(IllegalArgumentException.class, () -> layout.encode(-1)); // 음수
        assertThrows(IllegalArgumentException.class, () -> layout.encode(256)); // 범위를 넘음
    }

    @Test
    void testDecodeWithOffset() {
        UInt8Layout layout = new UInt8Layout();

        byte[] data = new byte[]{0x01, 0x02, 0x64}; // 데이터 배열 (64 = 100)
        Integer decodedValue = layout.decode(data, 2); // 오프셋 2

        assertNotNull(decodedValue); // 디코딩 결과가 null이 아니어야 함
        assertEquals(100, decodedValue); // 기대 값은 100
    }
}
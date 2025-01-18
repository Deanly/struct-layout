package net.deanly.structlayout.type.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UInt32LELayoutTest {

    @Test
    void testEncodeValidValues() {
        UInt32LELayout layout = new UInt32LELayout();

        // 유효한 값 테스트
        long value = 300; // 300의 리틀 엔디안 4바이트 표현은 [0x2C, 0x01, 0x00, 0x00]
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded);
        assertEquals(4, encoded.length);
        assertArrayEquals(new byte[]{0x2C, 0x01, 0x00, 0x00}, encoded);
    }

    @Test
    void testEncodeBoundaryValues() {
        UInt32LELayout layout = new UInt32LELayout();

        // 최소값(0)
        byte[] encodedMin = layout.encode(0L);
        assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x00}, encodedMin);

        // 최대값(4294967295)
        byte[] encodedMax = layout.encode(0xFFFFFFFFL); // 4294967295
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, encodedMax);
    }

    @Test
    void testEncodeInvalidValues() {
        UInt32LELayout layout = new UInt32LELayout();

        // 음수 값 및 null 테스트
        assertThrows(IllegalArgumentException.class, () -> layout.encode(-1L));
        assertThrows(IllegalArgumentException.class, () -> layout.encode(null));
    }

    @Test
    void testDecodeValidData() {
        UInt32LELayout layout = new UInt32LELayout();

        byte[] data = new byte[]{
                0x2C, 0x01, 0x00, 0x00, // 300 (리틀 엔디안)
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, // 4294967295 (리틀 엔디안)
                0x00, 0x00, 0x00, 0x00  // 0 (리틀 엔디안)
        };

        assertEquals(300, layout.decode(data, 0));
        assertEquals(0xFFFFFFFFL, layout.decode(data, 4)); // 최대값
        assertEquals(0, layout.decode(data, 8)); // 최소값
    }

    @Test
    void testDecodeInvalidData() {
        UInt32LELayout layout = new UInt32LELayout();

        // 데이터가 너무 짧은 경우
        assertThrows(IllegalArgumentException.class, () -> layout.decode(new byte[]{0x2C, 0x01, 0x00}, 0));
        // null 배열
        assertThrows(IllegalArgumentException.class, () -> layout.decode(null, 0));
        // 범위를 벗어난 offset
        assertThrows(IllegalArgumentException.class, () -> layout.decode(new byte[]{0x00, 0x01, 0x02, 0x03}, 2));
    }
}
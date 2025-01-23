package net.deanly.structlayout.type.basic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UInt16BEFieldTest {

    @Test
    void testEncodeValidValues() {
        UInt16BEField layout = new UInt16BEField();

        // 인코딩: 범위 내 유효한 값
        int value = 300; // 300의 빅 엔디안 2바이트 표현은 [0x01, 0x2C]
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded);
        assertEquals(2, encoded.length); // 2 바이트여야 함
        assertEquals((byte) 0x01, encoded[0]); // MSB (Most Significant Byte)
        assertEquals((byte) 0x2C, encoded[1]); // LSB (Least Significant Byte)
    }

    @Test
    void testEncodeBoundaryValues() {
        UInt16BEField layout = new UInt16BEField();

        // 최소값(0)
        byte[] encodedMin = layout.encode(0);
        assertEquals(2, encodedMin.length);
        assertArrayEquals(new byte[]{0x00, 0x00}, encodedMin);

        // 최대값(65535)
        byte[] encodedMax = layout.encode(65535);
        assertEquals(2, encodedMax.length);
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF}, encodedMax);
    }

    @Test
    void testEncodeInvalidValues() {
        UInt16BEField layout = new UInt16BEField();

        // 유효하지 않은 값 테스트
        assertThrows(IllegalArgumentException.class, () -> layout.encode(-1)); // 음수
        assertThrows(IllegalArgumentException.class, () -> layout.encode(65536)); // 범위 초과
        assertThrows(IllegalArgumentException.class, () -> layout.encode(null)); // null 값
    }

    @Test
    void testDecodeValidData() {
        UInt16BEField layout = new UInt16BEField();

        byte[] data = new byte[]{
                (byte) 0x01, (byte) 0x2C,      // 300 빅 엔디안
                (byte) 0xFF, (byte) 0xFF,      // 65535 빅 엔디안
                (byte) 0x00, (byte) 0x00       // 0 빅 엔디안
        };

        assertEquals(300, layout.decode(data, 0));
        assertEquals(65535, layout.decode(data, 2));
        assertEquals(0, layout.decode(data, 4));
    }

    @Test
    void testDecodeInvalidData() {
        UInt16BEField layout = new UInt16BEField();
        byte[] insufficientData = new byte[]{0x01}; // 데이터가 부족

        // 유효하지 않은 데이터 입력
        assertThrows(IllegalArgumentException.class, () -> layout.decode(insufficientData, 0)); // 길이 부족
        assertThrows(IllegalArgumentException.class, () -> layout.decode(null, 0)); // null 입력
        assertThrows(IllegalArgumentException.class, () -> layout.decode(new byte[]{0x00, 0x01}, 2)); // 범위를 초과하는 오프셋
    }
}
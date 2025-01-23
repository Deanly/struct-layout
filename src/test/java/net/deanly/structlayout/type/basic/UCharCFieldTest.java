package net.deanly.structlayout.type.basic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UCharCFieldTest {

    @Test
    void testEncodeValidValue() {
        UCharCField layout = new UCharCField();
        int value = 127; // 유효 범위 내 값
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded);
        assertEquals(1, encoded.length); // UChar는 1바이트여야 함
        assertEquals((byte) 127, encoded[0]); // 바이트 값 일치 확인
    }

    @Test
    void testEncodeBoundaryValues() {
        UCharCField layout = new UCharCField();

        // 최소값(0) 테스트
        byte[] encodedMin = layout.encode(0);
        assertNotNull(encodedMin);
        assertEquals(1, encodedMin.length);
        assertEquals((byte) 0, encodedMin[0]);

        // 최대값(255) 테스트
        byte[] encodedMax = layout.encode(255);
        assertNotNull(encodedMax);
        assertEquals(1, encodedMax.length);
        assertEquals((byte) 255, encodedMax[0]);
    }

    @Test
    void testEncodeInvalidValues() {
        UCharCField layout = new UCharCField();

        // 음수 값 테스트
        assertThrows(IllegalArgumentException.class, () -> layout.encode(-1));

        // 255 초과 값 테스트
        assertThrows(IllegalArgumentException.class, () -> layout.encode(256));

        // null 값 테스트
        assertThrows(IllegalArgumentException.class, () -> layout.encode(null));
    }

    @Test
    void testDecodeValidData() {
        UCharCField layout = new UCharCField();

        // 유효한 바이트 데이터를 배열로 제공
        byte[] data = new byte[]{0x00, 0x7F, (byte) 0xFF}; // 0, 127, 255

        assertEquals(0, layout.decode(data, 0));
        assertEquals(127, layout.decode(data, 1));
        assertEquals(255, layout.decode(data, 2));
    }

    @Test
    void testDecodeOutOfBounds() {
        UCharCField layout = new UCharCField();
        byte[] data = new byte[]{0x01};

        // 데이터를 벗어난 위치에 접근하려는 경우
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> layout.decode(data, 1));
    }

    @Test
    void testDecodeInvalidData() {
        UCharCField layout = new UCharCField();

        // null 배열 데이터 제공
        assertThrows(NullPointerException.class, () -> layout.decode(null, 0));
    }
}
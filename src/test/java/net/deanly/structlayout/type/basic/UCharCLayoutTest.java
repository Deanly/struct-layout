package net.deanly.structlayout.type.basic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UCharCLayoutTest {

    @Test
    void testEncodeValidValue() {
        UCharCLayout layout = new UCharCLayout();
        int value = 127; // 유효 범위 내 값
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded);
        assertEquals(1, encoded.length); // UChar는 1바이트여야 함
        assertEquals((byte) 127, encoded[0]); // 바이트 값 일치 확인
    }

    @Test
    void testEncodeBoundaryValues() {
        UCharCLayout layout = new UCharCLayout();

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
        UCharCLayout layout = new UCharCLayout();

        // 음수 값 테스트
        assertThrows(IllegalArgumentException.class, () -> layout.encode(-1));

        // 255 초과 값 테스트
        assertThrows(IllegalArgumentException.class, () -> layout.encode(256));

        // null 값 테스트
        assertThrows(IllegalArgumentException.class, () -> layout.encode(null));
    }

    @Test
    void testDecodeValidData() {
        UCharCLayout layout = new UCharCLayout();

        // 유효한 바이트 데이터를 배열로 제공
        byte[] data = new byte[]{0x00, 0x7F, (byte) 0xFF}; // 0, 127, 255

        assertEquals(0, layout.decode(data, 0));
        assertEquals(127, layout.decode(data, 1));
        assertEquals(255, layout.decode(data, 2));
    }

    @Test
    void testDecodeOutOfBounds() {
        UCharCLayout layout = new UCharCLayout();
        byte[] data = new byte[]{0x01};

        // 데이터를 벗어난 위치에 접근하려는 경우
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> layout.decode(data, 1));
    }

    @Test
    void testDecodeInvalidData() {
        UCharCLayout layout = new UCharCLayout();

        // null 배열 데이터 제공
        assertThrows(NullPointerException.class, () -> layout.decode(null, 0));
    }
}
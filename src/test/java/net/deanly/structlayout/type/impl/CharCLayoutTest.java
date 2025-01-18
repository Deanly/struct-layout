package net.deanly.structlayout.type.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharCLayoutTest {

    @Test
    void testDefaultConstructor() {
        // 기본 생성자 테스트
        CharCLayout layout = new CharCLayout();
        assertNotNull(layout);
        assertNull(layout.getProperty()); // 기본 생성자 호출 시 property는 null이어야 함
    }

    @Test
    void testParameterizedConstructor() {
        // 매개변수 생성자 테스트
        String testProperty = "testProperty";
        CharCLayout layout = new CharCLayout(testProperty);
        assertNotNull(layout);
        assertEquals(testProperty, layout.getProperty()); // 생성된 객체의 property 값 확인
    }

    @Test
    void testDecodeValidData() {
        // 올바른 data, offset으로 decode 테스트
        CharCLayout layout = new CharCLayout();

        byte[] data = {0x41, 0x42, 0x43}; // ASCII: 'A', 'B', 'C'
        int offset = 1;
        Byte decoded = layout.decode(data, offset);

        assertNotNull(decoded);
        assertEquals((byte) 0x42, decoded); // data[1]이 0x42 (ASCII 'B') 이어야 함
    }

    @Test
    void testDecodeEdgeCase() {
        // 한 바이트짜리 배열로 decode 테스트
        CharCLayout layout = new CharCLayout();

        byte[] data = {0x7F}; // ASCII: DEL
        int offset = 0;
        Byte decoded = layout.decode(data, offset);

        assertNotNull(decoded);
        assertEquals((byte) 0x7F, decoded); // data[0]이 0x7F 이어야 함
    }

    @Test
    void testDecodeInvalidOffset() {
        // 유효하지 않은 offset 처리 테스트
        CharCLayout layout = new CharCLayout();

        byte[] data = {0x41, 0x42, 0x43};

        // 배열 범위 밖의 offset 사용 시 예외 발생 여부 확인
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            layout.decode(data, 5);
        });

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            layout.decode(data, -1);
        });
    }

    @Test
    void testEncodeValidValue() {
        // 정상 입력 값으로 encode 테스트
        CharCLayout layout = new CharCLayout();

        Byte value = (byte) 0x41; // ASCII: 'A'
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded);
        assertEquals(1, encoded.length); // 길이는 항상 1이어야 함
        assertEquals((byte) 0x41, encoded[0]); // 문자열 값이 올바르게 변환되었는지 확인
    }

    @Test
    void testEncodeNullValue() {
        // 입력 값이 null일 경우 처리 확인
        CharCLayout layout = new CharCLayout();

        assertThrows(NullPointerException.class, () -> {
            layout.encode(null);
        });
    }

    @Test
    void testEncodeEdgeCase() {
        // 최대 바이트 값 및 최소 바이트 값 처리 확인
        CharCLayout layout = new CharCLayout();

        Byte maxValue = Byte.MAX_VALUE; // 127
        byte[] encodedMax = layout.encode(maxValue);
        assertNotNull(encodedMax);
        assertEquals(1, encodedMax.length);
        assertEquals((byte) 127, encodedMax[0]);

        Byte minValue = Byte.MIN_VALUE; // -128
        byte[] encodedMin = layout.encode(minValue);
        assertNotNull(encodedMin);
        assertEquals(1, encodedMin.length);
        assertEquals((byte) -128, encodedMin[0]);
    }

    @Test
    void testIntegration_EncodeDecode() {
        // encode한 데이터를 다시 decode하는 흐름 테스트
        CharCLayout layout = new CharCLayout();

        Byte originalValue = (byte) 0x5A; // ASCII: 'Z'

        // Encoding
        byte[] encoded = layout.encode(originalValue);

        assertNotNull(encoded);
        assertEquals(1, encoded.length);
        assertEquals((byte) 0x5A, encoded[0]);

        // Decoding
        Byte decoded = layout.decode(encoded, 0);

        assertNotNull(decoded);
        assertEquals(originalValue, decoded); // encode와 decode된 값이 동일한지 확인
    }

    @Test
    void testDecodeEmptyArray() {
        // 빈 데이터 배열 처리 테스트
        CharCLayout layout = new CharCLayout();

        byte[] data = {};

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            layout.decode(data, 0);
        });
    }

    @Test
    void testDecodeWithNullData() {
        // null 데이터 처리 테스트
        CharCLayout layout = new CharCLayout();

        assertThrows(NullPointerException.class, () -> {
            layout.decode(null, 0);
        });
    }
}
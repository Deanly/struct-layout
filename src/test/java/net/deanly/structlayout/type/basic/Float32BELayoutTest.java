package net.deanly.structlayout.type.basic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Float32BELayoutTest {

    @Test
    void testDefaultConstructor() {
        // 기본 생성자 테스트
        Float32BELayout layout = new Float32BELayout();
        assertNotNull(layout);
        assertNull(layout.getProperty()); // 기본 생성자 호출 시 property는 null이어야 함
        assertEquals(4, layout.getSpan()); // 32비트 float는 4바이트 길이
    }

    @Test
    void testParameterizedConstructor() {
        // 매개변수 생성자 테스트
        String testProperty = "testProperty";
        Float32BELayout layout = new Float32BELayout(testProperty);
        assertNotNull(layout);
        assertEquals(testProperty, layout.getProperty());
        assertEquals(4, layout.getSpan());
    }

    @Test
    void testDecodeValidData() {
        // 올바른 데이터와 offset으로 decode 테스트
        Float32BELayout layout = new Float32BELayout();

        // IEEE 754 표현으로 float 12.34를 나타내는 빅 엔디안 데이터
        byte[] data = {0x41, 0x45, (byte) 0x70, (byte) 0xA4}; // 12.34
        int offset = 0;

        Float decoded = layout.decode(data, offset);
        assertNotNull(decoded);
        assertEquals(12.34f, decoded, 0.001f); // 오차 범위 내에 있는지 확인
    }

    @Test
    void testDecodeValidDataWithOffsetUsingCorrectData() {
        // 데이터 오프셋을 적용한 경우의 decode 테스트
        Float32BELayout layout = new Float32BELayout();

        // 테스트할 값: -45.67
        float testValue = -45.67f;

        // Float.floatToIntBits()를 이용한 정확한 빅엔디안 데이터 생성
        int bits = Float.floatToIntBits(testValue);
        byte[] correctData = {
                (byte) ((bits >> 24) & 0xFF),
                (byte) ((bits >> 16) & 0xFF),
                (byte) ((bits >> 8) & 0xFF),
                (byte) (bits & 0xFF),
        };

        // correctData 배열을 테스트 데이터로 활용
        byte[] data = new byte[8]; // 4바이트 오프셋 이후 correctData 삽입
        System.arraycopy(correctData, 0, data, 4, 4); // correctData는 4번째 바이트부터 시작

        int offset = 4; // 4바이트 오프셋

        // Decode 수행
        Float decoded = layout.decode(data, offset);
        assertNotNull(decoded);
        assertEquals(testValue, decoded, 0.001f); // 원래의 값과 비교
    }

    @Test
    void testDecodeInvalidOffset() {
        // 유효하지 않은 offset 처리 확인
        Float32BELayout layout = new Float32BELayout();
        byte[] data = {0x00, 0x00, 0x00};

        assertThrows(IllegalArgumentException.class, () -> {
            layout.decode(data, 0); // 너무 짧은 배열
        });

        assertThrows(IllegalArgumentException.class, () -> {
            layout.decode(data, -1); // 음수 offset
        });

        assertThrows(IllegalArgumentException.class, () -> {
            layout.decode(data, 3); // 범위 밖
        });
    }

    @Test
    void testDecodeNullData() {
        // null 데이터 처리 테스트
        Float32BELayout layout = new Float32BELayout();

        assertThrows(IllegalArgumentException.class, () -> {
            layout.decode(null, 0);
        });
    }

    @Test
    void testEncodeValidValue() {
        // 정상적인 float 값을 encode 테스트
        Float32BELayout layout = new Float32BELayout();

        float value = -28.56f; // 테스트할 float 값

        // Float.floatToIntBits()를 통해 IEEE 754 빅엔디안 바이트 배열 생성
        int bits = Float.floatToIntBits(value);
        byte[] expectedEncoded = {
                (byte) ((bits >> 24) & 0xFF),
                (byte) ((bits >> 16) & 0xFF),
                (byte) ((bits >> 8) & 0xFF),
                (byte) (bits & 0xFF),
        };

        // 실제 encode 메서드의 결과 확인
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded); // 결과가 null이 아님을 확인
        assertEquals(4, encoded.length); // float은 항상 4바이트

        // encode의 결과를 기대값과 비교
        assertArrayEquals(expectedEncoded, encoded);
    }

    @Test
    void testEncodeNullValue() {
        // null 값을 encode 시 예외 처리 확인
        Float32BELayout layout = new Float32BELayout();

        assertThrows(IllegalArgumentException.class, () -> {
            layout.encode(null);
        });
    }

    @Test
    void testEncodeDecodeFlow() {
        // encode한 데이터를 다시 decode하는 흐름 테스트
        Float32BELayout layout = new Float32BELayout();

        float originalValue = 63.48f;

        // Encoding
        byte[] encoded = layout.encode(originalValue);
        assertNotNull(encoded);
        assertEquals(4, encoded.length);

        // Decoding
        Float decoded = layout.decode(encoded, 0);
        assertNotNull(decoded);
        assertEquals(originalValue, decoded, 0.001f);
    }

    @Test
    void testEncodeEdgeCases() {
        // 특정 엣지 케이스 처리 확인 (Max, Min, Positive Infinity, NaN, Zero)
        Float32BELayout layout = new Float32BELayout();

        // 1. Float.MAX_VALUE
        byte[] encodedMax = layout.encode(Float.MAX_VALUE);
        assertArrayEquals(new byte[]{0x7F, 0x7F, (byte) 0xFF, (byte) 0xFF}, encodedMax);

        // 2. Float.MIN_VALUE
        byte[] encodedMin = layout.encode(Float.MIN_VALUE);
        assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x01}, encodedMin);

        // 3. Positive Infinity
        assertThrows(IllegalArgumentException.class, () -> layout.encode(Float.POSITIVE_INFINITY));

        // 4. NaN
        byte[] encodedNaN = layout.encode(Float.NaN);
        assertTrue(Float.isNaN(layout.decode(encodedNaN, 0)));

        // 5. Zero
        byte[] encodedZero = layout.encode(0.0f);
        assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x00}, encodedZero);
    }
}
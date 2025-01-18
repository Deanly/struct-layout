package net.deanly.structlayout.type.basic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Float32LELayoutTest {

    @Test
    void testEncodeValidValue() {
        // Float32LELayout 인스턴스 생성
        Float32LELayout layout = new Float32LELayout();

        float value = -28.56f; // 테스트할 float 값

        // expectedEncoded: IEEE 754 little-endian 변환
        int bits = Float.floatToIntBits(value);
        byte[] expectedEncoded = {
                (byte) (bits & 0xFF),
                (byte) ((bits >> 8) & 0xFF),
                (byte) ((bits >> 16) & 0xFF),
                (byte) ((bits >> 24) & 0xFF),
        };

        // 실제 encode 결과 확인
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded); // 결과가 null이 아니어야 함
        assertEquals(4, encoded.length); // float은 항상 4바이트
        assertArrayEquals(expectedEncoded, encoded); // 기대값과 인코딩된 값 비교
    }

    @Test
    void testDecodeValidData() {
        // Float32LELayout 인스턴스 생성
        Float32LELayout layout = new Float32LELayout();

        float expectedValue = -28.56f; // 테스트할 예상 값

        // IEEE 754 little-endian 바이트 배열 생성
        int bits = Float.floatToIntBits(expectedValue);
        byte[] encodedData = {
                (byte) (bits & 0xFF),
                (byte) ((bits >> 8) & 0xFF),
                (byte) ((bits >> 16) & 0xFF),
                (byte) ((bits >> 24) & 0xFF),
        };

        // Decode 수행
        Float decodedValue = layout.decode(encodedData, 0);

        assertNotNull(decodedValue); // 결과가 null이 아니어야 함
        assertEquals(expectedValue, decodedValue, 0.001f); // 기대값과 디코딩된 값 비교
    }

    @Test
    void testEncodeAndDecode() {
        // Float32LELayout 인스턴스 생성
        Float32LELayout layout = new Float32LELayout();

        float testValue = 123.45f; // 테스트할 float 값

        // 인코딩
        byte[] encoded = layout.encode(testValue);

        // 디코딩
        Float decoded = layout.decode(encoded, 0);

        assertNotNull(decoded); // 디코딩 값이 null이 아니어야 함
        assertEquals(testValue, decoded, 0.001f); // 인코딩된 값이 디코딩 값과 같아야 함
    }

    @Test
    void testDecodeWithOffset() {
        // Float32LELayout 인스턴스 생성
        Float32LELayout layout = new Float32LELayout();

        float expectedValue = 78.91f; // 테스트할 예상 값

        // 오프셋을 포함한 데이터 생성
        int bits = Float.floatToIntBits(expectedValue);
        byte[] encodedData = {
                0x00, 0x00, 0x00, 0x00, // Dummy bytes at the start
                (byte) (bits & 0xFF),
                (byte) ((bits >> 8) & 0xFF),
                (byte) ((bits >> 16) & 0xFF),
                (byte) ((bits >> 24) & 0xFF),
        };

        // Decode 수행 (offset = 4)
        Float decodedValue = layout.decode(encodedData, 4);

        assertNotNull(decodedValue); // 결과가 null이 아니어야 함
        assertEquals(expectedValue, decodedValue, 0.001f); // 기대값과 디코딩된 값 비교
    }

    @Test
    void testEncodeNullValue() {
        // Float32LELayout 인스턴스 생성
        Float32LELayout layout = new Float32LELayout();

        // Null 값 테스트
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> layout.encode(null)
        );

        assertEquals("Value cannot be null.", exception.getMessage());
    }
}
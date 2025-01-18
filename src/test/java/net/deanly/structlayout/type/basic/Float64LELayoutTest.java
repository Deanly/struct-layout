package net.deanly.structlayout.type.basic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Float64LELayoutTest {

    @Test
    void testEncodeValidValue() {
        // Float64LELayout 인스턴스
        Float64LELayout layout = new Float64LELayout();

        double value = -12345.6789; // 테스트할 double 값

        // Expected encoded value (Little-Endian)
        long longBits = Double.doubleToLongBits(value);
        byte[] expectedEncoded = {
                (byte) (longBits & 0xFF),
                (byte) ((longBits >> 8) & 0xFF),
                (byte) ((longBits >> 16) & 0xFF),
                (byte) ((longBits >> 24) & 0xFF),
                (byte) ((longBits >> 32) & 0xFF),
                (byte) ((longBits >> 40) & 0xFF),
                (byte) ((longBits >> 48) & 0xFF),
                (byte) ((longBits >> 56) & 0xFF),
        };

        // 실제 encode 호출
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded); // Null이 아님을 확인
        assertEquals(8, encoded.length); // Double 데이터는 8바이트
        assertArrayEquals(expectedEncoded, encoded); // Little-Endian 결과 비교
    }

    @Test
    void testDecodeValidData() {
        // Float64LELayout 인스턴스
        Float64LELayout layout = new Float64LELayout();

        double expectedValue = -12345.6789; // 테스트 값

        // Little-Endian 바이트 배열 생성
        long longBits = Double.doubleToLongBits(expectedValue);
        byte[] encodedData = {
                (byte) (longBits & 0xFF),
                (byte) ((longBits >> 8) & 0xFF),
                (byte) ((longBits >> 16) & 0xFF),
                (byte) ((longBits >> 24) & 0xFF),
                (byte) ((longBits >> 32) & 0xFF),
                (byte) ((longBits >> 40) & 0xFF),
                (byte) ((longBits >> 48) & 0xFF),
                (byte) ((longBits >> 56) & 0xFF),
        };

        // Decode 수행
        Double decodedValue = layout.decode(encodedData, 0);

        assertNotNull(decodedValue); // Null이 아님을 확인
        assertEquals(expectedValue, decodedValue, 0.00001); // 결과 값 비교
    }

    @Test
    void testEncodeAndDecodeConsistency() {
        // Float64LELayout 인스턴스
        Float64LELayout layout = new Float64LELayout();

        double testValue = 98765.4321; // 테스트할 double 값

        // Encode 결과
        byte[] encoded = layout.encode(testValue);

        // Decode 결과
        Double decodedValue = layout.decode(encoded, 0);

        // 원래 값과 일치하는지 확인
        assertNotNull(decodedValue);
        assertEquals(testValue, decodedValue, 0.00001);
    }

    @Test
    void testDecodeWithOffset() {
        // Float64LELayout 인스턴스
        Float64LELayout layout = new Float64LELayout();

        double expectedValue = 31415.9265; // 테스트 값

        // 데이터: 앞에 4바이트 패딩을 포함한 Little-Endian 데이터
        long longBits = Double.doubleToLongBits(expectedValue);
        byte[] encodedData = {
                0x00, 0x00, 0x00, 0x00, // Dummy bytes
                (byte) (longBits & 0xFF),
                (byte) ((longBits >> 8) & 0xFF),
                (byte) ((longBits >> 16) & 0xFF),
                (byte) ((longBits >> 24) & 0xFF),
                (byte) ((longBits >> 32) & 0xFF),
                (byte) ((longBits >> 40) & 0xFF),
                (byte) ((longBits >> 48) & 0xFF),
                (byte) ((longBits >> 56) & 0xFF),
        };

        // Decode 수행 (offset = 4)
        Double decodedValue = layout.decode(encodedData, 4);

        assertNotNull(decodedValue);
        assertEquals(expectedValue, decodedValue, 0.00001);
    }
}
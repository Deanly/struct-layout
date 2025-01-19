package net.deanly.structlayout.type.basic;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class UInt64LELayoutTest {

    @Test
    void testEncode() {
        UInt64LELayout layout = new UInt64LELayout();

        // BigInteger 테스트 값들
        BigInteger[] values = {
                BigInteger.ZERO, // 0
                BigInteger.ONE, // 1
                new BigInteger("4294967295"), // 2^32 - 1
                new BigInteger("9223372036854775807"), // Long.MAX_VALUE (2^63 - 1)
                new BigInteger("18446744073709551615") // 최대 unsigned 64비트 값 (2^64 - 1)
        };

        for (BigInteger value : values) {
            byte[] encoded = layout.encode(value);

            // 인코딩된 데이터가 정확히 8바이트인지 확인
            assertNotNull(encoded, "Encoded byte array should not be null");
            assertEquals(8, encoded.length, "Encoded byte array should have 8 bytes");

            // 인코딩 후 다시 디코딩하여 원래 값과 확인
            BigInteger decoded = layout.decode(encoded, 0);
            assertEquals(value, decoded, "Decoded value should match the original input value");
        }
    }

    @Test
    void testDecode() {
        UInt64LELayout layout = new UInt64LELayout();

        // 특정 little-endian 표현 데이터 테스트
        byte[] data = {1, 0, 0, 0, 2, 0, 0, 0}; // Represents 0x0000000200000001
        BigInteger expected = new BigInteger("8589934593"); // 2^33 + 1

        BigInteger decoded = layout.decode(data, 0);
        assertEquals(expected, decoded, "Decoded value should match the expected value");
    }

    @Test
    void testEncodeDecodeWithOffset() {
        UInt64LELayout layout = new UInt64LELayout();

        BigInteger value = new BigInteger("123456789123456789"); // 테스트 값
        byte[] buffer = new byte[16];

        // 특정 오프셋에 값을 인코딩
        System.arraycopy(layout.encode(value), 0, buffer, 4, 8);

        // 지정된 오프셋에서 값을 디코딩
        BigInteger decoded = layout.decode(buffer, 4);
        assertEquals(value, decoded, "Decoded value should match the original value when using offset");
    }

    @Test
    void testInvalidInputs() {
        UInt64LELayout layout = new UInt64LELayout();

        // null 입력 테스트
        assertThrows(IllegalArgumentException.class, () -> layout.decode(null, 0), "Null input should throw IllegalArgumentException");

        // 범위를 넘어가는 값 테스트
        BigInteger tooLargeValue = new BigInteger("18446744073709551616"); // 2^64 (범위를 초과함)
        assertThrows(IllegalArgumentException.class,
                () -> layout.encode(tooLargeValue),
                "Value exceeding unsigned 64-bit range should throw IllegalArgumentException");

        // 데이터 길이가 부족한 경우
        byte[] insufficientData = new byte[7]; // 최소 8바이트 미만
        assertThrows(IllegalArgumentException.class, () -> layout.decode(insufficientData, 0), "Insufficient data length should throw IllegalArgumentException");

        // 잘못된 오프셋 테스트
        byte[] validData = new byte[8];
        assertThrows(IllegalArgumentException.class, () -> layout.decode(validData, 9), "Invalid offset should throw IllegalArgumentException");
    }

    @Test
    void testEncodeDecodeWithLargeValues() {
        UInt64LELayout layout = new UInt64LELayout();

        // 최대값 (2^64 - 1)
        BigInteger maxValue = new BigInteger("18446744073709551615"); // 0xFFFFFFFFFFFFFFFF
        byte[] encoded = layout.encode(maxValue);

        // 디코딩 결과 원래 값과 일치하는지 확인
        BigInteger decoded = layout.decode(encoded, 0);
        assertEquals(maxValue, decoded, "Decoded value should match the maximum UInt64 value");
    }

    @Test
    void testEdgeCases() {
        UInt64LELayout layout = new UInt64LELayout();

        // 각각 비트가 설정된 가장 작은 값과 중간값
        BigInteger[] edgeValues = {
                new BigInteger("1"), // 0x0000000000000001
                new BigInteger("256"), // 0x0000000000000100
                new BigInteger("4294967296"), // 0x0000000100000000
                new BigInteger("9223372036854775808") // 0x8000000000000000 (2^63)
        };

        for (BigInteger value : edgeValues) {
            byte[] encoded = layout.encode(value);

            // 인코딩 및 디코딩 확인
            BigInteger decoded = layout.decode(encoded, 0);
            assertEquals(value, decoded, "Decoded value should match the original edge case value");
        }
    }
}
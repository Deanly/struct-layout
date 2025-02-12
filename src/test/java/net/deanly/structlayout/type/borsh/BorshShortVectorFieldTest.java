package net.deanly.structlayout.type.borsh;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BorshShortVectorFieldTest {

    @Test
    void testEncodeAndDecode() {
        // 테스트 데이터 준비
        int inputData = 300; // 테스트 길이 값

        // 첫 번째 인스턴스를 통해 길이 encode
        BorshShortVectorField encoder = new BorshShortVectorField();
        byte[] encodedData = encoder.encode(inputData);

        // 두 번째 인스턴스를 통해 길이 decode
        BorshShortVectorField decoder = new BorshShortVectorField();
        int decodedData = decoder.decode(encodedData, 0);

        // 검증: 디코딩된 데이터가 원본과 동일해야 함
        assertEquals(inputData, decodedData, "Decoded length should match the original input length");

        // 검증: 인코딩된 데이터의 바이트 길이
        assertEquals(2, encodedData.length, "Encoded data for 300 should use 2 bytes");
    }

    @Test
    void testEncodeAndDecodeSmallValue() {
        // 테스트 데이터 준비
        int inputData = 127; // 작은 값 테스트

        // Encode
        BorshShortVectorField encoder = new BorshShortVectorField();
        byte[] encodedData = encoder.encode(inputData);

        // Decode
        BorshShortVectorField decoder = new BorshShortVectorField();
        int decodedData = decoder.decode(encodedData, 0);

        // 검증: 디코딩된 데이터가 원본과 동일해야 함
        assertEquals(inputData, decodedData, "Decoded length should match the original input length");

        // 검증: 작은 값은 1바이트로 인코딩됨
        assertEquals(1, encodedData.length, "Encoded data for 127 should use 1 byte");
    }

    @Test
    void testDecodeInvalidOffset() {
        // 잘못된 데이터 준비
        byte[] invalidData = { (byte) 0x81, 0x01 }; // 올바른 데이터지만 offset을 잘못 지정

        BorshShortVectorField decoder = new BorshShortVectorField();

        // 예외 발생 확인
        assertThrows(IllegalArgumentException.class, () -> decoder.decode(invalidData, 2),
                "Decoding should throw an exception for invalid offset");
    }

    @Test
    void testDecodeMultiByteValue() {
        // 테스트 데이터 준비 (멀티 바이트 값: 0x80 0x01 -> 128)
        byte[] inputData = { (byte) 0x80, 0x01 };

        // Decode
        BorshShortVectorField decoder = new BorshShortVectorField();
        int decodedData = decoder.decode(inputData, 0);

        // 검증: 디코딩된 데이터가 올바른 값이어야 함
        assertEquals(128, decodedData, "Decoded length should match the original input length");

        // 검증: 데이터의 길이 계산
        assertEquals(2, decoder.calculateSpan(inputData, 0), "Span calculation for 128 should be 2 bytes");
    }
}
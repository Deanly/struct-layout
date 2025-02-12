package net.deanly.structlayout.type.borsh;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BorshBooleanFieldTest {

    @Test
    void testEncodeAndDecode() {
        // 테스트 데이터 준비
        boolean inputData = true; // 테스트 boolean 값

        // 첫 번째 인스턴스를 통해 boolean encode
        BorshBooleanField encoder = new BorshBooleanField();
        byte[] encodedData = encoder.encode(inputData);

        // 두 번째 인스턴스를 통해 boolean decode
        BorshBooleanField decoder = new BorshBooleanField();
        boolean decodedData = decoder.decode(encodedData, 0);

        // 검증: 디코딩된 데이터가 원본과 동일해야 함
        assertEquals(inputData, decodedData, "Decoded boolean should match the original input boolean");

        // 검증: 인코딩된 데이터 길이는 항상 1바이트
        assertEquals(1, encodedData.length, "Encoded data for boolean should always be 1 byte");
        assertEquals((byte) 0x01, encodedData[0], "Encoded data for 'true' should be 0x01");
    }

    @Test
    void testEncodeAndDecodeFalse() {
        // 테스트 데이터 준비
        boolean inputData = false;

        // Encode
        BorshBooleanField encoder = new BorshBooleanField();
        byte[] encodedData = encoder.encode(inputData);

        // Decode
        BorshBooleanField decoder = new BorshBooleanField();
        boolean decodedData = decoder.decode(encodedData, 0);

        // 검증: 디코딩된 데이터가 원본과 동일해야 함
        assertEquals(inputData, decodedData, "Decoded boolean should match the original input boolean");

        // 검증: 인코딩된 데이터 길이는 항상 1바이트
        assertEquals(1, encodedData.length, "Encoded data for boolean should always be 1 byte");
        assertEquals((byte) 0x00, encodedData[0], "Encoded data for 'false' should be 0x00");
    }

    @Test
    void testDecodeInvalidValue() {
        // 잘못된 데이터 준비 (0x02는 유효하지 않은 boolean 값)
        byte[] invalidData = { 0x02 };

        BorshBooleanField decoder = new BorshBooleanField();

        // 예외 발생 확인
        assertThrows(IllegalArgumentException.class, () -> decoder.decode(invalidData, 0),
                "Decoding should throw an exception for invalid boolean value");
    }
}
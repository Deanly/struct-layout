package net.deanly.structlayout.type.borsh;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BorshStringFieldTest {

    @Test
    void testEncodeAndDecode() {
        // 테스트 데이터 준비
        String inputData = "Hello, Borsh String!"; // 테스트 문자열

        // 첫 번째 인스턴스를 통해 문자열 encode
        BorshStringField encoder = new BorshStringField();
        byte[] encodedData = encoder.encode(inputData);

        // 두 번째 인스턴스를 통해 문자열 decode
        BorshStringField decoder = new BorshStringField();
        String decodedData = decoder.decode(encodedData, 0);

        // 검증: 디코딩된 데이터가 원본과 동일해야 함
        assertEquals(inputData, decodedData, "Decoded string should match the original input string");

        // 인코딩된 데이터에서 길이 검증 (맨 앞 4바이트는 문자열 길이)
        int encodedLength =
                ((encodedData[0] & 0xFF)) |
                        ((encodedData[1] & 0xFF) << 8) |
                        ((encodedData[2] & 0xFF) << 16) |
                        ((encodedData[3] & 0xFF) << 24);
        assertEquals(inputData.getBytes().length, encodedLength, "Encoded length should match the input string length");
    }

    @Test
    void testEncodeEmptyString() {
        // 빈 문자열 준비
        String inputData = "";

        // Encode
        BorshStringField encoder = new BorshStringField();
        byte[] encodedData = encoder.encode(inputData);

        // 검증: 인코딩된 데이터는 최소 4바이트 (길이 필드)여야 합니다.
        assertEquals(4, encodedData.length, "Encoded data for empty input should contain only length field");

        // Decode
        BorshStringField decoder = new BorshStringField();
        String decodedData = decoder.decode(encodedData, 0);

        // 검증: 디코딩된 데이터는 빈 문자열이어야 함
        assertEquals(inputData, decodedData, "Decoded data for empty input should be an empty string");
    }

    @Test
    void testDecodeInvalidLength() {
        // 인코딩된 데이터가 잘못된 경우 (길이가 부족한 경우)
        byte[] invalidData = { 0, 0, 0, 10 }; // Length는 10이지만 실제 문자열 데이터는 없음

        BorshStringField decoder = new BorshStringField();

        // 예외 발생 확인
        assertThrows(IllegalArgumentException.class, () -> decoder.decode(invalidData, 0),
                "Decoding should throw an exception for insufficient string data");
    }

    @Test
    void testEncodeAndDecodeWithOffset() {
        // 임의 문자열 준비
        String inputData = "String with Offset!";
        byte[] prefixData = "PREFIX".getBytes(); // 데이터의 앞 부분
        byte[] fullData = new byte[prefixData.length + 4 + inputData.getBytes().length]; // Total: Prefix + Length(4바이트) + String data

        // Prefix 데이터 먼저 복사
        System.arraycopy(prefixData, 0, fullData, 0, prefixData.length);

        // BorshStringField를 이용해 중앙 부분에 데이터 추가 (Offset 사용)
        BorshStringField encoder = new BorshStringField();
        byte[] encodedData = encoder.encode(inputData);
        System.arraycopy(encodedData, 0, fullData, prefixData.length, encodedData.length);

        // Decode 시 사용할 인스턴스
        BorshStringField decoder = new BorshStringField();
        String decodedData = decoder.decode(fullData, prefixData.length);

        // 검증
        assertEquals(inputData, decodedData, "Decoded string with offset should match the original input");
    }
}
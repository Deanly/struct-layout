package net.deanly.structlayout.type.borsh;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BorshBlobFieldTest {

    @Test
    void testEncodeAndDecode() {
        // 테스트 데이터 준비
        byte[] inputData = "Hello, Length-Prefixed Blob!".getBytes(); // 임의 데이터

        // 첫 번째 인스턴스를 통해 데이터 encode
        BorshBlobField encoder = new BorshBlobField();
        byte[] encodedData = encoder.encode(inputData);

        // 두 번째 인스턴스를 통해 데이터 decode
        BorshBlobField decoder = new BorshBlobField();
        byte[] decodedData = decoder.decode(encodedData, 0);

        // 검증
        assertArrayEquals(inputData, decodedData, "Decoded data should match the original input data");

        // 인코딩된 데이터에서 길이 검증 (맨 앞 4바이트는 데이터 길이)
        int encodedLength =
                ((encodedData[0] & 0xFF) << 24) |
                        ((encodedData[1] & 0xFF) << 16) |
                        ((encodedData[2] & 0xFF) << 8) |
                        (encodedData[3] & 0xFF);
        assertEquals(inputData.length, encodedLength, "Encoded length should match the input data length");
    }

    @Test
    void testEncodeEmptyBlob() {
        // 빈 데이터 준비
        byte[] inputData = new byte[0];

        // Encode
        BorshBlobField encoder = new BorshBlobField();
        byte[] encodedData = encoder.encode(inputData);

        // 검증: 인코딩된 데이터는 최소 4바이트 (길이 필드)여야 합니다.
        assertEquals(4, encodedData.length, "Encoded data for empty input should contain length field");

        // Decode
        BorshBlobField decoder = new BorshBlobField();
        byte[] decodedData = decoder.decode(encodedData, 0);

        // 검증: 디코딩된 데이터는 빈 배열이어야 함.
        assertArrayEquals(inputData, decodedData, "Decoded data for empty input should be empty");
    }

    @Test
    void testDecodeInvalidLength() {
        // 인코딩된 데이터가 잘못된 경우 (길이가 부족한 경우)
        byte[] invalidData = { 0, 0, 0, 10 }; // Length는 10이지만 실제 Value는 없도록 설계

        BorshBlobField decoder = new BorshBlobField();

        // 예외 발생 확인
        assertThrows(IllegalArgumentException.class, () -> decoder.decode(invalidData, 0),
                "Decoding should throw an exception for insufficient data");
    }

    @Test
    void testEncodeAndDecodeWithOffset() {
        // 임의 데이터 준비
        byte[] inputData = "Blob Data With Offset!".getBytes();
        byte[] prefixData = "PREFIX".getBytes(); // 데이터의 앞 부분
        byte[] fullData = new byte[prefixData.length + inputData.length + 4]; // Total: Prefix + Length(4바이트) + Value

        // Prefix 데이터 먼저 복사
        System.arraycopy(prefixData, 0, fullData, 0, prefixData.length);

        // LengthPrefixedBlobField를 이용해 중앙 부분에 데이터 추가 (Offset 사용)
        BorshBlobField encoder = new BorshBlobField();
        byte[] encodedData = encoder.encode(inputData);
        System.arraycopy(encodedData, 0, fullData, prefixData.length, encodedData.length);

        // Decode 시 사용할 인스턴스
        BorshBlobField decoder = new BorshBlobField();
        byte[] decodedData = decoder.decode(fullData, prefixData.length);

        // 검증
        assertArrayEquals(inputData, decodedData, "Decoded data with offset should match the original input");
    }
}
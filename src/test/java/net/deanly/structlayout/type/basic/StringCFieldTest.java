package net.deanly.structlayout.type.basic;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class StringCFieldTest {

    @Test
    void testEncodeValidString() {
        StringCField layout = new StringCField();
        String value = "Hello, World!";
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded);
        assertEquals(value.length() + 1, encoded.length); // 널 종료 문자가 추가된 길이
        assertEquals(0, encoded[encoded.length - 1]); // 마지막 바이트가 0인지 확인
    }

    @Test
    void testEncodeEmptyString() {
        StringCField layout = new StringCField();
        String value = "";
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded);
        assertEquals(1, encoded.length); // 빈 문자열도 널 종료 문자 1바이트
        assertEquals(0, encoded[0]);
    }

    @Test
    void testEncodeNullString() {
        StringCField layout = new StringCField();
        byte[] encoded = layout.encode(null);

        assertNotNull(encoded);
        assertEquals(0, encoded.length); // null 문자열은 빈 배열 반환
    }

    @Test
    void testDecodeValidData() {
        StringCField layout = new StringCField();
        byte[] encodedData = "Hello, World!".getBytes(StandardCharsets.US_ASCII);
        byte[] encodedWithNull = new byte[encodedData.length + 1];
        System.arraycopy(encodedData, 0, encodedWithNull, 0, encodedData.length);
        encodedWithNull[encodedWithNull.length - 1] = 0; // 널 종료 추가

        String decoded = layout.decode(encodedWithNull, 0);

        assertNotNull(decoded);
        assertEquals("Hello, World!", decoded);
    }

    @Test
    void testDecodeWithOffset() {
        StringCField layout = new StringCField();
        String originalString = "Test String";
        byte[] encodedData = originalString.getBytes(StandardCharsets.US_ASCII);
        byte[] dataWithOffset = new byte[encodedData.length + 5 + 1];
        System.arraycopy(encodedData, 0, dataWithOffset, 5, encodedData.length);
        dataWithOffset[5 + encodedData.length] = 0; // 널 종료 추가

        String decoded = layout.decode(dataWithOffset, 5);

        assertNotNull(decoded);
        assertEquals(originalString, decoded);
    }

    @Test
    void testDecodeWithoutNullTerminator() {
        StringCField layout = new StringCField();
        byte[] dataWithoutNull = "Missing null terminator".getBytes(StandardCharsets.US_ASCII);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> layout.decode(dataWithoutNull, 0));
        assertEquals("Null-terminated character not found", exception.getMessage());
    }

    @Test
    void testDecodeEmptyData() {
        StringCField layout = new StringCField();
        byte[] emptyData = new byte[]{0}; // 널 종료 문자만 있음

        String decoded = layout.decode(emptyData, 0);

        assertNotNull(decoded);
        assertEquals("", decoded); // 빈 문자열 반환
    }

    @Test
    void testGetSpanValidData() {
        StringCField layout = new StringCField();
        byte[] data = "Valid Span".getBytes(StandardCharsets.US_ASCII);
        byte[] dataWithNull = new byte[data.length + 1];
        System.arraycopy(data, 0, dataWithNull, 0, data.length);
        dataWithNull[dataWithNull.length - 1] = 0; // 널 종료 추가

        int span = layout.calculateSpan(dataWithNull, 0);

        assertEquals(data.length + 1, span); // 널 종료 기준으로 Span 확인
    }

    @Test
    void testGetSpanWithoutNullTerminator() {
        StringCField layout = new StringCField();
        byte[] data = "No Null Terminator".getBytes(StandardCharsets.US_ASCII);

        assertThrows(IllegalArgumentException.class, () -> layout.calculateSpan(data, 0)); // 널 종료 없는 경우 예외
    }

    @Test
    void testBytesToHex() {
        StringCField layout = new StringCField();
        byte[] data = "Hello".getBytes(StandardCharsets.US_ASCII);
        byte[] withNullTerminator = new byte[data.length + 1];
        System.arraycopy(data, 0, withNullTerminator, 0, data.length);
        withNullTerminator[withNullTerminator.length - 1] = 0; // 널 종료 추가

        String hexString = layout.bytesToHex(withNullTerminator, 0);

        assertNotNull(hexString);
        assertEquals("48 65 6C 6C 6F 00", hexString); // "Hello"를 Hex로 변환
    }

    @Test
    void testBytesToHexWithOffset() {
        StringCField layout = new StringCField();
        byte[] data = "TestHex".getBytes(StandardCharsets.US_ASCII);
        byte[] withOffset = new byte[data.length + 2 + 1];
        System.arraycopy(data, 0, withOffset, 2, data.length);
        withOffset[2 + data.length] = 0; // 널 종료 추가

        String hexString = layout.bytesToHex(withOffset, 2);

        assertNotNull(hexString);
        assertEquals("54 65 73 74 48 65 78 00", hexString); // "TestHex"를 Hex로 변환 (Offset 적용)
    }

    @Test
    void testBytesToHexInvalidData() {
        StringCField layout = new StringCField();

        assertThrows(IllegalArgumentException.class, () -> layout.bytesToHex(null, 0)); // Null 데이터 예외 처리
        assertThrows(IllegalArgumentException.class, () -> layout.bytesToHex(new byte[0], 0)); // 빈 데이터 예외 처리
    }
}
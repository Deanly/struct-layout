package net.deanly.structlayout.type.basic;

import lombok.extern.slf4j.Slf4j;
import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.DynamicSpanField;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * The `StringCLayout` class is a layout implementation for handling C-style strings.
 * It encodes and decodes strings using a specified character set, with default support
 * for the US-ASCII encoding. Strings are managed as null-terminated sequences of bytes,
 * commonly used in C-style programming.
 *
 * This class extends the generic `Layout` class for managing string data types and
 * implements the `DynamicSpanLayout` interface to support strings with dynamic lengths.
 * The primary purpose is to manage fixed-size byte buffers containing C-style strings
 * and dynamically determine their span (length) during usage.
 *
 * Features:
 * 1. Encoding Java strings into C-style null-terminated byte arrays.
 * 2. Decoding C-style null-terminated byte arrays into Java strings.
 * 3. Dynamically calculating the span of the null-terminated string.
 * 4. Hexadecimal representation of string bytes for debugging purposes.
 * 5. Logging string information and content for field-specific debugging.
 *
 * Constructor Details:
 * - By default, the encoding type is US-ASCII and designed for cases where no charset is explicitly provided.
 * - A custom `Charset` can also be specified to work with non-default encodings.
 *
 * Methods:
 * - The `encode(String value)` method converts the input Java string into a byte array
 *   by encoding it with the configured character set, appending a null-terminator, and
 *   dynamically determining the span.
 * - The `decode(byte[] bytes, int offset)` method reads a byte array from the specified offset,
 *   determines the length of the null-terminated string, and decodes it into a Java string.
 * - The `calculateSpan(byte[] data, int offset)` method calculates the span (length) of a
 *   null-terminated string dynamically starting from the given offset within the byte array.
 * - The `bytesToHex(byte[] bytes, int offset)` method converts a byte array representation into
 *   a formatted hexadecimal string for debugging.
 * - The `printDebug(byte[] data, int offset, Field field)` method logs detailed information
 *   about the field and its associated data when in a test debug environment.
 *
 * Notes:
 * - An exception is thrown if a null-terminator is not found during decoding or span calculation.
 * - Internal logging mechanisms are used for debugging and traceability.
 */
public class StringCField extends FieldBase<String> implements DynamicSpanField, BasicType {

    private final Charset charset; // 문자열 인코딩 방식
    private int span;

    /**
     * Constructs a `StringCLayout` instance with a default character set of US-ASCII.
     *
     * This class is designed for handling C-style strings based on the ASCII character set
     * within fixed-size byte buffers, supporting scenarios where C-style null-terminated strings
     * need to be encoded or decoded.
     */
    // 기본 ASCII 기반의 C 문자열 처리
    public StringCField() {
        this(StandardCharsets.US_ASCII);
    }

    /**
     * Constructs a new instance of StringCLayout for handling C-Style strings using a specified character set encoding.
     * The layout handles strings whose lengths are not predefined and must be determined dynamically.
     *
     * @param charset The character set to be used for encoding and decoding C-style strings.
     */
    // 특정 인코딩을 사용하는 C 문자열 처리
    public StringCField(Charset charset) {
        super(-1); // 문자열 길이를 결정할 수 없으므로 Span은 동적으로 처리.
        this.charset = charset;
    }

    @Override
    public byte[] encode(String value) {
        if (value == null) {
            return new byte[0];
        }

        // 문자열을 바이트 배열로 변환
        byte[] stringBytes = value.getBytes(charset);

        // 마지막에 널 종료 문자를 추가
        byte[] result = new byte[stringBytes.length + 1];
        System.arraycopy(stringBytes, 0, result, 0, stringBytes.length);
        result[stringBytes.length] = 0;

        setSpan(result.length);

        return result;
    }

    @Override
    public String decode(byte[] bytes, int offset) {
        int length = calculateSpan(bytes, offset);
        setSpan(length);

        ArrayList<Byte> resultBytes = new ArrayList<>();

        // 바이트 배열을 탐색하며 종료 문자인 `\0`을 찾음
        for (int i = offset; i < bytes.length; i++) {
            if (bytes[i] == 0) {
                break;
            }
            resultBytes.add(bytes[i]);
        }

        // 결과 바이트 값을 문자열로 변환
        byte[] stringBytes = new byte[resultBytes.size()];
        for (int i = 0; i < resultBytes.size(); i++) {
            stringBytes[i] = resultBytes.get(i);
        }

        return new String(stringBytes, charset);
    }

    @Override
    public int calculateSpan(byte[] data, int offset) {
        for (int i = offset; i < data.length; i++) {
            if (data[i] == 0) {
                return i - offset + 1;
            }
        }
        throw new IllegalArgumentException("Null-terminated character not found");
    }

    public void setSpan(int span) {
        this.span = span;
    }

    public int getSpan() {
        return this.span;
    }

    @Override
    public String bytesToHex(byte[] bytes, int offset) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Cannot convert null or empty data to hex.");
        }

        StringBuilder hexBuilder = new StringBuilder();

        for (int i = offset; i < bytes.length; i++) {
            hexBuilder.append(String.format("%02X", bytes[i]));
            hexBuilder.append(" ");

            if (bytes[i] == 0) {
                break;
            }
        }

        if (!hexBuilder.isEmpty()) {
            hexBuilder.setLength(hexBuilder.length() - 1);
        }

        return hexBuilder.toString();
    }

}
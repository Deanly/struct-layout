package net.deanly.structlayout.type.impl;

import lombok.extern.slf4j.Slf4j;
import net.deanly.structlayout.Layout;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Slf4j
public class CStringLayout extends Layout<String> {

    private final Charset charset; // 문자열 인코딩 방식

    // 기본 ASCII 기반의 C 문자열 처리
    public CStringLayout() {
        this(StandardCharsets.US_ASCII);
    }

    // 특정 인코딩을 사용하는 C 문자열 처리
    public CStringLayout(Charset charset) {
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
        int length = getSpan(bytes, offset);
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

    public int getSpan(byte[] bytes, int offset) {
        // 길이를 널 문자를 기준으로 결정해야 하므로 바이트 크기를 동적으로 계산
        for (int i = offset; i < bytes.length; i++) {
            if (bytes[i] == 0) {
                return i - offset + 1;
            }
        }
        throw new IllegalArgumentException("Null-terminated character not found");
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

    @Override
    public void printDebug(byte[] data, int offset, Field field) {
        if (isTestEnvironment()) {
            log.debug("[Field: {}.{}] Bytes: [{}] ({} bytes), Value: \"{}\"",
                    field.getDeclaringClass().getSimpleName(), field.getName(),
                    bytesToHex(data, offset), getSpan(data, offset), decode(data, offset));
        }
    }
}
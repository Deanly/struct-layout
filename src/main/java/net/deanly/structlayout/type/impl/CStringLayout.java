package net.deanly.structlayout.type.impl;

import net.deanly.structlayout.Layout;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
        byte[] result = new byte[stringBytes.length + 1]; // 1은 널 문자
        System.arraycopy(stringBytes, 0, result, 0, stringBytes.length);
        result[stringBytes.length] = 0; // 마지막 바이트에 널 문자 삽입

        return result;
    }

    @Override
    public String decode(byte[] bytes, int offset) {
        ArrayList<Byte> resultBytes = new ArrayList<>();

        // 바이트 배열을 탐색하며 종료 문자인 `\0`을 찾음
        for (int i = offset; i < bytes.length; i++) {
            if (bytes[i] == 0) {
                break; // 널 문자 발견 시 중단
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
                return i - offset + 1; // 널 문자까지 포함한 길이 반환
            }
        }
        throw new IllegalArgumentException("Null-terminated character not found");
    }
}
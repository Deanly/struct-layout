package net.deanly.structlayout.type.impl;

import net.deanly.structlayout.Layout;

public class UInt8Layout extends Layout<Integer> {

    public UInt8Layout(String property) {
        super(1, property); // 1 byte
    }

    public UInt8Layout() {
        this(null);
    }

    @Override
    public Integer decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        return data[offset] & 0xFF; // 부호 없는 8비트 정수
    }

    @Override
    public byte[] encode(Integer value) {
        if (value == null || value < 0 || value > 255) {
            throw new IllegalArgumentException("Value must be in the range 0 to 255.");
        }
        return new byte[]{value.byteValue()}; // 1바이트로 인코딩
    }
}
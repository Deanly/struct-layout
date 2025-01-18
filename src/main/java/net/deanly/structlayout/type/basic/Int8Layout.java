package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.Layout;

public class Int8Layout extends Layout<Byte> {

    public Int8Layout(String property) {
        super(1, property); // 1 byte
    }

    public Int8Layout() {
        this(null);
    }

    @Override
    public Byte decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        return data[offset]; // 데이터 자체가 부호 있는 8비트 정수
    }

    @Override
    public byte[] encode(Byte value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        return new byte[]{value}; // 1바이트로 인코딩
    }
}
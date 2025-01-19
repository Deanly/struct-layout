package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.Layout;

public class Int8Layout extends Layout<Short> {

    public Int8Layout(String property) {
        super(1, property); // 1 byte
    }

    public Int8Layout() {
        this(null);
    }

    @Override
    public Short decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        return (short) data[offset]; // 데이터 자체가 부호 있는 8비트 정수
    }

    @Override
    public byte[] encode(Short value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        return new byte[]{(byte) (value & 0xFF)};
    }
}
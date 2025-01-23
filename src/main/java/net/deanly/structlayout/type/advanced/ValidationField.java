package net.deanly.structlayout.type.advanced;

import net.deanly.structlayout.Field;

public abstract class ValidationField<T> extends Field<T> {

    public ValidationField(int span, String property) {
        super(span, property);
    }

    @Override
    public T decode(byte[] data, int offset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (offset < 0 || offset + getSpan() > data.length) {
            throw new IndexOutOfBoundsException("Offset and span exceed data bounds.");
        }
        return performDecode(data, offset);
    }

    @Override
    public byte[] encode(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        return performEncode(value);
    }

    protected abstract T performDecode(byte[] data, int offset);

    protected abstract byte[] performEncode(T value);
}
package net.deanly.structlayout;

import lombok.Getter;
import net.deanly.structlayout.codec.Encoder;
import net.deanly.structlayout.codec.Decoder;

import java.nio.ByteBuffer;

/**
 * Abstract class providing encoding and decoding functionality.
 * @param <T> The type of the value being encoded/decoded.
 */
@Getter
public abstract class Layout<T> implements Encoder<T>, Decoder<T> {
    private final int span; // Number of bytes for this layout
    private final String property; // Optional associated property name

    public Layout(int span, String property) {
        this.span = span;
        this.property = property;
    }

    public Layout(int span) {
        this(span, null);
    }

    public abstract byte[] encode(T value);
    public abstract T decode(byte[] bytes, int offset);

}
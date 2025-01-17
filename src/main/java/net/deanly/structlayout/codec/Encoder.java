package net.deanly.structlayout.codec;

import java.nio.ByteBuffer;

/**
 * Interface for encoding data into a buffer.
 * @param <T> The type of the value to encode.
 */
public interface Encoder<T> {
    byte[] encode(T value);
}
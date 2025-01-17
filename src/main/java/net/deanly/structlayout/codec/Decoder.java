package net.deanly.structlayout.codec;

import java.nio.ByteBuffer;

/**
 * Interface for decoding data from a buffer.
 * @param <T> The type of the decoded value.
 */
public interface Decoder<T> {
    T decode(byte[] bytes, int offset);
}
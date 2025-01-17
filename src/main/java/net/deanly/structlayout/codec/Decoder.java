package net.deanly.structlayout.codec;

import java.nio.ByteBuffer;

/**
 * Interface for decoding data from a buffer.
 * @param <T> The type of the decoded value.
 */
public interface Decoder<T> {

    /**
     * Decode the data at the specific offset in the given byte array.
     * The method reads and handles only the required number of bytes (specified by getSpan())
     * starting from the offset, ignoring other parts of the byte array.
     *
     * @param bytes The full byte array containing the structured data.
     * @param offset The starting point to decode the specific data.
     * @return The decoded value.
     */
    T decode(byte[] bytes, int offset);
}
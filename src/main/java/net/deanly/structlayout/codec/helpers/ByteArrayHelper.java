package net.deanly.structlayout.codec.helpers;

import java.util.List;

/**
 * A utility class that provides helper methods for manipulating byte arrays.
 */
public class ByteArrayHelper {


    /**
     * Merges multiple byte array chunks into a single byte array.
     *
     * @param chunks the list of byte array chunks to be merged
     * @return a single byte array containing the concatenated data from all the given chunks
     */
    public static byte[] mergeChunks(List<byte[]> chunks) {
        int totalLength = chunks.stream().mapToInt(chunk -> chunk.length).sum();
        byte[] merged = new byte[totalLength];
        int offset = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, merged, offset, chunk.length);
            offset += chunk.length;
        }
        return merged;
    }

    /**
     * Merges multiple byte array chunks into a single byte array.
     *
     * @param chunks the variable-length argument list of byte array chunks to be merged
     * @return a single byte array containing the concatenated data from all the given chunks
     */
    public static byte[] mergeChunks(byte[]... chunks) {
        int totalLength = 0;
        for (byte[] chunk : chunks) {
            totalLength += chunk.length;
        }
        byte[] merged = new byte[totalLength];
        int offset = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, merged, offset, chunk.length);
            offset += chunk.length;
        }
        return merged;
    }


    /**
     * Returns a ByteBuffer that provides a view of the specified sub-region
     * of the given byte array, starting at the specified offset and length.
     *
     * @param data   The source byte array.
     * @param offset The starting offset of the sub-region.
     * @param length The length of the sub-region.
     * @return A ByteBuffer that represents the sub-region.
     * @throws IllegalArgumentException if the offset or length is invalid.
     */
    public static byte[] extractSubArray(byte[] data, int offset, int length) {
        if (data == null || offset < 0 || length < 0 || offset + length > data.length) {
            throw new IllegalArgumentException("Invalid offset or length.");
        }

        // Handling empty subarray
        if (length == 0) {
            return new byte[0];
        }

        // Copy the desired portion (minimal copying)
        byte[] result = new byte[length];
        System.arraycopy(data, offset, result, 0, length);
        return result;
    }
}
package net.deanly.structlayout.codec.helpers;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ByteArrayHelperTest {

    @Test
    void testMergeChunksWithList() {
        List<byte[]> chunks = new ArrayList<>();
        chunks.add(new byte[]{1, 2, 3});
        chunks.add(new byte[]{4, 5, 6});
        chunks.add(new byte[]{7, 8, 9});

        byte[] merged = ByteArrayHelper.mergeChunks(chunks);

        // 병합된 배열 확인
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, merged);
    }

    @Test
    void testMergeChunksWithVarArgs() {
        byte[] chunk1 = {1, 2, 3};
        byte[] chunk2 = {4, 5, 6};
        byte[] chunk3 = {7, 8, 9};

        byte[] merged = ByteArrayHelper.mergeChunks(chunk1, chunk2, chunk3);

        // 병합된 배열 확인
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, merged);
    }

    @Test
    void testMergeChunksEmptyList() {
        List<byte[]> chunks = new ArrayList<>();

        byte[] merged = ByteArrayHelper.mergeChunks(chunks);

        // 빈 배열 확인
        assertArrayEquals(new byte[]{}, merged);
    }

    @Test
    void testMergeChunksEmptyVarArgs() {
        byte[] merged = ByteArrayHelper.mergeChunks();

        // 빈 배열 확인
        assertArrayEquals(new byte[]{}, merged);
    }


    @Test
    public void testExtractSubArray_validInput() {
        byte[] data = {0, 1, 2, 3, 4, 5};
        byte[] result = ByteArrayHelper.extractSubArray(data, 2, 3);
        assertArrayEquals(new byte[]{2, 3, 4}, result);
    }

    @Test
    public void testExtractSubArray_fullArray() {
        byte[] data = {0, 1, 2, 3, 4, 5};
        byte[] result = ByteArrayHelper.extractSubArray(data, 0, data.length);
        assertArrayEquals(data, result);
    }

    @Test
    public void testExtractSubArray_emptySubArray() {
        byte[] data = {0, 1, 2, 3, 4, 5};
        byte[] result = ByteArrayHelper.extractSubArray(data, 3, 0);
        assertArrayEquals(new byte[]{}, result);
    }

    @Test
    public void testExtractSubArray_invalidOffset() {
        byte[] data = {0, 1, 2, 3, 4, 5};
        assertThrows(IllegalArgumentException.class, () ->
                ByteArrayHelper.extractSubArray(data, -1, 3)
        );
    }

    @Test
    public void testExtractSubArray_invalidLength() {
        byte[] data = {0, 1, 2, 3, 4, 5};
        assertThrows(IllegalArgumentException.class, () ->
                ByteArrayHelper.extractSubArray(data, 2, 10)
        );
    }

    @Test
    public void testExtractSubArray_nullArray() {
        assertThrows(IllegalArgumentException.class, () ->
                ByteArrayHelper.extractSubArray(null, 0, 3)
        );
    }
}
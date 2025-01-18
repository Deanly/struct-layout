import net.deanly.structlayout.type.impl.UInt64BELayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UInt64BELayoutTest {

    @Test
    void testEncode() {
        UInt64BELayout layout = new UInt64BELayout();

        // Test values
        long[] values = {0L, 1L, 4294967295L, 9223372036854775807L}; // Min, Max, Large unsigned integers

        for (long value : values) {
            byte[] encoded = layout.encode(value);

            // Assert that encoding produces exactly 8 bytes
            assertNotNull(encoded, "Encoded byte array should not be null");
            assertEquals(8, encoded.length, "Encoded byte array should have 8 bytes");

            // Decode and validate that the decoded result matches the original value
            long decoded = layout.decode(encoded, 0);
            assertEquals(value, decoded, "Decoded value should match the original input value");
        }
    }

    @Test
    void testDecode() {
        UInt64BELayout layout = new UInt64BELayout();

        // Test case: Fixed big-endian representation
        byte[] data = {0, 0, 0, 2, 0, 0, 0, 1}; // Represents 0x0000000200000001
        long expected = (2L << 32) | 1L;

        long decoded = layout.decode(data, 0);
        assertEquals(expected, decoded, "Decoded value should match the expected value");
    }

    @Test
    void testEncodeDecodeWithOffset() {
        UInt64BELayout layout = new UInt64BELayout();

        long value = 987654321987654321L;
        byte[] buffer = new byte[16];

        // Encode value at offset
        System.arraycopy(layout.encode(value), 0, buffer, 8, 8);

        // Decode value from specific offset
        long decoded = layout.decode(buffer, 8);
        assertEquals(value, decoded, "Decoded value should match the original value when using offset");
    }

    @Test
    void testInvalidInputs() {
        UInt64BELayout layout = new UInt64BELayout();

        // Test null input for decode
        assertThrows(IllegalArgumentException.class, () -> layout.decode(null, 0), "Null input should throw IllegalArgumentException");

        // Test negative value for encode
        assertThrows(IllegalArgumentException.class, () -> layout.encode(-1L), "Negative value should throw IllegalArgumentException");

        // Test insufficient data length
        byte[] insufficientData = new byte[7]; // Less than 8 bytes
        assertThrows(IllegalArgumentException.class, () -> layout.decode(insufficientData, 0), "Insufficient data length should throw IllegalArgumentException");

        // Test invalid offset
        byte[] validData = new byte[8];
        assertThrows(IllegalArgumentException.class, () -> layout.decode(validData, 9), "Invalid offset should throw IllegalArgumentException");
    }
}
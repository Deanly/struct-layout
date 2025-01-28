package net.deanly.structlayout.anootation;

import lombok.Getter;
import lombok.Setter;
import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class CustomFieldTest {

    @Getter
    @Setter
    public static class TestStruct {

        @StructField(order = 1, type = PublicKeyField.class)
        private String publicKey;

    }


    @Test
    public void testEncodeAndDecodeWithValidPublicKey() {
        // Arrange
        TestStruct original = new TestStruct();
        String validPublicKey = "12345678901234567890123456789012"; // Exactly 32 bytes
        original.setPublicKey(validPublicKey);

        // Act: Encode
        byte[] encodedData = StructLayout.encode(original);

        // Assert: Encoded byte length should match the layout
        assertNotNull(encodedData, "Encoded data should not be null");
        assertEquals(32, encodedData.length, "Encoded public key must be 32 bytes");

        // Act: Decode
        TestStruct deserialized = StructLayout.decode(encodedData, TestStruct.class);

        // Assert: Decoded publicKey value
        assertNotNull(deserialized, "Deserialized object should not be null");
        assertEquals(validPublicKey, deserialized.getPublicKey(), "Decoded public key must match the original value");
    }

    @Test
    public void testEncodeWithInvalidPublicKeyLength() {
        // Arrange
        TestStruct struct = new TestStruct();
        String invalidPublicKey = "short_key"; // Not 32 bytes long
        struct.setPublicKey(invalidPublicKey);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> StructLayout.encode(struct));
        assertTrue(exception.getMessage().contains("PublicKey must be exactly 32 bytes."), "Exception message should contain 'PublicKey must be exactly 32 bytes.'");
    }

    @Test
    public void testDecodeWithInvalidBufferLength() {
        // Arrange
        byte[] invalidBuffer = "short_buffer".getBytes(); // Not 32 bytes long

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> StructLayout.decode(invalidBuffer, TestStruct.class));
        assertTrue(exception.getMessage().contains("publicKey"), "Exception message should contain 'publicKey'");
    }

    @Test
    public void testEncodeWithNullPublicKey() {
        // Arrange
        TestStruct struct = new TestStruct();
        struct.setPublicKey(null); // Null value

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> StructLayout.encode(struct));
        assertTrue(exception.getMessage().contains("PublicKey value cannot be null"), "Exception message should contain 'PublicKey value cannot be null'");
    }

    @Test
    public void testDecodeWithOffset() {
        // Arrange
        TestStruct struct = new TestStruct();
        String validPublicKey = "12345678901234567890123456789012";
        struct.setPublicKey(validPublicKey);

        // Encode the struct
        byte[] encodedData = StructLayout.encode(struct);

        // Add some padding bytes before the actual encoded data
        byte[] paddedData = new byte[40]; // Add 8 extra bytes as padding
        System.arraycopy("PADDING_".getBytes(), 0, paddedData, 0, 8); // Padding
        System.arraycopy(encodedData, 0, paddedData, 8, encodedData.length); // Encoded data

        // Act: Decode with offset
        TestStruct deserialized = StructLayout.decode(paddedData, TestStruct.class);

        // Assert: Check if decoding works correctly with offset
        assertNotNull(deserialized, "Deserialized object should not be null");
        assertNotEquals(validPublicKey, deserialized.getPublicKey(), "Decoded public key must match the original value");
    }

    public static class PublicKeyField extends FieldBase<String> {

        private static final int PUBLIC_KEY_LENGTH = 32; // 32 bytes

        public PublicKeyField() {
            super(PUBLIC_KEY_LENGTH);
        }

        @Override
        public byte[] encode(String value) {
            if (value == null) {
                throw new IllegalArgumentException("PublicKey value cannot be null.");
            }
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

            if (bytes.length != PUBLIC_KEY_LENGTH) {
                throw new IllegalArgumentException("PublicKey must be exactly " + PUBLIC_KEY_LENGTH + " bytes.");
            }

            return bytes;
        }

        @Override
        public String decode(byte[] buffer, int offset) {
            if (buffer == null || buffer.length - offset < PUBLIC_KEY_LENGTH) {
                throw new IllegalArgumentException("Buffer does not contain enough data for a PublicKey.");
            }

            return new String(buffer, offset, PUBLIC_KEY_LENGTH, StandardCharsets.UTF_8);
        }


    }
}

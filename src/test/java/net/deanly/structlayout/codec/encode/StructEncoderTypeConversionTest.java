package net.deanly.structlayout.codec.encode;

import net.deanly.structlayout.type.DataType;
import net.deanly.structlayout.Layout;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class StructEncoderTypeConversionTest {

    @Test
    public void testUInt8ToShortConversion() {
        // Arrange
        Layout<Short> layout = createLayout(DataType.UINT8);
        short expectedValue = 255; // Max value for UINT8
        byte[] encoded = layout.encode(expectedValue);

        // Act
        Short decodedValue = layout.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, "UINT8 -> Short conversion failed.");
    }

    @Test
    public void testUInt16ToIntegerConversion() {
        // Arrange
        Layout<Integer> layout = createLayout(DataType.UINT16_BE);
        int expectedValue = 65535; // Max value for UINT16
        byte[] encoded = layout.encode(expectedValue);

        // Act
        Integer decodedValue = layout.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, "UINT16 -> Integer conversion failed.");
    }

    @Test
    public void testUInt32ToLongConversion() {
        // Arrange
        Layout<Long> layout = createLayout(DataType.UINT32_LE);
        long expectedValue = 4294967295L; // Max value for UINT32
        byte[] encoded = layout.encode(expectedValue);

        // Act
        Long decodedValue = layout.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, "UINT32 -> Long conversion failed.");
    }

    @Test
    public void testUInt64ToBigIntegerConversion() {
        // Arrange
        Layout<BigInteger> layout = createLayout(DataType.UINT64_LE);
        BigInteger expectedValue = new BigInteger("18446744073709551615"); // Max UINT64
        byte[] encoded = layout.encode(expectedValue);

        // Act
        BigInteger decodedValue = layout.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, "UINT64 -> BigInteger conversion failed.");
    }

    @Test
    public void testFloat32ToFloatConversion() {
        // Arrange
        Layout<Float> layout = createLayout(DataType.FLOAT32_BE);
        float expectedValue = 3.14f;
        byte[] encoded = layout.encode(expectedValue);

        // Act
        Float decodedValue = layout.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, 0.0001f, "FLOAT32 -> Float conversion failed.");
    }

    @Test
    public void testFloat64ToDoubleConversion() {
        // Arrange
        Layout<Double> layout = createLayout(DataType.FLOAT64_LE);
        double expectedValue = 1234567.89;
        byte[] encoded = layout.encode(expectedValue);

        // Act
        Double decodedValue = layout.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, 0.00001, "FLOAT64 -> Double conversion failed.");
    }

    @Test
    public void testInt64ToLongConversion() {
        // Arrange
        Layout<Long> layout = createLayout(DataType.INT64_LE);
        long expectedValue = -1234567890123456789L; // Large signed 64-bit integer
        byte[] encoded = layout.encode(expectedValue);

        // Act
        Long decodedValue = layout.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, "INT64 -> Long conversion failed.");
    }

    @Test
    public void testStringCLayoutConversion() {
        // Arrange
        Layout<String> layout = createLayout(DataType.STRING_C);
        String expectedValue = "Hello World!";
        byte[] encoded = layout.encode(expectedValue);

        // Act
        String decodedValue = layout.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, "STRING_C -> String conversion failed.");
    }

    @Test
    public void testInvalidUInt8Conversion() {
        // Arrange
        Layout<Short> layout = createLayout(DataType.UINT8);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            layout.encode((short) 256); // UINT8 Max = 255
        });

        assertTrue(exception.getMessage().contains("Value must be in the range 0 to 255"), "Expected out-of-range exception for UINT8.");
    }

    /**
     * Helper method to create a layout instance for a given DataType.
     *
     * @param dataType The DataType enum value.
     * @return The Layout instance associated with the specified DataType.
     */
    private <T> Layout<T> createLayout(DataType dataType) {
        try {
            return (Layout<T>) dataType.getLayout().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create layout for DataType: " + dataType, e);
        }
    }
}
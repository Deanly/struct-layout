package net.deanly.structlayout.codec.encode;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.BasicTypes;
import net.deanly.structlayout.type.guava.UnsignedLong;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class StructEncoderTypeConversionTest {

    @Test
    public void testUInt8ToShortConversion() {
        // Arrange
        FieldBase<Short> field = createLayout(BasicTypes.UINT8);
        short expectedValue = 255; // Max value for UINT8
        byte[] encoded = field.encode(expectedValue);

        // Act
        Short decodedValue = field.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, "UINT8 -> Short conversion failed.");
    }

    @Test
    public void testUInt16ToIntegerConversion() {
        // Arrange
        FieldBase<Integer> field = createLayout(BasicTypes.UINT16_BE);
        int expectedValue = 65535; // Max value for UINT16
        byte[] encoded = field.encode(expectedValue);

        // Act
        Integer decodedValue = field.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, "UINT16 -> Integer conversion failed.");
    }

    @Test
    public void testUInt32ToLongConversion() {
        // Arrange
        FieldBase<Long> field = createLayout(BasicTypes.UINT32_LE);
        long expectedValue = 4294967295L; // Max value for UINT32
        byte[] encoded = field.encode(expectedValue);

        // Act
        Long decodedValue = field.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, "UINT32 -> Long conversion failed.");
    }

    @Test
    public void testUInt64ToBigIntegerConversion() {
        // Arrange
        FieldBase<UnsignedLong> field = createLayout(BasicTypes.UINT64_LE);
        UnsignedLong expectedValue = UnsignedLong.valueOf("18446744073709551615"); // Max UINT64
        byte[] encoded = field.encode(expectedValue);

        // Act
        UnsignedLong decodedValue = field.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, "UINT64 -> UnsignedLong conversion failed.");
    }

    @Test
    public void testFloat32ToFloatConversion() {
        // Arrange
        FieldBase<Float> field = createLayout(BasicTypes.FLOAT32_BE);
        float expectedValue = 3.14f;
        byte[] encoded = field.encode(expectedValue);

        // Act
        Float decodedValue = field.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, 0.0001f, "FLOAT32 -> Float conversion failed.");
    }

    @Test
    public void testFloat64ToDoubleConversion() {
        // Arrange
        FieldBase<Double> field = createLayout(BasicTypes.FLOAT64_LE);
        double expectedValue = 1234567.89;
        byte[] encoded = field.encode(expectedValue);

        // Act
        Double decodedValue = field.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, 0.00001, "FLOAT64 -> Double conversion failed.");
    }

    @Test
    public void testInt64ToLongConversion() {
        // Arrange
        FieldBase<Long> field = createLayout(BasicTypes.INT64_LE);
        long expectedValue = -1234567890123456789L; // Large signed 64-bit integer
        byte[] encoded = field.encode(expectedValue);

        // Act
        Long decodedValue = field.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, "INT64 -> Long conversion failed.");
    }

    @Test
    public void testStringCLayoutConversion() {
        // Arrange
        FieldBase<String> field = createLayout(BasicTypes.STRING_C);
        String expectedValue = "Hello World!";
        byte[] encoded = field.encode(expectedValue);

        // Act
        String decodedValue = field.decode(encoded, 0);

        // Assert
        assertEquals(expectedValue, decodedValue, "STRING_C -> String conversion failed.");
    }

    @Test
    public void testInvalidUInt8Conversion() {
        // Arrange
        FieldBase<Short> field = createLayout(BasicTypes.UINT8);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            field.encode((short) 256); // UINT8 Max = 255
        });

        assertTrue(exception.getMessage().contains("Value must be in the range 0 to 255"), "Expected out-of-range exception for UINT8.");
    }

    /**
     * Helper method to create a layout instance for a given Field.
     *
     * @param basicTypes The Field enum value.
     * @return The Layout instance associated with the specified Field.
     */
    @SuppressWarnings("unchecked")
    private <T> FieldBase<T> createLayout(Class<? extends FieldBase<?>> basicTypes) {
        try {
            return (FieldBase<T>) basicTypes.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create layout for Field: " + basicTypes, e);
        }
    }
}
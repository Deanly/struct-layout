package net.deanly.structlayout.codec.helpers;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.exception.TypeConversionException;
import net.deanly.structlayout.type.BasicTypes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TypeConverterHelperTest {

    @Test
    public void testConvertToType_NumberConversions() {
        // Integer type tests
        assertEquals((byte) 127, TypeConverterHelper.convertToType(127L, Byte.class));
        assertEquals((short) 32000, TypeConverterHelper.convertToType(32000L, Short.class));
        assertEquals(123456, TypeConverterHelper.convertToType(123456L, Integer.class));
        assertEquals(10000000000L, TypeConverterHelper.convertToType(10000000000L, Long.class));

        // Floating-point type tests
        assertEquals(123.45f, TypeConverterHelper.convertToType(123.45, Float.class));
        assertEquals(123.45, TypeConverterHelper.convertToType(123.45, Double.class));

        // BigInteger type tests
        assertEquals(new BigInteger("123456789"), TypeConverterHelper.convertToType(123456789L, BigInteger.class));
    }

    @Test
    public void testConvertToType_SpecialTypes() {
        // BigDecimal conversion
        assertEquals(new BigDecimal("12345.67"), TypeConverterHelper.convertToType("12345.67", BigDecimal.class));

        // LocalDate conversion
        assertEquals(LocalDate.of(2023, 10, 14),
                TypeConverterHelper.convertToType("2023-10-14", LocalDate.class));
        // LocalDateTime conversion
        assertEquals(LocalDateTime.of(2023, 10, 14, 12, 30),
                TypeConverterHelper.convertToType("2023-10-14T12:30", LocalDateTime.class));
        // ZonedDateTime conversion
        assertEquals(ZonedDateTime.parse("2023-10-14T12:30:00Z"),
                TypeConverterHelper.convertToType("2023-10-14T12:30:00Z", ZonedDateTime.class));
    }

    @Test
    public void testConvertToType_UUIDConversion() {
        // UUID conversion
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, TypeConverterHelper.convertToType(uuid.toString(), UUID.class));
    }

    @Test
    public void testConvertToType_StringConversions() {
        // String to numeric types
        assertEquals(123, TypeConverterHelper.convertToType("123", Integer.class));
        assertEquals(123.45, TypeConverterHelper.convertToType("123.45", Double.class));

        // Object to String
        assertEquals("test", TypeConverterHelper.convertToType("test", String.class));
        assertEquals("123", TypeConverterHelper.convertToType(123, String.class));
    }

    @Test
    public void testConvertToType_EnumConversions() {
        // Enum conversion
        assertEquals(Thread.State.WAITING, TypeConverterHelper.convertToType("WAITING", Thread.State.class));
        assertEquals(Thread.State.RUNNABLE, TypeConverterHelper.convertToType("RUNNABLE", Thread.State.class));
    }

    @Test
    public void testConvertToType_NaNHandling() {
        // NaN conversion
        assertEquals(0.0, TypeConverterHelper.convertToType(Double.NaN, Double.class));
        assertEquals(0.0f, TypeConverterHelper.convertToType(Float.NaN, Float.class));

        // Infinity conversion
        assertEquals(Double.MAX_VALUE, TypeConverterHelper.convertToType(Double.POSITIVE_INFINITY, Double.class));
        assertEquals(-Double.MAX_VALUE, TypeConverterHelper.convertToType(Double.NEGATIVE_INFINITY, Double.class));
    }

    @Test
    public void testConvertToType_NullHandling() {
        // Null to numeric type
        assertEquals(0, TypeConverterHelper.convertToType(null, Integer.class));
        assertEquals(0.0, TypeConverterHelper.convertToType(null, Double.class));
        assertEquals(0.0f, TypeConverterHelper.convertToType(null, Float.class));
        assertEquals(0, TypeConverterHelper.convertToType(null, int.class));
        assertEquals(0L, TypeConverterHelper.convertToType(null, long.class));
        // Null to String
        assertEquals("", TypeConverterHelper.convertToType(null, String.class));
        // Null to general Object
        assertNull(TypeConverterHelper.convertToType(null, Object.class));
    }

    @Test
    public void testConvertWithValidation() {
        // Valid case
        assertEquals(123, TypeConverterHelper.convertWithValidation(123, Integer.class, value -> value instanceof Integer && (Integer) value > 0));

        // Invalid case
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                TypeConverterHelper.convertWithValidation(-1, Integer.class, value -> value instanceof Integer && (Integer) value > 0));
        assertTrue(exception.getMessage().contains("Value is invalid"));
    }

    @Test
    public void testConvertToLayoutType_WithValidDataType() {
        // Mocked Field
        Class<? extends FieldBase<?>> basicTypes = BasicTypes.INT32_LE;

        assertEquals(123, TypeConverterHelper.convertToLayoutType("123", basicTypes));
        assertEquals(0, TypeConverterHelper.convertToLayoutType(null, basicTypes));
    }

    @Test
    public void testConvertToLayoutType_InvalidConversion() {
        // Mocked Field
        Class<? extends FieldBase<?>> basicTypes = BasicTypes.INT32_BE;

        Exception exception = assertThrows(TypeConversionException.class, () ->
                TypeConverterHelper.convertToLayoutType("Not a number", basicTypes));
        assertTrue(exception.getMessage().contains("Cannot convert String to"));
    }

    @Test
    public void testConvertToType_InvalidConversion() {
        Exception exception = assertThrows(TypeConversionException.class, () ->
                TypeConverterHelper.convertToType("InvalidNumber", Integer.class));
        assertTrue(exception.getMessage().contains("Cannot convert String to"));
    }

    @Test
    public void testConvertToType_OutOfRangeNumber() {
        // Exceeding Byte range
        Exception exception = assertThrows(TypeConversionException.class, () ->
                TypeConverterHelper.convertToType(128, Byte.class));
        assertTrue(exception.getMessage().contains("out of range for type"));

        // Exceeding Short range
        exception = assertThrows(TypeConversionException.class, () ->
                TypeConverterHelper.convertToType(40000, Short.class));
        assertTrue(exception.getMessage().contains("out of range for type"));
    }

    @Test
    public void testConvertFromString() {
        // 숫자형 변환 테스트
        assertEquals((short) 123, TypeConverterHelper.convertToType("123", short.class));
        assertEquals((byte) 10, TypeConverterHelper.convertToType("10", byte.class));
        assertEquals(1000, TypeConverterHelper.convertToType("1000", int.class));
        assertEquals(123456789L, TypeConverterHelper.convertToType("123456789", long.class));
        assertEquals(12.34f, (float) TypeConverterHelper.convertToType("12.34", float.class), 0.0001);
        assertEquals(56.78, (double) TypeConverterHelper.convertToType("56.78", double.class), 0.0001);
        assertEquals(new java.math.BigInteger("987654321"), TypeConverterHelper.convertToType("987654321", java.math.BigInteger.class));
        assertEquals(new java.math.BigDecimal("12345.6789"), TypeConverterHelper.convertToType("12345.6789", java.math.BigDecimal.class));

        // Boolean 변환 테스트
        assertEquals(true, TypeConverterHelper.convertToType("true", boolean.class));
        assertEquals(false, TypeConverterHelper.convertToType("false", boolean.class));
        assertEquals(true, TypeConverterHelper.convertToType("TRUE", Boolean.class));

        // Character 변환 테스트
        assertEquals('A', TypeConverterHelper.convertToType("A", char.class));
        assertEquals('Z', TypeConverterHelper.convertToType("Z", Character.class));

        // 잘못된 입력 테스트
        assertThrows(TypeConversionException.class, () -> TypeConverterHelper.convertToType("notANumber", int.class));
        assertThrows(TypeConversionException.class, () -> TypeConverterHelper.convertToType("12.34", char.class)); // 길이가 1 이상
        assertThrows(TypeConversionException.class, () -> TypeConverterHelper.convertToType("invalid", java.math.BigInteger.class));
    }
}
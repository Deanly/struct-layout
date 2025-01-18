package net.deanly.structlayout.codec.helpers;

import net.deanly.structlayout.exception.StructParsingException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NumberConversionHelperTest {

    @Test
    void testInt8Conversion() {
        assertEquals((byte) 127, NumberConversionHelper.convertToExpectedNumericType(127, Byte.class, "field1"));
        assertThrows(IllegalArgumentException.class, () ->
                NumberConversionHelper.convertToExpectedNumericType(128, Byte.class, "field2"));
    }

    @Test
    void testInt16Conversion() {
        assertEquals((short) 32767, NumberConversionHelper.convertToExpectedNumericType(32767, Short.class, "field1"));
        assertThrows(IllegalArgumentException.class, () ->
                NumberConversionHelper.convertToExpectedNumericType(32768, Short.class, "field2"));
    }

    @Test
    void testInt32Conversion() {
        assertEquals(2147483647, NumberConversionHelper.convertToExpectedNumericType(2147483647L, Integer.class, "field1"));
        assertThrows(IllegalArgumentException.class, () ->
                NumberConversionHelper.convertToExpectedNumericType(2147483648L, Integer.class, "field2"));
    }

    @Test
    void testInt64Conversion() {
        assertEquals(123L, NumberConversionHelper.convertToExpectedNumericType(123, Long.class, "field1"));
    }

    @Test
    void testInvalidFloatConversion() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberConversionHelper.convertToExpectedNumericType(1.23f, Float.class, "field1"));
        assertThrows(IllegalArgumentException.class, () ->
                NumberConversionHelper.convertToExpectedNumericType(1.23, Double.class, "field2"));
    }

    @Test
    void testUnsupportedType() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberConversionHelper.convertToExpectedNumericType("InvalidType", String.class, "field1"));
    }

    @Test
    void testUnsupportedExpectedType() {
        // 기대 타입이 부동소수점(Float, Double)
        assertThrows(IllegalArgumentException.class, () ->
                NumberConversionHelper.convertToExpectedNumericType(123, Float.class, "floatTypeField"));
        assertThrows(IllegalArgumentException.class, () ->
                NumberConversionHelper.convertToExpectedNumericType(123, Double.class, "doubleTypeField"));

        // 기대 타입이 정수가 아님 (예: String)
        assertThrows(IllegalArgumentException.class, () ->
                NumberConversionHelper.convertToExpectedNumericType(123, String.class, "stringTypeField"));
    }

    @Test
    void testUnsupportedValueType() {
        // Value로 소수점(Float, Double)이 들어왔을 때
        assertThrows(IllegalArgumentException.class, () ->
                NumberConversionHelper.convertToExpectedNumericType(1.23f, Integer.class, "floatField"));
        assertThrows(IllegalArgumentException.class, () ->
                NumberConversionHelper.convertToExpectedNumericType(1.23, Integer.class, "doubleField"));

        // Value로 정수가 아닌 타입 (예: String)
        assertThrows(IllegalArgumentException.class, () ->
                NumberConversionHelper.convertToExpectedNumericType("InvalidString", Integer.class, "stringField"));
    }

    @Test
    void testSupportedConversions() {
        // 일반적인 정수형 변환 테스트
        assertEquals(127, NumberConversionHelper.convertToExpectedNumericType((byte) 127, Integer.class, "intField"));
        assertEquals(12345L, NumberConversionHelper.convertToExpectedNumericType(12345, Long.class, "longField"));
        assertEquals((short) -32768, NumberConversionHelper.convertToExpectedNumericType(-32768, Short.class, "shortField"));
    }

    @Test
    void testConvertToInt() {
        assertEquals(123, NumberConversionHelper.convertToInt(123L, "testField"));
        assertEquals(127, NumberConversionHelper.convertToInt((byte) 127, "testField"));
        assertEquals(32767, NumberConversionHelper.convertToInt((short) 32767, "testField"));

        assertThrows(StructParsingException.class, () ->
                        NumberConversionHelper.convertToInt("InvalidType", "testField"),
                "Should throw exception for non-numeric type");
    }

    @Test
    void testConvertToPrimitiveArray() {
        List<Object> intValues = List.of(1, 2, 3);
        int[] intArray = (int[]) NumberConversionHelper.convertToPrimitiveArray(intValues, int.class);
        assertArrayEquals(new int[]{1, 2, 3}, intArray);

        List<Object> longValues = List.of(1L, 2L, 3L);
        long[] longArray = (long[]) NumberConversionHelper.convertToPrimitiveArray(longValues, long.class);
        assertArrayEquals(new long[]{1L, 2L, 3L}, longArray);

        List<Object> byteValues = List.of((byte) 5, (byte) 10);
        byte[] byteArray = (byte[]) NumberConversionHelper.convertToPrimitiveArray(byteValues, byte.class);
        assertArrayEquals(new byte[]{5, 10}, byteArray);

        List<Object> unsupportedValues = List.of("String");
        assertThrows(UnsupportedOperationException.class, () ->
                        NumberConversionHelper.convertToPrimitiveArray(unsupportedValues, int.class),
                "Should throw exception for unsupported value type in List");
    }
}
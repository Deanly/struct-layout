package net.deanly.structlayout.type.borsh;

import lombok.Getter;
import lombok.Setter;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.basic.Int32LEField;
import net.deanly.structlayout.type.basic.StringCField;
import net.deanly.structlayout.type.basic.UInt64LEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class COptionFieldTest {

    public static class UInt8OptionField extends AbstractBorshOptionField<Short, UInt8Field> {
        @Override
        protected UInt8Field createField() {
            return new UInt8Field();
        }
    }

    public static class UInt64LEOptionField extends AbstractBorshOptionField<BigInteger, UInt64LEField> {
        @Override
        protected UInt64LEField createField() {
            return new UInt64LEField();
        }
    }

    public static class Int32OptionField extends AbstractBorshOptionField<Integer, Int32LEField> {
        @Override
        protected Int32LEField createField() {
            return new Int32LEField();
        }
    }

    public static class StringBorshOptionField extends AbstractBorshOptionField<String, StringCField> {
        @Override
        protected StringCField createField() {
            return new StringCField();
        }
    }

    @Getter
    @Setter
    public static class OptionalStruct {
        @StructField(order = 1, type = Int32OptionField.class)
        private Integer int32Value;

        @StructField(order = 2, type = UInt8OptionField.class)
        private Short optionalInt16Value;

        @StructField(order = 3, type = UInt64LEOptionField.class)
        private BigInteger optionalUInt64Value;

        @StructField(order = 4, type = StringBorshOptionField.class)
        private String message;
    }

    @Test
    public void testOptionalStruct() {
        // 1. 구조체 정의 및 데이터 설정
        OptionalStruct original = new OptionalStruct();
        original.setInt32Value(100); // 기본 int 값
        original.setOptionalInt16Value((short) 200); // Optional<Short>
        original.setOptionalUInt64Value(new BigInteger("12345678901234567890")); // Optional<BigInteger>
        original.setMessage("Hello, OptionalStruct!"); // String 값

        // 2. 직렬화 (Serialize to Byte Array)
        byte[] encodedData = StructLayout.encode(original);

        // 3. 역직렬화 (Deserialize to Struct Object)
        OptionalStruct deserialized = StructLayout.decode(encodedData, OptionalStruct.class);

        // 4. 디코딩된 값 검증 (Assertions)
        assertEquals(original.getInt32Value(), deserialized.getInt32Value());
        assertEquals(original.getOptionalInt16Value(), deserialized.getOptionalInt16Value());
        assertEquals(original.getOptionalUInt64Value(), deserialized.getOptionalUInt64Value());
        assertEquals(original.getMessage(), deserialized.getMessage());

        // 5. 디버그 출력
        StructLayout.debug(deserialized);

        System.out.println("Decoded Struct:");
        System.out.println("Int32 Value: " + deserialized.getInt32Value());
        System.out.println("Optional Int16 Value: " + deserialized.getOptionalInt16Value());
        System.out.println("Optional UInt64 Value: " + deserialized.getOptionalUInt64Value());
        System.out.println("Message: " + deserialized.getMessage());
    }

    @Test
    public void testOptionalStructWithEmptyFields() {
        // 1. 빈 Optional을 가진 구조체 정의
        OptionalStruct original = new OptionalStruct();
        original.setInt32Value(null);
        original.setOptionalInt16Value(null);
        original.setOptionalUInt64Value(null);
        original.setMessage(null); // 기본 문자열

        // 2. 직렬화 진행
        byte[] encodedData = StructLayout.encode(original);
        StructLayout.debug(encodedData);

        // 3. 역직렬화 진행
        OptionalStruct deserialized = StructLayout.decode(encodedData, OptionalStruct.class);
        StructLayout.debug(deserialized);

        // 4. 검증
        assertEquals(original.getInt32Value(), deserialized.getInt32Value());
        assertEquals(original.getOptionalInt16Value(), deserialized.getOptionalInt16Value());
        assertEquals(original.getOptionalUInt64Value(), deserialized.getOptionalUInt64Value());
        assertEquals(original.getMessage(), deserialized.getMessage());

        // 5. 디버그 출력
        StructLayout.debug(deserialized);

        System.out.println("Decoded Struct (Empty Optionals):");
        System.out.println("Int32 Value: " + deserialized.getInt32Value());
        System.out.println("Optional Int16 Value: " + deserialized.getOptionalInt16Value());
        System.out.println("Optional UInt64 Value: " + deserialized.getOptionalUInt64Value());
        System.out.println("Message: " + deserialized.getMessage());
    }


    @Test
    public void testUInt8OptionField() {
        UInt8OptionField field = new UInt8OptionField();

        // Optional Some: 값 설정
        byte[] encoded = field.encode((short) 120);
        Short decoded = field.decode(encoded, 0);

        assertNotNull(decoded);
        assertEquals((short) 120, decoded);
    }

    @Test
    public void testUInt8OptionFieldNone() {
        UInt8OptionField field = new UInt8OptionField();

        // Optional None: 값 없음
        byte[] encoded = field.encode(null);
        Short decoded = field.decode(encoded, 0);

        assertNull(decoded);
    }

    @Test
    public void testUInt64LEOptionField() {
        UInt64LEOptionField field = new UInt64LEOptionField();

        // Optional Some: 큰 값 설정
        BigInteger originalValue = new BigInteger("12345678901234567890");
        byte[] encoded = field.encode(originalValue);
        BigInteger decoded = field.decode(encoded, 0);

        assertNotNull(decoded);
        assertEquals(originalValue, decoded);
    }

    @Test
    public void testNullHandlingForPrimitiveAndReferenceTypes() {
        UInt8OptionField field = new UInt8OptionField();

        // Reference type (Short): null encoded/decoded as null
        byte[] encodedNull = field.encode(null);
        Short decodedNull = field.decode(encodedNull, 0);

        assertNull(decodedNull, "Reference type null value should be decoded as null");

        // Primitive type (int -> Short): null encoded/decoded as default (0)
        UInt8OptionField primitiveField = new UInt8OptionField();
        byte[] encodedPrimitive = primitiveField.encode(null);
        Short decodedPrimitive = primitiveField.decode(encodedPrimitive, 0);

        assertEquals(null, decodedPrimitive, "Primitive type null value should be decoded as null");
    }

    @Test
    public void testOptionalFieldsWithMixedNullValues() {
        // 1. 데이터 설정 - 일부 값만 초기화
        OptionalStruct original = new OptionalStruct();
        original.setInt32Value(300); // 값 존재
        original.setOptionalInt16Value(null); // 값 없음
        original.setOptionalUInt64Value(new BigInteger("9876543210123456789")); // 값 존재
        original.setMessage(null); // 값 없음

        // 2. 직렬화
        byte[] encodedData = StructLayout.encode(original);
        StructLayout.debug(encodedData); // 디버깅

        // 3. 역직렬화
        OptionalStruct deserialized = StructLayout.decode(encodedData, OptionalStruct.class);
        StructLayout.debug(deserialized);

        // 4. 검증
        assertEquals(original.getInt32Value(), deserialized.getInt32Value(), "Int32Value mismatch");
        assertEquals(original.getOptionalInt16Value(), deserialized.getOptionalInt16Value(), "OptionalInt16Value mismatch");
        assertEquals(original.getOptionalUInt64Value(), deserialized.getOptionalUInt64Value(), "OptionalUInt64Value mismatch");
        assertEquals(original.getMessage(), deserialized.getMessage(), "Message mismatch");
    }

    @Test
    public void testEncodeDecodeWithExtremeValues() {
        UInt64LEOptionField field = new UInt64LEOptionField();

        // BigInteger extreme test
        BigInteger maxValue = new BigInteger("18446744073709551615"); // UINT64_MAX
        byte[] encodedMax = field.encode(maxValue);
        BigInteger decodedMax = field.decode(encodedMax, 0);

        assertEquals(maxValue, decodedMax, "Decoded max value mismatch");

        BigInteger zero = BigInteger.ZERO;
        byte[] encodedZero = field.encode(zero);
        BigInteger decodedZero = field.decode(encodedZero, 0);

        assertEquals(zero, decodedZero, "Decoded zero mismatch");
    }

    @Test
    public void testStringOptionFieldHandling() {
        StringBorshOptionField field = new StringBorshOptionField();

        // Valid string test
        String originalValue = "TestString";
        byte[] encoded = field.encode(originalValue);
        String decoded = field.decode(encoded, 0);

        assertNotNull(decoded, "Decoded string should not be null");
        assertEquals(originalValue, decoded, "Decoded string mismatch");

        // null string test
        byte[] encodedNull = field.encode(null);
        String decodedNull = field.decode(encodedNull, 0);

        assertNull(decodedNull, "Decoded string should be null for original null");
    }

    @Test
    public void testMixedOptionalStructSerialization() {
        // 1. 데이터 설정
        OptionalStruct original = new OptionalStruct();
        original.setInt32Value(42);
        original.setOptionalInt16Value((short) 7);
        original.setOptionalUInt64Value(null); // null 포함
        original.setMessage("A message");

        // 2. 직렬화 수행
        byte[] encodedData = StructLayout.encode(original);
        StructLayout.debug(encodedData);

        // 3. 역직렬화 수행
        OptionalStruct deserialized = StructLayout.decode(encodedData, OptionalStruct.class);
        StructLayout.debug(deserialized);

        // 4. 검증
        assertEquals(original.getInt32Value(), deserialized.getInt32Value());
        assertEquals(original.getOptionalInt16Value(), deserialized.getOptionalInt16Value());
        assertNull(deserialized.getOptionalUInt64Value(), "Decoded null BigInteger field mismatch");
        assertEquals(original.getMessage(), deserialized.getMessage());
    }

    @Test
    public void testFieldSpanCalculation() {
        UInt8OptionField field = new UInt8OptionField();
        byte[] nullEncoded = field.encode(null);
        int span = field.calculateSpan(nullEncoded, 0);

        assertEquals(1, span, "Span for null value should be 1 byte");

        byte[] someEncoded = field.encode((short) 150);
        int someSpan = field.calculateSpan(someEncoded, 0);

        assertEquals(2, someSpan, "Span for a value (tag + data) should be 2 bytes");
    }

}
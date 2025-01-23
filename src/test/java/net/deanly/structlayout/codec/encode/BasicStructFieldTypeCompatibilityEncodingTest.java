package net.deanly.structlayout.codec.encode;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.exception.TypeConversionException;
import net.deanly.structlayout.type.BasicTypes;
import net.deanly.structlayout.type.basic.Float32LEField;
import net.deanly.structlayout.type.basic.UInt16BEField;
import net.deanly.structlayout.type.basic.UInt32LEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BasicStructFieldTypeCompatibilityEncodingTest {

    // **1. 호환 가능한 필드 테스트**
    @Test
    public void testCompatibleTypeTypes() {
        CompatibleStruct instance = new CompatibleStruct();
        instance.uint8AsInt = 255;            // UINT8에 적합한 값
        instance.uint16AsLong = 65535L;       // UINT16에 적합한 값
        instance.float32AsDouble = 3.14;     // FLOAT32에 적합한 값

        // Act
        byte[] encoded = StructEncoder.encode(instance);

        // Assert: 인코딩이 제대로 되었는지 확인
        assertNotNull(encoded);
    }

    // **2. 호환되지 않는 필드 테스트**
    @Test
    public void testIncompatibleTypeTypes() {
        IncompatibleStruct instance = new IncompatibleStruct();
        instance.uint8AsLong = 300L;  // UINT8의 최대값(255)을 초과한 long 값
        instance.uint16AsShort = -1; // UINT16에서 허용되지 않는 음수값
        instance.float32AsShort = 123; // FLOAT32에서 허용되지 않는 short 타입 값

        // Act & Assert
        // UINT8: 허용 범위를 넘어서는 값 사용 시 예외 발생
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            StructEncoder.encode(instance);
        });
        assertTrue(exception1.getMessage().contains("uint8AsLong"));

        // UINT16: 유형 및 범위 불일치 테스트
        instance.uint8AsLong = 100; // 이전 문제 무시하고 다른 필드 검증
        instance.uint16AsShort = Short.MIN_VALUE; // UINT16 범위 외 값을 short 로 정의
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            StructEncoder.encode(instance);
        });
        assertTrue(exception2.getMessage().contains("uint16AsShort"));

        // FLOAT32: short 사용 불가
        instance.uint16AsShort = 100;
        instance.float32AsShort = Double.MAX_VALUE;
        Exception exception3 = assertThrows(TypeConversionException.class, () -> {
            StructEncoder.encode(instance);
        });
        assertTrue(exception3.getMessage().contains("float32AsShort"));
    }

    // **3. SequenceField 및 CustomLayoutField 포함 테스트**
    @Test
    public void testComplexStructWithSequenceAndCustomLayout() {
        ComplexStruct instance = new ComplexStruct();
        instance.uint8Field = 128;             // UINT8
        instance.uint16Field = 10000;          // UINT16
        instance.float32Field = 1.23f;         // FLOAT32
        instance.sequenceField = Arrays.asList(10, 20, 30); // SequenceField 테스트
        instance.customField = "TestString";   // CustomLayoutField 테스트

        // Act
        byte[] encoded = StructEncoder.encode(instance);

        // Assert: 직렬화 성공
        assertNotNull(encoded);

        // 디코딩 테스트
        Field<Short> uint8Field = createLayout(BasicTypes.UINT8);
        Field<Integer> uint16Field = createLayout(BasicTypes.UINT16_BE);
        Field<Float> float32Field = createLayout(BasicTypes.FLOAT32_LE);

        // UINT8 필드
        short uint8Decoded = uint8Field.decode(encoded, 0);
        assertEquals(instance.uint8Field, uint8Decoded, "UINT8 decoding failed.");

        // UINT16 필드
        int uint16Decoded = uint16Field.decode(encoded, 1);
        assertEquals(instance.uint16Field, uint16Decoded, "UINT16 decoding failed.");

        // FLOAT32 필드
        float float32Decoded = float32Field.decode(encoded, 3);
        assertEquals(instance.float32Field, float32Decoded, 0.0001f, "FLOAT32 decoding failed.");
    }

    @SuppressWarnings("unchecked")
    private <T> Field<T> createLayout(Class<? extends Field<?>> basicType) {
        try {
            return (Field<T>) basicType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create layout for Field: " + basicType, e);
        }
    }

    // **호환 가능한 Struct 정의**
    public static class CompatibleStruct {
        @StructField(order = 1, type = UInt8Field.class)
        public int uint8AsInt; // UINT8 필드지만 int로 선언

        @StructField(order = 2, type = UInt16BEField.class)
        public long uint16AsLong; // UINT16 필드지만 long으로 선언

        @StructField(order = 3, type = Float32LEField.class)
        public double float32AsDouble; // FLOAT32 필드지만 double로 선언
    }

    // **호환되지 않는 Struct 정의**
    public static class IncompatibleStruct {
        @StructField(order = 1, type = UInt8Field.class)
        public long uint8AsLong; // UINT8의 범위를 초과한 long 값

        @StructField(order = 2, type = UInt16BEField.class)
        public short uint16AsShort; // UINT16이 아닌 short로 선언

        @StructField(order = 3, type = Float32LEField.class)
        public double float32AsShort; // FLOAT32가 아닌 double로 선언
    }

    // **복잡한 Struct 정의**
    public static class ComplexStruct {
        @StructField(order = 1, type = UInt8Field.class)
        public short uint8Field;

        @StructField(order = 2, type = UInt16BEField.class)
        public int uint16Field;

        @StructField(order = 3, type = Float32LEField.class)
        public float float32Field;

        @StructSequenceField(order = 4, lengthType = UInt8Field.class, elementType = UInt32LEField.class)
        public List<Integer> sequenceField;

        @StructField(order = 5, type = CustomStringField.class)
        public String customField;
    }

    // **테스트용 Custom Layout 클래스**
    public static class CustomStringField extends Field<String> {
        public CustomStringField() {
            super(10);
        }

        @Override
        public byte[] encode(String value) {
            return value.getBytes(); // 간단히 UTF-8 문자열 인코딩 사용
        }

        @Override
        public String decode(byte[] bytes, int offset) {
            return new String(bytes, offset, bytes.length - offset); // 문자열 복원
        }
    }
}
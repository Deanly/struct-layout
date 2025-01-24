package net.deanly.structlayout.codec.decode;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.exception.TypeConversionException;
import net.deanly.structlayout.type.basic.Float32LEField;
import net.deanly.structlayout.type.basic.Int8Field;
import net.deanly.structlayout.type.basic.UInt16BEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;

public class BasicStructFieldTypeCompatibilityDecodingTest {

    // **1. 단순 구조체 디코딩**
    @Test
    public void testSimpleStructDecoding() {
        // Arrange: 샘플 데이터 준비
        byte[] data = new byte[]{
                (byte) 0xFF,  // uint8Field = 255
                (byte) 0x00,  // int8Field = 0
                (byte) 0x7F,  // int8NegativeField = 127
                (byte) 0x3F   // uint8SmallField = 63
        };
        int startOffset = 0;

        // Act: 데이터를 디코딩
        SimpleStruct decoded = StructDecoder.decode(SimpleStruct.class, data, startOffset).getValue();

        // Assert: 각 필드별 올바른 디코딩 검증
        assertEquals(255, decoded.uint8Field);       // UINT8: 255
        assertEquals(0, decoded.int8Field);         // INT8: 0
        assertEquals(127, decoded.int8NegativeField); // INT8: 127
        assertEquals(63, decoded.uint8SmallField);  // UINT8: 63
    }

    // **2. 숫자 타입 간 변환 검증**
    @Test
    public void testNumericTypeCompatibility() {
        // Arrange: 샘플 데이터 준비
        byte[] data = {
                (byte) 0xFF,                 // uint8AsInt = 255
                (byte) 0x01, (byte) 0x00,    // uint16AsLong (Big Endian) = 256
                (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0x3F // float32AsDouble = 127.0 (Little Endian)
        };
        int startOffset = 0;

        // Act
        CompatibleStruct decoded = StructDecoder.decode(CompatibleStruct.class, data, startOffset).getValue();

        // Assert: 각 필드가 의도한 타입으로 변환되었는지 확인
        assertEquals(255, decoded.uint8AsInt); // UINT8 필드를 int로
        assertEquals(256, decoded.uint16AsLong); // UINT16 필드를 long으로
        assertEquals(1.984375, decoded.float32AsDouble, 0.0001); // FLOAT32 필드를 double로
    }

    // **3. 부적합한 타입에 대한 예외 처리 검증**
    @Test
    public void testIncompatibleTypeTypes() {
        // Arrange: 샘플 데이터 준비 (UINT8의 범위를 초과하는 값)
        byte[] data = new byte[]{
                (byte) 0xFF,                     // 첫 필드: uint8AsLong (정상적으로 처리)
                (byte) 0xFF, (byte) 0x7F,       // 두 번째 필드: floatField
                (byte) 0x80, (byte) 0xFF        // FLOAT32_LE (4바이트, 타입 변환 실패 예상)
        };
        int offset = 0;

        // Act & Assert: 타입 변환 실패 예외를 발생시킴
        Exception exception = assertThrows(TypeConversionException.class, () -> {
            StructDecoder.decode(IncompatibleStruct.class, data, offset);
        });

        assertTrue(exception.getMessage().contains("floatField"), "Expected type mismatch exception.");
    }

    // **4. 시퀀스 필드(SequenceField) 디코딩**
    @Test
    public void testSequenceTypeDecoding() {
        // Arrange: 샘플 데이터 (길이 값 3 + 시퀀스 값 10, 20, 30)
        byte[] data = new byte[]{3, 10, 20, 30};
        int offset = 0;

        // Act
        SequenceStruct decoded = StructDecoder.decode(SequenceStruct.class, data, offset).getValue();

        // Assert
        assertNotNull(decoded.sequenceField);
        assertEquals(3, decoded.sequenceField.size());
        assertIterableEquals(Arrays.asList(10, 20, 30), decoded.sequenceField);
    }

    // **5. 중첩된 구조체 처리**
    @Test
    public void testNestedStructDecoding() {
        // Arrange: 중첩된 구조체 디코딩
        byte[] data = new byte[]{10, 20, 30, 40, 50};
        int offset = 0;

        // Act
        NestedStruct decoded = StructDecoder.decode(NestedStruct.class, data, offset).getValue();

        // Assert
        assertNotNull(decoded.child);
        assertEquals(10, decoded.child.uint8Field);
        assertEquals(20, decoded.child.int8Field);
    }

    // 6. 복잡한 구조체 디코딩 테스트
    @Test
    public void testComplexStructDecoding() {
        // Arrange: 복잡한 구조체 디코딩 데이터 준비
        // - 필드별 데이터 크기:
        //   uint8Field (1 byte)
        //   int8Field (1 byte)
        //   SequenceField (1 byte for length + 3 * 1 byte for elements)
        //   customField (4 bytes for CustomStringLayout)
        byte[] data = new byte[]{
                (byte) 0x80,                 // uint8Field (128)
                0x00,                        // int8Field (0)
                0x03, 0x10, 0x20, 0x30,      // SequenceField (length: 3, elements: [16, 32, 48])
                0x54, 0x65, 0x73, 0x74       // customField ("Test" as bytes)
        };

        int offset = 0;

        // Act
        ComplexStruct decoded = StructDecoder.decode(ComplexStruct.class, data, offset).getValue();

        // Assert: 필드별 디코딩 결과 확인
        assertEquals(128, decoded.uint8Field); // UINT8 Field
        assertEquals(0, decoded.int8Field);   // INT8 Field
        assertNotNull(decoded.sequenceField); // SequenceField
        assertIterableEquals(Arrays.asList(16, 32, 48), decoded.sequenceField);
        assertEquals("Test", decoded.customField); // CustomLayoutField
    }

    // **테스트에 사용될 클래스 정의**

    // 1. 단순 구조체
    public static class SimpleStruct {
        @StructField(order = 1, type = UInt8Field.class) public short uint8Field;
        @StructField(order = 2, type = Int8Field.class) public byte int8Field;
        @StructField(order = 3, type = Int8Field.class) public byte int8NegativeField;
        @StructField(order = 4, type = UInt8Field.class) public byte uint8SmallField;
    }

    // 2. 호환 가능한 구조체
    public static class CompatibleStruct {
        @StructField(order = 1, type = UInt8Field.class) public int uint8AsInt;
        @StructField(order = 2, type = UInt16BEField.class) public long uint16AsLong;
        @StructField(order = 3, type = Float32LEField.class) public double float32AsDouble;
    }

    // 3. 부적합한 구조체
    public static class IncompatibleStruct {
        @StructField(order = 1, type = UInt8Field.class) public long uint8AsLong;
        @StructField(order = 2, type = Float32LEField.class) public short floatField;
    }

    // 4. 시퀀스 포함 구조체
    public static class SequenceStruct {
        @StructSequenceField(order = 1, lengthType = UInt8Field.class, elementType = UInt8Field.class)
        public List<Integer> sequenceField;
    }

    // 5. 중첩된 구조체
    public static class NestedStruct {
        @StructObjectField(order = 1) public SimpleStruct child;
    }

    // 6. 복잡한 구조체
    public static class ComplexStruct {
        @StructField(order = 1, type = UInt8Field.class) public short uint8Field;
        @StructField(order = 2, type = Int8Field.class) public int int8Field;
        @StructSequenceField(order = 3, lengthType = UInt8Field.class, elementType = UInt8Field.class)
        public List<Integer> sequenceField;
        @StructField(order = 4, type = CustomStringField.class) public String customField;
    }

    // 테스트용 CustomLayoutField
    public static class CustomStringField extends FieldBase<String> {
        public CustomStringField() { super(4); }
        @Override
        public byte[] encode(String value) { return value.getBytes(); }
        @Override
        public String decode(byte[] bytes, int offset) {
            return new String(bytes, offset, 4); // 고정 길이 디코딩
        }
    }
}


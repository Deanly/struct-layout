package net.deanly.structlayout.codec.encode;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.analysis.CachedLayoutProvider;
import net.deanly.structlayout.annotation.*;
import net.deanly.structlayout.exception.*;
import net.deanly.structlayout.type.basic.Int16BEField;
import net.deanly.structlayout.type.basic.Int16LEField;
import net.deanly.structlayout.type.basic.Int32BEField;
import net.deanly.structlayout.type.basic.Int8Field;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StructEncoderTest {

    public static class SampleObject {
        @StructField(order = 1, type = Int32BEField.class)
        public int field1 = 123;

        @StructSequenceField(order = 2, lengthType = Int8Field.class, elementType = Int16LEField.class)
        public List<Short> listField = List.of((short) 1, (short) 2, (short) 3);

        @StructObjectField(order = 3)
        public NestedObject nestedObject = new NestedObject();

        @StructField(order = 4, type = MyCustomField.class)
        public long customField = 987654321L;
    }

    public static class NestedObject {
        @StructField(order = 1, type = Int16BEField.class)
        public short field = 42;
    }

    public static class MyCustomField extends FieldBase<Long> {
        public MyCustomField() {
            super(8);
        }

        @Override
        public byte[] encode(Long value) {
            if (value == null) {
                throw new IllegalArgumentException("Value cannot be null for encoding");
            }

            return new byte[] {
                    (byte) ((value >> 56) & 0xFF),
                    (byte) ((value >> 48) & 0xFF),
                    (byte) ((value >> 40) & 0xFF),
                    (byte) ((value >> 32) & 0xFF),
                    (byte) ((value >> 24) & 0xFF),
                    (byte) ((value >> 16) & 0xFF),
                    (byte) ((value >> 8) & 0xFF),
                    (byte) (value & 0xFF)
            };
        }

        @Override
        public Long decode(byte[] data, int offset) {
            if (data == null || data.length - offset < 8) {
                throw new IllegalArgumentException("Data array is invalid or too short for decoding");
            }

            return ((long) (data[offset] & 0xFF) << 56) |
                    ((long) (data[offset + 1] & 0xFF) << 48) |
                    ((long) (data[offset + 2] & 0xFF) << 40) |
                    ((long) (data[offset + 3] & 0xFF) << 32) |
                    ((long) (data[offset + 4] & 0xFF) << 24) |
                    ((long) (data[offset + 5] & 0xFF) << 16) |
                    ((long) (data[offset + 6] & 0xFF) << 8)  |
                    ((long) (data[offset + 7] & 0xFF));
        }
    }

    @Test
    void testEncodeNormalObject() {
        SampleObject sample = new SampleObject();

        byte[] encoded = StructEncoder.encode(sample);

        assertNotNull(encoded, "Encoded byte array should not be null");
        assertTrue(encoded.length > 0, "Encoded byte array should have content");
    }

    @Test
    void testNullObject() {
        byte[] encoded = StructEncoder.encode(null);

        assertNotNull(encoded, "Encoded byte array for null object should not be null");
        assertEquals(0, encoded.length, "Encoded byte array for null object should be empty");
    }

    @Test
    void testInvalidSequenceField() {
        class InvalidSequenceObject {
            @StructSequenceField(order = 1, lengthType = Int8Field.class, elementType = Int16LEField.class)
            public String invalidSequence = "Invalid"; // 수정된 Field 사용
        }

        InvalidSequenceObject obj = new InvalidSequenceObject();
        assertThrows(InvalidSequenceTypeException.class, () -> StructEncoder.encode(obj),
                "Invalid sequence type should throw InvalidSequenceTypeException");
    }

    public static class MyBrokenCustomField extends FieldBase<Integer> {
        public MyBrokenCustomField() {
            super(4);
        }

        @Override
        public byte[] encode(Integer value) {
            throw new UnsupportedOperationException("Custom layout is broken");
        }

        @Override
        public Integer decode(byte[] data, int offset) {
            return null;
        }
    }

    public static class InvalidCustomLayout {
        @StructField(order = 1, type = MyBrokenCustomField.class)
        public int invalidField = 100;
    }

    @Test
    void testInvalidCustomLayoutField() {
        InvalidCustomLayout obj = new InvalidCustomLayout();
        assertThrows(StructParsingException.class, () -> StructEncoder.encode(obj),
                "Broken custom layout should throw StructParsingException");
    }

    @Test
    void testEmptyListArrayEncoding() {
        class EmptyListObject {
            @StructSequenceField(order = 1, lengthType = Int8Field.class, elementType = Int16LEField.class)
            public List<Short> emptyList = new ArrayList<>();
        }

        EmptyListObject obj = new EmptyListObject();
        byte[] encoded = StructEncoder.encode(obj);

        assertNotNull(encoded, "Encoded array should not be null for empty list");
        assertTrue(encoded.length >= 1, "Encoded array for empty list should include at least the length header");
    }

    // No-Argument 생성자가 없는 경우 (NoSuchMethodException 발생)
    static class NoDefaultConstructorField extends FieldBase<Integer> {
        public NoDefaultConstructorField(String arg) { // 파라미터가 있는 생성자만 존재
            super(4);
        }

        @Override
        public byte[] encode(Integer value) {
            return new byte[0];
        }

        @Override
        public Integer decode(byte[] data, int offset) {
            return null;
        }
    }

    @Test
    void testNoDefaultConstructor() {
        assertThrows(LayoutInitializationException.class,
                () -> CachedLayoutProvider.getLayout(NoDefaultConstructorField.class),
                "Should throw exception for a class without a no-arguments constructor");
    }

    public static class PrivateConstructorField extends FieldBase<Integer> {
        private PrivateConstructorField() { // private 생성자
            super(4);
        }

        @Override
        public byte[] encode(Integer value) {
            return new byte[0];
        }

        @Override
        public Integer decode(byte[] data, int offset) {
            return null;
        }
    }

    @Test
    void testPrivateConstructor() {
        Field<Integer> field = CachedLayoutProvider.getLayout(PrivateConstructorField.class);
        assertNotNull(field, "Should successfully instantiate a class with a private constructor");
    }

    // 내부 클래스이지만 static이 아닌 경우
    public class NonStaticInnerField extends FieldBase<Integer> { // static이 아님
        public NonStaticInnerField() {
            super(4);
        }

        @Override
        public byte[] encode(Integer value) {
            return new byte[0];
        }

        @Override
        public Integer decode(byte[] data, int offset) {
            return null;
        }
    }

    @Test
    void testNonStaticInnerClass() {
        LayoutInitializationException exception = assertThrows(LayoutInitializationException.class,
                () -> CachedLayoutProvider.getLayout(NonStaticInnerField.class),
                "Should throw exception for non-static inner class");

        assertTrue(exception.getMessage().contains("non-static inner class"),
                "Exception message should suggest making the inner class static");
    }

    // 생성자 호출 자체에서 예외가 발생하는 경우 (InvocationTargetException 발생)
    public static class ExceptionThrowingConstructorField extends FieldBase<Integer> {
        public ExceptionThrowingConstructorField() {
            super(4);
            throw new IllegalArgumentException("Simulating an error inside constructor");
        }

        @Override
        public byte[] encode(Integer value) {
            throw new UnsupportedOperationException("encode is not supported");
        }

        @Override
        public Integer decode(byte[] data, int offset) {
            return null;
        }
    }

    @Test
    void testExceptionInConstructor() {
        LayoutInitializationException exception = assertThrows(LayoutInitializationException.class,
                () -> CachedLayoutProvider.getLayout(ExceptionThrowingConstructorField.class),
                "Should throw exception for constructor throwing exception");

        assertTrue(exception.getMessage().contains("Exception occurred while initializing Layout"),
                "Exception message should mention an error in the constructor logic");
        assertNotNull(exception.getCause(), "Exception should contain the root cause");
        assertTrue(exception.getCause() instanceof IllegalArgumentException,
                "The root cause should be the specific exception thrown in the constructor");
    }

    // 추상 클래스 (InstantiationException 발생)
    public static abstract class AbstractField extends FieldBase<Integer> {
        public AbstractField() {
            super(4);
        }

        @Override
        public byte[] encode(Integer value) {
            return new byte[0];
        }

        @Override
        public Integer decode(byte[] data, int offset) {
            return null;
        }
    }

    @Test
    void testAbstractClass() {
        LayoutInitializationException exception = assertThrows(LayoutInitializationException.class,
                () -> CachedLayoutProvider.getLayout(AbstractField.class),
                "Should throw exception for abstract class");

        assertTrue(exception.getMessage().contains("Cannot instantiate Layout class"),
                "Exception message should mention instantiation failure for abstract class");
    }

    // 올바른 클래스가 정상적으로 생성되는지 확인
    public static class ValidField extends FieldBase<Integer> {
        public ValidField() {
            super(4);
        }

        @Override
        public byte[] encode(Integer value) {
            return new byte[]{};
        }

        @Override
        public Integer decode(byte[] data, int offset) {
            return null;
        }
    }

    @Test
    void testValidLayoutCreation() {
        Field<Integer> field = CachedLayoutProvider.getLayout(ValidField.class);
        assertNotNull(field, "Should successfully instantiate a valid Layout class");
    }
}
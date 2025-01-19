package net.deanly.structlayout.codec.encode;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.analysis.CachedLayoutProvider;
import net.deanly.structlayout.annotation.*;
import net.deanly.structlayout.exception.*;
import net.deanly.structlayout.type.DataType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StructEncoderTest {

    public static class SampleObject {
        @StructField(order = 1, dataType = DataType.INT32_BE)
        public int field1 = 123;

        @SequenceField(order = 2, lengthType = DataType.INT8, elementType = DataType.INT16_LE)
        public List<Short> listField = List.of((short) 1, (short) 2, (short) 3);

        @StructObjectField(order = 3)
        public NestedObject nestedObject = new NestedObject();

        @CustomLayoutField(order = 4, layout = MyCustomLayout.class)
        public long customField = 987654321L;
    }

    public static class NestedObject {
        @StructField(order = 1, dataType = DataType.INT16_BE)
        public short field = 42;
    }

    public static class MyCustomLayout extends Layout<Long> {
        public MyCustomLayout() {
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
            @SequenceField(order = 1, lengthType = DataType.INT8, elementType = DataType.INT16_LE)
            public String invalidSequence = "Invalid"; // 수정된 DataType 사용
        }

        InvalidSequenceObject obj = new InvalidSequenceObject();
        assertThrows(InvalidSequenceTypeException.class, () -> StructEncoder.encode(obj),
                "Invalid sequence type should throw InvalidSequenceTypeException");
    }

    public static class MyBrokenCustomLayout extends Layout<Integer> {
        public MyBrokenCustomLayout() {
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
        @CustomLayoutField(order = 1, layout = MyBrokenCustomLayout.class)
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
            @SequenceField(order = 1, lengthType = DataType.INT8, elementType = DataType.INT16_LE)
            public List<Short> emptyList = new ArrayList<>();
        }

        EmptyListObject obj = new EmptyListObject();
        byte[] encoded = StructEncoder.encode(obj);

        assertNotNull(encoded, "Encoded array should not be null for empty list");
        assertTrue(encoded.length >= 1, "Encoded array for empty list should include at least the length header");
    }

    // No-Argument 생성자가 없는 경우 (NoSuchMethodException 발생)
    static class NoDefaultConstructorLayout extends Layout<Integer> {
        public NoDefaultConstructorLayout(String arg) { // 파라미터가 있는 생성자만 존재
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
                () -> CachedLayoutProvider.getLayout(NoDefaultConstructorLayout.class),
                "Should throw exception for a class without a no-arguments constructor");
    }

    public static class PrivateConstructorLayout extends Layout<Integer> {
        private PrivateConstructorLayout() { // private 생성자
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
        Layout<Integer> layout = CachedLayoutProvider.getLayout(PrivateConstructorLayout.class);
        assertNotNull(layout, "Should successfully instantiate a class with a private constructor");
    }

    // 내부 클래스이지만 static이 아닌 경우
    public class NonStaticInnerLayout extends Layout<Integer> { // static이 아님
        public NonStaticInnerLayout() {
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
                () -> CachedLayoutProvider.getLayout(NonStaticInnerLayout.class),
                "Should throw exception for non-static inner class");

        assertTrue(exception.getMessage().contains("non-static inner class"),
                "Exception message should suggest making the inner class static");
    }

    // 생성자 호출 자체에서 예외가 발생하는 경우 (InvocationTargetException 발생)
    public static class ExceptionThrowingConstructorLayout extends Layout<Integer> {
        public ExceptionThrowingConstructorLayout() {
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
                () -> CachedLayoutProvider.getLayout(ExceptionThrowingConstructorLayout.class),
                "Should throw exception for constructor throwing exception");

        assertTrue(exception.getMessage().contains("Exception occurred while initializing Layout"),
                "Exception message should mention an error in the constructor logic");
        assertNotNull(exception.getCause(), "Exception should contain the root cause");
        assertTrue(exception.getCause() instanceof IllegalArgumentException,
                "The root cause should be the specific exception thrown in the constructor");
    }

    // 추상 클래스 (InstantiationException 발생)
    public static abstract class AbstractLayout extends Layout<Integer> {
        public AbstractLayout() {
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
                () -> CachedLayoutProvider.getLayout(AbstractLayout.class),
                "Should throw exception for abstract class");

        assertTrue(exception.getMessage().contains("Cannot instantiate Layout class"),
                "Exception message should mention instantiation failure for abstract class");
    }

    // 올바른 클래스가 정상적으로 생성되는지 확인
    public static class ValidLayout extends Layout<Integer> {
        public ValidLayout() {
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
        Layout<Integer> layout = CachedLayoutProvider.getLayout(ValidLayout.class);
        assertNotNull(layout, "Should successfully instantiate a valid Layout class");
    }
}
package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.type.CountableField;
import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.basic.NoneField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class StructSequenceFieldEncoderHandlerTest {

    // 테스트에 사용될 클래스: 리스트 타입
    public static class ListTestStruct {
        @StructSequenceField(order = 1, lengthType = TestLengthField.class, elementType = TestField.class)
        public List<Integer> elements;
    }

    // 테스트에 사용될 클래스: 배열 타입
    public static class ArrayTestStruct {
        @StructSequenceField(order = 1, lengthType = TestLengthField.class, elementType = TestField.class)
        public Integer[] elements;
    }

    // 테스트용 길이 타입 구현 (CountableField)
    public static class TestLengthField extends FieldBase<Integer> implements CountableField<Integer> {
        public TestLengthField() {
            super(1);
        }

        @Override
        public Integer decode(byte[] data, int offset) {
            return (int) data[offset];
        }

        @Override
        public byte[] encode(Integer value) {
            return new byte[]{value.byteValue()};
        }
    }

    // 테스트용 요소 타입 구현 (Field)
    public static class TestField extends FieldBase<Integer> implements Field<Integer> {
        public TestField() {
            super(2);
        }

        @Override
        public Integer decode(byte[] data, int offset) {
            return (data[offset] << 8) | (data[offset + 1] & 0xFF);
        }

        @Override
        public byte[] encode(Integer value) {
            return new byte[]{
                    (byte) ((value >> 8) & 0xFF),
                    (byte) (value & 0xFF)
            };
        }
    }

    @Test
    public void testListEncoding() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // 테스트 데이터
        ListTestStruct instance = new ListTestStruct();
        instance.elements = Arrays.asList(256, 512, 768); // 0x0100, 0x0200, 0x0300

        // 인코딩 수행
        byte[] encoded = handler.handleField(instance, ListTestStruct.class.getField("elements"));

        // 검증: 길이(요소 개수 3) + 각 요소(2바이트씩 3개)
        byte[] expected = new byte[]{
                0x03,       // 길이 (3)
                0x01, 0x00, // 첫 번째 요소 256
                0x02, 0x00, // 두 번째 요소 512
                0x03, 0x00  // 세 번째 요소 768
        };
        Assertions.assertArrayEquals(expected, encoded);
    }

    @Test
    public void testArrayEncoding() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // 테스트 데이터
        ArrayTestStruct instance = new ArrayTestStruct();
        instance.elements = new Integer[]{256, 512, 768}; // 0x0100, 0x0200, 0x0300

        // 인코딩 수행
        byte[] encoded = handler.handleField(instance, ArrayTestStruct.class.getField("elements"));

        // 검증: 길이(요소 개수 3) + 각 요소(2바이트씩 3개)
        byte[] expected = new byte[]{
                0x03,       // 길이 (3)
                0x01, 0x00, // 첫 번째 요소 256
                0x02, 0x00, // 두 번째 요소 512
                0x03, 0x00  // 세 번째 요소 768
        };
        Assertions.assertArrayEquals(expected, encoded);
    }

    @Test
    public void testVoidFieldArrayEncoding() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // 테스트 데이터
        ArrayTestStruct instance = new ArrayTestStruct();
        instance.elements = new Integer[]{256, 512, 768}; // 0x0100, 0x0200, 0x0300

        // @StructSequenceField에 VoidField 사용
        class UnsafeArrayTest {
            @StructSequenceField(order = 1, lengthType = NoneField.class, elementType = TestField.class)
            public Integer[] elements;
        }

        UnsafeArrayTest unsafeInstance = new UnsafeArrayTest();
        unsafeInstance.elements = instance.elements;

        // 길이 정보 포함하지 않고 인코딩
        byte[] encoded = handler.handleField(unsafeInstance, UnsafeArrayTest.class.getField("elements"));

        // 검증: 요소만 인코딩 (길이 제외)
        byte[] expected = new byte[]{
                0x01, 0x00, // 첫 번째 요소 256
                0x02, 0x00, // 두 번째 요소 512
                0x03, 0x00  // 세 번째 요소 768
        };
        Assertions.assertArrayEquals(expected, encoded);
    }

    @Test
    public void testEmptyArrayEncoding() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // 빈 배열 테스트
        ArrayTestStruct instance = new ArrayTestStruct();
        instance.elements = new Integer[]{};

        byte[] encoded = handler.handleField(instance, ArrayTestStruct.class.getField("elements"));

        // 검증: 길이는 0 (1바이트)만 인코딩
        byte[] expected = new byte[]{
                0x00 // 길이(0)
        };
        Assertions.assertArrayEquals(expected, encoded);
    }

    @Test
    public void testNullArrayEncoding() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // null 배열 테스트
        ArrayTestStruct instance = new ArrayTestStruct();
        instance.elements = null;

        byte[] encoded = handler.handleField(instance, ArrayTestStruct.class.getField("elements"));

        // 검증: 길이는 0 (1바이트)만 인코딩
        byte[] expected = new byte[]{
                0x00 // 길이(0)
        };
        Assertions.assertArrayEquals(expected, encoded);
    }


    // VoidField를 사용한 테스트용 리스트 타입 클래스
    public static class VoidFieldListTestStruct {
        @StructSequenceField(order = 1, lengthType = NoneField.class, elementType = CustomField.class)
        public List<Integer> elements;
    }

    // VoidField를 사용한 테스트용 배열 타입 클래스
    public static class VoidFieldArrayTestStruct {
        @StructSequenceField(order = 1, lengthType = NoneField.class, elementType = CustomField.class)
        public Integer[] elements;
    }

    // 테스트용 커스텀 요소 타입 클래스
    public static class CustomField extends FieldBase<Integer> implements Field<Integer> {
        public CustomField() {
            super(2);
        }

        @Override
        public Integer decode(byte[] data, int offset) {
            return (data[offset] << 8) | (data[offset + 1] & 0xFF);
        }

        @Override
        public byte[] encode(Integer value) {
            return new byte[]{
                    (byte) ((value >> 8) & 0xFF),
                    (byte) (value & 0xFF)
            };
        }
    }

    @Test
    public void testVoidFieldListEncodingData() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // VoidField를 lengthType으로 사용한 리스트
        VoidFieldListTestStruct instance = new VoidFieldListTestStruct();
        instance.elements = Arrays.asList(256, 512, 768); // 0x0100, 0x0200, 0x0300

        // 인코딩 수행
        byte[] encoded = handler.handleField(instance, VoidFieldListTestStruct.class.getField("elements"));

        // 검증: 길이 포함되지 않고 요소 데이터만 인코딩
        byte[] expected = new byte[]{
                0x01, 0x00, // 첫 번째 요소 256
                0x02, 0x00, // 두 번째 요소 512
                0x03, 0x00  // 세 번째 요소 768
        };
        Assertions.assertArrayEquals(expected, encoded);
    }

    @Test
    public void testVoidFieldArrayEncodingData() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // VoidField를 lengthType으로 사용한 배열
        VoidFieldArrayTestStruct instance = new VoidFieldArrayTestStruct();
        instance.elements = new Integer[]{256, 512, 768}; // 0x0100, 0x0200, 0x0300

        // 인코딩 수행
        byte[] encoded = handler.handleField(instance, VoidFieldArrayTestStruct.class.getField("elements"));

        // 검증: 길이 포함되지 않고 요소 데이터만 인코딩
        byte[] expected = new byte[]{
                0x01, 0x00, // 첫 번째 요소 256
                0x02, 0x00, // 두 번째 요소 512
                0x03, 0x00  // 세 번째 요소 768
        };
        Assertions.assertArrayEquals(expected, encoded);
    }

    @Test
    public void testVoidFieldArrayEncodingEmpty() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // VoidField를 lengthType으로 사용한 빈 배열
        VoidFieldArrayTestStruct instance = new VoidFieldArrayTestStruct();
        instance.elements = new Integer[]{}; // 빈 배열

        // 인코딩 수행
        byte[] encoded = handler.handleField(instance, VoidFieldArrayTestStruct.class.getField("elements"));

        // 검증: 데이터 없음
        byte[] expected = new byte[]{}; // 빈 배열 -> 빈 바이트 배열
        Assertions.assertArrayEquals(expected, encoded);
    }

    @Test
    public void testVoidFieldArrayEncodingNull() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // VoidField를 lengthType으로 사용한 null 배열
        VoidFieldArrayTestStruct instance = new VoidFieldArrayTestStruct();
        instance.elements = null; // null

        // 인코딩 수행
        byte[] encoded = handler.handleField(instance, VoidFieldArrayTestStruct.class.getField("elements"));

        // 검증: 데이터 없음
        byte[] expected = new byte[]{}; // null -> 빈 바이트 배열
        Assertions.assertArrayEquals(expected, encoded);
    }

}
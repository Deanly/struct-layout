package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.type.basic.NoneField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class StructSequenceFieldDecoderHandlerTest {

    // 테스트에 사용할 간단한 데이터 클래스
    public static class TestStruct {
        @StructSequenceField(order = 1, lengthType = NoneField.class, elementType = TestField.class)
        public List<Integer> elements;
    }

    // 요소를 표현하는 Field 클래스 (테스트용)
    public static class TestField implements Field<Integer> {
        private int span;

        public TestField() {
            this.span = 2;  // 각 요소는 테스트에서 2바이트로 정의 (예시)
        }

        @Override
        public Integer decode(byte[] data, int offset) {
            // 2바이트 읽어서 정수로 변환
            return (data[offset] << 8) | (data[offset + 1] & 0xFF);
        }

        @Override
        public int getSpan() {
            return this.span;
        }

        @Override
        public byte[] encode(Integer value) {
            return new byte[0];
        }
    }

    /**
     * 성공적인 VoidField 테스트
     */
    @Test
    public void testVoidFieldDecodingSuccess() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // 테스트 데이터 (4개의 정수, 각 2바이트씩)
        byte[] testData = new byte[]{
                0x00, 0x01,  // 1
                0x00, 0x02,  // 2
                0x00, 0x03,  // 3
                0x00, 0x04   // 4
        };

        TestStruct instance = new TestStruct();
        java.lang.reflect.Field field = TestStruct.class.getField("elements");

        int finalOffset = handler.handleField(instance, field, testData, 0);

        // 결과 검증
        Assertions.assertNotNull(instance.elements);
        Assertions.assertEquals(4, instance.elements.size());
        Assertions.assertEquals(List.of(1, 2, 3, 4), instance.elements);

        // 오프셋 체크
        Assertions.assertEquals(testData.length, finalOffset);
    }

    /**
     * 데이터가 잘못된 경우 (불완전한 데이터)
     */
    @Test
    public void testVoidFieldDecodingWithIncompleteData() throws NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // 테스트 데이터 (불완전한 마지막 요소 포함)
        byte[] testData = new byte[]{
                0x00, 0x01,  // 1
                0x00, 0x02,  // 2
                0x00         // 불완전 데이터 (3의 일부)
        };

        TestStruct instance = new TestStruct();
        java.lang.reflect.Field field;
        field = TestStruct.class.getField("elements");

        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            handler.handleField(instance, field, testData, 0);
        });
    }

    /**
     * 데이터를 모두 읽는 경우와 VoidField 동작 체크
     */
    @Test
    public void testVoidFieldEmptyData() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // 빈 데이터
        byte[] testData = new byte[]{};

        TestStruct instance = new TestStruct();
        java.lang.reflect.Field field = TestStruct.class.getField("elements");

        int finalOffset = handler.handleField(instance, field, testData, 0);

        // 결과 검증
        Assertions.assertNotNull(instance.elements);
        Assertions.assertEquals(0, instance.elements.size()); // 빈 데이터니까 아무것도 없어야 함
        Assertions.assertEquals(0, finalOffset);
    }


    // 테스트에 사용할 간단한 데이터 클래스
    public static class TestStruct2 {
        @StructSequenceField(order = 1, lengthType = NoneField.class, elementType = TestField2.class)
        public List<Integer> elements;
    }

    // 배열을 사용하는 테스트 클래스
    public static class ArrayTestStruct {
        @StructSequenceField(order = 1, lengthType = NoneField.class, elementType = TestField2.class)
        public Integer[] elements;
    }

    // 요소를 표현하는 Field 클래스 (테스트용)
    public static class TestField2 implements Field<Integer> {
        private int span;

        public TestField2() {
            this.span = 2;  // 각 요소는 테스트에서 2바이트로 정의 (예시)
        }

        @Override
        public Integer decode(byte[] data, int offset) {
            // 2바이트 읽어서 정수로 변환
            return (data[offset] << 8) | (data[offset + 1] & 0xFF);
        }

        @Override
        public int getSpan() {
            return this.span;
        }

        @Override
        public byte[] encode(Integer value) {
            return new byte[0];
        }
    }

    /**
     * 성공적인 VoidField 테스트 (List)
     */
    @Test
    public void testVoidFieldDecodingForListSuccess() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // 테스트 데이터 (4개의 정수, 각 2바이트씩)
        byte[] testData = new byte[]{
                0x00, 0x01,  // 1
                0x00, 0x02,  // 2
                0x00, 0x03,  // 3
                0x00, 0x04   // 4
        };

        TestStruct instance = new TestStruct();
        java.lang.reflect.Field field = TestStruct.class.getField("elements");

        int finalOffset = handler.handleField(instance, field, testData, 0);

        // 결과 검증
        Assertions.assertNotNull(instance.elements);
        Assertions.assertEquals(4, instance.elements.size());
        Assertions.assertEquals(List.of(1, 2, 3, 4), instance.elements);

        // 오프셋 체크
        Assertions.assertEquals(testData.length, finalOffset);
    }

    /**
     * 성공적인 VoidField 테스트 (배열)
     */
    @Test
    public void testVoidFieldDecodingForArraySuccess() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // 테스트 데이터 (4개의 정수, 각 2바이트씩)
        byte[] testData = new byte[]{
                0x00, 0x01,  // 1
                0x00, 0x02,  // 2
                0x00, 0x03,  // 3
                0x00, 0x04   // 4
        };

        ArrayTestStruct instance = new ArrayTestStruct();
        java.lang.reflect.Field field = ArrayTestStruct.class.getField("elements");

        int finalOffset = handler.handleField(instance, field, testData, 0);

        // 결과 검증
        Assertions.assertNotNull(instance.elements);
        Assertions.assertEquals(4, instance.elements.length);
        Assertions.assertArrayEquals(new Integer[]{1, 2, 3, 4}, instance.elements);

        // 오프셋 체크
        Assertions.assertEquals(testData.length, finalOffset);
    }

    /**
     * 잘못된 데이터로 배열 테스트
     */
    @Test
    public void testVoidFieldDecodingIncompleteDataForArray() throws NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // 테스트 데이터 (불완전한 마지막 요소 포함)
        byte[] testData = new byte[]{
                0x00, 0x01,  // 1
                0x00, 0x02,  // 2
                0x00         // 불완전 데이터 (3의 일부)
        };

        ArrayTestStruct instance = new ArrayTestStruct();
        java.lang.reflect.Field field = ArrayTestStruct.class.getField("elements");

        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            handler.handleField(instance, field, testData, 0);
        });
    }

    /**
     * 빈 데이터로 배열 테스트
     */
    @Test
    public void testVoidFieldEmptyDataForArray() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // 빈 데이터
        byte[] testData = new byte[]{};

        ArrayTestStruct instance = new ArrayTestStruct();
        java.lang.reflect.Field field = ArrayTestStruct.class.getField("elements");

        int finalOffset = handler.handleField(instance, field, testData, 0);

        // 결과 검증
        Assertions.assertNotNull(instance.elements);
        Assertions.assertEquals(0, instance.elements.length); // 빈 배열
        Assertions.assertEquals(0, finalOffset);
    }

    @Test
    public void testUnsafeModeArrayConversion() throws IllegalAccessException, NoSuchFieldException {
        StructSequenceFieldHandler handler = new StructSequenceFieldHandler();

        // 테스트 데이터 (4개의 정수, 각 2바이트 씩)
        byte[] testData = new byte[]{
                0x00, 0x01,  // 1
                0x00, 0x02,  // 2
                0x00, 0x03,  // 3
                0x00, 0x04   // 4
        };

        class TestStruct {
            @StructSequenceField(order = 1, lengthType = NoneField.class, elementType = TestField.class)
            public Integer[] elements;
        }

        // 인스턴스 및 필드 설정
        TestStruct instance = new TestStruct();
        java.lang.reflect.Field field = TestStruct.class.getField("elements");

        // 디코딩 실행
        int finalOffset = handler.handleField(instance, field, testData, 0);

        // 결과 검증
        Assertions.assertNotNull(instance.elements);
        Assertions.assertEquals(4, instance.elements.length);
        Assertions.assertArrayEquals(new Integer[]{1, 2, 3, 4}, instance.elements);

        // 오프셋 체크
        Assertions.assertEquals(testData.length, finalOffset);
    }
}
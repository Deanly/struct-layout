package net.deanly.structlayout.codec.helpers;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.annotation.CustomLayoutField;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.type.DataType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldHelperTest {

    static class TestStruct {
        @StructField(order = 2, dataType = DataType.INT16_BE)
        private int field1;

        @SequenceField(order = 1, elementType = DataType.INT8, lengthType = DataType.INT32_LE)
        private List<Integer> field2;

        @StructObjectField(order = 4)
        private TestObject field3;

        @CustomLayoutField(order = 3, layout = CustomLayout.class)
        private String field4;
    }

    static class TestObject {
        @StructField(order = 1, dataType = DataType.INT16_LE)
        private int nestedField;
    }

    static class CustomLayout extends Layout<String> {
        public CustomLayout(int span) {
            super(1);
        }

        @Override
        public byte[] encode(String value) {
            return new byte[1];
        }

        @Override
        public String decode(byte[] bytes, int offset) {
            return "H";
        }
    }

    @Test
    void testGetOrderedFields() throws NoSuchFieldException {
        Field[] fields = TestStruct.class.getDeclaredFields();
        List<Field> orderedFields = FieldHelper.getOrderedFields(fields);

        // 정렬된 필드 순서 확인
        assertEquals(4, orderedFields.size());
        assertEquals("field2", orderedFields.get(0).getName()); // SequenceField: order 1
        assertEquals("field1", orderedFields.get(1).getName()); // StructField: order 2
        assertEquals("field4", orderedFields.get(2).getName()); // CustomLayoutField: order 3
        assertEquals("field3", orderedFields.get(3).getName()); // StructObjectField: order 4
    }

    @Test
    void testGetOrderValue() throws NoSuchFieldException {
        Field field1 = TestStruct.class.getDeclaredField("field1");
        Field field2 = TestStruct.class.getDeclaredField("field2");
        Field field3 = TestStruct.class.getDeclaredField("field3");
        Field field4 = TestStruct.class.getDeclaredField("field4");

        assertEquals(2, FieldHelper.getOrderValue(field1)); // StructField order
        assertEquals(1, FieldHelper.getOrderValue(field2)); // SequenceField order
        assertEquals(4, FieldHelper.getOrderValue(field3)); // StructObjectField order
        assertEquals(3, FieldHelper.getOrderValue(field4)); // CustomLayoutField order
    }

    @Test
    void testGetOrderValueNoAnnotation() {
        class InvalidClass {
            private int invalidField; // 어노테이션 없음
        }

        Field invalidField;
        try {
            invalidField = InvalidClass.class.getDeclaredField("invalidField");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e); // 테스트에서만 발생 가능
        }

        // 어노테이션 없는 경우 예외가 발생해야 함
        assertThrows(RuntimeException.class, () -> FieldHelper.getOrderValue(invalidField));
    }
}
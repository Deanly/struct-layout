package net.deanly.structlayout.codec;

import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.codec.decode.StructDecoder;
import net.deanly.structlayout.codec.encode.StructEncoder;
import net.deanly.structlayout.type.basic.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StructInheritanceTest {

    // 부모 클래스 정의
    public static class ParentStruct {
        @StructField(order = 0, type = Int32LEField.class)
        private int parentField1;

        @StructField(order = 1, type = Int16LEField.class)
        private short parentField2;

        public ParentStruct() {
        }

        public ParentStruct(int parentField1, short parentField2) {
            this.parentField1 = parentField1;
            this.parentField2 = parentField2;
        }

        public int getParentField1() {
            return parentField1;
        }

        public short getParentField2() {
            return parentField2;
        }
    }

    // 자식 클래스 정의
    public static class ChildStruct extends ParentStruct {
        @StructField(order = 2, type = ByteField.class)
        private byte childField1;

        @StructSequenceField(order = 3, lengthType = UInt8Field.class, elementType = Int32BEField.class)
        private int[] childField2;

        @StructObjectField(order = 4)
        private NestedObject nestedObject;

        public ChildStruct() {
        }

        public ChildStruct(int parentField1, short parentField2, byte childField1, int[] childField2, NestedObject nestedObject) {
            super(parentField1, parentField2);
            this.childField1 = childField1;
            this.childField2 = childField2;
            this.nestedObject = nestedObject;
        }

        public byte getChildField1() {
            return childField1;
        }

        public int[] getChildField2() {
            return childField2;
        }

        public NestedObject getNestedObject() {
            return nestedObject;
        }
    }

    // 중첩된 객체 정의
    public static class NestedObject {
        @StructField(order = 0, type = Int32BEField.class)
        private int nestedField1;

        @StructField(order = 1, type = Int16BEField.class)
        private short nestedField2;

        public NestedObject() {
        }

        public NestedObject(int nestedField1, short nestedField2) {
            this.nestedField1 = nestedField1;
            this.nestedField2 = nestedField2;
        }

        public int getNestedField1() {
            return nestedField1;
        }

        public short getNestedField2() {
            return nestedField2;
        }
    }

    @Test
    public void testEncodeDecodeInheritanceStruct() {
        // 1. 테스트 데이터를 설정
        int parentField1 = 42;
        short parentField2 = 320;
        byte childField1 = 7;
        int[] childField2 = new int[]{10, 20, 30};
        NestedObject nestedObject = new NestedObject(130, (short) 25);

        ChildStruct original = new ChildStruct(parentField1, parentField2, childField1, childField2, nestedObject);

        // 2. Encode 처리
        byte[] encodedData = StructEncoder.encode(original);

        // 3. Decode 처리
        ChildStruct decoded = StructDecoder.decode(ChildStruct.class, encodedData, 0).getValue();

        // 4. 부모 필드 검증
        assertEquals(parentField1, decoded.getParentField1(), "ParentField1 did not match");
        assertEquals(parentField2, decoded.getParentField2(), "ParentField2 did not match");

        // 5. 자식 필드 검증
        assertEquals(childField1, decoded.getChildField1(), "ChildField1 did not match");

        for (int i = 0; i < childField2.length; i++) {
            assertEquals(childField2[i], decoded.getChildField2()[i], "ChildField2[" + i + "] did not match");
        }

        // 6. 중첩 객체 검증
        assertEquals(nestedObject.getNestedField1(), decoded.getNestedObject().getNestedField1(), "NestedField1 did not match");
        assertEquals(nestedObject.getNestedField2(), decoded.getNestedObject().getNestedField2(), "NestedField2 did not match");
    }
}
package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceObjectField;
import net.deanly.structlayout.type.basic.Int32BEField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.structlayout.type.basic.NoneField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StructSequenceObjectFieldDecoderHandlerTest {

    private StructSequenceObjectFieldHandler handler;

    @BeforeEach
    void setUp() {
        handler = new StructSequenceObjectFieldHandler();
    }

    @Test
    void testHandleField_withSequenceList() throws Exception {
        // Arrange
        TestClass instance = new TestClass();
        java.lang.reflect.Field targetField = TestClass.class.getDeclaredField("sequenceList");
        targetField.setAccessible(true);

        // Mock data: length (1 byte as UInt8) + elements (each 4 bytes as Int32BEField)
        byte[] data = new byte[]{
                3,         // Length: 3 elements (UInt8)
                0, 0, 0, 1, // Element 1: id = 1
                0, 0, 0, 2, // Element 2: id = 2
                0, 0, 0, 3  // Element 3: id = 3
        };

        // Act
        int processedBytes = handler.handleField(instance, targetField, data, 0);

        // Assert
        assertEquals(13, processedBytes); // 1 byte (length) + 3 elements * 4 bytes each
        assertNotNull(instance.sequenceList);
        assertEquals(3, instance.sequenceList.size());
        assertEquals(1, instance.sequenceList.get(0).id);
        assertEquals(2, instance.sequenceList.get(1).id);
        assertEquals(3, instance.sequenceList.get(2).id);
    }

    @Test
    void testHandleField_withSequenceWithLength() throws Exception {
        // Arrange
        TestClass instance = new TestClass();
        java.lang.reflect.Field targetField = TestClass.class.getDeclaredField("sequenceWithLength");
        targetField.setAccessible(true);

        // Mock data: length (1 byte as UInt8) + elements (each 4 bytes as Int32BEField)
        byte[] data = new byte[]{
                2,         // Length: 2 elements (UInt8)
                0, 0, 0, 4, // Element 1: id = 4
                0, 0, 0, 5  // Element 2: id = 5
        };

        // Act
        int processedBytes = handler.handleField(instance, targetField, data, 0);

        // Assert
        assertEquals(9, processedBytes); // 1 byte (length) + 2 elements * 4 bytes each
        assertNotNull(instance.sequenceWithLength);
        assertEquals(2, instance.sequenceWithLength.length);
        assertEquals(4, instance.sequenceWithLength[0].id);
        assertEquals(5, instance.sequenceWithLength[1].id);
    }

    @Test
    void testHandleField_withSequenceWithoutLength() throws Exception {
        // Arrange
        TestClass instance = new TestClass();
        java.lang.reflect.Field targetField = TestClass.class.getDeclaredField("sequenceWithoutLength");
        targetField.setAccessible(true);

        // Mock data: no length field, all bytes are part of the sequence
        byte[] data = new byte[]{
                0, 0, 0, 7, // Element 1: id = 7
                0, 0, 0, 8, // Element 2: id = 8
                0, 0, 0, 9  // Element 3: id = 9
        };

        // Act
        int processedBytes = handler.handleField(instance, targetField, data, 0);

        // Assert
        assertEquals(12, processedBytes); // 3 elements * 4 bytes each
        assertNotNull(instance.sequenceWithoutLength);
        assertEquals(3, instance.sequenceWithoutLength.length);
        assertEquals(7, instance.sequenceWithoutLength[0].id);
        assertEquals(8, instance.sequenceWithoutLength[1].id);
        assertEquals(9, instance.sequenceWithoutLength[2].id);
    }

    public static class TestClass {

        public TestClass() {}

        @StructSequenceObjectField(order = 1, lengthType = UInt8Field.class)
        private List<TestSubClass> sequenceList;

        @StructSequenceObjectField(order = 2, lengthType = UInt8Field.class)
        private TestSubClass[] sequenceWithLength;

        @StructSequenceObjectField(order = 3, lengthType = NoneField.class)
        private TestSubClass[] sequenceWithoutLength;
    }

    public static class TestSubClass {

        public TestSubClass() {}

        @StructField(order = 1, type = Int32BEField.class)
        private int id;
    }
}
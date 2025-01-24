package net.deanly.structlayout.codec.decode;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.annotation.*;
import net.deanly.structlayout.exception.InvalidDataOffsetException;
import net.deanly.structlayout.exception.StructParsingException;
import net.deanly.structlayout.type.basic.Int16BEField;
import net.deanly.structlayout.type.basic.Int16LEField;
import net.deanly.structlayout.type.basic.Int32BEField;
import net.deanly.structlayout.type.basic.Int8Field;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StructDecoderTest {

    public static class SampleObject {
        @StructField(order = 1, type = Int32BEField.class)
        public int field1;

        @StructSequenceField(order = 2, lengthType = Int8Field.class, elementType = Int16LEField.class)
        public List<Short> listField;

        @StructObjectField(order = 3)
        public NestedObject nestedObject;

        @StructField(order = 4, type = MyCustomField.class)
        public long customField;

        // 기본 생성자 필수
        public SampleObject() {}
    }

    public static class NestedObject {
        @StructField(order = 1, type = Int16BEField.class)
        public short field;

        public NestedObject() {}
    }

    public static class MyCustomField extends FieldBase<Long> {
        public MyCustomField() {
            super(8);
        }

        @Override
        public byte[] encode(Long value) {
            return null;
        }

        @Override
        public Long decode(byte[] data, int offset) {
            return  ((long) (data[offset + 7] & 0xFF)) |
                    ((long) (data[offset + 6] & 0xFF) << 8) |
                    ((long) (data[offset + 5] & 0xFF) << 16) |
                    ((long) (data[offset + 4] & 0xFF) << 24) |
                    ((long) (data[offset + 3] & 0xFF) << 32) |
                    ((long) (data[offset + 2] & 0xFF) << 40) |
                    ((long) (data[offset + 1] & 0xFF) << 48) |
                    ((long) (data[offset] & 0xFF) << 56);
        }
    }

    @Test
    void testDecodeNormalObject() {
        // Given: Encoded byte array
        byte[] encodedData = new byte[] {
                // field1 (INT32_BE) = 123
                0x00, 0x00, 0x00, 0x7B,
                // listField length (INT8) = 3
                0x03,
                // listField elements (INT16_LE) = [1, 2, 3]
                0x01, 0x00, 0x02, 0x00, 0x03, 0x00,
                // nestedObject.field (INT16_BE) = 42
                0x00, 0x2A,
                // customField (MyCustomLayout, INT64_BE) = 252839506297L
                0x00, 0x00, 0x00, 0x3A, (byte) 0xDE, 0x68, (byte)0xB1, 0x79
        };

        // When: Decode the byte array into a SampleObject
        SampleObject decoded = StructDecoder.decode(SampleObject.class, encodedData, 0).getValue();

        // Then: Assert the fields are correctly decoded
        assertNotNull(decoded, "Decoded object should not be null");
        assertEquals(123, decoded.field1, "Field1 should be decoded correctly");
        assertEquals(List.of((short) 1, (short) 2, (short) 3), decoded.listField, "ListField should be decoded correctly");
        assertNotNull(decoded.nestedObject, "NestedObject should not be null");
        assertEquals(42, decoded.nestedObject.field, "NestedObject.field should be decoded correctly");
        assertEquals(252839506297L, decoded.customField, "CustomField should be decoded correctly");
    }

    @Test
    void testDecodeWithInvalidOffset() {
        // Given: Encoded byte array
        byte[] encodedData = new byte[] { 0x00 };

        // When & Then: Attempt to decode with an invalid start offset
        assertThrows(InvalidDataOffsetException.class,
                () -> StructDecoder.decode(SampleObject.class, encodedData, 10),
                "Decoding with an invalid offset should throw InvalidDataOffsetException");
    }

    @Test
    void testDecodeWithPartialData() {
        // Given: Incomplete byte array
        byte[] partialData = new byte[] {
                // field1 (INT32_BE): First 3 bytes only
                0x00, 0x00, 0x00
        };

        // When & Then: Attempt to decode partial data
        assertThrows(IllegalArgumentException.class,
                () -> StructDecoder.decode(SampleObject.class, partialData, 0),
                "Decoding partial data should throw IllegalArgumentException");
    }

    @Test
    void testDecodeEmptyData() {
        // Given: Empty byte array
        byte[] emptyData = new byte[0];

        // When: Decode the empty byte array
        assertThrows(InvalidDataOffsetException.class,
                () -> StructDecoder.decode(SampleObject.class, emptyData, 0),
                "Decoding an empty byte array should throw InvalidDataOffsetException");
    }

    public static class MyBrokenCustomField extends FieldBase<Integer> {
        public MyBrokenCustomField() {
            super(4);
        }

        @Override
        public byte[] encode(Integer value) {
            return null;
        }

        @Override
        public Integer decode(byte[] data, int offset) {
            throw new UnsupportedOperationException("Custom layout is broken");
        }
    }

    public static class InvalidCustomLayoutObject {
        @StructField(order = 1, type = MyBrokenCustomField.class)
        public int invalidField;

        public InvalidCustomLayoutObject() {}
    }

    @Test
    void testDecodeInvalidCustomLayout() {
        byte[] encodedData = new byte[] { 0x00, 0x00, 0x00, 0x64 }; // Some encoded data

        assertThrows(StructParsingException.class,
                () -> StructDecoder.decode(InvalidCustomLayoutObject.class, encodedData, 0),
                "Decoding with a broken custom layout should throw UnsupportedOperationException");
    }


    // When: Decode the data into a class with an empty list
    public static class EmptyListObject {
        @StructSequenceField(order = 1, lengthType = Int8Field.class, elementType = Int16LEField.class)
        public List<Short> emptyList;

        public EmptyListObject() {}
    }

    @Test
    void testDecodeEmptyListArray() {
        // Given: Encoded byte array with an empty list
        byte[] encodedData = new byte[] {
                // listField length (INT8) = 0
                0x00
        };

        EmptyListObject decoded = StructDecoder.decode(EmptyListObject.class, encodedData, 0).getValue();

        // Then: Assert the list is initialized and empty
        assertNotNull(decoded.emptyList, "Decoded list should not be null");
        assertTrue(decoded.emptyList.isEmpty(), "Decoded list should be empty");
    }

    @Test
    void testDecodeNestedObject() {
        // Given: Encoded byte array for a nested object
        byte[] encodedData = new byte[] {
                // nestedObject.field (INT16_BE) = 1234
                0x04, (byte) 0xD2
        };

        // When: Decode the byte array into a nested object
        NestedObject decoded = StructDecoder.decode(NestedObject.class, encodedData, 0).getValue();

        // Then: Assert the field is correctly decoded
        assertNotNull(decoded, "Nested object should not be null");
        assertEquals(1234, decoded.field, "Field in nested object should be decoded correctly");
    }
}
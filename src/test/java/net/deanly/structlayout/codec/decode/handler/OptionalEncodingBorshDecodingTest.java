package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.annotation.OptionalEncoding;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.annotation.StructSequenceObjectField;
import net.deanly.structlayout.codec.decode.StructDecoder;
import net.deanly.structlayout.type.advanced.NoneField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class OptionalEncodingBorshDecodingTest {

    // ────── Test Structs ──────

    public static class PrimitiveOptionalStruct {
        @StructField(order = 1, type = IntField.class, optional = OptionalEncoding.BORSH)
        public Integer number;
    }

    public static class ObjectOptionalStruct {
        @StructObjectField(order = 1, optional = OptionalEncoding.BORSH)
        public Nested nested;
    }

    public static class SequenceOptionalStruct {
        @StructSequenceField(order = 1, elementType = IntField.class, lengthType = NoneField.class, optional = OptionalEncoding.BORSH)
        public List<Integer> numbers;
    }

    public static class SequenceObjectOptionalStruct {
        @StructSequenceObjectField(order = 1, lengthType = NoneField.class, optional = OptionalEncoding.BORSH)
        public List<Nested> nestedList;
    }

    public static class Nested {
        @StructField(order = 1, type = IntField.class)
        public Integer value;
    }

    // ────── Custom Int Field ──────

    public static class IntField implements Field<Integer> {
        @Override
        public Integer decode(byte[] data, int offset) {
            return (data[offset] << 8) | (data[offset + 1] & 0xFF);
        }

        @Override
        public byte[] encode(Integer value) {
            return new byte[]{(byte) (value >> 8), (byte) (value & 0xFF)};
        }

        @Override
        public int getSpan() {
            return 2;
        }
    }

    // ────── Tests ──────

    @Test
    public void testOptionalPrimitiveWithBorshEncoding_present() {
        byte[] data = new byte[]{0x01, 0x00, 0x64}; // 0x01 (Some), 0x0064 = 100
        PrimitiveOptionalStruct result = StructDecoder.decode(PrimitiveOptionalStruct.class, data, 0).getValue();
        Assertions.assertNotNull(result.number);
        Assertions.assertEquals(100, result.number);
    }

    @Test
    public void testOptionalPrimitiveWithBorshEncoding_none() {
        byte[] data = new byte[]{0x00}; // None
        PrimitiveOptionalStruct result = StructDecoder.decode(PrimitiveOptionalStruct.class, data, 0).getValue();
        Assertions.assertNull(result.number);
    }

    @Test
    public void testOptionalObjectWithBorshEncoding_present() {
        byte[] data = new byte[]{0x01, 0x00, 0x2A}; // Some, value = 42
        ObjectOptionalStruct result = StructDecoder.decode(ObjectOptionalStruct.class, data, 0).getValue();
        Assertions.assertNotNull(result.nested);
        Assertions.assertEquals(42, result.nested.value);
    }

    @Test
    public void testOptionalObjectWithBorshEncoding_none() {
        byte[] data = new byte[]{0x00}; // None
        ObjectOptionalStruct result = StructDecoder.decode(ObjectOptionalStruct.class, data, 0).getValue();
        Assertions.assertNull(result.nested);
    }

    @Test
    public void testOptionalSequenceWithBorshEncoding_present() {
        byte[] data = new byte[]{0x01, 0x00, 0x0A, 0x00, 0x14}; // Some, 10 and 20
        SequenceOptionalStruct result = StructDecoder.decode(SequenceOptionalStruct.class, data, 0).getValue();
        Assertions.assertNotNull(result.numbers);
        Assertions.assertEquals(List.of(10, 20), result.numbers);
    }

    @Test
    public void testOptionalSequenceWithBorshEncoding_none() {
        byte[] data = new byte[]{0x00};
        SequenceOptionalStruct result = StructDecoder.decode(SequenceOptionalStruct.class, data, 0).getValue();
        Assertions.assertNull(result.numbers);
    }

    @Test
    public void testOptionalSequenceObjectWithBorshEncoding_present() {
        byte[] data = new byte[]{0x01, 0x00, 0x05, 0x00, 0x0A}; // Some, two Nested values: 5 and 10
        SequenceObjectOptionalStruct result = StructDecoder.decode(SequenceObjectOptionalStruct.class, data, 0).getValue();
        Assertions.assertNotNull(result.nestedList);
        Assertions.assertEquals(2, result.nestedList.size());
        Assertions.assertEquals(5, result.nestedList.get(0).value);
        Assertions.assertEquals(10, result.nestedList.get(1).value);
    }

    @Test
    public void testOptionalSequenceObjectWithBorshEncoding_none() {
        byte[] data = new byte[]{0x00};
        SequenceObjectOptionalStruct result = StructDecoder.decode(SequenceObjectOptionalStruct.class, data, 0).getValue();
        Assertions.assertNull(result.nestedList);
    }
}
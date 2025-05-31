package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.annotation.OptionalEncoding;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.annotation.StructSequenceObjectField;
import net.deanly.structlayout.codec.encode.StructEncoder;
import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.advanced.NoneField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class OptionalEncodingBorshEncodingTest {

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

        public Nested() {}

        public Nested(int value) {
            this.value = value;
        }
    }

    // ────── Custom Int Field ──────

    public static class IntField extends FieldBase<Integer> implements Field<Integer> {
        public IntField() {
            super(2);
        }

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
        PrimitiveOptionalStruct obj = new PrimitiveOptionalStruct();
        obj.number = 100;
        byte[] encoded = StructEncoder.encode(obj);
        Assertions.assertArrayEquals(new byte[]{0x01, 0x00, 0x64}, encoded);
    }

    @Test
    public void testOptionalPrimitiveWithBorshEncoding_none() {
        PrimitiveOptionalStruct obj = new PrimitiveOptionalStruct();
        obj.number = null;
        byte[] encoded = StructEncoder.encode(obj);
        Assertions.assertArrayEquals(new byte[]{0x00}, encoded);
    }

    @Test
    public void testOptionalObjectWithBorshEncoding_present() {
        ObjectOptionalStruct obj = new ObjectOptionalStruct();
        obj.nested = new Nested(42);
        byte[] encoded = StructEncoder.encode(obj);
        Assertions.assertArrayEquals(new byte[]{0x01, 0x00, 0x2A}, encoded);
    }

    @Test
    public void testOptionalObjectWithBorshEncoding_none() {
        ObjectOptionalStruct obj = new ObjectOptionalStruct();
        obj.nested = null;
        byte[] encoded = StructEncoder.encode(obj);
        Assertions.assertArrayEquals(new byte[]{0x00}, encoded);
    }

    @Test
    public void testOptionalSequenceWithBorshEncoding_present() {
        SequenceOptionalStruct obj = new SequenceOptionalStruct();
        obj.numbers = List.of(10, 20);
        byte[] encoded = StructEncoder.encode(obj);
        Assertions.assertArrayEquals(new byte[]{0x01, 0x00, 0x0A, 0x00, 0x14}, encoded);
    }

    @Test
    public void testOptionalSequenceWithBorshEncoding_none() {
        SequenceOptionalStruct obj = new SequenceOptionalStruct();
        obj.numbers = null;
        byte[] encoded = StructEncoder.encode(obj);
        Assertions.assertArrayEquals(new byte[]{0x00}, encoded);
    }

    @Test
    public void testOptionalSequenceObjectWithBorshEncoding_present() {
        SequenceObjectOptionalStruct obj = new SequenceObjectOptionalStruct();
        obj.nestedList = List.of(new Nested(5), new Nested(10));
        byte[] encoded = StructEncoder.encode(obj);
        Assertions.assertArrayEquals(new byte[]{0x01, 0x00, 0x05, 0x00, 0x0A}, encoded);
    }

    @Test
    public void testOptionalSequenceObjectWithBorshEncoding_none() {
        SequenceObjectOptionalStruct obj = new SequenceObjectOptionalStruct();
        obj.nestedList = null;
        byte[] encoded = StructEncoder.encode(obj);
        Assertions.assertArrayEquals(new byte[]{0x00}, encoded);
    }
}
package net.deanly.structlayout;

import lombok.*;
import net.deanly.structlayout.annotation.*;
import net.deanly.structlayout.dispatcher.StructTypeDispatcher;
import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.basic.*;
import net.deanly.structlayout.type.borsh.BorshBlobField;
import net.deanly.structlayout.type.borsh.BorshStringField;
import net.deanly.structlayout.type.borsh.COptionFieldTest;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BasicStructFieldTest {

    public static class SimpleStruct {
        @StructField(order = 1, type = Int16LEField.class)
        private int intValue;

        @StructField(order = 2, type = Float32LEField.class)
        private float floatValue;

        @StructSequenceField(order = 3, elementType = ByteField.class)
        private byte[] byteArray;

        public int getIntValue() {
            return intValue;
        }

        public void setIntValue(int intValue) {
            this.intValue = intValue;
        }

        public float getFloatValue() {
            return floatValue;
        }

        public void setFloatValue(float floatValue) {
            this.floatValue = floatValue;
        }

        public byte[] getByteArray() {
            return byteArray;
        }

        public void setByteArray(byte[] byteArray) {
            this.byteArray = byteArray;
        }
    }

    @Test
    public void test() {
        // Create and populate the struct
        SimpleStruct struct = new SimpleStruct();
        struct.setIntValue(42);
        struct.setFloatValue(3.14f);
        struct.setByteArray(new byte[] { 1, 2, 3 });

        // Encode to byte array
        byte[] serializedData = StructLayout.encode(struct);

        // Decode from byte array
        SimpleStruct decodedStruct = StructLayout.decode(serializedData, SimpleStruct.class);

        // Debug the byte array
        StructLayout.debug(serializedData);

        // Output the decoded struct
        System.out.println("Decoded Struct:");
        System.out.println("Int Value: " + decodedStruct.getIntValue());
        System.out.println("Float Value: " + decodedStruct.getFloatValue());

        // Debugging a byte array
        StructLayout.debug(decodedStruct);
    }

    @Test
    public void testAllDataTypesWithStructObject() {

        // Example Struct with all data types, including nested StructObjectField
        AllDataTypesStruct original = new AllDataTypesStruct();
        original.setInt32Value(42);
        original.setInt32BeValue(42);
        original.setFloatValue(123.45f);
        original.setStringValue("Hello, StructObject!");
        original.setFloatArray(new float[]{3.14f, 1.59f});
        original.setDoubleList(List.of(1.23, 4.56));
        original.setCustomStruct(new CustomStruct(7, new Key("NestedStructKey11111111111111111")));
        original.setInterfaceStructList(List.of(new StructAImpl(), new StructBImpl()));

        // Encode the object
        byte[] serializedData = StructLayout.encode(original);
        StructLayout.debug(serializedData);
        StructLayout.debug(original);

        // Decode the serialized data
        AllDataTypesStruct deserialized = StructLayout.decode(serializedData, AllDataTypesStruct.class);
        StructLayout.debug(deserialized);

        // Assertions to verify correct encoding/decoding
        assertEquals(original.getInt32Value(), deserialized.getInt32Value());
        assertEquals(original.getInt32BeValue(), deserialized.getInt32BeValue());
        assertEquals(original.getFloatValue(), deserialized.getFloatValue());
        assertEquals(original.getStringValue(), deserialized.getStringValue());
        assertArrayEquals(original.getFloatArray(), deserialized.getFloatArray());
        assertEquals(original.getDoubleList(), deserialized.getDoubleList());
        assertEquals(original.getCustomStruct().getId(), deserialized.getCustomStruct().getId());
        assertEquals(original.getCustomStruct().getKey(), deserialized.getCustomStruct().getKey());
        assertEquals(original.getInterfaceStructList().get(0).getClass(), deserialized.getInterfaceStructList().get(0).getClass());
        assertEquals(original.getInterfaceStructList().get(1).getClass(), deserialized.getInterfaceStructList().get(1).getClass());
    }

    @Test
    public void testSerializedDataEndianDifference() {
        AllDataTypesStruct original = new AllDataTypesStruct();
        original.setInt32Value(42); // Little-Endian
        original.setInt32BeValue(42); // Big-Endian
//        original.setStringValue("Hello, StructObject!");

        // Serialize object
        byte[] serializedData = StructLayout.encode(original);

        // Debug: serializedData 출력
        System.out.println("Serialized Data (First 8 Bytes):");
        for (int i = 0; i < 8; i++) {
            System.out.printf("%02X ", serializedData[i]);
        }
        System.out.println();

        // Little-Endian 값 읽기
        ByteBuffer leBuffer = ByteBuffer.wrap(serializedData, 0, 4).order(ByteOrder.LITTLE_ENDIAN);
        int int32Value = leBuffer.getInt();

        // Big-Endian 값 읽기
        ByteBuffer beBuffer = ByteBuffer.wrap(serializedData, 4, 4).order(ByteOrder.BIG_ENDIAN);
        int int32BeValue = beBuffer.getInt();

        // 원래 값과 비교
        assertEquals(42, int32Value, "Little-Endian 값을 제대로 읽지 못했습니다.");
        assertEquals(42, int32BeValue, "Big-Endian 값을 제대로 읽지 못했습니다.");

        // Little-Endian과 Big-Endian 값 비교
        assertFalse(java.util.Arrays.equals(
                        serializedData, 0, 4,
                        serializedData, 4, 8),
                "Little-Endian과 Big-Endian 값이 달라야 합니다.");
    }

    @Test
    public void debugPrintingTest() {
        byte[] data = new byte[] {
                (byte) 0x48, (byte) 0x65, (byte) 0x6c, (byte) 0x6c, (byte) 0x6f,
                (byte) 0x20, (byte) 0x57, (byte) 0x6f, (byte) 0x72, (byte) 0x6c,
                (byte) 0x64, (byte) 0x21, (byte) 0x00, (byte) 0x01, (byte) 0x02,
                (byte) 0x03, (byte) 0x04, (byte) 0x05
        };

        StructLayout.debug(data);
    }

    @Test
    public void debugPrintingTest2() {
        // Create and populate the struct
        AllDataTypesStruct struct = new AllDataTypesStruct();
        struct.setInt32Value(42);
        struct.setInt32BeValue(42);
        struct.setFloatValue(123.45f);
        struct.setStringValue("Hello, StructObject!");
        struct.setFloatArray(new float[]{3.14f, 1.59f});
        struct.setDoubleList(List.of(1.23, 4.56));
        struct.setCustomStruct(new CustomStruct(7L, new Key("11111111111111111111111111111111")));
        struct.setCustomStructList(List.of(
                new CustomStruct(10000L, new Key("11111111111111111111111111111111")),
                new CustomStruct(80000L, new Key("11111111111111111111111111111111"))
        ));
        struct.setInterfaceStruct(new StructBImpl());

        StructLayout.debug(struct);

        // Encode to byte array
        byte[] serializedData = StructLayout.encode(struct);
        StructLayout.debug(serializedData);

        // Decode from byte array
        AllDataTypesStruct decodedStruct = StructLayout.decode(serializedData, AllDataTypesStruct.class);

        // Debug the byte array
        System.out.println("Debug Serialized Data:");
        StructLayout.debug(serializedData);

        // Output the decoded struct
        System.out.println("Decoded Struct:");
        System.out.println("Int32 Value (Little-Endian): " + struct.getInt32Value());
        System.out.println("Int32 Value (Big-Endian): " + struct.getInt32BeValue());
        System.out.println("Float Value: " + struct.getFloatValue());
        System.out.println("String Value: " + struct.getStringValue());
        System.out.print("Float Array: ");
        for (float f : struct.getFloatArray()) {
            System.out.print(f + " ");
        }
        System.out.println();
        System.out.print("Double List: ");
        for (double d : struct.getDoubleList()) {
            System.out.print(d + " ");
        }
        System.out.println();
        System.out.println("Custom Struct:");
        System.out.println("  ID: " + struct.getCustomStruct().getId());
        System.out.println("  Key: " + struct.getCustomStruct().getKey());

        // Debug the decoded struct
        System.out.println("Debug Decoded Struct:");
        StructLayout.debug(struct);

        System.out.println("Debug with Field:");
        StructLayout.debug(decodedStruct);
    }

    @Getter
    @Setter
    public static class AllDataTypesStruct {
        @StructField(order = 1, type = Int32LEField.class)
        private int int32Value;

        @StructField(order = 2, type = Int32BEField.class)
        private int int32BeValue;

        @StructField(order = 3, type = Float32LEField.class)
        private float floatValue;

        @StructField(order = 4, type = StringCField.class)
        private String stringValue;

        @StructSequenceField(order = 5, elementType = Float32LEField.class, lengthType = Int32LEField.class)
        private float[] floatArray;

        @StructSequenceField(order = 6, elementType = Float64BEField.class)
        private List<Double> doubleList;

        @StructObjectField(order = 7)
        private CustomStruct customStruct;

        @StructSequenceObjectField(order = 8, lengthType = Int16LEField.class)
        private List<CustomStruct> customStructList;

        @StructObjectField(order = 9)
        private InterfaceStruct interfaceStruct;

        @StructSequenceObjectField(order = 10, lengthType = Int16LEField.class)
        private List<InterfaceStruct> interfaceStructList;

        private CustomStruct[] test;
    }

    @Getter
    @Setter
    public static class CustomStruct {
        @StructField(order = 1, type = Int32LEField.class)
        private long id;

        @StructField(order = 2, type = KeyField.class)
        private Key key;

        public CustomStruct() {
        }

        public CustomStruct(long id, Key key) {
            this.id = id;
            this.key = key;
        }
    }

    public static class KeyField extends FieldBase<Key> {
        private static final int KEY_LENGTH = 32; // 32 bytes
        public KeyField() {
            super(KEY_LENGTH);
        }

        @Override
        public byte[] encode(Key value) {
            byte[] byteValue = value.getKey().getBytes(StandardCharsets.UTF_8);
            if (byteValue.length != KEY_LENGTH) {
                throw new RuntimeException(
                        String.format(
                                "Invalid KeyField length: expected %d bytes but was %d bytes.",
                                KEY_LENGTH,
                                byteValue.length
                        )
                );
            }
            return byteValue;
        }

        @Override
        public Key decode(byte[] buffer, int offset) {
            if (buffer == null || buffer.length < offset + KEY_LENGTH) {
                throw new RuntimeException(
                        String.format(
                                "Invalid buffer length for KeyField decoding. Expected at least %d bytes from offset %d.",
                                KEY_LENGTH, offset
                        )
                );
            }
            return new Key(new String(buffer, offset, KEY_LENGTH, StandardCharsets.UTF_8));
        }
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class Key {
        private final String key;
    }

    @StructTypeSelector(dispatcher = InterfaceStruct.Dispatcher.class)
    public interface InterfaceStruct {
        class Dispatcher implements StructTypeDispatcher {
            @Override
            public Class<?> dispatch(byte[] data, int startOffset) {
                return switch (data[startOffset]) {
                    case 0, 1 -> StructAImpl.class;
                    case 2 -> StructBImpl.class;
                    default -> throw new RuntimeException("Invalid Struct Type" + data[startOffset] + " was detected.");
                };
            }

            @Override
            public int getNoDataSpan() {
                return 5;
            }
        }
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class StructAImpl implements InterfaceStruct {
        @StructField(order = 1, type = UInt8Field.class)
        private int id = 1;

        @StructField(order = 2, type = BorshStringField.class)
        private String text = "Hello, Struct A Object!";
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class StructBImpl implements InterfaceStruct {
        @StructField(order = 1, type = UInt8Field.class)
        private int id = 2;

        @StructField(order = 2, type = BorshStringField.class)
        private String text = "Hello, Struct B Object!";
    }
}
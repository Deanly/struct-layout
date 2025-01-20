package net.deanly.structlayout;

import lombok.Getter;
import lombok.Setter;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.type.DataType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StructLayoutTest {

    public static class SimpleStruct {
        @StructField(order = 1, dataType = DataType.INT16_LE)
        private int intValue;

        @StructField(order = 2, dataType = DataType.FLOAT32_LE)
        private float floatValue;

        @SequenceField(order = 3, elementType = DataType.BYTE)
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
        StructLayout.debug(serializedData);
        // 00000000: 2a 00 00 00 c3 f5 48 40 03 00 00 00 01 02 03      *.....H@.......
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
        original.setCustomStruct(new CustomStruct(7, "NestedStruct"));

        // Encode the object
        byte[] serializedData = StructLayout.encode(original);

        // Decode the serialized data
        AllDataTypesStruct deserialized = StructLayout.decode(serializedData, AllDataTypesStruct.class);

        // Assertions to verify correct encoding/decoding
        assertEquals(original.getInt32Value(), deserialized.getInt32Value());
        assertEquals(original.getInt32BeValue(), deserialized.getInt32BeValue());
        assertEquals(original.getFloatValue(), deserialized.getFloatValue());
        assertEquals(original.getStringValue(), deserialized.getStringValue());
        assertArrayEquals(original.getFloatArray(), deserialized.getFloatArray());
        assertEquals(original.getDoubleList(), deserialized.getDoubleList());
        assertEquals(original.getCustomStruct().getId(), deserialized.getCustomStruct().getId());
        assertEquals(original.getCustomStruct().getName(), deserialized.getCustomStruct().getName());
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
        struct.setCustomStruct(new CustomStruct(7L, "NestedStruct"));

        // Encode to byte array
        byte[] serializedData = StructLayout.encode(struct);

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
        System.out.println("  Name: " + struct.getCustomStruct().getName());

        // Debug the decoded struct
        System.out.println("Debug Decoded Struct:");
        StructLayout.debug(decodedStruct);
    }

    @Getter
    @Setter
    public static class AllDataTypesStruct {
        @StructField(order = 1, dataType = DataType.INT32_LE)
        private int int32Value;

        @StructField(order = 2, dataType = DataType.INT32_BE)
        private int int32BeValue;

        @StructField(order = 3, dataType = DataType.FLOAT32_LE)
        private float floatValue;

        @StructField(order = 4, dataType = DataType.STRING_C)
        private String stringValue;

        @SequenceField(order = 5, elementType = DataType.FLOAT32_LE)
        private float[] floatArray;

        @SequenceField(order = 6, elementType = DataType.FLOAT64_BE)
        private List<Double> doubleList;

        @StructObjectField(order = 7)
        private CustomStruct customStruct;
    }

    @Getter
    @Setter
    public static class CustomStruct {
        @StructField(order = 1, dataType = DataType.INT32_LE)
        private long id;

        @StructField(order = 2, dataType = DataType.STRING_C)
        private String name;

        public CustomStruct() {
        }

        public CustomStruct(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
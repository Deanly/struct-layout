package net.deanly.structlayout;

import lombok.Getter;
import lombok.Setter;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.DataType;
import net.deanly.structlayout.type.Endianness;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

public class StructLayoutTest {

    @Test
    public void testAllDataTypesWithStructObject() {

        // Example Struct with all data types, including nested StructObjectField
        AllDataTypesStruct original = new AllDataTypesStruct();
        original.setInt32Value(42);
        original.setInt32BeValue(42);
        original.setFloatValue(123.45f);
        original.setStringValue("Hello, StructObject!");
//        original.setFloatArray(new float[]{3.14f, 1.59f});
//        original.setCustomObject(new CustomObject(7, "NestedStruct"));

        // Encode the object
        byte[] serializedData = StructLayout.encode(original);

        // Decode the serialized data
        AllDataTypesStruct deserialized = StructLayout.decode(serializedData, AllDataTypesStruct.class);

        // Assertions to verify correct encoding/decoding
        assertEquals(original.getInt32Value(), deserialized.getInt32Value());
        assertEquals(original.getInt32BeValue(), deserialized.getInt32BeValue());
        assertEquals(original.getFloatValue(), deserialized.getFloatValue());
        assertEquals(original.getStringValue(), deserialized.getStringValue());
//        assertArrayEquals(original.getFloatArray(), deserialized.getFloatArray());
//        assertEquals(original.getCustomObject().getId(), deserialized.getCustomObject().getId());
//        assertEquals(original.getCustomObject().getName(), deserialized.getCustomObject().getName());
    }

    @Test
    public void testSerializedDataEndianDifference() {
        AllDataTypesStruct original = new AllDataTypesStruct();
        original.setInt32Value(42); // Little-Endian
        original.setInt32BeValue(42); // Big-Endian

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

//        @SequenceField(elementType = DataType.FLOAT32, lengthBytes = 1)
//        private float[] floatArray;
//
//        @StructObjectField(order = 4)
//        private CustomObject customObject;
    }

    @Getter
    @Setter
    public static class CustomObject {
        @StructField(order = 1, dataType = DataType.INT32_LE)
        private int id;

        @StructField(order = 2, dataType = DataType.STRING_C)
        private String name;

        public CustomObject() {
        }

        public CustomObject(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
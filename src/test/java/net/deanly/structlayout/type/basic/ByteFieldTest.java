package net.deanly.structlayout.type.basic;

import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceField;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ByteFieldTest {

    @Test
    void testDefaultConstructor() {
        // 기본 생성자 테스트
        ByteField layout = new ByteField();
        assertNotNull(layout);
        assertEquals(1, layout.getSpan()); // 스팬은 항상 1이어야 함
    }

    @Test
    void testDecodeValidData() {
        // 올바른 데이터와 오프셋 사용하여 decode 테스트
        ByteField layout = new ByteField();

        byte[] data = {0x11, 0x22, 0x33}; // 범용 데이터
        int offset = 1;
        Byte decoded = layout.decode(data, offset);

        assertNotNull(decoded);
        assertEquals((byte) 0x22, decoded); // data[1] 값 0x22 확인
    }

    @Test
    void testDecodeMinimumValue() {
        // 최소 `byte` 값(-128) 테스트
        ByteField layout = new ByteField();

        byte[] data = {-128};
        int offset = 0;
        Byte decoded = layout.decode(data, offset);

        assertNotNull(decoded);
        assertEquals(Byte.MIN_VALUE, decoded); // 최소값 확인
    }

    @Test
    void testDecodeMaximumValue() {
        // 최대 `byte` 값(127) 테스트
        ByteField layout = new ByteField();

        byte[] data = {127};
        int offset = 0;
        Byte decoded = layout.decode(data, offset);

        assertNotNull(decoded);
        assertEquals(Byte.MAX_VALUE, decoded); // 최대값 확인
    }

    @Test
    void testDecodeInvalidOffset() {
        // 유효하지 않은 offset 사용 시 예외 처리 테스트
        ByteField layout = new ByteField();

        byte[] data = {0x11, 0x22, 0x33};

        // 배열 범위를 벗어나는 offset 사용 시 예외 발생
        assertThrows(IllegalArgumentException.class, () -> {
            layout.decode(data, 3);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            layout.decode(data, -1);
        });
    }

    @Test
    void testEncodeValidValue() {
        // 정상 입력 값으로 encode 테스트
        ByteField layout = new ByteField();

        Byte value = (byte) 0x55; // 0x55 값 테스트
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded);
        assertEquals(1, encoded.length); // 결과 길이는 항상 1이어야 함
        assertEquals((byte) 0x55, encoded[0]); // 배열 값 확인
    }

    @Test
    void testEncodeMinimumValue() {
        // 최소값 -128 테스트
        ByteField layout = new ByteField();

        Byte value = Byte.MIN_VALUE;
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded);
        assertEquals(1, encoded.length);
        assertEquals(Byte.MIN_VALUE, encoded[0]); // 결과 값 확인
    }

    @Test
    void testEncodeMaximumValue() {
        // 최대값 127 테스트
        ByteField layout = new ByteField();

        Byte value = Byte.MAX_VALUE;
        byte[] encoded = layout.encode(value);

        assertNotNull(encoded);
        assertEquals(1, encoded.length);
        assertEquals(Byte.MAX_VALUE, encoded[0]); // 결과 값 확인
    }

    @Test
    void testEncodeNullValue() {
        // 입력 값이 null일 경우 확인
        ByteField layout = new ByteField();

        assertThrows(IllegalArgumentException.class, () -> {
            layout.encode(null);
        });
    }

    @Test
    void testIntegration_EncodeDecode() {
        // encode한 데이터를 다시 decode하는 동작 테스트
        ByteField layout = new ByteField();

        Byte originalValue = (byte) 0x7A;

        // Encoding
        byte[] encoded = layout.encode(originalValue);

        assertNotNull(encoded);
        assertEquals(1, encoded.length);
        assertEquals((byte) 0x7A, encoded[0]);

        // Decoding
        Byte decoded = layout.decode(encoded, 0);

        assertNotNull(decoded);
        assertEquals(originalValue, decoded); // encode와 decode 값이 동일한지 확인
    }

    @Test
    void testDecodeEmptyArray() {
        // 빈 데이터 배열 처리 확인
        ByteField layout = new ByteField();

        byte[] data = {};

        assertThrows(IllegalArgumentException.class, () -> {
            layout.decode(data, 0);
        });
    }

    @Test
    void testDecodeNullData() {
        // null 데이터 처리 확인
        ByteField layout = new ByteField();

        assertThrows(IllegalArgumentException.class, () -> {
            layout.decode(null, 0);
        });
    }

    public static class ByteStruct {
        @StructField(order = 1, type = ByteField.class)
        private byte singleByte; // 단일 byte 필드

        @StructSequenceField(order = 2, lengthType = UInt8Field.class, elementType = ByteField.class)
        private byte[] byteArray; // byte[] 필드

        public byte getSingleByte() {
            return singleByte;
        }

        public void setSingleByte(byte singleByte) {
            this.singleByte = singleByte;
        }

        public byte[] getByteArray() {
            return byteArray;
        }

        public void setByteArray(byte[] byteArray) {
            this.byteArray = byteArray;
        }
    }

    @Test
    public void testByteStructEncodingDecoding() {
        // Struct 정의
        ByteStruct struct = new ByteStruct();
        struct.setSingleByte((byte) 0x7A); // 단일 byte
        struct.setByteArray(new byte[]{0x1A, 0x2B, 0x3C}); // byte 배열

        // ** Encoding 테스트 **
        byte[] serializedData = StructLayout.encode(struct);
        System.out.println("Serialized Data: " + Arrays.toString(serializedData));
        assertNotNull(serializedData);
        assertEquals(5, serializedData.length); // 단일 byte + 배열 길이 + 배열 크기 = 1 + 1 + 3 = 5

        // ** Decoding 테스트 **
        ByteStruct decodedStruct = StructLayout.decode(serializedData, ByteStruct.class);
        assertNotNull(decodedStruct);

        // 단일 byte 필드 검증
        assertEquals(struct.getSingleByte(), decodedStruct.getSingleByte());
        // 배열 필드 검증
        assertArrayEquals(struct.getByteArray(), decodedStruct.getByteArray());
    }

    @Test
    public void testEmptyByteArray() {
        // ByteStruct의 배열이 비어있는 경우 테스트
        ByteStruct struct = new ByteStruct();
        struct.setSingleByte((byte) 0x7A); // 단일 byte
        struct.setByteArray(new byte[]{}); // 빈 배열

        // ** Encoding 테스트 **
        byte[] serializedData = StructLayout.encode(struct);
        System.out.println("Serialized Data with Empty Array: " + Arrays.toString(serializedData));
        assertNotNull(serializedData);
        assertEquals(2, serializedData.length); // 단일 byte와 길이 uint8 포함으로 2이어야 함

        // ** Decoding 테스트 **
        ByteStruct decodedStruct = StructLayout.decode(serializedData, ByteStruct.class);
        assertNotNull(decodedStruct);
        assertEquals(struct.getSingleByte(), decodedStruct.getSingleByte());
        assertArrayEquals(struct.getByteArray(), decodedStruct.getByteArray());
    }

    @Test
    public void testNullByteArray() {
        // ByteStruct의 배열이 null인 경우 테스트
        ByteStruct struct = new ByteStruct();
        struct.setSingleByte((byte) 0x7A); // 단일 byte
        struct.setByteArray(null); // null 배열

        // ** Encoding 테스트 **
        byte[] serializedData = StructLayout.encode(struct);
        System.out.println("Serialized Data with Null Array: " + Arrays.toString(serializedData));
        assertNotNull(serializedData);
        assertEquals(2, serializedData.length);

        // ** Decoding 테스트 **
        ByteStruct decodedStruct = StructLayout.decode(serializedData, ByteStruct.class);
        assertNotNull(decodedStruct);
        assertEquals(struct.getSingleByte(), decodedStruct.getSingleByte());
        assertArrayEquals(new byte[]{}, decodedStruct.getByteArray());
    }

    @Test
    public void testByteStructWithBoundaryValues() {
        // 최대 및 최소 byte 값을 테스트
        ByteStruct struct = new ByteStruct();
        struct.setSingleByte(Byte.MIN_VALUE); // 최소 byte 값 (-128)
        struct.setByteArray(new byte[]{Byte.MAX_VALUE, 0, Byte.MIN_VALUE}); // 최대값, 0, 최소값

        // ** Encoding 테스트 **
        byte[] serializedData = StructLayout.encode(struct);
        System.out.println("Serialized Data with Boundary Values: " + Arrays.toString(serializedData));
        assertNotNull(serializedData);
        assertEquals(5, serializedData.length);

        // ** Decoding 테스트 **
        ByteStruct decodedStruct = StructLayout.decode(serializedData, ByteStruct.class);
        assertNotNull(decodedStruct);
        assertEquals(struct.getSingleByte(), decodedStruct.getSingleByte());
        assertArrayEquals(struct.getByteArray(), decodedStruct.getByteArray());
    }
}
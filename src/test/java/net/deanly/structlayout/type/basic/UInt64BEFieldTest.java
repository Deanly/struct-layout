package net.deanly.structlayout.type.basic;

import lombok.Getter;
import lombok.Setter;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import org.junit.jupiter.api.Test;

import net.deanly.structlayout.type.guava.UnsignedLong;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class UInt64BEFieldTest {

    @Test
    void testEncode() {
        UInt64BEField layout = new UInt64BEField();

        // UnsignedLong 테스트 값들
        UnsignedLong[] values = {
                UnsignedLong.ZERO, // 0
                UnsignedLong.ONE, // 1
                UnsignedLong.valueOf("4294967295"), // 2^32 - 1
                UnsignedLong.valueOf("9223372036854775807"), // Long.MAX_VALUE (2^63 - 1)
                UnsignedLong.MAX_VALUE // 최대 unsigned 64비트 값 (2^64 - 1)
        };

        for (UnsignedLong value : values) {
            byte[] encoded = layout.encode(value);

            // 인코딩된 데이터가 정확히 8바이트인지 확인
            assertNotNull(encoded, "Encoded byte array should not be null");
            assertEquals(8, encoded.length, "Encoded byte array should have 8 bytes");

            // 인코딩 후 다시 디코딩하여 원래 값과 확인
            UnsignedLong decoded = layout.decode(encoded, 0);
            assertEquals(value, decoded, "Decoded value should match the original input value");
        }
    }

    @Test
    void testDecode() {
        UInt64BEField layout = new UInt64BEField();

        // 특정 big-endian 표현 데이터
        byte[] data = {0, 0, 0, 2, 0, 0, 0, 1}; // Represents 0x0000000200000001
        UnsignedLong expected = UnsignedLong.valueOf("8589934593"); // 2^33 + 1

        UnsignedLong decoded = layout.decode(data, 0);
        assertEquals(expected, decoded, "Decoded value should match the expected value");
    }

    @Test
    void testEncodeDecodeWithOffset() {
        UInt64BEField layout = new UInt64BEField();

        UnsignedLong value = UnsignedLong.valueOf("987654321987654321"); // 테스트 값
        byte[] buffer = new byte[16];

        // 특정 오프셋에 값을 인코딩
        System.arraycopy(layout.encode(value), 0, buffer, 8, 8);

        // 지정된 오프셋에서 값을 디코딩
        UnsignedLong decoded = layout.decode(buffer, 8);
        assertEquals(value, decoded, "Decoded value should match the original value when using offset");
    }

    @Test
    void testInvalidInputs() {
        UInt64BEField layout = new UInt64BEField();

        // null 입력 테스트
        assertThrows(IllegalArgumentException.class, () -> layout.decode(null, 0), "Null input should throw IllegalArgumentException");

        // 범위를 넘는 값 테스트
        assertThrows(IllegalArgumentException.class, () -> layout.encode(UnsignedLong.valueOf("18446744073709551616")), "Value exceeding unsigned 64-bit range should throw IllegalArgumentException");

        // 데이터 길이가 부족한 경우
        byte[] insufficientData = new byte[7]; // 8바이트 미만
        assertThrows(IllegalArgumentException.class, () -> layout.decode(insufficientData, 0), "Insufficient data length should throw IllegalArgumentException");

        // 잘못된 오프셋 테스트
        byte[] validData = new byte[8];
        assertThrows(IllegalArgumentException.class, () -> layout.decode(validData, 9), "Invalid offset should throw IllegalArgumentException");
    }

    @Test
    void testEncodeDecodeWithLargeValues() {
        UInt64BEField layout = new UInt64BEField();

        // 최대값 (2^64 - 1)
        UnsignedLong maxValue = UnsignedLong.MAX_VALUE;
        byte[] encoded = layout.encode(maxValue);

        // 디코딩 결과가 원래 값과 같은지 확인
        UnsignedLong decoded = layout.decode(encoded, 0);
        assertEquals(maxValue, decoded, "Decoded value should match the maximum UInt64 value");
    }

    @Test
    void testEdgeCases() {
        UInt64BEField layout = new UInt64BEField();

        // Edge case 테스트 값들
        UnsignedLong[] edgeValues = {
                UnsignedLong.valueOf("1"), // 0x0000000000000001
                UnsignedLong.valueOf("256"), // 0x0000000000000100
                UnsignedLong.valueOf("4294967296"), // 0x0000000100000000
                UnsignedLong.valueOf("9223372036854775808") // 0x8000000000000000 (2^63)
        };

        for (UnsignedLong value : edgeValues) {
            byte[] encoded = layout.encode(value);

            // 인코딩 및 디코딩 확인
            UnsignedLong decoded = layout.decode(encoded, 0);
            assertEquals(value, decoded, "Decoded value should match the original edge case value");
        }
    }

    @Getter
    @Setter
    public static class TestBEStruct {
        @StructField(order = 1, type = UInt64BEField.class)
        private UnsignedLong value1;
        @StructField(order = 2, type = UInt64BEField.class)
        private long value2;
    }
}


package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceObjectField;
import net.deanly.structlayout.type.basic.Int32BEField;
import net.deanly.structlayout.type.basic.StringCField;
import net.deanly.structlayout.type.basic.UInt8Field;
import net.deanly.structlayout.codec.encode.StructEncoder;
import net.deanly.structlayout.type.basic.NoneField;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class StructSequenceObjectFieldEncoderHandlerTest {

    // 테스트용 Struct 클래스 정의
    public static class SimpleStruct {
        @StructField(order = 1, type = Int32BEField.class)
        private int id;

        @StructField(order = 2, type = StringCField.class)
        private String value;

        // 생성자
        public SimpleStruct(int id, String value) {
            this.id = id;
            this.value = value;
        }

        // 기본 생성자 (필수)
        public SimpleStruct() {}

        // Getters (테스트에 필요 시 추가)
    }

    // 테스트용 클래스에 `@StructSequenceObjectField` 사용
    public static class StructWithSequence {
        @StructSequenceObjectField(order = 1, lengthType = UInt8Field.class)
        private List<SimpleStruct> items;

        // 생성자
        public StructWithSequence(List<SimpleStruct> items) {
            this.items = items;
        }

        // 기본 생성자 (필수)
        public StructWithSequence() {}
    }

    @Test
    void testEncodeStructSequenceObjectField() {
        // 1. 테스트 데이터 설정
        List<SimpleStruct> itemList = Arrays.asList(
                new SimpleStruct(1, "A"),
                new SimpleStruct(2, "B")
        );
        StructWithSequence testStruct = new StructWithSequence(itemList);

        // 2. 직렬화를 수행
        byte[] encodedData = StructEncoder.encode(testStruct);

        // 3. 예상 출력값 설정 (샘플)
        // 이 부분은 예상되는 바이트 배열로 설정 (단순화된 예시)
        // 각 SimpleStruct가 특정 형식(Int32 + UTF-8 String 등)으로 변환될 경우로 가정
        byte[] expectedData = new byte[] {
                2,                // UInt8Field로 표현된 길이 (2개의 items)
                // Item 1 (id=1, value="A")
                0, 0, 0, 1,       // Int32 (id)
                65, 0,               // UTF-8 ("A")
                // Item 2 (id=2, value="B")
                0, 0, 0, 2,       // Int32 (id)
                66, 0                // UTF-8 ("B")
        };

        StructLayout.debug(encodedData);
        StructLayout.debug(expectedData);

        // 4. 결과 검증
        assertArrayEquals(expectedData, encodedData, "Encoded data should match the expected byte array.");
    }

    // VoidField를 lengthType으로 설정
    public static class StructWithVoidLength {
        @StructSequenceObjectField(order = 1, lengthType = NoneField.class)
        private List<SimpleStruct> items;

        public StructWithVoidLength(List<SimpleStruct> items) {
            this.items = items;
        }

        public StructWithVoidLength() {}
    }

    @Test
    void testEncodeStructSequenceObjectField_withVoidField() {
        // 1. 테스트 데이터 준비
        List<SimpleStruct> itemList = Arrays.asList(
                new SimpleStruct(100, "Test1"),
                new SimpleStruct(200, "Test2")
        );

        StructWithVoidLength testStruct = new StructWithVoidLength(itemList);

        // 2. 직렬화 수행
        byte[] encodedData = StructEncoder.encode(testStruct);

        // 3. 예상 출력값 설정 (길이 정보 없이 요소만 직렬화됨)
        byte[] expectedData = new byte[] {
                // Item 1 (id=100, value="Test1")
                0, 0, 0, 100,   // Int32 (id)
                'T', 'e', 's', 't', '1', 0, // UTF-8 ("Test1")
                // Item 2 (id=200, value="Test2")
                0, 0, 0, (byte) 200,   // Int32 (id)
                'T', 'e', 's', 't', '2', 0  // UTF-8 ("Test2")
        };

        StructLayout.debug(encodedData); // 디버깅용 출력
        StructLayout.debug(expectedData); // 예상값 출력

        // 4. 결과 검증
        assertArrayEquals(expectedData, encodedData, "Encoded data should match the expected byte array.");
    }
}
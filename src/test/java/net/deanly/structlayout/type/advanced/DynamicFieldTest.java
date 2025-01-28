package net.deanly.structlayout.type.advanced;

import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.basic.Int32LEField;
import org.junit.jupiter.api.Test;

public class DynamicFieldTest {

    // Plan
    // 1. ParentLayout: 부모-자식 관계를 관리하고 데이터를 제공.
    // 2. DynamicSpanLayout 가변 크기 데이터를 처리할 수 있는 기능.
    // 3. ValidationLayout 데이터 검증을 자동화하여 안정성 증가.
    @Test
    public void test() {
        // Parent Layout spans 12 bytes
        ParentField<Integer> parentLayout = new ParentField<Integer>(12) {
            @Override
            public Integer decode(byte[] data, int offset) {
                return (int) data[offset]; // Example: Parent provides a simple value
            }

            @Override
            public byte[] encode(Integer value) {
                return new byte[]{value.byteValue()}; // Example encoding
            }

            @Override
            public byte[] getDataForChild(FieldBase<?> child) {
                return new byte[0]; // Example implementation
            }
        };

        // OffsetLayout with a base layout of 4 bytes (child data at offset 4)
        OffsetField<Integer> offsetLayout = new OffsetField<>(new Int32LEField(), 4);
        parentLayout.addChild(offsetLayout);

        // Testing decoding
        byte[] exampleData = new byte[12];
        Integer childValue = offsetLayout.decode(exampleData, 0); // Decodes at offset: 4
    }
}

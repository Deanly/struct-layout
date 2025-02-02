package net.deanly.structlayout.analysis;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.type.DynamicSpanField;
import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.basic.Int32LEField;
import net.deanly.structlayout.type.basic.StringCField;
import net.deanly.structlayout.type.basic.UInt8Field;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CachedLayoutProviderTest {

    @Test
    void testNonDynamicSpanFieldIsCached() {
        // Arrange
        Field<?> field1 = CachedLayoutProvider.getLayout(UInt8Field.class);
        Field<?> field2 = CachedLayoutProvider.getLayout(UInt8Field.class);

        // Assert - 정적 타입 캐싱 확인
        assertNotNull(field1);
        assertTrue(field1 instanceof UInt8Field);
        assertSame(field1, field2); // 캐싱된 동일 객체 확인
    }

    @Test
    void testDynamicSpanFieldIsNotCached() {
        // Arrange: 로컬 클래스 대신 정적 중첩 클래스로 변경
        Field<Integer> dynamicField1 = CachedLayoutProvider.getLayout(DynamicField.class);
        Field<Integer> dynamicField2 = CachedLayoutProvider.getLayout(DynamicField.class);

        // Assert
        assertNotNull(dynamicField1);
        assertNotNull(dynamicField2);
        assertNotSame(dynamicField1, dynamicField2); // 새로운 인스턴스를 반환
    }

    // 새로 정의한 정적 중첩 클래스
    static class DynamicField extends FieldBase<Integer> implements DynamicSpanField {
        private int span = 32;

        public DynamicField() {
            super(-1);
        }

        @Override
        public byte[] encode(Integer value) {
            return new byte[0];
        }

        @Override
        public Integer decode(byte[] bytes, int offset) {
            return null;
        }

        public void setSpan(int span) {
            this.span = span;
        }

        @Override
        public int calculateSpan(byte[] data, int offset) {
            return span;
        }

        @Override
        public int getNoDataSpan() {
            return 0;
        }

        @Override
        public int getSpan() {
            return span;
        }
    }

    @Test
    void testFinalSpanFieldIsCached() {
        // Arrange
        Field<String> field1 = CachedLayoutProvider.getLayout(FinalSpanField.class);
        Field<String> field2 = CachedLayoutProvider.getLayout(FinalSpanField.class);

        // Assert
        assertNotNull(field1);
        assertSame(field1, field2); // 캐싱된 동일 객체 확인
    }

    // 새로 정의한 정적 중첩 클래스
    static class FinalSpanField extends FieldBase<String> implements DynamicSpanField {
        private static final int span = 64;

        public FinalSpanField() {
            super(span);
        }

        @Override
        public byte[] encode(String value) {
            return new byte[0];
        }

        @Override
        public String decode(byte[] bytes, int offset) {
            return null;
        }

        public void setSpan(int span) {
            throw new UnsupportedOperationException("Span is final");
        }

        @Override
        public int getSpan() {
            return span;
        }

        @Override
        public int calculateSpan(byte[] data, int offset) {
            return span;
        }

        @Override
        public int getNoDataSpan() {
            return 0;
        }
    }

    @Test
    void testStringCFieldCachingBehavior() {
        // Arrange - StringCField는 span이 동적이므로 DynamicSpanField 조건 확인
        Field<?> field1 = CachedLayoutProvider.getLayout(StringCField.class);
        Field<?> field2 = CachedLayoutProvider.getLayout(StringCField.class);

        // Assert - StringCField는 DynamicSpanField이므로 새로운 객체가 반환되어야 함
        assertNotNull(field1);
        assertNotNull(field2);
        assertNotSame(field1, field2);
    }

    @Test
    void testInt32LEFieldIsCached() {
        // Arrange
        Field<?> field1 = CachedLayoutProvider.getLayout(Int32LEField.class);
        Field<?> field2 = CachedLayoutProvider.getLayout(Int32LEField.class);

        // Assert - 일반 `Field` 구현체는 캐싱되어야 함
        assertNotNull(field1);
        assertTrue(field1 instanceof Int32LEField);
        assertSame(field1, field2);
    }
}
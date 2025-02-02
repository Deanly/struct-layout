package net.deanly.structlayout.dispatcher;

import lombok.NoArgsConstructor;
import net.deanly.structlayout.annotation.StructTypeSelector;
import net.deanly.structlayout.codec.decode.StructDecoder;
import net.deanly.structlayout.codec.decode.StructDecodeResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructTypeResolverTest {

    @StructTypeSelector(dispatcher = TestDispatcher.class)
    interface TestBase {
        byte getIdentifier();
    }

    public static class TypeA implements TestBase {
        private final byte identifier;

        public TypeA() {
            this.identifier = 1; // ID 설정
        }

        @Override
        public byte getIdentifier() {
            return identifier;
        }
    }

    public static class TypeB implements TestBase {
        private final byte identifier;

        public TypeB() {
            this.identifier = 2; // ID 설정
        }

        @Override
        public byte getIdentifier() {
            return identifier;
        }
    }

    public static class TestDispatcher implements StructTypeDispatcher {
        // identifier(바이트 값)에 따라 결정
        @Override
        public Class<?> dispatch(byte[] data, int startOffset) {
            if (data[startOffset] == 1) {
                return TypeA.class;
            } else if (data[startOffset] == 2) {
                return TypeB.class;
            }
            throw new IllegalArgumentException("Invalid identifier: " + data[0]);
        }

        @Override
        public int getNoDataSpan() {
            return 0;
        }
    }

    @Test
    void testResolve_TypeA() throws Exception {
        byte[] data = {1}; // identifier 1은 TypeA에 매핑
        Class<? extends TestBase> resolvedClass = StructTypeResolver.resolveClass(data, TestBase.class, 0);

        assertNotNull(resolvedClass, "Resolved class should not be null");
        assertEquals(TypeA.class, resolvedClass, "Should resolve to TypeA");

        TestBase instance = resolvedClass.getDeclaredConstructor().newInstance();
        assertInstanceOf(TypeA.class, instance, "Instance should be of TypeA");
        assertEquals(1, instance.getIdentifier(), "Identifier should match TypeA");
    }

    @Test
    void testResolve_TypeB() throws Exception {
        byte[] data = {2}; // identifier 2은 TypeB에 매핑
        Class<? extends TestBase> resolvedClass = StructTypeResolver.resolveClass(data, TestBase.class, 0);

        assertNotNull(resolvedClass, "Resolved class should not be null");
        assertEquals(TypeB.class, resolvedClass, "Should resolve to TypeB");

        TestBase instance = resolvedClass.getDeclaredConstructor().newInstance();
        assertInstanceOf(TypeB.class, instance, "Instance should be of TypeB");
        assertEquals(2, instance.getIdentifier(), "Identifier should match TypeB");
    }

    @Test
    void testInvalidIdentifier() {
        byte[] data = {3}; // invalid identifier
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            StructTypeResolver.resolveClass(data, TestBase.class, 0);
        });

        assertEquals("Invalid identifier: 3", exception.getMessage());
    }

}
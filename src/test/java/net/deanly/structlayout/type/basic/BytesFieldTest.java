package net.deanly.structlayout.type.basic;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class BytesFieldTest {

    @Test
    void testBytes2Field() {
        Bytes2Field field = new Bytes2Field();
        byte[] original = {0x01, 0x02};
        byte[] data = {0x01, 0x02, 0x03, 0x04};

        // Encode Test
        assertEquals(2, field.encode(original).length);
        assertArrayEquals(new byte[]{0x01, 0x02}, field.encode(original));

        // Decode Test
        byte[] decoded = field.decode(data, 0);
        assertArrayEquals(original, decoded);

        assertThrows(IllegalArgumentException.class, () -> field.encode(new byte[]{0x01}));
        assertThrows(IllegalArgumentException.class, () -> field.decode(data, 3)); // Out of bound
    }

    @Test
    void testBytes3Field() {
        Bytes3Field field = new Bytes3Field();
        byte[] original = {0x01, 0x02, 0x03};
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05};

        // Encode Test
        assertEquals(3, field.encode(original).length);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, field.encode(original));

        // Decode Test
        byte[] decoded = field.decode(data, 1);
        assertArrayEquals(new byte[]{0x02, 0x03, 0x04}, decoded);

        assertThrows(IllegalArgumentException.class, () -> field.encode(new byte[]{0x01, 0x02}));
        assertThrows(IllegalArgumentException.class, () -> field.decode(data, 3)); // Out of bound
    }

    @Test
    void testBytes4Field() {
        Bytes4Field field = new Bytes4Field();
        byte[] original = {0x01, 0x02, 0x03, 0x04};
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};

        // Encode Test
        assertEquals(4, field.encode(original).length);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, field.encode(original));

        // Decode Test
        byte[] decoded = field.decode(data, 2);
        assertArrayEquals(new byte[]{0x03, 0x04, 0x05, 0x06}, decoded);

        assertThrows(IllegalArgumentException.class, () -> field.encode(new byte[]{0x01, 0x02, 0x03}));
        assertThrows(IllegalArgumentException.class, () -> field.decode(data, 3)); // Out of bound
    }

    @Test
    void testBytes5Field() {
        Bytes5Field field = new Bytes5Field();
        byte[] original = {0x01, 0x02, 0x03, 0x04, 0x05};
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};

        // Encode Test
        assertEquals(5, field.encode(original).length);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05}, field.encode(original));

        // Decode Test
        byte[] decoded = field.decode(data, 1);
        assertArrayEquals(new byte[]{0x02, 0x03, 0x04, 0x05, 0x06}, decoded);

        assertThrows(IllegalArgumentException.class, () -> field.encode(new byte[]{0x01, 0x02, 0x03, 0x04}));
        assertThrows(IllegalArgumentException.class, () -> field.decode(data, 3)); // Out of bound
    }

    @Test
    void testBytes6Field() {
        Bytes6Field field = new Bytes6Field();
        byte[] original = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};

        // Encode Test
        assertEquals(6, field.encode(original).length);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06}, field.encode(original));

        // Decode Test
        byte[] decoded = field.decode(data, 2);
        assertArrayEquals(new byte[]{0x03, 0x04, 0x05, 0x06, 0x07, 0x08}, decoded);

        assertThrows(IllegalArgumentException.class, () -> field.encode(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05}));
        assertThrows(IllegalArgumentException.class, () -> field.decode(data, 3)); // Out of bound
    }

    @Test
    void testBytes7Field() {
        Bytes7Field field = new Bytes7Field();
        byte[] original = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};

        // Encode Test
        assertEquals(7, field.encode(original).length);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07}, field.encode(original));

        // Decode Test
        byte[] decoded = field.decode(data, 1);
        assertArrayEquals(new byte[]{0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08}, decoded);

        assertThrows(IllegalArgumentException.class, () -> field.encode(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06}));
        assertThrows(IllegalArgumentException.class, () -> field.decode(data, 3)); // Out of bound
    }

    @Test
    void testBytes8Field() {
        Bytes8Field field = new Bytes8Field();
        byte[] original = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A};

        // Encode Test
        assertEquals(8, field.encode(original).length);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08}, field.encode(original));

        // Decode Test
        byte[] decoded = field.decode(data, 2);
        assertArrayEquals(new byte[]{0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A}, decoded);

        assertThrows(IllegalArgumentException.class, () -> field.encode(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07}));
        assertThrows(IllegalArgumentException.class, () -> field.decode(data, 3)); // Out of bound
    }
}
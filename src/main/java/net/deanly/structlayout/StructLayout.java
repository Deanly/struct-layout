package net.deanly.structlayout;

import net.deanly.structlayout.codec.StructEncoder;
import net.deanly.structlayout.codec.StructDecoder;

public class StructLayout {

    /**
     * Encodes an object into a serialized byte array.
     *
     * @param object The object to serialize.
     * @param <T>    The type of the object.
     * @return The serialized byte array.
     */
    public static <T> byte[] encode(T object) {
        return StructEncoder.encode(object);
    }

    /**
     * Decodes a byte array into an instance of the given class.
     *
     * @param data The serialized byte array.
     * @param type The class type to deserialize into.
     * @param <T>  The type of the class.
     * @return The deserialized object.
     */
    public static <T> T decode(byte[] data, Class<T> type) {
        return StructDecoder.decode(type, data, 0);
    }
}
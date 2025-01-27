package net.deanly.structlayout;

import net.deanly.structlayout.analysis.HexDumpUtil;
import net.deanly.structlayout.codec.encode.StructEncoder;
import net.deanly.structlayout.codec.decode.StructDecoder;

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
        return StructDecoder.decode(type, data, 0).getValue();
    }

    /**
     * Debugs the given byte array by outputting it in a hex dump format.
     *
     * @param data The byte array to debug.
     */
    public static void debug(byte[] data) {
        if (data == null || data.length == 0) {
            System.out.println("[Empty byte array]");
            return;
        }
        String hexDump = HexDumpUtil.toHexDump(data);
        System.out.println(hexDump);
        System.out.println("Total Bytes: " + data.length);
    }

    /**
     * Debugs the object by encoding it and providing a breakdown of each field.
     *
     * @param obj The object to debug.
     */
    public static void debug(Object obj) {
        if (obj == null) {
            System.out.println("[Object is null]");
            return;
        }

        try {
            StructEncoder.encodeWithDebug(obj);
        } catch (Exception e) {
            System.err.println("[Field Debug Error: " + e.getMessage() + "]");
        }
    }

}
package net.deanly.structlayout;

import net.deanly.structlayout.analysis.FieldDebugger;
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
    }

    /**
     * Debugs an object by encoding it into a byte array using StructLayout.encode
     * and displaying it in a hex dump format.
     *
     * @param obj The Object to debug.
     */
    public static void debug(Object obj) {
        if (obj == null) {
            System.out.println("[Object is null]");
            return;
        }
        try {
            // Object를 encode하여 byte[]로 변환
            byte[] encoded = StructEncoder.encode(obj);
            // 변환한 byte[]를 debug(byte[])로 전달
            debug(encoded);
        } catch (Exception e) {
            System.err.println("[Error encoding object: " + e.getMessage() + "]");
        }
    }

    /**
     * Debugs the object by encoding it and providing a breakdown of each field.
     *
     * @param obj The object to debug.
     */
    public static void debugWithFields(Object obj) {
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

//    /**
//     * Debugs the given byte array by class structure and provides a breakdown of its fields.
//     *
//     * @param data  The serialized byte array to debug.
//     * @param clazz The class type to decode.
//     */
//    public static void debugWithFields(byte[] data, Class<?> clazz) {
//        if (data == null || data.length == 0) {
//            System.out.println("[Empty byte array]");
//            return;
//        }
//
//        try {
//            FieldDebugger.debugByteArrayWithFields(data, clazz);
//        } catch (Exception e) {
//            System.err.println("[Field Debug Error: " + e.getMessage() + "]");
//        }
//    }
}
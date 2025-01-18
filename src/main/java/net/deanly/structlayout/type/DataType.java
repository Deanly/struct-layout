package net.deanly.structlayout.type;

import lombok.Getter;
import net.deanly.structlayout.Layout;
import net.deanly.structlayout.type.impl.*;

/**
 * Defines the supported data types for annotation and layout.
 */
@Getter
public enum DataType {
    // 정수 타입 (Integer Types)
    INT8(Int8Layout.class, Byte.class),    // Signed 8-bit integer
    UInt8(UInt8Layout.class, Short.class),  // Unsigned 8-bit integer (represented as short in Java)
    INT16_LE(Int16LELayout.class, Short.class),
    INT16_BE(Int16BELayout.class, Short.class),
    UINT16_LE(UInt16LELayout.class, Integer.class), // Unsigned 16-bit integer
    UINT16_BE(UInt16BELayout.class, Integer.class),
    INT32_LE(Int32LELayout.class, Integer.class), // Signed 32-bit integer
    INT32_BE(Int32BELayout.class, Integer.class),
    UINT32_LE(UInt32LELayout.class, Long.class), // Unsigned 32-bit integer
    UINT32_BE(UInt32BELayout.class, Long.class),
    INT64_LE(Int64LELayout.class, Long.class),    // Signed 64-bit integer
    INT64_BE(Int64BELayout.class, Long.class),
    UINT64_LE(UInt64LELayout.class, java.math.BigInteger.class), // Unsigned 64-bit uses BigInteger
    UINT64_BE(UInt64BELayout.class, java.math.BigInteger.class),

    // 부동소수점 타입 (Floating-Point Types)
    FLOAT32_LE(Float32LELayout.class, Float.class), // IEEE 754 32-bit floating point
    FLOAT32_BE(Float32BELayout.class, Float.class),
    FLOAT64_LE(Float64LELayout.class, Double.class), // IEEE 754 64-bit floating point
    FLOAT64_BE(Float64BELayout.class, Double.class),

    // 문자 타입 (Character Types)
    CHAR_C(CharCLayout.class, Byte.class),   // Character (equivalent to int8_t in C, usually ASCII)
    UCHAR_C(UCharCLayout.class, Short.class), // Unsigned character (0 to 255, uint8_t equivalent)

    // 문자열
    STRING_C(StringCLayout.class, String.class), // Variable-length string
    ;

    DataType(Class<? extends Layout<?>> layout, Class<?> fieldType) {
        this.layout = layout;
        this.fieldType = fieldType;
    }

    private final Class<? extends Layout<?>> layout;
    private final Class<?> fieldType;

}
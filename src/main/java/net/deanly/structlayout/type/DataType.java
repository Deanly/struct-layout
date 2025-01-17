package net.deanly.structlayout.type;

/**
 * Defines the supported data types for annotation and layout.
 */
public enum DataType {
    // 정수 타입 (Integer Types)
    INT8_LE, INT6_BE,        // Signed 8-bit integer
    UINT8_LE, UINT8_BE,      // Unsigned 8-bit integer
    INT16_LE, INT16_BE,      // Signed 16-bit integer
    UINT16_LE, UINT16_BE,     // Unsigned 16-bit integer
    INT32_LE, INT32_BE,      // Signed 32-bit integer
    UINT32_LE, UINT32_BE,     // Unsigned 32-bit integer
    INT64_LE, INT64_BE,      // Signed 64-bit integer
    UINT64_LE, UINT64_BE,     // Unsigned 64-bit integer

    // 부동소수점 타입 (Floating-Point Types)
    FLOAT32_LE, FLOAT32_BE,    // IEEE 754 32-bit floating point
    FLOAT64_LE, FLOAT64_BE,    // IEEE 754 64-bit floating point

    // 문자 타입 (Character Types)
    CHAR_C,       // Character (equivalent to int8_t in C, usually ASCII)
    UCHAR_C,      // Unsigned character (0 to 255, uint8_t equivalent)

    // 문자열
    STRING_C,     // Variable-length string,
}

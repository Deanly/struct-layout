package net.deanly.structlayout.type;

import lombok.Getter;
import net.deanly.structlayout.Layout;
import net.deanly.structlayout.type.basic.*;

/**
 * Defines the supported data types for annotation and layout.
 */
@Getter
public enum DataType {
    // Byte 타입
    /**
     * Represents a DataType configuration for handling byte values.
     * This configuration links the ByteLayout class with the Byte type,
     * enabling operations for encoding and decoding single signed byte values.
     *
     * The associated layout class, ByteLayout, is used to manage the data's
     * structure and representation in serialized form, and the field type
     * is defined as Byte.
     */
    BYTE(ByteLayout.class, Byte.class),

    // 정수 타입 (Integer Types)
    /**
     * Represents a DataType configuration for handling signed 8-bit integer (INT8) values.
     *
     * This configuration associates the {@link Int8Layout} class with the {@link Byte} type.
     * It enables the encoding and decoding of single signed 8-bit integer values,
     * where the {@link Int8Layout} serves as the structure defining the data layout
     * in serialized form.
     */
    INT8(Int8Layout.class, Short.class),    // Signed 8-bit integer
    /**
     * Represents a DataType configuration for handling unsigned 8-bit integer (UINT8) values.
     *
     * This configuration associates the {@link UInt8Layout} class with the {@link Short} type.
     * It enables the encoding and decoding of single unsigned 8-bit integer values.
     *
     * The {@link UInt8Layout} class serves as the layout definition, managing the
     * structure and representation of the data in a serialized form. The unsigned
     * 8-bit value is represented as {@link Short} in Java, as Java does not natively
     * support unsigned byte types.
     */
    UINT8(UInt8Layout.class, Short.class),  // Unsigned 8-bit integer (represented as short in Java)
    /**
     * Represents a DataType configuration for handling signed 16-bit integer (INT16)
     * values in little-endian byte order.
     *
     * This configuration associates the {@code Int16LELayout} class with the
     * {@code Short} type. It provides support for encoding and decoding signed
     * 16-bit integer values in a little-endian format.
     *
     * The {@code Int16LELayout} class defines the underlying structure and
     * representation of the data, while the field type is represented as a
     * {@code Short} in the Java environment.
     */
    INT16_LE(Int16LELayout.class, Short.class),
    /**
     * Represents a DataType configuration for handling signed 16-bit integer (INT16)
     * values in big-endian byte order.
     *
     * This configuration associates the {@code Int16BELayout} class with the
     * {@code Short} type. It provides support for encoding and decoding signed
     * 16-bit integer values in a big-endian format.
     *
     * The {@code Int16BELayout} class defines the underlying structure and
     * representation of the data, while the field type is represented as a
     * {@code Short} in the Java environment.
     */
    INT16_BE(Int16BELayout.class, Short.class),
    /**
     * Represents a DataType configuration for handling unsigned 16-bit integer (UINT16)
     * values in little-endian byte order.
     *
     * This configuration associates the {@code UInt16LELayout} class with the
     * {@code Integer} type. It enables the encoding and decoding of unsigned 16-bit
     * integer values in a little-endian format.
     *
     * The {@code UInt16LELayout} class defines the underlying structure and
     * representation of the data, while the field type is represented as an
     * {@code Integer} in the Java environment, as Java does not natively support
     * unsigned integer types.
     */
    UINT16_LE(UInt16LELayout.class, Integer.class), // Unsigned 16-bit integer
    /**
     * Represents a DataType configuration for handling unsigned 16-bit integer (UINT16)
     * values in big-endian byte order.
     *
     * This configuration associates the {@code UInt16BELayout} class with the
     * {@code Integer} type. It provides support for encoding and decoding unsigned
     * 16-bit integer values in a big-endian format.
     *
     * The {@code UInt16BELayout} class is used to define the structure and representation
     * of the data in serialized form. Since Java does not natively support unsigned
     * integer types, the unsigned 16-bit value is represented as an {@code Integer}
     * in the Java environment.
     */
    UINT16_BE(UInt16BELayout.class, Integer.class),
    /**
     * Represents a DataType configuration for handling signed 32-bit integer (INT32) values
     * in little-endian byte order.
     *
     * This configuration associates the {@code Int32LELayout} class with the {@code Integer}
     * type. It provides support for encoding and decoding signed 32-bit integer values stored
     * in a little-endian byte format.
     *
     * The {@code Int32LELayout} class defines the underlying structure and representation
     * of the data in serialized form. The field type is represented as an {@code Integer}
     * in the Java environment.
     */
    INT32_LE(Int32LELayout.class, Integer.class), // Signed 32-bit integer
    /**
     * Represents a DataType configuration for handling signed 32-bit integer (INT32)
     * values in big-endian byte order.
     *
     * This configuration associates the {@code Int32BELayout} class with the
     * {@code Integer} type. It enables the encoding and decoding of signed 32-bit
     * integer values stored in a big-endian byte format.
     *
     * The {@code Int32BELayout} class defines the underlying structure and representation
     * of the data in serialized form. The field type is represented as an {@code Integer}
     * in the Java environment.
     */
    INT32_BE(Int32BELayout.class, Integer.class),
    /**
     * Represents a DataType configuration for handling unsigned 32-bit integer (UINT32)
     * values in little-endian byte order.
     *
     * This configuration associates the {@code UInt32LELayout} class with the {@code Long}
     * type. It provides support for encoding and decoding unsigned 32-bit integer values
     * in a little-endian format.
     *
     * The {@code UInt32LELayout} class defines the underlying structure and representation
     * of the data in serialized form. Since Java does not natively support unsigned integer
     * types, the unsigned 32-bit value is represented as a {@code Long} in the Java environment.
     */
    UINT32_LE(UInt32LELayout.class, Long.class), // Unsigned 32-bit integer
    /**
     * Represents a 32-bit unsigned integer in Big-Endian byte order.
     * The type is defined by a specific layout class and the associated data type.
     *
     * This constant utilizes {@link UInt32BELayout} for its layout logic and is
     * tied to {@link Long} as its output data type.
     */
    UINT32_BE(UInt32BELayout.class, Long.class),
    /**
     * INT64_LE represents a 64-bit signed integer layout in little-endian byte order.
     * It defines a structured data type for handling 64-bit integer values with
     * little-endian endianness, commonly used in binary data parsing or serialization.
     *
     * This constant associates the Int64LELayout class for the data layout definition
     * and the Long class representing the Java type it maps to.
     */
    INT64_LE(Int64LELayout.class, Long.class),    // Signed 64-bit integer
    /**
     * Represents a 64-bit (8-byte) integer stored in Big Endian format.
     * Associates with a specific layout definition class and the Long data type.
     * It is used for handling structured binary data with specified byte order
     * and layout conventions.
     *
     * This ensures compatibility with systems that follow Big Endian storage.
     */
    INT64_BE(Int64BELayout.class, Long.class),
    /**
     * Represents an unsigned 64-bit integer in little-endian byte order.
     * This constant defines a specific data layout for serialization or
     * deserialization of 64-bit unsigned integers when working with structured binary data.
     *
     * It utilizes {@code UInt64LELayout} to manage the layout and {@code java.math.BigInteger}
     * to handle the mathematical representation of large unsigned values.
     */
    UINT64_LE(UInt64LELayout.class, java.math.BigInteger.class), // Unsigned 64-bit uses BigInteger
    /**
     * UINT64_BE represents a 64-bit unsigned integer in Big-Endian format.
     * It is used in structured data layouts requiring precise control over
     * data size and endianness.
     *
     * This variable is associated with the `UInt64BELayout` class to define
     * its specific layout characteristics and the `java.math.BigInteger` class
     * to enable handling large, unsigned integer values.
     */
    UINT64_BE(UInt64BELayout.class, java.math.BigInteger.class),

    // 부동소수점 타입 (Floating-Point Types)
    /**
     * Represents a 32-bit floating-point number with little-endian byte order.
     * This constant is used to define the layout of structured data fields
     * where the value is stored in a 32-bit floating-point format and follows
     * the little-endian endianness convention.
     */
    FLOAT32_LE(Float32LELayout.class, Float.class), // IEEE 754 32-bit floating point
    /**
     * Represents a 32-bit floating-point data type with big-endian byte order.
     * This type is defined using the {@link Float32BELayout} layout class and
     * corresponds to the {@link Float} Java type.
     *
     * The layout class ({@link Float32BELayout}) provides the specific details
     * of how this data type is structured and interpreted in memory, specifically
     * for the big-endian format.
     */
    FLOAT32_BE(Float32BELayout.class, Float.class),
    /**
     * The FLOAT64_LE variable represents a 64-bit floating-point number
     * in little-endian byte order. It utilizes the Float64LELayout for
     * layout handling and maps to the Double Java type.
     */
    FLOAT64_LE(Float64LELayout.class, Double.class), // IEEE 754 64-bit floating point
    /**
     * Represents a 64-bit floating point data type in big-endian byte order.
     * <p>
     * The constant is associated with the layout class {@code Float64BELayout}
     * for interpreting the binary structure, and the high-level Java type {@code Double}.
     */
    FLOAT64_BE(Float64BELayout.class, Double.class),

    // 문자 타입 (Character Types)
    /**
     * CHAR_C defines the layout for a single char data type.
     * This layout maps a character value using a specific layout class (CharCLayout)
     * and byte representation (Byte.class).
     *
     * The associated layout class ({@code CharCLayout}) is responsible for
     * interpreting and structuring the character data, potentially considering
     * constraints or characteristics specific to the char type in structured data.
     */
    CHAR_C(CharCLayout.class, Byte.class),   // Character (equivalent to int8_t in C, usually ASCII)
    /**
     * Represents a character type layout for unsigned char values.
     * This variable provides a mapping between the low-level memory layout
     * representation of an unsigned character and its corresponding Java type.
     *
     * UCHAR_C is used in structured data parsing to define a specific field
     * layout for a two-byte unsigned character, facilitating interaction with
     * binary data using an appropriate type and memory alignment.
     */
    UCHAR_C(UCharCLayout.class, Integer.class), // Unsigned character (0 to 255, uint8_t equivalent)
    /**
     * A predefined configuration for a layout mapping to a string structure. This variable defines
     * specific layout properties and type mappings for handling string data in a structured format.
     *
     * The layout is tied to the `StringCLayout` class responsible for managing the mapping rules,
     * and it is associated with the `String` class as its corresponding data type.
     */
    STRING_C(StringCLayout.class, String.class), // Variable-length string
    ;

    DataType(Class<? extends Layout<?>> layout, Class<?> fieldType) {
        this.layout = layout;
        this.fieldType = fieldType;
    }

    private final Class<? extends Layout<?>> layout;
    private final Class<?> fieldType;

    /**
     * Checks if the current DataType is an integer type.
     */
    public boolean isIntegerType() {
        return fieldType == Integer.class
                || fieldType == Short.class
                || fieldType == Byte.class
                || fieldType == Long.class
                || fieldType == java.math.BigInteger.class;
    }

    /**
     * Checks if the current DataType is a floating-point type.
     */
    public boolean isFloatType() {
        return fieldType == Float.class || fieldType == Double.class;
    }

    /**
     * Checks if the current DataType is a string type.
     */
    public boolean isStringType() {
        return fieldType == String.class;
    }

    /**
     * Checks if the current DataType matches the provided field type.
     *
     * @param clazz The target field type.
     * @return True if the field type matches the current DataType.
     */
    public boolean matches(Class<?> clazz) {
        return fieldType.isAssignableFrom(clazz);
    }

    /**
     * Checks if the current DataType is numeric (integer or floating-point).
     *
     * @return True if numeric, otherwise false.
     */
    public boolean isNumeric() {
        return isIntegerType() || isFloatType();
    }
}
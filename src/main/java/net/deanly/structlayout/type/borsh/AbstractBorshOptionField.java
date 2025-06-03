package net.deanly.structlayout.type.borsh;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.type.DynamicSpanField;
import net.deanly.structlayout.type.FieldBase;

/**
 * <h1>AbstractCOptionField</h1>
 * AbstractCOptionField is an abstract class designed to handle optional fields in struct layouts.
 * It uses a tagging mechanism to distinguish between `Some` (value exists) and `None` (null) states
 * during serialization and deserialization, providing flexibility for optional field management.
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Supports encoding optional fields with `None` (null) and `Some` (non-null values).</li>
 *   <li>Dynamically calculates the span (size) of the field based on its presence or absence.</li>
 *   <li>Abstract method {@code createField()} allows specific field types to be defined by subclasses.</li>
 *   <li>Handles both primitive and reference data types appropriately.</li>
 * </ul>
 *
 * <h2>Working Mechanism</h2>
 * <p>AbstractCOptionField utilizes a tagging system for serialization:</p>
 * <ul>
 *   <li>Tag `0`: Represents `None` (null value).</li>
 *   <li>Tag `1`: Represents `Some` (non-null value), followed by the field's serialized data.</li>
 * </ul>
 * <p>During deserialization, the tag is read to determine if the field has a value (`Some`) or is absent (`None`).</p>
 *
 * <h2>Core Methods</h2>
 * <ul>
 *   <li>{@code byte[] encode(T value)}: Serializes the value into a tagged byte array.</li>
 *   <li>{@code T decode(byte[] data, int offset)}: Decodes a tagged byte array back into the original value.</li>
 *   <li>{@code int calculateSpan(byte[] data, int offset)}: Calculates the size (span) of the field in the byte array.</li>
 *   <li>{@code abstract F createField()}: Abstract method to create field instances for specific data types.</li>
 * </ul>
 *
 * <h2>Usage Guide</h2>
 * <p>To use AbstractCOptionField, you need to create a subclass for each specific field type.
 * Implement the {@code createField()} method to return the appropriate field instance for encoding and decoding.</p>
 *
 * Example: Creating a Custom Field
 * <pre>{@code
 * public class UInt8OptionField extends AbstractCOptionField<Short, UInt8Field> {
 *     @Override
 *     protected UInt8Field createField() {
 *         return new UInt8Field();
 *     }
 * }
 * }</pre>
 *
 * Using AbstractCOptionField in a Struct
 * <pre>{@code
 * public class OptionalStruct {
 *     @StructField(order = 1, type = UInt8OptionField.class)
 *     private Short optionalInt16Value;
 *
 *     @StructField(order = 2, type = StringCOptionField.class)
 *     private String optionalMessage;
 * }
 *
 * // Example usage
 * OptionalStruct struct = new OptionalStruct();
 * struct.setOptionalInt16Value((short) 42);
 * struct.setOptionalMessage("Test Message");
 *
 * byte[] serialized = StructLayout.encode(struct);
 * OptionalStruct deserialized = StructLayout.decode(serialized, OptionalStruct.class);
 *
 * assertEquals(struct.getOptionalInt16Value(), deserialized.getOptionalInt16Value());
 * assertEquals(struct.getOptionalMessage(), deserialized.getOptionalMessage());
 * }</pre>
 *
 * Dynamic Span Calculation
 * <p>The {@code calculateSpan(byte[] data, int offset)} method dynamically determines the size of the field:
 * <ul>
 *   <li>For `None` (null): 1 byte (tag only).</li>
 *   <li>For `Some`: 1 byte for the tag + length of the serialized value.</li>
 * </ul>
 * </p>
 *
 * Encoding and Decoding
 * <p>AbstractCOptionField handles encoding and decoding of optional fields as follows:</p>
 * <pre>{@code
 * UInt8OptionField field = new UInt8OptionField();
 *
 * // Encode a value
 * byte[] encoded = field.encode((short) 120); // Tag 1 followed by the serialized value.
 *
 * // Decode the value
 * Short decoded = field.decode(encoded, 0);
 *
 * assertEquals(120, decoded);
 * }</pre>
 *
 * Handling Null Values
 * <ul>
 *   <li>If the value is {@code null}, {@code encode()} produces a tag of 0 (representing `None`).</li>
 *   <li>When decoding a tag of 0, {@code decode()} returns {@code null}.</li>
 *   <li>For primitive types, ensure default values (e.g., 0) are handled correctly where needed.</li>
 * </ul>
 *
 * <h2>Extended Example</h2>
 * <pre>{@code
 * public class Int32OptionField extends AbstractCOptionField<Integer, Int32LEField> {
 *     @Override
 *     protected Int32LEField createField() {
 *         return new Int32LEField();
 *     }
 * }
 *
 * public class CustomStruct {
 *     @StructField(order = 1, type = Int32OptionField.class)
 *     private Integer int32Value;
 *
 *     @StructField(order = 2, type = UInt8OptionField.class)
 *     private Short optionalInt16Value;
 *
 *     @StructField(order = 3, type = StringCOptionField.class)
 *     private String optionalMessage;
 * }
 *
 * CustomStruct struct = new CustomStruct();
 * struct.setInt32Value(123);
 * struct.setOptionalInt16Value(null);
 * struct.setOptionalMessage("Hello");
 *
 * byte[] encoded = StructLayout.encode(struct);
 * CustomStruct decoded = StructLayout.decode(encoded, CustomStruct.class);
 *
 * assertEquals(struct.getInt32Value(), decoded.getInt32Value());
 * assertNull(decoded.getOptionalInt16Value());
 * assertEquals(struct.getOptionalMessage(), decoded.getOptionalMessage());
 * }</pre>
 */
public abstract class AbstractBorshOptionField<T, F extends FieldBase<T>> extends FieldBase<T> implements DynamicSpanField {

    private final F fieldInstance;

    public AbstractBorshOptionField() {
        super(1); // tag is 1 byte
        this.fieldInstance = createField();
    }

    /**
     * Subclass must implement this to provide a concrete field instance.
     */
    protected abstract F createField();

    @Override
    public byte[] encode(T value) {
        if (value == null) {
            return new byte[]{0}; // Tag 0: None
        }

        byte[] inner = fieldInstance.encode(value);
        byte[] result = new byte[1 + inner.length];
        result[0] = 1; // Tag 1: Some
        System.arraycopy(inner, 0, result, 1, inner.length);
        return result;
    }

    @Override
    public T decode(byte[] data, int offset) {
        validateLength(data, offset);

        if (data.length <= offset) {
            throw new IllegalArgumentException("Insufficient data for Option tag at offset " + offset);
        }

        byte tag = data[offset];
        if (tag == 0) {
            return null; // None
        } else if (tag == 1) {
            return fieldInstance.decode(data, offset + 1);
        } else {
            throw new IllegalArgumentException("Invalid Option tag value: " + tag);
        }
    }

    @Override
    public int calculateSpan(byte[] data, int offset) {
        validateLength(data, offset);

        byte tag = data[offset];
        if (tag == 0) {
            return 1;
        } else if (tag == 1) {
            return 1 + fieldInstance.getSpan();
        } else {
            throw new IllegalArgumentException("Invalid Option tag value: " + tag);
        }
    }

    @Override
    public int getSpan() {
        throw new UnsupportedOperationException("Borsh Option has variable span.");
    }

    @Override
    public int getNoDataSpan() {
        return 1;
    }
}
package net.deanly.structlayout.annotation;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.type.CountableField;
import net.deanly.structlayout.type.basic.UInt8Field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a sequence field within a structured layout.
 * It targets fields that are part of a sequence with multiple elements
 * and optionally specifies the type of length prefix and element data types.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StructSequenceField {

    /**
     * Specifies the order of the field within a structured object or layout.
     * The order determines the sequence in which the field will be processed,
     * serialized, or deserialized in relation to other fields.
     *
     * @return the order of the field
     */
    int order();

    /**
     * Specifies the data type of the length prefix for a sequence field.
     * This determines how the length of the sequence is encoded or decoded
     * based on the provided {@link CountableField} implementation.
     *
     * @return the class type representing the length of the sequence,
     *         defaulting to {@code BasicTypes.UINT32_LE}.
     */
    Class<? extends CountableField<?>> lengthType() default UInt8Field.class;

    /**
     * Specifies the type of the elements in the sequence.
     * This is represented as a class extending the {@code Field<?>} base class,
     * which defines the behavior for encoding and decoding the elements.
     *
     * @return the class type of the sequence elements
     */
    Class<? extends Field<?>> elementType();

    /**
     * Defines how the field should be treated when the value is {@code null},
     * specifying the optional encoding strategy to apply during serialization and deserialization.
     *
     * <p>This supports multiple optional formats used across various binary serialization standards:</p>
     *
     * <ul>
     *   <li>{@link OptionalEncoding#NONE} (default):<br>
     *       The field is considered required and always serialized. If {@code null}, an error may be thrown.</li>
     *
     *   <li>{@link OptionalEncoding#BORSH}:<br>
     *       The field is encoded with a 1-byte prefix: {@code 0x00} for {@code None}, {@code 0x01} followed by the value for {@code Some}.<br>
     *       This format is commonly used in Solana's Borsh serialization.</li>
     *
     *   <li>{@link OptionalEncoding#OMIT_IF_NULL}:<br>
     *       The field is completely omitted from the serialized output if the value is {@code null}.<br>
     *       This is typical in formats like Protobuf and FlatBuffers.</li>
     *
     *   <li>{@link OptionalEncoding#NULL_LITERAL}:<br>
     *       A literal null value (e.g., {@code 0xC0} for nil) is written to the output to indicate {@code null}.<br>
     *       This approach is used in formats like MessagePack or CBOR.</li>
     * </ul>
     *
     * <p>This option allows fine-grained control over the serialization behavior of nullable fields, especially when
     * supporting multiple formats or interoperating with blockchain or external systems.</p>
     *
     * @return the optional encoding strategy to use; defaults to {@code NONE} (not optional)
     */
    OptionalEncoding optional() default OptionalEncoding.NONE;

}
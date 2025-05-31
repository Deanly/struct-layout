package net.deanly.structlayout.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as a Struct object field for encoding/decoding.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StructObjectField {
    /**
     * Specifies the order of a field within a structured object.
     * The order determines the sequence in which the field will be
     * processed during serialization or deserialization.
     *
     * @return the order of the field
     */
    int order(); // Field order in the struct

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
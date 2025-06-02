package net.deanly.structlayout.annotation;

import net.deanly.structlayout.type.CountableField;
import net.deanly.structlayout.type.basic.UInt8Field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StructSequenceObjectField {

    int order();

    Class<? extends CountableField<?>> lengthType() default UInt8Field.class;

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
     * </ul>
     *
     * <p>This option allows fine-grained control over the serialization behavior of nullable fields, especially when
     * supporting multiple formats or interoperating with blockchain or external systems.</p>
     *
     * @return the optional encoding strategy to use; defaults to {@code NONE} (not optional)
     */
    OptionalEncoding optional() default OptionalEncoding.NONE;

}

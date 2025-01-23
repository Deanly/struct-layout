package net.deanly.structlayout.annotation;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.type.basic.CountableType;
import net.deanly.structlayout.type.basic.UInt32LEField;

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
     * based on the provided {@link CountableType} implementation.
     *
     * @return the class type representing the length of the sequence,
     *         defaulting to {@code BasicTypes.UINT32_LE}.
     */
    Class<? extends CountableType> lengthType() default UInt32LEField.class;

    /**
     * Specifies the type of the elements in the sequence.
     * This is represented as a class extending the {@code Field<?>} base class,
     * which defines the behavior for encoding and decoding the elements.
     *
     * @return the class type of the sequence elements
     */
    Class<? extends Field<?>> elementType();

}
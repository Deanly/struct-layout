package net.deanly.structlayout.annotation;

import net.deanly.structlayout.type.DataType;

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
public @interface SequenceField {

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
     * This determines how the length of the sequence is represented, utilizing
     * a specific data type from the {@link DataType} enum.
     *
     * @return the data type used to represent the length of the sequence,
     *         with the default being {@code DataType.UINT32_LE}.
     */
    DataType lengthType() default DataType.UINT32_LE; // Default length type (nullable)

    /**
     * Specifies the data type of the elements in the sequence.
     *
     * @return the data type of elements, as defined by the {@link DataType} enum
     */
    DataType elementType(); // Type of the elements in the sequence

}
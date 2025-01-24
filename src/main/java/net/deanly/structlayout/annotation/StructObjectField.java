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
}
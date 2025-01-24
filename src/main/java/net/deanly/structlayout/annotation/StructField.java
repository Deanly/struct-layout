package net.deanly.structlayout.annotation;

import net.deanly.structlayout.Field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define custom Layout class for a structured field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StructField {

    /**
     * Specifies the order of a field within a structured layout or object.
     * This value determines the sequential position of the field during
     * serialization or deserialization processes.
     *
     * @return the order of the field
     */
    int order();

    /**
     * The custom Layout class to use for the field.
     */
    Class<? extends Field<?>> type();
}
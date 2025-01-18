package net.deanly.structlayout.annotation;

import net.deanly.structlayout.Layout;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define custom Layout class for a structured field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CustomLayoutField {
    int order();

    /**
     * The custom Layout class to use for the field.
     */
    Class<? extends Layout<?>> layout();
}
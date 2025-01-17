package net.deanly.structlayout.annotation;

import net.deanly.structlayout.type.DataType;
import net.deanly.structlayout.type.Endianness;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a field as part of a structured layout.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StructField {
    int order();
    DataType dataType();
}
package net.deanly.structlayout.annotation;

import net.deanly.structlayout.type.DataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SequenceField {
    int order();           // Field's order in the structure

    /**
     * The data type of the length prefix, indicating the number of elements in the sequence.
     * If null, no length prefix is used.
     */
    DataType lengthType() default DataType.UINT32_LE; // Default length type (nullable)

    /**
     * Specifies the sequence element type using the Type enum.
     */
    DataType elementType(); // Type of the elements in the sequence

}
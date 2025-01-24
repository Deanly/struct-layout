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

}

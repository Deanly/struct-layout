package net.deanly.structlayout.annotation;

import net.deanly.structlayout.dispatcher.StructTypeDispatcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StructTypeSelector {
    Class<? extends StructTypeDispatcher> dispatcher();
}

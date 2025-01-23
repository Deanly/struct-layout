package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.analysis.CachedLayoutProvider;

public abstract class BaseFieldHandler {

    public abstract <T> int handleField(T instance, java.lang.reflect.Field field, byte[] data, int offest) throws IllegalAccessException;

    protected Field<Object> resolveLayout(Class<? extends Field<?>> fieldType) {
        return CachedLayoutProvider.getLayout(fieldType);
    }
}
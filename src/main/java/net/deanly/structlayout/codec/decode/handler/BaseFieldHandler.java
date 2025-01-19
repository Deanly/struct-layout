package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.analysis.CachedLayoutProvider;
import net.deanly.structlayout.type.DataType;

import java.lang.reflect.Field;

public abstract class BaseFieldHandler {

    public abstract <T> int handleField(T instance, Field field, byte[] data, int offest) throws IllegalAccessException;

    protected Layout<Object> resolveLayout(DataType dataType) {
        return CachedLayoutProvider.getLayout(dataType);
    }
}
package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.analysis.CachedLayoutProvider;
import net.deanly.structlayout.annotation.OptionalEncoding;

public abstract class BaseFieldHandler {

    public abstract <T> int handleField(T instance, java.lang.reflect.Field field, byte[] data, int offest) throws IllegalAccessException;

    protected Field<Object> resolveLayout(Class<? extends Field<?>> fieldType) {
        return CachedLayoutProvider.getLayout(fieldType);
    }

    protected OptionalEncoding resolveOptionalEncoding(java.lang.reflect.Field field) {
        if (field.isAnnotationPresent(net.deanly.structlayout.annotation.StructField.class)) {
            return field.getAnnotation(net.deanly.structlayout.annotation.StructField.class).optional();
        }
        if (field.isAnnotationPresent(net.deanly.structlayout.annotation.StructObjectField.class)) {
            return field.getAnnotation(net.deanly.structlayout.annotation.StructObjectField.class).optional();
        }
        if (field.isAnnotationPresent(net.deanly.structlayout.annotation.StructSequenceField.class)) {
            return field.getAnnotation(net.deanly.structlayout.annotation.StructSequenceField.class).optional();
        }
        if (field.isAnnotationPresent(net.deanly.structlayout.annotation.StructSequenceObjectField.class)) {
            return field.getAnnotation(net.deanly.structlayout.annotation.StructSequenceObjectField.class).optional();
        }
        return OptionalEncoding.NONE;
    }

    protected boolean isValuePresent(byte[] data, int offset, OptionalEncoding encoding) {
        return switch (encoding) {
            case BORSH -> data[offset] == 1;
//            case OMIT_IF_NULL, NULL_LITERAL ->
//                    throw new UnsupportedOperationException("This optional encoding is not yet supported in decoding.");
            default -> true;
        };
    }
}
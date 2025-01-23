package net.deanly.structlayout.codec.decode;

import net.deanly.structlayout.annotation.*;
import net.deanly.structlayout.codec.decode.handler.*;
import net.deanly.structlayout.exception.FieldAccessException;
import net.deanly.structlayout.exception.StructParsingException;
import net.deanly.structlayout.exception.TypeConversionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class FieldProcessor {

    private static final Map<Class<? extends Annotation>, BaseFieldHandler> HANDLERS = new HashMap<>();

    static {
        HANDLERS.put(StructSequenceField.class, new SequenceFieldHandler());
        HANDLERS.put(StructObjectField.class, new StructObjectFieldHandler());
        HANDLERS.put(StructField.class, new StructFieldHandler());
    }

    public static <T> int processField(T instance, Field field, byte[] data, int offset) {
        for (Map.Entry<Class<? extends Annotation>, BaseFieldHandler> entry : HANDLERS.entrySet()) {
            if (field.isAnnotationPresent(entry.getKey())) {
                try {
                    return entry.getValue().handleField(instance, field, data, offset);
                } catch (IllegalAccessException e) {
                    throw new FieldAccessException(field.getName(), field.getClass().getSimpleName(), e);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Failed to process field: `" + field.getName() + "` => " + e.getMessage(), e);
                } catch (TypeConversionException e) {
                    throw new TypeConversionException("Failed to process field: `" + field.getName() + "` => " + e.getMessage(), e);
                } catch (StructParsingException e) {
                    throw e;
                } catch (RuntimeException e) {
                    throw new StructParsingException("Failed to process field: `" + field.getName() + "` => " + e.getMessage(), e);
                }
            }
        }
        throw new IllegalArgumentException("No handler found for field: `" + field.getName() + "`");
    }
}
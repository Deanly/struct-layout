package net.deanly.structlayout.codec.decode;

import net.deanly.structlayout.annotation.*;
import net.deanly.structlayout.codec.decode.handler.*;
import net.deanly.structlayout.exception.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class FieldProcessor {

    private static final Map<Class<? extends Annotation>, BaseFieldHandler> HANDLERS = new HashMap<>();

    static {
        HANDLERS.put(StructField.class, new StructFieldHandler());
        HANDLERS.put(StructSequenceField.class, new StructSequenceFieldHandler());
        HANDLERS.put(StructObjectField.class, new StructObjectFieldHandler());
        HANDLERS.put(StructSequenceObjectField.class, new StructSequenceObjectFieldHandler());
    }

    public static <T> int processField(T instance, Field field, byte[] data, int offset) {
        for (Map.Entry<Class<? extends Annotation>, BaseFieldHandler> entry : HANDLERS.entrySet()) {
            if (field.isAnnotationPresent(entry.getKey())) {
                try {
                    return entry.getValue().handleField(instance, field, data, offset);
                } catch (IllegalAccessException e) {
                    throw new FieldAccessException(field.getName(), field.getClass().getSimpleName(), e);
                } catch (FieldOrderException e) {
                    throw new FieldOrderException("Failed to process field: `" + field.getName() + "` => " + e.getMessage(), e);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Failed to process field: `" + field.getName() + "` => " + e.getMessage(), e);
                } catch (TypeConversionException e) {
                    throw new TypeConversionException("Failed to process field: `" + field.getName() + "` => " + e.getMessage(), e);
                } catch (InvalidDataOffsetException e) {
                    throw new InvalidDataOffsetException("Failed to process field: `" + field.getName() + "` => " + e.getMessage(), e);
                } catch (InvalidSequenceTypeException e) {
                    throw new InvalidSequenceTypeException("Failed to process field: `" + field.getName() + "` => " + e.getMessage(), e);
                } catch (LayoutInitializationException e) {
                    throw new LayoutInitializationException("Failed to process field: `" + field.getName() + "` => " + e.getMessage(), e);
                } catch (NoDefaultConstructorException e) {
                    throw new NoDefaultConstructorException("Failed to process field: `" + field.getName() + "` => " + e.getMessage(), e);
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
package net.deanly.structlayout.codec.encode;

import net.deanly.structlayout.analysis.FieldDebugInfo;
import net.deanly.structlayout.annotation.*;
import net.deanly.structlayout.codec.encode.handler.*;
import net.deanly.structlayout.codec.helpers.FieldHelper;
import net.deanly.structlayout.dispatcher.StructTypeResolver;
import net.deanly.structlayout.exception.FieldAccessException;
import net.deanly.structlayout.exception.StructParsingException;
import net.deanly.structlayout.exception.TypeConversionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class FieldProcessor {

    private static final Map<Class<? extends Annotation>, BaseFieldHandler> HANDLERS = new HashMap<>();

    static {
        HANDLERS.put(StructField.class, new StructFieldHandler());
        HANDLERS.put(StructSequenceField.class, new StructSequenceFieldHandler());
        HANDLERS.put(StructObjectField.class, new StructObjectFieldHandler());
        HANDLERS.put(StructSequenceObjectField.class, new StructSequenceObjectFieldHandler());
    }

    public static <T> byte[] processField(T instance, Field field) {
        for (Map.Entry<Class<? extends Annotation>, BaseFieldHandler> entry : HANDLERS.entrySet()) {
            if (field.isAnnotationPresent(entry.getKey())) {
                try {
                    return entry.getValue().handleField(instance, field);
                } catch (IllegalAccessException e) {
                    throw new FieldAccessException(field.getName(), field.getClass().getSimpleName(), e);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Failed to process field: `" + field.getName() + "` => " + e.getMessage(), e);
                } catch (TypeConversionException e) {
                    throw new TypeConversionException("Failed to process field: `" + field.getName() + "` => " + e.getMessage(), e);
                } catch (StructParsingException e) {
                    throw e;
                } catch (RuntimeException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                    throw new StructParsingException("Failed to process field: `" + field.getName() + "` => " + e.getMessage(), e);
                }
            }
        }
        throw new IllegalArgumentException("No handler found for field: `" + field.getName() + "`");
    }

    public static <T> List<FieldDebugInfo> processFieldRecursivelyWithDebug(T instance, Field field, String parentOrder) {
        List<FieldDebugInfo> debugInfos = new ArrayList<>();

        for (Map.Entry<Class<? extends Annotation>, BaseFieldHandler> entry : HANDLERS.entrySet()) {
            if (field.isAnnotationPresent(entry.getKey())) {
                try {
                    String order = parentOrder == null ? String.valueOf(FieldHelper.getOrderValue(field)) : parentOrder + "-" + FieldHelper.getOrderValue(field);

                    if (field.isAnnotationPresent(StructObjectField.class)) {
                        Object childInstance = field.get(instance);

                        Class<?> fieldType = field.getType();
                        if ((fieldType.isInterface() || java.lang.reflect.Modifier.isAbstract(fieldType.getModifiers()))
                                && childInstance != null) {
                            fieldType = childInstance.getClass();
                        }

                        if (childInstance != null) {
                            Field[] childFields = fieldType.getDeclaredFields();
                            for (Field childField : childFields) {
                                childField.setAccessible(true);
                                debugInfos.addAll(processFieldRecursivelyWithDebug(childInstance, childField, order));
                            }
                        }

                    }
                    else if (field.isAnnotationPresent(StructSequenceObjectField.class)) {
                        List<FieldDebugInfo.Builder> builders = entry.getValue().handleDebug(instance, field);

                        for (FieldDebugInfo.Builder builder : builders) {
                            builder.order(order);
                            debugInfos.add(builder.build());
                        }
                    }
                    else if (field.isAnnotationPresent(StructSequenceField.class)) {
                        List<FieldDebugInfo.Builder> builders = entry.getValue().handleDebug(instance, field);
                        for (FieldDebugInfo.Builder builder : builders) {
                            builder.order(order);
                            debugInfos.add(builder.build());
                        }
                    }
                    else if (field.isAnnotationPresent(StructField.class))  {
                        FieldDebugInfo.Builder builder = entry.getValue().handleDebug(instance, field).get(0);
                        builder.order(order);
                        debugInfos.add(builder.build());
                    }
                    return debugInfos;
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to debug field: `" + field.getName() + "` -> " + e.getMessage(), e
                    );
                }
            }
        }

        return debugInfos;
    }

}
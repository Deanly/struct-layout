package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.analysis.CachedLayoutProvider;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.codec.decode.StructDecoder;
import net.deanly.structlayout.codec.helpers.FieldHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class StructObjectFieldHandler extends BaseFieldHandler {

    @Override
    public <T> int handleField(T instance, java.lang.reflect.Field field, byte[] data, int offset) throws IllegalAccessException {
        StructObjectField annotation = field.getAnnotation(StructObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @StructObjectField", field.getName())
            );
        }

        Class<?> nestedType = field.getType();

        Object nestedInstance = StructDecoder.decode(nestedType, data, offset).getValue();

        field.setAccessible(true);
        field.set(instance, nestedInstance);

        int objectSize = calculateEncodedObjectSize(nestedType, nestedInstance);
        return objectSize;
    }

    /**
     * 중첩된 객체의 크기를 계산
     */
    private static int calculateObjectSizeFromFields(Class<?> type, Object instance) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        java.lang.reflect.Field[] fields = type.getDeclaredFields();
        List<java.lang.reflect.Field> orderedFields = FieldHelper.getOrderedFields(fields);
        int size = 0;

        for (java.lang.reflect.Field field : orderedFields) {
            field.setAccessible(true);

             if (field.isAnnotationPresent(StructSequenceField.class)) {
                StructSequenceField structSequenceField = field.getAnnotation(StructSequenceField.class);
                @SuppressWarnings("unchecked")
                Field<?> lengthField = CachedLayoutProvider.getLayout((Class<? extends Field<?>>) structSequenceField.lengthType());
                Field<?> elementField = CachedLayoutProvider.getLayout(structSequenceField.elementType());
                int length = (Integer) lengthField.decode((byte[]) field.get(instance), 0); // For length
                size += lengthField.getSpan(); // Length field itself
                size += elementField.getSpan() * length; // Elements
            } else if (field.isAnnotationPresent(StructObjectField.class)) {
                StructObjectField structObjectField = field.getAnnotation(StructObjectField.class);
                Class<?> nestedType = field.getType();
                Object nestedInstance = field.get(instance);
                size += calculateObjectSizeFromFields(nestedType, nestedInstance); // Nested object's size
            } else if (field.isAnnotationPresent(StructField.class)) {
                StructField structField = field.getAnnotation(StructField.class);
                Field<?> layout = structField.type().getDeclaredConstructor().newInstance();
                size += layout.getSpan(); // Custom layout's size
            }
        }

        return size;

    }

    /**
     * 중첩된 객체의 크기를 계산
     */
    private static int calculateEncodedObjectSize(Class<?> type, Object instance) {
        // StructLayout.encode 메소드를 활용하여 크기를 계산
        byte[] encodedData = StructLayout.encode(instance);
        return encodedData.length;
    }
}
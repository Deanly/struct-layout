package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.analysis.CachedLayoutProvider;
import net.deanly.structlayout.annotation.CustomLayoutField;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.codec.decode.StructDecoder;
import net.deanly.structlayout.codec.helpers.FieldHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class StructObjectFieldHandler extends BaseFieldHandler {

    @Override
    public <T> int handleField(T instance, Field field, byte[] data, int offset) throws IllegalAccessException {
        StructObjectField annotation = field.getAnnotation(StructObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @StructObjectField", field.getName())
            );
        }

        Class<?> nestedType = field.getType();

        Object nestedInstance = StructDecoder.decode(nestedType, data, offset);

        field.setAccessible(true);
        field.set(instance, nestedInstance);

        int objectSize = calculateEncodedObjectSize(nestedType, nestedInstance);
        return objectSize;
    }

    /**
     * 중첩된 객체의 크기를 계산
     */
    private static int calculateObjectSizeFromFields(Class<?> type, Object instance) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Field[] fields = type.getDeclaredFields();
        List<Field> orderedFields = FieldHelper.getOrderedFields(fields);
        int size = 0;

        for (Field field : orderedFields) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(StructField.class)) {
                // TODO(dean): 일부 DataTypeMapping 에 캐싱되지 않는 동적 타입의 처리가 필요함.
                StructField structField = field.getAnnotation(StructField.class);
                Layout<?> layout = CachedLayoutProvider.getLayout(structField.dataType());
                size += layout.getSpan();
            } else if (field.isAnnotationPresent(SequenceField.class)) {
                SequenceField sequenceField = field.getAnnotation(SequenceField.class);
                Layout<?> lengthLayout = CachedLayoutProvider.getLayout(sequenceField.lengthType());
                Layout<?> elementLayout = CachedLayoutProvider.getLayout(sequenceField.elementType());
                int length = (Integer) lengthLayout.decode((byte[]) field.get(instance), 0); // For length
                size += lengthLayout.getSpan(); // Length field itself
                size += elementLayout.getSpan() * length; // Elements
            } else if (field.isAnnotationPresent(StructObjectField.class)) {
                StructObjectField structObjectField = field.getAnnotation(StructObjectField.class);
                Class<?> nestedType = field.getType();
                Object nestedInstance = field.get(instance);
                size += calculateObjectSizeFromFields(nestedType, nestedInstance); // Nested object's size
            } else if (field.isAnnotationPresent(CustomLayoutField.class)) {
                CustomLayoutField customLayoutField = field.getAnnotation(CustomLayoutField.class);
                Layout<?> layout = customLayoutField.layout().getDeclaredConstructor().newInstance();
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
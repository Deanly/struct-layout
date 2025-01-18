package net.deanly.structlayout.codec;

import net.deanly.structlayout.annotation.CustomLayoutField;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.analysis.CachedLayoutProvider;
import net.deanly.structlayout.Layout;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.codec.helpers.ByteArrayHelper;
import net.deanly.structlayout.codec.helpers.FieldHelper;
import net.deanly.structlayout.codec.helpers.NumberConversionHelper;
import net.deanly.structlayout.exception.FieldAccessException;
import net.deanly.structlayout.exception.InvalidSequenceTypeException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class StructEncoder {

    public static <T> byte[] encode(T instance) {
        if (instance == null) {
            return new byte[0]; // Null 객체는 빈 바이트 배열로 처리
        }

        List<byte[]> fieldChunks = new ArrayList<>();
        Field[] fields = instance.getClass().getDeclaredFields();
        List<Field> orderedFields = FieldHelper.getOrderedFields(fields);

        // 정렬된 필드를 처리
        for (Field field : orderedFields) {
            field.setAccessible(true);

            try {
                if (field.isAnnotationPresent(StructField.class)) {
                    StructField structField = field.getAnnotation(StructField.class);
                    byte[] chunk = encodeStructField(instance, field, structField);
                    fieldChunks.add(chunk);
                } else if (field.isAnnotationPresent(SequenceField.class)) {
                    SequenceField sequenceField = field.getAnnotation(SequenceField.class);
                    byte[] chunk = encodeSequenceField(instance, field, sequenceField);
                    fieldChunks.add(chunk);
                } else if (field.isAnnotationPresent(StructObjectField.class)) {
                    StructObjectField structObjectField = field.getAnnotation(StructObjectField.class);
                    byte[] chunk = encodeStructObjectField(instance, field, structObjectField);
                    fieldChunks.add(chunk);
                } else if (field.isAnnotationPresent(CustomLayoutField.class)) {
                    CustomLayoutField customLayoutField = field.getAnnotation(CustomLayoutField.class);
                    byte[] chunk = encodeCustomLayoutField(instance, field, customLayoutField);
                    fieldChunks.add(chunk);
                }
            } catch (IllegalAccessException e) {
                throw new FieldAccessException(field.getName(), instance.getClass().getName(), e);
            }
        }

        // Merge all byte chunks into one
        return ByteArrayHelper.mergeChunks(fieldChunks);
    }

    private static <T> byte[] encodeStructField(T instance, Field field, StructField annotation) throws IllegalAccessException {
        Object value = field.get(instance);

        Layout<Object> layout = CachedLayoutProvider.getLayout(annotation.dataType());

        byte[] bytes = layout.encode(value);
        layout.printDebug(bytes, 0, field);

        return bytes;
    }

    @SuppressWarnings("unchecked")
    private static <T> byte[] encodeSequenceField(T instance, Field field, SequenceField annotation) throws IllegalAccessException {
        Object sequence = field.get(instance);
        if (sequence == null) {
            return new byte[]{0};
        }

        int length;
        List<T> elements;

        if (sequence instanceof List<?>) {
            elements = (List<T>) sequence;
            length = elements.size();
        } else if (sequence.getClass().isArray()) {
            length = java.lang.reflect.Array.getLength(sequence);
            elements = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                elements.add((T) java.lang.reflect.Array.get(sequence, i));
            }
        } else {
            throw new InvalidSequenceTypeException(field.getName(), sequence.getClass());
        }

        // 직렬화
        Layout<Object> lengthLayout = CachedLayoutProvider.getLayout(annotation.lengthType());
        Layout<Object> elementLayout = CachedLayoutProvider.getLayout(annotation.elementType());

        // 타입 변환 추가
        Class<?> expectedLengthType = annotation.lengthType().getFieldType(); // 예상되는 타입을 조회 (Layout과 연동)
        Object lengthValue = NumberConversionHelper.convertToExpectedNumericType(length, expectedLengthType, field.getName());

        // Encode length
        byte[] lengthHeader = lengthLayout.encode(lengthValue); // 변환된 값 사용
        lengthLayout.printDebug(lengthHeader, 0, field);

        // Encode elements
        List<byte[]> elementChunks = new ArrayList<>();
        for (Object element : elements) {
            byte[] elementBytes = elementLayout.encode(element);
            elementChunks.add(elementBytes);
        }

        return ByteArrayHelper.mergeChunks(lengthHeader, ByteArrayHelper.mergeChunks(elementChunks));
    }

    private static <T> byte[] encodeStructObjectField(T instance, Field field, StructObjectField annotation) throws IllegalAccessException {
        Object value = field.get(instance);
        return StructEncoder.encode(value);
    }

    private static <T> byte[] encodeCustomLayoutField(T instance, Field field, CustomLayoutField annotation) throws IllegalAccessException {
        Object value = field.get(instance);
        Class<? extends Layout<?>> layoutClazz = annotation.layout();
        Layout<Object> layout = CachedLayoutProvider.getLayout(layoutClazz);

        byte[] bytes = layout.encode(value);
        layout.printDebug(bytes, 0, field);

        return bytes;
    }

}
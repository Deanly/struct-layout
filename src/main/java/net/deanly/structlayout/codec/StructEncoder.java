package net.deanly.structlayout.codec;

import net.deanly.structlayout.annotation.CustomLayoutField;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.analysis.DataTypeMapping;
import net.deanly.structlayout.Layout;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.exception.FieldAccessException;
import net.deanly.structlayout.exception.InvalidSequenceTypeException;
import net.deanly.structlayout.exception.StructParsingException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StructEncoder {

    public static <T> byte[] encode(T instance) {
        if (instance == null) {
            return new byte[0]; // Null 객체는 빈 바이트 배열로 처리
        }

        List<byte[]> fieldChunks = new ArrayList<>();
        Field[] fields = instance.getClass().getDeclaredFields();
        List<Field> orderedFields = getOrderedFields(fields);

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
        return mergeChunks(fieldChunks);
    }

    /**
     * 필드를 `OrderedField` 기준으로 정렬 후 반환
     */
    private static List<Field> getOrderedFields(Field[] fields) {
        List<Field> orderedFields = new ArrayList<>();

        for (Field field : fields) {
            // 지원되는 어노테이션 확인
            if (field.isAnnotationPresent(StructField.class) ||
                    field.isAnnotationPresent(SequenceField.class) ||
                    field.isAnnotationPresent(StructObjectField.class) ||
                    field.isAnnotationPresent(CustomLayoutField.class)) {
                orderedFields.add(field);
            }
        }

        // 각 필드의 order 값을 기준으로 정렬
        orderedFields.sort(Comparator.comparingInt(StructEncoder::getOrderValue));

        return orderedFields;
    }

    /**
     * 주어진 필드의 order 값을 반환
     */
    private static int getOrderValue(Field field) {
        if (field.isAnnotationPresent(StructField.class)) {
            return field.getAnnotation(StructField.class).order();
        } else if (field.isAnnotationPresent(SequenceField.class)) {
            return field.getAnnotation(SequenceField.class).order();
        } else if (field.isAnnotationPresent(StructObjectField.class)) {
            return field.getAnnotation(StructObjectField.class).order();
        } else if (field.isAnnotationPresent(CustomLayoutField.class)) {
            return field.getAnnotation(CustomLayoutField.class).order();
        }
        throw new IllegalArgumentException("Field [" + field.getName() + "] does not have any supported order field.");
    }

    private static <T> byte[] encodeStructField(T instance, Field field, StructField annotation) throws IllegalAccessException {
        Object value = field.get(instance);

        Layout<Object> layout = DataTypeMapping.getLayout(annotation.dataType());

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

        try {
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
        } catch (Exception e) {
            throw new StructParsingException("Error while processing sequence field '" + field.getName() + "'.", e);
        }

        // 직렬화
        Layout<Object> lengthLayout = DataTypeMapping.getLayout(annotation.lengthType());
        Layout<Object> elementLayout = DataTypeMapping.getLayout(annotation.elementType());

        // 타입 변환 추가
        Class<?> expectedLengthType = annotation.lengthType().getFieldType(); // 예상되는 타입을 조회 (Layout과 연동)
        Object lengthValue = convertValueToExpectedType(length, expectedLengthType, field.getName());

        // Encode length
        byte[] lengthHeader = lengthLayout.encode(lengthValue); // 변환된 값 사용
        lengthLayout.printDebug(lengthHeader, 0, field);

        // Encode elements
        List<byte[]> elementChunks = new ArrayList<>();
        for (Object element : elements) {
            byte[] elementBytes = elementLayout.encode(element);
            elementChunks.add(elementBytes);
        }

        return mergeChunks(lengthHeader, mergeChunks(elementChunks));
    }

    private static Object convertValueToExpectedType(Object value, Class<?> expectedType, String fieldName) {
        if (expectedType == null) {
            throw new IllegalArgumentException("Expected type cannot be null for field: " + fieldName);
        }

        // 이미 예상 타입과 일치하는 경우 그대로 반환
        if (expectedType.isInstance(value)) {
            return value;
        }

        // int → Long 변환
        if (value instanceof Integer && expectedType == Long.class) {
            return ((Integer) value).longValue();
        }

        // int/long → BigInteger 변환
        if ((value instanceof Integer || value instanceof Long) && expectedType == java.math.BigInteger.class) {
            return java.math.BigInteger.valueOf(((Number) value).longValue());
        }

        // 다른 타입 변환을 처리하지 못한 경우
        throw new IllegalArgumentException(
                String.format("Field '%s' cannot be converted from %s to %s.", fieldName, value.getClass().getName(), expectedType.getName())
        );
    }

    private static <T> byte[] encodeStructObjectField(T instance, Field field, StructObjectField annotation) throws IllegalAccessException {
        Object value = field.get(instance);
        return StructEncoder.encode(value);
    }

    private static <T> byte[] encodeCustomLayoutField(T instance, Field field, CustomLayoutField annotation) throws IllegalAccessException {
        Object value = field.get(instance);
        Class<? extends Layout<?>> layoutClazz = annotation.layout();
        Layout<Object> layout = DataTypeMapping.getLayout(layoutClazz);

        byte[] bytes = layout.encode(value);
        layout.printDebug(bytes, 0, field);

        return bytes;
    }

    private static byte[] mergeChunks(List<byte[]> chunks) {
        int totalLength = chunks.stream().mapToInt(chunk -> chunk.length).sum();
        byte[] merged = new byte[totalLength];

        int offset = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, merged, offset, chunk.length);
            offset += chunk.length;
        }

        return merged;
    }

    private static byte[] mergeChunks(byte[]... chunks) {
        int totalLength = 0;
        for (byte[] chunk : chunks) {
            totalLength += chunk.length;
        }

        byte[] merged = new byte[totalLength];
        int offset = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, merged, offset, chunk.length);
            offset += chunk.length;
        }

        return merged;
    }
}
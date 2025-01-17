package net.deanly.structlayout.codec;

import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.analysis.DataTypeMapping;
import net.deanly.structlayout.Layout;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.type.DataType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StructEncoder {

    public static <T> byte[] encode(T instance) {
        List<byte[]> fieldChunks = new ArrayList<>();

        // 필드를 "order" 기준으로 정렬
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
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field [" + field.getName() + "] for encoding.", e);
            }
        }

        // Merge all chunks into a single byte array
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
                    field.isAnnotationPresent(StructObjectField.class)) {
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
        }
        throw new IllegalArgumentException("Field [" + field.getName() + "] does not have any supported order field.");
    }

    @SuppressWarnings("unchecked")
    private static <T> byte[] encodeStructField(T instance, Field field, StructField annotation) throws IllegalAccessException {
        Object value = field.get(instance);

        // Retrieve the appropriate Layout for this data type & endianness
        Layout<Object> layout = DataTypeMapping.getLayout(annotation.dataType());

        // Encode the single value into a byte array
        return layout.encode(value);
    }

    @SuppressWarnings("unchecked")
    private static <T> byte[] encodeSequenceField(T instance, Field field, SequenceField annotation) throws IllegalAccessException {
        Object[] values = (Object[]) field.get(instance);

        // Retrieve the appropriate Layout for the sequence element type
        Layout<Object> layout = DataTypeMapping.getLayout(annotation.elementType());

        // Encode sequence elements into individual chunks
        List<byte[]> chunks = new ArrayList<>();
        for (Object value : values) {
            chunks.add(layout.encode(value));
        }

        // Add length of the array at the beginning
        byte[] lengthHeader = new byte[]{(byte) values.length};

        // Merge length header and all encoded elements
        return mergeChunks(lengthHeader, mergeChunks(chunks));
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
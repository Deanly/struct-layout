package net.deanly.structlayout.codec;

import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.analysis.DataTypeMapping;
import net.deanly.structlayout.Layout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class StructDecoder {

    public static <T> T decode(Class<T> type, byte[] data, int startOffset) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();

            // 필드를 "order" 기준으로 정렬
            Field[] fields = type.getDeclaredFields();
            List<Field> orderedFields = getOrderedFields(fields);

            int offset = startOffset; // 데이터의 현재 위치를 추적
            for (Field field : orderedFields) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(StructField.class)) {
                    StructField structField = field.getAnnotation(StructField.class);
                    offset = decodeStructField(instance, field, structField, data, offset);
                } else if (field.isAnnotationPresent(SequenceField.class)) {
                    SequenceField sequenceField = field.getAnnotation(SequenceField.class);
                    offset = decodeSequenceField(instance, field, sequenceField, data, offset);
                } else if (field.isAnnotationPresent(StructObjectField.class)) {
                    StructObjectField structObjectField = field.getAnnotation(StructObjectField.class);
                    offset = decodeStructObjectField(instance, field, structObjectField, data, offset);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode structure: " + type.getName(), e);
        }
    }

    /**
     * 필드를 `order` 기준으로 정렬 후 반환
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
        orderedFields.sort(Comparator.comparingInt(field -> getOrderValue(field)));

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

    /**
     * StructField를 디코딩
     */
    private static <T> int decodeStructField(T instance, Field field, StructField annotation, byte[] data, int offset) {
        try {
            Layout<Object> layout = DataTypeMapping.getLayout(annotation.dataType());
            Object value = layout.decode(data, offset);
            // 현재 필드 값을 설정
            field.set(instance, value);

            // 필드의 길이만큼 오프셋 이동
            return offset + layout.getSpan();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to decode field: " + field.getName(), e);
        }
    }

    /**
     * SequenceField를 디코딩
     */
    private static <T> int decodeSequenceField(T instance, Field field, SequenceField annotation, byte[] data, int offset) {
        try {
            Layout<Object> elementLayout = DataTypeMapping.getLayout(annotation.elementType());
            Layout<Object> lengthLayout = DataTypeMapping.getLayout(annotation.lengthType());

            // 길이 정보 읽기
            int length = (Integer) Objects.requireNonNull(lengthLayout.decode(data, offset));
            offset += lengthLayout.getSpan(); // 길이 필드 크기만큼 이동

            List<Object> values = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                Object element = elementLayout.decode(data, offset);
                values.add(element);
                offset += elementLayout.getSpan(); // 요소 크기만큼 이동
            }

            field.set(instance, values.toArray());

            return offset;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to decode sequence field: " + field.getName(), e);
        }
    }

    /**
     * StructObjectField를 디코딩 (중첩 구조체)
     */
    private static <T> int decodeStructObjectField(T instance, Field field, StructObjectField annotation, byte[] data, int offset) {
        try {
            Class<?> nestedType = field.getType();
            Object nestedInstance = decode(nestedType, data, offset);

            field.set(instance, nestedInstance);

            // 중첩된 객체의 끝 오프셋 계산
            return offset + calculateObjectSize(nestedType, nestedInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to decode nested struct field: " + field.getName(), e);
        }
    }

    /**
     * 중첩된 객체의 크기를 계산
     */
    private static int calculateObjectSize(Class<?> type, Object instance) {
        // TODO: 중첩된 구조체의 필드 크기 계산. 이를 위해 Layout 등을 활용. 복잡한 계산이 필요할 수 있으므로 사용자 정의 로직 필요.
        return 0; // 일단 더미 값
    }
}
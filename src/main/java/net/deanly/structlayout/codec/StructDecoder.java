package net.deanly.structlayout.codec;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        orderedFields.sort(Comparator.comparingInt(StructDecoder::getOrderValue));

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

            layout.printDebug(data, offset, field);

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

            log.debug("[Decode SequenceField] data.length={}, offset={}", data.length, offset);

            // 길이 정보 읽기
            int length = (Integer) Objects.requireNonNull(lengthLayout.decode(data, offset));
            lengthLayout.printDebug(data, offset, field);
            offset += lengthLayout.getSpan(); // 길이 필드 크기만큼 이동

            // 값을 디코딩
            List<Object> values = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                Object element = elementLayout.decode(data, offset);
                values.add(element);
                elementLayout.printDebug(data, offset, field);
                offset += elementLayout.getSpan(); // 요소 크기만큼 이동
            }

            // List 또는 배열로 변환 및 필드 설정
            Class<?> fieldType = field.getType();
            if (List.class.isAssignableFrom(fieldType)) {
                field.set(instance, values); // List 타입 지원
            } else if (fieldType.isArray()) {
                if (fieldType.getComponentType().isPrimitive()) {
                    field.set(instance, convertToPrimitiveArray(values, fieldType.getComponentType()));
                } else {
                    field.set(instance, values.toArray((Object[]) java.lang.reflect.Array.newInstance(fieldType.getComponentType(), values.size())));
                }
            } else {
                throw new UnsupportedOperationException("Unsupported field type: " + fieldType);
            }

            return offset;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to decode sequence field: " + field.getName(), e);
        }
    }

    /**
     * Converts a List<Object> to a primitive array (e.g., float[], int[], etc.).
     * Supports primitive types only.
     *
     * @param values      The list of values to convert.
     * @param componentType The type of the primitive array (e.g., float, int).
     * @return A primitive array of the desired type.
     */
    private static Object convertToPrimitiveArray(List<Object> values, Class<?> componentType) {
        if (componentType == float.class) {
            float[] array = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = ((Number) values.get(i)).floatValue();
            }
            return array;
        } else if (componentType == int.class) {
            int[] array = new int[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = ((Number) values.get(i)).intValue();
            }
            return array;
        } else if (componentType == double.class) {
            double[] array = new double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = ((Number) values.get(i)).doubleValue();
            }
            return array;
        } else if (componentType == long.class) {
            long[] array = new long[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = ((Number) values.get(i)).longValue();
            }
            return array;
        } else if (componentType == short.class) {
            short[] array = new short[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = ((Number) values.get(i)).shortValue();
            }
            return array;
        } else if (componentType == byte.class) {
            byte[] array = new byte[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = ((Number) values.get(i)).byteValue();
            }
            return array;
        } else if (componentType == char.class) {
            char[] array = new char[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = (char) ((Number) values.get(i)).intValue();
            }
            return array;
        } else if (componentType == boolean.class) {
            boolean[] array = new boolean[values.size()];
            for (int i = 0; i < values.size(); i++) {
                array[i] = (Boolean) values.get(i);
            }
            return array;
        }

        throw new UnsupportedOperationException("Unsupported primitive array type: " + componentType);
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
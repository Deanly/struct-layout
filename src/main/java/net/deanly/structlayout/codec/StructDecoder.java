package net.deanly.structlayout.codec;

import lombok.extern.slf4j.Slf4j;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.CustomLayoutField;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.analysis.DataTypeMapping;
import net.deanly.structlayout.Layout;
import net.deanly.structlayout.exception.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
public class StructDecoder {

    public static <T> T decode(Class<T> type, byte[] data, int startOffset) {
        try {
            if (startOffset < 0 || startOffset >= data.length) {
                throw new InvalidDataOffsetException(startOffset, data.length);
            }

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
                } else if (field.isAnnotationPresent(CustomLayoutField.class)) {
                    CustomLayoutField customLayoutField = field.getAnnotation(CustomLayoutField.class);
                    offset = decodeCustomLayoutField(instance, field, customLayoutField, data, offset);
                }
            }

            return instance;
        } catch (NoSuchMethodException e) {
            throw new NoDefaultConstructorException(type.getName());
        } catch (InstantiationException e) {
            throw new StructParsingException("Failed to instantiate class: " + type.getName(), e);
        } catch (InvocationTargetException e) {
            throw new StructParsingException("An exception occurred while invoking the constructor of class: " + type.getName(), e.getCause());
        } catch (IllegalAccessException e) {
            throw new FieldAccessException(type.getName(), type.getDeclaringClass().getName(), e);
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
                    field.isAnnotationPresent(StructObjectField.class) ||
                    field.isAnnotationPresent(CustomLayoutField.class)) {
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
        } else if (field.isAnnotationPresent(CustomLayoutField.class)) {
            return field.getAnnotation(CustomLayoutField.class).order();
        }
        throw new FieldOrderException(field.getName());
    }

    /**
     * StructField를 디코딩
     */
    private static <T> int decodeStructField(T instance, Field field, StructField annotation, byte[] data, int offset) throws IllegalAccessException {
        Layout<Object> layout = DataTypeMapping.getLayout(annotation.dataType());
        Object value = layout.decode(data, offset);
        // 현재 필드 값을 설정
        field.set(instance, value);

        layout.printDebug(data, offset, field);

        // 필드의 길이만큼 오프셋 이동
        return offset + layout.getSpan();
    }

    /**
     * SequenceField를 디코딩
     */
    private static <T> int decodeSequenceField(T instance, Field field, SequenceField annotation, byte[] data, int offset) throws IllegalAccessException {
        Layout<Object> elementLayout = DataTypeMapping.getLayout(annotation.elementType());
        Layout<Object> lengthLayout = DataTypeMapping.getLayout(annotation.lengthType());

        // 길이 정보 읽기
        Object rawLength = lengthLayout.decode(data, offset);
        Class<?> expectedLengthType = annotation.lengthType().getFieldType(); // 예상 타입 가져오기
        Object lengthValue = convertDecodedValue(rawLength, expectedLengthType, field.getName());

        int length;
        if (lengthValue instanceof Integer) {
            length = (Integer) lengthValue;
        } else if (lengthValue instanceof Long) {
            length = Math.toIntExact((Long) lengthValue); // Long 값을 int로 변환
        } else {
            throw new StructParsingException(
                    String.format("Field '%s': Unable to decode length. Expected Integer or Long but got %s.", field.getName(), lengthValue.getClass().getName())
            );
        }

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
            throw new InvalidSequenceTypeException(field.getName(), fieldType);
        }

        return offset;
    }

    private static Object convertDecodedValue(Object value, Class<?> expectedType, String fieldName) {
        if (value == null) {
            throw new StructParsingException("Decoded value cannot be null for field: " + fieldName);
        }

        // 이미 예상 타입과 일치하면 그대로 반환
        if (expectedType.isInstance(value)) {
            return value;
        }

        // Long 타입으로 변환
        if (value instanceof Integer && expectedType == Long.class) {
            return ((Integer) value).longValue();
        }

        // Integer 타입으로 변환
        if (value instanceof Long && expectedType == Integer.class) {
            long longValue = (Long) value;
            if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
                throw new StructParsingException(
                        String.format("Field '%s': Value %d exceeds the range for Integer type.", fieldName, longValue)
                );
            }
            return (int) longValue;
        }

        // BigInteger로 변환
        if ((value instanceof Integer || value instanceof Long) && expectedType == java.math.BigInteger.class) {
            return java.math.BigInteger.valueOf(((Number) value).longValue());
        }

        // 변환 불가능한 경우 예외 발생
        throw new StructParsingException(
                String.format("Field '%s': Cannot convert value of type %s to %s.", fieldName, value.getClass().getName(), expectedType.getName())
        );
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
    private static <T> int decodeStructObjectField(T instance, Field field, StructObjectField annotation, byte[] data, int offset) throws IllegalAccessException {
        Class<?> nestedType = field.getType();
        Object nestedInstance = decode(nestedType, data, offset);

        field.set(instance, nestedInstance);

        // 중첩된 객체의 끝 오프셋 계산
        return offset + calculateEncodedObjectSize(nestedType, nestedInstance);
    }

    /**
     * 중첩된 객체의 크기를 계산
     */
    private static int calculateObjectSizeFromFields(Class<?> type, Object instance) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Field[] fields = type.getDeclaredFields();
        List<Field> orderedFields = getOrderedFields(fields);
        int size = 0;

        for (Field field : orderedFields) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(StructField.class)) {
                // TODO(dean): 일부 DataTypeMapping 에 캐싱되지 않는 동적 타입의 처리가 필요함.
                StructField structField = field.getAnnotation(StructField.class);
                Layout<?> layout = DataTypeMapping.getLayout(structField.dataType());
                size += layout.getSpan();
            } else if (field.isAnnotationPresent(SequenceField.class)) {
                SequenceField sequenceField = field.getAnnotation(SequenceField.class);
                Layout<?> lengthLayout = DataTypeMapping.getLayout(sequenceField.lengthType());
                Layout<?> elementLayout = DataTypeMapping.getLayout(sequenceField.elementType());
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

    /**
     * 주어진 데이터와 오프셋으로부터 특정 필드의 데이터를 해석(decode)하여 인스턴스 객체의 필드에 값을 설정
     */
    @SuppressWarnings("unchecked")
    private static <T> int decodeCustomLayoutField(T instance, Field field, CustomLayoutField annotation, byte[] data, int offset) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Class<? extends Layout<?>> layoutClazz = annotation.layout();
        Layout<Object> layout = (Layout<Object>) layoutClazz.getDeclaredConstructor().newInstance();

        Object value = layout.decode(data, offset);
        field.set(instance, value);

        layout.printDebug(data, offset, field);

        return offset + layout.getSpan();
    }

}
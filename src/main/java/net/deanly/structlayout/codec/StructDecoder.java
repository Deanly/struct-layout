package net.deanly.structlayout.codec;

import lombok.extern.slf4j.Slf4j;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.CustomLayoutField;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.analysis.CachedLayoutProvider;
import net.deanly.structlayout.Layout;
import net.deanly.structlayout.codec.helpers.FieldHelper;
import net.deanly.structlayout.factory.ClassFactory;
import net.deanly.structlayout.codec.helpers.NumberConversionHelper;
import net.deanly.structlayout.exception.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StructDecoder {

    public static <T> T decode(Class<T> type, byte[] data, int startOffset) {
        try {
            if (startOffset < 0 || startOffset >= data.length) {
                throw new InvalidDataOffsetException(startOffset, data.length);
            }

            T instance = ClassFactory.createNoArgumentsInstance(type);

            // 필드를 "order" 기준으로 정렬
            Field[] fields = type.getDeclaredFields();
            List<Field> orderedFields = FieldHelper.getOrderedFields(fields);

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
            throw new StructParsingException("Failed to create an instance of class: " + type.getName(), e);
        } catch (InstantiationException e) {
            throw new StructParsingException("Failed to instantiate class: " + type.getName(), e);
        } catch (InvocationTargetException e) {
            throw new StructParsingException("An exception occurred while invoking the constructor of class: " + type.getName(), e.getCause());
        } catch (IllegalAccessException e) {
            throw new FieldAccessException(type.getName(), type.getDeclaringClass().getName(), e);
        }
    }

    /**
     * StructField를 디코딩
     */
    private static <T> int decodeStructField(T instance, Field field, StructField annotation, byte[] data, int offset) throws IllegalAccessException {
        Layout<Object> layout = CachedLayoutProvider.getLayout(annotation.dataType());
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
    private static <T> int decodeSequenceField(
            T instance, Field field, SequenceField annotation, byte[] data, int offset
    ) throws IllegalAccessException {
        Layout<Object> elementLayout = CachedLayoutProvider.getLayout(annotation.elementType());
        Layout<Object> lengthLayout = CachedLayoutProvider.getLayout(annotation.lengthType());

        // 길이 정보 읽기
        Object rawLength = lengthLayout.decode(data, offset);
        int length = NumberConversionHelper.convertToInt(rawLength, field.getName());

        lengthLayout.printDebug(data, offset, field);
        offset += lengthLayout.getSpan();

        // 요소 값들을 디코딩
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            Object element = elementLayout.decode(data, offset);
            values.add(element);

            elementLayout.printDebug(data, offset, field);
            offset += elementLayout.getSpan();
        }

        // List 또는 배열로 변환 및 필드 할당
        if (List.class.isAssignableFrom(field.getType())) {
            field.set(instance, values);
        } else if (field.getType().isArray()) {
            if (field.getType().getComponentType().isPrimitive()) {
                field.set(instance, NumberConversionHelper.convertToPrimitiveArray(values, field.getType().getComponentType()));
            } else {
                field.set(instance, values.toArray((Object[]) java.lang.reflect.Array.newInstance(field.getType().getComponentType(), values.size())));
            }
        } else {
            throw new InvalidSequenceTypeException(field.getName(), field.getType());
        }

        return offset;
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

    /**
     * 주어진 데이터와 오프셋으로부터 특정 필드의 데이터를 해석(decode)하여 인스턴스 객체의 필드에 값을 설정
     */
    private static <T> int decodeCustomLayoutField(T instance, Field field, CustomLayoutField annotation, byte[] data, int offset) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Layout<?> layout = ClassFactory.createLayoutInstance(annotation.layout());

        Object value = layout.decode(data, offset);
        if (field.getType().isPrimitive() && value == null) {
            throw new IllegalArgumentException("Cannot set a null value to a primitive field: " + field.getName());
        }
        field.set(instance, value);

        layout.printDebug(data, offset, field);

        return offset + layout.getSpan();
    }

}
package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.exception.InvalidSequenceTypeException;
import net.deanly.structlayout.exception.LayoutInitializationException;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class StructSequenceFieldHandler extends BaseFieldHandler {

    @SuppressWarnings("unchecked")
    @Override
    public <T> int handleField(T instance, java.lang.reflect.Field field, byte[] data, int offset) throws IllegalAccessException {
        // 1. 어노테이션 확인
        StructSequenceField annotation = field.getAnnotation(StructSequenceField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @SequenceField", field.getName())
            );
        }

        // 2. Layout 인스턴스 가져오기
        Field<?> lengthField = resolveLayout(annotation.lengthType()); // 길이 타입 Layout
        Class<? extends Field<?>> elementFieldClass = annotation.elementType();

        // 3. 길이 정보 디코딩
        Object rawLengthValue = lengthField.decode(data, offset);
        int length = (int) TypeConverterHelper.convertToType(rawLengthValue, Integer.class);
        int currentOffset = offset + lengthField.getSpan();

        // 4. 배열 또는 컬렉션 타입 확인
        Class<?> fieldType = field.getType();
        Object result;
        Class<?> elementType;

        if (fieldType.isArray()) {
            // 배열인 경우
            elementType = fieldType.getComponentType();
            result = Array.newInstance(elementType, length);
        } else if (Collection.class.isAssignableFrom(fieldType)) {
            // 컬렉션인 경우
            elementType = resolveCollectionElementType(field);
            result = createCollectionInstance(fieldType);
        } else {
            throw new InvalidSequenceTypeException(fieldType.getName(), fieldType, "Only Array or Collection types are allowed.");
        }

        // 5. 개별 요소 디코드
        Field<Object> elementField;
        try {
            Field<?> elementFieldWildcard = elementFieldClass.getDeclaredConstructor().newInstance();
            elementField = (Field<Object>) elementFieldWildcard;
        } catch (Exception ex) {
            throw new LayoutInitializationException("Failed to initialize Field for elementType", ex);
        }
        for (int i = 0; i < length; i++) {
            Object rawElement = elementField.decode(data, currentOffset);
            Object convertedElement = TypeConverterHelper.convertToType(rawElement, elementType);

            if (fieldType.isArray()) {
                Array.set(result, i, convertedElement);
            } else {
                ((Collection<Object>) result).add(convertedElement);
            }

            currentOffset += elementField.getSpan();
        }

        // 6. 필드 값 설정
        field.setAccessible(true);
        field.set(instance, result);

        return currentOffset - offset;
    }

    private Class<?> resolveCollectionElementType(java.lang.reflect.Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            if (parameterizedType.getActualTypeArguments().length > 0) {
                return (Class<?>) parameterizedType.getActualTypeArguments()[0];
            }
        }
        return Object.class;
    }

    private Collection<Object> createCollectionInstance(Class<?> fieldType) {
        if (List.class.isAssignableFrom(fieldType)) {
            return new ArrayList<>();
        } else if (Set.class.isAssignableFrom(fieldType)) {
            return new HashSet<>();
        } else if (Queue.class.isAssignableFrom(fieldType)) {
            return new LinkedList<>();
        } else {
            throw new IllegalArgumentException(
                    String.format("Unsupported collection type '%s'. Only List, Set, and Queue are supported.", fieldType.getName())
            );
        }
    }
}
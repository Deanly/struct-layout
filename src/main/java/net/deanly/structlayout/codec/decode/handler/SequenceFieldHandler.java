package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.exception.InvalidSequenceTypeException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class SequenceFieldHandler extends BaseFieldHandler {

    @SuppressWarnings("unchecked")
    @Override
    public <T> int handleField(T instance, Field field, byte[] data, int offset) throws IllegalAccessException {
        // **1. 어노테이션 확인**
        SequenceField annotation = field.getAnnotation(SequenceField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @SequenceField", field.getName())
            );
        }

        // **2. Layout 인스턴스 가져오기**
        Layout<?> lengthLayout = resolveLayout(annotation.lengthType()); // 길이 타입 Layout
        Layout<?> elementLayout = resolveLayout(annotation.elementType()); // 요소 타입 Layout

        // **3. 길이 정보 디코딩**
        Object rawLengthValue = lengthLayout.decode(data, offset);
        if (rawLengthValue == null) {
            throw new IllegalArgumentException("Length decoding resulted in a null value for field: " + field.getName());
        }

        // 길이 값을 적절하게 변환
        int length = (int) TypeConverterHelper.convertToType(rawLengthValue, Integer.class);
        int currentOffset = offset + lengthLayout.getSpan(); // 길이 필드 크기만큼 오프셋 이동

        // **4. 배열 또는 컬렉션 타입 확인**
        Class<?> fieldType = field.getType();
        Object result = null; // 결과로 설정할 객체 (배열 또는 컬렉션)
        Class<?> elementType;

        if (fieldType.isArray()) {
            // 배열인 경우
            elementType = fieldType.getComponentType();
            result = Array.newInstance(elementType, length);
        } else if (Collection.class.isAssignableFrom(fieldType)) {
            // 컬렉션인 경우
            elementType = resolveCollectionElementType(field);
            result = createCollectionInstance(fieldType); // 결과로 사용할 컬렉션 객체
        } else {
            // 배열도 컬렉션도 아니면 예외 처리
            throw new InvalidSequenceTypeException(
                    fieldType.getName(), fieldType, "Only Array or Collection types are allowed."
            );
        }

        if (elementType == null) {
            throw new InvalidSequenceTypeException(field.getName(), fieldType);
        }

        // **5. 데이터를 디코드하여 요소 추가**
        for (int i = 0; i < length; i++) {
            // 요소 디코딩
            Object rawElement = elementLayout.decode(data, currentOffset);
            if (rawElement == null) {
                throw new IllegalArgumentException(
                        String.format("Element decoding resulted in a null value at index %d for field: %s", i, field.getName())
                );
            }
            // 요소를 적절한 타입으로 변환
            Object convertedElement = TypeConverterHelper.convertToType(rawElement, elementType);

            // 배열 또는 컬렉션에 추가
            if (fieldType.isArray()) {
                Array.set(result, i, convertedElement);
            } else if (result instanceof Collection) {
                ((Collection<Object>) result).add(convertedElement);
            }

            currentOffset += elementLayout.getSpan(); // 요소 데이터 크기만큼 이동
        }

        // **6. 필드에 값 설정**
        field.setAccessible(true);
        field.set(instance, result);

        // **7. 소비된 바이트 반환**
        return currentOffset - offset;
    }

    /**
     * 컬렉션의 제네릭 요소 타입 추출
     */
    private Class<?> resolveCollectionElementType(Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            if (parameterizedType.getActualTypeArguments().length > 0) {
                return (Class<?>) parameterizedType.getActualTypeArguments()[0];
            }
        }
        return Object.class; // 제네릭 타입을 알 수 없을 경우 기본적으로 Object 사용
    }

    /**
     * 컬렉션 또는 지원 가능한 컬렉션 타입을 생성합니다.
     */
    private Collection<Object> createCollectionInstance(Class<?> fieldType) {
        if (List.class.isAssignableFrom(fieldType)) {
            return new ArrayList<>();
        } else if (Set.class.isAssignableFrom(fieldType)) {
            return new HashSet<>();
        } else if (Queue.class.isAssignableFrom(fieldType)) {
            return new LinkedList<>();
        } else if (Deque.class.isAssignableFrom(fieldType)) {
            return new ArrayDeque<>();
        } else {
            throw new IllegalArgumentException(
                    String.format("Unsupported collection type '%s'. Only List, Set, Queue, and Deque are supported.", fieldType.getName())
            );
        }
    }
}
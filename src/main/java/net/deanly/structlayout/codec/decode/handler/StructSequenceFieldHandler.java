package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.annotation.OptionalEncoding;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.exception.InvalidSequenceTypeException;
import net.deanly.structlayout.exception.LayoutInitializationException;
import net.deanly.structlayout.type.DynamicSpanField;
import net.deanly.structlayout.type.advanced.NoneField;

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

        OptionalEncoding optionalEncoding = annotation.optional();
        int consumed = 0;

        // OptionalEncoding.BORSH 체크
        if (optionalEncoding == OptionalEncoding.BORSH) {
            boolean isPresent = isValuePresent(data, offset, optionalEncoding);
            consumed += 1;

            if (!isPresent) {
                field.setAccessible(true);
                field.set(instance, null);
                return consumed;
            }

            offset += 1;
        }

        // 2. Layout 인스턴스 가져오기
        Field<?> lengthField = resolveLayout(annotation.lengthType()); // 길이 타입 Layout
        Class<? extends Field<?>> elementFieldClass = annotation.elementType();

        // 3. 길이 정보 디코딩
        int length;
        boolean unsafeMode = lengthField instanceof NoneField;
        if (unsafeMode) {
            length = -1;
        } else {
            Object rawLengthValue = lengthField.decode(data, offset);
            length = (int) TypeConverterHelper.convertToType(rawLengthValue, Integer.class);
        }
        int currentOffset = offset + ((lengthField instanceof DynamicSpanField) ?
                ((DynamicSpanField) lengthField).calculateSpan(data, offset) :
                lengthField.getSpan());

        // 4. 배열 또는 컬렉션 타입 확인
        Class<?> fieldType = field.getType();
        Object result;
        Class<?> elementType;

        if (fieldType.isArray()) {
            // 배열인 경우
            elementType = fieldType.getComponentType();
            result = !unsafeMode ? Array.newInstance(elementType, length) : new ArrayList<>();
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
        int elementCount = 0;
        while ((unsafeMode && currentOffset < data.length) || (!unsafeMode && elementCount < length)) {
            Object rawElement = elementField.decode(data, currentOffset);

            int expectedSpan;
            if (elementField instanceof DynamicSpanField) {
                expectedSpan = ((DynamicSpanField) elementField).calculateSpan(data, currentOffset);
            } else {
                expectedSpan = elementField.getSpan();
            }

            if (expectedSpan == 0) {
                throw new IllegalStateException(
                        String.format(
                                "Failed to decode data at offset %d. The decoding process returned zero span. This indicates that parsing the given data (%s) into an instance of '%s' is not possible or the input data is corrupted.",
                                currentOffset,
                                java.util.Arrays.toString(data),
                                elementType != null ? elementType.getCanonicalName() : "Unknown Type"
                        )
                );
            }

            currentOffset += expectedSpan;

            if (rawElement != null) {
                Object convertedElement = TypeConverterHelper.convertToType(rawElement, elementType);

                if (fieldType.isArray() && !unsafeMode) {
                    Array.set(result, elementCount, convertedElement);
                } else {
                    ((Collection<Object>) result).add(convertedElement);
                }
            }
            elementCount++;
        }

        // 6. 필드 값 설정
        field.setAccessible(true);
        if (unsafeMode && fieldType.isArray()) {
            Object arrayResult = Array.newInstance(elementType, elementCount);
            List<?> tempList = (List<?>) result;
            for (int i = 0; i < elementCount; i++) {
                Array.set(arrayResult, i, tempList.get(i));
            }
            field.set(instance, arrayResult);
        } else {
            field.set(instance, result);
        }

        return consumed + (currentOffset - offset);
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
package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.codec.decode.StructDecodeResult;
import net.deanly.structlayout.codec.decode.StructDecoder;
import net.deanly.structlayout.annotation.StructSequenceObjectField;
import net.deanly.structlayout.codec.helpers.FieldHelper;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.exception.InvalidAnnotationUsageException;
import net.deanly.structlayout.exception.InvalidSequenceTypeException;
import net.deanly.structlayout.exception.LayoutInitializationException;
import net.deanly.structlayout.type.DynamicSpanField;
import net.deanly.structlayout.type.advanced.NoneField;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Decoder handler for fields annotated with @StructSequenceObjectField.
 */
public class StructSequenceObjectFieldHandler extends BaseFieldHandler {


    @SuppressWarnings("unchecked")
    @Override
    public <T> int handleField(T instance, java.lang.reflect.Field field, byte[] data, int offset) throws IllegalAccessException {
        // 1. 어노테이션 확인
        StructSequenceObjectField annotation = field.getAnnotation(StructSequenceObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @StructSequenceObjectField", field.getName())
            );
        }

        // 2. 길이와 요소 타입 확인
        Class<?> fieldType = field.getType();
        if (!fieldType.isArray() && !Collection.class.isAssignableFrom(fieldType)) {
            throw new InvalidSequenceTypeException(
                    field.getName(), fieldType,
                    "Only Array or Collection types are supported for @StructSequenceObjectField"
            );
        }
        Class<?> elementType = fieldType.isArray() ? fieldType.getComponentType() : resolveCollectionElementType(field);
        if (elementType.isPrimitive() || FieldHelper.PRIMITIVE_WRAPPERS.contains(elementType)) {
            throw new InvalidAnnotationUsageException(
                    String.format(
                            "Field '%s' uses @StructSequenceObjectField but the element type '%s' is a primitive type. Use @StructSequenceField instead.",
                            field.getName(), elementType.getName()
                    )
            );
        }
        if (!hasPublicNoArgsConstructor(elementType)) {
            throw new LayoutInitializationException(
                    String.format(
                            "The Layout class '%s' must have a public no-arguments constructor. Check field '%s'.",
                            elementType.getName(), field.getName()
                    )
            );
        }

        // 3. Layout 인스턴스 가져오기
        Field<?> lengthField = resolveLayout(annotation.lengthType());
        boolean unsafeMode = lengthField instanceof NoneField;

        // 4. 길이 정보 디코딩
        int currentOffset = offset;
        int length = 0;
        if (!unsafeMode) {
            Object lengthRawValue = lengthField.decode(data, offset);
            length = (int) TypeConverterHelper.convertToType(lengthRawValue, Integer.class);
            currentOffset += ((lengthField instanceof DynamicSpanField) ?
                    ((DynamicSpanField) lengthField).calculateSpan(data, offset) :
                    lengthField.getSpan());
        }

        // 5. 배열 또는 컬렉션 타입 확인
        Object result;
        if (fieldType.isArray()) {
            // 배열인 경우
            elementType = fieldType.getComponentType();
            result = !unsafeMode ? Array.newInstance(elementType, length) : new ArrayList<>();
        } else if (Collection.class.isAssignableFrom(fieldType)) {
            // 컬렉션인 경우
            elementType = resolveCollectionElementType(field);
            result = createCollectionInstance(fieldType);
        } else {
            throw new InvalidSequenceTypeException(field.getName(), fieldType, "Only Array or Collection types are supported for @StructSequenceObjectField");
        }

        // 6. 개별 요소 디코드
        int elementCount = 0;
        while (unsafeMode ? currentOffset < data.length : elementCount < length) {
            StructDecodeResult<?> decodeResult = StructDecoder.decode(elementType, data, currentOffset);
            Object decodedValue = decodeResult.getValue();
            int decodedSize = decodeResult.getSize();

            if (decodedValue == null || decodedSize == 0) {
                throw new IllegalStateException(
                        String.format(
                                "Failed to decode data at offset %d. The decoding process returned null. This indicates that parsing the given data (%s) into an instance of '%s' is not possible or the input data is corrupted.",
                                currentOffset,
                                java.util.Arrays.toString(data),
                                elementType != null ? elementType.getCanonicalName() : "Unknown Type"
                        )
                );
            }

            if (fieldType.isArray()) {
                if (unsafeMode) {
                    // Unsafe Mode에서 리스트에 추가
                    ((List<Object>) result).add(decodedValue);
                } else {
                    Array.set(result, elementCount, decodedValue);
                }
            } else {
                ((Collection<Object>) result).add(decodedValue);
            }

            currentOffset += decodedSize;
            elementCount++;
        }

        // 7. 필드 값 설정
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

        return currentOffset - offset;
    }

    private boolean hasPublicNoArgsConstructor(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            return java.lang.reflect.Modifier.isPublic(constructor.getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private Class<?> resolveCollectionElementType(java.lang.reflect.Field field) {
        // Resolve the generic type of the Collection
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
        } else {
            throw new IllegalArgumentException(
                    String.format("Unsupported collection type '%s'. Only List is currently supported.", fieldType.getName())
            );
        }
    }
}
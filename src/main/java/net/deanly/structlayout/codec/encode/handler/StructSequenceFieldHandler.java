package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.analysis.FieldDebugInfo;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.exception.InvalidSequenceTypeException;
import net.deanly.structlayout.exception.LayoutInitializationException;
import net.deanly.structlayout.codec.helpers.ByteArrayHelper;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.type.CountableField;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class StructSequenceFieldHandler extends BaseFieldHandler {

    @Override
    public <T> byte[] handleField(T instance, java.lang.reflect.Field field) throws IllegalAccessException {
        // 1. @StructSequenceField 어노테이션 가져오기
        StructSequenceField annotation = field.getAnnotation(StructSequenceField.class);
        if (annotation == null) {
            throw new InvalidSequenceTypeException(field.getName(), field.getType());
        }

        // 2. 필드 값 추출
        Object arrayOrList = extractFieldValue(instance, field);

        // 3. 길이 및 요소 타입 메타데이터 조회
        Class<? extends CountableField<?>> lengthType = annotation.lengthType();
        Class<? extends Field<?>> elementFieldType = annotation.elementType();

        // 4. Null 처리: 빈 배열로 대체
        if (arrayOrList == null) {
            int length = 0; // 길이는 0으로 설정
            return encodeLengthAndElements(length, new ArrayList<>(), lengthType, elementFieldType);
        }

        // 5. 배열 또는 List인지 확인, 아니면 예외 발생
        List<Object> elements = new ArrayList<>();
        if (arrayOrList.getClass().isArray()) {
            int arrayLength = Array.getLength(arrayOrList);
            for (int i = 0; i < arrayLength; i++) {
                elements.add(Array.get(arrayOrList, i));
            }
        } else if (arrayOrList instanceof Iterable) {
            for (Object element : (Iterable<?>) arrayOrList) {
                elements.add(element);
            }
        } else {
            // 지원되지 않는 필드 타입인 경우
            throw new InvalidSequenceTypeException(field.getName(), arrayOrList.getClass());
        }

        // 6. 길이와 요소 병합 처리
        return encodeLengthAndElements(elements.size(), elements, lengthType, elementFieldType);
    }

    /**
     * 길이와 요소 데이터 처리 후 인코딩
     */
    private byte[] encodeLengthAndElements(
            int length,
            List<Object> elements,
            Class<? extends CountableField<?>> lengthType,
            Class<? extends Field<?>> elementFieldType
    ) {
        List<byte[]> encodedChunks = new ArrayList<>();
        try {
            // 길이 인코딩
            Object convertedLength = TypeConverterHelper.convertToLayoutType(length, lengthType);
            encodedChunks.add(encodeElement(lengthType, convertedLength));

            // 요소 인코딩
            for (Object element : elements) {
                Object convertedElement = TypeConverterHelper.convertToLayoutType(element, elementFieldType);
                encodedChunks.add(encodeElement(elementFieldType, convertedElement));
            }
        } catch (Exception e) {
            throw new LayoutInitializationException(
                    "Failed to initialize layout for element or length type. Ensure that Field class is correctly defined.", e);
        }

        // 전체 병합 반환
        return ByteArrayHelper.mergeChunks(encodedChunks);
    }

    @Override
    public <T> List<FieldDebugInfo.Builder> handleDebug(T instance, java.lang.reflect.Field field) throws IllegalAccessException {
        List<FieldDebugInfo.Builder> builders = new ArrayList<>();

        // 1. @StructSequenceField 어노테이션 가져오기
        StructSequenceField annotation = field.getAnnotation(StructSequenceField.class);
        if (annotation == null) {
            throw new InvalidSequenceTypeException(field.getName(), field.getType());
        }

        // 2. 길이 및 요소 타입 메타데이터 조회
        Class<? extends CountableField<?>> lengthType = annotation.lengthType();
        Class<? extends Field<?>> elementFieldType = annotation.elementType();

        // 3. 필드 값 추출
        Object arrayOrList = extractFieldValue(instance, field);
        if (arrayOrList == null) {
            int length = 0; // 길이는 0으로 설정
            return debugLengthAndElements(field, length, new ArrayList<>(), lengthType, elementFieldType);
        }

        // 5. 배열 또는 List인지 확인, 아니면 예외 발생
        List<Object> elements = new ArrayList<>();
        if (arrayOrList.getClass().isArray()) {
            int arrayLength = Array.getLength(arrayOrList);
            for (int i = 0; i < arrayLength; i++) {
                elements.add(Array.get(arrayOrList, i));
            }
        } else if (arrayOrList instanceof Iterable) {
            for (Object element : (Iterable<?>) arrayOrList) {
                elements.add(element);
            }
        } else {
            // 지원되지 않는 필드 타입인 경우
            throw new InvalidSequenceTypeException(field.getName(), arrayOrList.getClass());
        }

        // 6. 길이와 요소 병합 처리
        return debugLengthAndElements(field, elements.size(), elements, lengthType, elementFieldType);
    }

    private List<FieldDebugInfo.Builder> debugLengthAndElements(
            java.lang.reflect.Field field,
            int length,
            List<Object> elements,
            Class<? extends CountableField<?>> lengthType,
            Class<? extends Field<?>> elementFieldType
    ) {
        List<FieldDebugInfo.Builder> builders = new ArrayList<>();
        try {
            Object convertedLength = TypeConverterHelper.convertToLayoutType(length, lengthType);
            byte[] encodedLength = encodeElement(lengthType, convertedLength);
            builders.add(FieldDebugInfo.builder()
                    .orderSuffix("[].length")
                    .fieldName(field.getName())
                    .encodedBytes(encodedLength));

            for (int i = 0; i < elements.size(); i++) {
                Object convertedElement = TypeConverterHelper.convertToLayoutType(elements.get(i), elementFieldType);
                byte[] encodedElement = encodeElement(elementFieldType, convertedElement);
                builders.add(FieldDebugInfo.builder()
                        .orderSuffix("[" + i + "]")
                        .fieldName(field.getName())
                        .encodedBytes(encodedElement));
            }
        } catch (Exception e) {
            throw new LayoutInitializationException(
                    "Failed to initialize layout for element or length type. Ensure that Field class is correctly defined.", e);
        }

        return builders;
    }

}
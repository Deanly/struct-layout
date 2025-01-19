package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.exception.InvalidSequenceTypeException;
import net.deanly.structlayout.exception.LayoutInitializationException;
import net.deanly.structlayout.codec.helpers.ByteArrayHelper;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.type.DataType;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SequenceFieldHandler extends BaseFieldHandler {

    @Override
    protected DataType resolveDataType(Field field) {
        SequenceField annotation = field.getAnnotation(SequenceField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is missing @SequenceField annotation.", field.getName()));
        }
        return annotation.elementType(); // 요소 데이터 타입 반환
    }

    @Override
    public <T> byte[] handleField(T instance, Field field) throws IllegalAccessException {
        // 1. 필드 값 추출
        Object arrayOrList = extractFieldValue(instance, field);

        // 2. 어노테이션과 데이터 타입 조회
        SequenceField annotation = field.getAnnotation(SequenceField.class);
        if (annotation == null) {
            throw new InvalidSequenceTypeException(
                    field.getName(),
                    field.getType()
            );
        }

        DataType lengthType = annotation.lengthType();
        DataType elementType = annotation.elementType();

        // 3. Null 처리: 빈 배열로 대체
        if (arrayOrList == null) {
            int length = 0; // 길이는 0으로 설정
            return encodeLengthAndElements(length, new ArrayList<>(), lengthType, elementType);
        }

        // 4. 배열 또는 List인지 확인, 아니면 예외 발생
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

        // 5. 길이와 요소 병합 처리
        return encodeLengthAndElements(elements.size(), elements, lengthType, elementType);
    }

    /**
     * 길이와 요소 데이터 처리 후 인코딩
     */
    private byte[] encodeLengthAndElements(
            int length,
            List<Object> elements,
            DataType lengthType,
            DataType elementType
    ) {
        List<byte[]> encodedChunks = new ArrayList<>();
        try {
            // 길이 인코딩
            Layout<Object> lengthLayout = resolveLayout(lengthType);
            Object convertedLength = TypeConverterHelper.convertToLayoutType(length, lengthType);
            encodedChunks.add(lengthLayout.encode(convertedLength));

            // 요소 인코딩
            Layout<Object> elementLayout = resolveLayout(elementType);
            for (Object element : elements) {
                // 각 요소 변환 및 인코딩
                Object convertedElement = TypeConverterHelper.convertToLayoutType(element, elementType);
                encodedChunks.add(elementLayout.encode(convertedElement));
            }
        } catch (NullPointerException e) {
            throw new LayoutInitializationException(
                    "Failed to initialize layout for element or length type. Ensure that DataType is correctly defined.", e);
        }

        // 전체 병합 반환
        return ByteArrayHelper.mergeChunks(encodedChunks);
    }
}
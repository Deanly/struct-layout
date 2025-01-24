package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Field;
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
    @SuppressWarnings("unchecked")
    private byte[] encodeLengthAndElements(
            int length,
            List<Object> elements,
            Class<? extends CountableField<?>> lengthType,
            Class<? extends Field<?>> elementFieldType
    ) {
        List<byte[]> encodedChunks = new ArrayList<>();
        try {
            // 길이 인코딩
            Field<Object> lengthField = resolveLayout(lengthType);
            Object convertedLength = TypeConverterHelper.convertToLayoutType(length, lengthType);
            encodedChunks.add(lengthField.encode(convertedLength));

            // 요소 인코딩
            Field<Object> elementField;
            try {
                Field<?> elementFieldWildcard = elementFieldType.getDeclaredConstructor().newInstance();
                elementField = (Field<Object>) elementFieldWildcard;
            } catch (Exception ex) {
                throw new LayoutInitializationException("Failed to initialize Field for elementType", ex);
            }
            for (Object element : elements) {
                Object convertedElement = TypeConverterHelper.convertToLayoutType(element, elementFieldType);
                encodedChunks.add(elementField.encode(convertedElement));
            }
        } catch (Exception e) {
            throw new LayoutInitializationException(
                    "Failed to initialize layout for element or length type. Ensure that Field class is correctly defined.", e);
        }

        // 전체 병합 반환
        return ByteArrayHelper.mergeChunks(encodedChunks);
    }
}
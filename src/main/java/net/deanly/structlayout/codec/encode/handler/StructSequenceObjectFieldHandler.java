
package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.annotation.StructSequenceObjectField;
import net.deanly.structlayout.codec.encode.StructEncoder;
import net.deanly.structlayout.codec.helpers.ByteArrayHelper;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.exception.InvalidSequenceTypeException;
import net.deanly.structlayout.exception.LayoutInitializationException;
import net.deanly.structlayout.type.CountableField;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class StructSequenceObjectFieldHandler extends BaseFieldHandler {

    @Override
    public <T> byte[] handleField(T instance, java.lang.reflect.Field field) throws IllegalAccessException {
        // 1. `@StructSequenceObjectField` 어노테이션 가져오기
        StructSequenceObjectField annotation = field.getAnnotation(StructSequenceObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Field is not annotated with @StructSequenceObjectField");
        }

        // 2. 필드 값 추출
        Object arrayOrList = extractFieldValue(instance, field);

        // 3. 길이 타입 메타데이터 조회
        Class<? extends CountableField<?>> lengthType = annotation.lengthType();

        // 4. Null 처리
        if (arrayOrList == null) {
            return encodeLengthAndElements(0, new ArrayList<>(), lengthType);
        }

        // 5. 배열 또는 List 인지 확인
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

        // 6. 인코딩
        return encodeLengthAndElements(elements.size(), elements, lengthType);
    }

    /**
     * 길이와 요소 데이터 처리 후 인코딩
     */
    @SuppressWarnings("unchecked")
    private byte[] encodeLengthAndElements(
            int length,
            List<Object> elements,
            Class<? extends CountableField<?>> lengthType
    ) {
        List<byte[]> encodedChunks = new ArrayList<>();
        try {
            // 길이 인코딩
            Field<Object> lengthField = resolveLayout(lengthType);
            Object convertedLength = TypeConverterHelper.convertToLayoutType(length, lengthType);
            encodedChunks.add(lengthField.encode(convertedLength));

            // 요소 인코딩
            for (Object element : elements) {
                byte[] convertedElement = StructEncoder.encode(element);
                encodedChunks.add(convertedElement);
            }
        } catch (Exception e) {
            throw new LayoutInitializationException(
                    "Failed to initialize layout for element or length type. Ensure that Field class is correctly defined.", e);
        }

        // 전체 병합 반환
        return ByteArrayHelper.mergeChunks(encodedChunks);
    }
}

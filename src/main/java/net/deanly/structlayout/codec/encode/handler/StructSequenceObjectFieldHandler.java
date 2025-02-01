
package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.analysis.FieldDebugInfo;
import net.deanly.structlayout.annotation.StructSequenceObjectField;
import net.deanly.structlayout.codec.encode.StructEncoder;
import net.deanly.structlayout.codec.helpers.ByteArrayHelper;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.exception.InvalidSequenceTypeException;
import net.deanly.structlayout.exception.LayoutInitializationException;
import net.deanly.structlayout.type.CountableField;
import net.deanly.structlayout.type.advanced.NoneField;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static net.deanly.structlayout.codec.encode.FieldProcessor.processFieldRecursivelyWithDebug;

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
        boolean unsafeMode = NoneField.class.isAssignableFrom(lengthType);

        // 4. Null 처리
        if (arrayOrList == null) {
            return encodeLengthAndElements(new ArrayList<>(), lengthType, unsafeMode);
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
        return encodeLengthAndElements(elements, lengthType, unsafeMode);
    }

    /**
     * 길이와 요소 데이터 처리 후 인코딩
     */
    @SuppressWarnings("unchecked")
    private byte[] encodeLengthAndElements(
            List<Object> elements,
            Class<? extends CountableField<?>> lengthType,
            boolean unsafeMode
    ) {
        List<byte[]> encodedChunks = new ArrayList<>();
        try {
            // 길이 인코딩
            if (!unsafeMode) {
                Field<Object> lengthField = resolveLayout(lengthType);
                Object convertedLength = TypeConverterHelper.convertToLayoutType(elements.size(), lengthType);
                encodedChunks.add(lengthField.encode(convertedLength));
            }

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

    @Override
    public <T> List<FieldDebugInfo.Builder> handleDebug(T instance, java.lang.reflect.Field field) throws IllegalAccessException {
        // 1. `@StructSequenceObjectField` 어노테이션 가져오기
        StructSequenceObjectField annotation = field.getAnnotation(StructSequenceObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Field is not annotated with @StructSequenceObjectField");
        }

        // 2. 필드 값 추출
        Object arrayOrList = extractFieldValue(instance, field);

        // 3. 길이 타입 메타데이터 조회
        Class<? extends CountableField<?>> lengthType = annotation.lengthType();
        boolean unsafeMode = NoneField.class.isAssignableFrom(lengthType);

        // 4. Null 처리
        if (arrayOrList == null) {
            return debugLengthAndElements(field, new ArrayList<>(), lengthType, unsafeMode);
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
        return debugLengthAndElements(field, elements, lengthType, unsafeMode);
    }

    private List<FieldDebugInfo.Builder> debugLengthAndElements(
            java.lang.reflect.Field field,
            List<Object> elements,
            Class<? extends CountableField<?>> lengthType,
            boolean unsafeMode
    ) {
        List<FieldDebugInfo.Builder> builders = new ArrayList<>();
        try {
            if (!unsafeMode) {
                // 길이 인코딩
                Field<Object> lengthField = resolveLayout(lengthType);
                Object convertedLength = TypeConverterHelper.convertToLayoutType(elements.size(), lengthType);
                byte[] encodedLenth = lengthField.encode(convertedLength);
                builders.add(FieldDebugInfo.builder()
                        .orderSuffix("[].length")
                        .fieldName(field.getName())
                        .encodedBytes(encodedLenth));
            }

            // 요소 인코딩
            for (int i = 0; i < elements.size(); i++) {
                java.lang.reflect.Field[] elementFields = elements.get(i).getClass().getDeclaredFields();

                for (java.lang.reflect.Field elementField : elementFields) {
                    elementField.setAccessible(true);
                    List<FieldDebugInfo> infos = processFieldRecursivelyWithDebug(elements.get(i), elementField, "");
                    for (FieldDebugInfo info : infos) {
                        builders.add(FieldDebugInfo.builder()
                                .orderSuffix("[" + i + "]" + info.getOrderString())
                                .fieldName(info.getFieldName())
                                .encodedBytes(info.getEncodedBytes()));
                    }
                };
            }
        } catch (Exception e) {
            throw new LayoutInitializationException(
                    "Failed to initialize layout for element or length type. Ensure that Field class is correctly defined.", e);
        }

        // 전체 병합 반환
        return builders;
    }
}

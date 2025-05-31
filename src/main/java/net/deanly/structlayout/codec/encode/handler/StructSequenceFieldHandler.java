package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.analysis.FieldDebugInfo;
import net.deanly.structlayout.annotation.OptionalEncoding;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.exception.InvalidSequenceTypeException;
import net.deanly.structlayout.exception.LayoutInitializationException;
import net.deanly.structlayout.codec.helpers.ByteArrayHelper;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.type.CountableField;
import net.deanly.structlayout.type.advanced.NoneField;

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

        OptionalEncoding opt = annotation.optional();

        // 2. 필드 값 추출
        Object arrayOrList = extractFieldValue(instance, field);

        // 3. 길이 및 요소 타입 메타데이터 조회
        Class<? extends CountableField<?>> lengthType = annotation.lengthType();
        Class<? extends Field<?>> elementFieldType = annotation.elementType();
        boolean unsafeMode = NoneField.class.isAssignableFrom(lengthType);

        if (opt == OptionalEncoding.BORSH) {
            if (arrayOrList == null) {
                return new byte[]{0x00}; // None
            } else {
                List<Object> elements = toElementList(arrayOrList);
                byte[] encoded = encodeLengthAndElements(elements, lengthType, elementFieldType, unsafeMode);
                byte[] result = new byte[1 + encoded.length];
                result[0] = 0x01;
                System.arraycopy(encoded, 0, result, 1, encoded.length);
                return result;
            }
        }

        if (arrayOrList == null) {
            return encodeLengthAndElements(new ArrayList<>(), lengthType, elementFieldType, unsafeMode);
        }

        List<Object> elements = toElementList(arrayOrList);
        return encodeLengthAndElements(elements, lengthType, elementFieldType, unsafeMode);
    }

    private List<Object> toElementList(Object arrayOrList) {
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
            throw new InvalidSequenceTypeException("Unsupported field type", arrayOrList.getClass());
        }
        return elements;
    }

    /**
     * 길이와 요소 데이터 처리 후 인코딩
     */
    private byte[] encodeLengthAndElements(
            List<Object> elements,
            Class<? extends CountableField<?>> lengthType,
            Class<? extends Field<?>> elementFieldType,
            boolean unsafeMode
    ) {
        List<byte[]> encodedChunks = new ArrayList<>();
        try {
            if (!unsafeMode) {
                // 길이 인코딩
                Object convertedLength = TypeConverterHelper.convertToLayoutType(elements.size(), lengthType);
                encodedChunks.add(encodeElement(lengthType, convertedLength));
            }

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
        StructSequenceField annotation = field.getAnnotation(StructSequenceField.class);
        if (annotation == null) {
            throw new InvalidSequenceTypeException(field.getName(), field.getType());
        }

        OptionalEncoding opt = annotation.optional();
        Class<? extends CountableField<?>> lengthType = annotation.lengthType();
        Class<? extends Field<?>> elementFieldType = annotation.elementType();
        boolean unsafeMode = NoneField.class.isAssignableFrom(lengthType);

        Object arrayOrList = extractFieldValue(instance, field);
        List<FieldDebugInfo.Builder> builders = new ArrayList<>();

        // OptionalEncoding.BORSH: Always write 1-byte tag
        if (opt == OptionalEncoding.BORSH) {
            builders.add(FieldDebugInfo.builder()
                    .fieldName(field.getName())
                    .orderSuffix("[].optional_tag")
                    .encodedBytes(new byte[] { (arrayOrList == null) ? (byte) 0x00 : (byte) 0x01 }));

            if (arrayOrList == null) {
                return builders; // no additional data
            }
        }

        // null-safe 변환
        List<Object> elements = (arrayOrList == null)
                ? new ArrayList<>()
                : toElementList(arrayOrList);

        // length field (if not unsafe)
        if (!unsafeMode) {
            Object convertedLength = TypeConverterHelper.convertToLayoutType(elements.size(), lengthType);
            byte[] encodedLength = encodeElement(lengthType, convertedLength);
            builders.add(FieldDebugInfo.builder()
                    .fieldName(field.getName())
                    .orderSuffix("[].length")
                    .encodedBytes(encodedLength));
        }

        // element list
        for (int i = 0; i < elements.size(); i++) {
            Object converted = TypeConverterHelper.convertToLayoutType(elements.get(i), elementFieldType);
            byte[] encoded = encodeElement(elementFieldType, converted);
            builders.add(FieldDebugInfo.builder()
                    .fieldName(field.getName())
                    .orderSuffix("[" + i + "]")
                    .encodedBytes(encoded));
        }

        return builders;
    }

}
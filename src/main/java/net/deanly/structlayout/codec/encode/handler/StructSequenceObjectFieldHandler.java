
package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.analysis.FieldDebugInfo;
import net.deanly.structlayout.annotation.OptionalEncoding;
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
        StructSequenceObjectField annotation = field.getAnnotation(StructSequenceObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Field is not annotated with @StructSequenceObjectField");
        }

        OptionalEncoding opt = annotation.optional();
        Object arrayOrList = extractFieldValue(instance, field);
        Class<? extends CountableField<?>> lengthType = annotation.lengthType();
        boolean unsafeMode = NoneField.class.isAssignableFrom(lengthType);

        if (opt == OptionalEncoding.BORSH) {
            if (arrayOrList == null) {
                return new byte[]{0x00};
            } else {
                List<Object> elements = toElementList(arrayOrList);
                byte[] encoded = encodeLengthAndElements(elements, lengthType, unsafeMode);
                byte[] result = new byte[1 + encoded.length];
                result[0] = 0x01;
                System.arraycopy(encoded, 0, result, 1, encoded.length);
                return result;
            }
        }

        List<Object> elements = (arrayOrList == null) ? new ArrayList<>() : toElementList(arrayOrList);
        return encodeLengthAndElements(elements, lengthType, unsafeMode);
    }

    @SuppressWarnings("unchecked")
    private byte[] encodeLengthAndElements(List<Object> elements,
                                           Class<? extends CountableField<?>> lengthType,
                                           boolean unsafeMode) {
        List<byte[]> encodedChunks = new ArrayList<>();
        try {
            if (!unsafeMode) {
                Field<Object> lengthField = resolveLayout(lengthType);
                Object convertedLength = TypeConverterHelper.convertToLayoutType(elements.size(), lengthType);
                encodedChunks.add(lengthField.encode(convertedLength));
            }

            for (Object element : elements) {
                encodedChunks.add(StructEncoder.encode(element));
            }

        } catch (Exception e) {
            throw new LayoutInitializationException("Failed to encode sequence object", e);
        }

        return ByteArrayHelper.mergeChunks(encodedChunks);
    }

    @Override
    public <T> List<FieldDebugInfo.Builder> handleDebug(T instance, java.lang.reflect.Field field) throws IllegalAccessException {
        StructSequenceObjectField annotation = field.getAnnotation(StructSequenceObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Field is not annotated with @StructSequenceObjectField");
        }

        OptionalEncoding opt = annotation.optional();
        Class<? extends CountableField<?>> lengthType = annotation.lengthType();
        boolean unsafeMode = NoneField.class.isAssignableFrom(lengthType);

        Object arrayOrList = extractFieldValue(instance, field);
        List<FieldDebugInfo.Builder> builders = new ArrayList<>();

        if (opt == OptionalEncoding.BORSH) {
            builders.add(FieldDebugInfo.builder()
                    .fieldName(field.getName())
                    .orderSuffix("[].optional_tag")
                    .encodedBytes(new byte[]{(byte) ((arrayOrList == null) ? 0x00 : 0x01)}));

            if (arrayOrList == null) {
                return builders;
            }
        }

        List<Object> elements = (arrayOrList == null) ? new ArrayList<>() : toElementList(arrayOrList);

        if (!unsafeMode) {
            Field<Object> lengthField = resolveLayout(lengthType);
            Object convertedLength = TypeConverterHelper.convertToLayoutType(elements.size(), lengthType);
            byte[] encodedLength = lengthField.encode(convertedLength);
            builders.add(FieldDebugInfo.builder()
                    .fieldName(field.getName())
                    .orderSuffix("[].length")
                    .encodedBytes(encodedLength));
        }

        for (int i = 0; i < elements.size(); i++) {
            Object element = elements.get(i);
            java.lang.reflect.Field[] declaredFields = element.getClass().getDeclaredFields();

            for (java.lang.reflect.Field innerField : declaredFields) {
                innerField.setAccessible(true);
                List<FieldDebugInfo> subInfos = processFieldRecursivelyWithDebug(element, innerField, "");
                for (FieldDebugInfo info : subInfos) {
                    builders.add(FieldDebugInfo.builder()
                            .fieldName(info.getFieldName())
                            .orderSuffix("[" + i + "]" + info.getOrderString())
                            .encodedBytes(info.getEncodedBytes()));
                }
            }
        }

        return builders;
    }

    private List<Object> toElementList(Object arrayOrList) {
        List<Object> elements = new ArrayList<>();
        if (arrayOrList.getClass().isArray()) {
            int len = Array.getLength(arrayOrList);
            for (int i = 0; i < len; i++) {
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
}

package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.analysis.FieldDebugInfo;
import net.deanly.structlayout.annotation.OptionalEncoding;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.annotation.StructTypeSelector;
import net.deanly.structlayout.codec.encode.StructEncoder;
import net.deanly.structlayout.codec.helpers.CalculateStructureSizeHelper;
import net.deanly.structlayout.dispatcher.StructTypeResolver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static net.deanly.structlayout.codec.encode.FieldProcessor.processFieldRecursivelyWithDebug;

public class StructObjectFieldHandler extends BaseFieldHandler {
    @Override
    public <T> byte[] handleField(T instance, Field field) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        // 1. 필드 값 추출
        Object fieldValue = extractFieldValue(instance, field);

        // 2. @StructObjectField 어노테이션 확인
        StructObjectField annotation = field.getAnnotation(StructObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @StructObjectField", field.getName())
            );
        }

        // Optional 처리
        OptionalEncoding opt = annotation.optional();

        if (opt == OptionalEncoding.BORSH) {
            if (fieldValue == null) {
                return new byte[]{0x00}; // None
            } else {
                byte[] encoded = StructEncoder.encode(fieldValue);
                byte[] result = new byte[1 + encoded.length];
                result[0] = 0x01; // Some
                System.arraycopy(encoded, 0, result, 1, encoded.length);
                return result;
            }
        }

        // Optional NONE
        if (fieldValue == null) {
            if (field.getType().getAnnotation(StructTypeSelector.class) != null){
                int span = StructTypeResolver.resolveNoDataSpan(field.getType());
                return new byte[span]; // Null 처리: 빈 배열
            } else {
                int span = CalculateStructureSizeHelper.calculateNoDataClassSize(field.getType());
                return new byte[span];
            }
        }

        // StructEncoder를 사용하여 재귀적으로 인코딩 처리
        return StructEncoder.encode(fieldValue);
    }

    @Override
    public <T> List<FieldDebugInfo.Builder> handleDebug(T instance, Field field) throws IllegalAccessException {
        Object fieldValue = extractFieldValue(instance, field);
        StructObjectField annotation = field.getAnnotation(StructObjectField.class);
        OptionalEncoding opt = annotation.optional();

        List<FieldDebugInfo.Builder> builders = new ArrayList<>();

        if (opt == OptionalEncoding.BORSH) {
            builders.add(FieldDebugInfo.builder()
                    .fieldName(field.getName())
                    .orderSuffix(".optional_tag")
                    .encodedBytes(new byte[]{(byte) ((fieldValue == null) ? 0x00 : 0x01)}));

            if (fieldValue == null) {
                return builders;
            }
        }

        if (fieldValue == null) {
            return builders; // optional = NONE 이면서 null인 경우 skip
        }

        for (Field innerField : fieldValue.getClass().getDeclaredFields()) {
            innerField.setAccessible(true);
            List<FieldDebugInfo> infos = processFieldRecursivelyWithDebug(fieldValue, innerField, "");
            for (FieldDebugInfo info : infos) {
                builders.add(FieldDebugInfo.builder()
                        .fieldName(info.getFieldName())
                        .orderSuffix(info.getOrderString())
                        .encodedBytes(info.getEncodedBytes()));
            }
        }

        return builders;
    }
}
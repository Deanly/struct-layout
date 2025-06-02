package net.deanly.structlayout.codec.decode;

import net.deanly.structlayout.analysis.DecodedFieldInfo;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.codec.helpers.FieldHelper;
import net.deanly.structlayout.factory.ClassFactory;
import net.deanly.structlayout.exception.*;
import net.deanly.structlayout.support.Tuple2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StructDecoder {

    public static <T> StructDecodeResult<T> decode(Class<T> type, byte[] data, int startOffset) {
        if (startOffset < 0 || startOffset >= data.length) {
            throw new InvalidDataOffsetException(startOffset, data.length);
        }

        // 1. 디코딩할 객체의 인스턴스 생성
        T instance = ClassFactory.createNoArgumentsInstance(type);

        // 2. 상속 계층의 모든 필드 수집 및 정렬
        List<Field> allFields = FieldHelper.getAllDeclaredFieldsIncludingSuperclasses(type);
        List<Field> orderedFields = FieldHelper.getOrderedFields(allFields);

        // 3. 디코딩: 필드 순서대로 Byte 데이터를 객체 필드에 매핑
        int offset = startOffset;

        for (Field field : orderedFields) {
            try {
                offset += FieldProcessor.processField(instance, field, data, offset);
            } catch (Exception ex) {
                // 예외 발생 시, 디버깅용으로 다시 순회하여 성공한 필드들 수집
                List<DecodedFieldInfo> decodedInfos = collectDecodedFields(type, data, startOffset, field);
                throw new StructDecodingException(type, field, offset, decodedInfos, ex);
            }
        }

        return StructDecodeResult.of(instance, offset - startOffset);
    }

    private static <T> List<DecodedFieldInfo> collectDecodedFields(Class<T> type, byte[] data, int startOffset, Field failedField) {
        List<Field> allFields = FieldHelper.getAllDeclaredFieldsIncludingSuperclasses(type);
        List<Tuple2<Field, Integer>> orderedFields = FieldHelper.getOrderedFieldsWithOrder(allFields);

        T instance = ClassFactory.createNoArgumentsInstance(type);
        int offset = startOffset;
        List<DecodedFieldInfo> decodedInfos = new ArrayList<>();

        for (Tuple2<Field, Integer> tuple : orderedFields) {
            Field field = tuple.first;
            int order = tuple.second;

            if (field.equals(failedField)) {
                // 실패한 필드에 도달하면 수집 중단
                break;
            }

            try {
                int before = offset;
                int consumed = FieldProcessor.processField(instance, field, data, offset);
                offset += consumed;

                byte[] fieldBytes = Arrays.copyOfRange(data, before, before + consumed);
                decodedInfos.add(new DecodedFieldInfo(field.getName(), order, before, fieldBytes));
            } catch (Exception ignored) {
                // collect 단계에서는 내부 오류 무시하고 로그 수집 목적만 수행
                break;
            }
        }

        return decodedInfos;
    }
}
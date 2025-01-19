package net.deanly.structlayout.codec.encode;

import net.deanly.structlayout.codec.helpers.ByteArrayHelper;
import net.deanly.structlayout.codec.helpers.FieldHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class StructEncoder {

    public static <T> byte[] encode(T instance) {
        if (instance == null) {
            return new byte[0]; // Null 객체는 빈 배열 반환
        }

        // 1. 필드 정렬
        Field[] fields = instance.getClass().getDeclaredFields();
        List<Field> orderedFields = FieldHelper.getOrderedFields(fields);

        // 2. 필드 별 byte[] 처리
        List<byte[]> fieldChunks = new ArrayList<>();
        for (Field field : orderedFields) {
            field.setAccessible(true);
            byte[] chunk = FieldProcessor.processField(instance, field);
            fieldChunks.add(chunk);
        }

        // 3. 생성된 byte[] 병합 및 반환
        return ByteArrayHelper.mergeChunks(fieldChunks);
    }
}
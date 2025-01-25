package net.deanly.structlayout.codec.encode;

import net.deanly.structlayout.analysis.FieldDebugInfo;
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


    public static <T> void encodeWithDebug(T instance) {
        if (instance == null) {
            return;
        }

        Field[] fields = instance.getClass().getDeclaredFields();
        List<Field> orderedFields = FieldHelper.getOrderedFields(fields);

        List<FieldDebugInfo> debugInfos = new ArrayList<>();

        for (Field field : orderedFields) {
            field.setAccessible(true);
            debugInfos.addAll(FieldProcessor.processFieldRecursivelyWithDebug(instance, field, null));
        }

        int offset = 0;

        // 헤더 출력: 각 컬럼의 길이에 맞춰 정렬
        System.out.printf("%-10s %-20s %-10s %s%n", "Order", "Field", "Offset", "Bytes (HEX)");

        // 구분선 출력
        System.out.println("=".repeat(60));

        for (FieldDebugInfo info : debugInfos) {
            System.out.printf(
                    "%-10s %-20s %07d     %s%n",
                    info.getOrderString(), // Order
                    info.getFieldName(),   // Field
                    offset,                // Offset
                    info.getEncodedBytesHex() // Bytes (HEX)
            );
            offset += info.getEncodedBytes().length;
        }

        System.out.println("=".repeat(60));
        System.out.printf("Total Bytes: %d%n", offset);
    }
}
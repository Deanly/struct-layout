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

        // 1. 상속 계층 필드 가져오기
        List<Field> allFields = getAllDeclaredFields(instance.getClass());

        // 2. 필드 정렬
        List<Field> orderedFields = FieldHelper.getOrderedFields(allFields);

        // 3. 필드 처리 및 병합
        List<byte[]> fieldChunks = new ArrayList<>();
        for (Field field : orderedFields) {
            field.setAccessible(true);
            byte[] chunk = FieldProcessor.processField(instance, field);
            fieldChunks.add(chunk);
        }

        // 4. 병합된 결과 반환
        return ByteArrayHelper.mergeChunks(fieldChunks);
    }

    /**
     * 상속 계층의 모든 필드를 가져오는 메서드
     */
    private static List<Field> getAllDeclaredFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (FieldHelper.isStructField(field)) { // `@Struct*` 관련 필드만 필터링
                    fields.add(field);
                }
            }
            clazz = clazz.getSuperclass(); // 부모 클래스로 이동
        }
        return fields;
    }


    public static <T> void encodeWithDebug(T instance) {
        if (instance == null) {
            return;
        }

        // 1. 상속 계층 필드 가져오기
        List<Field> allFields = getAllDeclaredFields(instance.getClass());

        // 2. 필드 정렬
        List<Field> orderedFields = FieldHelper.getOrderedFields(allFields);

        // 3. Debug 정보를 생성
        List<FieldDebugInfo> debugInfos = new ArrayList<>();
        for (Field field : orderedFields) {
            field.setAccessible(true);
            debugInfos.addAll(FieldProcessor.processFieldRecursivelyWithDebug(instance, field, null));
        }

        // 4. Debug 출력
        printDebugInfo(debugInfos);
    }

    private static void printDebugInfo(List<FieldDebugInfo> debugInfos) {
        int offset = 0;
        int totalBytes = 0;

        int maxOrderLength = "Order".length();
        int maxFieldNameLength = "Field".length();
        int maxOffsetLength = "Offset".length();

        for (FieldDebugInfo info : debugInfos) {
            maxOrderLength = Math.max(maxOrderLength, info.getOrderString().length());
            maxFieldNameLength = Math.max(maxFieldNameLength, info.getFieldName().length());
            maxOffsetLength = Math.max(maxOffsetLength, String.format("%07d", offset).length());
            offset += info.getEncodedBytes().length;
        }

        System.out.printf(
                "%-" + maxOrderLength + "s %-" + maxFieldNameLength + "s %-" + maxOffsetLength + "s %s%n",
                "Order", "Field", "Offset", "Bytes (HEX)"
        );
        System.out.println("=".repeat(maxOrderLength + maxFieldNameLength + maxOffsetLength + 20));

        offset = 0;
        for (FieldDebugInfo info : debugInfos) {
            System.out.printf(
                    "%-" + maxOrderLength + "s %-" + maxFieldNameLength + "s %0" + maxOffsetLength + "d %s%n",
                    info.getOrderString(),
                    info.getFieldName(),
                    offset,
                    info.getEncodedBytesHex()
            );
            offset += info.getEncodedBytes().length;
            totalBytes += info.getEncodedBytes().length;
        }
        System.out.println("=".repeat(maxOrderLength + maxFieldNameLength + maxOffsetLength + 20));
        System.out.printf("Total Bytes: %d%n", totalBytes);
    }
}
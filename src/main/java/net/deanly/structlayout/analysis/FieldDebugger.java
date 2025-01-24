package net.deanly.structlayout.analysis;

import net.deanly.structlayout.codec.decode.StructDecoder;
import net.deanly.structlayout.codec.encode.FieldProcessor;
import net.deanly.structlayout.codec.helpers.FieldHelper;
import net.deanly.structlayout.exception.StructParsingException;

import java.lang.reflect.Field;
import java.util.List;

public class FieldDebugger {

    public static void debugObjectFields(Object obj) {
        Class<?> clazz = obj.getClass();
        System.out.println("[Debug Object Fields: " + clazz.getSimpleName() + "]");

        // 1. 객체의 모든 필드 가져오기
        Field[] fields = clazz.getDeclaredFields();
        List<Field> orderedFields = FieldHelper.getOrderedFields(fields);

        for (Field field : orderedFields) {
            field.setAccessible(true);
            try {
                // 필드 값 인코딩
                byte[] encodedData = FieldProcessor.processField(obj, field);

                // 필드 디버깅 정보 출력
                System.out.println("  Field: " + field.getName());
                System.out.println("  Type:  " + field.getType().getSimpleName());
                System.out.println("  Data:  \n" + HexDumpUtil.toHexDump(encodedData));
            } catch (Exception e) {
                System.err.println("  [Error processing field: " + field.getName() + "] " + e.getMessage());
            }
        }
    }


//    public static <T> void debugByteArrayWithFields(byte[] data, Class<T> clazz) {
//        System.out.println("[Debug Byte Array With Fields: " + clazz.getSimpleName() + "]");
//
//        try {
//            // 1. 클래스 필드 정렬
//            Field[] fields = clazz.getDeclaredFields();
//            List<Field> orderedFields = FieldHelper.getOrderedFields(fields);
//
//            int offset = 0; // 데이터를 읽을 오프셋 초기화
//
//            // 2. 각 필드 처리
//            for (Field field : orderedFields) {
//                field.setAccessible(true);
//
//                try {
//                    // 2.1 필드에 할당된 데이터를 디코딩
//                    Object fieldValue = StructDecoder.decode(field.getClass(), data, offset);
//                    int fieldSize = FieldDecoder.getFieldSpan(data, offset, field);
//
//                    // 2.2 필드 정보 출력
//                    System.out.println("  Field: " + field.getName());
//                    System.out.println("  Type:  " + field.getType().getSimpleName());
//                    System.out.println("  Data:  " + HexDumpUtil.toHexDump(data, offset, fieldSize));
//                    System.out.println();
//
//                    // 2.3 다음 필드로 오프셋 이동
//                    offset += fieldSize;
//                } catch (Exception e) {
//                    System.err.println("  [Error processing field: " + field.getName() + "] " + e.getMessage());
//                }
//            }
//
//            // 3. 데이터가 남아있는 상태이면 경고 출력
//            if (offset < data.length) {
//                System.out.println("[Warning] Not all data was consumed during decoding. Remaining data:");
//                System.out.println(HexDumpUtil.toHexDump(data, offset, data.length - offset));
//            }
//        } catch (StructParsingException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new StructParsingException("Failed to debug byte array fields for class: " + clazz.getName(), e);
//        }
//    }
}
package net.deanly.structlayout.codec.helpers;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.*;
import net.deanly.structlayout.dispatcher.StructTypeResolver;
import net.deanly.structlayout.type.CountableField;
import net.deanly.structlayout.type.DynamicSpanField;

public class CalculateStructureSizeHelper {

    /**
     * Calculates the size of a serialized object in bytes by encoding it into a byte array.
     *
     * @param instance The object to be serialized and measured.
     * @return The size of the serialized object in bytes.
     */
    public static int calculateEncodedObjectSize(Object instance) {
        // StructLayout.encode 메소드를 활용하여 크기를 계산 (비효율)
        byte[] encodedData = StructLayout.encode(instance);
        return encodedData.length;
    }

    /**
     * Calculates the size of a class or structure in bytes.
     * The calculation is typically based on the memory layout of the class.
     *
     * @param type The class whose size needs to be calculated.
     * @return The size of the class in bytes.
     */
    public static int calculateClassSize(Class<?> type) {
        // TODO: impl
        return 0;
    }
    public static int calculateNoDataClassSize(Class<?> type) {
        int totalSize = 0;

        // 클래스에 선언된 모든 필드 순회
        for (java.lang.reflect.Field field : type.getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(StructField.class)) {
                // StructField 어노테이션 처리
                totalSize += calculateStructFieldNoDataSize(field);

            } else if (field.isAnnotationPresent(StructObjectField.class)) {
                // StructObjectField 어노테이션 처리 - 재귀 호출
                totalSize += calculateStructObjectFieldNoDataSize(field);

            } else if (field.isAnnotationPresent(StructSequenceField.class)) {
                // StructSequenceField 어노테이션 처리
                totalSize += calculateStructSequenceFieldNoDataSize(field);

            } else if (field.isAnnotationPresent(StructSequenceObjectField.class)) {
                // StructSequenceObjectField 어노테이션 처리
                totalSize += calculateStructSequenceObjectFieldNoDataSize(field);
            }
        }

        return totalSize;
    }


    private static int calculateStructFieldNoDataSize(java.lang.reflect.Field field) {
        try {
            StructField annotation = field.getAnnotation(StructField.class);
            Class<? extends Field<?>> fieldType = annotation.type();

            // Field 타입 가져오기
            Field<?> fieldInstance = fieldType.getDeclaredConstructor().newInstance();

            if (fieldInstance instanceof DynamicSpanField) {
                // DynamicSpanField의 경우 NoDataSpan 사용
                return ((DynamicSpanField) fieldInstance).getNoDataSpan();
            } else {
                // Static Field의 경우 Span 사용
                return fieldInstance.getSpan();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate size for @StructField on field: " + field.getName(), e);
        }
    }

    private static int calculateStructObjectFieldNoDataSize(java.lang.reflect.Field field) {
        try {
            StructObjectField annotation = field.getAnnotation(StructObjectField.class);
            Class<?> fieldType = field.getType();
            if (fieldType.isInterface() && fieldType.isAnnotationPresent(StructTypeSelector.class)) {
                return StructTypeResolver.resolveNoDataSpan(fieldType);
            }
            return calculateNoDataClassSize(fieldType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate size for @StructObjectField on field: " + field.getName(), e);
        }
    }

    private static int calculateStructSequenceFieldNoDataSize(java.lang.reflect.Field field) {
        try {
            StructSequenceField annotation = field.getAnnotation(StructSequenceField.class);

            // 시퀀스의 길이 타입 크기 계산
            Class<? extends Field<?>> lengthType = annotation.lengthType();
            Field<?> lengthFieldInstance = lengthType.getDeclaredConstructor().newInstance();
            int lengthTypeSize = lengthFieldInstance.getSpan();

            // 시퀀스 크기에 기본적으로 0개의 요소 처리
            return lengthTypeSize;
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate size for @StructSequenceField on field: " + field.getName(), e);
        }
    }

    private static int calculateStructSequenceObjectFieldNoDataSize(java.lang.reflect.Field field) {
        try {
            StructSequenceObjectField annotation = field.getAnnotation(StructSequenceObjectField.class);

            // 시퀀스의 길이 타입 크기 계산
            Class<? extends CountableField<?>> lengthType = annotation.lengthType();
            Field<?> lengthFieldInstance = lengthType.getDeclaredConstructor().newInstance();
            int lengthTypeSize = lengthFieldInstance.getSpan();

            // 시퀀스 크기에 기본적으로 0개의 요소 처리
            return lengthTypeSize;
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate size for @StructSequenceObjectField on field: " + field.getName(), e);
        }
    }
}

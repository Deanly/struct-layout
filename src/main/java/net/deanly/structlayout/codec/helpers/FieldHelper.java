package net.deanly.structlayout.codec.helpers;

import net.deanly.structlayout.annotation.CustomLayoutField;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.exception.FieldOrderException;
import net.deanly.structlayout.type.DataType;
import net.deanly.structlayout.type.helpers.DataTypeHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FieldHelper {

    /**
     * Returns a list of fields from the given array that are annotated with specific annotations
     * and orders them according to the order value defined in their annotations.
     *
     * The supported annotations are:
     * - {@code StructField}
     * - {@code SequenceField}
     * - {@code StructObjectField}
     * - {@code CustomLayoutField}
     *
     * @param fields the array of fields to process
     * @return a list of fields annotated with supported annotations, ordered by their {@code order} value
     */
    public static List<Field> getOrderedFields(Field[] fields) {
        List<Field> orderedFields = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(StructField.class) ||
                    field.isAnnotationPresent(SequenceField.class) ||
                    field.isAnnotationPresent(StructObjectField.class) ||
                    field.isAnnotationPresent(CustomLayoutField.class)) {
                orderedFields.add(field);
            }
        }
        orderedFields.sort(Comparator.comparingInt(FieldHelper::getOrderValue));
        return orderedFields;
    }

    /**
     * Retrieves the order value of the given field based on its annotation.
     * The method checks for specific annotations in the following order:
     * - StructField
     * - SequenceField
     * - StructObjectField
     * - CustomLayoutField
     *
     * If none of these annotations are present, a {@code FieldOrderException} is thrown.
     *
     * @param field the field for which the order value will be retrieved
     * @return the order value of the field if an applicable annotation is present
     * @throws FieldOrderException if the field does not have a valid order annotation
     */
    public static int getOrderValue(Field field) {
        if (field.isAnnotationPresent(StructField.class)) {
            return field.getAnnotation(StructField.class).order();
        } else if (field.isAnnotationPresent(SequenceField.class)) {
            return field.getAnnotation(SequenceField.class).order();
        } else if (field.isAnnotationPresent(StructObjectField.class)) {
            return field.getAnnotation(StructObjectField.class).order();
        } else if (field.isAnnotationPresent(CustomLayoutField.class)) {
            return field.getAnnotation(CustomLayoutField.class).order();
        }
        throw new FieldOrderException(field.getName());
    }


    public static boolean isFieldTypeApplicable(Class<?> structFieldType, DataType dataType) {
        // DataType의 필드 타입 가져오기
        Class<?> dataTypeFieldType = dataType.getFieldType();

        // 숫자형 변환 규칙 처리
        if (isNumericType(structFieldType) && isNumericType(dataTypeFieldType)) {
            return true; // 두 필드가 모두 숫자형이면 허용
        }

        // DataTypeHelper 활용: 필드 타입 매칭 여부 확인
        return DataTypeHelper.matches(dataType, structFieldType);
    }

    private static boolean isNumericType(Class<?> type) {
        return Number.class.isAssignableFrom(type) ||
                type == byte.class || type == short.class ||
                type == int.class || type == long.class ||
                type == float.class || type == double.class;
    }

    /**
     * 기본 배열과 DataType의 구성 요소 타입을 매칭합니다.
     */
    public static boolean isPrimitiveArrayApplicable(Class<?> componentType, DataType dataType) {
        Class<?> expectedType = dataType.getFieldType(); // DataType에서 필요한 Java 타입
        if (componentType.isPrimitive()) {
            // 기본 타입 배열의 경우, 구성 요소 타입과 예상 타입 매칭
            if ((componentType == byte.class && expectedType == Byte.class) ||
                    (componentType == short.class && expectedType == Short.class) ||
                    (componentType == int.class && expectedType == Integer.class) ||
                    (componentType == long.class && expectedType == Long.class) ||
                    (componentType == float.class && expectedType == Float.class) ||
                    (componentType == double.class && expectedType == Double.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 특정 요소 타입과 DataType의 예상 타입 매칭.
     */
    public static boolean isApplicableToDataType(Class<?> elementType, DataType dataType) {
        Class<?> expectedType = dataType.getFieldType();
        return elementType.equals(expectedType);
    }

    public static boolean isFieldTypeCompatible(Class<?> structFieldType, Class<?> dataTypeFieldType) {
        if (structFieldType == dataTypeFieldType) {
            return true; // 정확히 일치하는 타입
        }

        // 데이터 타입 크기 비교 (작으면 불일치로 간주)
        if (isNumericType(structFieldType) && isNumericType(dataTypeFieldType)) {
            int structFieldSize = getTypeSize(structFieldType);
            int dataTypeFieldSize = getTypeSize(dataTypeFieldType);
            return structFieldSize >= dataTypeFieldSize;
        }

        // 예외: 문자열 등 기타 호환되지 않는 항목
        return false;
    }

    private static int getTypeSize(Class<?> type) {
        if (type == Byte.class || type == byte.class) return 1;
        if (type == Short.class || type == short.class) return 2;
        if (type == Integer.class || type == int.class) return 4;
        if (type == Long.class || type == long.class) return 8;
        if (type == Float.class || type == float.class) return 4;
        if (type == Double.class || type == double.class) return 8;
        if (type == java.math.BigInteger.class) return 16; // 일반적으로 BigInteger는 16바이트 이상
        return 0; // 크기 비교 불가능한 타입
    }

}
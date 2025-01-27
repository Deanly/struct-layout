package net.deanly.structlayout.codec.helpers;

import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.annotation.StructSequenceObjectField;
import net.deanly.structlayout.exception.FieldOrderException;
import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.helpers.DataTypeHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class FieldHelper {

    public static final Set<Class<?>> PRIMITIVE_WRAPPERS = Set.of(
            Integer.class, Long.class, Short.class, Byte.class, Double.class, Float.class, Boolean.class, Character.class
    );

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
            if (field.isAnnotationPresent(StructSequenceField.class) ||
                    field.isAnnotationPresent(StructObjectField.class) ||
                    field.isAnnotationPresent(StructField.class) ||
                    field.isAnnotationPresent(StructSequenceObjectField.class)) {
                orderedFields.add(field);
            }
        }
        orderedFields.sort(Comparator.comparingInt(FieldHelper::getOrderValue));
        return orderedFields;
    }

    /**
     * Returns a list of fields from the given array that are annotated with specific annotations
     * and orders them according to the order value defined in their annotations.
     *
     * The supported annotations are:
     * - {@code StructSequenceField}
     * - {@code StructObjectField}
     * - {@code StructField}
     * - {@code StructSequenceObjectField}
     *
     * @param fields the list of fields to be filtered and ordered
     * @return a list of fields annotated with the supported annotations, ordered by their `order` value
     */
    public static List<Field> getOrderedFields(List<Field> fields) {
        List<Field> orderedFields = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(StructSequenceField.class) ||
                    field.isAnnotationPresent(StructObjectField.class) ||
                    field.isAnnotationPresent(StructField.class) ||
                    field.isAnnotationPresent(StructSequenceObjectField.class)) {
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
        if (field.isAnnotationPresent(StructSequenceField.class)) {
            return field.getAnnotation(StructSequenceField.class).order();
        } else if (field.isAnnotationPresent(StructObjectField.class)) {
            return field.getAnnotation(StructObjectField.class).order();
        } else if (field.isAnnotationPresent(StructField.class)) {
            return field.getAnnotation(StructField.class).order();
        } else if (field.isAnnotationPresent(StructSequenceObjectField.class)) {
            return field.getAnnotation(StructSequenceObjectField.class).order();
        }
        throw new FieldOrderException(field.getName());
    }

    /**
     * Checks if the given {@code Field} is annotated with any of the supported
     * struct-related annotations: {@code StructField}, {@code StructSequenceField},
     * {@code StructObjectField}, or {@code StructSequenceObjectField}.
     *
     * @param field the {@code Field} to be checked for struct-related annotations
     * @return {@code true} if the field is annotated with one of the supported
     *         struct-related annotations; {@code false} otherwise
     */
    public static boolean isStructField(Field field) {
        return field.isAnnotationPresent(StructField.class)
                || field.isAnnotationPresent(StructSequenceField.class)
                || field.isAnnotationPresent(StructObjectField.class)
                || field.isAnnotationPresent(StructSequenceObjectField.class);
    }

    /**
     * Retrieves all declared fields from the specified class and its superclasses,
     * filtering only those annotated with struct-related annotations.
     *
     * The method iterates through the class hierarchy, starting from the given
     * class and moving up to its superclasses, excluding {@code Object.class}.
     * For each class, it collects declared fields that satisfy the given filter criteria.
     *
     * @param clazz the class to inspect for declared fields, including fields
     *              from its superclasses
     * @return a list of fields that are declared in the class and its superclasses,
     *         and are annotated with struct-related annotations
     */
    public static List<Field> getAllDeclaredFieldsIncludingSuperclasses(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (isStructField(field)) { // `@Struct*` 어노테이션 필터링
                    fields.add(field);
                }
            }
            clazz = clazz.getSuperclass(); // 부모 클래스로 이동
        }
        return fields;
    }

    public static boolean isFieldTypeApplicable(Class<?> structFieldType, Class<? extends net.deanly.structlayout.Field<?>> fieldType) {
        // Field의 필드 타입 가져오기
        Class<?> dataTypeFieldType = FieldBase.getGenericTypeAsObject(fieldType);

        // 숫자형 변환 규칙 처리
        if (isNumericType(structFieldType) && isNumericType(dataTypeFieldType)) {
            return true; // 두 필드가 모두 숫자형이면 허용
        }

        // DataTypeHelper 활용: 필드 타입 매칭 여부 확인
        return DataTypeHelper.matches(fieldType, structFieldType);
    }

    private static boolean isNumericType(Class<?> type) {
        return Number.class.isAssignableFrom(type) ||
                type == byte.class || type == short.class ||
                type == int.class || type == long.class ||
                type == float.class || type == double.class;
    }

    /**
     * 기본 배열과 Field의 구성 요소 타입을 매칭합니다.
     */
    public static boolean isPrimitiveArrayApplicable(Class<?> componentType, Class<? extends net.deanly.structlayout.Field<?>> fieldType) {
        Class<?> expectedType = FieldBase.getGenericTypeAsObject(fieldType);
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
     * 특정 요소 타입과 Field의 예상 타입 매칭.
     */
    public static boolean isApplicableToDataType(Class<?> elementType, Class<? extends net.deanly.structlayout.Field<?>> fieldType) {
        Class<?> expectedType = FieldBase.getGenericTypeAsObject(fieldType);
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
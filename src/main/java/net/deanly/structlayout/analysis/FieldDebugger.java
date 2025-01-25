package net.deanly.structlayout.analysis;

import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.type.FieldBase;
import java.lang.reflect.Field;
import java.util.Collection;

public class FieldDebugger {

    public static void debugWithFields(Object struct) throws IllegalAccessException {
        if (struct == null) {
            System.out.println("[Object is null]");
            return;
        }

        Class<?> clazz = struct.getClass();
        Field[] fields = clazz.getDeclaredFields();

        System.out.println("[Debugging Struct Fields: " + clazz.getSimpleName() + "]");

        for (Field field : fields) {
            field.setAccessible(true); // Private 필드 접근 가능하게 설정

            // 필드 값 가져오기
            Object value = field.get(struct);

            if (value == null) {
                System.out.println("Field: " + field.getName() + " = null");
                continue;
            }

            // StructField와 StructSequenceField 등이 있는 필드에 대한 처리
            if (field.isAnnotationPresent(StructField.class) ||
                    field.isAnnotationPresent(StructSequenceField.class) ||
                    field.isAnnotationPresent(StructObjectField.class)) {

                System.out.println("Field: " + field.getName() + " -> " + value.getClass().getSimpleName());

                // FieldBase 타입일 경우
                if (FieldBase.class.isAssignableFrom(value.getClass())) {
                    debugFieldBase((FieldBase<?>) value, field);
                }
                // 배열 혹은 Collection 처리
                else if (Collection.class.isAssignableFrom(value.getClass())) {
                    debugCollectionField((Collection<?>) value, field);
                }
                // 다른 재귀적으로 평가 가능한 Struct 처리
                else {
                    debugWithFields(value);
                }
            }
        }
    }

    private static void debugFieldBase(FieldBase<?> fieldBase, Field field) {
        System.out.println("  [FieldBase Debug]");
        System.out.println("  Field: " + field.getName());
        System.out.println("  Type: " + fieldBase.getClass().getSimpleName());
        System.out.println("  HEX Debug: " + fieldBase.bytesToHex(new byte[fieldBase.getSpan()])); // 샘플
    }

    private static void debugCollectionField(Collection<?> collection, Field field) throws IllegalAccessException {
        System.out.println("  [SequenceField Debug]");
        System.out.println("  Field: " + field.getName());
        System.out.println("  Type: " + field.getType().getSimpleName());
        int index = 0;
        for (Object elem : collection) {
            System.out.println("  Element[" + index + "]: " + elem.getClass().getSimpleName());
            debugWithFields(elem); // 재귀적으로 각 요소 디버그
            index++;
        }
    }
}
package net.deanly.structlayout.codec.helpers;

import net.deanly.structlayout.annotation.CustomLayoutField;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.exception.FieldOrderException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FieldHelper {

    /**
     * 지정된 필드를 order 기준으로 정렬하여 반환
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
     * 필드의 order 값을 반환
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
}
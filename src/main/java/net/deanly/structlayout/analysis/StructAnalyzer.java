package net.deanly.structlayout.analysis;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.type.DataType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

/**
 * The StructAnalyzer class provides utilities to compute the total size of an
 * object based on its annotated fields. It processes fields annotated with
 * specific annotations, including {@code StructField}, {@code SequenceField},
 * and {@code StructObjectField}, to calculate the byte layout of an object
 * for structured serialization or other purposes.
 */
public class StructAnalyzer {

    /**
     * Computes the total size of the structure based on annotations.
     *
     * @param structInstance The annotated object instance.
     * @return The total size in bytes.
     */
    public static int calculateSize(Object structInstance) {
        Class<?> structClass = structInstance.getClass();
        int totalSize = 0;

        // Iterate over all fields in the class
        for (Field field : structClass.getDeclaredFields()) {

            // Process each annotation type
            if (field.isAnnotationPresent(StructField.class)) {
                Object fieldValue = getFieldValue(structInstance, field);
                totalSize += calculateStructFieldSize(field);

            } else if (field.isAnnotationPresent(SequenceField.class)) {
                Object fieldValue = getFieldValue(structInstance, field);
                totalSize += calculateSequenceFieldSize(field, fieldValue);

            } else if (field.isAnnotationPresent(StructObjectField.class)) {
                Object fieldValue = getFieldValue(structInstance, field);
                totalSize += calculateStructObjectFieldSize(field, fieldValue);
            }
        }

        return totalSize;
    }

    private static int calculateStructFieldSize(Field field) {
        StructField structField = field.getAnnotation(StructField.class);
        DataType dataType = structField.dataType();

        // Use layout mapping to determine size.
        Layout<?> layout = CachedLayoutProvider.getLayout(dataType);
        return layout.getSpan();
    }

    private static int calculateSequenceFieldSize(Field field, Object fieldValue) {
        if (!(fieldValue instanceof Collection<?> collection)) {
            throw new IllegalArgumentException(String.format(
                    "Field '%s' must be a collection for @SequenceField.", field.getName()));
        }

        SequenceField sequenceField = field.getAnnotation(SequenceField.class);

        // Length bytes (if defined) add to the total size
        int totalSize = 0;

        DataType lengthType = sequenceField.lengthType();
        if (lengthType != null) {
            Layout<?> lengthLayout = CachedLayoutProvider.getLayout(lengthType);
            totalSize += lengthLayout.getSpan();
        }

        // Elements' Span multiplied by collection size
        DataType elementType = sequenceField.elementType();
        Layout<?> elementLayout = CachedLayoutProvider.getLayout(elementType);

        totalSize += elementLayout.getSpan() * collection.size();
        return totalSize;
    }

    private static int calculateStructObjectFieldSize(Field field, Object fieldValue) {
        // Ensure the field value is not null
        if (fieldValue == null) {
            return 0;
        }

        // Recursively calculate size of nested object
        return calculateSize(fieldValue);
    }

    private static boolean shouldProcessField(Field field) {
        // Ignore synthetic, static, or transient fields (e.g., JVM-internal fields)
        if (field.isSynthetic() || Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
            return false;
        }

        // Only process fields from the expected user-defined class, not from system classes
        String className = field.getDeclaringClass().getName();
        // 여기선 패키지를 제한하거나 특정 클래스만 접근하도록 추가 조건 필요
        if (className.startsWith("java.") || className.startsWith("javax.")) {
            return false;
        }

        return true;
    }

    /**
     * Attempts to retrieve a field's value, falling back to direct access if a getter method is unavailable.
     */
    private static Object getFieldValue(Object instance, Field field) {
        try {
            String fieldName = field.getName();
            String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

            // Invoke getter if it exists, otherwise use field directly
            try {
                Method getterMethod = instance.getClass().getMethod(getterName);
                return getterMethod.invoke(instance);
            } catch (NoSuchMethodException e) {
                // Fallback to direct field access
                if (!field.canAccess(instance)) {
                    field.setAccessible(true);
                }
                return field.get(instance);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error accessing field: " + field.getName(), e);
        }
    }
}
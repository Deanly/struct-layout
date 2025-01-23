package net.deanly.structlayout.analysis;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.type.basic.CountableType;

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
        for (java.lang.reflect.Field field : structClass.getDeclaredFields()) {

            // Process each annotation type
            if (field.isAnnotationPresent(StructField.class)) {
                Object fieldValue = getFieldValue(structInstance, field);
                totalSize += calculateStructFieldSize(field);

            } else if (field.isAnnotationPresent(StructSequenceField.class)) {
                Object fieldValue = getFieldValue(structInstance, field);
                totalSize += calculateSequenceFieldSize(field, fieldValue);

            } else if (field.isAnnotationPresent(StructObjectField.class)) {
                Object fieldValue = getFieldValue(structInstance, field);
                totalSize += calculateStructObjectFieldSize(field, fieldValue);
            }
        }

        return totalSize;
    }

    private static int calculateStructFieldSize(java.lang.reflect.Field field) {
        StructField basicStructField = field.getAnnotation(StructField.class);
        Class<? extends Field<?>> dataType = basicStructField.type();

        // Use layout mapping to determine size.
        Field<?> layout = CachedLayoutProvider.getLayout(dataType);
        return layout.getSpan();
    }

    private static int calculateSequenceFieldSize(java.lang.reflect.Field field, Object fieldValue) {
        if (!(fieldValue instanceof Collection<?> collection)) {
            throw new IllegalArgumentException(String.format(
                    "Field '%s' must be a collection for @StructSequenceField.", field.getName()));
        }
        StructSequenceField structSequenceField = field.getAnnotation(StructSequenceField.class);

        // Length bytes (if defined) add to the total size
        int totalSize = 0;

        Class<? extends CountableType> lengthType = structSequenceField.lengthType();
        if (lengthType != null) {
            @SuppressWarnings("unchecked")
            Field<?> lengthField = CachedLayoutProvider.getLayout((Class<? extends Field<?>>) lengthType);
            totalSize += lengthField.getSpan();
        }

        // Fetch Class for element type and resolve Layout
        Class<? extends Field<?>> elementClass = structSequenceField.elementType();
        Field<?> elementField = CachedLayoutProvider.getLayout(elementClass);

        // Elements' Span multiplied by collection size
        totalSize += elementField.getSpan() * collection.size();
        return totalSize;
    }

    private static int calculateStructObjectFieldSize(java.lang.reflect.Field field, Object fieldValue) {
        // Ensure the field value is not null
        if (fieldValue == null) {
            return 0;
        }

        // Recursively calculate size of nested object
        return calculateSize(fieldValue);
    }

    private static boolean shouldProcessField(java.lang.reflect.Field field) {
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
    private static Object getFieldValue(Object instance, java.lang.reflect.Field field) {
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
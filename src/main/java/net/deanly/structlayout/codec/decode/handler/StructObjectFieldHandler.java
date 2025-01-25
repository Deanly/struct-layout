package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.StructLayout;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.codec.decode.StructDecoder;

public class StructObjectFieldHandler extends BaseFieldHandler {

    @Override
    public <T> int handleField(T instance, java.lang.reflect.Field field, byte[] data, int offset) throws IllegalAccessException {
        StructObjectField annotation = field.getAnnotation(StructObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @StructObjectField", field.getName())
            );
        }

        Class<?> nestedType = field.getType();

        Object nestedInstance = StructDecoder.decode(nestedType, data, offset).getValue();

        field.setAccessible(true);
        field.set(instance, nestedInstance);

        return calculateEncodedObjectSize(nestedType, nestedInstance);
    }

    /**
     * 중첩된 객체의 크기를 계산
     */
    private static int calculateEncodedObjectSize(Class<?> type, Object instance) {
        // StructLayout.encode 메소드를 활용하여 크기를 계산
        byte[] encodedData = StructLayout.encode(instance);
        return encodedData.length;
    }
}
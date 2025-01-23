package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;

public class StructFieldHandler extends BaseFieldHandler {

    @Override
    public <T> int handleField(T instance, java.lang.reflect.Field field, byte[] data, int offset) throws IllegalAccessException {
        StructField annotation = field.getAnnotation(StructField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @CustomLayoutField", field.getName())
            );
        }

        Field<?> layout = createLayoutInstance(annotation.type());

        Object decodedValue = layout.decode(data, offset);

        Object targetValue = TypeConverterHelper.convertToType(decodedValue, field.getType());

        field.setAccessible(true);
        field.set(instance, targetValue);

        return layout.getSpan();
    }

    /**
     * Creates an instance of the layout class provided in the annotation.
     */
    private Field<?> createLayoutInstance(Class<? extends Field<?>> layoutClass) {
        try {
            return layoutClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Failed to instantiate custom layout class '%s'", layoutClass.getName()), e
            );
        }
    }
}
package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.annotation.CustomLayoutField;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;

import java.lang.reflect.Field;

public class CustomLayoutFieldHandler extends BaseFieldHandler {

    @Override
    public <T> int handleField(T instance, Field field, byte[] data, int offset) throws IllegalAccessException {
        CustomLayoutField annotation = field.getAnnotation(CustomLayoutField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @CustomLayoutField", field.getName())
            );
        }

        Layout<?> layout = createLayoutInstance(annotation.layout());

        Object decodedValue = layout.decode(data, offset);

        Object targetValue = TypeConverterHelper.convertToType(decodedValue, field.getType());

        field.setAccessible(true);
        field.set(instance, targetValue);

        return layout.getSpan();
    }

    /**
     * Creates an instance of the layout class provided in the annotation.
     */
    private Layout<?> createLayoutInstance(Class<? extends Layout<?>> layoutClass) {
        try {
            return layoutClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Failed to instantiate custom layout class '%s'", layoutClass.getName()), e
            );
        }
    }
}
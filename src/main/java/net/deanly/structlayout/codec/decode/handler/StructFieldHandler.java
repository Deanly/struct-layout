package net.deanly.structlayout.codec.decode.handler;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.annotation.OptionalEncoding;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.codec.helpers.TypeConverterHelper;
import net.deanly.structlayout.type.DynamicSpanField;

public class StructFieldHandler extends BaseFieldHandler {

    @Override
    public <T> int handleField(T instance, java.lang.reflect.Field field, byte[] data, int offset) throws IllegalAccessException {
        StructField annotation = field.getAnnotation(StructField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @CustomLayoutField", field.getName())
            );
        }

        OptionalEncoding optional = annotation.optional();

        // Handle BORSH Optional prefix
        if (optional == OptionalEncoding.BORSH) {
            if (!isValuePresent(data, offset, optional)) {
                field.setAccessible(true);
                field.set(instance, null);
                return 1; // only prefix byte consumed
            }
            offset += 1; // skip prefix
        }

        Field<?> layout = createLayoutInstance(annotation.type());
        Object decodedValue = layout.decode(data, offset);
        Object targetValue = TypeConverterHelper.convertToType(decodedValue, field.getType());

        field.setAccessible(true);
        field.set(instance, targetValue);

        if (layout instanceof DynamicSpanField) {
            return ((DynamicSpanField) layout).calculateSpan(data, offset) + (optional == OptionalEncoding.BORSH ? 1 : 0);
        } else {
            return layout.getSpan() + (optional == OptionalEncoding.BORSH ? 1 : 0);
        }
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
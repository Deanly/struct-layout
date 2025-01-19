package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.DataType;

import java.lang.reflect.Field;

public class StructFieldHandler extends BaseFieldHandler {
    @Override
    protected DataType resolveDataType(Field field) {
        StructField annotation = field.getAnnotation(StructField.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Field does not have @StructField annotation");
        }
        return annotation.dataType();
    }
}
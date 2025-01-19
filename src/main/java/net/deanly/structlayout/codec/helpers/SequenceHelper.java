package net.deanly.structlayout.codec.helpers;

import net.deanly.structlayout.exception.InvalidSequenceTypeException;

import java.util.ArrayList;
import java.util.List;

public class SequenceHelper {

    public static List<Object> processSequenceField(Object rawSequence, String fieldName) {
        List<Object> elements = new ArrayList<>();
        if (rawSequence instanceof List<?>) {
            elements.addAll((List<?>) rawSequence);
        } else if (rawSequence.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(rawSequence);
            for (int i = 0; i < length; i++) {
                Object element = java.lang.reflect.Array.get(rawSequence, i);
                elements.add(element);
            }
        } else {
            throw new InvalidSequenceTypeException(fieldName, rawSequence.getClass());
        }
        return elements;
    }
}
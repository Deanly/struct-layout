package net.deanly.structlayout.codec.encode.handler;

import net.deanly.structlayout.analysis.FieldDebugInfo;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.annotation.StructTypeSelector;
import net.deanly.structlayout.codec.encode.StructEncoder;
import net.deanly.structlayout.codec.helpers.CalculateStructureSizeHelper;
import net.deanly.structlayout.dispatcher.StructTypeResolver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class StructObjectFieldHandler extends BaseFieldHandler {
    @Override
    public <T> byte[] handleField(T instance, Field field) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        // 1. 필드 값 추출
        Object fieldValue = extractFieldValue(instance, field);

        // 2. @StructObjectField 어노테이션 확인
        StructObjectField annotation = field.getAnnotation(StructObjectField.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("Field '%s' is not annotated with @StructObjectField", field.getName())
            );
        }

        // 3. 필드 값이 null인지 확인
        if (fieldValue == null) {
            if (field.getType().getAnnotation(StructTypeSelector.class) != null){
                int span = StructTypeResolver.resolveNoDataSpan(field.getType());
                return new byte[span]; // Null 처리: 빈 배열
            } else {
                int span = CalculateStructureSizeHelper.calculateNoDataClassSize(field.getType());
                return new byte[span];
            }
        }

        // 4. StructEncoder를 사용하여 재귀적으로 인코딩 처리
        return StructEncoder.encode(fieldValue);
    }

    @Override
    public <T> List<FieldDebugInfo.Builder> handleDebug(T instance, Field field) throws IllegalAccessException {
        return List.of();
    }
}
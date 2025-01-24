package net.deanly.structlayout.codec.decode;

import lombok.extern.slf4j.Slf4j;
import net.deanly.structlayout.codec.helpers.FieldHelper;
import net.deanly.structlayout.factory.ClassFactory;
import net.deanly.structlayout.exception.*;

import java.lang.reflect.Field;
import java.util.List;

@Slf4j
public class StructDecoder {
    public static <T> StructDecodeResult<T> decode(Class<T> type, byte[] data, int startOffset) {
        if (startOffset < 0 || startOffset >= data.length) {
            throw new InvalidDataOffsetException(startOffset, data.length);
        }

        // 1. 인스턴스 생성
        T instance = ClassFactory.createNoArgumentsInstance(type);

        // 2. 필드 정렬
        Field[] fields = type.getDeclaredFields();
        List<Field> orderedFields = FieldHelper.getOrderedFields(fields);

        // 3. 디코딩 처리
        int offset = startOffset;
        for (Field field : orderedFields) {
            offset += FieldProcessor.processField(instance, field, data, offset);
        }

        return StructDecodeResult.of(instance, offset);
    }
}
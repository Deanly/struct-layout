package net.deanly.structlayout.type.advanced;

import lombok.Getter;
import net.deanly.structlayout.type.FieldBase;
import net.deanly.structlayout.type.DynamicSpanField;

@Getter
public class OffsetField<T> extends FieldBase<T> implements DynamicSpanField {
    private final FieldBase<T> field;
    private final int offset;
    private int span;

    public OffsetField(FieldBase<T> field, int offset) {
        super(field instanceof DynamicSpanField ? -1 : field.getSpan(), field.getValueType());
        if (field == null) {
            throw new IllegalArgumentException("Base layout must not be null.");
        }
        this.field = field;
        this.offset = offset;
    }

    @Override
    public int calculateSpan(byte[] data, int currentOffset) {
        return field instanceof DynamicSpanField
                ? ((DynamicSpanField) field).calculateSpan(data, currentOffset + offset)
                : field.getSpan();
    }

    @Override
    public int getNoDataSpan() {
        return 0;
    }

    @Override
    public T decode(byte[] data, int currentOffset) {
        int adjustedOffset = currentOffset + this.offset;
        if (adjustedOffset < 0 || adjustedOffset >= data.length) {
            throw new IllegalArgumentException("Offset is out of bounds.");
        }
        return field.decode(data, adjustedOffset); // 참조 레이아웃을 통해 디코딩
    }

    @Override
    public byte[] encode(T value) {
        return field.encode(value); // 참조 레이아웃과 동일하게 인코딩
    }
}

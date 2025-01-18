package net.deanly.structlayout.type.ext;

import lombok.Getter;
import net.deanly.structlayout.Layout;

@Getter
public class OffsetLayout<T> extends Layout<T> implements DynamicSpanLayout {
    private final Layout<T> layout;
    private final int offset;

    public OffsetLayout(Layout<T> layout, int offset, String property) {
        super(layout instanceof DynamicSpanLayout ? -1 : layout.getSpan(), property); // Dynamic Span 지원
        if (layout == null) {
            throw new IllegalArgumentException("Base layout must not be null.");
        }
        this.layout = layout;
        this.offset = offset;
    }

    @Override
    public int calculateSpan(byte[] data, int currentOffset) {
        return layout instanceof DynamicSpanLayout
                ? ((DynamicSpanLayout) layout).calculateSpan(data, currentOffset + offset)
                : layout.getSpan();
    }

    @Override
    public T decode(byte[] data, int currentOffset) {
        int adjustedOffset = currentOffset + this.offset;
        if (adjustedOffset < 0 || adjustedOffset >= data.length) {
            throw new IllegalArgumentException("Offset is out of bounds.");
        }
        return layout.decode(data, adjustedOffset); // 참조 레이아웃을 통해 디코딩
    }

    @Override
    public byte[] encode(T value) {
        return layout.encode(value); // 참조 레이아웃과 동일하게 인코딩
    }
}

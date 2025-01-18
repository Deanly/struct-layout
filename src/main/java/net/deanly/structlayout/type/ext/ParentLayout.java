package net.deanly.structlayout.type.ext;

import lombok.Getter;
import net.deanly.structlayout.Layout;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class ParentLayout<T> extends Layout<T> {
    private final List<Layout<?>> childLayouts = new ArrayList<>(); // 하위 레이아웃들

    public ParentLayout(int span, String property) {
        super(span, property);
    }

    /**
     * Add a child layout to this parent.
     *
     * @param layout The child layout to be added.
     */
    public void addChild(Layout<?> layout) {
        childLayouts.add(layout);
    }

    /**
     * Extendable method to process or retrieve data for child layouts.
     */
    public abstract byte[] getDataForChild(Layout<?> child);
}
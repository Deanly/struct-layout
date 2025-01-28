package net.deanly.structlayout.type.advanced;

import lombok.Getter;
import net.deanly.structlayout.type.FieldBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a parent layout that can contain multiple child layouts.
 * This class extends the Layout class and provides additional functionality
 * for managing and processing child layouts. It serves as a container for
 * layouts that can work together, while allowing child layouts to implement
 * their specific encoding and decoding logic.
 *
 * @param <T> The type of the value handled by this ParentLayout.
 */
@Getter
public abstract class ParentField<T> extends FieldBase<T> {
    private final List<FieldBase<?>> childFields = new ArrayList<>(); // 하위 레이아웃들

    /**
     * Constructs a ParentLayout with a specified span and property.
     *
     * @param span The number of bytes that this layout will process. This value
     *             is critical as it defines the number of bytes processed during
     *             encoding/decoding and validation offsets.
     * @param property An optional property name associated with the layout, which
     *                 can be used for debugging or mapping.
     */
    public ParentField(int span) {
        super(span);
    }

    /**
     * Adds a child layout to the parent layout's collection of child layouts.
     *
     * @param field The child layout to be added. This layout is an instance
     *               of the generic Layout class and may define its specific
     *               encoding and decoding logic.
     */
    public void addChild(FieldBase<?> field) {
        childFields.add(field);
    }

    /**
     * Retrieves the data associated with a specific child layout within a parent layout.
     *
     * @param child The child layout for which the data needs to be retrieved.
     *              This layout must be a part of the parent layout's collection of child layouts.
     * @return A byte array representing the data associated with the specified child layout.
     */
    public abstract byte[] getDataForChild(FieldBase<?> child);
}
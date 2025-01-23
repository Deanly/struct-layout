package net.deanly.structlayout.analysis;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.exception.LayoutInitializationException;
import net.deanly.structlayout.factory.ClassFactory;
import net.deanly.structlayout.type.DynamicSpanField;
import net.deanly.structlayout.type.basic.*;

import java.util.HashMap;
import java.util.Map;

public class CachedLayoutProvider {

    // 정적 타입 캐싱
    private static final Map<Class<? extends Field<?>>, Field<?>> layoutCache = new HashMap<>();

    static {
        // 정수 타입 (Integer Types)
        layoutCache.put(UInt8Field.class, new UInt8Field());
        layoutCache.put(Int8Field.class, new Int8Field());

        layoutCache.put(UInt16LEField.class, new UInt16LEField());
        layoutCache.put(UInt16BEField.class, new UInt16BEField());
        layoutCache.put(Int16LEField.class, new Int16LEField());
        layoutCache.put(Int16BEField.class, new Int16BEField());

        layoutCache.put(UInt32LEField.class, new UInt32LEField());
        layoutCache.put(UInt32BEField.class, new UInt32BEField());
        layoutCache.put(Int32LEField.class, new Int32LEField());
        layoutCache.put(Int32BEField.class, new Int32BEField());

        layoutCache.put(UInt64LEField.class, new UInt64LEField());
        layoutCache.put(UInt64BEField.class, new UInt64BEField());
        layoutCache.put(Int64LEField.class, new Int64LEField());
        layoutCache.put(Int64BEField.class, new Int64BEField());

        // 부동소수점 타입 (Floating-Point Types)
        layoutCache.put(Float32LEField.class, new Float32LEField());
        layoutCache.put(Float32BEField.class, new Float32BEField());
        layoutCache.put(Float64LEField.class, new Float64LEField());
        layoutCache.put(Float64BEField.class, new Float64BEField());

        // 문자 타입 (Character Types)
        layoutCache.put(CharCField.class, new CharCField());
        layoutCache.put(UCharCField.class, new UCharCField());
    }

    /**
     * Retrieves a cached instance of a layout for the given {@code BasicType}. If no cached
     * instance exists, it attempts to create a new instance of the layout.
     *
     * @param <T> The type of the value being processed by the layout.
     * @param basicType The {@code BasicType} for which the layout is to be retrieved.
     *        This parameter specifies the type of data the layout will process.
     * @return An instance of the {@code Field} associated with the provided {@code BasicType}.
     *         If a cached layout exists, it is returned; otherwise, a new instance is created.
     * @throws LayoutInitializationException If the layout cannot be retrieved or instantiated.
     */
    @SuppressWarnings("unchecked")
    public static <T> Field<T> getLayout(BasicType basicType) {
        try {
            if (!(basicType instanceof Field<?>)) {
                throw new IllegalArgumentException("Provided BasicType does not implement Field: " + basicType.getClass().getName());
            }
            Class<? extends Field<?>> fieldClass = (Class<? extends Field<?>>) basicType.getClass();
            return getLayout(fieldClass);

        } catch (Exception e) {
            throw new LayoutInitializationException("Failed to create Layout for BasicType: " + basicType, e);
        }
    }

    /**
     * Retrieves a cached instance of the specified layout class or creates a new instance if not already cached.
     *
     * @param <T> The type of the value being processed by the layout.
     * @param layoutClass The class of the layout to retrieve. The layout class must extend {@code Field}.
     * @return An instance of the requested layout class. If the layout is cached, the cached instance is returned.
     *         Otherwise, a new instance is created using {@code ClassFactory.createLayoutInstance}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Field<T> getLayout(Class<? extends Field<?>> layoutClass) {
        Field<?> cachedField = layoutCache.get(layoutClass);
        if (cachedField != null) {
            return (Field<T>) cachedField;
        }

        Field<T> field = ClassFactory.createLayoutInstance(layoutClass);

        if (DynamicSpanField.class.isAssignableFrom(layoutClass)) {
            layoutCache.put(layoutClass, field);
        }

        return field;
    }

}
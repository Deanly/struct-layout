package net.deanly.structlayout.analysis;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.exception.LayoutInitializationException;
import net.deanly.structlayout.factory.ClassFactory;
import net.deanly.structlayout.type.DataType;
import net.deanly.structlayout.type.basic.*;

import java.util.HashMap;
import java.util.Map;

public class CachedLayoutProvider {

    // 정적 타입 캐싱
    private static final Map<DataType, Layout<?>> layoutCache = new HashMap<>();

    static {
        // 정수 타입 (Integer Types)
        layoutCache.put(DataType.UINT8, new UInt8Layout());
        layoutCache.put(DataType.INT8, new Int8Layout());

        layoutCache.put(DataType.UINT16_LE, new UInt16LELayout());
        layoutCache.put(DataType.UINT16_BE, new UInt16BELayout());
        layoutCache.put(DataType.INT16_LE, new Int16LELayout());
        layoutCache.put(DataType.INT16_BE, new Int16BELayout());

        layoutCache.put(DataType.UINT32_LE, new UInt32LELayout());
        layoutCache.put(DataType.UINT32_BE, new UInt32BELayout());
        layoutCache.put(DataType.INT32_LE, new Int32LELayout());
        layoutCache.put(DataType.INT32_BE, new Int32BELayout());

        layoutCache.put(DataType.UINT64_LE, new UInt64LELayout());
        layoutCache.put(DataType.UINT64_BE, new UInt64BELayout());
        layoutCache.put(DataType.INT64_LE, new Int64LELayout());
        layoutCache.put(DataType.INT64_BE, new Int64BELayout());

        // 부동소수점 타입 (Floating-Point Types)
        layoutCache.put(DataType.FLOAT32_LE, new Float32LELayout());
        layoutCache.put(DataType.FLOAT32_BE, new Float32BELayout());
        layoutCache.put(DataType.FLOAT64_LE, new Float64LELayout());
        layoutCache.put(DataType.FLOAT64_BE, new Float64BELayout());

        // 문자 타입 (Character Types)
        layoutCache.put(DataType.CHAR_C, new CharCLayout());
        layoutCache.put(DataType.UCHAR_C, new UCharCLayout());
    }

    /**
     * Retrieves a Layout instance corresponding to the specified DataType. If the layout
     * instance is not already cached, it will attempt to create a new instance using the
     * layout class provided by the DataType.
     *
     * @param <T> The type of the value being processed by the Layout.
     * @param dataType The DataType for which the Layout needs to be retrieved.
     *                 Must not be null and must provide a valid layout definition.
     * @return A Layout instance associated with the specified DataType.
     *         Never null unless an unexpected error occurs.
     * @throws LayoutInitializationException If the Layout cannot be instantiated or
     *                                       there is an error during initialization.
     */
    @SuppressWarnings("unchecked")
    public static <T> Layout<T> getLayout(DataType dataType) {
        try {
            Layout<T> layout = (Layout<T>) layoutCache.get(dataType);

            if (layout == null) {
                layout = (Layout<T>) dataType.getLayout().getDeclaredConstructor().newInstance();
            }

            return layout;
        } catch (Exception e) {
            throw new LayoutInitializationException("Failed to create Layout for DataType: " + dataType, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Layout<T> getLayout(Class<? extends Layout<?>> layoutClass) {
        return ClassFactory.createLayoutInstance(layoutClass);
    }

}
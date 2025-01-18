package net.deanly.structlayout.analysis;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.exception.LayoutInitializationException;
import net.deanly.structlayout.type.DataType;
import net.deanly.structlayout.type.impl.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class DataTypeMapping {

    // 정적 타입 캐싱
    private static final Map<DataType, Layout<?>> layoutCache = new HashMap<>();

    static {
        // 정수 타입 (Integer Types)
//        layoutCache.put(DataType.UINT8_LE, new UInt8LELayout());
//        layoutCache.put(DataType.UINT8_BE, new UInt8BELayout());
//        layoutCache.put(DataType.INT8_LE, new Int8LELayout());
//        layoutCache.put(DataType.INT6_BE, new Int8BELayout());

//        layoutCache.put(DataType.UINT16_LE, new UInt16LELayout());
//        layoutCache.put(DataType.UINT16_BE, new UInt16BELayout());
//        layoutCache.put(DataType.INT16_LE, new Int16LELayout());
//        layoutCache.put(DataType.INT16_BE, new Int16BELayout());

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
//        layoutCache.put(DataType.CHAR_C, new CharCLayout());
//        layoutCache.put(DataType.UCHAR_C, new UCharCLayout());

        // 문자열 타입 (String Types)
    }

    /**
     * 주어진 DataType에 맞는 Layout을 가져옵니다.
     *
     * @param dataType DataType (엔디안을 포함한 데이터 타입)
     * @return Layout<T> 인스턴스
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
        try {
            return (Layout<T>) layoutClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            // No-Arguments 생성자가 없는 경우
            throw new LayoutInitializationException("The Layout class '"
                    + layoutClass.getName()
                    + "' must have a public no-arguments constructor.", e);
        } catch (Exception e) {
            // 다른 예외들 (예: 접근 불가, 인스턴스화 실패 등)
            throw new LayoutInitializationException("Failed to instantiate Layout class: "
                    + layoutClass.getName(), e);
        }
    }

}
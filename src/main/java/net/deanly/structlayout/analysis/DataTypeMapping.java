package net.deanly.structlayout.analysis;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.type.DataType;
import net.deanly.structlayout.type.impl.*;

import java.util.HashMap;
import java.util.Map;

public class DataTypeMapping {

    // 매핑 테이블: DataType -> Layout 인스턴스
    private static final Map<DataType, Layout<?>> layoutCache = new HashMap<>();

    static {
        // 정수 타입 (Integer Types) - 초기화된 Layout 객체를 생성 및 캐싱
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
        layoutCache.put(DataType.STRING_C, new CStringLayout()); // C 문자열 (null-terminated)
    }

    /**
     * 주어진 DataType에 맞는 Layout 인스턴스를 반환합니다.
     *
     * @param dataType DataType (엔디안을 포함한 데이터 타입)
     * @return Layout<T> 캐싱된 인스턴스
     */
    @SuppressWarnings("unchecked")
    public static <T> Layout<T> getLayout(DataType dataType) {
        Layout<?> layout = layoutCache.get(dataType);
        if (layout == null) {
            throw new IllegalArgumentException("Unsupported DataType: " + dataType);
        }
        return (Layout<T>) layout; // 안전한 타입 캐스팅
    }
}
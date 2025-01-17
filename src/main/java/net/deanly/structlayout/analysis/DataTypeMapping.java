package net.deanly.structlayout.analysis;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.type.DataType;
import net.deanly.structlayout.type.impl.*;

import java.util.HashMap;
import java.util.Map;

public class DataTypeMapping {

    // 매핑 테이블: 데이터 타입(DataType) -> Layout 클래스
    private static final Map<DataType, Class<? extends Layout<?>>> mappingTable = new HashMap<>();
    private static final Map<DataType, Layout<?>> layoutCache = new HashMap<>();

    static {
        // 정수 타입 (Integer Types)
//        mappingTable.put(DataType.UINT8_LE, UInt8LELayout.class);
//        mappingTable.put(DataType.UINT8_BE, UInt8BELayout.class);
//        mappingTable.put(DataType.INT8_LE, Int8LELayout.class);
//        mappingTable.put(DataType.INT6_BE, Int8BELayout.class);
//        layoutCache.put(DataType.UINT8_LE, new UInt8LELayout());
//        layoutCache.put(DataType.UINT8_BE, new UInt8BELayout());
//        layoutCache.put(DataType.INT8_LE, new Int8LELayout());
//        layoutCache.put(DataType.INT6_BE, new Int8BELayout());

//        mappingTable.put(DataType.UINT16_LE, UInt16LELayout.class);
//        mappingTable.put(DataType.UINT16_BE, UInt16BELayout.class);
//        mappingTable.put(DataType.INT16_LE, Int16LELayout.class);
//        mappingTable.put(DataType.INT16_BE, Int16BELayout.class);
//        layoutCache.put(DataType.UINT16_LE, new UInt16LELayout());
//        layoutCache.put(DataType.UINT16_BE, new UInt16BELayout());
//        layoutCache.put(DataType.INT16_LE, new Int16LELayout());
//        layoutCache.put(DataType.INT16_BE, new Int16BELayout());

        mappingTable.put(DataType.UINT32_LE, UInt32LELayout.class);
        mappingTable.put(DataType.UINT32_BE, UInt32BELayout.class);
        mappingTable.put(DataType.INT32_LE, Int32LELayout.class);
        mappingTable.put(DataType.INT32_BE, Int32BELayout.class);
        layoutCache.put(DataType.UINT32_LE, new UInt32LELayout());
        layoutCache.put(DataType.UINT32_BE, new UInt32BELayout());
        layoutCache.put(DataType.INT32_LE, new Int32LELayout());
        layoutCache.put(DataType.INT32_BE, new Int32BELayout());

        mappingTable.put(DataType.UINT64_LE, UInt64LELayout.class);
        mappingTable.put(DataType.UINT64_BE, UInt64BELayout.class);
        mappingTable.put(DataType.INT64_LE, Int64LELayout.class);
        mappingTable.put(DataType.INT64_BE, Int64BELayout.class);
        layoutCache.put(DataType.UINT64_LE, new UInt64LELayout());
        layoutCache.put(DataType.UINT64_BE, new UInt64BELayout());
        layoutCache.put(DataType.INT64_LE, new Int64LELayout());
        layoutCache.put(DataType.INT64_BE, new Int64BELayout());

        // 부동소수점 타입 (Floating-Point Types)
        mappingTable.put(DataType.FLOAT32_LE, Float32LELayout.class);
        mappingTable.put(DataType.FLOAT32_BE, Float32BELayout.class);
        mappingTable.put(DataType.FLOAT64_LE, Float64LELayout.class);
        mappingTable.put(DataType.FLOAT64_BE, Float64BELayout.class);
        layoutCache.put(DataType.FLOAT32_LE, new Float32LELayout());
        layoutCache.put(DataType.FLOAT32_BE, new Float32BELayout());
        layoutCache.put(DataType.FLOAT64_LE, new Float64LELayout());
        layoutCache.put(DataType.FLOAT64_BE, new Float64BELayout());

        // 문자 타입 (Character Types)
//        mappingTable.put(DataType.CHAR_C, CharCLayout.class);
//        mappingTable.put(DataType.UCHAR_C, UCharCLayout.class);
//        layoutCache.put(DataType.CHAR_C, new CharCLayout());
//        layoutCache.put(DataType.UCHAR_C, new UCharCLayout());

        // 문자열 타입 (String Types)
        mappingTable.put(DataType.STRING_C, CStringLayout.class); // C 문자열 (null-terminated)
    }

    /**
     * 주어진 DataType에 맞는 Layout을 가져옵니다.
     *
     * @param dataType DataType (엔디안을 포함한 데이터 타입)
     * @return Layout<T> 인스턴스
     */
    @SuppressWarnings("unchecked")
    public static <T> Layout<T> getLayout(DataType dataType) {
        if (!mappingTable.containsKey(dataType)) {
            throw new IllegalArgumentException("Unsupported DataType: " + dataType);
        }

        try {
            Layout<T> layout = (Layout<T>) layoutCache.get(dataType);
            if (layout != null) {
                return layout;
            } else {
                layout = (Layout<T>) mappingTable.get(dataType).getDeclaredConstructor().newInstance();
                return layout;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Layout for DataType: " + dataType, e);
        }
    }
}
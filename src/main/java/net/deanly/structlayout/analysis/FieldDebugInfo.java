package net.deanly.structlayout.analysis;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder(builderClassName = "Builder")
public class FieldDebugInfo {
    private final String order;
    private final String orderPrefix;
    private final String orderSuffix;
    private final String fieldName;
    private final byte[] encodedBytes;

    public String getEncodedBytesHex() {
        StringBuilder hex = new StringBuilder();
        for (byte b : encodedBytes) {
            hex.append(String.format("%02X ", b));
        }
        return hex.toString().trim();
    }

    public String getOrderString() {
        return (orderPrefix == null ? "" : orderPrefix) + order + (orderSuffix == null ? "" : orderSuffix);
    }
}

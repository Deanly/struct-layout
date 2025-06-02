package net.deanly.structlayout.exception;

import lombok.Getter;
import net.deanly.structlayout.analysis.DecodedFieldInfo;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StructDecodingException extends RuntimeException {
    private final Class<?> targetType;
    private final Field failedField;
    private final int failedOffset;
    @Getter
    private final List<DecodedFieldInfo> decodedFields;

    public StructDecodingException(Class<?> targetType, Field failedField, int failedOffset, List<DecodedFieldInfo> decodedFields, Throwable cause) {
        super(String.format("Failed to decode field '%s' at offset %d of struct '%s'.",
                failedField.getName(), failedOffset, targetType.getSimpleName()), cause);
        this.targetType = targetType;
        this.failedField = failedField;
        this.failedOffset = failedOffset;
        this.decodedFields = decodedFields;
    }

    public void printDebugLog() {
        System.out.println(getDebugLog());
    }

    public String getDebugLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("Failed to decode struct: ").append(targetType.getSimpleName()).append("\n");
        sb.append("Failure at field: ").append(failedField.getName())
                .append(" (offset ").append(failedOffset).append(")\n\n");

        if (decodedFields == null || decodedFields.isEmpty()) {
            sb.append("No fields decoded.\n");
            return sb.toString();
        }

        // Precompute sizes
        int totalBytes = 0;
        int offset = 0;
        int maxOrderLength = "Order".length();
        int maxFieldNameLength = "Field".length();
        int maxOffsetLength = "Offset".length();

        for (DecodedFieldInfo info : decodedFields) {
            maxOrderLength = Math.max(maxOrderLength, String.valueOf(info.order()).length());
            maxFieldNameLength = Math.max(maxFieldNameLength, info.fieldName().length());
            maxOffsetLength = Math.max(maxOffsetLength, String.format("%07d", offset).length());
            offset += (info.bytes() != null) ? info.bytes().length : 0;
        }

        sb.append(String.format(
                "%-" + maxOrderLength + "s %-" + maxFieldNameLength + "s %-" + maxOffsetLength + "s %s\n",
                "Order", "Field", "Offset", "Bytes (HEX)"
        ));
        sb.append("=".repeat(maxOrderLength + maxFieldNameLength + maxOffsetLength + 20)).append("\n");

        offset = 0;
        for (DecodedFieldInfo info : decodedFields) {
            String hex = bytesToHex(info.bytes());
            sb.append(String.format(
                    "%-" + maxOrderLength + "d %-" + maxFieldNameLength + "s %0" + maxOffsetLength + "d %s\n",
                    info.order(), info.fieldName(), offset, hex
            ));
            int len = (info.bytes() != null) ? info.bytes().length : 0;
            offset += len;
            totalBytes += len;
        }

        sb.append("=".repeat(maxOrderLength + maxFieldNameLength + maxOffsetLength + 20)).append("\n");
        sb.append("Total Bytes: ").append(totalBytes).append("\n");

        return sb.toString();
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "(empty)";
        return IntStream.range(0, bytes.length)
                .mapToObj(i -> String.format("%02X", bytes[i] & 0xFF))
                .collect(Collectors.joining(" "));
    }
}
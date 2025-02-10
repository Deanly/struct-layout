package net.deanly.structlayout.type.advanced;

import net.deanly.structlayout.type.CountableField;
import net.deanly.structlayout.type.FieldBase;

/**
 * Represents a "no-length" or "none" field in a structured sequence.
 *
 * <p>The {@code NoneField} is used primarily in scenarios where the length metadata
 * is intentionally omitted or ignored. For example, when serializing or deserializing
 * sequences where the length information is unnecessary or not included in the data format.</p>
 *
 * <p>This class treats the field as having zero length (0 bytes) during encoding,
 * decoding, and spans calculation. It integrates seamlessly with sequences that
 * require "unsafe mode" functionality to handle elements directly without length constraints.</p>
 *
 * Key Features:
 * <ul>
 *   <li><b>Zero Length:</b> Always returns 0 for length (`getSpan`) and produces an empty byte array on encoding.</li>
 *   <li><b>No Value:</b> The decode method always returns {@code null}, and encoding expects a {@code Void} value.</li>
 *   <li><b>Integration:</b> Works as the {@code lengthType} for annotations like {@code @StructSequenceObjectField}
 *       to indicate the absence of length metadata.</li>
 * </ul>
 *
 * Use Cases:
 * <ul>
 *   <li>Handling sequences without length metadata (e.g., raw byte sequences or "unsafe mode").</li>
 *   <li>When length prefixing/constraining is irrelevant or unsupported by the data structure.</li>
 *   <li>Facilitating flexible encoding/decoding operations where only element data matters.</li>
 * </ul>
 */
public class NoneField extends FieldBase<Void> implements CountableField<Void> {
    public NoneField() {
        super(0);
    }

    @Override
    public int getSpan() {
        return 0;
    }

    @Override
    public Void decode(byte[] bytes, int offset) {
        return null;
    }

    @Override
    public byte[] encode(Void value) {
        return new byte[0];
    }
}

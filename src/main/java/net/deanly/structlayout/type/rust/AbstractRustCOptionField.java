package net.deanly.structlayout.type.rust;

import net.deanly.structlayout.Field;
import net.deanly.structlayout.type.DynamicSpanField;
import net.deanly.structlayout.type.FieldBase;

/**
 * <h1>AbstractRustCOptionField</h1>
 *
 * Represents the Rust-style COption<T> layout where the presence of a value
 * is encoded with a fixed-size 4-byte tag (LE): 0x00000000 for None and
 * 0x01000000 for Some, followed by a fixed-size value.
 *
 * <h2>Encoding Format</h2>
 * <ul>
 *   <li>None: [0x00, 0x00, 0x00, 0x00]</li>
 *   <li>Some: [0x01, 0x00, 0x00, 0x00] + value (fixed size)</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public class PubkeyCOptionField extends AbstractRustCOptionField<Pubkey, PubkeyField> {
 *     @Override
 *     protected PubkeyField createField() {
 *         return new PubkeyField(); // span = 32 bytes
 *     }
 * }
 * }</pre>
 *
 * @param <T> Type of the value to encode/decode
 * @param <F> Concrete field implementation
 */
public abstract class AbstractRustCOptionField<T, F extends FieldBase<T>> extends FieldBase<T> implements DynamicSpanField {

    private static final byte[] NONE_TAG = new byte[] {0, 0, 0, 0};
    private static final byte[] SOME_TAG = new byte[] {1, 0, 0, 0};

    private final F fieldInstance;

    public AbstractRustCOptionField() {
        super(4);
        this.fieldInstance = createField();
    }

    protected abstract F createField();

    @Override
    public byte[] encode(T value) {
        byte[] result;
        if (value == null) {
            byte[] zero = new byte[fieldInstance.getSpan()];
            result = new byte[4 + zero.length];
            System.arraycopy(NONE_TAG, 0, result, 0, 4);
            System.arraycopy(zero, 0, result, 4, zero.length);
        } else {
            byte[] inner = fieldInstance.encode(value);
            result = new byte[4 + inner.length];
            System.arraycopy(SOME_TAG, 0, result, 0, 4);
            System.arraycopy(inner, 0, result, 4, inner.length);
        }

        return result;
    }

    @Override
    public T decode(byte[] data, int offset) {
        validateLength(data, offset);

        if (data.length - offset < 4) {
            throw new IllegalArgumentException("Data too short to read COption tag.");
        }

        boolean isSome = (data[offset] == 1 && data[offset + 1] == 0 && data[offset + 2] == 0 && data[offset + 3] == 0);

        if (!isSome && !(data[offset] == 0 && data[offset + 1] == 0 && data[offset + 2] == 0 && data[offset + 3] == 0)) {
            throw new IllegalArgumentException("Invalid COption tag at offset " + offset);
        }

        return isSome ? fieldInstance.decode(data, offset + 4) : null;
    }

    @Override
    public int calculateSpan(byte[] data, int offset) {
        validateLength(data, offset);
        return 4 + fieldInstance.getSpan();
    }

    @Override
    public int getSpan() {
        return 4 + fieldInstance.getSpan();
    }

    @Override
    public int getNoDataSpan() {
        return 4 + fieldInstance.getSpan();
    }
}
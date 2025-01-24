package net.deanly.structlayout;

import net.deanly.structlayout.codec.Decoder;
import net.deanly.structlayout.codec.Encoder;

public interface Field<T> extends Encoder<T>, Decoder<T> {
    int getSpan();
}

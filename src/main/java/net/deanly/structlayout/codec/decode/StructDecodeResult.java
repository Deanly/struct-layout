package net.deanly.structlayout.codec.decode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class StructDecodeResult<T> {
    private final T value;
    private final int size;
}

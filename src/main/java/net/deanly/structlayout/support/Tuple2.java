package net.deanly.structlayout.support;

public class Tuple2<F, S> {
    public final F first;
    public final S second;

    public Tuple2(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Tuple2<F, S> of(F first, S second) {
        return new Tuple2<>(first, second);
    }
}
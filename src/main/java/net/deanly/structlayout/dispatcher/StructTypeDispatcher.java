package net.deanly.structlayout.dispatcher;

public interface StructTypeDispatcher {
    Class<?> dispatch(byte[] data);
}

package net.deanly.structlayout.analysis;

public record DecodedFieldInfo(String fieldName, int order, int offset, byte[] bytes) { }

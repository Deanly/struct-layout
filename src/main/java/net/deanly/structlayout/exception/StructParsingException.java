package net.deanly.structlayout.exception;

// 기본 디코딩 관련 예외
public class StructParsingException extends RuntimeException {
    public StructParsingException(String message) {
        super(message);
    }

    public StructParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}


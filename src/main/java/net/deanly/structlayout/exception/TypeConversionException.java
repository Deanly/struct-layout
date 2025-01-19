package net.deanly.structlayout.exception;

public class TypeConversionException extends StructParsingException {
    public TypeConversionException(String message) {
        super(message);
    }

    public TypeConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
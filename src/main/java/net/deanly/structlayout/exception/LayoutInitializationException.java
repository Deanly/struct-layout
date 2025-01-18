package net.deanly.structlayout.exception;

public class LayoutInitializationException extends RuntimeException {

    public LayoutInitializationException(String message) {
        super(message);
    }

    public LayoutInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
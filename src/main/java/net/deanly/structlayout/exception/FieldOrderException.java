package net.deanly.structlayout.exception;

// 사용되지 않는 순서 어노테이션 (정렬 오류 등)
public class FieldOrderException extends StructParsingException {
    public FieldOrderException(String fieldName) {
        super(String.format(
                "Field '%s' does not define a valid 'order'. Please specify a proper 'order' value.",
                fieldName));
    }
    public FieldOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
package net.deanly.structlayout.exception;

// Sequence 필드(배열 또는 리스트)에서 잘못된 데이터 타입
public class InvalidSequenceTypeException extends StructParsingException {
    public InvalidSequenceTypeException(String fieldName, Class<?> fieldType) {
        super(String.format(
                "Unsupported sequence type '%s' in field '%s'. ",
                fieldType.getName(), fieldName));
    }
    public InvalidSequenceTypeException(String fieldName, Class<?> fieldType, String message) {
        super(String.format(
                "Unsupported sequence type '%s' in field '%s'. %s",
                fieldType.getName(), fieldName, message));
    }
}
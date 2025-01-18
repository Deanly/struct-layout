package net.deanly.structlayout.exception;

// 필드 접근 불가능 또는 설정 오류
public class FieldAccessException extends StructParsingException {
  public FieldAccessException(String fieldName, String className, Throwable cause) {
    super(String.format("Failed to access or set field '%s' in class '%s'. Please ensure the field is accessible and properly annotated.", fieldName, className), cause);
  }
}

package net.deanly.structlayout.exception;

// No-Arguments 생성자 없음
public class NoDefaultConstructorException extends StructParsingException {
  public NoDefaultConstructorException(String className) {
    super(String.format("Class '%s' must have a public no-arguments constructor to be decodable. Please add a default constructor.", className));
  }
  public NoDefaultConstructorException(String message, Throwable cause) {
    super(message, cause);
  }
}
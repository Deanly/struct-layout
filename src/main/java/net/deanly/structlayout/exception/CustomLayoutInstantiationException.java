package net.deanly.structlayout.exception;

// 사용자 정의 Layout 인스턴스화 실패
public class CustomLayoutInstantiationException extends StructParsingException {
    public CustomLayoutInstantiationException(String layoutName, Throwable cause) {
        super(String.format("Failed to instantiate custom Layout class '%s'. Ensure the class has a valid public no-arguments constructor.", layoutName), cause);
    }
}

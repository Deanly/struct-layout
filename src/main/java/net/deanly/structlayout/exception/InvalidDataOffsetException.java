package net.deanly.structlayout.exception;

// 데이터 처리 중 잘못된 오프셋
public class InvalidDataOffsetException extends StructParsingException {
    public InvalidDataOffsetException(int offset, int dataLength) {
        super(String.format("Invalid offset '%d'. Exceeds the data length of '%d'. Ensure the data array and offset are valid.", offset, dataLength));
    }
}

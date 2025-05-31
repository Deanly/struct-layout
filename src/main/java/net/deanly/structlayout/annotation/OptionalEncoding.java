package net.deanly.structlayout.annotation;

public enum OptionalEncoding {
    /**
     * Field is not optional — always present.
     */
    NONE,

    /**
     * Borsh-style optional encoding using a 1-byte prefix:
     * 0x00 = None, 0x01 = Some + value.
     */
    BORSH,

//    /**
//     * Field is omitted entirely from serialization if value is null.
//     * No prefix is used. Common in Protobuf, FlatBuffers.
//     */
//    OMIT_IF_NULL,

//    /**
//     * Field is encoded as a literal null (e.g., `nil` in MessagePack/CBOR)
//     */
//    NULL_LITERAL
}

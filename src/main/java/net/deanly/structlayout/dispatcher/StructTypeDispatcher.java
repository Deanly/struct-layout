package net.deanly.structlayout.dispatcher;

public interface StructTypeDispatcher {
    /**
     * Determines the specific class type associated with the provided data
     * and offset values. This method is used to identify a concrete class
     * based on the given byte array and starting offset.
     *
     * @param data the byte array containing information for determining the class
     * @param startOffset the starting position within the byte array to begin processing
     * @return the resolved class type based on the input data and offset
     */
    Class<?> dispatch(byte[] data, int startOffset);

    /**
     * Retrieves the span or length of data that is considered as "no data"
     * or an unutilized data segment. The returned value may represent a
     * specific predefined buffer space, offset, or gap within the structured
     * layout being processed.
     *
     * @return the span representing the "no data" section, as an integer
     */
    int getNoDataSpan();
}

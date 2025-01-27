package net.deanly.structlayout.type;

public interface DynamicSpanField {
    /**
     * Calculates the span dynamically based on provided data.
     *
     * @param data   The input data.
     * @param offset The starting offset.
     * @return The dynamically calculated span.
     */
    int calculateSpan(byte[] data, int offset);
//
//    /**
//     * Retrieves the current span value.
//     *
//     * @return The current span value as an integer.
//     */
//    int getSpan();
//
//    /**
//     * Sets the span value for the current instance.
//     *
//     * @param span The span value to be set, represented as an integer.
//     */
//    void setSpan(int span);
}

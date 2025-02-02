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

    /**
     * Retrieves the minimum span size that the field occupies when no data is present.
     *
     * @return The minimum span size representing the space occupied by the field in the absence of data.
     */
    int getNoDataSpan();

}

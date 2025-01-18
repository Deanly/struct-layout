package net.deanly.structlayout.type.ext;

public interface DynamicSpanLayout {
    /**
     * Calculates the span dynamically based on provided data.
     *
     * @param data   The input data.
     * @param offset The starting offset.
     * @return The dynamically calculated span.
     */
    int calculateSpan(byte[] data, int offset);
}

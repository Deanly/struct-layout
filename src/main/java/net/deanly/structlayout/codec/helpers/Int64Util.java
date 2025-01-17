package net.deanly.structlayout.codec.helpers;

public class Int64Util {

    // Constant value for 2^32
    private static final long V2E32 = (1L << 32);

    /**
     * Divides a 64-bit integer into high and low 32-bit words.
     *
     * The high word contains the quotient, and the low word contains the remainder.
     * The low word is always non-negative.
     *
     * @param src 64-bit integer value.
     * @return A result object containing the high and low 32-bit parts.
     */
    public static DivModResult divmodInt64(long src) {
        // Calculate high 32 bits
        int hi32 = (int) (src / V2E32);

        // Calculate low 32 bits (non-negative remainder)
        int lo32 = (int) (src % V2E32);

        return new DivModResult(hi32, lo32);
    }

    /**
     * Reconstructs a 64-bit integer from high and low 32-bit words.
     *
     * @param hi32 The high 32-bit word.
     * @param lo32 The low 32-bit word.
     * @return The reconstructed 64-bit integer value.
     */
    public static long roundedInt64(int hi32, int lo32) {
        // Reconstruct the 64-bit integer
        return (hi32 * V2E32) + (lo32 & 0xFFFFFFFFL);
    }

    /**
     * A simple data class to store the result of high and low 32-bit words.
     */
    public static class DivModResult {
        private final int hi32;
        private final int lo32;

        public DivModResult(int hi32, int lo32) {
            this.hi32 = hi32;
            this.lo32 = lo32;
        }

        public int getHi32() {
            return hi32;
        }

        public int getLo32() {
            return lo32;
        }

        @Override
        public String toString() {
            return "DivModResult{" +
                    "hi32=" + hi32 +
                    ", lo32=" + lo32 +
                    '}';
        }
    }
}
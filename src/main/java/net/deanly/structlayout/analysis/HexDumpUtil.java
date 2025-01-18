package net.deanly.structlayout.analysis;

public class HexDumpUtil {

    /**
     * Converts a byte array into a formatted hex dump string.
     * The output mimics the format returned by macOS's `xxd` command.
     *
     * @param data The input byte array to be converted.
     * @return A formatted hex dump string.
     */
    public static String toHexDump(byte[] data) {
        StringBuilder hexDump = new StringBuilder();

        int length = data.length;
        int offset = 0;

        while (offset < length) {
            // Append the offset (in hexadecimal).
            hexDump.append(String.format("%08x: ", offset));

            // Append the hex values for this line.
            for (int i = 0; i < 16; i++) {
                if (offset + i < length) {
                    hexDump.append(String.format("%02x ", data[offset + i]));
                } else {
                    hexDump.append("   "); // Padding for incomplete lines.
                }
            }

            hexDump.append("  "); // Space between hex column and ASCII column.

            // Append the ASCII values for this line.
            for (int i = 0; i < 16; i++) {
                if (offset + i < length) {
                    byte b = data[offset + i];
                    char c = (char) b;
                    // Print printable ASCII characters, otherwise use '.'
                    if (c >= 32 && c <= 126) { // Printable ASCII range.
                        hexDump.append(c);
                    } else {
                        hexDump.append('.');
                    }
                }
            }

            hexDump.append(System.lineSeparator());
            offset += 16;
        }

        return hexDump.toString();
    }
}
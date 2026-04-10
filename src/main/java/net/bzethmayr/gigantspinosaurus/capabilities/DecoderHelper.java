package net.bzethmayr.gigantspinosaurus.capabilities;

import java.nio.ByteBuffer;

import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;

public final class DecoderHelper {
    public static void expect(final ByteBuffer in, final byte expected) {
        byte actual = in.get();
        if (actual != expected) {
            throw becauseIllegal("Unexpected character '%s'", actual);
        }
    }

    public static IllegalArgumentException becauseBadSeparator(final byte sep) {
        return becauseIllegal("Bad separator: %s", sep);
    }

    public static IllegalArgumentException becauseBadKey(final String key) {
        return becauseIllegal("Unknown key: %s", key);
    }

    /**
     * Consumes and checks the separator, returning true if it ends the section.
     * @param in the input buffer.
     * @return true if the section ended, false if the section can continue. Otherwise, throws.
     */
    public static boolean checkSep(final ByteBuffer in) {
        byte sep = in.get();
        if (sep == CLOSE) return true;
        if (sep != SEP) throw becauseBadSeparator(sep);
        return false;
    }

    /**
     * Starts a section.
     */
    public static final byte OPEN = (byte) '{';
    /**
     * Precedes a value.
     */
    public static final byte VAL = (byte) ':';
    /**
     * Separates values.
     */
    public static final byte SEP = (byte) ',';
    /**
     * Ends a section.
     */
    public static final byte CLOSE = (byte) '}';

    private static boolean validForKey(final byte b) {
        return 1 == ((((b - 'A') | ('Z' - b)) >>> 31) ^ 1) +
                ((((b - '0') | ('9' - b)) >>> 31) ^ 1) +
                ((((b - 'a') | ('z' - b)) >>> 31) ^ 1) +
                ((((b - '_') | ('_' - b)) >>> 31) ^ 1);
    }

    /**
     * Reads as long as valid ASCII identifier characters are encountered.
     * Accepts letters, numbers, and the underscore.
     * Leaves the buffer after the last valid identifier character.
     * @param in the input buffer.
     * @return any identifier encountered, or an empty string if none are.
     */
    public static String readAsciiKey(final ByteBuffer in) {
        final StringBuilder keyOut = new StringBuilder();
        while (true) {
            byte b = in.get();
            if (!validForKey(b)) break;
            keyOut.appendCodePoint(b);
        }
        in.position(in.position() - 1);
        return keyOut.toString();
    }
}

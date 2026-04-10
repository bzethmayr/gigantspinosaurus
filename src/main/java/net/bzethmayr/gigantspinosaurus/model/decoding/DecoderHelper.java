package net.bzethmayr.gigantspinosaurus.model.decoding;

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

    public static boolean checkSep(final ByteBuffer in) {
        byte sep = in.get();
        if (sep == CLOSE) return true;
        if (sep != SEP) throw becauseBadSeparator(sep);
        return false;
    }

    public static final byte OPEN = (byte) '{';
    public static final byte VAL = (byte) ':';
    public static final byte SEP = (byte) ',';
    public static final byte CLOSE = (byte) '}';

    private static boolean validForKey(final byte b) {
        return 1 == ((((b - 'A') | ('Z' - b)) >>> 31) ^ 1) +
                ((((b - '0') | ('9' - b)) >>> 31) ^ 1) +
                ((((b - 'a') | ('z' - b)) >>> 31) ^ 1) +
                ((((b - '_') | ('_' - b)) >>> 31) ^ 1);
    }

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

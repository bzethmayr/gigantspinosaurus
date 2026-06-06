package net.bzethmayr.gigantspinosaurus.model.media;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * Extracts the MAR bytes from a media mark.
 */
@FunctionalInterface
public interface ExtractsMar extends Function<ByteBuffer, byte[]> {
}

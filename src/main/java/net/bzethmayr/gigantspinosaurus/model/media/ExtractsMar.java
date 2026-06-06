package net.bzethmayr.gigantspinosaurus.model.media;

import java.nio.ByteBuffer;
import java.util.function.Function;

@FunctionalInterface
public interface ExtractsMar extends Function<ByteBuffer, byte[]> {
}

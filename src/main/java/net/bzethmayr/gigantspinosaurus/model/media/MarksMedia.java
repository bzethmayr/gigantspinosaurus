package net.bzethmayr.gigantspinosaurus.model.media;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface MarksMedia {
    void mark(final ByteBuffer mark, final ByteBuffer target);
}

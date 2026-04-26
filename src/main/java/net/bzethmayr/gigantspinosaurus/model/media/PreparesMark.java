package net.bzethmayr.gigantspinosaurus.model.media;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

public interface PreparesMark extends BiConsumer<byte[], ByteBuffer> {
    ByteBuffer emptyMark();
}

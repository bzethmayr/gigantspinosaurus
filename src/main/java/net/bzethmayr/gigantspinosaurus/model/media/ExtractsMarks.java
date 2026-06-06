package net.bzethmayr.gigantspinosaurus.model.media;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

@FunctionalInterface
public interface ExtractsMarks extends UnaryOperator<ByteBuffer> {
}

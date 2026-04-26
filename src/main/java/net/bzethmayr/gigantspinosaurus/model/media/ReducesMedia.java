package net.bzethmayr.gigantspinosaurus.model.media;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

public interface ReducesMedia extends UnaryOperator<ByteBuffer> {
    ReductionStep[] reductions();
}

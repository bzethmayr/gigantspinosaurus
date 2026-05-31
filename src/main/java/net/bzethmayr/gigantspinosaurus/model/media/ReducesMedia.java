package net.bzethmayr.gigantspinosaurus.model.media;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.UnaryOperator;

public interface ReducesMedia extends UnaryOperator<ByteBuffer> {
    ReductionStep[] reductions();

    default ReductionStep[] reductions(final ReductionStep... prepend) {
        final ReductionStep[] base = reductions();
        final ReductionStep[] result = Arrays.copyOf(prepend, prepend.length + base.length);
        System.arraycopy(base, 0, result, prepend.length, base.length);
        return result;
    }
}

package net.bzethmayr.gigantspinosaurus.model.correlation;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.function.ToLongFunction;

public interface HashesMedia extends Function<ByteBuffer, byte[]> {
    /**
     * {@inheritDoc}
     * @param payload the payload to hash
     * @return a 64-bit hash
     */
    @Override
    byte[] apply(ByteBuffer payload);
}

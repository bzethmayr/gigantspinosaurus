package net.bzethmayr.gigantspinosaurus.model.correlation;

import java.util.function.ToLongFunction;

public interface HashesMedia extends ToLongFunction<byte[]> {
    /**
     * {@inheritDoc}
     * @param payload the payload to hash
     * @return a 64-bit hash
     */
    @Override
    long applyAsLong(byte[] payload);
}

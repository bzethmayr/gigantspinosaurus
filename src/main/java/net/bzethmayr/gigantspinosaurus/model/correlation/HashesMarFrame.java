package net.bzethmayr.gigantspinosaurus.model.correlation;

import java.util.function.ToLongBiFunction;

@FunctionalInterface
public interface HashesMarFrame extends ToLongBiFunction<byte[], byte[]> {

    /**
     * {@inheritDoc}
     * @param key the per-frame key
     * @param payload the payload to hash
     * @return a 64-bit hash
     */
    @Override
    long applyAsLong(byte[] key, byte[] payload);
}

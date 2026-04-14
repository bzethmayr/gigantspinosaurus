package net.bzethmayr.gigantspinosaurus.model.correlation;

import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

@FunctionalInterface
public interface Hashes extends ToLongBiFunction<byte[], byte[]> {

    /**
     * {@inheritDoc}
     * @param payload the payload to hash
     * @return a 64-bit hash
     */
    @Override
    long applyAsLong(byte[] key, byte[] payload);
}

package net.bzethmayr.gigantspinosaurus.capabilities.signature;

import java.util.function.Function;

@FunctionalInterface
public interface Signs extends Function<byte[], byte[]> {

    /**
     * {@inheritDoc}
     * @param payload the payload to sign
     * @return the 64-byte signature
     */
    @Override
    byte[] apply(byte[] payload);
}

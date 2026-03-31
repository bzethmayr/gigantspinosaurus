package net.bzethmayr.gigantspinosaurus.capabilities.nonce;

import java.util.function.LongSupplier;

/**
 * A nonce supplier. Calling {@link #getAsLong()} must be a non-blocking operation.
 */
@FunctionalInterface
public interface GeneratesNonce extends LongSupplier {

    /**
     * {@inheritDoc}
     * This call must be a non-blocking operation.
     * @return the next nonce.
     */
    @Override
    long getAsLong();
}

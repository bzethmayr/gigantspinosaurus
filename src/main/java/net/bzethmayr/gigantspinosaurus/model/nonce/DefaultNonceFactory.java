package net.bzethmayr.gigantspinosaurus.model.nonce;

import java.security.SecureRandom;

import static java.util.Objects.requireNonNull;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseFactory;

/**
 * Default / reference nonce-source.
 */
public final class DefaultNonceFactory {
    private DefaultNonceFactory() {
        throw becauseFactory();
    }

    private static final SecureRandom NONCE_RANDOM = new SecureRandom();

    /**
     * Force initialization.
     */
    public static void init() {
        requireNonNull(NONCE_RANDOM);
    }

    /**
     * This call must not block - call {@link #init()} early to prevent this.
     */
    public static long freshNonce() {
        return NONCE_RANDOM.nextLong();
    }

    /**
     * If this generator is obtained before use, it is guaranteed not to block.
     * @return {@link #freshNonce()}
     */
    public GeneratesNonce defaultGenerator() {
        return DefaultNonceFactory::freshNonce;
    }
}

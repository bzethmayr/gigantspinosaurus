package net.bzethmayr.gigantspinosaurus.capabilities.signature;

import java.util.function.Supplier;
@FunctionalInterface
public interface ExposesSigningKeys extends Supplier<byte[]> {

    /**
     * {@inheritDoc}
     * @return the 32-byte exposable (public) key.
     */
    @Override
    byte[] get();
}

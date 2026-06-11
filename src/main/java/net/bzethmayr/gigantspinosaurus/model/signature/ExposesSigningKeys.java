package net.bzethmayr.gigantspinosaurus.model.signature;

import java.util.function.Supplier;
@FunctionalInterface
public interface ExposesSigningKeys extends Supplier<byte[]> {

    /**
     * {@inheritDoc}
     * @return the 44-byte X509-encoded public key.
     */
    @Override
    byte[] get();
}

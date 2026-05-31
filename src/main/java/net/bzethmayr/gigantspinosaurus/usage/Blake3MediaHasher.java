package net.bzethmayr.gigantspinosaurus.usage;

import io.github.rctcwyvrn.blake3.Blake3;
import net.bzethmayr.gigantspinosaurus.model.correlation.HashesMedia;

import java.nio.ByteBuffer;

public class Blake3MediaHasher implements HashesMedia {

    @Override
    public byte[] apply(final ByteBuffer payload) {
        final Blake3 hasher = Blake3.newInstance();
        final byte[] bytes = new byte[payload.remaining()];
        payload.duplicate().get(bytes);
        hasher.update(bytes);
        return hasher.digest();
    }
}

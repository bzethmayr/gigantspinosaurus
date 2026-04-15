package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.correlation.HashesMedia;
import net.jpountz.xxhash.XXHash64;
import net.jpountz.xxhash.XXHashFactory;

public class Xx64MediaHasher implements HashesMedia {
    private final XXHash64 hasher;

    public Xx64MediaHasher() {
        hasher = XXHashFactory.safeInstance().hash64();
    }

    @Override
    public long applyAsLong(byte[] payload) {
        return hasher.hash(payload, 0, payload.length, 0);
    }
}

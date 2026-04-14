package net.bzethmayr.gigantspinosaurus.usage;

import io.whitfin.siphash.SipHash;
import net.bzethmayr.gigantspinosaurus.model.correlation.Hashes;

public class SipHasher implements Hashes {
    @Override
    public long applyAsLong(byte[] key, byte[] payload) {
        return SipHash.hash(key, payload, 4, 8);
    }
}

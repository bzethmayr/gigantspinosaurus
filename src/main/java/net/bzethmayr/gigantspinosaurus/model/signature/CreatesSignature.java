package net.bzethmayr.gigantspinosaurus.model.signature;

import java.util.HashSet;
import java.util.Set;

import static net.bzethmayr.gigantspinosaurus.capabilities.DecoderHelper.*;
import static net.bzethmayr.gigantspinosaurus.capabilities.Versioned.VERSION_FIELD;
import static net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature.*;

@FunctionalInterface
public interface CreatesSignature<T extends ExposesSignature> {

    T createSignature(byte[] ed25519Pub,
                                     byte[] ed25519,
                                     short version);

    static <T extends ExposesSignature> CanonicalDecoder<T> createsSignatures(final CreatesSignature<T> ctor) {
        return (in, decoders) -> {
            expect(in, OPEN);

            byte[] ed25519Pub = new byte[PUB_KEY_LENGTH];
            byte[] ed25519 = new byte[SIGNATURE_LENGTH];
            short version = SIGNATURE_VERSION;

            final Set<String> keys = new HashSet<>();
            while (true) {
                String key = readAsciiKey(in); // reads up to ':'
                requireKeyUnique(keys, key);
                expect(in, VAL);

                switch (key) {
                    case PUB_KEY_FIELD -> in.get(ed25519Pub);
                    case SIGNATURE_FIELD -> in.get(ed25519);
                    case VERSION_FIELD -> version = in.getShort();
                    default -> throw becauseBadKey(key);
                }

                if (checkSep(in)) break;
            }

            return ctor.createSignature(ed25519Pub, ed25519, version);
        };
    }
}

package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.model.MarDecoder.*;

public record MarSignature(
        byte[] ed25519Pub,
        byte[] ed25519,
        short version
) implements ExposesSignature {

    public MarSignature(final byte[] ed25519Pub, final byte[] ed25519) {
        this(ed25519Pub, ed25519, SIGNATURE_VERSION);
    }

    public static MarSignature decode(final ByteBuffer in, final CanonizesDecoders decoders) {
        expect(in, OPEN);

        byte[] ed25519Pub = new byte[PUB_KEY_LENGTH];
        byte[] ed25519 = new byte[SIGNATURE_LENGTH];
        short version = SIGNATURE_VERSION;

        while (true) {
            String key = readAsciiKey(in); // reads up to ':'
            expect(in, VAL);

            switch (key) {
                case PUB_KEY_FIELD -> in.get(ed25519Pub);
                case SIGNATURE_FIELD -> in.get(ed25519);
                case VERSION_FIELD -> version = in.getShort();
            }

            byte sep = in.get();
            if (sep == CLOSE) break;
            if (sep != SEP) throw becauseBadSeparator(sep);
        }

        return new MarSignature(ed25519Pub, ed25519, version);
    }
}

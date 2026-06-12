package net.bzethmayr.gigantspinosaurus.usage.defaults;

import net.bzethmayr.gigantspinosaurus.model.MarSignature;
import net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PermanentSignatoryTest {

    @Test
    void generateOnFirstUse_andReload() {
        final InMemorySignatory first = new InMemorySignatory();
        final byte[] pubKey = first.get();
        final byte[] signature = first.apply("hello".getBytes());
        assertEquals(44, pubKey.length);
        assertEquals(64, signature.length);

        final InMemorySignatory reloaded = new InMemorySignatory();
        assertArrayEquals(pubKey, reloaded.get(), "public key must persist across reloads");

        final byte[] secondSig = reloaded.apply("hello".getBytes());
        assertArrayEquals(signature, secondSig, "signature must be deterministic for same payload");
    }

    @Test
    void signAndVerifyRoundTrip() {
        final InMemorySignatory signatory = new InMemorySignatory();
        final byte[] payload = "test payload".getBytes();
        final byte[] signature = signatory.apply(payload);

        final ExposesSignature exposes = new MarSignature(
                signatory.get(),
                signature,
                ExposesSignature.SIGNATURE_VERSION
        );

        assertTrue(signatory.test(exposes, payload), "signature must verify");
    }

    @Test
    void rejectSignatureForWrongPayload() {
        final InMemorySignatory signatory = new InMemorySignatory();
        final byte[] payload = "test payload".getBytes();
        final byte[] wrongPayload = "wrong payload".getBytes();
        final byte[] signature = signatory.apply(payload);

        final ExposesSignature exposes = new MarSignature(
                signatory.get(),
                signature,
                ExposesSignature.SIGNATURE_VERSION
        );

        assertFalse(signatory.test(exposes, wrongPayload),
                "signature for different payload must not verify");
    }

    @Test
    void sameKeyBetweenInstances_producesIdenticalPublicKey() {
        final InMemorySignatory a = new InMemorySignatory();
        final InMemorySignatory b = new InMemorySignatory();

        assertArrayEquals(a.get(), b.get(), "same storage must produce same public key");
    }

    private static class InMemorySignatory extends PermanentSignatory {
        private static byte[] storedPriv;
        private static byte[] storedPub;

        @Override
        protected byte[] loadPrivateKeyBytes() {
            return storedPriv;
        }

        @Override
        protected byte[] loadPublicKeyBytes() {
            return storedPub;
        }

        @Override
        protected void storePrivateKeyBytes(final byte[] privKeyBytes) {
            storedPriv = Arrays.copyOf(privKeyBytes, privKeyBytes.length);
        }

        @Override
        protected void storePublicKeyBytes(final byte[] pubKeyBytes) {
            storedPub = Arrays.copyOf(pubKeyBytes, pubKeyBytes.length);
        }
    }
}

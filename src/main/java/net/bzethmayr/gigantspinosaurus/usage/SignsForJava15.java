package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature;
import net.bzethmayr.gigantspinosaurus.model.signature.Signatory;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseImpossible;

/**
 * Wraps the Java 15+ Ed25519 support.
 */
public class SignsForJava15 implements Signatory {
    private final KeyPair signingPair;

    public SignsForJava15(final KeyPair signingPair) {
        this.signingPair = signingPair;
    }

    @Override
    public byte[] get() {
        return signingPair.getPublic().getEncoded();
    }

    private static IllegalStateException becauseEdHasGone() {
        return becauseImpossible("Ed25519 has gone?");
    }

    private static IllegalArgumentException becauseBadKey() {
        return becauseIllegal("Invalid key.");
    }

    private static IllegalStateException becauseSignatureBroke(final SignatureException se) {
        return becauseImpossible("Something happened: %s", se.getLocalizedMessage());
    }

    private static Signature getEd() {
        try {
            return Signature.getInstance("Ed25519");
        } catch (final NoSuchAlgorithmException nsae) {
            throw becauseEdHasGone();
        }
    }

    @Override
    public byte[] apply(byte[] payload) {
        final Signature sig = getEd();
        try {
            sig.initSign(signingPair.getPrivate());
        } catch (final InvalidKeyException ke) {
            throw becauseBadKey();
        }
        try {
            sig.update(payload);
            return sig.sign();
        } catch (final SignatureException se) {
            throw becauseSignatureBroke(se);
        }
    }

    @Override
    public boolean test(final ExposesSignature exposesSignature, final byte[] payload) {
        final KeyFactory kf;
        try {
            kf = KeyFactory.getInstance("Ed25519");
        } catch (final NoSuchAlgorithmException nsae) {
            throw becauseEdHasGone();
        }
        final PublicKey pk;
        try {
            pk = kf.generatePublic(new
                    X509EncodedKeySpec(exposesSignature.ed25519Pub()));
        } catch (final InvalidKeySpecException ke) {
            throw becauseBadKey();
        }
        final Signature sig = getEd();
        try {
            sig.initVerify(pk);
        } catch (final InvalidKeyException ke) {
            throw becauseBadKey();
        }
        try {
            sig.update(payload);
            return sig.verify(exposesSignature.ed25519());
        } catch (final SignatureException se) {
            throw becauseSignatureBroke(se);
        }
    }
}

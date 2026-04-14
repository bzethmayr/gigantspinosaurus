package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.signature.Signatory;

import java.security.*;

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

    @Override
    public byte[] apply(byte[] payload) {
        final Signature sig;
        try {
            sig = Signature.getInstance("Ed25519");
        } catch (final NoSuchAlgorithmException nsae) {
            throw becauseImpossible("Ed25519 has gone?");
        }
        try {
            sig.initSign(signingPair.getPrivate());
        } catch (final InvalidKeyException ike) {
            throw becauseIllegal("Invalid private key.");
        }
        try {
            sig.update(payload);
            return sig.sign();
        } catch (final SignatureException se) {
            throw becauseImpossible("Something happened: %s", se.getLocalizedMessage());
        }
    }
}

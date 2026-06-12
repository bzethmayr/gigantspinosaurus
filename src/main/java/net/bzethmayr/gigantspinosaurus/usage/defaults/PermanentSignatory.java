package net.bzethmayr.gigantspinosaurus.usage.defaults;

import net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature;
import net.bzethmayr.gigantspinosaurus.model.signature.Signatory;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature.SIGNATURE_ALGORITHM;
import static net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature.becauseEdHasGone;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseIllegal;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseImpossible;

public abstract class PermanentSignatory implements Signatory {
    private final KeyPair keyPair;

    protected PermanentSignatory() {
        final byte[] storedPriv = loadPrivateKeyBytes();
        if (storedPriv != null) {
            final byte[] storedPub = loadPublicKeyBytes();
            this.keyPair = restoreKeyPair(storedPriv, storedPub);
        } else {
            this.keyPair = generateAndStore();
        }
    }

    protected abstract byte[] loadPrivateKeyBytes();
    protected abstract byte[] loadPublicKeyBytes();
    protected abstract void storePrivateKeyBytes(byte[] privKeyBytes);
    protected abstract void storePublicKeyBytes(byte[] pubKeyBytes);

    private static KeyPair generateKeyPair() {
        try {
            final KeyPairGenerator kpg = KeyPairGenerator.getInstance(SIGNATURE_ALGORITHM);
            return kpg.generateKeyPair();
        } catch (final NoSuchAlgorithmException nsae) {
            throw becauseEdHasGone();
        }
    }

    private KeyPair generateAndStore() {
        final KeyPair kp = generateKeyPair();
        storePrivateKeyBytes(kp.getPrivate().getEncoded());
        storePublicKeyBytes(kp.getPublic().getEncoded());
        return kp;
    }

    private static KeyPair restoreKeyPair(final byte[] priv, final byte[] pub) {
        try {
            final KeyFactory kf = KeyFactory.getInstance(SIGNATURE_ALGORITHM);
            final PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(priv));
            final PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(pub));
            return new KeyPair(publicKey, privateKey);
        } catch (final NoSuchAlgorithmException nsae) {
            throw becauseEdHasGone();
        } catch (final InvalidKeySpecException e) {
            throw becauseIllegal("Stored key is corrupted.");
        }
    }

    @Override
    public byte[] get() {
        return keyPair.getPublic().getEncoded();
    }

    @Override
    public byte[] apply(final byte[] payload) {
        try {
            final Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initSign(keyPair.getPrivate());
            sig.update(payload);
            return sig.sign();
        } catch (final NoSuchAlgorithmException nsae) {
            throw becauseEdHasGone();
        } catch (final InvalidKeyException e) {
            throw becauseIllegal("Invalid key.");
        } catch (final SignatureException e) {
            throw becauseImpossible("Signature failed: %s", e.getLocalizedMessage());
        }
    }

    @Override
    public boolean test(final ExposesSignature exposesSignature, final byte[] payload) {
        try {
            final KeyFactory kf = KeyFactory.getInstance(SIGNATURE_ALGORITHM);
            final PublicKey pubKey = kf.generatePublic(new X509EncodedKeySpec(exposesSignature.ed25519Pub()));
            final Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(pubKey);
            sig.update(payload);
            return sig.verify(exposesSignature.ed25519());
        } catch (final NoSuchAlgorithmException nsae) {
            throw becauseEdHasGone();
        } catch (final InvalidKeySpecException | InvalidKeyException e) {
            throw becauseIllegal("Invalid key.");
        } catch (final SignatureException e) {
            throw becauseImpossible("Verification failed: %s", e.getLocalizedMessage());
        }
    }
}

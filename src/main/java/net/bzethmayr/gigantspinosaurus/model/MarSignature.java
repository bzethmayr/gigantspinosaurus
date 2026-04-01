package net.bzethmayr.gigantspinosaurus.model;

public record MarSignature(
        byte[] ed25519Pub,
        byte[] ed25519
) {
}

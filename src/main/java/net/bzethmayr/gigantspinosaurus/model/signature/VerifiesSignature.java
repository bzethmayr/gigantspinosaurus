package net.bzethmayr.gigantspinosaurus.model.signature;

import java.util.function.BiPredicate;

public interface VerifiesSignature extends BiPredicate<ExposesSignature, byte[]> {
}

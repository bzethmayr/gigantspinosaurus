package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.correlation.Hashes;
import net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming;
import net.bzethmayr.gigantspinosaurus.model.nonce.GeneratesNonce;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.Signatory;
import net.bzethmayr.gigantspinosaurus.model.time.ExposesUtcDoubleSeconds;

public record BindsEnvironment(GeneratesNonce nonceSource,
                               Hashes hasher,
                               ExposesUtcDoubleSeconds timeSource,
                               ExposesPosition positionSource,
                               ExposesOrientation<?> orientationSource,
                               ExposesFraming framingSource,
                               Signatory signatory) {
}

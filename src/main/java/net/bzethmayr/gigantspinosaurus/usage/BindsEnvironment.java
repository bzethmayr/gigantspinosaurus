package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.gpu.GpuContext;
import net.bzethmayr.gigantspinosaurus.model.correlation.HashesMarFrame;
import net.bzethmayr.gigantspinosaurus.model.correlation.HashesMedia;
import net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming;
import net.bzethmayr.gigantspinosaurus.model.nonce.GeneratesNonce;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.Signatory;
import net.bzethmayr.gigantspinosaurus.model.time.ExposesUtcDoubleSeconds;

public record BindsEnvironment(GeneratesNonce nonceSource,
                               HashesMarFrame marHasher,
                               HashesMedia mediaHasher,
                               ExposesUtcDoubleSeconds timeSource,
                               ExposesPosition positionSource,
                               ExposesOrientation<?> orientationSource,
                               ExposesFraming framingSource,
                               Signatory signatory,
                               GpuContext gpuContext) {

    public BindsEnvironment(GeneratesNonce nonceSource,
                            HashesMarFrame marHasher,
                            HashesMedia mediaHasher,
                            ExposesUtcDoubleSeconds timeSource,
                            ExposesPosition positionSource,
                            ExposesOrientation<?> orientationSource,
                            ExposesFraming framingSource,
                            Signatory signatory) {
        this(nonceSource, marHasher, mediaHasher, timeSource, positionSource,
                orientationSource, framingSource, signatory, null);
    }

    public BindsEnvironment withPosition(final ExposesPosition positionSource) {
        return new BindsEnvironment(nonceSource, marHasher, mediaHasher, timeSource,
                positionSource, orientationSource, framingSource, signatory, gpuContext);
    }

    public BindsEnvironment withOrientation(final ExposesOrientation<?> orientationSource) {
        return new BindsEnvironment(nonceSource, marHasher, mediaHasher, timeSource, positionSource,
                orientationSource, orientationSource.framing(), signatory, gpuContext);
    }

    public BindsEnvironment withSignatory(final Signatory signatory) {
        return new BindsEnvironment(nonceSource, marHasher, mediaHasher, timeSource, positionSource, orientationSource, framingSource,
                signatory, gpuContext);
    }
}

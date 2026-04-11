package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.TestsModel;
import net.bzethmayr.gigantspinosaurus.model.correlation.Hashes;
import net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming;
import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.model.nonce.GeneratesNonce;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.Signatory;
import net.bzethmayr.gigantspinosaurus.model.time.ExposesUtcDoubleSeconds;
import org.junit.jupiter.api.Test;

import static net.bzethmayr.gigantspinosaurus.usage.BindsConstructors.defaultConstructors;
import static org.mockito.Mockito.mock;

class MarPublicationTest implements TestsModel {

    private MarPublication underTest;
    private BindsConstructors ctors = defaultConstructors();
    private GeneratesNonce nonceSource = mock();
    private Hashes hasher = mock();
    private ExposesUtcDoubleSeconds timeSource = mock();
    private ExposesPosition positionSource = mock();
    private ExposesOrientation<?> orientationSource = mock();
    private ExposesFraming framingSource = mock();
    private Signatory signatory = mock();
    private BindsEnvironment env;

    void setUpFromMocks() {
        env = new BindsEnvironment(
                nonceSource,
                hasher,
                timeSource,
                positionSource,
                orientationSource,
                framingSource,
                signatory
        );
        underTest = new MarPublication(ctors, env);
    }

    @Test
    void frameZero_producesFrameZero() {
        setUpFromMocks();

        final ExposesMar result = underTest.frameZero();
    }
}

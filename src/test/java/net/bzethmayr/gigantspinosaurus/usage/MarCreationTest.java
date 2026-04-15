package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.TestsModel;
import net.bzethmayr.gigantspinosaurus.model.TestsWithBytes;
import net.bzethmayr.gigantspinosaurus.model.correlation.HashesMarFrame;
import net.bzethmayr.gigantspinosaurus.model.correlation.HashesMedia;
import net.bzethmayr.gigantspinosaurus.model.framing.ExposesFraming;
import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.model.nonce.GeneratesNonce;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.Signatory;
import net.bzethmayr.gigantspinosaurus.model.time.ExposesUtcDoubleSeconds;
import net.bzethmayr.gigantspinosaurus.usage.MarCreation.MediaFrameReceiver;
import org.junit.jupiter.api.Test;

import static net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar.MAR_VERSION;
import static net.bzethmayr.gigantspinosaurus.usage.BindsConstructors.defaultConstructors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class MarCreationTest implements TestsModel, TestsWithBytes {

    private MarCreation underTest;
    private final BindsConstructors ctors = defaultConstructors();
    private final GeneratesNonce nonceSource = mock();
    private final HashesMedia mediaHasher = mock();
    private final HashesMarFrame marHasher = mock();
    private final ExposesUtcDoubleSeconds timeSource = mock();
    private final ExposesPosition positionSource = mock();
    private final ExposesOrientation<?> orientationSource = mock();
    private final ExposesFraming framingSource = mock();
    private final Signatory signatory = mock();

    void setUpFromMocks() {
        underTest = new MarCreation(ctors, new BindsEnvironment(
                nonceSource,
                marHasher,
                mediaHasher,
                timeSource,
                positionSource,
                orientationSource,
                framingSource,
                signatory
        ));
    }

    @Test
    void intentFrame_producesIntentFrame() {
        setUpFromMocks();

        final ExposesMar result = underTest.intentFrame();

        assertNotNull(result);
        assertEquals(MAR_VERSION, result.version());
        assertEquals(-1, result.index());
        verify(nonceSource, times(2)).getAsLong();
        verify(timeSource).utcDoubleSeconds();
        assertNotNull(result.position());
        assertNotNull(result.orientation());
        assertNotNull(result.signature());
    }

    @Test
    void intentToRecord_returnsFrameReceiver() {
        setUpFromMocks();

        final MediaFrameReceiver receiver = underTest.intentToRecord();
        verify(nonceSource, times(2)).getAsLong();
        final ExposesMar first = receiver.mediaFrame(fakeMediaBytes(65536), 0);
        final ExposesMar second = receiver.mediaFrame(fakeMediaBytes(65536), 1);
        verifyNoMoreInteractions(nonceSource);

        assertEquals(MAR_VERSION, first.version());
        assertEquals(0, first.index());
        verify(timeSource, times(3)).utcDoubleSeconds();
        assertNotNull(first.position());
        assertNotNull(first.orientation());
        assertNotNull(first.signature());
        assertEquals(MAR_VERSION, second.version());
        assertEquals(1, second.index());
        assertEquals(first.nonce(), second.nonce());
        assertNotNull(second.position());
        assertNotNull(second.orientation());
        assertNotNull(second.signature());
    }
}

package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.MarDecoder;
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

import static java.nio.ByteBuffer.wrap;
import static net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar.MAR_VERSION;
import static net.bzethmayr.gigantspinosaurus.usage.BindsConstructors.defaultConstructors;
import static net.bzethmayr.gigantspinosaurus.usage.BindsEnvironment.desktopEnvironment;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.*;
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
        doReturn(new byte[32]).when(mediaHasher).apply(any());
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
    void intentFrame_againstMocks_producesIntentFrame() {
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
    void intentToRecord_againstMocks_returnsFrameReceiver() {
        setUpFromMocks();

        final MediaFrameReceiver receiver = underTest.intentToRecord();
        verify(nonceSource, times(2)).getAsLong();
        final ExposesMar first = receiver.mediaFrame(fakeMediaBytes(MANY), 0);
        final ExposesMar second = receiver.mediaFrame(fakeMediaBytes(MANY), 1);
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

    void setUpForDesktopEphemeral() {
        underTest = new MarCreation(ctors, desktopEnvironment());
    }

    @Test
    void intentFrame_againstDesktopBindings_returnsRoundTripIntentFrame() {
        setUpForDesktopEphemeral();

        final ExposesMar result = underTest.intentFrame();
        final byte[] serialized = result.canonicalBytes();
        assertThat(serialized.length, lessThan(600));
        final ExposesMar parsed = MarDecoder.decode(wrap(serialized));

        assertEquals(result, parsed);
    }

    @Test
    void intentToRecord_givenIntentFrame_againstDesktopBindings_returnsRoundTripReceiver() {
        setUpForDesktopEphemeral();

        final ExposesMar intentFrame = underTest.intentFrame();
        final MediaFrameReceiver receiver = underTest.intentToRecord(intentFrame);
        final ExposesMar firstFrame = receiver.mediaFrame(fakeMediaBytes(LOTS), 0);
        final ExposesMar secondFrame = receiver.mediaFrame(fakeMediaBytes(LOTS), 1);

        final long nonce = intentFrame.nonce();
        assertEquals(-1, intentFrame.index());
        assertNotEquals(0L, intentFrame.priorSipH4_8());
        assertNotNull(intentFrame.mediaBLK3());
        assertEquals(0, intentFrame.mediaBLK3()[0]);
        assertNotEquals(0L, intentFrame.currentSipH4_8());
        assertNotNull(intentFrame.signature());
        final int intentSize = intentFrame.canonicalBytes().length;
        assertNotNull(intentFrame.signature());
        assertEquals(nonce, firstFrame.nonce());
        assertEquals(0, firstFrame.index());
        assertEquals(intentFrame.currentSipH4_8(), firstFrame.priorSipH4_8());
        assertNotNull(firstFrame.mediaBLK3());
        assertNotEquals(0L, firstFrame.currentSipH4_8());
        assertNotNull(firstFrame.signature());
        assertEquals(intentSize, firstFrame.canonicalBytes().length);
        assertEquals(nonce, secondFrame.nonce());
        assertEquals(1, secondFrame.index());
        assertEquals(firstFrame.currentSipH4_8(), secondFrame.priorSipH4_8());
        assertNotNull(secondFrame.mediaBLK3());
        assertNotEquals(0L, secondFrame.currentSipH4_8());
        assertNotNull(secondFrame.signature());
        assertEquals(intentSize, secondFrame.canonicalBytes().length);
    }
}

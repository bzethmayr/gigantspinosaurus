package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.TestsModel;
import net.bzethmayr.gigantspinosaurus.model.TestsWithBytes;
import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.usage.MarCreation.MediaFrameReceiver;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.usage.BindsEnvironment.desktopEnvironment;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarVerificationTest implements TestsModel, TestsWithBytes {

    private MarCreation creation;
    private MarVerification underTest;

    void setUpDesktopEphemeral() {
        final BindsEnvironment env = desktopEnvironment();
        creation = new MarCreation(BindsConstructors.defaultConstructors(), env);
        underTest = new MarVerification(env);
    }

    @Test
    void verifyIntent_givenValidIntentFrame_returnsValid() {
        setUpDesktopEphemeral();
        final ExposesMar original = creation.intentFrame();

        final boolean result = underTest.verifyIntent(original);

        assertTrue(result);
        dump(original.canonicalBytes());
    }

    @Test
    void verifyMar_givenValidIntentFrameAndAnyMedia_returnsValid() {
        setUpDesktopEphemeral();
        final ExposesMar original = creation.intentFrame();

        final boolean result = underTest.verifyMar(original, fakeMediaBytes(16384));

        assertTrue(result);
    }

    @Test
    void verifyMediaFrame_givenFrameAndValidMedia_returnsValid() {
        setUpDesktopEphemeral();
        final MediaFrameReceiver receiver = creation.intentToRecord();
        final ByteBuffer fakeFrame = fakeMediaBytes(16384);
        final ExposesMar frameZero = receiver.mediaFrame(fakeFrame, 0);

        final boolean result = underTest.verifyMedia(frameZero, fakeFrame);

        assertTrue(result);
    }

    @RepeatedTest(1024)
    void verifyMediaFrame_givenFrameAndInvalidMedia_neverReturnsValid() {
        setUpDesktopEphemeral();
        final MediaFrameReceiver receiver = creation.intentToRecord();
        final ExposesMar frameZero = receiver.mediaFrame(fakeMediaBytes(1024), 0);

        final boolean result = underTest.verifyMedia(frameZero, fakeMediaBytes(1024));

        assertFalse(result);
    }

}

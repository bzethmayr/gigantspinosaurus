package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.TestsModel;
import net.bzethmayr.gigantspinosaurus.model.TestsWithBytes;
import net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar;
import net.bzethmayr.gigantspinosaurus.usage.MarCreation.ReducedFrameReceiver;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.usage.BindsEnvironment.desktopEnvironment;
import static net.zethmayr.fungu.test.TestConstants.TEST_RANDOM;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarVerificationTest implements TestsModel, TestsWithBytes {

    private MarCreation creation;
    private MarVerification underTest;

    void setUpDesktopEphemeral() {
        final BindsConstructors ctors = BindsConstructors.defaultConstructors();
        final BindsEnvironment env = desktopEnvironment();
        creation = new MarCreation(ctors, env);
        underTest = new MarVerification(ctors, env);
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

        final boolean result = underTest.verifyMar(original, fakeMediaBytes(SOME));

        assertTrue(result);
    }

    @RepeatedTest(16)
    void verifyMediaFrame_givenFrameAndValidMedia_returnsValid() {
        setUpDesktopEphemeral();
        final ReducedFrameReceiver receiver = creation.intentToRecord();
        final ByteBuffer fakeFrame = fakeMediaBytes(LOTS);
        final ExposesMar frameZero = receiver.reducedFrame(fakeFrame, 0);

        final boolean result = underTest.verifyMedia(frameZero, fakeFrame);

        assertTrue(result);
    }

    @RepeatedTest(FEW)
    void verifyMediaFrame_givenFrameAndInvalidMedia_neverReturnsValid() {
        setUpDesktopEphemeral();
        final ReducedFrameReceiver receiver = creation.intentToRecord();
        final ExposesMar frameZero = receiver.reducedFrame(fakeMediaBytes(SOME), 0);

        final boolean result = underTest.verifyMedia(frameZero, fakeMediaBytes(SOME));

        assertFalse(result);
    }

    @RepeatedTest(64)
    void verifyMediaFrame_givenFrameAndCorruptedMedia_neverReturnsValid() {
        setUpDesktopEphemeral();
        final ReducedFrameReceiver receiver = creation.intentToRecord();
        final ByteBuffer fakeFrame = fakeMediaBytes(MANY);
        final ExposesMar frameZero = receiver.reducedFrame(fakeFrame, 0);
        fakeFrame.array()[TEST_RANDOM.nextInt(0, MANY)] ^= 1;

        final boolean result = underTest.verifyMedia(frameZero, fakeFrame);

        assertFalse(result);
    }


}

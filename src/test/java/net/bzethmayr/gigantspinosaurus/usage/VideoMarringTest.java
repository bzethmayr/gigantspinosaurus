package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.TestsModel;
import net.bzethmayr.gigantspinosaurus.model.TestsWithBytes;
import net.bzethmayr.gigantspinosaurus.model.media.MarksMedia;
import net.bzethmayr.gigantspinosaurus.model.media.PreparesMark;
import net.bzethmayr.gigantspinosaurus.model.media.ReducesMedia;
import net.bzethmayr.gigantspinosaurus.model.media.ReductionStep;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoMarringTest implements TestsModel, TestsWithBytes {
    private static final int CADENCE = 60;
    private static final int EMPTY = 10;

    private BindsMediaPipeline pipeline;
    private VideoMarring underTest;

    @SafeVarargs
    private void setUpMockPipeline(final Consumer<BindsMediaPipeline>... configs) {
        final ReducesMedia mockReduction = mock();
        final PreparesMark mockPreparation = mock();
        final MarksMedia mockMarking = mock();
        pipeline = new BindsMediaPipeline(mockReduction, mockPreparation, mockMarking);
        Stream.of(configs).forEach(c -> c.accept(pipeline));
        underTest = new VideoMarring(
                BindsConstructors.defaultConstructors(), BindsEnvironment.desktopEnvironment(),
                pipeline, CADENCE, EMPTY);
    }

    private Consumer<BindsMediaPipeline> reducerSteps(final ReductionStep... steps) {
        return p -> doReturn(steps).when(p.reducer()).reductions();
    }

    private Consumer<BindsMediaPipeline> fakeReducer() {
        return p -> doAnswer(iom ->
                fakeMediaBytes(SOME)).when(p.reducer()).apply(any());
    }

    private Consumer<BindsMediaPipeline> fakePreparer() {
        return p -> doAnswer(iom ->
                fakeMediaBytes(MANY)).when(p.combiner()).emptyMark();
    }

    private Consumer<BindsMediaPipeline> minimalFakes() {
        return reducerSteps().andThen(fakeReducer()).andThen(fakePreparer());
    }

    @Test
    void background_onMediaThread_throwsOnUse() {
        setUpMockPipeline(minimalFakes());
        final VideoMarring.MediaFrameAcceptor media = underTest.mediaFrame();
        final VideoMarring.BackgroundCalculator background = underTest.background();
        final ByteBuffer rawFrame = fakeMediaBytes(LOTS);

        assertDoesNotThrow(() -> media.accept(rawFrame, 0));
        assertThrows(IllegalArgumentException.class, background::calculate);
    }
}
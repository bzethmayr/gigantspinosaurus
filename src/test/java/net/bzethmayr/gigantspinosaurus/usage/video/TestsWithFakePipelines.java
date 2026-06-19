package net.bzethmayr.gigantspinosaurus.usage.video;

import net.bzethmayr.gigantspinosaurus.model.TestsWithBytes;
import net.bzethmayr.gigantspinosaurus.model.media.MarksMedia;
import net.bzethmayr.gigantspinosaurus.model.media.PreparesMark;
import net.bzethmayr.gigantspinosaurus.model.media.ReducesMedia;
import net.bzethmayr.gigantspinosaurus.model.media.ReductionStep;
import net.bzethmayr.gigantspinosaurus.usage.BindsMarkingPipeline;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static net.bzethmayr.gigantspinosaurus.usage.BindsConstructors.defaultConstructors;
import static net.bzethmayr.gigantspinosaurus.usage.defaults.DefaultEnvironments.desktopEnvironment;
import static net.bzethmayr.gigantspinosaurus.usage.video.VideoMarringCoordinator.blockingCoordinator;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doAnswer;

public interface TestsWithFakePipelines extends TestsWithPipelines, TestsWithBytes {
    default MarkingTestPipeline mockPipeline(
            final int cadence,
            final int empty,
            final Consumer<BindsMarkingPipeline>... configs
    ) {
        final ReducesMedia mockReduction = mock();
        final PreparesMark mockPreparation = mock();
        final MarksMedia mockMarking = mock();
        final BindsMarkingPipeline pipeline = new BindsMarkingPipeline(mockReduction, mockPreparation, mockMarking);
        Stream.of(configs).forEach(c -> c.accept(pipeline));
        final VideoMarring marring = new VideoMarring(defaultConstructors(), desktopEnvironment(),
                pipeline, blockingCoordinator(), cadence, empty);
        return new MarkingTestPipeline(pipeline, marring);
    }

    default Consumer<BindsMarkingPipeline> reducerSteps(final ReductionStep... steps) {
        return p -> doReturn(steps).when(p.reducer()).reductions();
    }

    default Consumer<BindsMarkingPipeline> fakeReducer() {
        return p -> doAnswer(iom ->
                fakeMediaBytes(SOME)).when(p.reducer()).apply(any());
    }

    default Consumer<BindsMarkingPipeline> fakePreparer() {
        return p -> doAnswer(iom ->
                fakeMediaBytes(MANY)).when(p.encoder()).emptyMark(anyInt());
    }

    default Consumer<BindsMarkingPipeline> minimalFakes() {
        return reducerSteps().andThen(fakeReducer()).andThen(fakePreparer());
    }

}

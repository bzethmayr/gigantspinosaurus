package net.bzethmayr.gigantspinosaurus.usage.video;

import net.bzethmayr.gigantspinosaurus.usage.BindsMarkingPipeline;
import net.zethmayr.fungu.test.ExampleUncheckedException;

public interface TestsWithPipelines {

    record MarkingTestPipeline(BindsMarkingPipeline pipeline, VideoMarring marring) {

    }

    default ExampleUncheckedException deviceLost() {
        return new ExampleUncheckedException("GPU device lost");
    }

    default ExampleUncheckedException gpuCrash() {
        return new ExampleUncheckedException("gpu crash");
    }
}

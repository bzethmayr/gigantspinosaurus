package net.bzethmayr.gigantspinosaurus.usage.video;

import net.bzethmayr.gigantspinosaurus.usage.BindsMarkingPipeline;

public interface TestsWithPipelines {

    record MarkingTestPipeline(BindsMarkingPipeline pipeline, VideoMarring marring) {

    }

}

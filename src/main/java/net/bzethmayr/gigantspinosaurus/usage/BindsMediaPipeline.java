package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.media.MarksMedia;
import net.bzethmayr.gigantspinosaurus.model.media.PreparesMark;
import net.bzethmayr.gigantspinosaurus.model.media.ReducesMedia;

public record BindsMediaPipeline(
        ReducesMedia reducer,
        PreparesMark combiner,
        MarksMedia marker) {
}

package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.media.MarksMedia;
import net.bzethmayr.gigantspinosaurus.model.media.PreparesMark;
import net.bzethmayr.gigantspinosaurus.model.media.ReducesMedia;

/**
 * A media marking pipeline.
 * @param reducer the frame reducing function.
 * @param encoder updates the mark based on new MARs.
 * @param marker applies the mark to a frame.
 */
public record BindsMarkingPipeline(
        ReducesMedia reducer,
        PreparesMark encoder,
        MarksMedia marker) {
}

/**
 * Media reduction and hashing abstractions.
 *
 * <p>Defines the shape ({@link net.bzethmayr.gigantspinosaurus.model.media.ExposesMedia})
 * and construction ({@link net.bzethmayr.gigantspinosaurus.model.media.CreatesMedia})
 * of the media component within a MAR frame.
 *
 * <p>Reductions are described by {@link net.bzethmayr.gigantspinosaurus.model.media.ReductionStep}
 * and applied via {@link net.bzethmayr.gigantspinosaurus.model.media.ReducesMedia}.
 * {@link net.bzethmayr.gigantspinosaurus.model.media.MarksMedia} and
 * {@link net.bzethmayr.gigantspinosaurus.model.media.PreparesMark} handle
 * the watermarking pipeline.
 */
@LowerLevel
package net.bzethmayr.gigantspinosaurus.model.media;

import net.zethmayr.fungu.core.declarations.LowerLevel;

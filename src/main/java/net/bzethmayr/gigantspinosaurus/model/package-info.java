/**
 * Top-level model consolidations and the MAR data model.
 *
 * <p>This package defines the canonical record types used across the system:
 * {@link net.bzethmayr.gigantspinosaurus.model.MinimalAttestationRecord},
 * {@link net.bzethmayr.gigantspinosaurus.model.Framing},
 * {@link net.bzethmayr.gigantspinosaurus.model.Geoposition},
 * {@link net.bzethmayr.gigantspinosaurus.model.Media},
 * {@link net.bzethmayr.gigantspinosaurus.model.MarSignature}, and
 * {@link net.bzethmayr.gigantspinosaurus.model.Orientation}.
 *
 * <p>Subpackages provide the capability interface for each constituent
 * (e.g. {@code model.signature}, {@code model.position}) and the
 * serialization/deserialization layer (e.g. {@code model.mar}, {@code model.media}).
 */
@HigherLevel
package net.bzethmayr.gigantspinosaurus.model;

import net.zethmayr.fungu.core.declarations.HigherLevel;
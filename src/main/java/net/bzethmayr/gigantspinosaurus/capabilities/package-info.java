/**
 * Byte-level attribute mapping, versioning, and canonical serialization.
 *
 * <p>{@link net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes}
 * and {@link net.bzethmayr.gigantspinosaurus.capabilities.HasMappedAttributes}
 * define the contract for attribute-based byte serialization.
 * {@link net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes} and
 * {@link net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations}
 * provide the binding and encoding machinery.
 *
 * <p>Supporting types include version tracking
 * ({@link net.bzethmayr.gigantspinosaurus.capabilities.Versioned}),
 * mutable resource reset ({@link net.bzethmayr.gigantspinosaurus.capabilities.Resettable}),
 * and canonical decoder helpers ({@link net.bzethmayr.gigantspinosaurus.capabilities.DecoderHelper}).
 */
@LowerLevel
package net.bzethmayr.gigantspinosaurus.capabilities;

import net.zethmayr.fungu.core.declarations.LowerLevel;
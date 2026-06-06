/**
 * Application-layer entry points for the MAR lifecycle.
 *
 * <p>This package orchestrates creation ({@link net.bzethmayr.gigantspinosaurus.usage.MarCreation}),
 * decoding ({@link net.bzethmayr.gigantspinosaurus.usage.MarDecoding}), verification
 * ({@link net.bzethmayr.gigantspinosaurus.usage.MarVerification}), and hashing
 * ({@link net.bzethmayr.gigantspinosaurus.usage.defaults.SipMarHasher},
 * {@link net.bzethmayr.gigantspinosaurus.usage.defaults.Blake3MediaHasher}) of attestation records.
 *
 * <p>The {@code vk} subpackage provides a Vulkan-based GPU implementation;
 * the {@code desktop} subpackage provides desktop-platform stubs for
 * sensor readings.
 */
@HigherLevel
package net.bzethmayr.gigantspinosaurus.usage;

import net.zethmayr.fungu.core.declarations.HigherLevel;
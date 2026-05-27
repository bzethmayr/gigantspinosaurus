/**
 * MAR frame shape and deserialization.
 *
 * <p>{@link net.bzethmayr.gigantspinosaurus.model.mar.ExposesMar} defines the
 * field layout and canonical attribute binding of a Minimal Attestation Record.
 * {@link net.bzethmayr.gigantspinosaurus.model.mar.CreatesMar} provides the
 * factory contract and a canonical decoder for reconstructing MAR frames
 * from byte-level input.
 */
@LowerLevel
package net.bzethmayr.gigantspinosaurus.model.mar;

import net.zethmayr.fungu.core.declarations.LowerLevel;

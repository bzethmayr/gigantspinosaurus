package net.bzethmayr.gigantspinosaurus.model.media;

import net.zethmayr.fungu.core.declarations.SingleUse;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

/**
 * Extracts the mark mask that was applied to a media frame.
 * When the marking strategy uses temporal modulation,
 * this refers to the first/base modulation case.
 * Implementations are not assumed to be stateless.
 */
@FunctionalInterface
@SingleUse
public interface ExtractsMarks extends UnaryOperator<ByteBuffer> {
}

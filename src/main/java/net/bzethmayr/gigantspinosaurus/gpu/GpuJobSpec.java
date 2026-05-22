package net.bzethmayr.gigantspinosaurus.gpu;

import net.bzethmayr.gigantspinosaurus.capabilities.Resettable;
import net.zethmayr.fungu.core.declarations.ReuseResults;
import net.zethmayr.fungu.core.declarations.SingleUse;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * Mutable spec builder provided to job submission.
 */
@SingleUse
public interface GpuJobSpec {
    /**
     * Job is empty.
     */
    Object[] NO_PARTS = {};

    /**
     * All stages before a barrier are assumed to be independent of each other.
     */
    sealed interface SpecPart permits Stage, Barrier {}

    /**
     * A stage runs a program on the GPU per the given user's specifications.
     * @param program the shader
     * @param user binds buffers and sets scalars.
     */
    record Stage(GpuProgram program, UsesGpuProgram user) implements SpecPart {}

    /**
     * A barrier must be reached by all prior stages before subsequent stages can proceed.
     */
    record Barrier() implements SpecPart {}

    /**
     * The parts accumulated by this builder.
     * @return the job parts.
     */
    Stream<SpecPart> parts();

    /**
     * Adds a {@link Stage stage} as the last part.
     * @param program the program being used.
     * @param user the usage specifier.
     * @return this builder.
     */
    GpuJobSpec stage(GpuProgram program, UsesGpuProgram user);

    /**
     * Adds a {@link Barrier barrier} as the last part.
     * @return this builder.
     */
    GpuJobSpec barrier();

    static GpuJobSpec emptySpec() {
        return new DefaultSpecImpl();
    }

    /**
     * Default implementation is resettable by owner
     */
    @ReuseResults
    final class DefaultSpecImpl implements GpuJobSpec, Resettable {
        private final List<SpecPart> parts = new CopyOnWriteArrayList<>();

        @Override
        public void reset() {
            parts.clear();
        }

        @Override
        public Stream<SpecPart> parts() {
            return parts.stream();
        }

        @Override
        public GpuJobSpec stage(GpuProgram program, UsesGpuProgram user) {
            parts.add(new Stage(program, user));
            return this;
        }

        @Override
        public GpuJobSpec barrier() {
            parts.add(new Barrier());
            return this;
        }
    }
}

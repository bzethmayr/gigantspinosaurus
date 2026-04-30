package net.bzethmayr.gigantspinosaurus.gpu;

import java.util.function.Consumer;

@FunctionalInterface
public interface UsesGpuProgram extends Consumer<GpuProgramLoan> {
}

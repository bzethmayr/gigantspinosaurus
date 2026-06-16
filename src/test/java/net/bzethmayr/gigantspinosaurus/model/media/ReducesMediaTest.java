package net.bzethmayr.gigantspinosaurus.model.media;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.*;
import static org.junit.jupiter.api.Assertions.*;

class ReducesMediaTest {

    @Test
    void reductions_prepend_addsStepsBeforeBase() {
        final var base = new ReductionStep(SPATIAL_ID, SPATIAL_VERSION);
        final var prepend = new ReductionStep((short) 99, (short) 1);
        final var underTest = new FakeReducer(base);

        final ReductionStep[] result = underTest.reductions(prepend);

        assertArrayEquals(new ReductionStep[]{prepend, base}, result);
    }

    @Test
    void reductions_multiplePrepend_stepsInCorrectOrder() {
        final var base = new ReductionStep(YCBCR_ID, YCBCR_VERSION);
        final var a = new ReductionStep((short) 10, (short) 0);
        final var b = new ReductionStep((short) 20, (short) 1);
        final var underTest = new FakeReducer(base);

        final ReductionStep[] result = underTest.reductions(a, b);

        assertArrayEquals(new ReductionStep[]{a, b, base}, result);
    }

    @Test
    void reductions_noPrepend_returnsBaseArray() {
        final var step = new ReductionStep(YCBCR_ID, YCBCR_VERSION);
        final var underTest = new FakeReducer(step);

        final ReductionStep[] result = underTest.reductions();

        assertArrayEquals(new ReductionStep[]{step}, result);
    }

    private static class FakeReducer implements ReducesMedia {
        private final ReductionStep[] steps;

        FakeReducer(final ReductionStep... steps) {
            this.steps = steps;
        }

        @Override
        public ReductionStep[] reductions() {
            return steps;
        }

        @Override
        public ByteBuffer apply(final ByteBuffer byteBuffer) {
            return byteBuffer;
        }
    }
}

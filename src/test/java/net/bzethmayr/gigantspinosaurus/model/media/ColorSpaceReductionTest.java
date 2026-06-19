package net.bzethmayr.gigantspinosaurus.model.media;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.YCBCR_ID;
import static net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.YCBCR_VERSION;
import static org.junit.jupiter.api.Assertions.*;

class ColorSpaceReductionTest {

    @Test
    void reductions_returnsSingleYcbcrStep() {
        final var underTest = new ColorSpaceReduction(2, 2);
        final ReductionStep[] steps = underTest.reductions();
        assertArrayEquals(new ReductionStep[]{new ReductionStep(YCBCR_ID, YCBCR_VERSION)}, steps);
    }

    @Test
    void apply_singlePixel_returnsCorrectLuma() {
        // RGBA: R=255, G=0, B=0, slack=0 -> Y = (77*255) >> 8 = 76
        final ByteBuffer input = ByteBuffer.allocateDirect(4)
                .put((byte) 0xFF).put((byte) 0x00).put((byte) 0x00).put((byte) 0x00).flip();
        final var underTest = new ColorSpaceReduction(1, 1);

        final ByteBuffer output = underTest.apply(input);

        assertEquals(4, output.remaining());
        assertEquals(76, output.getInt());
    }

    @Test
    void apply_greenPixel_returnsCorrectLuma() {
        // RGBA: R=0, G=255, B=0 -> Y = (150*255) >> 8 = 149
        final ByteBuffer input = ByteBuffer.allocateDirect(4)
                .put((byte) 0x00).put((byte) 0xFF).put((byte) 0x00).put((byte) 0x00).flip();
        final var underTest = new ColorSpaceReduction(1, 1);

        final ByteBuffer output = underTest.apply(input);

        assertEquals(4, output.remaining());
        assertEquals(149, output.getInt());
    }

    @Test
    void apply_bluePixel_returnsCorrectLuma() {
        // RGBA: R=0, G=0, B=255 -> Y = (29*255) >> 8 = 28
        final ByteBuffer input = ByteBuffer.allocateDirect(4)
                .put((byte) 0x00).put((byte) 0x00).put((byte) 0xFF).put((byte) 0x00).flip();
        final var underTest = new ColorSpaceReduction(1, 1);

        final ByteBuffer output = underTest.apply(input);

        assertEquals(4, output.remaining());
        assertEquals(28, output.getInt());
    }

    @Test
    void apply_whitePixel_returnsCorrectLuma() {
        // RGBA: R=255, G=255, B=255 -> Y = (77+150+29)*255 >> 8 = (256*255) >> 8 = 255
        final ByteBuffer input = ByteBuffer.allocateDirect(4)
                .put((byte) 0xFF).put((byte) 0xFF).put((byte) 0xFF).put((byte) 0x00).flip();
        final var underTest = new ColorSpaceReduction(1, 1);

        final ByteBuffer output = underTest.apply(input);

        assertEquals(4, output.remaining());
        assertEquals(255, output.getInt());
    }

    @Test
    void apply_twoByTwoGrid_outputHasFourInts() {
        final ByteBuffer input = ByteBuffer.allocateDirect(4 * 4);
        for (int i = 0; i < 4; i++) {
            input.put((byte) 0x80).put((byte) 0x80).put((byte) 0x80).put((byte) 0x00);
        }
        input.flip();
        final var underTest = new ColorSpaceReduction(2, 2);

        final ByteBuffer output = underTest.apply(input);

        assertEquals(4 * 4, output.remaining());
        // All four pixels are medium gray: Y = (77+150+29)*128 >> 8 = 128
        for (int i = 0; i < 4; i++) {
            assertEquals(128, output.getInt());
        }
    }

    @Test
    void apply_inputBufferIsRewoundAndConsumed() {
        final ByteBuffer input = ByteBuffer.allocateDirect(4)
                .put((byte) 0xFF).put((byte) 0x00).put((byte) 0x00).put((byte) 0x00).flip();
        final var underTest = new ColorSpaceReduction(1, 1);

        underTest.apply(input);

        assertEquals(4, input.position());
    }

    @Test
    void apply_outputBufferIsFlipped() {
        final ByteBuffer input = ByteBuffer.allocateDirect(4)
                .put((byte) 0xFF).put((byte) 0x00).put((byte) 0x00).put((byte) 0x00).flip();
        final var underTest = new ColorSpaceReduction(1, 1);

        final ByteBuffer output = underTest.apply(input);

        assertEquals(0, output.position());
        assertTrue(output.limit() > 0);
    }
}

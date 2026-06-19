package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.media.ExtractsMar;
import net.bzethmayr.gigantspinosaurus.model.media.ExtractsMarks;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BindsExtractionPipelineTest {

    @Test
    void extractAndDecode_givenValidMaskAndMar_returnsMarBytes() {
        final var mask = ByteBuffer.allocate(4).putInt(0xDEADBEEF);
        mask.flip();
        final byte[] expected = new byte[]{1, 2, 3};
        final ExtractsMarks extractor = m -> {
            mask.rewind();
            return mask;
        };
        final ExtractsMar decoder = m -> expected;
        final var underTest = new BindsExtractionPipeline(() -> extractor, decoder);

        final Optional<byte[]> result = underTest.extractAndDecode(ByteBuffer.allocate(0));

        assertTrue(result.isPresent());
        assertArrayEquals(expected, result.get());
    }

    @Test
    void extractAndDecode_whenMaskHasNoRemaining_returnsEmpty() {
        final ByteBuffer emptyMask = ByteBuffer.allocate(0);
        final ExtractsMarks extractor = m -> emptyMask;
        final ExtractsMar decoder = m -> new byte[]{1, 2, 3};
        final var underTest = new BindsExtractionPipeline(() -> extractor, decoder);

        final Optional<byte[]> result = underTest.extractAndDecode(ByteBuffer.allocate(0));

        assertTrue(result.isEmpty());
    }

    @Test
    void extractAndDecode_whenDecodeReturnsEmpty_returnsEmpty() {
        final var mask = ByteBuffer.allocate(4).putInt(1).flip();
        final ExtractsMarks extractor = m -> {
            mask.rewind();
            return mask;
        };
        final ExtractsMar decoder = m -> new byte[0];
        final var underTest = new BindsExtractionPipeline(() -> extractor, decoder);

        final Optional<byte[]> result = underTest.extractAndDecode(ByteBuffer.allocate(0));

        assertTrue(result.isEmpty());
    }

    @Test
    void extractAndDecode_callsFactoryFreshEachTime() {
        final Supplier<ExtractsMarks> factory = mock(Supplier.class);
        final var mask = ByteBuffer.allocate(4).putInt(1).flip();
        when(factory.get()).thenReturn(m -> {
            mask.rewind();
            return mask;
        });
        final ExtractsMar decoder = m -> new byte[]{42};
        final var underTest = new BindsExtractionPipeline(factory, decoder);

        underTest.extractAndDecode(ByteBuffer.allocate(0));
        underTest.extractAndDecode(ByteBuffer.allocate(0));

        verify(factory, times(2)).get();
    }
}

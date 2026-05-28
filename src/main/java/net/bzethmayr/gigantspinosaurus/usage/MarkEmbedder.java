package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.media.MarksMedia;
import net.bzethmayr.gigantspinosaurus.model.media.PreparesMark;

import java.nio.ByteBuffer;

public class MarkEmbedder implements PreparesMark, MarksMedia {
    private static final int MARK_SIZE = 600;

    @Override
    public ByteBuffer emptyMark() {
        return ByteBuffer.allocateDirect(MARK_SIZE);
    }

    @Override
    public void accept(final byte[] marBytes, final ByteBuffer markBuffer) {
        markBuffer.clear();
        final int len = Math.min(marBytes.length, MARK_SIZE);
        markBuffer.put(marBytes, 0, len);
        while (markBuffer.hasRemaining()) {
            markBuffer.put((byte) 0);
        }
        markBuffer.flip();
    }

    @Override
    public void mark(final ByteBuffer mark, final ByteBuffer target) {
        final int markLen = Math.min(mark.remaining(), target.remaining());
        mark.rewind();
        target.rewind();
        for (int i = 0; i < markLen; i++) {
            final int mp = mark.get(i) & 0xFF;
            final int tp = target.get(i) & 0xFF;
            target.put(i, (byte) (tp ^ mp));
        }
    }
}

package net.bzethmayr.gigantspinosaurus.util;

public record ClosingChain(AutoCloseable res, ClosingChain parent) implements AutoCloseable {
    public ClosingChain(AutoCloseable res) {
        this(res, null);
    }

    public ClosingChain link(AutoCloseable res) {
        return new ClosingChain(res, this);
    }

    @Override
    public void close() {
        Exception caught = null;
        try {
            res.close();
        } catch (final Exception e) {
            caught = e;
        } finally {
            if (parent != null) {
                try {
                    parent.close();
                } catch (final Exception e) {
                    if (caught != null) {
                        e.addSuppressed(caught);
                    }
                }
            }
        }
        if (caught != null) {
            throw new RuntimeException("errors during close", caught);
        }
    }
}

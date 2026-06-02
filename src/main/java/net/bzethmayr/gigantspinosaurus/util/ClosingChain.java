package net.bzethmayr.gigantspinosaurus.util;

/**
 * Links the lifecycle of multiple resources.
 * Close propagates from child (most dependent) to parent (least dependent).
 * If linking siblings from the same parent -
 * note that doing so will _not_ result in close from a sibling chain affecting other siblings,
 * but _will_ result in multiple {@link #close()} calls to the parent chain.
 * This will still maintain call order but requires idempotent close operations.
 * @param res a resource
 * @param parent the (optional) parent chain.
 */
public record ClosingChain(AutoCloseable res, ClosingChain parent) implements AutoCloseable {
    public ClosingChain(AutoCloseable res) {
        this(res, null);
    }

    public ClosingChain link(AutoCloseable res) {
        return new ClosingChain(res, this);
    }

    public ClosingChain swap(AutoCloseable res) {
        return new ClosingChain(this, new ClosingChain(res));
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

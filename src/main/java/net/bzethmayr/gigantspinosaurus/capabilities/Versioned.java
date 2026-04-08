package net.bzethmayr.gigantspinosaurus.capabilities;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromShort;
import static net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes.AttributeBinder.binder;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

/**
 * Implementations have a canonical version attribute - this is a short, it should go at the end.
 */
public interface Versioned extends HasMappedAttributes {
    String VERSION_FIELD = "ver";

    short version();

    /**
     * Binds the version attribute at the end. If you bind more, it will probably no longer be at the end.
     * @return an attribute binder for {@value #VERSION_FIELD}.
     * @param <T> the bound type.
     */
    static <T extends Versioned> BoundAttributes.AttributeBinder<T> addsVersion() {
        return binder(adds(VERSION_FIELD, fromShort(T::version)));
    }
}

package net.bzethmayr.gigantspinosaurus.capabilities;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromShort;
import static net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes.AttributeBinder.binder;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;
import static net.zethmayr.fungu.core.ExceptionFactory.becauseImpossible;

/**
 * Implementations have a canonical version attribute - this is a short that goes at the front.
 */
public interface Versioned extends HasMappedAttributes {
    String VERSION_FIELD = "ver";

    short version();

    /**
     * Binds the version attribute.
     * @return an attribute binder for {@value #VERSION_FIELD}.
     * @param <T> the bound type.
     */
    static <T extends Versioned> BoundAttributes.AttributeBinder<T> addsVersion() {
        return binder(adds(VERSION_FIELD, fromShort(T::version)).andThen(m -> {
            if (m.size() > 1) {
                throw becauseImpossible("Version goes first.");
            }
        }));
    }
}

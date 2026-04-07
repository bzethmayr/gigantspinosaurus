package net.bzethmayr.gigantspinosaurus.capabilities;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.fromShort;
import static net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes.AttributeBinder.binder;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

public interface Versioned {
    String VERSION_FIELD = "ver";

    short version();

    static <T extends Versioned> BoundAttributes.AttributeBinder<T> addsVersion() {
        return binder(adds(VERSION_FIELD, fromShort(T::version)));
    }
}

package net.bzethmayr.gigantspinosaurus.capabilities;

import net.zethmayr.fungu.Fork;

import java.util.SequencedMap;
import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.toSequencedMap;
import static net.zethmayr.fungu.ForkFactory.over;

public interface HasCanonicalAttributes extends HasMappedAttributes {
    SequencedSet<String> getCanonicalAttributes();

    default SequencedMap<String, byte[]> getCanonicalAttributeValues() {
        return getCanonicalAttributes().stream()
                .map(over(this::getAttributeValue))
                .collect(toSequencedMap(Fork::top, Fork::bottom));
    }
}

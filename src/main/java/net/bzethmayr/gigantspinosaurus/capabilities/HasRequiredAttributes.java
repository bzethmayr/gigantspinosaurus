package net.bzethmayr.gigantspinosaurus.capabilities;

import java.util.SequencedSet;

public interface HasRequiredAttributes extends HasCanonicalAttributes {
    SequencedSet<String> getRequiredAttributes();
}

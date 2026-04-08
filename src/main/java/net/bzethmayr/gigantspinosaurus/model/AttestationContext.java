package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.capabilities.HasMappedAttributes;
import net.zethmayr.fungu.core.declarations.NotDone;


import java.util.SequencedMap;

@NotDone
public class AttestationContext implements HasMappedAttributes {
    private final SequencedMap<String, byte[]> contextValues;

    public AttestationContext(final SequencedMap<String, byte[]> initialValues) {
        contextValues = HasMappedAttributes.deepCopyMappedAttributes(initialValues);
    }

    public AttestationContext(final AttestationContext prior) {
        this(prior.contextValues);
    }

    @Override
    public byte[] getAttributeValue(String attributeName) {
        return contextValues.get(attributeName);
    }
}

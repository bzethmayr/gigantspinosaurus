package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.Geoposition;
import net.bzethmayr.gigantspinosaurus.model.MarSignature;
import net.bzethmayr.gigantspinosaurus.model.MinimalAttestationRecord;
import net.bzethmayr.gigantspinosaurus.model.Orientation;
import net.bzethmayr.gigantspinosaurus.model.mar.CreatesMar;
import net.bzethmayr.gigantspinosaurus.model.orientation.CreatesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.CreatesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.CreatesSignature;

public record BindsConstructors(
        CreatesMar<?> marCtor,
        CreatesPosition<?> positionCtor,
        CreatesOrientation<?> orientationCtor,
        CreatesSignature<?> signatureCtor
) {

    public static BindsConstructors defaultConstructors() {
        return new BindsConstructors(
                MinimalAttestationRecord::new,
                Geoposition::new,
                (CreatesOrientation) Orientation::new,
                MarSignature::new
        );
    }
}

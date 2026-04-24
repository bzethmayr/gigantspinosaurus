package net.bzethmayr.gigantspinosaurus.usage;

import net.bzethmayr.gigantspinosaurus.model.*;
import net.bzethmayr.gigantspinosaurus.model.mar.CreatesMar;
import net.bzethmayr.gigantspinosaurus.model.media.CreatesMedia;
import net.bzethmayr.gigantspinosaurus.model.orientation.CreatesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.CreatesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.CreatesSignature;

public record BindsConstructors(
        CreatesMar<?> marCtor,
        CreatesPosition<?> positionCtor,
        CreatesOrientation<?> orientationCtor,
        CreatesMedia<?> mediaCtor,
        CreatesSignature<?> signatureCtor
) {

    public static BindsConstructors defaultConstructors() {
        return new BindsConstructors(
                MinimalAttestationRecord::new,
                Geoposition::new,
                (CreatesOrientation) Orientation::new,
                Media::new,
                MarSignature::new
        );
    }
}

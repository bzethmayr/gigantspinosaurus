package net.bzethmayr.gigantspinosaurus.model.mar;

import net.bzethmayr.gigantspinosaurus.capabilities.BoundAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.HasCanonicalAttributes;
import net.bzethmayr.gigantspinosaurus.capabilities.Versioned;
import net.bzethmayr.gigantspinosaurus.model.media.ExposesMedia;
import net.bzethmayr.gigantspinosaurus.model.orientation.ExposesOrientation;
import net.bzethmayr.gigantspinosaurus.model.position.ExposesPosition;
import net.bzethmayr.gigantspinosaurus.model.signature.ExposesSignature;

import java.nio.ByteBuffer;
import java.util.SequencedSet;

import static net.bzethmayr.gigantspinosaurus.capabilities.AttributeValuations.*;
import static net.bzethmayr.gigantspinosaurus.util.CollectionHelper.adds;

/**
 * The shape of the MAR frame.
 */
public interface ExposesMar extends HasCanonicalAttributes {
    String MAR_FIELD = "mar";
    String NONCE_FIELD = "nonce";
    String INDEX_FIELD = "index";
    String PRIOR_HASH_FIELD = "priorSipH4_8";
    String TIME_FIELD = "utcEpochSeconds";
    String POSITION_FIELD = "position";
    String ORIENTATION_FIELD = "orientation";
    String MEDIA_FIELD = "media";
    String CURRENT_HASH_FIELD = "currentSipH4_8";
    String SIGNATURE_FIELD = "signature";

    /**
     * 3 - unusably old
     * 4 - renamed hash fields to `previous/currentSipH4_8`
     * 5 - renamed hash fields to `prev/curr_Mxx64_FsipH4_8`
     * 6 - adds mediaBLK3 field, back to `prior/currentSipH4_8` naming
     * 7 - removes mediaBLK3 field and adds media object
     */
    short MAR_VERSION = 7;
    long nonce();
    int index();
    long priorSipH4_8();
    double utcEpochSeconds();
    ExposesPosition position();
    ExposesOrientation<?> orientation();
    ExposesMedia media();
    long currentSipH4_8();
    ExposesSignature signature();

    BoundAttributes<ExposesMar> ACCESSORS = new BoundAttributes<>(
            ExposesMar.class,
            Versioned.addsVersion(),
            adds(NONCE_FIELD, fromLong(ExposesMar::nonce)),
            adds(INDEX_FIELD, fromInt(ExposesMar::index)),
            adds(PRIOR_HASH_FIELD, fromLong(ExposesMar::priorSipH4_8)),
            adds(TIME_FIELD, fromDouble(ExposesMar::utcEpochSeconds)),
            adds(POSITION_FIELD, fromConverted(ExposesMar::position, ExposesPosition::canonicalBytes)),
            adds(ORIENTATION_FIELD, fromConverted(ExposesMar::orientation, ExposesOrientation::canonicalBytes)),
            adds(MEDIA_FIELD, fromConverted(ExposesMar::media, ExposesMedia::canonicalBytes)),
            adds(CURRENT_HASH_FIELD, fromLong(ExposesMar::currentSipH4_8)),
            adds(SIGNATURE_FIELD, fromConverted(ExposesMar::signature, ExposesSignature::canonicalBytes))
    );

    @Override
    default short version() {
        return MAR_VERSION;
    }

    @Override
    default SequencedSet<String> getCanonicalAttributes() {
        return ACCESSORS.fieldNames();
    }

    @Override
    default byte[] getAttributeValue(String attributeName) {
        return ACCESSORS.getBoundValue(attributeName, this);
    }

    default byte[] sipHashKey() {
        final ByteBuffer out = ByteBuffer.allocate(16);
        out.putLong(nonce()).putLong(priorSipH4_8());
        return out.array();
    }
}

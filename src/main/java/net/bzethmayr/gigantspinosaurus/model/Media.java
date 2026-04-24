package net.bzethmayr.gigantspinosaurus.model;

import net.bzethmayr.gigantspinosaurus.model.media.ExposesMedia;
import net.bzethmayr.gigantspinosaurus.model.media.ReductionStep;

import java.util.Arrays;
import java.util.Objects;

public record Media(ReductionStep r0, ReductionStep r1, ReductionStep r2, ReductionStep r3, byte[] BLK3, short version) implements ExposesMedia {

    @Override
    public boolean equals(final Object other) {
        if (other instanceof ExposesMedia brother) {
            return Objects.equals(r0, brother.r0())
                    && Objects.equals(r1, brother.r1())
                    && Objects.equals(r2, brother.r2())
                    && Objects.equals(r3, brother.r3())
                    && Arrays.equals(BLK3, brother.BLK3())
                    && version == brother.version();
        }
        return false;
    }
}

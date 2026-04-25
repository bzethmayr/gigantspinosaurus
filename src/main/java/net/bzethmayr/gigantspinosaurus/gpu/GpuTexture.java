package net.bzethmayr.gigantspinosaurus.gpu;

import net.zethmayr.fungu.core.declarations.NotDone;

public interface GpuTexture extends AutoCloseable {
    int width();
    int height();
    int levels();

    @NotDone
    record TextureDesc(int width, int height, int levels) {};
}

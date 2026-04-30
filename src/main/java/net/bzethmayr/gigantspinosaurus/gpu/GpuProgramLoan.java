package net.bzethmayr.gigantspinosaurus.gpu;

import java.nio.ByteBuffer;

public interface GpuProgramLoan {
    GpuProgramLoan bindBuffer(int slot, GpuBuffer buffer);
    GpuProgramLoan bindTexture(int slot, GpuTexture texture);
    GpuProgramLoan setPushConstants(ByteBuffer data);

    void dispatch(int xGroups, int yGroups, int zGroups);
}
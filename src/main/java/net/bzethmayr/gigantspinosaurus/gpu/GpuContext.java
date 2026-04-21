package net.bzethmayr.gigantspinosaurus.gpu;

public interface GpuContext {
    GpuTextureLoan loanTexture2D(int w, int h, int internalFormat);
    GpuProgramLoan loanProgram(String glslSource);

    void bindImage(int unit, GpuTextureLoan tex, int access, int format);
    void bindUniform1i(int location, int value);

    void dispatch(int groupsX, int groupsY, int groupsZ);
    void memoryBarrier(int barriers);
}
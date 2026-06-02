// optional YCbCr reduction,
// with reduction ID 1 defined at net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.YCBCR_ID
// and reduction version tracked at net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.YCBCR_VERSION
// This is a versioned step - changes to the logic of this file must be accompanied by a version increase.

[[vk::binding(0, 0)]] RWStructuredBuffer<uint> inputRgb  : register(u0);
[[vk::binding(1, 0)]] RWStructuredBuffer<uint> outputY   : register(u1);

struct PushData {
    uint width;
    uint height;
};
[[vk::push_constant]] ConstantBuffer<PushData> PC : register(b0);

[numthreads(8, 8, 1)]
void main(uint3 tid : SV_DispatchThreadID) {
    if (tid.x >= PC.width || tid.y >= PC.height) return;

    uint packed = inputRgb[tid.y * PC.width + tid.x];
    uint r = packed & 0xFF;
    uint g = (packed >> 8) & 0xFF;
    uint b = (packed >> 16) & 0xFF;
    uint y = (77 * r + 150 * g + 29 * b) >> 8;

    outputY[tid.y * PC.width + tid.x] = y;
}

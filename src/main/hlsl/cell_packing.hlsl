// reduction result concatenation - structural,
// sharing reduction ID 2 defined at net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.SPATIAL_ID
// and reduction version tracked at net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.SPATIAL_VERSION
// This is part of a versioned step - changes to the logic of this file must be accompanied by a version increase.

[[vk::binding(0, 0)]] RWStructuredBuffer<uint> inputDwt   : register(u0);
[[vk::binding(1, 0)]] RWStructuredBuffer<uint> inputSobel : register(u1);
[[vk::binding(2, 0)]] RWStructuredBuffer<uint> outputPacked : register(u2);

struct PushData {
    uint cellsPerInput;
};
[[vk::push_constant]] ConstantBuffer<PushData> PC : register(b0);

[numthreads(6, 6, 1)]
void main(uint3 tid : SV_DispatchThreadID) {
    uint ti = tid.y * 6 + tid.x;
    uint src = ti * 4;

    uint a0 = inputDwt[src]     & 0xFF;
    uint a1 = inputDwt[src + 1] & 0xFF;
    uint a2 = inputDwt[src + 2] & 0xFF;
    uint a3 = inputDwt[src + 3] & 0xFF;
    outputPacked[ti] = (a3 << 24) | (a2 << 16) | (a1 << 8) | a0;

    uint b0 = inputSobel[src]     & 0xFF;
    uint b1 = inputSobel[src + 1] & 0xFF;
    uint b2 = inputSobel[src + 2] & 0xFF;
    uint b3 = inputSobel[src + 3] & 0xFF;
    outputPacked[ti + 36] = (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
}

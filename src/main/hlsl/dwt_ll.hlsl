// DWT (structure) reduction,
// sharing reduction ID 2 defined at net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.SPATIAL_ID
// and reduction version tracked at net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.SPATIAL_VERSION
// This is part of a versioned step - changes to the logic of this file must be accompanied by a version increase.

[[vk::binding(0, 0)]] RWStructuredBuffer<uint> inputY      : register(u0);
[[vk::binding(1, 0)]] RWStructuredBuffer<uint> outputDwt  : register(u1);

struct PushData {
    uint width;
    uint height;
    uint scale;  // right-shift for quantization
};
[[vk::push_constant]] ConstantBuffer<PushData> PC : register(b0);

[numthreads(12, 12, 1)]
void main(uint3 tid : SV_DispatchThreadID) {
    if (tid.x >= 12 || tid.y >= 12) return;

    uint ll_w = PC.width / 2;
    uint ll_h = PC.height / 2;

    uint cell_start_lx = (tid.x * ll_w) / 12;
    uint cell_end_lx   = ((tid.x + 1) * ll_w) / 12;
    uint cell_start_ly = (tid.y * ll_h) / 12;
    uint cell_end_ly   = ((tid.y + 1) * ll_h) / 12;

    uint sum = 0;
    uint count = 0;
    for (uint ly = cell_start_ly; ly < cell_end_ly; ly++) {
        for (uint lx = cell_start_lx; lx < cell_end_lx; lx++) {
            uint px = 2 * lx;
            uint py = 2 * ly;
            uint a = inputY[py * PC.width + px];
            uint b = inputY[py * PC.width + px + 1];
            uint c = inputY[(py + 1) * PC.width + px];
            uint d = inputY[(py + 1) * PC.width + px + 1];
            sum += (a + b + c + d) >> 2;
            count++;
        }
    }

    uint avg = count > 0 ? sum / count : 0;
    uint quantized = min(avg >> PC.scale, 0xFF);
    outputDwt[tid.y * 12 + tid.x] = quantized;
}

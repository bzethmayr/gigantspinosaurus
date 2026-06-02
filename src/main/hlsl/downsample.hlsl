// downsampler,
// sharing reduction ID 2 defined at net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.SPATIAL_ID
// and reduction version tracked at net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.SPATIAL_VERSION
// This is part of a versioned step - changes to the logic of this file must be accompanied by a version increase.

[[vk::binding(0, 0)]] RWStructuredBuffer<uint> inputY   : register(u0);
[[vk::binding(1, 0)]] RWStructuredBuffer<uint> outputY  : register(u1);

struct PushData {
    uint inWidth;
    uint inHeight;
    uint outWidth;
    uint outHeight;
};
[[vk::push_constant]] ConstantBuffer<PushData> PC : register(b0);

[numthreads(8, 8, 1)]
void main(uint3 tid : SV_DispatchThreadID) {
    if (tid.x >= PC.outWidth || tid.y >= PC.outHeight) return;

    uint ix_fp = (tid.x * PC.inWidth * 256) / PC.outWidth;
    uint iy_fp = (tid.y * PC.inHeight * 256) / PC.outHeight;
    uint ix0 = ix_fp >> 8;
    uint iy0 = iy_fp >> 8;
    uint ix1 = min(ix0 + 1, PC.inWidth - 1);
    uint iy1 = min(iy0 + 1, PC.inHeight - 1);
    uint fx = ix_fp & 0xFF;
    uint fy = iy_fp & 0xFF;

    uint ya = inputY[iy0 * PC.inWidth + ix0];
    uint yb = inputY[iy0 * PC.inWidth + ix1];
    uint yc = inputY[iy1 * PC.inWidth + ix0];
    uint yd = inputY[iy1 * PC.inWidth + ix1];

    uint top = ((256 - fx) * ya + fx * yb) >> 8;
    uint bot = ((256 - fx) * yc + fx * yd) >> 8;
    uint y  = ((256 - fy) * top + fy * bot) >> 8;

    outputY[tid.y * PC.outWidth + tid.x] = y;
}

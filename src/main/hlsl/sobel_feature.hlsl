// Sobel (feature) reduction,
// sharing reduction ID 2 defined at net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.SPATIAL_ID
// and reduction version tracked at net.bzethmayr.gigantspinosaurus.model.media.ReductionIds.SPATIAL_VERSION
// This is part of a versioned step - changes to the logic of this file must be accompanied by a version increase.

[[vk::binding(0, 0)]] RWStructuredBuffer<uint> inputY       : register(u0);
[[vk::binding(1, 0)]] RWStructuredBuffer<uint> outputSobel : register(u1);

struct PushData {
    uint width;
    uint height;
    uint strengthShift; // right-shift for mag quantization
};
[[vk::push_constant]] ConstantBuffer<PushData> PC : register(b0);

[numthreads(12, 12, 1)]
void main(uint3 tid : SV_DispatchThreadID) {
    if (tid.x >= 12 || tid.y >= 12) return;

    uint cell_start_x = (tid.x * PC.width) / 12;
    uint cell_end_x   = ((tid.x + 1) * PC.width) / 12;
    uint cell_start_y = (tid.y * PC.height) / 12;
    uint cell_end_y   = ((tid.y + 1) * PC.height) / 12;

    uint max_mag = 0;
    uint best_ox = 0;
    uint best_oy = 0;
    uint best_gx = 0;
    uint best_gy = 0;

    for (uint cy = cell_start_y; cy < cell_end_y; cy++) {
        for (uint cx = cell_start_x; cx < cell_end_x; cx++) {
            if (cx == 0 || cx >= PC.width - 1 || cy == 0 || cy >= PC.height - 1) continue;

            uint p0 = inputY[(cy - 1) * PC.width + (cx - 1)];
            uint p1 = inputY[(cy - 1) * PC.width + cx];
            uint p2 = inputY[(cy - 1) * PC.width + (cx + 1)];
            uint p3 = inputY[cy * PC.width + (cx - 1)];
            uint p4 = inputY[cy * PC.width + cx];
            uint p5 = inputY[cy * PC.width + (cx + 1)];
            uint p6 = inputY[(cy + 1) * PC.width + (cx - 1)];
            uint p7 = inputY[(cy + 1) * PC.width + cx];
            uint p8 = inputY[(cy + 1) * PC.width + (cx + 1)];

            uint gx = (p2 + 2 * p5 + p8) - (p0 + 2 * p3 + p6);
            uint gy = (p0 + 2 * p1 + p2) - (p6 + 2 * p7 + p8);
            uint mag = (gx & 0x80000000 ? ~gx + 1 : gx) + (gy & 0x80000000 ? ~gy + 1 : gy);

            if (mag > max_mag) {
                max_mag = mag;
                best_gx = gx;
                best_gy = gy;
                uint cell_w = cell_end_x - cell_start_x;
                uint cell_h = cell_end_y - cell_start_y;
                uint sub_w = cell_w > 0 ? cell_w : 1;
                uint sub_h = cell_h > 0 ? cell_h : 1;
                best_ox = ((cx - cell_start_x) * 4) / sub_w;
                best_oy = ((cy - cell_start_y) * 4) / sub_h;
            }
        }
    }

    uint orientation;
    uint abs_gx = best_gx & 0x80000000 ? ~best_gx + 1 : best_gx;
    uint abs_gy = best_gy & 0x80000000 ? ~best_gy + 1 : best_gy;
    if (abs_gx > abs_gy) {
        orientation = (best_gx & 0x80000000) ? 2 : 0;
    } else {
        orientation = (best_gy & 0x80000000) ? 3 : 1;
    }

    uint strength = min(max_mag >> PC.strengthShift, 3);

    uint packed = (strength << 6) | (best_ox << 4) | (best_oy << 2) | orientation;
    outputSobel[tid.y * 12 + tid.x] = packed;
}

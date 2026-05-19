// test_buffer.hlsl — minimal integer compute shader for buffer pipeline validation

// Vulkan 1.0 guaranteed storage buffers
[[vk::binding(0, 0)]] RWStructuredBuffer<uint> inputBuffer  : register(u0);
[[vk::binding(1, 0)]] RWStructuredBuffer<uint> outputBuffer : register(u1);

// Push constants for 2D boundaries (zero descriptor overhead)
struct PushData {
    uint width;
    uint height;
};
[[vk::push_constant]] ConstantBuffer<PushData> PushConstants : register(b0);

[numthreads(8, 8, 1)]
void main(uint3 tid : SV_DispatchThreadID) {
    // Bounds check to prevent out-of-bounds buffer writes
    if (tid.x >= PushConstants.width || tid.y >= PushConstants.height) {
        return;
    }

    // Manual 2D to 1D flat index calculation
    uint flatIndex = (tid.y * PushConstants.width) + tid.x;

    // Read from the 1D buffer
    uint v = inputBuffer[flatIndex];

    // Trivial bit twiddle validation logic
    uint x = (v << 1) ^ 0x5A5A5A5A;

    // Write back to the 1D buffer
    outputBuffer[flatIndex] = x;
}

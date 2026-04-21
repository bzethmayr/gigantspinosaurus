// test.hlsl — minimal integer compute shader for pipeline validation

// Input and output textures (uint per pixel)
RWTexture2D<uint> inputTex  : register(u0);
RWTexture2D<uint> outputTex : register(u1);

// 8×8 threads per workgroup (safe default)
[numthreads(8, 8, 1)]
void main(uint3 tid : SV_DispatchThreadID)
{
    uint v = inputTex[tid.xy];

    // trivial bit twiddle so we know it ran
    uint x = (v << 1) ^ 0x5A5A5A5A;

    outputTex[tid.xy] = x;
}
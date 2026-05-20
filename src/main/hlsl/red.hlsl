 // Invalid example - do not repeat these mistakes:
 // 1. Textures are not part of our model - we need to be working with buffers.
 // 2. A valid DWT for our purposes needs to include more than just the red channel.
 // 3. Produces compilation warnings (see build.gradle.kts for reproduction steps)
 RWTexture2D<uint> inputTex  : register(u0);
 RWTexture2D<uint> outputTex : register(u1);

 [numthreads(16, 16, 1)]
 void main(uint3 dispatchThreadID : SV_DispatchThreadID)
 {
     // 1. Calculate coordinates within the current block.
     uint2 blockCoord = dispatchThreadID.xy;

     // 2. Get Pixel data. (Need appropriate boundary logic)
     float P_left = inputTex[blockCoord].r; // Example
     float P_right = inputTex[float2(blockCoord.x + 1, blockCoord.y)].r;

     // 3. Horizontal Filtering (Low Pass)
     float L_h = (P_left + P_right) * 0.5f;

     // 4. Vertical Filtering (Low Pass)
     //  (Similar steps as horizontal - needs to correctly sample the previous
     //   horizontal output).
     float Final_Coefficient = L_h * 0.9f;  // Weighted Low Pass

     // 5. Output
     outputTex[dispatchThreadID.xy] = float4(Final_Coefficient, 0, 0, 1);
 }

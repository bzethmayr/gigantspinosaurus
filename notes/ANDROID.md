your "mutation" is an invisible watermark by design, you are no longer just using the AR system to understand the world; you are acting as a real-time digital signal processor (DSP) for the camera feed. You need to inject calculated, non-visible data into the raw image buffer itself.

The core ARCore APIs are designed for understanding reality, not for generating arbitrary pixel data. To achieve this, you must look at the low-level Android media and graphics APIs.

Here is a breakdown of the necessary APIs and the revised architecture.

🛰️ Required API Shift: From ARCore to Camera/Graphics Stack
The key is to intercept the video stream before it gets passed to the final rendering output, allowing your worker-derived data to modify the raw pixel data.

1. The Image Interception Hook (The Source)
   Recommended API: Android CameraX Library (or the lower-level Camera2 API).
   Component: You must use the ImageAnalysis Use Case.
   Function: This component provides an ImageAnalysis.Analyzer that receives continuous, raw ImageProxy objects from the camera feed. This ImageProxy contains the raw pixel data (YUV or RGB format) that you can read from the camera at near-native speed.
   Benefit: This gives you direct access to the pixel buffer before the ARCore processing pipeline fully consumes it for feature matching, giving you a clean slate to inject your watermark.
2. The Data Processing Pipeline (The Logic)
   This remains similar to before, but the data consumed/outputted is different:

Worker Thread: Receives necessary inputs (e.g., current Pose, time elapsed, or raw ImageProxy data chunk) and performs heavy computation.
Output: Instead of calculating a virtual object's location, the worker calculates the Watermark Parameters (e.g., a precise pseudo-random sequence, a specific high-frequency noise pattern, or a phase shift value).
Main Thread Handler: Receives the calculated parameters from the worker.
3. The Invisible Mutation (The Injection Point)
   This is the hardest part, as it requires graphics programming knowledge. You are effectively creating a Shader Effect.

Goal: Use the parameters from Step 2 to modify the raw pixel values in the ImageProxy buffer before rendering.
Implementation Method (Highly Recommended): OpenGL ES / Vulkan Shaders.
Texture Input: The raw ImageProxy data must be uploaded to the GPU as a texture.
Shader Logic: You write a custom fragment shader (GLSL). This shader receives the texture coordinates (the pixel location) and the Watermark Parameters (passed as a uniform variable).
The "Mutation": Inside the shader, you modify the final color output (vec4 finalColor = ...) based on the watermark logic.
Example (Frequency Watermark): You might calculate finalColor.r = originalColor.r * (1.0 + sin(pixelX * watermarkFreq) * watermarkDepth); This subtly modulates the color based on position and a calculated frequency, making it mathematically present but perceptually invisible unless analyzed.
📐 Revised Architectural Workflow
Initialization: Set up CameraX with the ImageAnalysis use case. Configure the session and the rendering pipeline (OpenGL/Vulkan context) to run concurrently.
Main Loop (CameraX Callback):
CameraX delivers the raw ImageProxy buffer ($\text{Buffer}_{\text{raw}}$) to the ImageAnalysis analyzer on the main thread.
Pass Data: Immediately pass this $\text{Buffer}_{\text{raw}}$ (or necessary metadata/a downsampled copy) to the Worker Thread.
Worker Thread (Calculation):
Reads $\text{Buffer}_{\text{raw}}$'s data (or related AR data).
Calculates the Watermark Parameters ($\text{Params}_{\text{water}}$) based on the AR state and the current time/location.
Returns $\text{Params}_{\text{water}}$ to the Main Thread.
Main Thread (Rendering/Injection):
Receives $\text{Params}_{\text{water}}$.
Uploads $\text{Buffer}_{\text{raw}}$ to a GPU texture.
Renders the scene using a custom Shader Program.
The Shader Program reads the texture ($\text{Buffer}{\text{raw}}$) and multiplies/modulates its color output using the values from $\text{Params}{\text{water}}$ (the watermark effect).
Output: The resulting, watermarked texture is displayed to the user.
In summary: You are shifting the focus from using the AR SDK's output to the Android Camera/Graphics pipeline to generate your output, using the AR SDK's state information purely as the input determinant for your watermark's pattern.

Phase 1: Capture & Staging (AR Thread)
Goal: Preserve the current moment's raw data before the worker thread corrupts the timing/context.

API Focus: CameraX (ImageAnalysis).
Action: When the AR thread captures a frame:
Obtain the raw ImageProxy buffer ($\text{Buffer}_{\text{raw}}$).
CRITICAL STEP: Immediately copy the byte content of this buffer into a dedicated, persistent buffer in your application's memory. Do not keep using the ImageProxy object, as it will be overwritten by the next frame.
Signal the Worker Thread that a new reference frame is ready.
Output: $\text{Buffer}_{\text{Staged}}$ (A snapshot of raw camera data).
Phase 2: Complex Processing (Worker Thread)
Goal: Deterministically calculate the watermark $\text{Blob}_{\text{final}}$ using the staged data and cryptographic inputs.

Inputs: $\text{Buffer}{\text{Staged}}$, Nonce, Prior Hash, Current Hash, Media Hash, $\text{Pose}{\text{context}}$.
Process: This is pure compute work. The goal is to generate a fixed-size, tamper-evident payload: $\text{Blob}_{\text{final}}$.
Output: $\text{Blob}_{\text{final}}$ (A fixed 600-byte binary blob).
Thread Management: The Worker Thread MUST signal completion and wait for the AR Thread to acknowledge receipt before proceeding or going to sleep.
Phase 3: Encoding (AR Thread Writes)
Goal: Convert the raw binary signature ($\text{Blob}_{\text{final}}$) into a usable GPU resource (the Watermark Texture).

Timing: This happens after the worker thread signals completion and before the next frame is processed.
Action:
Create a new, dedicated Watermark Texture on the GPU (Dimension: $W \times H$, where $W \ge 600$).
Write the bytes of $\text{Blob}_{\text{final}}$ sequentially into the pixel data of this texture, treating each byte as a color channel's influence (e.g., Byte 1 sets Red value, Byte 2 sets Green value, etc., across the width).
Signal the rendering engine that the Watermark Texture is READY and available for the next frame.
Phase 4: Display (Shader Overlay)
Goal: Overlay the pre-calculated, static watermark texture onto the live camera feed texture in the final render pass.

Inputs:

$\text{Texture}_{\text{raw}}$: The live frame data (from CameraX, ideally after it has been processed by ARCore, if you want the watermark to align with the AR view).
$\text{Texture}{\text{watermark}}$: The pre-loaded, fixed texture containing the $\text{Blob}{\text{final}}$.
$\text{Pose}_{\text{Display}}$: The current AR system pose.
Shader Logic: The shader performs a blending operation, not a calculation. It samples the watermark texture and uses that color/intensity information to modulate the output color.

FinalPixelColor
=
Blend
(
Texture
raw
,
Texture
watermark
,
Pose
Display
)
FinalPixelColor=Blend(Texture
raw
​
,Texture
watermark
​
,Pose
Display
​
)

💡 Addressing the "Sneaky Spot" Problem
You are correct: the hardest problem is making it invisible while remaining cryptographically verifiable. This requires understanding perceptual coding.

If your watermark signature is robust, it must be perceptible by a specialized tool (which is the goal). To make it invisible to the naked eye:

Luminosity Modulation (The Best Bet): Instead of changing the Hue (which is very noticeable), modulate the overall perceived brightness or contrast of the scene by a factor derived from the watermark. A small, predictable, and consistent dip/rise in overall brightness across the entire image plane, matching the watermark pattern, is often visually acceptable but mathematically verifiable.
Shader Term: Adjusting the finalColor by a slight multiplier (e.g., $1.005$ instead of $1.0$).
Frequency Domain Encoding: This is the most robust method but requires significant effort. Instead of encoding color, you encode the data into the High-Frequency Components of the image (the fast changes in color/light). These components are often filtered out by cameras or are too small for the human eye to notice individually, but an analyzer can reconstruct them.
Conclusion: Your current plan—Generate Signature $\rightarrow$ Bake to Texture $\rightarrow$ Blend Overlay—is the industry-standard, correct way to implement this. Focus on perfecting the mapping from the $\text{Blob}_{\text{final}}$ to the texture coordinates, and the shader will handle the rest.
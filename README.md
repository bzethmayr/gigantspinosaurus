# gigantspinosaurus
To retain the possibility of a point of fact.

## cryptographically signed attestations at point of fact as evidence watermark / provenance marker

The notion here is to take point-of-capture image, video, or audio evidence and
produce a durable provenance and content signature,
preventing trivial repudiation or manipulation.
For video, the frame is downsampled, wavelet-transformed,
and reduced to gradient features via a Sobel grid,
producing a compact signature that survives compression.
The resulting attestation is embedded as a QR-code-derived mark
using bipolar luma modulation with temporal persistence;
extraction uses rolling-frame subtraction and ZXing decode.
A user presents intent to record. We generate an "Intent" MAR consisting of the conditions of recording at index -1.
We don't ask what the user says they are recording. We provide a unique reference to the event,
interpretation beyond the conditions of capture are intentionally out of our domain.
The user's client software is free to ask them, and associate the event ID with user data,
but this is not part of the MAR.

Publishing an "Intent" frame does not establish any correlation to a piece of media -
this requires media frames.
At each frame, we compute the media hash using BLAKE3,
then use SipHash 4-8 to combine this with the frame data for the frame hash.
Each frame's SipHash key consists of the nonce followed by the prior hash.
We continue to update the attestation, at frequency per application, with updated conditions and the media index.
We can publish Frame 0, intermediate attestations, or Frame N.

Signatures are embedded into the evidence.

### Intent frame
Signed conditions of capture, with random nonce and prior hash,
a zero-value media field (no media content), index -1,
and a current hash over those data.

### Media frame
Signed conditions of capture including nonce and prior hashes based on the previous frame,
a BLAKE3 hash of the reduced media,
a valid current hash, and non-negative index.
The media is reduced according to the declared `ReductionStep`s
before hashing; the frame receiver operates on pre-reduced data.

## MAR
The core datatype here is the MAR (Minimal Attestation Record),
which carries the provenance and verification information and specifies the byte-level representation.
The MAR is transmitted independently of the artifact.

### Versioning
Each part of the MAR data format is independently versioned.

### On MAR creation:
- encode MAR_core fields (conditions)
- fast hash of media bytes
- keyed hash of partial MAR (with media hash)
- partial MAR with keyed hash replacing fast hash
- signature = Sign(privateKey, encodedMAR)
- embed publicKey + signature in MAR

### Application interface

The MAR creation and verification pipeline depends on a `BindsEnvironment`
record that supplies `GeneratesNonce`, `HashesMarFrame` (SipHash),
`HashesMedia` (BLAKE3), `ExposesUtcDoubleSeconds`, position/orientation/framing sources,
and a `Signatory`. This is the application's interface boundary.
The library supplies `Blake3MediaHasher`, `SipMarHasher`, and `SignsForJava15`
as reasonable defaults for desktop use; position, orientation, and framing
sources are application-specific.

### properties
The MAR in the artifact allows verification.
In a production deployment, the accumulated MAR frames form a referential spine.
It stores attestations, not artifacts.
It prevents lying, not disagreement.
It has no authority node, no ontology, and no interpretive layer.
It is uncentralized by design.

### In Core Library

- **MAR creation, verification, decoding** — `MarCreation`, `MarVerification`, `MarDecoding`
- **Canonical serialization** — `{key:value,}` map format via `HasCanonicalAttributes`
- **ReductionStep pipeline** — 4-slot configurable media reduction before BLAKE3 hashing
- **VideoMarring** — dual-thread near-real-time frame attestation worker
- **GPU/Vulkan compute pipeline** — `gpu/` + `usage/vk/` for compute-shader-accelerated reduction and marking
- **Framing/orientation/position model** — quaternion orientation, cardinal-direction enums, elevation
- **Desktop environment defaults** — `Blake3MediaHasher`, `SipMarHasher`, `SignsForJava15`, `DesktopOrientation`, `DesktopPosition`

### Planned ecosystem
These are necessary for production deployment but not yet implemented in the library.

#### install flow
- generate hardware-backed keypair (Ed25519)
- store private key in secure hardware
- store public key in app storage

Could be brought into library scope, given common OS support for this workflow.

#### embeddings
In progress for sequences of frames.

#### device bindings
E.g. Android, iOS, OSX, Windows, Linux - access to the sensors needed to make Orientation and Framing meaningful.

#### key access/persistence
Mostly covered in install flow - this is explicitly not in our direct scopes.

#### registrar propagation
Propagation of MAR frames to durable external ledgers for establishing a referential spine of attestations.
This is definitely an application vs a library scope, or at least it's a different library's scope.

## agentic involvement
Limited, with mixed results. Evaluations are specific to the development system.

### opencode
Competent. If you aren't paying for anything already, you should probably use this.
* Does not introspect ollama for models by default
* BigPickle - Seems to have written this buffer copying pretty well?
* Same tool-calling issues for most local models
* orieg/gemma3-tools:12b-ft - wrong tool format.

### Models
At about 50k tokens as context:
* gemma:e4b is much too small for this, drifts rapidly on summarization task
* *gemma3:12b performs well on summarization task* but is not recognized as tool-capable
* qwen3.5:9b is a little small for this, did not drift wholesale but summary not fully accurate
* context overflow for:
  * qwen2.5-coder:14b and "-instruct

### IDEA plugins
I cannot currently recommend using anything except IDEA's paid plugin,
because JetBrains has not exposed APIs in a way necessary for fully competent alternate plugins.
Difficulty even making tool calls across various models.
* Poor results from DevoxxGenie - overfeatured and regressing
  * Everything else still misbehaves worse, but the regressed state appears to be the stable state these days.
* No appetite for having the AI plugin self-terminate after 30 days
* OllamAssist so far has successfully read this file, this is progress!
  * but this has also regressed.
* ProxyAI was also previously tried, and disabled, but I hadn't documented why, let's find out...
  * does not use IDE builtin tools.

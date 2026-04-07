# gigantspinosaurus
To retain the possibility of a point of fact.

## cryptographically signed attestations at point of fact as evidence watermark / provenance marker

The notion here is to take point-of-capture image, video, or audio evidence and
watermark it with a durable provenance and content signature,
preventing trivial repudiation or manipulation.
A user presents intent to record. We generate a "Frame 0" packet consisting of the conditions of recording at index 0 (prior).
We don't ask what the user says they are recording. We provide a unique reference to the event,
interpretation beyond the conditions of capture are intentionally out of our domain.
The user's client software is free to ask them, and associate the event ID with user data,
but this is not part of the MAR.
We continue to update the attestation, at frequency per application, with updated conditions and the media index.
We can publish Frame 0, intermediate attestations, or Frame N.

Signatures are embedded into the evidence as well as propagated to one or more external registrars,
which may use any durable ledger to record the fact of acquisition.

## MAR
The core datatype here is the MAR (Minimal Attestation Record),
which carries the provenance and verification information and specifies the byte-level representation.
The MAR is transmitted independently of the artifact.

### On install:
- generate hardware-backed keypair (Ed25519)
- store private key in secure hardware
- store public key in app storage

### On MAR creation:
- encode MAR_core fields
- signature = Sign(privateKey, encodedBytes)
- embed publicKey + signature in MAR

### properties
The set of stored MAR form a referential spine.
The MAR in the artifact allows verification.
It stores attestations, not artifacts.
It prevents lying, not disagreement.
It has no authority node, no ontology, and no interpretive layer.
It is uncentralized by design.

## agentic involvement
Limited, with mixed results. Evaluations are specific to the development system.
* Poor results from DevoxxGenie - overfeatured and regressing
  * But, here we are. Everything else misbehaves worse.
* No appetite for having the AI plugin self-terminate after 30 days
* OllamAssist so far has successfully read this file, this is progress!
  * but this has also regressed.
* ProxyAI was also previously tried, and disabled, but I hadn't documented why, let's find out...
  * does not use IDE builtin tools.
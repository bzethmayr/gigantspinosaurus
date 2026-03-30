# gigantspinosaurus
To retain the possibility of a point of fact.

## cryptographically signed attestations at point of fact as evidence watermark / provenance marker

The notion here is to take point-of-capture image, video, or audio evidence and
watermark it with a durable provenance and content signature,
preventing trivial repudiation or manipulation.
A user presents intent to record. We generate a "Frame 0" packet consisting of the conditions of recording.
We don't ask what the user says they are recording. We provide a unique reference to the event,
interpretation beyond the conditions of capture are intentionally out of our domain.
The user's client software is free to ask them, and associate the event ID with user data,
but this is not part of the MAR.

Signatures are embedded in or appended to the evidence as well as propagated to one or more external registrars,
which may use any durable ledger to record the fact of acquisition.

The core datatype here is the MAR (Minimal Attestation Record),
which carries the provenance and verification information and specifies the byte-level representation.
The MAR is transmitted independently of the artifact.

The set of stored MAR form a referential spine.
The MAR in the artifact allows verification.
It stores attestations, not artifacts.
It prevents lying, not disagreement.
It has no authority node, no ontology, and no interpretive layer.
It is uncentralized by design.

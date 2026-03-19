# gigantspinosaurus
To retain the possibility of a point of fact.

## cryptographically signed attestations at point of fact as evidence watermark / provenance marker

The notion here is to take point-of-capture image, video, or audio evidence and
watermark it with a durable provenance and content signature,
preventing trivial repudiation or manipulation.

Signatures are embedded in or appended to the evidence as well as propagated to one or more external registrars,
which may use any durable ledger to record the fact of acquisition.

The core datatype here is the MAR (Minimal Attestation Record),
which carries the provenance and verification information and specifies the byte-level representation.
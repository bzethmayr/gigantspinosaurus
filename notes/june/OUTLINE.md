Consuming CURRENT.md
In QR.md we have (for video)
+ calculating mark (complete)
- embedding mark (not yet - needs shader)
  - punted to CPU, it's not _awful_
- extracting mark (need two similar frames with +- embedding)
- subtracting mark (to clean for verification, if we're paranoid)
  - this would be the mark from a previous frame
- decoding mark (for MAR content)
- verifying vs the frame asserted about (not the frame embedded in)


This does not work for images - the calculation can be much simpler there, but we have no temporal axis to modulate.
Consuming CURRENT.md
In QR.md we are (for video)
+ calculating mark (complete)
- embedding mark (not yet - needs shader)
- extracting mark (need two similar frames with +- embedding)
- subtracting mark (to clean for verification, if we're paranoid)
  - this would be the mark from a previous frame
- decoding mark (for MAR content)
- verifying vs the frame asserted about (not the frame embedded in)

Implying
- embedding needs shader and shader needs to be fragment shader
  - why does shader need to be fragment shader?

This does not work for images.
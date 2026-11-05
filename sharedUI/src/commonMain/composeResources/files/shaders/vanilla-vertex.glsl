#version 300 es
precision highp float;

// ---------------------------------------------------------------------------
// Per-vertex attribute (QuadRect vbo, stride 8): unit quad, 0..1
// ---------------------------------------------------------------------------
layout (location = 0) in vec2 aPosition;

// ---------------------------------------------------------------------------
// Per-instance attributes (instance vbo, stride 24)
//   aInstanceRect  = (fracX, fracW, tickInChunk, durationTick)
//                    fracX/fracW are fractions of the canvas width,
//                    tickInChunk is the note start relative to the chunk start,
//                    durationTick is the note length in ticks.
//   aInstanceColor = ARGB int uploaded little-endian, so the four bytes arrive
//                    as (b, g, r, a) -> channel order must be swapped below.
//   aDepth         = reserved, currently unused (no depth attachment).
// ---------------------------------------------------------------------------
layout (location = 1) in vec4 aInstanceRect;
layout (location = 2) in uvec4 aInstanceColor;
layout (location = 3) in uint aDepth;

// ---------------------------------------------------------------------------
// Uniforms
//   windowSize     canvas size in px, same unit as gridPos
//   pxPerTick      vertical px per midi tick
//   currChunkTick  currTick - chunkStartTick (already relative on the CPU side,
//                  so no large absolute tick ever reaches the GPU)
//   roundedCorners corner radius as a fraction of half the note width
// ---------------------------------------------------------------------------
uniform vec2 windowSize;
uniform float pxPerTick;
uniform float currChunkTick;
uniform float roundedCorners;

out vec2 vUv;      // 0..1 inside the note quad
out vec2 vSizePx;  // note size in px
out vec4 vColor;   // straight (non-premultiplied) rgba 0..1

void main() {
    vec2 safeSize = max(windowSize, vec2(1.0));
    float scaleY = max(pxPerTick, 0.0);

    // bottom-left corner of this instance, in px
    float xPx = (aInstanceRect.x + aPosition.x * aInstanceRect.y) * safeSize.x;

    // bottom edge sits at (noteTick - currTick) * pxPerTick above the canvas
    // bottom (= left edge of the note in the CPU renderer), then grow upward
    // by aPosition.y * duration.
    float tickFromNow = aInstanceRect.z - currChunkTick;
    float yPx = (tickFromNow + aPosition.y * aInstanceRect.w) * scaleY;

    vec2 ndc = vec2(
        xPx / safeSize.x * 2.0 - 1.0,
        yPx / safeSize.y * 2.0 - 1.0
    );

    gl_Position = vec4(ndc, 0.0, 1.0);

    vUv = aPosition;
    vSizePx = vec2(aInstanceRect.y * safeSize.x, aInstanceRect.w * scaleY);

    // uvec4 is (b, g, r, a) because 0xAARRGGBB was written as a little-endian int
    vColor = vec4(
        float(aInstanceColor.z),
        float(aInstanceColor.y),
        float(aInstanceColor.x),
        float(aInstanceColor.w)
    ) / 255.0;
}

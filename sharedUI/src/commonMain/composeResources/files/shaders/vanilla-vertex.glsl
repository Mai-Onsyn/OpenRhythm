#version 300 es
precision highp float;

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec4 aInstanceRect;
layout (location = 2) in uvec4 aInstanceColor;
//layout (location = 3) in uint aDepth;

uniform vec2 windowSize;
uniform float pxPerTick;
uniform float currChunkTick;
uniform float roundedCorners;

out vec2 vUv;
out vec2 vSizePx;
out vec4 vColor;
out float vTickY;

void main() {
    vec2 safeSize = max(windowSize, vec2(1.0));
    float scaleY = max(pxPerTick, 0.0);

    float xPx = (aInstanceRect.x + aPosition.x * aInstanceRect.y) * safeSize.x;

    float tickFromNow = aInstanceRect.z - currChunkTick;
    float yPx = (tickFromNow + aPosition.y * aInstanceRect.w) * scaleY;

    vec2 ndc = vec2(
        xPx / safeSize.x * 2.0 - 1.0,
        yPx / safeSize.y * 2.0 - 1.0
    );

    gl_Position = vec4(ndc, 0, 1.0);

    vUv = aPosition;
    vSizePx = vec2(aInstanceRect.y * safeSize.x, aInstanceRect.w * scaleY);

    vColor = vec4(
        float(aInstanceColor.z),
        float(aInstanceColor.y),
        float(aInstanceColor.x),
        float(aInstanceColor.w)
    ) * 0.0039215686274509803921568627451;

    vTickY = aInstanceRect.z + aPosition.y * aInstanceRect.w;
}
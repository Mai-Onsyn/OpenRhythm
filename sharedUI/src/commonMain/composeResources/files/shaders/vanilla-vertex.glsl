#version 300 es
precision highp float;

layout (location = 0) in vec2 quadRect;
layout (location = 1) in vec4 rectVertex;
layout (location = 2) in uvec4 rectColor;
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

    float xFrac = (rectVertex.x + quadRect.x * rectVertex.y);

    float tickFromNow = rectVertex.z - currChunkTick;
    float yPx = (tickFromNow + quadRect.y * rectVertex.w) * pxPerTick;

    vec2 ndc = vec2(
        xFrac * 2.0 - 1.0,
        yPx / safeSize.y * 2.0 - 1.0
    );

    gl_Position = vec4(ndc, 0, 1.0);

    vUv = quadRect;
    vSizePx = vec2(rectVertex.y * safeSize.x, rectVertex.w * pxPerTick);

    vColor = vec4(
        float(rectColor.z),
        float(rectColor.y),
        float(rectColor.x),
        float(rectColor.w)
    ) * 0.0039215686274509803921568627451;

    vTickY = rectVertex.z + quadRect.y * rectVertex.w;
}
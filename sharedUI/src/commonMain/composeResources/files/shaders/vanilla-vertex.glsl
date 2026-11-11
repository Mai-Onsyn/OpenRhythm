#version 300 es
precision highp float;

layout (location = 0) in vec2 quadRect;
layout (location = 1) in vec4 rectVertex;
layout (location = 2) in uvec4 rectColor;

uniform vec2 windowSize;
uniform float pxPerTick;
uniform float currChunkTick;
uniform float roundedCorners;

out vec2 vUv;
out vec2 vSizePx;
out vec4 vColor;
out float vTickY;

const vec2 shadowOffset = vec2(1.1, 1.07);

void main() {
    vec2 safeSize = max(windowSize, vec2(1.0));

    float tickFromNow = rectVertex.z - currChunkTick;

    #ifndef DRAW_SHADOW
    float xFrac = rectVertex.x + quadRect.x * rectVertex.y;
    float yFrac = (tickFromNow + quadRect.y * rectVertex.w) * pxPerTick / safeSize.y;
    vUv = quadRect;
    vTickY = rectVertex.z + quadRect.y * rectVertex.w;
    #else
    float xFrac = rectVertex.x + quadRect.x * rectVertex.y * shadowOffset.x;
    float yAddition = (1.0 - quadRect.y) * rectVertex.y * (shadowOffset.y - 1.0) * safeSize.x / safeSize.y;
    float yFrac = (tickFromNow + quadRect.y * rectVertex.w) * pxPerTick / safeSize.y - yAddition;

    float extraHeightPx = rectVertex.y * safeSize.x * (shadowOffset.y - 1.0);
    float noteHeightPx = rectVertex.w * pxPerTick;
    vUv = vec2(
        quadRect.x * shadowOffset.x,
        quadRect.y * (1.0 + extraHeightPx / noteHeightPx) - extraHeightPx / noteHeightPx
    );
    float extraTick = extraHeightPx / max(pxPerTick, 0.000001);
    vTickY = rectVertex.z + quadRect.y * rectVertex.w - (1.0 - quadRect.y) * extraTick;
    #endif

    vec2 ndc = vec2(
        xFrac * 2.0 - 1.0,
        yFrac * 2.0 - 1.0
    );

    gl_Position = vec4(ndc, 0, 1.0);

    vSizePx = vec2(rectVertex.y * safeSize.x, rectVertex.w * pxPerTick);

    vColor = vec4(
        float(rectColor.z),
        float(rectColor.y),
        float(rectColor.x),
        float(rectColor.w)
    ) * 0.0039215686274509803921568627451;
}
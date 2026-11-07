#version 300 es
precision highp float;

in vec2 vUv;
in vec2 vSizePx;
in vec4 vColor;
in float vTickY;

uniform float roundedCorners;
uniform float chunkTickSize;

out vec4 fragColor;

float sdRoundBox(vec2 p, vec2 halfSize, float radius) {
    vec2 q = abs(p) - halfSize + radius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

void main() {
    if (vTickY < 0.0 || vTickY >= chunkTickSize) discard;

    vec2 halfSize = max(vSizePx * 0.5, vec2(0.0001));
    float radius = clamp(roundedCorners, 0.0, 1.0) * halfSize.x;
    radius = min(radius, min(halfSize.x, halfSize.y));

    vec2 p = (vUv - 0.5) * vSizePx;

    float distancePx = sdRoundBox(p, halfSize, radius);
    float aaWidth = max(fwidth(distancePx), 0.0001);
    float coverage = clamp(0.5 - distancePx / aaWidth, 0.0, 1.0);

    if (coverage <= 0.0) discard;

    vec4 color = vColor;
    color.rgb *= color.a;

    fragColor = color * coverage;
}

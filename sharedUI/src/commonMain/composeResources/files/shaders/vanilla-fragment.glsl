#version 300 es
precision highp float;

in vec2 vUv;
in vec2 vSizePx;
in vec4 vColor;
in float vTickY;

uniform float roundedCorners;
uniform float chunkTickSize;

out vec4 fragColor;

const vec2 shadowOffset = vec2(1.1, 1.07);
const vec2 shadowRectCenterOffset = (shadowOffset - vec2(1)) * vec2(-1.0, 1.0);

float sdRoundBox(vec2 p, vec2 halfSize, float radius) {
    vec2 q = abs(p) - halfSize + radius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

float roundBoxCoverage(float distancePx) {
//    float aaWidth = max(fwidth(distancePx), 0.0001);
    float aaWidth = 1.0;
    return clamp(0.5 - distancePx / aaWidth, 0.0, 1.0);
}

void main() {
    if (vTickY < 0.0 || vTickY >= chunkTickSize) discard;

    vec2 halfSize = max(vSizePx * 0.5, vec2(0.0001));
    float radius = roundedCorners * halfSize.x;
    radius = min(radius, min(halfSize.x, halfSize.y));

    vec2 p = (vUv - 0.5) * vSizePx;
    float distancePx = sdRoundBox(p, halfSize, radius);
    float coverage = roundBoxCoverage(distancePx);

    if (coverage <= 0.0) {
        #ifdef DRAW_SHADOW
        vec2 ps = vec2(
            (vUv.x - 0.5 + shadowRectCenterOffset.x) * vSizePx.x,
            (vUv.y - 0.5) * vSizePx.y + shadowRectCenterOffset.y * vSizePx.x
        );

        float distanceShadowPx = sdRoundBox(ps, halfSize, radius);
        float coverageS = roundBoxCoverage(distanceShadowPx);
        if (coverageS <= 0.0) {
            discard;
        }
        else {
            fragColor = vec4(0.0, 0.0, 0.0, coverageS * 0.12);
        }
        #else
        discard;
        #endif
    } else {
        vec4 color = vColor;
        color.rgb *= color.a;
        fragColor = color * coverage;
    }
}

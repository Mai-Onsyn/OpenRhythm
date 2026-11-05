#version 300 es
precision highp float;

in vec2 vUv;      // 0..1 inside the note quad
in vec2 vSizePx;  // note size in px
in vec4 vColor;   // straight (non-premultiplied) rgba 0..1

// corner radius as a fraction of half the note width (same meaning as
// noteRoundPercent in the CPU renderer: radius = width * 0.5 * percent)
uniform float roundedCorners;

out vec4 fragColor;

// signed distance to a rounded box, p in px from the center
float sdRoundBox(vec2 p, vec2 halfSize, float radius) {
    vec2 q = abs(p) - halfSize + radius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

void main() {
    vec2 halfSize = max(vSizePx * 0.5, vec2(0.0001));
    float radius = clamp(roundedCorners, 0.0, 1.0) * halfSize.x;
    radius = min(radius, min(halfSize.x, halfSize.y));

    vec2 p = (vUv - 0.5) * vSizePx;

    float distancePx = sdRoundBox(p, halfSize, radius);
    float aaWidth = max(fwidth(distancePx), 0.0001);
    float coverage = clamp(0.5 - distancePx / aaWidth, 0.0, 1.0);

    if (coverage <= 0.0) discard;

    // the readback target is a premultiplied RGBA bitmap blended with
    // (GL_ONE, GL_ONE_MINUS_SRC_ALPHA), so premultiply here
    vec4 color = vColor;
    color.rgb *= color.a;

    fragColor = color * coverage;
}

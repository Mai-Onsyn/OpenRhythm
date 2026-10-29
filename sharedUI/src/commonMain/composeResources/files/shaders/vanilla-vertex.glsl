#version 300 es
layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec4 aInstanceRect;
layout (location = 2) in vec4 aInstanceColor;

out vec4 vColor;

void main() {
    vec2 pos = aPosition * aInstanceRect.zw + aInstanceRect.xy;
    gl_Position = vec4(pos, 0.0, 1.0);
    vColor = aInstanceColor;
}
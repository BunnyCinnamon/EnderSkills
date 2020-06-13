#version 120

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec2 InSize;

uniform float Time;
uniform vec2 Frequency;
uniform vec2 WobbleAmount;

void main() {
    float xOffset = sin(texCoord.y * Frequency.x + Time * 3.1415926535 * 2.0) * WobbleAmount.x;
    float yOffset = cos(texCoord.x * Frequency.y + Time * 3.1415926535 * 2.0) * WobbleAmount.y;
    vec2 offset = vec2(xOffset, yOffset);
    vec4 rgb = texture2D(DiffuseSampler, texCoord + offset);
    gl_FragColor = vec4(rgb.rgb, 1.0);
}

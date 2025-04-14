#version 150

uniform sampler2D OitOutput;
uniform sampler2D BloomMask;

in vec2 texCoord;
out vec4 bloomColor;

void main() {
    vec4 marker = texture(BloomMask, texCoord);
    vec4 oitOutput = texture(OitOutput, texCoord);
    bloomColor = oitOutput * marker;
}
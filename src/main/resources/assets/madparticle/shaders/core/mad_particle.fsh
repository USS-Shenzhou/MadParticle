#version 150

#define SHIMMER

#ifdef SHIMMER
#extension GL_ARB_explicit_attrib_location : require
#endif

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;

#ifdef SHIMMER
in vec3 bloomFactor;
#endif

layout (location = 0) out vec4 fragColor;

#ifdef SHIMMER
layout (location = 1) out vec4 bloomColor;
#endif

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);

    #ifdef SHIMMER
    bloomColor = vec4(bloomFactor,1.0f) * fragColor;
    #endif
}

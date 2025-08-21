#version 400

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;

out vec4 accum;
out float reveal;

void main() {
    vec4 color = apply_fog(texture(Sampler0, texCoord0) * vertexColor * ColorModulator, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
    if (color.a < 0.001) {
        discard;
    }
    float weight = clamp(
        pow(
            min(1.0, color.a * 5) + 0.01,
            3.0
        )
        * pow(
            1.0 - gl_FragCoord.z * 0.9,
            3.0
        )
        * min(2, gl_FragCoord.w)
        * 1e6,
        1e-2,
        3e3
    );
    accum = vec4(color.rgb * color.a, color.a) * weight;
    reveal = color.a;
}

#version 330

#moj_import <fog.glsl>

layout (location=0) in vec3 Position;
layout (location=1) in ivec4 UVControl;
layout (location=2) in vec4 instanceUV;
layout (location=3) in vec4 instanceColor;
layout (location=4) in ivec2 instanceUV2;
layout (location=5) in mat4 instanceMatrix;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out float vertexDistance;
out vec2 texCoord0;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * instanceMatrix * vec4(Position, 1.0);

    vertexDistance = fog_distance(ModelViewMat, Position, FogShape);
    texCoord0 = vec2(instanceUV.x * UVControl.x + instanceUV.y * UVControl.y, instanceUV.z * UVControl.z + instanceUV.w * UVControl.w);
    vertexColor = instanceColor * texelFetch(Sampler2, instanceUV2 / 16, 0);
}

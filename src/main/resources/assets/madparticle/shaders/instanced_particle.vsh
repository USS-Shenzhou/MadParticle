#version 400

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

//-----per tick update-----
//single float
layout (location=0) in vec4 instanceXYZRoll;
//single float
layout (location=1) in vec4 prevInstanceXYZRoll;
//half float
layout (location=2) in vec4 instanceUV;
//half float
layout (location=3) in vec4 instanceColor;
//half float
layout (location=4) in vec2 sizeExtraLight;
//(4+4 bits) 1 byte + 3 byte padding
layout (location=5) in uint instanceUV2;

uniform sampler2D Sampler2;

layout(std140) uniform CameraCorrection {
    vec4 CamQuat;
    vec4 CamXYZPartialTick;
};

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec2 texCoord0;
out vec4 vertexColor;

const vec4 UV_CONTROL[4] = vec4[4](
vec4(0, 1, 0, 1),
vec4(0, 1, 1, 0),
vec4(1, 0, 0, 1),
vec4(1, 0, 1, 0)
);
const vec4 RELATIVE[4] = vec4[4](
vec4(1, -1, 0, 1),
vec4(1, 1, 0, 1),
vec4(-1, -1, 0, 1),
vec4(-1, 1, 0, 1)
);

mat4 rotate(vec4 quat, mat4 matrix);
mat4 rotateZ(float roll, mat4 matrix);

void main() {
    vec4 xyzRoll = mix(prevInstanceXYZRoll, instanceXYZRoll, CamXYZPartialTick.w);
    vec3 pos = xyzRoll.xyz - CamXYZPartialTick.xyz;

    //matrix4fSingle.identity()
    mat4 m = mat4(1.0);
    //.translation(x + camPosCompensate.x, y + camPosCompensate.y, z + camPosCompensate.z)
    m[3][0] = pos.x;
    m[3][1] = pos.y;
    m[3][2] = pos.z;
    //.rotate(camera.rotation())
    m = rotate(CamQuat, m);
    //.scale(particle.getQuadSize(partialTicks));
    m[0] *= sizeExtraLight.x;
    m[1] *= sizeExtraLight.x;
    m[2] *= sizeExtraLight.x;
    //matrix4fSingle.rotateZ(roll);
    m = rotateZ(xyzRoll.w, m);

    gl_Position = ProjMat * ModelViewMat * m * RELATIVE[gl_VertexID];
    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);
    vec4 uvControl = UV_CONTROL[gl_VertexID];
    texCoord0 = vec2(dot(instanceUV.xy, uvControl.xy), dot(instanceUV.zw, uvControl.zw));
    ivec2 uv2 = ivec2(instanceUV2 & 0xfu, (instanceUV2 >> 4) & 0xfu);
    vertexColor = vec4(instanceColor.xyz * sizeExtraLight.y, instanceColor.w) * texelFetch(Sampler2, uv2, 0);
}

mat4 rotate(vec4 quat, mat4 matrix) {
    float w2 = quat.w * quat.w;
    float x2 = quat.x * quat.x;
    float y2 = quat.y * quat.y;
    float z2 = quat.z * quat.z;
    float zw = quat.z * quat.w;
    float dzw = zw + zw;
    float xy = quat.x * quat.y;
    float dxy = xy + xy;
    float xz = quat.x * quat.z;
    float dxz = xz + xz;
    float yw = quat.y * quat.w;
    float dyw = yw + yw;
    float yz = quat.y * quat.z;
    float dyz = yz + yz;
    float xw = quat.x * quat.w;
    float dxw = xw + xw;
    float rm00 = w2 + x2 - z2 - y2;
    float rm01 = dxy + dzw;
    float rm02 = dxz - dyw;
    float rm10 = -dzw + dxy;
    float rm11 = y2 - z2 + w2 - x2;
    float rm12 = dyz + dxw;
    float rm20 = dyw + dxz;
    float rm21 = dyz - dxw;
    float rm22 = z2 - y2 - x2 + w2;
    matrix[2][0] = rm20;
    matrix[2][1] = rm21;
    matrix[2][2] = rm22;
    matrix[2][3] = 0.0f;
    matrix[0][0] = rm00;
    matrix[0][1] = rm01;
    matrix[0][2] = rm02;
    matrix[0][3] = 0.0f;
    matrix[1][0] = rm10;
    matrix[1][1] = rm11;
    matrix[1][2] = rm12;
    matrix[1][3] = 0.0f;
    return matrix;
}

mat4 rotateZ(float roll, mat4 matrix){
    float s = sin(roll);
    float c = cos(roll);
    float nm00 = fma(matrix[0][0], c, matrix[1][0] * s);
    float nm01 = fma(matrix[0][1], c, matrix[1][1] * s);
    float nm02 = fma(matrix[0][2], c, matrix[1][2] * s);
    float nm03 = fma(matrix[0][3], c, matrix[1][3] * s);
    matrix[1][0]=fma(matrix[0][0], -s, matrix[1][0] * c);
    matrix[1][1]=fma(matrix[0][1], -s, matrix[1][1] * c);
    matrix[1][2]=fma(matrix[0][2], -s, matrix[1][2] * c);
    matrix[1][3]=fma(matrix[0][3], -s, matrix[1][3] * c);
    matrix[0][0]=nm00;
    matrix[0][1]=nm01;
    matrix[0][2]=nm02;
    matrix[0][3]=nm03;
    return matrix;
}

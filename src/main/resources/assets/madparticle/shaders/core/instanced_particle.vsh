#version 400

#moj_import <fog.glsl>

layout (location=0) in vec3 Position;
layout (location=1) in vec4 instanceUV;
layout (location=2) in vec4 instanceColor;
layout (location=3) in vec4 instanceUV2SizeRoll;
layout (location=4) in vec3 instanceXYZ;

uniform sampler2D Sampler2;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

uniform vec3 CamXYZ;
uniform vec4 CamQuat;

out float vertexDistance;
out vec2 texCoord0;
out vec4 vertexColor;

const ivec4 uvControlArray[4] = ivec4[4](
    ivec4(0, 1, 0, 1),
    ivec4(0, 1, 1, 0),
    ivec4(1, 0, 1, 0),
    ivec4(1, 0, 0, 1)
);

mat4 rotate(vec4 quat, mat4 matrix);
mat4 rotateZ(float roll, mat4 matrix);

void main() {
    //matrix4fSingle.identity()
    mat4 m = mat4(1.0);
    //.translation(x + camPosCompensate.x, y + camPosCompensate.y, z + camPosCompensate.z)
    m[3][0] = instanceXYZ.x - CamXYZ.x;
    m[3][1] = instanceXYZ.y - CamXYZ.y;
    m[3][2] = instanceXYZ.z - CamXYZ.z;
    //.rotate(camera.rotation())
    m = rotate(CamQuat, m);
    //.scale(particle.getQuadSize(partialTicks));
    m[0] *= instanceUV2SizeRoll.z;
    m[1] *= instanceUV2SizeRoll.z;
    m[2] *= instanceUV2SizeRoll.z;
    //matrix4fSingle.rotateZ(roll);
    m = rotateZ(instanceUV2SizeRoll.w, m);


    gl_Position = ProjMat * ModelViewMat * m * vec4(Position, 1.0);

    vertexDistance = fog_distance(instanceXYZ, FogShape);
    ivec4 uvControl = uvControlArray[gl_VertexID % 4];
    texCoord0 = vec2(instanceUV.x * uvControl.x + instanceUV.y * uvControl.y, instanceUV.z * uvControl.z + instanceUV.w * uvControl.w);
    vertexColor = instanceColor * texelFetch(Sampler2, ivec2(instanceUV2SizeRoll.xy) / 16, 0);
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

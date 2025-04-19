#version 330

layout(location = 0) in vec2 Position;
layout(location = 1) in vec2 UV0;

out vec2 texCoord0;

void main()
{
    gl_Position = vec4(Position.x, Position.y, 0.0, 1.0);
    texCoord0 = UV0;
}
#version 330 core

layout(location = 0) in vec3 in_position; 

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform vec3 color;

out vec4 final_colour;

void main()
{
    gl_Position = projection * view * model * vec4(in_position, 1.0f);
    final_colour = vec4(color, 1.0f);
}
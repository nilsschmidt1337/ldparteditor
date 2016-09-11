#version 330 core

layout(location = 0) in vec3 in_position;       
layout(location = 1) in vec3 in_normal; 
layout(location = 2) in vec4 in_color;  

out vec4 final_colour;

void main()
{
    gl_Position = vec4(in_position, 1.0f);
    final_colour = in_color;
}
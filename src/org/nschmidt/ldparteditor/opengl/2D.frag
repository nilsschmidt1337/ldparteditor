#version 330 core

out vec4 color;
in vec4 final_colour;

void main()
{
    color = final_colour;
} 
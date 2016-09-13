#version 330 core

out vec4 color;
in vec4 final_colour;

uniform float lightswitch;

void main()
{
    color = final_colour;
} 
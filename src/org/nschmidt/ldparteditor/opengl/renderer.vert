#version 330 core

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec3 in_normal;       
layout(location = 2) in vec4 in_color;  

uniform float lightswitch;
uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;


out vec4 final_colour;

void main()
{	
    gl_Position = projection * view * model * vec4(in_position, 1.0f);
    final_colour = in_color;
    if (lightswitch == 0.0f) {
		final_colour.x = in_color.x * 2.0f;
	}
}
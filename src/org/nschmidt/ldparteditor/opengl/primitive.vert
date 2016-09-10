#version 330 core

layout (location = 0) in vec3 position; // The position variable has attribute position 0
  
out vec4 vertexColor; // Specify a color output to the fragment shader

void main()
{
    gl_Position = vec4(position, 1.0); // See how we directly give a vec3 to vec4's constructor
    vertexColor = vec4(0.5f, 0.0f, 0.0f, 1.0f); // Set the output variable to a dark-red color
}
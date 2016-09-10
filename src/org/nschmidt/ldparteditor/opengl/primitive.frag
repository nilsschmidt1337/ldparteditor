#version 330 core

in vec4 vertexColor; // The input variable from the vertex shader (same name and same type)
  
out vec4 color;

void main()
{
    color = vertexColor;
} 
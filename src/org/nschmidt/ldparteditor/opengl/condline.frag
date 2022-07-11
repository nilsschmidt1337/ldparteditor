#version 330 core

out vec4 color;
in vec4 sceneColor;

void main()
{
	if (sceneColor.a < 0.5f) {
		discard;
	}
	color = sceneColor;
}
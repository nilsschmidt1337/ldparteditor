#version 460 core

out vec4 color;
in vec4 sceneColor;

void main()
{
	if (sceneColor.a < 0.4f) {
		discard;
	}
	color = sceneColor;
}
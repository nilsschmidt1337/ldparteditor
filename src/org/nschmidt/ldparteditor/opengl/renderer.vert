#version 330 core

layout(location = 0) in vec3 in_position;
layout(location = 1) in vec3 in_normal;
layout(location = 2) in vec4 in_color;
layout(location = 3) in vec2 in_tex;  

uniform float factor;
uniform float pngswitch;
uniform float texmapswitch;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform mat4 rotation;

out vec4 sceneColor;
out vec3 normal;
out vec3 position;
out vec2 tex;

void main()
{
    if (pngswitch > 0.5f || texmapswitch > 0.5f) {
        tex = in_tex;
    }
    gl_Position = projection * view * model * vec4(in_position, 1.0f);
    position = vec3(view * model * vec4(in_position, 1.0f));
    sceneColor = vec4(in_color.r * factor, in_color.g * factor, in_color.b * factor, in_color.a);
    
    mat4 glNormalMatrix_m;
    vec3 n1;
    vec3 n2;
    vec3 n3;
    n1.x = model[0][0];
    n1.y = model[0][1];
    n1.z = model[0][2];
    
    n2.x = model[1][0];
    n2.y = model[1][1];
    n2.z = model[1][2];
    
    n3.x = model[2][0];
    n3.y = model[2][1];
    n3.z = model[2][2];
    
    n1 = normalize(n1);
    n2 = normalize(n2);
    n3 = normalize(n3);
    
    for (int i=0; i<3; ++i)
    {
        glNormalMatrix_m[0][i] = n1[i];
        glNormalMatrix_m[1][i] = n2[i];
        glNormalMatrix_m[2][i] = n3[i];
    }
    glNormalMatrix_m[3][3] = 1.0f;
    
    normal = vec3(rotation * glNormalMatrix_m * vec4(normalize(in_normal), 1.0f));
}
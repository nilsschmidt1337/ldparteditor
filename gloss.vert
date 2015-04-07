uniform sampler2D colorMap;
uniform sampler2D glossMap;
uniform sampler2D cubeMap;
uniform sampler2D cubeMapMatte;
uniform sampler2D cubeMapMetal;
uniform float alphaSwitch;
uniform float normalSwitch;
uniform float noTextureSwitch;
uniform float noGlossMapSwitch;
uniform float cubeMapSwitch;
uniform float noLightSwitch;
varying vec3 normal, position;

void main()
{
   gl_FrontColor = gl_Color;

   normal = normalize(gl_NormalMatrix * gl_Normal);
   if (normalSwitch == 0f) normal = vec3(-normal.x, -normal.y, -normal.z);

   position = vec3(gl_ModelViewMatrix * gl_Vertex);
   gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
   gl_TexCoord[0] = gl_MultiTexCoord0;
}
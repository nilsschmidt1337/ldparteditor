#version 330 core

out vec4 color;
in vec4 sceneColor;
in vec3 normal;
in vec3 position;
in vec2 tex;

uniform float lightswitch;

uniform float l0_r;
uniform float l0_g;
uniform float l0_b;
uniform float l1_r;
uniform float l1_g;
uniform float l1_b;
uniform float l2_r;
uniform float l2_g;
uniform float l2_b;
uniform float l3_r;
uniform float l3_g;
uniform float l3_b;

uniform float l0s_r;
uniform float l0s_g;
uniform float l0s_b;
uniform float l1s_r;
uniform float l1s_g;
uniform float l1s_b;
uniform float l2s_r;
uniform float l2s_g;
uniform float l2s_b;
uniform float l3s_r;
uniform float l3s_g;
uniform float l3s_b;

struct lightSource
{
  vec4 position;
  vec4 diffuse;
  vec4 specular;
  float constantAttenuation, linearAttenuation, quadraticAttenuation;
};
const int numberOfLights = 4;
lightSource lights[numberOfLights];
lightSource light0 = lightSource(
	vec4(2.0,  2.0,  2.0, 1.0),
	vec4(l0_r,  l0_g,  l0_b, 1.0),
	vec4(l0s_r,  l0s_g,  l0s_b, 1.0),
	1.0, .001f, 0.0
);
lightSource light1 = lightSource(
	vec4(-2.0, 2.0,  2.0, 1.0),
	vec4(l1_r,  l1_g,  l1_b, 1.0),
	vec4(l1s_r,  l1s_g,  l1s_b, 1.0),
	1.0, .001f, 0.0
);
lightSource light2 = lightSource(
	vec4(2.0, -2.0,  2.0, 1.0),
	vec4(l2_r,  l2_g,  l2_b, 1.0),
	vec4(l2s_r,  l2s_g,  l2s_b, 1.0),
	1.0, .001f, 0.0
);
lightSource light3 = lightSource(
	vec4(-2.0, -2.0,  0.0, 1.0),
	vec4(l3_r,  l3_g,  l3_b, 1.0),
	vec4(l3s_r,  l3s_g,  l3s_b, 1.0),
	1.0, .001f, 0.0
);

struct material
{
  vec4 ambient;
  vec4 diffuse;
  vec4 specular;
  float shininess;
};
material frontMaterial = material(
  vec4(0.1, 0.1, 0.1, 1.0),
  vec4(1.0, 1.0, 1.0, 1.0),
  vec4(1.0, 1.0, 1.0, 1.0),
  128.0
);

void main()
{
	lights[0] = light0;
	lights[1] = light1;
	lights[2] = light2;
	lights[3] = light3;

	vec3   lightDir;
	float  attenFactor;
	vec3   eyeDir               = normalize(-position); // camera is at (0,0,0) in ModelView space
	vec4   lightTextureDiffuse  = vec4(0.0,0.0,0.0,0.0);
	vec4   lightAmbientDiffuse  = vec4(0.0,0.0,0.0,0.0);
	vec4   lightTextureSpecular = vec4(0.0,0.0,0.0,0.0);
	vec4   lightSpecular        = vec4(0.0,0.0,0.0,0.0);

	// iterate all lights
	for (int i=0; i<4; ++i)
	{
		// attenuation and light direction
     	// positional light source
		float dist  = distance(lights[i].position.xyz, position);
		attenFactor = 1.0 /( lights[i].constantAttenuation +
		lights[i].linearAttenuation * dist +
		lights[i].quadraticAttenuation * dist * dist );
		lightDir = normalize(lights[i].position.xyz - position);
				
		if (lightswitch > 0.0)
		{
			/*if (normalSwitch == 0.0) lightDir = vec3(-lightDir.x, -lightDir.y, -lightDir.z);*/
		} else {
			lightDir = normal;
			attenFactor = .6;
		}

		lightAmbientDiffuse += lights[i].diffuse * frontMaterial.diffuse * max(dot(normal, lightDir), 0.0) * attenFactor;
		
		// specular
		vec3 r = normalize(reflect(-lightDir, normal));

		lightSpecular += lights[i].specular * frontMaterial.specular * pow(max(dot(r, eyeDir), 0.0), frontMaterial.shininess) * attenFactor;
		
	}
    lightSpecular *= .05;
	color = (sceneColor + lightAmbientDiffuse) + lightSpecular;
}
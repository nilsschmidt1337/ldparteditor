#version 330 core

out vec4 color;
in vec4 sceneColor;
in vec3 normal;
in vec3 position;
in vec2 tex;

uniform float factor;
uniform float alphaswitch;
uniform float texmapswitch;
uniform float lightswitch;
uniform float pngswitch;
uniform sampler2D ldpePngSampler;

uniform sampler2D cubeMap;
uniform sampler2D cubeMapMatte;
uniform sampler2D cubeMapMetal;
uniform sampler2D cubeMapPearl;

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

void MyFunction(in vec4 col, out vec4 result);

void MyFunction(in vec4 col, out vec4 result)
{

vec2   uv = vec2(0.0, 0.0);
vec3   pos;

pos = normal;
pos.y = -pos.y;

float ax = abs(pos.x) + 0.0001;
float ay = abs(pos.y) + 0.0001;
float az = abs(pos.z) + 0.0001;

if (ax >= ay) {
  if (ax >= az) {
    // X
    if (pos.x >= 0) {
      uv.x = (-pos.z / ax + 1.0) / 2.0;
      uv.y = (pos.y / ax + 1.0) / 2.0;
      uv.x = uv.x * 0.25 + 0.5;
      uv.y = uv.y * 0.33 + 0.33;
    } else {
      uv.x = (-pos.z / ax + 1.0) / 2.0;
      uv.y = (pos.y / ax + 1.0) / 2.0;
      uv.x = uv.x * 0.25;
      uv.y = uv.y * 0.33 + 0.33;
    }
  }
}

if (ay >= ax) {
  if (ay >= az) {
    // Y
    if (pos.y >= 0) {
      uv.x = (pos.x / ay + 1.0) / 2.0;
      uv.y = (pos.z / ay + 1.0) / 2.0;
      uv.x = uv.x * 0.25 + 0.25;
      uv.y = uv.y * 0.33 + 0.66;
    } else {
      uv.x = (pos.x / ay + 1.0) / 2.0;
      uv.y = (pos.z / ay + 1.0) / 2.0;
      uv.x = uv.x * 0.25 + 0.25;
      uv.y = uv.y * 0.33;
    }
  }
}

if (az >= ax) {
  if (az >= ay) {
    // Z
    if (pos.z >= 0) {
      uv.x = (pos.x / az + 1.0) / 2.0;
      uv.y = (pos.y / az + 1.0) / 2.0;
      uv.x = uv.x * 0.25 + 0.25;
      uv.y = uv.y * 0.33 + 0.33;
    } else {
      uv.x = (pos.x / az + 1.0) / 2.0;
      uv.y = (pos.y / az + 1.0) / 2.0;
      uv.x = uv.x * 0.25 + 0.75;
      uv.y = uv.y * 0.33 + 0.33;
    }
  }
}

   vec4 cubeColor = vec4(0.0,0.0,0.0,0.0);
   float cubeMapSwitch = col.a;
   if (cubeMapSwitch < 3.0f) {
      cubeColor = texture2D(cubeMap, uv);
      result.a = col.a;
      cubeColor = mix(cubeColor, col, cubeColor);
   } else if (cubeMapSwitch < 4.0f) {
      cubeColor = texture2D(cubeMapMetal, uv);
      result.a = col.a;
      cubeColor = mix(cubeColor, col, cubeColor);
   } else if (cubeMapSwitch < 5.0f) {
      cubeColor = texture2D(cubeMapMatte, uv);
      result.a = col.a;
      cubeColor = mix(cubeColor, col, cubeColor);
   } else if (cubeMapSwitch < 6.0f) {
      cubeColor = texture2D(cubeMapMatte, uv);
      result.a = col.a;
      cubeColor = mix(cubeColor, col, cubeColor);
   } else {
      cubeColor = texture2D(cubeMapPearl, uv);
      result.a = 7.0f;
   }
   
   result.r = cubeColor.r;
   result.g = cubeColor.g;
   result.b = cubeColor.b;
}

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
    
    vec4 resultColor = vec4(sceneColor);
    vec3 normal2 = vec3(normal);
    
    if (resultColor.a < 0.1f) {
        discard;
    } else if (resultColor.a > 1.2f && resultColor.a < 6.0f) {
        vec4 cubeColor = vec4(0.0,0.0,0.0,0.0);
        MyFunction(resultColor, cubeColor);
        resultColor = cubeColor;
    } else if (resultColor.a > 1.2f && resultColor.a < 7.0f) {
        vec3 eyeDir = normalize(-position); // camera is at (0,0,0) in ModelView space
        vec4 cubeColor = texture2D(cubeMapPearl, tex);
        vec3 normal3 = normalize(mix(normal2, normalize(vec3(cubeColor.r - 0.2f, cubeColor.g - 0.2f, cubeColor.b - 0.2f)), normal2));
        normal2 = normalize(mix(normal2, normalize(vec3(cubeColor.r - 0.5f, cubeColor.g - 0.5f, cubeColor.b - 0.5f)), normal2));
        
        vec3 magic1 = vec3(1.0f,0.0f,0.0f);
        vec3 magic2 = vec3(0.0f,1.0f,0.0f);
        
        float saturation1 = max(dot(magic1, normal), 0.0f);
        float saturation2 = max(dot(magic2, normal), 0.0f);
        float saturation = max(max(saturation1, saturation2) * 2.0f, 1.0f);
        float grayscale = (resultColor.r * 0.33f + resultColor.g * 0.33f + resultColor.b * 0.33f) * (1.0f - saturation);
        
        resultColor.r = resultColor.r * saturation + grayscale;
        
        magic1 = vec3(-1.0f,0.0f,0.0f);
        magic2 = vec3(0.0f,-1.0f,0.0f);
        
        saturation1 = max(dot(magic1, normal), 0.0f);
        saturation2 = max(dot(magic2, normal), 0.0f);
        saturation = max(max(saturation1, saturation2) * 2.0f, 1.0f);
        grayscale = (resultColor.r * 0.33f + resultColor.g * 0.33f + resultColor.b * 0.33f) * (1.0f - saturation);
        
        resultColor.g = resultColor.g * saturation + grayscale;
        
        magic1 = vec3(1.0f,0.0f,0.0f);
        magic2 = vec3(0.719f,-0.719f,0.0f);
        
        saturation1 = max(dot(magic1, normal), 0.0f);
        saturation2 = max(dot(magic2, normal), 0.0f);
        saturation = max(max(saturation1, saturation2) * 2.0f, 1.0f);
        grayscale = (resultColor.r * 0.33f + resultColor.g * 0.33f + resultColor.b * 0.33f) * (1.0f - saturation);
        
        resultColor.b = resultColor.b * saturation + grayscale;
        
        resultColor.a = 1.0f;
    }
    
    lights[0] = light0;
    lights[1] = light1;
    lights[2] = light2;
    lights[3] = light3;

    vec3   lightDir;
    float  attenFactor;
    vec3   eyeDir               = normalize(-position); // camera is at (0,0,0) in ModelView space
    vec4   lightAmbientDiffuse  = vec4(0.0,0.0,0.0,0.0);
    vec4   lightSpecular        = vec4(0.0,0.0,0.0,0.0);
    
    int count = 4;
    
    // No light effect on matte rubber material
    if ((resultColor.a > 5.0f && resultColor.a < 6.0f) || lightswitch < 0.5f) {
        count = 0;
    }
    
    // iterate all lights
    for (int i=0; i<count; ++i)
    {
        // attenuation and light direction
        // positional light source
        float dist  = distance(lights[i].position.xyz, position);
        attenFactor = 1.0 /( lights[i].constantAttenuation +
        lights[i].linearAttenuation * dist +
        lights[i].quadraticAttenuation * dist * dist );
        lightDir = normalize(lights[i].position.xyz - position);
        
        lightAmbientDiffuse += lights[i].diffuse * frontMaterial.diffuse * max(dot(normal2, lightDir), 0.0) * attenFactor;
        
        // specular
        vec3 r = normalize(reflect(-lightDir, normal2));
        
        lightSpecular += lights[i].specular * frontMaterial.specular * pow(max(dot(r, eyeDir), 0.0), frontMaterial.shininess) * attenFactor;
        
    }
    
    // It is not possible to set alpha with lightAmbientDiffuse.a = X or lightSpecular.a = X
    lightAmbientDiffuse  = vec4(lightAmbientDiffuse.r,lightAmbientDiffuse.g,lightAmbientDiffuse.b,0.0);
    lightSpecular        = vec4(lightSpecular.r,lightSpecular.g,lightSpecular.b,0.0);
    
    if (texmapswitch > 0.5f) {
        vec4 texColor = texture2D(ldpePngSampler, tex.xy);
        texColor.r *= factor;
        texColor.g *= factor;
        texColor.b *= factor;
        lightSpecular *= .05;
        if (alphaswitch < 0.5f) {
            if (resultColor.a < 0.9f) {
                if (texColor.a < 0.1f) {
                    discard;
                }
                if (texColor.a < 0.9f) {
                    float oneMinusTexAlpha = 1.0 - texColor.a;
                    texColor.r = texColor.r * texColor.a + resultColor.r * oneMinusTexAlpha;
                    texColor.g = texColor.g * texColor.a + resultColor.g * oneMinusTexAlpha;
                    texColor.b = texColor.b * texColor.a + resultColor.b * oneMinusTexAlpha;
                    texColor.a = resultColor.a;
                    resultColor = texColor;
                } else {
                    resultColor = texColor;
                    resultColor.a = 1.0f;
                }
                color = (resultColor + lightAmbientDiffuse) + lightSpecular;
            } else {
                if (texColor.a < 0.9f) {
                    if (texColor.a > 0.1f) {
                        float oneMinusTexAlpha = 1.0 - texColor.a;
                        resultColor.r = texColor.r * texColor.a + resultColor.r * oneMinusTexAlpha;
                        resultColor.g = texColor.g * texColor.a + resultColor.g * oneMinusTexAlpha;
                        resultColor.b = texColor.b * texColor.a + resultColor.b * oneMinusTexAlpha;
                        resultColor.a = 1.0f;
                    }
                } else {
                    resultColor = texColor;
                }
                color = (resultColor + lightAmbientDiffuse) + lightSpecular;
            }
        } else {
            if (resultColor.a < 0.9f) {
                color = (resultColor + lightAmbientDiffuse) + lightSpecular;
            } else {
                discard;
            }
        }
    } else if (pngswitch < 0.5f) {
        lightSpecular *= .05;
        color = (resultColor + lightAmbientDiffuse) + lightSpecular;
    } else {
        vec4 texColor = texture2D(ldpePngSampler, tex.xy);
        if (resultColor.a == 7.0f) {
            color = resultColor;
        } else {
            color = texColor + resultColor;
        }
    }
}
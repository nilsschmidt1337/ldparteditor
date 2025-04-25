#version 120

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
varying vec4 colorResult;

void MyFunction(in vec4 col);

void MyFunction(in vec4 col)
{

vec2   uv = vec2(0.0, 0.0);
vec3   pos;

pos = normal;

 if (normalSwitch == 0) pos = vec3(-pos.x, -pos.y, -pos.z);

float ax = abs(pos.x) + 0.0001;
float ay = abs(pos.y) + 0.0001;
float az = abs(pos.z) + 0.0001;

/*

Cubemap UV calculation for the +X layer
U = ((-Z/|X|) + 1)/2
V = ((-Y/|X|) + 1)/2

*/

// Standard Cubemap approach

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
   if (cubeMapSwitch < 2.0) {
      cubeColor = texture2D(cubeMap, uv);
   } else if (cubeMapSwitch < 3.0) {
      cubeColor = texture2D(cubeMapMatte, uv);
   } else {
      cubeColor = texture2D(cubeMapMetal, uv);
   }
   cubeColor = mix(cubeColor, col, cubeColor);
   gl_FragColor.r = cubeColor.r;
   gl_FragColor.g = cubeColor.g;
   gl_FragColor.b = cubeColor.b;
   gl_FragColor.a = 1.0;
}


void main (void)
{
  vec3   lightDir;
  float  attenFactor;
  vec3   eyeDir           = normalize(-position); // camera is at (0,0,0) in ModelView space
  vec4   lightTextureDiffuse  = vec4(0.0,0.0,0.0,0.0);
  vec4   lightAmbientDiffuse  = vec4(0.0,0.0,0.0,0.0);
  vec4   lightTextureSpecular = vec4(0.0,0.0,0.0,0.0);
  vec4   lightSpecular        = vec4(0.0,0.0,0.0,0.0);

  vec4   texColor = texture2D(colorMap, gl_TexCoord[0].xy);

  // iterate all lights
  for (int i=0; i<4; ++i)
  {
    // attenuation and light direction
    if (gl_LightSource[i].position.w != 0.0)
    {
      // positional light source
      float dist  = distance(gl_LightSource[i].position.xyz, position);
      attenFactor = 1.0 /( gl_LightSource[i].constantAttenuation +
                    gl_LightSource[i].linearAttenuation * dist +
                    gl_LightSource[i].quadraticAttenuation * dist * dist );
      lightDir    = normalize(gl_LightSource[i].position.xyz - position);
    }
    else
    {
      // directional light source
      attenFactor = 1.0;
      lightDir    = gl_LightSource[i].position.xyz;
    }
    if (noLightSwitch > 0.0) {
       lightDir = normal;
       attenFactor = .6;
    } else {
       if (normalSwitch == 0.0) lightDir = vec3(-lightDir.x, -lightDir.y, -lightDir.z);
    }
    // ambient + diffuse
    lightAmbientDiffuse     += gl_FrontLightProduct[i].diffuse * max(dot(normal, lightDir), 0.0) * attenFactor;
    // specular
    vec3 r      = normalize(reflect(-lightDir, normal));
    if (cubeMapSwitch > 0.0) {
       lightSpecular          += vec4(1.0, 1.0, 1.0, 1.0) * pow(max(dot(r, eyeDir), 0.0), gl_FrontMaterial.shininess) * attenFactor;
    } else {
       lightSpecular          += gl_FrontLightProduct[i].specular * pow(max(dot(r, eyeDir), 0.0), gl_FrontMaterial.shininess) * attenFactor;
    }
    if (noTextureSwitch < 1.0) {
       lightTextureSpecular   += gl_LightSource[i].specular * pow(max(dot(r, eyeDir), 0.0), gl_FrontMaterial.shininess) * attenFactor;
       vec4 tn           = vec4(texColor.r, texColor.g, texColor.b, 1.0) * gl_LightSource[i].diffuse;
       lightTextureDiffuse     += tn * max(dot(normal, lightDir), 0.0)*attenFactor;
    }
  }
  // compute final color

  // Do a gloss map look up and compute the reflectivity.
  vec3 gloss_color = texture2D(glossMap, gl_TexCoord[0].xy).rgb;
  float reflectivity = 0.0;
  if (noLightSwitch < 1.0) {
    if (noGlossMapSwitch < 1.0) {
      reflectivity = .30*gloss_color.r + .59*gloss_color.g + .11*gloss_color.b;
    }
  }


  if (noTextureSwitch > 0.0) {
    if (colorResult.a < 1.0) {
          if (alphaSwitch < 1.0) {
              // This is the default case for transparent colors without a cubemap
              gl_FragColor.r = lightAmbientDiffuse.r * colorResult.r + lightSpecular.r * reflectivity;
              gl_FragColor.g = lightAmbientDiffuse.g * colorResult.g + lightSpecular.g * reflectivity;
              gl_FragColor.b = lightAmbientDiffuse.b * colorResult.b + lightSpecular.b * reflectivity;
              gl_FragColor.a = 1.0;
          } else {
             discard;
          }
       } else {
          if (alphaSwitch > 0.0) {
             if (cubeMapSwitch < 1.0) {
               // This is the default case for solid colors without a cubemap
               gl_FragColor.r = lightAmbientDiffuse.r * colorResult.r + lightSpecular.r * reflectivity;
               gl_FragColor.g = lightAmbientDiffuse.g * colorResult.g + lightSpecular.g * reflectivity;
               gl_FragColor.b = lightAmbientDiffuse.b * colorResult.b + lightSpecular.b * reflectivity;
               gl_FragColor.a = 1.0;
             } else {
               // This is the case for solid materials with a cube map
               if (cubeMapSwitch == 2.0 && noLightSwitch > 0.0) lightSpecular *= .05;
               vec4 materialColor = vec4(1.0, 1.0, 1.0, 1.0);
               materialColor.r = lightAmbientDiffuse.r * colorResult.r + lightSpecular.r * reflectivity;
               materialColor.g = lightAmbientDiffuse.g * colorResult.g + lightSpecular.g * reflectivity;
               materialColor.b = lightAmbientDiffuse.b * colorResult.b + lightSpecular.b * reflectivity;
               MyFunction(materialColor);
             }
          } else {
              discard;
          }
       }
  } else {
    if (texColor.a == 0.0) {
       if (colorResult.a < 1.0) {
          // OK
          if (alphaSwitch < 1.0) {
             gl_FragColor.r = lightAmbientDiffuse.r * colorResult.r + lightSpecular.r * reflectivity;
             gl_FragColor.g = lightAmbientDiffuse.g * colorResult.g + lightSpecular.g * reflectivity;
             gl_FragColor.b = lightAmbientDiffuse.b * colorResult.b + lightSpecular.b * reflectivity;
             gl_FragColor.a = 1.0;
          } else {
             discard;
          }
       } else {
          // OK
          if (alphaSwitch > 0.0) {
            if (cubeMapSwitch < 1.0) {
              gl_FragColor.r = lightAmbientDiffuse.r * colorResult.r + lightSpecular.r * reflectivity;
              gl_FragColor.g = lightAmbientDiffuse.g * colorResult.g + lightSpecular.g * reflectivity;
              gl_FragColor.b = lightAmbientDiffuse.b * colorResult.b + lightSpecular.b * reflectivity;
              gl_FragColor.a = 1.0;
            } else {
              if (cubeMapSwitch == 2.0 && noLightSwitch > 0.0) lightSpecular *= .05;
              vec4 materialColor = vec4(1.0, 1.0, 1.0, 1.0);
              materialColor.r = lightAmbientDiffuse.r * colorResult.r + lightSpecular.r * reflectivity;
              materialColor.g = lightAmbientDiffuse.g * colorResult.g + lightSpecular.g * reflectivity;
              materialColor.b = lightAmbientDiffuse.b * colorResult.b + lightSpecular.b * reflectivity;
              MyFunction(materialColor);
            }
          } else {
              discard;
          }
       }
    } else if (texColor.a < 1.0) {
       float groundAlpha = colorResult.a;
       if (groundAlpha >= 1.0) {
         if (alphaSwitch > 0.0) {
            vec3 textureColor = vec3(1.0, 1.0, 1.0);
            textureColor.r = lightTextureDiffuse.r * texColor.r + lightTextureSpecular.r * reflectivity;
            textureColor.g = lightTextureDiffuse.g * texColor.g + lightTextureSpecular.g * reflectivity;
            textureColor.b = lightTextureDiffuse.b * texColor.b + lightTextureSpecular.b * reflectivity;
            vec3 groundColor = vec3(1.0, 1.0, 1.0);
            groundColor.r = lightAmbientDiffuse.r * colorResult.r + lightSpecular.r * reflectivity;
            groundColor.g = lightAmbientDiffuse.g * colorResult.g + lightSpecular.g * reflectivity;
            groundColor.b = lightAmbientDiffuse.b * colorResult.b + lightSpecular.b * reflectivity;
            float oneMinusTexAlpha = 1.0 - texColor.a;
            gl_FragColor.r = textureColor.r * texColor.a + groundColor.r * oneMinusTexAlpha;
            gl_FragColor.g = textureColor.g * texColor.a + groundColor.g * oneMinusTexAlpha;
            gl_FragColor.b = textureColor.b * texColor.a + groundColor.b * oneMinusTexAlpha;
            gl_FragColor.a = 1.0;
         } else {
            discard;
         }
       } else {
         if (alphaSwitch > 0.0) {
            discard;
         } else {
            vec3 textureColor = vec3(1.0, 1.0, 1.0);
            textureColor.r = lightTextureDiffuse.r * texColor.r + lightTextureSpecular.r * reflectivity;
            textureColor.g = lightTextureDiffuse.g * texColor.g + lightTextureSpecular.g * reflectivity;
            textureColor.b = lightTextureDiffuse.b * texColor.b + lightTextureSpecular.b * reflectivity;
            vec3 groundColor = vec3(1.0, 1.0, 1.0);
            groundColor.r = lightAmbientDiffuse.r * colorResult.r + lightSpecular.r * reflectivity;
            groundColor.g = lightAmbientDiffuse.g * colorResult.g + lightSpecular.g * reflectivity;
            groundColor.b = lightAmbientDiffuse.b * colorResult.b + lightSpecular.b * reflectivity;
            float oneMinusTexAlpha = 1.0 - texColor.a;
            gl_FragColor.r = textureColor.r * texColor.a + groundColor.r * oneMinusTexAlpha;
            gl_FragColor.g = textureColor.g * texColor.a + groundColor.g * oneMinusTexAlpha;
            gl_FragColor.b = textureColor.b * texColor.a + groundColor.b * oneMinusTexAlpha;
            gl_FragColor.a = 1.0;
         }
      }
    } else {
       // OK
       if (alphaSwitch > 0.0) {
         gl_FragColor.r = lightTextureDiffuse.r * texColor.r + lightTextureSpecular.r * reflectivity;
         gl_FragColor.g = lightTextureDiffuse.g * texColor.g + lightTextureSpecular.g * reflectivity;
         gl_FragColor.b = lightTextureDiffuse.b * texColor.b + lightTextureSpecular.b * reflectivity;
         gl_FragColor.a = 1.0;
       } else {
         discard;
       }
    }
  }
}

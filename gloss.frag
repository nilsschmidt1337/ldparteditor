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

void MyFunction(in vec4 col);

void MyFunction(in vec4 col)
{

vec2   uv = vec2(0.0,0.0);
vec3   pos;

pos = normal;

 if (normalSwitch == 0f) pos = vec3(-pos.x, -pos.y, -pos.z);

float ax = abs(pos.x) + 0.0001f;
float ay = abs(pos.y) + 0.0001f;
float az = abs(pos.z) + 0.0001f;

/*

Cubemap UV calculation for the +X layer
U = ((-Z/|X|) + 1)/2
V = ((-Y/|X|) + 1)/2

*/

// Standard Cubemap approach

if (ax >= ay) {
  if (ax >= az) {
    // X
    if (pos.x >= 0f) {
      uv.x = (-pos.z / ax + 1.0f) / 2.0f;
      uv.y = (pos.y / ax + 1.0f) / 2.0f;
      uv.x = uv.x * 0.25f + 0.5f;
      uv.y = uv.y * 0.33f + 0.33f;
    } else {
      uv.x = (-pos.z / ax + 1.0f) / 2.0f;
      uv.y = (pos.y / ax + 1.0f) / 2.0f;
      uv.x = uv.x * 0.25f;
      uv.y = uv.y * 0.33f + 0.33f;
    }
  }
}

if (ay >= ax) {
  if (ay >= az) {
    // Y
    if (pos.y >= 0f) {
      uv.x = (pos.x / ay + 1.0f) / 2.0f;
      uv.y = (pos.z / ay + 1.0f) / 2.0f;
      uv.x = uv.x * 0.25f + 0.25f;
      uv.y = uv.y * 0.33f + 0.66f;
    } else {
      uv.x = (pos.x / ay + 1.0f) / 2.0f;
      uv.y = (pos.z / ay + 1.0f) / 2.0f;
      uv.x = uv.x * 0.25f + 0.25f;
      uv.y = uv.y * 0.33f;
    }
  }
}

if (az >= ax) {
  if (az >= ay) {
    // Z
    if (pos.z >= 0f) {
      uv.x = (pos.x / az + 1.0f) / 2.0f;
      uv.y = (pos.y / az + 1.0f) / 2.0f;
      uv.x = uv.x * 0.25f + 0.25f;
      uv.y = uv.y * 0.33f + 0.33f;
    } else {
      uv.x = (pos.x / az + 1.0f) / 2.0f;
      uv.y = (pos.y / az + 1.0f) / 2.0f;
      uv.x = uv.x * 0.25f + 0.75f;
      uv.y = uv.y * 0.33f + 0.33f;
    }
  }
}

// uv.x = (pos.x + 1.0f) / 2.0f;
//uv.y = (pos.y + 1.0f) / 2.0f;


   vec4 cubeColor = vec4(0.0,0.0,0.0,0.0);
   if (cubeMapSwitch < 2f) {
      cubeColor = texture2D(cubeMap, uv);
   } else if (cubeMapSwitch < 3f) {
      cubeColor = texture2D(cubeMapMatte, uv);
   } else {
      cubeColor = texture2D(cubeMapMetal, uv);
   }
   cubeColor = mix(cubeColor, col, cubeColor);
   gl_FragColor.r = cubeColor.r;
   gl_FragColor.g = cubeColor.g;
   gl_FragColor.b = cubeColor.b;
   // gl_FragColor.r = (pos.x + 1.0f) / 2.0f;
   // gl_FragColor.g = (pos.y + 1.0f) / 2.0f;
   // gl_FragColor.b = (pos.z + 1.0f) / 2.0f;
   gl_FragColor.a = 1.0f;
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
      attenFactor = 1.0/( gl_LightSource[i].constantAttenuation +
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
    if (noLightSwitch > 0.0f) {
       lightDir = normal;
       attenFactor = .6;
    } else {
       if (normalSwitch == 0f) lightDir = vec3(-lightDir.x, -lightDir.y, -lightDir.z);
    }
    // ambient + diffuse
    // lightAmbientDiffuse     += gl_FrontLightProduct[i].ambient*attenFactor;
    lightAmbientDiffuse     += gl_FrontLightProduct[i].diffuse * max(dot(normal, lightDir), 0.0) * attenFactor;
    // specular
    vec3 r      = normalize(reflect(-lightDir, normal));
    if (cubeMapSwitch > 0.0f) {
       lightSpecular          += vec4(1.0f, 1.0f, 1.0f, 1.0f) * pow(max(dot(r, eyeDir), 0.0), gl_FrontMaterial.shininess) * attenFactor;
    } else {
       lightSpecular          += gl_FrontLightProduct[i].specular * pow(max(dot(r, eyeDir), 0.0), gl_FrontMaterial.shininess) * attenFactor;
    }
    if (noTextureSwitch < 1.0f) {
       lightTextureSpecular   += gl_LightSource[i].specular * pow(max(dot(r, eyeDir), 0.0), gl_FrontMaterial.shininess) * attenFactor;
       vec4 tn           = vec4(texColor.r,texColor.g,texColor.b,1.0) * gl_LightSource[i].diffuse;
       lightTextureDiffuse     += tn * max(dot(normal, lightDir), 0.0)*attenFactor;
    }
  }
  // compute final color

  // Do a gloss map look up and compute the reflectivity.
  vec3 gloss_color = texture2D(glossMap, gl_TexCoord[0].xy).rgb;
  float reflectivity = 0f;
  if (noLightSwitch < 1.0f) {
    if (noGlossMapSwitch < 1.0f) {
      reflectivity = .30*gloss_color.r + .59*gloss_color.g + .11*gloss_color.b;
    }
  }


  if (noTextureSwitch > 0.0f) {
    if (gl_FrontLightModelProduct.sceneColor.a < 1.0f) {
          if (alphaSwitch < 1.0f) {
              gl_FragColor = (gl_FrontLightModelProduct.sceneColor + lightAmbientDiffuse) + lightSpecular * reflectivity;
              gl_FragColor.a = 1.0f;
          } else {
             discard;
          }
       } else {
          if (alphaSwitch > 0.0f) {
             if (cubeMapSwitch < 0.0f) {
               gl_FragColor = (gl_FrontLightModelProduct.sceneColor + lightAmbientDiffuse) + lightSpecular * reflectivity;
             } else {
               MyFunction((gl_FrontLightModelProduct.sceneColor + lightAmbientDiffuse) + lightSpecular);
             }
          } else {
              discard;
          }
       }
  } else {
    if (texColor.a == 0f) {
       if (gl_FrontLightModelProduct.sceneColor.a < 1.0) {
          // OK
          if (alphaSwitch < 1.0f) {
              gl_FragColor = (gl_FrontLightModelProduct.sceneColor + lightAmbientDiffuse) + lightSpecular * reflectivity;
              gl_FragColor.a = 1.0f;
          } else {
             discard;
          }
       } else {
          // OK
          if (alphaSwitch > 0.0f) {
            if (cubeMapSwitch < 0.0f) {
              gl_FragColor = (gl_FrontLightModelProduct.sceneColor + lightAmbientDiffuse) + lightSpecular * reflectivity;
            } else {
              MyFunction((gl_FrontLightModelProduct.sceneColor + lightAmbientDiffuse) + lightSpecular);
            }
          } else {
              discard;
          }
       }
    } else if (texColor.a < 1.0f) {
       vec4 groundColor = gl_FragColor = (gl_FrontLightModelProduct.sceneColor + lightAmbientDiffuse) + lightSpecular * reflectivity;
       float groundAlpha = gl_FrontLightModelProduct.sceneColor.a;
       if (groundAlpha == 1.0f) {
         if (alphaSwitch > 0.0f) {
            vec4 textureColor =  (gl_FrontLightModelProduct.sceneColor + lightTextureDiffuse) + lightTextureSpecular * reflectivity;
            textureColor = textureColor * groundColor;
            gl_FragColor.r = textureColor.r;
            gl_FragColor.g = textureColor.g;
            gl_FragColor.b = textureColor.b;
            gl_FragColor.a = 1.0f;
         } else {
            discard;
         }
       } else {
         if (alphaSwitch > 0.0f) {
            vec4 textureColor =  (gl_FrontLightModelProduct.sceneColor + lightTextureDiffuse) + lightTextureSpecular * reflectivity;
            float oneMinusGroundAlpha = 1.0f - groundAlpha;
            gl_FragColor.r = textureColor.r * oneMinusGroundAlpha - groundColor.r * groundAlpha;
            gl_FragColor.g = textureColor.g * oneMinusGroundAlpha - groundColor.g * groundAlpha;
            gl_FragColor.b = textureColor.b * oneMinusGroundAlpha - groundColor.b * groundAlpha;
            gl_FragColor.a = 1.0f;
         } else {
            discard;
         }
      }
    } else {
       // OK
       if (alphaSwitch > 0.0f) {
           vec4 textureColor =  (gl_FrontLightModelProduct.sceneColor + lightTextureDiffuse) + lightTextureSpecular * reflectivity;
           gl_FragColor.r = textureColor.r;
           gl_FragColor.g = textureColor.g;
           gl_FragColor.b = textureColor.b;
           gl_FragColor.a = 1.0f;
       } else {
           discard;
       }
    }
}

  /*
  else if (gloss_color.x == 0.0)
  {
    texColor = gl_Color * texColor;
    gl_FragColor  = texColor * (gl_FrontLightModelProduct.sceneColor + lightAmbientDiffuse) + lightSpecular;
    gl_FragColor.w = gl_Color.w;
  } else {
    float reflectivity = 0.30*gloss_color.r + 0.59*gloss_color.g + 0.11*gloss_color.b;
    texColor = gl_Color * texColor;
    gl_FragColor  = texColor * (gl_FrontLightModelProduct.sceneColor + lightAmbientDiffuse) + lightSpecular * reflectivity;
    gl_FragColor.w = gl_Color.w;
  }
  */
}

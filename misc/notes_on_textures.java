
int pId = 0;

private void setupShaders() {
        // Load the vertex shader
        vsId = this.loadShader("src/thequad/vertex_textured.glsl", GL20.GL_VERTEX_SHADER);
        // Load the fragment shader
        fsId = this.loadShader("gloss.frag", GL20.GL_FRAGMENT_SHADER);

        // Create a new shader program that links both shaders
        pId = GL20.glCreateProgram();
        GL20.glAttachShader(pId, vsId);
        GL20.glAttachShader(pId, fsId);

        // Position information will be attribute 0
        GL20.glBindAttribLocation(pId, 0, "in_Position");
        // Color information will be attribute 1
        GL20.glBindAttribLocation(pId, 1, "in_Color");
        // Textute information will be attribute 2
        GL20.glBindAttribLocation(pId, 2, "in_TextureCoord");

        GL20.glLinkProgram(pId);
        GL20.glValidateProgram(pId);

    }


// Shaders in action

GL20.glUseProgram(pId);
(draw things)
GL20.glUseProgram(0);

// Dispose shaders

GL20.glUseProgram(0);
GL20.glDetachShader(pId, vsId);
GL20.glDetachShader(pId, fsId);

GL20.glDeleteShader(vsId);
GL20.glDeleteShader(fsId);
GL20.glDeleteProgram(pId);

// Glossmap - Shader (GLSL)

GLint baseImageLoc = glGetUniformLocation(pId, "colorMap");
GLint normalMapLoc = glGetUniformLocation(pId, "glossMap");

glUseProgram(pId);
glUniform1i(baseImageLoc, 0); //Texture unit 0 is for base images.
glUniform1i(normalMapLoc, 2); //Texture unit 2 is for normal maps.

//When rendering an objectwith this program.
glActiveTexture(GL_TEXTURE0 + 0);
glBindTexture(GL_TEXTURE_2D, object1BaseImage);
glBindSampler(0, linearFiltering);
glActiveTexture(GL_TEXTURE0 + 2);
glBindTexture(GL_TEXTURE_2D, object1NormalMap);
glBindSampler(2, linearFiltering); //Same filtering as before

/*


[Pixel_Shader]

varying vec4 color
uniform sampler2D colorMap;
uniform sampler2D glossMap;

void main (void)
{
    vec4 output_color;

    color = gl_Color.rgba

    // Perform a simple 2D texture look up.
    vec4 base_color = texture2D(colorMap, gl_TexCoord[0].st * 2.0).rgba;

    // Do a gloss map look up and compute the reflectivity.
    vec3 gloss_color = texture2D(glossMap, gl_TexCoord[0].st).rgb;
    float reflectivity = 0.30*gloss_color.r +
            0.59*gloss_color.g +
            0.11*gloss_color.b;

    // Write the final pixel.
    gl_FragColor = base_color + (color*reflectivity);
}

 */

public class RenderEngine
{
    private IntBuffer intbuf = BufferUtils.createIntBuffer(1);
    private ColorModel glAlphaColorModel;
    private ColorModel glColorModel;

    public RenderEngine()
    {
        this.glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 }, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        this.glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 0 }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
    }

    GL11

    public void bindTexture(String filename)
    {
        try
        {
            File file = new File(CivilPolitica.instance.getDir(), "resources/" + filename);
            FileInputStream fis = new FileInputStream(file);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.getTexture(fis));
            fis.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private int getTexture(InputStream in)
    {
        try
        {
            GL11.glGenTextures(this.intbuf);
            int id = this.intbuf.get(0);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

            BufferedImage bi = ImageIO.read(in);
            int format = bi.getColorModel().hasAlpha() ? GL11.GL_RGBA : GL11.GL_RGB;

            ByteBuffer texData;
            WritableRaster raster;
            BufferedImage texImage;

            int texWidth = 2;
            int texHeight = 2;

            while (texWidth < bi.getWidth())
            {
                texWidth *= 2;
            }
            while (texHeight < bi.getHeight())
            {
                texHeight *= 2;
            }

            if (bi.getColorModel().hasAlpha())
            {
                raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth, texHeight, 4, null);
                texImage = new BufferedImage(this.glAlphaColorModel, raster, false, new Hashtable<String, Object>());
            }
            else
            {
                raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, texWidth, texHeight, 3, null);
                texImage = new BufferedImage(this.glColorModel, raster, false, new Hashtable<String, Object>());
            }

            Graphics g = texImage.getGraphics();
            g.setColor(new Color(0f, 0f, 0f, 0f));
            g.fillRect(0, 0, texWidth, texHeight);
            g.drawImage(bi, 0, 0, null);

            byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();

            texData = ByteBuffer.allocateDirect(data.length);
            texData.order(ByteOrder.nativeOrder());
            texData.put(data, 0, data.length);
            texData.flip();

            glTexParameteri(GL11.GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            glTexParameteri(GL11.GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, bi.getWidth(), bi.getHeight(), 0, format, GL_UNSIGNED_BYTE, texData);

            return id;
        }
        catch (Exception e)
        {
        e.printStackTrace();
            System.exit(0);
        return 0;
       }
   }
}



    CivilPolitica.instance.renderer.bindTexture("test.png");

        GL11.glPushMatrix();
        GL11.glTranslatef(128, 128, 0);

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2i(0, 0);

        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2i(0, 128);

        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2i(128, 128);

        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2i(128, 0);

        GL11.glEnd();

        GL11.glPopMatrix();


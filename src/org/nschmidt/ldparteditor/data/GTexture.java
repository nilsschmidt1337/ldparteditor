/* MIT - License

Copyright (c) 2012 - this year, Nils Schmidt

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.nschmidt.ldparteditor.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.FileHelper;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.GLShader;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer20;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

/**
 * @author nils
 *
 */
public class GTexture {

    public static final GTexture NO_TEXTURE = new GTexture();

    private long accessTime = System.currentTimeMillis();

    private HashMap<OpenGLRenderer, Integer> OpenGlID = new HashMap<OpenGLRenderer, Integer>();
    private HashMap<OpenGLRenderer, Integer> OpenGlID_glossmap = new HashMap<OpenGLRenderer, Integer>();
    private HashMap<OpenGLRenderer, Integer> OpenGlID_cubemap = new HashMap<OpenGLRenderer, Integer>();
    private HashMap<OpenGLRenderer, Integer> OpenGlID_cubemapMatte = new HashMap<OpenGLRenderer, Integer>();
    private HashMap<OpenGLRenderer, Integer> OpenGlID_cubemapMetal = new HashMap<OpenGLRenderer, Integer>();
    private HashMap<OpenGLRenderer, Boolean> OpenGlDisposed = new HashMap<OpenGLRenderer, Boolean>();

    private String texture = ""; //$NON-NLS-1$
    private String glossmap = ""; //$NON-NLS-1$

    private boolean glossy = false;
    private int cubeMapIndex = 0;

    final private TexType type;
    private Vector3f point1 = new Vector3f();
    private Vector3f point2 = new Vector3f();
    private Vector3f point3 = new Vector3f();
    private Vector3f[] tripoints = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f()};
    private float a = 0f;
    private float b = 0f;

    private float width;
    private float height;

    final float EPSILON = 1f;

    private Map<GData, UV> uvCache = new HashMap<GData, UV>();
    private Set<GData> cacheUsage = new HashSet<GData>();

    private GTexture() {
        this.type = TexType.NONE;
    }

    public GTexture(TexType type, String texture, String glossmap, int useCubemap, Vector3f point1, Vector3f point2, Vector3f point3, float a, float b) {
        this.type = type;
        this.point1.set(point1);
        this.point2.set(point2);
        this.point3.set(point3);
        this.a = a;
        this.b = b;
        glossy = glossmap != null;
        cubeMapIndex = useCubemap;
        this.texture = texture;
        this.glossmap = glossmap;
    }

    private class UV {
        private final float[] uv = new float[8];

        public UV(float[] u, float[] v) {
            this.uv[0] = u[0];
            this.uv[1] = u[1];
            this.uv[2] = u[2];
            this.uv[3] = u[3];
            this.uv[4] = v[0];
            this.uv[5] = v[1];
            this.uv[6] = v[2];
            this.uv[7] = v[3];
        }

        public float[] getUV() {
            return uv;
        }
    }

    public void calcUVcoords1(float x, float y, float z, GData1 parent, GData ID) {
        if (type == TexType.SPHERICAL) tripoints[0].set(x, y, z);
        calcUVcoords(x, y, z, parent, 0, ID);
    }

    public void calcUVcoords2(float x, float y, float z, GData1 parent) {
        if (type == TexType.SPHERICAL) tripoints[1].set(x, y, z);
        calcUVcoords(x, y, z, parent, 1, null);
    }

    public void calcUVcoords3(float x, float y, float z, GData1 parent) {
        if (type == TexType.SPHERICAL) tripoints[2].set(x, y, z);
        calcUVcoords(x, y, z, parent, 2, null);
    }

    public void calcUVcoords4(float x, float y, float z, GData1 parent) {
        calcUVcoords(x, y, z, parent, 3, null);
    }

    private float[] tU = new float[4];
    private float[] tV = new float[4];
    private boolean cacheTriggered = false;

    private void calcUVcoords(float x, float y, float z, GData1 parent, int i, GData ID) {
        if (cacheTriggered)
            return;
        if (i == 0) {
            if (ID != null && uvCache.containsKey(ID)) {
                cacheUsage.add(ID);
                cacheTriggered = true;
                float[] cacheUV = uvCache.get(ID).getUV();
                tU[0] = cacheUV[0];
                tU[1] = cacheUV[1];
                tU[2] = cacheUV[2];
                tU[3] = cacheUV[3];
                tV[0] = cacheUV[4];
                tV[1] = cacheUV[5];
                tV[2] = cacheUV[6];
                tV[3] = cacheUV[7];
                return;
            }
        }
        Vector4f realPos = new Vector4f(x, y, z, 1f);
        Matrix4f.transform(parent.productMatrix, realPos, realPos);
        float[] uv = new float[2];
        if (type == TexType.PLANAR) {
            final Vector3f diff1to3 = Vector3f.sub(point3, point1, null);
            final Vector3f diff1to2 = Vector3f.sub(point2, point1, null);
            final float length1to3 = diff1to3.length();
            final float length1to2 = diff1to2.length();
            if (length1to2 < EPSILON || length1to3 < EPSILON) {
                uv[0] = 0f;
                uv[1] = 0f;
                return;
            }
            diff1to2.normalise();
            diff1to3.normalise();
            final Vector3f pos = new Vector3f(realPos.x, realPos.y, realPos.z);
            float u = Vector3f.dot(Vector3f.sub(point1, pos, null), diff1to2);
            float v = Vector3f.dot(Vector3f.sub(point1, pos, null), diff1to3);
            if (v < 0f) {
                v = -v / length1to3;
                if (v > 1f)
                    v = 1f;
            } else {
                v = v / length1to3;
                if (v > 1f)
                    v = 1f;
            }
            if (u < 0f) {
                u = -u / length1to2;
                if (u > 1f)
                    u = 1f;
            } else {
                u = u / length1to2;
                if (u > 1f)
                    u = 1f;
            }
            tU[i] = u;
            tV[i] = v;
            uv[0] = u;
            uv[1] = v;
        } else if (type == TexType.CYLINDRICAL) {
            final Vector3f pos = new Vector3f(realPos.x, realPos.y, realPos.z);
            final Vector3f diff1to2 = Vector3f.sub(point2, point1, null);
            final Vector3f P1n = (Vector3f) Vector3f.sub(point2, point1, null).normalise();
            final float P1d = Vector3f.dot(Vector3f.sub(point1, pos, null), P1n);
            final float length1to2 = diff1to2.length();
            float v = P1d;
            if (v > 0f) {
                v = -v / length1to2;
                if (v > 1f)
                    v = 1f;
            } else {
                v = v / length1to2;
                if (v > 1f)
                    v = 1f;
            }
            uv[1] = v;
            tV[i] = v;
            final Vector3f P1D = new Vector3f(P1d * P1n.x, P1d * P1n.y, P1d * P1n.z);
            final Vector3f posP1 = Vector3f.add(pos, P1D, null);
            final Vector3f diff1toP1 = Vector3f.sub(posP1, point1, null);
            final Vector3f diff1to3 = Vector3f.sub(point3, point1, null);
            uv[0] = Vector3f.angle(diff1toP1, diff1to3);
            Vector3f cross = Vector3f.cross(diff1toP1, diff1to3, null);
            if (cross.length() == 0f) {
                cross = Vector3f.cross(diff1toP1, Vector3f.cross(diff1to3, P1n, null), null);
            }
            if (Vector3f.dot(P1n, cross) < 0f) {
                uv[0] = -uv[0];
            }
            tU[i] = uv[0];
        } else {

            final Vector4f posT = new Vector4f(realPos.x, realPos.y, realPos.z, 1f);
            final Matrix4f localToWorld = new Matrix4f();

            localToWorld.m30 = point1.x;
            localToWorld.m31 = point1.y;
            localToWorld.m32 = point1.z;

            final Vector3f bz = Vector3f.sub(point2, point1, null);
            if (bz.lengthSquared() > 0f) {
                bz.normalise();
            } else {
                uv[0] = 0f;
                uv[1] = 0f;
                return;
            }
            localToWorld.m02 = bz.x;
            localToWorld.m12 = bz.y;
            localToWorld.m22 = bz.z;

            final Vector3f by = Vector3f.sub(point3, point2, null);
            if (by.lengthSquared() > 0f) {
                by.normalise();
            } else {
                uv[0] = 0f;
                uv[1] = 0f;
                return;
            }
            localToWorld.m01 = by.x;
            localToWorld.m11 = by.y;
            localToWorld.m21 = by.z;
            final Vector3f bx = Vector3f.cross(by, bz, null);
            if (bx.lengthSquared() > 0f) {
                bx.normalise();
            } else {
                uv[0] = 0f;
                uv[1] = 0f;
                return;
            }
            localToWorld.m00 = bx.x;
            localToWorld.m10 = bx.y;
            localToWorld.m20 = bx.z;
            localToWorld.invert();
            Matrix4f.transform(localToWorld, posT, posT);
            final Vector3f d = new Vector3f(posT.x, posT.y, posT.z);
            if (d.lengthSquared() > 0f) {
                d.normalise();
            }
            tU[i] = (float) Math.atan2(d.x, d.z);
            tV[i] = (float) Math.asin(d.y);
        }
        return;
    }

    public float[] getUVcoords(boolean isTriangle, GData ID) {
        float[] result = new float[8];
        if (ID == null) {
            NLogger.debug(getClass(), "getUVcoords(): ID is null."); //$NON-NLS-1$
            return result;
        }
        final int size = isTriangle ? 3 : 4;
        if (cacheTriggered) {
            for (int i = 0; i < size; i++) {
                int r = 2 * i;
                result[r] = tU[i];
                result[r + 1] = tV[i];
            }
            cacheTriggered = false;
            return result;
        }
        if (type == TexType.PLANAR) {
            for (int i = 0; i < size; i++) {
                int r = 2 * i;
                result[r] = tU[i];
                result[r + 1] = tV[i];
            }
        } else if (type == TexType.CYLINDRICAL) {
            // Sign check
            boolean hasNegative = false;
            boolean hasPositive = false;
            for (int i = 0; i < size; i++) {
                float u = tU[i];
                if (u >= Math.PI / 2f)
                    hasPositive = true;
                if (u < -Math.PI / 2f)
                    hasNegative = true;
            }
            if (hasNegative && hasPositive) {
                for (int i = 0; i < size; i++) {
                    float u = tU[i];
                    if (u < -Math.PI / 2f) {
                        tU[i] = (float) (u + Math.PI * 2f);
                    }
                }
            }
            for (int i = 0; i < size; i++) {
                int r = 2 * i;
                float u = tU[i];
                u = (float) (u / Math.PI / 2.0);
                if (u < -.5f && a < 3.141f)
                    u = -.5f;
                if (u > .5f && a < 3.141f)
                    u = .5f;
                u = .5f - u;
                if (i > 0) {
                    float delta = Math.abs(u - tU[i - 1]);
                    if (delta > .5f) {
                        u = 1f - u;
                    }
                }
                tU[i] = u;
                result[r] = u;
                result[r + 1] = tV[i];
            }
        } else {

            // Correct poles
            if (isTriangle) {
                int poleIndex = -1;
                for (int i = 0; i < size; i++) {
                    float u = tU[i];
                    float v = tV[i];
                    if (u < 0.001f && (v < -1.56f || v > 1.56f  )) {
                        poleIndex = i;
                        break;
                    }
                }

                if (poleIndex != -1) {

                    Vector3f pole = tripoints[poleIndex];

                    Vector3f adjacent1 = tripoints[(poleIndex + 1) % 3];
                    Vector3f adjacent2 = tripoints[(poleIndex + 2) % 3];

                    Vector3f adjacentMidpoint = new Vector3f();
                    Vector3f.add(adjacent1, adjacent2, adjacentMidpoint);
                    adjacentMidpoint.scale(0.5f);

                    Vector3f deltaPoleMidpoint = new Vector3f();
                    Vector3f.sub(pole, adjacentMidpoint, deltaPoleMidpoint);
                    deltaPoleMidpoint.scale(0.999f);

                    Vector3f.add(adjacentMidpoint, deltaPoleMidpoint, pole);

                    calcUVcoords(pole.x, pole.y, pole.z, ID.parent, poleIndex, ID);
                }
            }

            // Sign check
            boolean hasNegative = false;
            boolean hasPositive = false;
            for (int i = 0; i < size; i++) {
                float u = tU[i];
                if (u >= Math.PI / 2f)
                    hasPositive = true;
                if (u < -Math.PI / 2f)
                    hasNegative = true;
            }
            if (hasNegative && hasPositive) {
                for (int i = 0; i < size; i++) {
                    float u = tU[i];
                    if (u < -Math.PI / 2f) {
                        tU[i] = (float) (u + Math.PI * 2f);
                    }
                }
            }
            for (int i = 0; i < size; i++) {
                int r = 2 * i;
                float u = tU[i];
                u = (float) (u / Math.PI / 2.0);
                if (u < -.5f && a < 3.141f)
                    u = -.5f;
                if (u > .5f && a < 3.141f)
                    u = .5f;
                u = u - .5f;
                if (i > 0) {
                    float delta = Math.abs(u - tU[i - 1]);
                    if (delta > .5f) {
                        u = 1f - u;
                    }
                }

                float v = tV[i];
                v = (float) (v / Math.PI);
                if (v < -.5f)
                    v = -.5f;
                if (v > .5f)
                    v = .5f;
                v = .5f - v;
                if (i > 0) {
                    float delta = Math.abs(v - tV[i - 1]);
                    if (delta > .5f) {
                        v = 1f - v;
                    }
                }

                tU[i] = u;
                tV[i] = v;

                result[r] = u;
                result[r + 1] = v;
            }
            // u = .5f + tU[i] / a / 2f;
            // v = .5f - tV[i] / b / 2;
        }
        if (ID != null && !uvCache.containsKey(ID)) {
            uvCache.put(ID, new UV(tU, tV));
        }
        return result;
    }

    public void dispose(OpenGLRenderer renderer) {
        if (OpenGlDisposed.containsKey(renderer)) {
            boolean disposed = OpenGlDisposed.get(renderer);
            int ID = OpenGlID.get(renderer);
            int ID_glossmap = OpenGlID_glossmap.get(renderer);
            int ID_cubemap = OpenGlID_cubemap.get(renderer);
            int ID_cubemapMatte = OpenGlID_cubemapMatte.get(renderer);
            int ID_cubemapMetal = OpenGlID_cubemapMetal.get(renderer);
            if (!disposed) {
                uvCache.clear();
                cacheUsage.clear();
                if (ID != -1)
                    GL11.glDeleteTextures(ID);
                if (ID_glossmap != -1)
                    GL11.glDeleteTextures(ID_glossmap);
                if (renderer.containsOnlyCubeMaps() && renderer.getC3D().getRenderMode() != 5) {
                    if (ID_cubemap != -1) GL11.glDeleteTextures(ID_cubemap);
                    if (ID_cubemapMatte != -1) GL11.glDeleteTextures(ID_cubemapMatte);
                    if (ID_cubemapMetal != -1) GL11.glDeleteTextures(ID_cubemapMetal);
                }
                OpenGlDisposed.put(renderer, true);
                OpenGlID.put(renderer, -1);
                OpenGlID_glossmap.put(renderer, -1);
                OpenGlID_cubemap.put(renderer, -1);
                OpenGlID_cubemapMatte.put(renderer, -1);
                OpenGlID_cubemapMetal.put(renderer, -1);
            }
        }
    }

    public void refreshCache() {
        if (!cacheUsage.isEmpty()) {
            Set<GData> isolatedIDs = new HashSet<GData>(uvCache.keySet());
            isolatedIDs.removeAll(cacheUsage);
            for (GData ID : isolatedIDs) {
                uvCache.remove(ID);
            }
            cacheUsage.clear();
        }
    }

    public void bind(boolean drawSolidMaterials, boolean normalSwitch, boolean lightOn, OpenGLRenderer20 renderer, int useCubeMap) {

        int ID = -1;
        int ID_glossmap = -1;
        int ID_cubemap = -1;
        int ID_cubemap_matte = -1;
        int ID_cubemap_metal = -1;
        boolean disposed = true;

        if (OpenGlDisposed.containsKey(renderer)) {
            disposed = OpenGlDisposed.get(renderer);
            ID = OpenGlID.get(renderer);
            ID_glossmap = OpenGlID_glossmap.get(renderer);
            ID_cubemap = OpenGlID_cubemap.get(renderer);
            ID_cubemap_matte = OpenGlID_cubemapMatte.get(renderer);
            ID_cubemap_metal = OpenGlID_cubemapMetal.get(renderer);
        } else {
            OpenGlDisposed.put(renderer, true);
        }

        if (disposed) {

            DatFile df = renderer.getC3D().getLockableDatFileReference();

            ID = loadPNGTexture(texture, GL13.GL_TEXTURE0, df);
            if (glossy)
                ID_glossmap = loadPNGTexture(glossmap, GL13.GL_TEXTURE1, df);
            if (cubeMapIndex > 0) {
                switch (cubeMapIndex) {
                case 1:
                    ID_cubemap = loadPNGTexture("cmap.png", GL13.GL_TEXTURE2, df ); //$NON-NLS-1$
                    break;
                case 2:
                    ID_cubemap_matte = loadPNGTexture("matte_metal.png", GL13.GL_TEXTURE3, df); //$NON-NLS-1$
                    break;
                case 3:
                    ID_cubemap_metal = loadPNGTexture("metal.png", GL13.GL_TEXTURE4, df); //$NON-NLS-1$
                    break;
                }
            }
            OpenGlDisposed.put(renderer, false);
            renderer.registerTexture(this);
            OpenGlID.put(renderer, ID);
            OpenGlID_glossmap.put(renderer, ID_glossmap);
            OpenGlID_cubemap.put(renderer, ID_cubemap);
            OpenGlID_cubemapMatte.put(renderer, ID_cubemap_matte);
            OpenGlID_cubemapMetal.put(renderer, ID_cubemap_metal);
        } else if (ID != -1) {
            accessTime = System.currentTimeMillis();
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, ID);
            GL20.glUniform1f(renderer.getAlphaSwitchLoc(), drawSolidMaterials ? 1f : 0f); // Draw transparent
            GL20.glUniform1f(renderer.getNormalSwitchLoc(), normalSwitch ? 1f : 0f); // Draw transparent
            GL20.glUniform1i(renderer.getBaseImageLoc(), 0); // Texture unit 0 is for base images.
            GL20.glUniform1f(renderer.getNoTextureSwitch(), 0f);
            GL20.glUniform1f(renderer.getNoLightSwitch(), lightOn ? 0f : 1f);
            GL20.glUniform1f(renderer.getCubeMapSwitch(), useCubeMap);

            if (glossy) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + 2);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, ID_glossmap);
                GL20.glUniform1i(renderer.getGlossMapLoc(), 2); // Texture unit 2 is for gloss maps.
                GL20.glUniform1f(renderer.getNoGlossMapSwitch(), 0f);
            } else {
                GL20.glUniform1f(renderer.getNoGlossMapSwitch(), 1f);
            }
            if (cubeMapIndex > 0) {
                switch (cubeMapIndex) {
                case 1:
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + 4);
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, ID_cubemap);
                    GL20.glUniform1i(renderer.getCubeMapLoc(), 4); // Texture unit 4 is for cube maps.
                    break;
                case 2:
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + 8);
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, ID_cubemap_matte);
                    GL20.glUniform1i(renderer.getCubeMapMatteLoc(), 8); // Texture unit 8 is for cube maps.
                    break;
                case 3:
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + 16);
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, ID_cubemap_metal);
                    GL20.glUniform1i(renderer.getCubeMapMetalLoc(), 16); // Texture unit 16 is for cube maps.
                    break;
                }
            }
        }
    }

    public void bindGL33(OpenGLRenderer renderer, GLShader shader) {

        int ID = -1;
        int ID_glossmap = -1;
        int ID_cubemap = -1;
        int ID_cubemap_matte = -1;
        int ID_cubemap_metal = -1;
        boolean disposed = true;

        if (OpenGlDisposed.containsKey(renderer)) {
            disposed = OpenGlDisposed.get(renderer);
            ID = OpenGlID.get(renderer);
            ID_glossmap = OpenGlID_glossmap.get(renderer);
            ID_cubemap = OpenGlID_cubemap.get(renderer);
            ID_cubemap_matte = OpenGlID_cubemapMatte.get(renderer);
            ID_cubemap_metal = OpenGlID_cubemapMetal.get(renderer);
        } else {
            OpenGlDisposed.put(renderer, true);
        }

        if (disposed) {

            DatFile df = renderer.getC3D().getLockableDatFileReference();

            ID = loadPNGTexture(texture, GL13.GL_TEXTURE0, df);
            if (cubeMapIndex > 0) {
                switch (cubeMapIndex) {
                case 1:
                    ID_cubemap = loadPNGTexture("cmap.png", GL13.GL_TEXTURE2, df ); //$NON-NLS-1$
                    break;
                case 2:
                    ID_cubemap_matte = loadPNGTexture("matte_metal.png", GL13.GL_TEXTURE3, df); //$NON-NLS-1$
                    break;
                case 3:
                    ID_cubemap_metal = loadPNGTexture("metal.png", GL13.GL_TEXTURE4, df); //$NON-NLS-1$
                    break;
                }
            }
            OpenGlDisposed.put(renderer, false);
            renderer.registerTexture(this);
            OpenGlID.put(renderer, ID);
            OpenGlID_glossmap.put(renderer, ID_glossmap);
            OpenGlID_cubemap.put(renderer, ID_cubemap);
            OpenGlID_cubemapMatte.put(renderer, ID_cubemap_matte);
            OpenGlID_cubemapMetal.put(renderer, ID_cubemap_metal);
        } else if (ID != -1) {
            accessTime = System.currentTimeMillis();
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, ID);
            GL20.glUniform1i(shader.getUniformLocation("ldpePngSampler"), 0); // Texture unit 0 is for base images. //$NON-NLS-1$

            if (cubeMapIndex > 0) {
                switch (cubeMapIndex) {
                case 1:
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + 4);
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, ID_cubemap);
                    GL20.glUniform1i(shader.getUniformLocation("cubeMap"), 4); // Texture unit 4 is for cube maps. //$NON-NLS-1$
                    break;
                case 2:
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + 8);
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, ID_cubemap_matte);
                    GL20.glUniform1i(shader.getUniformLocation("cubeMapMatte"), 8); // Texture unit 8 is for cube maps. //$NON-NLS-1$
                    break;
                case 3:
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + 16);
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, ID_cubemap_metal);
                    GL20.glUniform1i(shader.getUniformLocation("cubeMapMetal"), 16); // Texture unit 16 is for cube maps. //$NON-NLS-1$
                    break;
                }
            }
        }
    }

    public boolean isTooOld() {
        return System.currentTimeMillis() - accessTime > 10000;
    }

    /**
     *
     * @param filename
     * @param textureUnit
     *            e.g. GL13.GL_TEXTURE0
     * @return
     */
    private int loadPNGTexture(String filename, int textureUnit, DatFile datFile) {

        int max = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);

        ByteBuffer buf = null;
        int tWidth = 1;
        int tHeight = 1;

        if (filename == "") { //$NON-NLS-1$
            buf = ByteBuffer.allocateDirect(4);
            final byte[] bytes = new byte[] { 0, 0, 0, -1 };
            buf.put(bytes);
            buf.flip();
        } else {
            // Check folders
            File fileToOpen;
            String o_tex = WorkbenchManager.getUserSettingState().getLdrawFolderPath() + File.separator + filename;
            String o_tex_u = WorkbenchManager.getUserSettingState().getLdrawFolderPath() + File.separator + "TEXTURES" + File.separator + filename; //$NON-NLS-1$
            String o_tex_l = WorkbenchManager.getUserSettingState().getLdrawFolderPath() + File.separator + "textures" + File.separator + filename; //$NON-NLS-1$
            String u_tex = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + filename;
            String u_tex_u = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "TEXTURES" + File.separator + filename; //$NON-NLS-1$
            String u_tex_l = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + "textures" + File.separator + filename; //$NON-NLS-1$
            String p_tex = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + filename;
            String p_tex_u = Project.getProjectPath() + File.separator + "TEXTURES" + File.separator + filename; //$NON-NLS-1$
            String p_tex_l = Project.getProjectPath() + File.separator + "textures" + File.separator + filename; //$NON-NLS-1$
            String f_tex = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + File.separator + filename;
            String f_tex_u = Project.getProjectPath() + File.separator + "TEXTURES" + File.separator + filename; //$NON-NLS-1$
            String f_tex_l = Project.getProjectPath() + File.separator + "textures" + File.separator + filename; //$NON-NLS-1$
            if (datFile != null && !datFile.isProjectFile() && !View.DUMMY_DATFILE.equals(datFile)) {
                File dff = new File(datFile.getOldName()).getParentFile();
                if (dff != null && dff.exists() && dff.isDirectory()) {
                    String ap = dff.getAbsolutePath();
                    f_tex = ap + File.separator + filename;
                    f_tex_u = ap + File.separator + "TEXTURES" + File.separator + filename; //$NON-NLS-1$
                    f_tex_l = ap + File.separator + "textures" + File.separator + filename; //$NON-NLS-1$
                }
            }

            String tex = filename;

            File official_texture = new File(o_tex);
            File official_texture_u = new File(o_tex_u);
            File official_texture_l = new File(o_tex_l);
            File unofficial_texture = new File(u_tex);
            File unofficial_texture_u = new File(u_tex_u);
            File unofficial_texture_l = new File(u_tex_l);
            File project_texture = new File(p_tex);
            File project_texture_u = new File(p_tex_u);
            File project_texture_l = new File(p_tex_l);
            File local_texture = new File(f_tex);
            File local_texture_u = new File(f_tex_u);
            File local_texture_l = new File(f_tex_l);
            File texture = new File(tex);

            boolean fileExists = (
                    (fileToOpen = FileHelper.exist(local_texture)) != null
                    || (fileToOpen = FileHelper.exist(local_texture_u)) != null
                    || (fileToOpen = FileHelper.exist(local_texture_l)) != null
                    || (fileToOpen = FileHelper.exist(project_texture)) != null
                    || (fileToOpen = FileHelper.exist(project_texture_u)) != null
                    || (fileToOpen = FileHelper.exist(project_texture_l)) != null
                    || (fileToOpen = FileHelper.exist(unofficial_texture)) != null
                    || (fileToOpen = FileHelper.exist(unofficial_texture_u)) != null
                    || (fileToOpen = FileHelper.exist(unofficial_texture_l)) != null
                    || (fileToOpen = FileHelper.exist(official_texture)) != null
                    || (fileToOpen = FileHelper.exist(official_texture_u)) != null
                    || (fileToOpen = FileHelper.exist(official_texture_l)) != null
                    || (fileToOpen = FileHelper.exist(texture)) != null)
                    && fileToOpen.isFile();

            InputStream in = null;
            try {
                if (fileExists) {
                    // Open the PNG file as an FileInputStream
                    filename = fileToOpen.getAbsolutePath();
                    in = new FileInputStream(filename);
                } else {
                    // Try to get PNG file from org.nschmidt.ldparteditor.opengl
                    in = GLShader.class.getResourceAsStream(filename);
                    if (in == null) {
                        return -1;
                    }
                }

                // Link the PNG decoder to this stream
                PNGDecoder decoder = new PNGDecoder(in);

                // Get the width and height of the texture
                tWidth = decoder.getWidth();
                tHeight = decoder.getHeight();

                this.width = tWidth;
                this.height = tHeight;

                if (tWidth > max)
                    throw new OutOfMemoryError();
                if (tHeight > max)
                    throw new OutOfMemoryError();

                // Decode the PNG file in a ByteBuffer
                buf = ByteBuffer.allocateDirect(4 * tWidth * tHeight);
                decoder.decode(buf, tWidth * 4, Format.RGBA);
                buf.flip();

                final List<Byte> bytes;
                {
                    final byte[] tbytes = new byte[buf.remaining()];
                    buf.get(tbytes);
                    buf = ByteBuffer.allocateDirect(1);
                    bytes = new ArrayList<Byte>(tbytes.length);
                    for (final byte b : tbytes) {
                        bytes.add(b);
                    }
                }

                // TODO angle dependent adjustment (alpha fill)
                if (textureUnit != GL13.GL_TEXTURE2 && textureUnit != GL13.GL_TEXTURE3 && textureUnit != GL13.GL_TEXTURE4 && (type == TexType.CYLINDRICAL || type == TexType.SPHERICAL)) {

                    int delta = (int) (tWidth * (Math.PI / a - 1f) / 2f);
                    if (tWidth + delta > max || delta / a > max)
                        throw new OutOfMemoryError();

                    List<Byte> stride = new ArrayList<Byte>(tWidth);
                    final int strideLength = tWidth * 4;
                    for (int j = 0; j < delta; j++) {
                        stride.add((byte) 0x00);
                        stride.add((byte) 0x00);
                        stride.add((byte) 0x00);
                        stride.add((byte) 0x00);
                    }
                    final int strideLength2 = stride.size();
                    int index = 0;
                    for (int i = 0; i < tHeight; i++) {
                        if (index + 1 > bytes.size()) {
                            break;
                        }
                        bytes.addAll(index, stride);
                        index += strideLength + strideLength2;
                        if (index + 1 > bytes.size()) {
                            if (index == bytes.size()) {
                                bytes.addAll(stride);
                            } else {
                                break;
                            }
                        } else {
                            bytes.addAll(index, stride);
                        }
                        index += strideLength2;
                    }
                    tWidth = tWidth + delta * 2;
                    if (type == TexType.SPHERICAL) {
                        int delta2 = (int) (tHeight * (Math.PI / (b * 2f) - 1f) / 2f);
                        if (tHeight + delta2 > max || delta2 / b > max)
                            throw new OutOfMemoryError();
                        stride.clear();
                        for (int j = 0; j < tWidth; j++) {
                            stride.add((byte) 0x00);
                            stride.add((byte) 0x00);
                            stride.add((byte) 0x00);
                            stride.add((byte) 0x00);
                        }
                        for (int i = 0; i < delta2; i++) {
                            bytes.addAll(0, stride);
                            bytes.addAll(stride);
                        }
                        tHeight = tHeight + delta2 * 2;
                    }
                }

                int c = 0;
                final int as = bytes.size();
                for (int i = 0; i < as; i++) {
                    c++;
                    byte b = bytes.get(i);
                    if (c < 4) {
                        if (b == (byte) 0xFF)
                            bytes.set(i, (byte) 0xFE);
                        if (b == (byte) 0x01)
                            bytes.set(i, (byte) 0x02);
                        if (b == (byte) 0x00)
                            bytes.set(i, (byte) 0x02);
                    } else {
                        c = 0;
                    }
                }
                {
                    byte[] tmp = new byte[bytes.size()];
                    c = 0;
                    for (final byte b : bytes) {
                        tmp[c] = b;
                        c++;
                    }
                    buf = ByteBuffer.allocateDirect(4 * tWidth * tHeight);
                    buf.put(tmp);
                    buf.flip();
                }

                in.close();
            } catch (OutOfMemoryError e) {
                tWidth = 1;
                tHeight = 1;
                buf = ByteBuffer.allocateDirect(4);
                final byte[] bytes = new byte[] { 0, 0, 0, -1 };
                buf.put(bytes);
                buf.flip();
                // TODO Debug only System.gc();
                try {
                    in.close();
                } catch (Exception ex) {
                }
            } catch (BufferOverflowException e) {
                tWidth = 1;
                tHeight = 1;
                buf = ByteBuffer.allocateDirect(4);
                final byte[] bytes = new byte[] { 0, 0, 0, -1 };
                buf.put(bytes);
                buf.flip();
                // TODO Debug only System.gc();
                try {
                    in.close();
                } catch (Exception ex) {
                }
            } catch (IOException e) {
                try {
                    in.close();
                } catch (Exception ex) {
                }
                return -1;
            }
        }

        // Create a new texture object in memory and bind it
        int texId = GL11.glGenTextures();
        GL13.glActiveTexture(textureUnit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

        // All RGB bytes are aligned to each other and each component is 1 byte
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        // Upload the texture data
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, tWidth, tHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);

        // Setup the ST coordinate system
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        // Setup what to do when the texture has to be scaled
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

        return texId;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public TexType getType() {
        return type;
    }

    public Vector4f getPoint1() {
        return new Vector4f(point1.x, point1.y, point1.z, 1f);
    }

    public Vector4f getPoint2() {
        return new Vector4f(point2.x, point2.y, point2.z, 1f);
    }

    public Vector4f getPoint3() {
        return new Vector4f(point3.x, point3.y, point3.z, 1f);
    }

    public int getCubeMapIndex() {
        return cubeMapIndex;
    }
}

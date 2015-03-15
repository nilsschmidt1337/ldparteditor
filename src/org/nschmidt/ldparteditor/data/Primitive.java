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

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Primitive {


    private String name = ""; //$NON-NLS-1$
    private String description = ""; //$NON-NLS-1$
    private ArrayList<GData> graphicalData = new ArrayList<GData>();
    private ArrayList<Primitive> primitives = new ArrayList<Primitive>();
    private ArrayList<Primitive> primitivesExtended = new ArrayList<Primitive>();
    private boolean extended = false;
    private boolean category = false;
    private float zoom = 1f;

    public Primitive() {
        primitives.add(this);
        primitivesExtended.add(this);
    }

    public Primitive(boolean category) {
        primitives.add(this);
        primitivesExtended.add(this);
        primitivesExtended.add(new Primitive());
        setCategory(true);
    }

    public void toggle() {
        if (isCategory()) {
            this.extended = !this.extended;
        }
    }

    public ArrayList<Primitive> getPrimitives() {
        if (isExtended()) {
            return primitivesExtended;
        } else {
            return primitives;
        }
    }

    public void draw(float x, float y, FloatBuffer m) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x + 10f, y + 10f, 0f);
        GL11.glMultMatrix(m);
        GL11.glScalef(zoom, zoom, zoom);
        for (GData gd : graphicalData) {
            gd.drawBFCprimitive();
        }
        GL11.glPopMatrix();
    }

    public void setPrimitives(ArrayList<Primitive> primitives) {
        this.primitives = primitives;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public boolean isCategory() {
        return category;
    }

    public void setCategory(boolean category) {
        this.category = category;
    }

    public ArrayList<GData> getGraphicalData() {
        return graphicalData;
    }

    public void setGraphicalData(ArrayList<GData> graphicalData) {
        this.graphicalData = graphicalData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " - " + description; //$NON-NLS-1$
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public void calculateZoom() {
        float maxX = 0f;
        float maxY = 0f;
        float maxZ = 0f;
        for (GData gd : graphicalData) {
            switch (gd.type()) {
            case 1:
                float[] result = calculateZoom(maxX, maxY, maxZ, (GData1) gd);
                maxX = Math.max(maxX, result[0]);
                maxY = Math.max(maxY, result[1]);
                maxZ = Math.max(maxZ, result[2]);
                break;
            case 2:
                GData2 gd2 = (GData2) gd;
                maxX = Math.max(maxX, Math.abs(gd2.x1));
                maxX = Math.max(maxX, Math.abs(gd2.x2));
                maxY = Math.max(maxY, Math.abs(gd2.y1));
                maxY = Math.max(maxY, Math.abs(gd2.y2));
                maxZ = Math.max(maxZ, Math.abs(gd2.z1));
                maxZ = Math.max(maxZ, Math.abs(gd2.z2));
                break;
            case 3:
                GData3 gd3 = (GData3) gd;
                maxX = Math.max(maxX, Math.abs(gd3.x1));
                maxX = Math.max(maxX, Math.abs( gd3.x2));
                maxX = Math.max(maxX, Math.abs(gd3.x3));
                maxY = Math.max(maxY, Math.abs(gd3.y1));
                maxY = Math.max(maxY, Math.abs(gd3.y2));
                maxY = Math.max(maxY, Math.abs(gd3.y3));
                maxZ = Math.max(maxZ, Math.abs(gd3.z1));
                maxZ = Math.max(maxZ, Math.abs(gd3.z2));
                maxZ = Math.max(maxZ, Math.abs(gd3.z3));
                break;
            case 4:
                GData4 gd4 = (GData4) gd;
                maxX = Math.max(maxX, Math.abs(gd4.x1));
                maxX = Math.max(maxX, Math.abs(gd4.x2));
                maxX = Math.max(maxX, Math.abs(gd4.x3));
                maxX = Math.max(maxX, Math.abs(gd4.x4));
                maxY = Math.max(maxY, Math.abs(gd4.y1));
                maxY = Math.max(maxY, Math.abs(gd4.y2));
                maxY = Math.max(maxY, Math.abs(gd4.y3));
                maxY = Math.max(maxY, Math.abs(gd4.y4));
                maxZ = Math.max(maxZ, Math.abs(gd4.z1));
                maxZ = Math.max(maxZ, Math.abs(gd4.z2));
                maxZ = Math.max(maxZ, Math.abs(gd4.z3));
                maxZ = Math.max(maxZ, Math.abs(gd4.z4));
                break;
            case 5:
                GData5 gd5 = (GData5) gd;
                maxX = Math.max(maxX, Math.abs(gd5.x1));
                maxX = Math.max(maxX, Math.abs(gd5.x2));
                maxY = Math.max(maxY, Math.abs(gd5.y1));
                maxY = Math.max(maxY, Math.abs(gd5.y2));
                maxZ = Math.max(maxZ, Math.abs(gd5.z1));
                maxZ = Math.max(maxZ, Math.abs(gd5.z2));
                break;
            default:
                break;
            }
        }
        Vector3f maxV = new Vector3f(maxX, maxY, maxZ);
        float length = maxV.length();
        if (length > 0.0001f)
            zoom = 10f / length;
    }

    private float[] calculateZoom(float maxX, float maxY, float maxZ, GData1 gd0) {
        float[] result = new float[]{maxX, maxY, maxZ};
        Matrix4f productMatrix = gd0.productMatrix;
        if (productMatrix == null) return result;
        GData gd = gd0.myGData;
        while ((gd = gd.getNext()) != null) {
            switch (gd.type()) {
            case 1:
                float[] result2 = calculateZoom(maxX, maxY, maxZ, (GData1) gd);
                maxX = Math.max(maxX, result2[0]);
                maxY = Math.max(maxY, result2[1]);
                maxZ = Math.max(maxZ, result2[2]);
                break;
            case 2:
            {
                GData2 g = (GData2) gd;
                Vector4f v1 = new Vector4f(g.x1, g.y1, g.z1, 1f);
                Vector4f v2 = new Vector4f(g.x2, g.y2, g.z2, 1f);
                Matrix4f.transform(productMatrix, v1, v1);
                Matrix4f.transform(productMatrix, v2, v2);
                maxX = Math.max(maxX, Math.abs(v1.x));
                maxX = Math.max(maxX, Math.abs(v2.x));
                maxY = Math.max(maxY, Math.abs(v1.y));
                maxY = Math.max(maxY, Math.abs(v2.y));
                maxZ = Math.max(maxZ, Math.abs(v1.z));
                maxZ = Math.max(maxZ, Math.abs(v2.z));
            }
            break;
            case 3:
            {
                GData3 g = (GData3) gd;
                Vector4f v1 = new Vector4f(g.x1, g.y1, g.z1, 1f);
                Vector4f v2 = new Vector4f(g.x2, g.y2, g.z2, 1f);
                Vector4f v3 = new Vector4f(g.x3, g.y3, g.z3, 1f);
                Matrix4f.transform(productMatrix, v1, v1);
                Matrix4f.transform(productMatrix, v2, v2);
                Matrix4f.transform(productMatrix, v3, v3);
                maxX = Math.max(maxX, Math.abs(v1.x));
                maxX = Math.max(maxX, Math.abs(v2.x));
                maxX = Math.max(maxX, Math.abs(v3.x));
                maxY = Math.max(maxY, Math.abs(v1.y));
                maxY = Math.max(maxY, Math.abs(v2.y));
                maxY = Math.max(maxY, Math.abs(v3.y));
                maxZ = Math.max(maxZ, Math.abs(v1.z));
                maxZ = Math.max(maxZ, Math.abs(v2.z));
                maxZ = Math.max(maxZ, Math.abs(v3.z));
            }
            break;
            case 4:
            {
                GData4 g = (GData4) gd;
                Vector4f v1 = new Vector4f(g.x1, g.y1, g.z1, 1f);
                Vector4f v2 = new Vector4f(g.x2, g.y2, g.z2, 1f);
                Vector4f v3 = new Vector4f(g.x3, g.y3, g.z3, 1f);
                Vector4f v4 = new Vector4f(g.x4, g.y4, g.z4, 1f);
                Matrix4f.transform(productMatrix, v1, v1);
                Matrix4f.transform(productMatrix, v2, v2);
                Matrix4f.transform(productMatrix, v3, v3);
                Matrix4f.transform(productMatrix, v4, v4);
                maxX = Math.max(maxX, Math.abs(v1.x));
                maxX = Math.max(maxX, Math.abs(v2.x));
                maxX = Math.max(maxX, Math.abs(v3.x));
                maxX = Math.max(maxX, Math.abs(v4.x));
                maxY = Math.max(maxY, Math.abs(v1.y));
                maxY = Math.max(maxY, Math.abs(v2.y));
                maxY = Math.max(maxY, Math.abs(v3.y));
                maxY = Math.max(maxY, Math.abs(v4.y));
                maxZ = Math.max(maxZ, Math.abs(v1.z));
                maxZ = Math.max(maxZ, Math.abs(v2.z));
                maxZ = Math.max(maxZ, Math.abs(v3.z));
                maxZ = Math.max(maxZ, Math.abs(v4.z));
            }
            break;
            case 5:
            {
                GData5 g = (GData5) gd;
                Vector4f v1 = new Vector4f(g.x1, g.y1, g.z1, 1f);
                Vector4f v2 = new Vector4f(g.x2, g.y2, g.z2, 1f);
                Matrix4f.transform(productMatrix, v1, v1);
                Matrix4f.transform(productMatrix, v2, v2);
                maxX = Math.max(maxX, Math.abs(v1.x));
                maxX = Math.max(maxX, Math.abs(v2.x));
                maxY = Math.max(maxY, Math.abs(v1.y));
                maxY = Math.max(maxY, Math.abs(v2.y));
                maxZ = Math.max(maxZ, Math.abs(v1.z));
                maxZ = Math.max(maxZ, Math.abs(v2.z));
            }
            break;
            default:
                break;
            }
        }
        result[0] = maxX;
        result[1] = maxY;
        result[2] = maxZ;
        return result;
    }
}

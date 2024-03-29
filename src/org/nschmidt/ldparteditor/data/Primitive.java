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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.enumtype.Rule;
import org.nschmidt.ldparteditor.opengl.GLMatrixStack;

public class Primitive implements Comparable<Primitive> {

    private String name = ""; //$NON-NLS-1$
    private String description = ""; //$NON-NLS-1$
    private List<PGData> graphicalData = new ArrayList<>();
    private List<Primitive> primitives = new ArrayList<>();
    private List<Primitive> primitivesExtended = new ArrayList<>();
    private boolean extended = false;
    private boolean category = false;
    private float zoom = 1f;

    private static Pattern numberAndMinus = Pattern.compile("[\\d|\\-\\\\]+"); //$NON-NLS-1$

    public static Primitive createPrimitiveCategory() {
        final Primitive category = createPrimitive();
        category.setCategory(true);
        return category;
    }

    public static Primitive createPrimitive() {
        return new Primitive();
    }

    private Primitive() {
        primitives.add(this);
    }

    public void toggle() {
        if (isCategory()) {
            this.extended = !this.extended;
        }
    }

    public List<Primitive> getPrimitives() {
        if (isExtended()) {
            List<Primitive> result = new ArrayList<>();
            result.addAll(primitives);
            for (Primitive p : primitivesExtended) {
                result.addAll(p.getPrimitives());
            }
            if (result.size() == 1) {
                return new ArrayList<>();
            }
            return result;
        } else {
            return new ArrayList<>(primitives);
        }
    }

    public List<Primitive> getAllPrimitives() {
        if (isCategory()) {
            List<Primitive> result = new ArrayList<>();
            result.addAll(primitives);
            for (Primitive p : primitivesExtended) {
                result.addAll(p.getPrimitives());
            }
            if (result.size() == 1) {
                return new ArrayList<>();
            }
            return result;
        } else {
            return new ArrayList<>(primitives);
        }
    }

    public List<Primitive> getCategories() {
        return primitivesExtended;
    }

    public void drawGL20(float x, float y, FloatBuffer m) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x + 10f, y + 10f, 0f);
        GL11.glMultMatrixf(m);
        GL11.glScalef(-zoom, zoom, zoom);
        for (PGData gd : graphicalData) {
            switch (gd.type()) {
            case 0, 1, 3, 4, 6, 7:
                gd.drawBFCprimitiveGL20(1);
                break;
            default:
                break;
            }
        }
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef(x + 10f, y + 10f, .5f);
        GL11.glMultMatrixf(m);
        GL11.glScalef(-zoom, zoom, zoom);
        for (PGData gd : graphicalData) {
            switch (gd.type()) {
            case 0, 1, 2, 5, 6, 7:
                gd.drawBFCprimitiveGL20(2);
                break;
            default:
                break;
            }
        }
        GL11.glPopMatrix();
    }

    public void drawGL33(GLMatrixStack stack, float x, float y, Matrix4f m) {
        stack.glPushMatrix();
        stack.glTranslatef(x + 10f, y + 10f, 0f);
        stack.glMultMatrixf(m);
        stack.glScalef(-zoom, zoom, zoom);
        for (PGData gd : graphicalData) {
            switch (gd.type()) {
            case 0, 1, 3, 4, 6, 7:
                gd.drawBFCprimitiveGL33(stack, 1);
                break;
            default:
                break;
            }
        }
        stack.glPopMatrix();
        stack.glPushMatrix();
        stack.glTranslatef(x + 10f, y + 10f, .5f);
        stack.glMultMatrixf(m);
        stack.glScalef(-zoom, zoom, zoom);
        for (PGData gd : graphicalData) {
            switch (gd.type()) {
            case 0, 1, 2, 5, 6, 7:
                gd.drawBFCprimitiveGL33(stack, 2);
                break;
            default:
                break;
            }
        }
        stack.glPopMatrix();
    }

    public void draw(float x, float y, float z) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        GL11.glScalef(1000f, 1000f, 1000f);
        for (PGData gd : graphicalData) {
            gd.drawBFCprimitiveGL20(0);
        }
        GL11.glPopMatrix();
    }

    public void drawGL33(GLMatrixStack stack, float x, float y, float z) {
        stack.glPushMatrix();
        stack.glTranslatef(x, y, z);
        stack.glScalef(1000f, 1000f, 1000f);
        for (PGData gd : graphicalData) {
            gd.drawBFCprimitiveGL33(stack, 0);
        }
        stack.glPopMatrix();
    }

    public void setPrimitives(List<Primitive> primitives) {
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

    public List<PGData> getGraphicalData() {
        return graphicalData;
    }

    public void setGraphicalData(List<PGData> graphicalData) {
        this.graphicalData = graphicalData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void search(Pattern pattern, List<Primitive> results) {
        if (isCategory()) {
            for (Primitive p : primitivesExtended) {
                p.search(pattern, results);
            }
        } else if (pattern.matcher(toString()).matches()) {
            results.add(this);
        }
    }

    public void collapse() {
        if (isCategory()) {
            setExtended(false);
            for (Primitive p : primitivesExtended) {
                p.collapse();
            }
        }
    }

    public boolean sort(Rule r) {
        if (isCategory()) {
            final Primitive me = this;
            switch (r) {
            case FILENAME_ORDER_BY_ALPHABET:
                Collections.sort(primitivesExtended, (o1, o2) -> {
                    if (o1 == me) return 1;
                    if (o2 == me) return -1;
                    if (o1 == o2) return 0;
                    return o1.compareTo(o2);
                });
                break;
            case FILENAME_ORDER_BY_ALPHABET_WO_NUMBERS:
                Collections.sort(primitivesExtended, (o1, o2) -> {
                    if (o1 == me) return 1;
                    if (o2 == me) return -1;
                    if (o1 == o2) return 0;
                    String nameO1 = o1.name;
                    String nameO2 = o2.name;
                    return numberAndMinus.matcher(nameO1).replaceAll("").compareToIgnoreCase(numberAndMinus.matcher(nameO2).replaceAll("")); //$NON-NLS-1$ //$NON-NLS-2$
                });
                break;
            case FILENAME_ORDER_BY_FRACTION:
                Collections.sort(primitivesExtended, (o1, o2) -> {
                    if (o1 == me) return 1;
                    if (o2 == me) return -1;
                    if (o1 == o2) return 0;

                    float fractionThis = 0f;
                    float fractionOther = 0f;
                    String suffixThis = ""; //$NON-NLS-1$
                    String suffixOther = ""; //$NON-NLS-1$

                    try {
                        String nameO1 = o1.name;
                        if (nameO1.startsWith("48\\")) nameO1 = nameO1.substring(3); //$NON-NLS-1$
                        StringBuilder upperThis = new StringBuilder();
                        StringBuilder lowerThis = new StringBuilder();
                        if (nameO1.charAt(1) == '-' || nameO1.charAt(2) == '-') {
                            boolean readUpper = true;
                            int charCount = 0;
                            char[] charsThis = nameO1.toCharArray();
                            for (char c : charsThis) {
                                if (Character.isDigit(c)) {
                                    if (readUpper) {
                                        upperThis.append(c);
                                    } else {
                                        lowerThis.append(c);
                                    }
                                } else {
                                    if (readUpper) {
                                        readUpper = false;
                                    } else {
                                        suffixThis = nameO1.substring(charCount, nameO1.length());
                                        break;
                                    }
                                }
                                charCount++;
                            }
                            fractionThis = Float.parseFloat(upperThis.toString()) / Float.parseFloat(lowerThis.toString());
                        } else {
                            return 1;
                        }
                    } catch (Exception ex) {
                        return 1;
                    }

                    try {
                        String nameO2 = o2.name;
                        if (nameO2.startsWith("48\\")) nameO2 = nameO2.substring(3); //$NON-NLS-1$
                        StringBuilder upperOther = new StringBuilder();
                        StringBuilder lowerOther = new StringBuilder();
                        if (nameO2.charAt(1) == '-' || nameO2.charAt(2) == '-') {
                            boolean readUpper = true;
                            int charCount = 0;
                            char[] charsOther = nameO2.toCharArray();
                            for (char c : charsOther) {
                                if (Character.isDigit(c)) {
                                    if (readUpper) {
                                        upperOther.append(c);
                                    } else {
                                        lowerOther.append(c);
                                    }
                                } else {
                                    if (readUpper) {
                                        readUpper = false;
                                    } else {
                                        suffixOther = nameO2.substring(charCount, nameO2.length());
                                        break;
                                    }
                                }
                                charCount++;
                            }
                            fractionOther = Float.parseFloat(upperOther.toString()) / Float.parseFloat(lowerOther.toString());
                        } else {
                            return -1;
                        }
                    } catch (Exception ex) {
                        return -1;
                    }

                    if (!suffixThis.equals(suffixOther)) {
                        return suffixThis.compareTo(suffixOther);
                    } else {
                        if (fractionThis < fractionOther) {
                            return 1;
                        } else if (fractionThis > fractionOther) {
                            return -1;
                        }
                    }
                    return 0;
                });
                break;
            case FILENAME_ORDER_BY_LASTNUMBER:
                Collections.sort(primitivesExtended, (o1, o2) -> {
                    if (o1 == me) return 1;
                    if (o2 == me) return -1;
                    if (o1 == o2) return 0;
                    String nameO1 = o1.name;
                    String nameO2 = o2.name;
                    char[] charsThis = nameO1.toCharArray();
                    char[] charsOther = nameO2.toCharArray();
                    StringBuilder numberThis = new StringBuilder();
                    StringBuilder numberOther = new StringBuilder();
                    StringBuilder prefixThis = new StringBuilder();
                    StringBuilder prefixOther = new StringBuilder();
                    try {
                        boolean readDigit = false;
                        for (int i = charsThis.length - 1; i > 0 ; i--) {
                            char c = charsThis[i];
                            if (Character.isDigit(c)) {
                                numberThis.insert(0, c);
                                readDigit = true;
                            } else if (readDigit) {
                                for (int j = 0; j < i + 1; j++) {
                                    if (!Character.isDigit(charsThis[j])) prefixThis.append(charsThis[j]);
                                }
                                break;
                            } else if (i < charsThis.length - 5) {
                                break;
                            }
                        }
                        if (!readDigit) {
                            numberThis.setLength(0);
                            numberThis.append('0');
                        }
                    } catch (Exception ex) {
                        numberThis.setLength(0);
                        numberThis.append('0');
                    }
                    try {
                        boolean readDigit = false;
                        for (int i = charsOther.length - 1; i > 0 ; i--) {
                            char c = charsOther[i];
                            if (Character.isDigit(c)) {
                                numberOther.insert(0, c);
                                readDigit = true;
                            } else if (readDigit) {
                                for (int j = 0; j < i + 1; j++) {
                                    if (!Character.isDigit(charsOther[j])) prefixOther.append(charsOther[j]);
                                }
                                break;
                            } else if (i < charsOther.length - 5) {
                                break;
                            }
                        }
                        if (!readDigit) {
                            numberOther.setLength(0);
                            numberOther.append('0');
                        }
                    } catch (Exception ex) {
                        numberOther.setLength(0);
                        numberOther.append('0');
                    }
                    return Integer.compare(Integer.parseInt(numberThis.toString()), Integer.parseInt(numberOther.toString()));
                });
                break;
            default:
                break;
            }
        }
        return isCategory();
    }

    @Override
    public String toString() {
        if (description.isEmpty()) {
            return name;
        } else {
            return name + " - " + description; //$NON-NLS-1$
        }
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
        for (PGData gd : graphicalData) {
            gd = gd.data();
            switch (gd.type()) {
            case 1:
                float[] result = calculateZoom(maxX, maxY, maxZ, (PGData1) gd);
                maxX = Math.max(maxX, result[0]);
                maxY = Math.max(maxY, result[1]);
                maxZ = Math.max(maxZ, result[2]);
                break;
            case 2:
                PGData2 gd2 = (PGData2) gd;
                maxX = Math.max(maxX, Math.abs(gd2.x1));
                maxX = Math.max(maxX, Math.abs(gd2.x2));
                maxY = Math.max(maxY, Math.abs(gd2.y1));
                maxY = Math.max(maxY, Math.abs(gd2.y2));
                maxZ = Math.max(maxZ, Math.abs(gd2.z1));
                maxZ = Math.max(maxZ, Math.abs(gd2.z2));
                break;
            case 3:
                PGData3 gd3 = (PGData3) gd;
                maxX = Math.max(maxX, Math.abs(gd3.x1));
                maxX = Math.max(maxX, Math.abs(gd3.x2));
                maxX = Math.max(maxX, Math.abs(gd3.x3));
                maxY = Math.max(maxY, Math.abs(gd3.y1));
                maxY = Math.max(maxY, Math.abs(gd3.y2));
                maxY = Math.max(maxY, Math.abs(gd3.y3));
                maxZ = Math.max(maxZ, Math.abs(gd3.z1));
                maxZ = Math.max(maxZ, Math.abs(gd3.z2));
                maxZ = Math.max(maxZ, Math.abs(gd3.z3));
                break;
            case 4:
                PGData4 gd4 = (PGData4) gd;
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
                PGData5 gd5 = (PGData5) gd;
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

    private float[] calculateZoom(float maxX, float maxY, float maxZ, PGData1 gd0) {
        float[] result = new float[]{maxX, maxY, maxZ};
        Matrix4f productMatrix = gd0.productMatrix;
        if (productMatrix == null) return result;
        PGData gd1 = gd0.myGData;
        PGData gd2;
        while ((gd1 = gd1.getNext()) != null) {
            gd2 = gd1.data();
            switch (gd2.type()) {
            case 1:
                float[] result2 = calculateZoom(maxX, maxY, maxZ, (PGData1) gd2);
                maxX = Math.max(maxX, result2[0]);
                maxY = Math.max(maxY, result2[1]);
                maxZ = Math.max(maxZ, result2[2]);
                break;
            case 2:
            {
                PGData2 g = (PGData2) gd2;
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
                PGData3 g = (PGData3) gd2;
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
                PGData4 g = (PGData4) gd2;
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
                PGData5 g = (PGData5) gd2;
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

    @Override
    public int compareTo(Primitive o) {
        if (o.category && !category) {
            return 1;
        } else if (!o.category && category) {
            return -1;
        } else if (category) {
            return name.compareToIgnoreCase(o.name);
        } else {
            return description.compareToIgnoreCase(o.description);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, description, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Primitive))
            return false;
        Primitive other = (Primitive) obj;
        return this.compareTo(other) == 0;
    }
}

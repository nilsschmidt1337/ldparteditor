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
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.enums.Rule;
import org.nschmidt.ldparteditor.logger.NLogger;

public class Primitive implements Comparable<Primitive> {


    private String name = ""; //$NON-NLS-1$
    private String description = ""; //$NON-NLS-1$
    private ArrayList<PGData> graphicalData = new ArrayList<PGData>();
    private ArrayList<Primitive> primitives = new ArrayList<Primitive>();
    private ArrayList<Primitive> primitivesExtended = new ArrayList<Primitive>();
    private boolean extended = false;
    private boolean category = false;
    private float zoom = 1f;

    private static Pattern numberAndMinus = Pattern.compile("[\\d|\\-\\\\]+"); //$NON-NLS-1$

    public Primitive() {
        primitives.add(this);
    }

    public Primitive(boolean category) {
        primitives.add(this);
        setCategory(true);
    }

    public void toggle() {
        if (isCategory()) {
            this.extended = !this.extended;
        }
    }

    public ArrayList<Primitive> getPrimitives() {
        if (isExtended()) {
            ArrayList<Primitive> result = new ArrayList<Primitive>();
            result.addAll(primitives);
            for (Primitive p : primitivesExtended) {
                result.addAll(p.getPrimitives());
            }
            if (result.size() == 1) {
                return new ArrayList<Primitive>();
            }
            return result;
        } else {
            return new ArrayList<Primitive>(primitives);
        }
    }

    public ArrayList<Primitive> getAllPrimitives() {
        if (isCategory()) {
            ArrayList<Primitive> result = new ArrayList<Primitive>();
            result.addAll(primitives);
            for (Primitive p : primitivesExtended) {
                result.addAll(p.getPrimitives());
            }
            if (result.size() == 1) {
                return new ArrayList<Primitive>();
            }
            return result;
        } else {
            return new ArrayList<Primitive>(primitives);
        }
    }

    public ArrayList<Primitive> getCategories() {
        return primitivesExtended;
    }

    public void draw(float x, float y, FloatBuffer m) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x + 10f, y + 10f, 0f);
        GL11.glMultMatrix(m);
        GL11.glScalef(-zoom, zoom, zoom);
        for (PGData gd : graphicalData) {
            switch (gd.type()) {
            case 0:
            case 1:
            case 3:
            case 4:
            case 6:
            case 7:
                gd.drawBFCprimitive(1);
                break;
            default:
                break;
            }
        }
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef(x + 10f, y + 10f, .5f);
        GL11.glMultMatrix(m);
        GL11.glScalef(-zoom, zoom, zoom);
        for (PGData gd : graphicalData) {
            switch (gd.type()) {
            case 0:
            case 1:
            case 2:
            case 5:
            case 6:
            case 7:
                gd.drawBFCprimitive(2);
                break;
            default:
                break;
            }
        }
        GL11.glPopMatrix();
    }

    public void draw(float x, float y, float z) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        GL11.glScalef(1000f, 1000f, 1000f);
        for (PGData gd : graphicalData) {
            gd.drawBFCprimitive(0);
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

    public ArrayList<PGData> getGraphicalData() {
        return graphicalData;
    }

    public void setGraphicalData(ArrayList<PGData> graphicalData) {
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
                Collections.sort(primitivesExtended, new Comparator<Primitive>() {
                    @Override
                    public int compare(Primitive o1, Primitive o2) {
                        if (o1 == me) return 1;
                        if (o2 == me) return -1;
                        return o1.compareTo(o2);
                    }
                });
                break;
            case FILENAME_ORDER_BY_ALPHABET_WO_NUMBERS:
                Collections.sort(primitivesExtended, new Comparator<Primitive>() {
                    @Override
                    public int compare(Primitive o1, Primitive o2) {
                        if (o1 == me) return 1;
                        if (o2 == me) return -1;
                        String name_o1 = o1.name;
                        String name_o2 = o2.name;
                        return numberAndMinus.matcher(name_o1).replaceAll("").compareToIgnoreCase(numberAndMinus.matcher(name_o2).replaceAll("")); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                });
                break;
            case FILENAME_ORDER_BY_FRACTION:
                Collections.sort(primitivesExtended, new Comparator<Primitive>() {
                    @Override
                    public int compare(Primitive o1, Primitive o2) {
                        if (o1 == me) return 1;
                        if (o2 == me) return -1;
                        try {
                            String name_o1 = o1.name;
                            String name_o2 = o2.name;
                            if (name_o1.startsWith("48\\")) name_o1 = name_o1.substring(3); //$NON-NLS-1$
                            if (name_o2.startsWith("48\\")) name_o2 = name_o2.substring(3); //$NON-NLS-1$
                            // Special cases: unknown parts numbers "u[Number]" and unknown
                            // stickers "s[Number]"
                            if (name_o1.charAt(0) == 'u' && name_o2.charAt(0) == 'u' || name_o1.charAt(0) == 's' && name_o2.charAt(0) == 's') {
                                name_o1 = name_o1.substring(1, name_o1.length());
                                name_o2 = name_o2.substring(1, name_o2.length());
                            }

                            if ((name_o1.charAt(1) == '-' || name_o1.charAt(2) == '-')
                                    && (name_o2.charAt(1) == '-' || name_o2.charAt(2) == '-')) {
                                String upper_this = ""; //$NON-NLS-1$
                                String upper_other = ""; //$NON-NLS-1$
                                String lower_this = ""; //$NON-NLS-1$
                                String lower_other = ""; //$NON-NLS-1$
                                String suffix_this = ""; //$NON-NLS-1$
                                String suffix_other = ""; //$NON-NLS-1$
                                boolean readUpper = true;
                                int charCount = 0;
                                char[] chars_this = name_o1.toCharArray();
                                for (char c : chars_this) {
                                    if (Character.isDigit(c)) {
                                        if (readUpper) {
                                            upper_this = upper_this + c;
                                        } else {
                                            lower_this = lower_this + c;
                                        }
                                    } else {
                                        if (readUpper) {
                                            readUpper = false;
                                        } else {
                                            suffix_this = name_o1.substring(charCount, name_o1.length());
                                            break;
                                        }
                                    }
                                    charCount++;
                                }
                                readUpper = true;
                                charCount = 0;
                                char[] chars_other = name_o2.toCharArray();
                                for (char c : chars_other) {
                                    if (Character.isDigit(c)) {
                                        if (readUpper) {
                                            upper_other = upper_other + c;
                                        } else {
                                            lower_other = lower_other + c;
                                        }
                                    } else {
                                        if (readUpper) {
                                            readUpper = false;
                                        } else {
                                            suffix_other = name_o2.substring(charCount, name_o2.length());
                                            break;
                                        }
                                    }
                                    charCount++;
                                }
                                float fraction_this = Float.parseFloat(upper_this) / Float.parseFloat(lower_this);
                                float fraction_other = Float.parseFloat(upper_other) / Float.parseFloat(lower_other);

                                if (!suffix_this.equals(suffix_other)) {
                                    return suffix_this.compareTo(suffix_other);
                                } else {
                                    if (fraction_this < fraction_other) {
                                        return 1;
                                    } else if (fraction_this > fraction_other) {
                                        return -1;
                                    }
                                }
                            }
                            return 0; // name_o1.compareTo(name_o2);
                        } catch (Exception ex) {
                            NLogger.error(getClass(), "Can't compare primitives (ORDER_BY_FRACTION)!"); //$NON-NLS-1$
                            NLogger.error(getClass(), ex);
                            return 0; // o1.name.compareTo(o2.name);
                        }
                    }
                });
                break;
            case FILENAME_ORDER_BY_LASTNUMBER:
                Collections.sort(primitivesExtended, new Comparator<Primitive>() {
                    @Override
                    public int compare(Primitive o1, Primitive o2) {
                        if (o1 == me) return 1;
                        if (o2 == me) return -1;
                        String name_o1 = o1.name;
                        String name_o2 = o2.name;
                        char[] chars_this = name_o1.toCharArray();
                        char[] chars_other = name_o2.toCharArray();
                        String number_this = ""; //$NON-NLS-1$
                        String number_other = ""; //$NON-NLS-1$
                        String prefix_this = ""; //$NON-NLS-1$
                        String prefix_other = ""; //$NON-NLS-1$
                        try {
                            boolean readDigit = false;
                            for (int i = chars_this.length - 1; i > 0 ; i--) {
                                char c = chars_this[i];
                                if (Character.isDigit(c)) {
                                    number_this = c + number_this;
                                    readDigit = true;
                                } else if (readDigit) {
                                    for (int j = 0; j < i + 1; j++) {
                                        if (!Character.isDigit(chars_this[j])) prefix_this = prefix_this + chars_this[j];
                                    }
                                    break;
                                } else if (i < chars_this.length - 5) {
                                    break;
                                }
                            }
                            if (!readDigit) {
                                number_this = "0"; //$NON-NLS-1$
                            }
                        } catch (Exception ex) {
                            number_this = "0"; //$NON-NLS-1$
                        }
                        try {
                            boolean readDigit = false;
                            for (int i = chars_other.length - 1; i > 0 ; i--) {
                                char c = chars_other[i];
                                if (Character.isDigit(c)) {
                                    number_other = c + number_other;
                                    readDigit = true;
                                } else if (readDigit) {
                                    for (int j = 0; j < i + 1; j++) {
                                        if (!Character.isDigit(chars_other[j])) prefix_other = prefix_other + chars_other[j];
                                    }
                                    break;
                                } else if (i < chars_other.length - 5) {
                                    break;
                                }
                            }
                            if (!readDigit) {
                                number_other = "0"; //$NON-NLS-1$
                            }
                        } catch (Exception ex) {
                            number_other = "0"; //$NON-NLS-1$
                        }
                        return Integer.compare(Integer.parseInt(number_this), Integer.parseInt(number_other));
                    }
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
        PGData gd = gd0.myGData;
        while ((gd = gd.getNext()) != null) {
            switch (gd.type()) {
            case 1:
                float[] result2 = calculateZoom(maxX, maxY, maxZ, (PGData1) gd);
                maxX = Math.max(maxX, result2[0]);
                maxY = Math.max(maxY, result2[1]);
                maxZ = Math.max(maxZ, result2[2]);
                break;
            case 2:
            {
                PGData2 g = (PGData2) gd;
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
                PGData3 g = (PGData3) gd;
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
                PGData4 g = (PGData4) gd;
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
                PGData5 g = (PGData5) gd;
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
}

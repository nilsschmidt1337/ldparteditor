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
package org.nschmidt.ldparteditor.enumtype;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.colour.GCChrome;
import org.nschmidt.ldparteditor.data.colour.GCGlitter;
import org.nschmidt.ldparteditor.data.colour.GCMatteMetal;
import org.nschmidt.ldparteditor.data.colour.GCMetal;
import org.nschmidt.ldparteditor.data.colour.GCPearl;
import org.nschmidt.ldparteditor.data.colour.GCRubber;
import org.nschmidt.ldparteditor.data.colour.GCSpeckle;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

@java.lang.SuppressWarnings({"java:S1104", "java:S1444"})
public enum LDConfig {
    INSTANCE;

    public static float colour16overrideR = 0f;
    public static float colour16overrideG = 0f;
    public static float colour16overrideB = 0f;

    private static final GColour BLACK = new GColour(-1, 0f, 0f, 0f, 1f);
    private static IndexedEntry col16IndexedEntry = new IndexedEntry(.5f + .000016f, .5f + .000016f, .5f + .000016f);
    private static GColour originalColour16 = new GColour(-1, 0f, 0f, 0f, 1f);

    private static final Map<Integer, GColour> colourFromIndex = new HashMap<>();
    private static final Map<Integer, GColour> edgeColourFromIndex = new HashMap<>();
    private static final Map<Integer, String> colourNameFromIndex = new HashMap<>();

    private static final Map<IndexedEntry, Integer> indexFromColour = new HashMap<>();

    public static final GColour getColour(int index) {
        GColour result =  colourFromIndex.get(index);
        if (result == null) result = new GColour(index, 0f, 0f, 0f, 1f);
        return result;
    }

    public static final GColour getColour16() {
        GColour result =  colourFromIndex.get(16);
        if (result == null) result = new GColour(16, 0f, 0f, 0f, 1f);
        return result;
    }

    public static final boolean hasColour(int index) {
        GColour result = colourFromIndex.get(index);
        return result != null;
    }

    public static final GColour getEdgeColour(int index, Composite3D c3d) {
        if (c3d.isBlackEdges()) return LDConfig.BLACK;
        GColour result = edgeColourFromIndex.get(index);
        if (result == null) result = new GColour(index, 0f, 0f, 0f, 1f);
        return result;
    }

    public static String getColourName(Integer index) {
        return colourNameFromIndex.getOrDefault(index, "<???>"); //$NON-NLS-1$
    }

    public static final Map<Integer, GColour> getColourMap() {
        return colourFromIndex;
    }

    public static final Map<Integer, String> getNameMap() {
        return colourNameFromIndex;
    }

    /**
    *
    * @param r
    * @param g
    * @param b
    * @return {@code -1} if the index was not found
    */
   public static final int getIndex(float r, float g, float b) {
       IndexedEntry e = new IndexedEntry(r,g,b);
       if (indexFromColour.containsKey(e)) {
           return indexFromColour.get(e);
       }
       return -1;
   }

   private static class IndexedEntry {

       final float r;
       final float g;
       final float b;

       public IndexedEntry(float r, float g, float b) {
           this.r = r;
           this.g = g;
           this.b = b;
       }

       @Override
       public int hashCode() {
           final int prime = 31;
           int result = 1;
           result = prime * result + Float.floatToIntBits(b);
           result = prime * result + Float.floatToIntBits(g);
           result = prime * result + Float.floatToIntBits(r);
           return result;
       }

       @Override
       public boolean equals(Object obj) {
           if (!(obj instanceof IndexedEntry)) {
               return false;
           }

           IndexedEntry other = (IndexedEntry) obj;
           if (Float.floatToIntBits(b) != Float.floatToIntBits(other.b))
               return false;
           if (Float.floatToIntBits(g) != Float.floatToIntBits(other.g))
               return false;
           return Float.floatToIntBits(r) == Float.floatToIntBits(other.r);
       }
   }

   public static final boolean loadConfig(String location) {
       if (new File(location).exists()) {
           indexFromColour.clear();
           edgeColourFromIndex.clear();
           colourFromIndex.clear();
           colourNameFromIndex.clear();
           Pattern pAlpha = Pattern.compile("ALPHA\\s+\\d+"); //$NON-NLS-1$
           Pattern pFraction = Pattern.compile("FRACTION\\s+\\d+.?\\d*"); //$NON-NLS-1$
           Pattern pSize = Pattern.compile("SIZE\\s+\\d+.?\\d*"); //$NON-NLS-1$
           Pattern pMinSize = Pattern.compile("MINSIZE\\s+\\d+.?\\d*"); //$NON-NLS-1$
           Pattern pMaxSize = Pattern.compile("MAXSIZE\\s+\\d+.?\\d*"); //$NON-NLS-1$
           Pattern pSpeckle = Pattern.compile("SPECKLE\\s+VALUE\\s+#[A-F0-9]{6}"); //$NON-NLS-1$
           Pattern pGlitter = Pattern.compile("GLITTER\\s+VALUE\\s+#[A-F0-9]{6}"); //$NON-NLS-1$
           try (UTF8BufferedReader reader = new UTF8BufferedReader(location)) {
               indexFromColour.put(new IndexedEntry(Colour.lineColourR, Colour.lineColourG, Colour.lineColourB), 24);
               while (true) {
                   String line = reader.readLine();
                   if (line == null) {
                       break;
                   }
                   String[] dataSegments = line.trim().split("\\s+"); //$NON-NLS-1$
                   if (dataSegments.length > 6 && "!COLOUR".equals(dataSegments[1])) { //$NON-NLS-1$
                       int index = Integer.parseInt(dataSegments[4]);

                       float magicIndexNumber = index;
                       while (magicIndexNumber > 512f) {
                           magicIndexNumber = magicIndexNumber / 13.37f;
                       }

                       float r = Integer.parseInt(dataSegments[6].substring(1, 3), 16) / 255f + .000001f * magicIndexNumber;
                       float g = Integer.parseInt(dataSegments[6].substring(3, 5), 16) / 255f + .000001f * magicIndexNumber;
                       float b = Integer.parseInt(dataSegments[6].substring(5, 7), 16) / 255f + .000001f * magicIndexNumber;

                       float r2 = Integer.parseInt(dataSegments[8].substring(1, 3), 16) / 255f;
                       float g2 = Integer.parseInt(dataSegments[8].substring(3, 5), 16) / 255f;
                       float b2 = Integer.parseInt(dataSegments[8].substring(5, 7), 16) / 255f;

                       Matcher m = pAlpha.matcher(line);

                       if (m.find()) {
                           String alphaStr = m.group().replace("ALPHA", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                           float alpha = Float.parseFloat(alphaStr) / 255f;
                           GColour colour = new GColour(index, r, g, b, alpha);
                           if (line.contains(" MATERIAL")) { //$NON-NLS-1$
                               try {

                                   Matcher m2 = pFraction.matcher(line);
                                   Matcher m3 = pSize.matcher(line);
                                   Matcher m4 = pMinSize.matcher(line);
                                   Matcher m5 = pMaxSize.matcher(line);

                                   m2.find();

                                   float fraction = Float.parseFloat(m2.group().replaceAll("FRACTION\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                   float minSize = 0f;
                                   float maxSize = 0f;
                                   if (!m4.find()) {
                                       m3.find();
                                       minSize = Float.parseFloat(m3.group().replaceAll("SIZE\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                       maxSize = minSize;
                                   } else {
                                       m5.find();
                                       minSize = Float.parseFloat(m4.group().replaceAll("MINSIZE\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                       maxSize = Float.parseFloat(m5.group().replaceAll("MAXSIZE\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                   }

                                   if (line.contains(" GLITTER")) { //$NON-NLS-1$
                                       Matcher m6 = pGlitter.matcher(line);
                                       m6.find();
                                       String valStr = m6.group().replaceAll("GLITTER\\s+VALUE\\s+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                       float vR = Integer.parseInt(valStr.substring(1, 3), 16) / 255f;
                                       float vG = Integer.parseInt(valStr.substring(3, 5), 16) / 255f;
                                       float vB = Integer.parseInt(valStr.substring(5, 7), 16) / 255f;
                                       colour = new GColour(index, r, g, b, Math.min(alpha, .99f), new GCGlitter(vR, vG, vB, fraction, minSize, maxSize));
                                   } else if (line.contains(" SPECKLE")) { //$NON-NLS-1$
                                       Matcher m6 = pSpeckle.matcher(line);
                                       m6.find();
                                       String valStr = m6.group().replaceAll("SPECKLE\\s+VALUE\\s+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                       float vR = Integer.parseInt(valStr.substring(1, 3), 16) / 255f;
                                       float vG = Integer.parseInt(valStr.substring(3, 5), 16) / 255f;
                                       float vB = Integer.parseInt(valStr.substring(5, 7), 16) / 255f;
                                       colour = new GColour(index, r, g, b, Math.min(alpha, .99f), new GCSpeckle(vR, vG, vB, fraction, minSize, maxSize));
                                   }
                               } catch (Exception e) {
                                   NLogger.error(View.class, "Line: " + line); //$NON-NLS-1$
                                   NLogger.error(View.class, e);
                               }
                           }
                           colourFromIndex.put(index, colour);
                       } else if (line.contains(" CHROME")) { //$NON-NLS-1$
                           GColour colour = new GColour(index, r, g, b, 1f, new GCChrome());
                           colourFromIndex.put(index, colour);
                       } else if (line.contains(" RUBBER")) { //$NON-NLS-1$
                           GColour colour = new GColour(index, r, g, b, 1f, new GCRubber());
                           colourFromIndex.put(index, colour);
                       } else if (line.contains(" MATTE_METALLIC")) { //$NON-NLS-1$
                           GColour colour = new GColour(index, r, g, b, 1f, new GCMatteMetal());
                           colourFromIndex.put(index, colour);
                       } else if (line.contains(" METAL")) { //$NON-NLS-1$
                           GColour colour = new GColour(index, r, g, b, 1f, new GCMetal());
                           colourFromIndex.put(index, colour);
                       } else if (line.contains(" PEARLESCENT")) { //$NON-NLS-1$
                           if (WorkbenchManager.getUserSettingState().getOpenGLVersion() == 20) {
                               GColour colour = new GColour(index, r, g, b, .99f, new GCPearl());
                               colourFromIndex.put(index, colour);
                           } else {
                               GColour colour = new GColour(index, r, g, b, 1f, new GCPearl());
                               colourFromIndex.put(index, colour);
                           }
                       } else {
                           GColour colour = new GColour(index, r, g, b, 1f);
                           if (line.contains(" MATERIAL")) { //$NON-NLS-1$
                               try {

                                   Matcher m2 = pFraction.matcher(line);
                                   Matcher m3 = pSize.matcher(line);
                                   Matcher m4 = pMinSize.matcher(line);
                                   Matcher m5 = pMaxSize.matcher(line);

                                   m2.find();

                                   float fraction = Float.parseFloat(m2.group().replaceAll("FRACTION\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                   float minSize = 0f;
                                   float maxSize = 0f;
                                   if (!m4.find()) {
                                       m3.find();
                                       minSize = Float.parseFloat(m3.group().replaceAll("SIZE\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                       maxSize = minSize;
                                   } else {
                                       m5.find();
                                       minSize = Float.parseFloat(m4.group().replaceAll("MINSIZE\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                       maxSize = Float.parseFloat(m5.group().replaceAll("MAXSIZE\\s+", "").trim()); //$NON-NLS-1$ //$NON-NLS-2$
                                   }

                                   if (line.contains(" GLITTER")) { //$NON-NLS-1$
                                       Matcher m6 = pGlitter.matcher(line);
                                       m6.find();
                                       String valStr = m6.group().replaceAll("GLITTER\\s+VALUE\\s+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                       float vR = Integer.parseInt(valStr.substring(1, 3), 16) / 255f;
                                       float vG = Integer.parseInt(valStr.substring(3, 5), 16) / 255f;
                                       float vB = Integer.parseInt(valStr.substring(5, 7), 16) / 255f;
                                       colour = new GColour(index, r, g, b, 99f, new GCGlitter(vR, vG, vB, fraction, minSize, maxSize));
                                   } else if (line.contains(" SPECKLE")) { //$NON-NLS-1$
                                       Matcher m6 = pSpeckle.matcher(line);
                                       m6.find();
                                       String valStr = m6.group().replaceAll("SPECKLE\\s+VALUE\\s+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                       float vR = Integer.parseInt(valStr.substring(1, 3), 16) / 255f;
                                       float vG = Integer.parseInt(valStr.substring(3, 5), 16) / 255f;
                                       float vB = Integer.parseInt(valStr.substring(5, 7), 16) / 255f;
                                       colour = new GColour(index, r, g, b, .99f, new GCSpeckle(vR, vG, vB, fraction, minSize, maxSize));
                                   }
                               } catch (Exception e) {
                                   NLogger.error(View.class, "Line: " + line); //$NON-NLS-1$
                                   NLogger.error(View.class, e);
                               }
                           }
                           colourFromIndex.put(index, colour);
                       }
                       IndexedEntry entry = new IndexedEntry(r, g, b);
                       if (index == 16) {
                           LDConfig.col16IndexedEntry = entry;
                           LDConfig.originalColour16 = colourFromIndex.get(16).createClone();
                       }
                       indexFromColour.put(entry, index);
                       edgeColourFromIndex.put(index, new GColour(index, r2, g2, b2, 1f));
                       colourNameFromIndex.put(index, dataSegments[2].replace('_', ' ' ));
                   }
               }
               return true;
           } catch (Exception e) {
               NLogger.debug(View.class, e);
           }
       }
       return false;
   }

   public static void overrideColour16() {
       float r;
       float g;
       float b;
       GColour col16 = getColour(16);
       if (LDConfig.colour16overrideR > 0f && LDConfig.colour16overrideG > 0f && LDConfig.colour16overrideB > 0f) {
           r = LDConfig.colour16overrideR;
           g = LDConfig.colour16overrideG;
           b = LDConfig.colour16overrideB;
       } else {
           r = LDConfig.originalColour16.getR();
           g = LDConfig.originalColour16.getG();
           b = LDConfig.originalColour16.getB();
       }
       col16.setR(r);
       col16.setG(g);
       col16.setB(b);
       indexFromColour.remove(LDConfig.col16IndexedEntry);
       LDConfig.col16IndexedEntry = new IndexedEntry(r + .000016f, g + .000016f, b + .000016f);
       indexFromColour.put(LDConfig.col16IndexedEntry, 16);
   }
}

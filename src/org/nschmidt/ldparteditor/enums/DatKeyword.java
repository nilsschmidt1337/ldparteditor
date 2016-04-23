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
package org.nschmidt.ldparteditor.enums;

import java.util.HashSet;

/**
 * A collection of keywords which will be highlighted in type 0 lines
 *
 * @author nils
 *
 */
public enum DatKeyword {
    INSTANCE;

    private static final HashSet<String> keywords = new HashSet<String>();

    static {
        keywords.add("!:"); //$NON-NLS-1$
        keywords.add("!CATEGORY"); //$NON-NLS-1$
        keywords.add("!CMDLINE"); //$NON-NLS-1$
        keywords.add("!COLOUR"); //$NON-NLS-1$
        keywords.add("!HELP"); //$NON-NLS-1$
        keywords.add("!HISTORY"); //$NON-NLS-1$
        keywords.add("!KEYWORDS"); //$NON-NLS-1$
        keywords.add("!LDRAW_ORG"); //$NON-NLS-1$
        keywords.add("!LICENSE"); //$NON-NLS-1$
        keywords.add("!TEXMAP"); //$NON-NLS-1$
        keywords.add("ALPHA"); //$NON-NLS-1$
        keywords.add("BFC"); //$NON-NLS-1$
        keywords.add("CCW"); //$NON-NLS-1$
        keywords.add("CERTIFY"); //$NON-NLS-1$
        keywords.add("CHROME"); //$NON-NLS-1$
        keywords.add("CLIP"); //$NON-NLS-1$
        keywords.add("CODE"); //$NON-NLS-1$
        keywords.add("CW"); //$NON-NLS-1$
        keywords.add("CYLINDRICAL"); //$NON-NLS-1$
        keywords.add("EDGE"); //$NON-NLS-1$
        keywords.add("END"); //$NON-NLS-1$
        keywords.add("FALLBACK"); //$NON-NLS-1$
        keywords.add("FRACTION "); //$NON-NLS-1$
        keywords.add("GLITTER "); //$NON-NLS-1$
        keywords.add("GLOSSMAP"); //$NON-NLS-1$
        keywords.add("INVERTNEXT"); //$NON-NLS-1$
        keywords.add("LUMINANCE"); //$NON-NLS-1$
        keywords.add("MATERIAL"); //$NON-NLS-1$
        keywords.add("MATTE_METALLIC"); //$NON-NLS-1$
        keywords.add("MAXSIZE"); //$NON-NLS-1$
        keywords.add("METAL"); //$NON-NLS-1$
        keywords.add("MINSIZE"); //$NON-NLS-1$
        keywords.add("NEXT"); //$NON-NLS-1$
        keywords.add("NOCERTIFY"); //$NON-NLS-1$
        keywords.add("NOCLIP"); //$NON-NLS-1$
        keywords.add("PEARLESCENT"); //$NON-NLS-1$
        keywords.add("PLANAR"); //$NON-NLS-1$
        keywords.add("RUBBER"); //$NON-NLS-1$
        keywords.add("SIZE"); //$NON-NLS-1$
        keywords.add("SPECKLE"); //$NON-NLS-1$
        keywords.add("SPHERICAL"); //$NON-NLS-1$
        keywords.add("START"); //$NON-NLS-1$
        keywords.add("UPDATE"); //$NON-NLS-1$
        keywords.add("VALUE"); //$NON-NLS-1$
        keywords.add("VFRACTION"); //$NON-NLS-1$
        keywords.add("ORIGINAL"); //$NON-NLS-1$
        keywords.add("STEP"); //$NON-NLS-1$

        keywords.add("!LPE"); //$NON-NLS-1$

        keywords.add("TODO"); //$NON-NLS-1$
        keywords.add("VERTEX"); //$NON-NLS-1$

        keywords.add("DISTANCE"); //$NON-NLS-1$
        keywords.add("PROTRACTOR"); //$NON-NLS-1$

        keywords.add("PNG"); //$NON-NLS-1$

        keywords.add("INLINE"); //$NON-NLS-1$
        keywords.add("INLINE_END"); //$NON-NLS-1$

        keywords.add("CSG_UNION"); //$NON-NLS-1$
        keywords.add("CSG_DIFFERENCE"); //$NON-NLS-1$
        keywords.add("CSG_INTERSECTION"); //$NON-NLS-1$
        keywords.add("CSG_TRANSFORM"); //$NON-NLS-1$

        keywords.add("CSG_MESH"); //$NON-NLS-1$
        keywords.add("CSG_CUBOID"); //$NON-NLS-1$
        keywords.add("CSG_ELLIPSOID"); //$NON-NLS-1$
        keywords.add("CSG_CYLINDER"); //$NON-NLS-1$
        keywords.add("CSG_CONE"); //$NON-NLS-1$
        keywords.add("CSG_QUAD"); //$NON-NLS-1$
        keywords.add("CSG_CIRCLE"); //$NON-NLS-1$
        keywords.add("CSG_COMPILE"); //$NON-NLS-1$

        keywords.add("CSG_QUALITY"); //$NON-NLS-1$
        keywords.add("CSG_EPSILON"); //$NON-NLS-1$
        keywords.add("CSG_EPSILON_T_JUNCTION"); //$NON-NLS-1$
    }

    /**
     * @return all valid LDraw/LPC/TEXMAP Keywords
     */
    public static HashSet<String> getKeywords() {
        return keywords;
    }
}

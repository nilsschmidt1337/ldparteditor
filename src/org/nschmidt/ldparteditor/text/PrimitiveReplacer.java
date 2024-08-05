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
package org.nschmidt.ldparteditor.text;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nschmidt.ldparteditor.dialog.primgen2.PrimGen2Dialog;
import org.nschmidt.ldparteditor.logger.NLogger;

public enum PrimitiveReplacer {
    INSTANCE;

    private static final Map<PrimitiveKey, List<String>> GENERATED_PRIMITIVES_CACHE = new HashMap<>();

    public static void clearCache() {
        GENERATED_PRIMITIVES_CACHE.clear();
    }

    public static List<String> substitutePrimitives(String shortFilename, int primitiveSubstitutionQuality) {
        final PrimitiveKey key = new PrimitiveKey(shortFilename, primitiveSubstitutionQuality);
        final List<String> cachedResult = GENERATED_PRIMITIVES_CACHE.get(key);
        if (cachedResult != null) {
            NLogger.debug(PrimitiveReplacer.class, "Cache hit for : {0}", shortFilename); //$NON-NLS-1$
            return cachedResult;
        }

        NLogger.debug(PrimitiveReplacer.class, "Checking potential primitive for substitution (quality {0}) : {1}", primitiveSubstitutionQuality, shortFilename); //$NON-NLS-1$

        final boolean isHiQuality = shortFilename.startsWith("48\\"); //$NON-NLS-1$

        if (shortFilename.startsWith("8\\") || primitiveSubstitutionQuality <= 48 && isHiQuality) { //$NON-NLS-1$
            NLogger.debug(PrimitiveReplacer.class, "Skipping primitive (low-quality or reduced hi-quality) : {0}", shortFilename); //$NON-NLS-1$
            return List.of();
        }

        if (isHiQuality) {
            shortFilename = shortFilename.substring(3);
        }

        return substitutePrimitivesParseFraction(key, shortFilename, primitiveSubstitutionQuality);
    }

    private static List<String> substitutePrimitivesParseFraction(PrimitiveKey key, String name, int quality) {
        int length = name.length();

        if (name.endsWith(".dat")) { //$NON-NLS-1$
            name = name.substring(0, length - 4);
            length -= 4;
        }

        boolean hasFraction = true;
        int upper = 0;
        int lower = 0;
        int i;
        for (i = 0; i < name.length(); i++) {
            if (!Character.isDigit(name.charAt(i))) break;
        }

        if (i > 0 && i < length && name.charAt(i) == '-') {
            try {
                upper = Integer.parseInt(name.substring(0, i));
                i++;
            } catch (NumberFormatException nfe) {
                NLogger.debug(PrimitiveReplacer.class, nfe);
                hasFraction = false;
            }
        } else {
            hasFraction = false;
        }

        int lowerStart = i;
        while (i < length) {
            if (!Character.isDigit(name.charAt(i))) break;
            i++;
        }

        if (hasFraction && i > 0 && i < length && lowerStart < i) {
            try {
                lower = Math.max(Integer.parseInt(name.substring(lowerStart, i)), 1);
            } catch (NumberFormatException nfe) {
                NLogger.debug(PrimitiveReplacer.class, nfe);
                hasFraction = false;
            }

            if (upper > lower) upper = lower;

            NLogger.debug(PrimitiveReplacer.class, "Primitive {0} uses fraction {1}/{2}.", name, upper, lower); //$NON-NLS-1$
            name = name.substring(i);
        } else {
            hasFraction = false;
        }

        if (hasFraction) {
            final PrimitiveFraction fraction = new PrimitiveFraction(upper, lower, hasFraction);
            return substitutePrimitivesWithFraction(key, name, fraction, quality);
        }

        return substitutePrimitivesTori(key, name, quality);
    }

    private static List<String> substitutePrimitivesWithFraction(PrimitiveKey key, String name, PrimitiveFraction fraction,
            int quality) {
        final int segments = quality * fraction.upper / fraction.lower;

        final List<String> simpleResult = substituteSimplePrimitivesWithFraction(name, quality, segments);
        if (!simpleResult.isEmpty()) {
            return simpleResult;
        }

        final List<String> ringResult = substituteRingPrimitivesWithFraction(name, quality, segments);
        if (!ringResult.isEmpty()) {
            return ringResult;
        }

        // TODO Needs implementation!
        return List.of();
    }

    public static List<String> substituteRingPrimitivesWithFraction(String name, int quality, final int segments) {
        if (name.startsWith("ring") && name.length() > 4) { //$NON-NLS-1$
            try {
                final int size = Integer.parseInt(name.substring(4));
                return buildPrimitive(PrimGen2Dialog.RING, quality, segments, size);
            } catch (NumberFormatException nfe) {
                NLogger.debug(PrimitiveReplacer.class, nfe);
            }
        }


        if (name.startsWith("rin") && name.length() > 3) { //$NON-NLS-1$
            try {
                final int size = Integer.parseInt(name.substring(3));
                return buildPrimitive(PrimGen2Dialog.RING, quality, segments, size);
            } catch (NumberFormatException nfe) {
                NLogger.debug(PrimitiveReplacer.class, nfe);
            }
        }

        if (name.startsWith("ri") && name.length() > 2) { //$NON-NLS-1$
            try {
                final int size = Integer.parseInt(name.substring(2));
                return buildPrimitive(PrimGen2Dialog.RING, quality, segments, size);
            } catch (NumberFormatException nfe) {
                NLogger.debug(PrimitiveReplacer.class, nfe);
            }
        }

        return List.of();
    }

    public static List<String> substituteSimplePrimitivesWithFraction(String name, int quality, final int segments) {
        // Substitute edges
        if ("edge".equals(name)) { //$NON-NLS-1$
            return buildPrimitive(PrimGen2Dialog.CIRCLE, quality, segments);
        }

        // Substitute discs
        if ("disc".equals(name)) { //$NON-NLS-1$
            return buildPrimitive(PrimGen2Dialog.DISC, quality, segments);
        }

        // Substitute n-discs
        if ("ndis".equals(name)) { //$NON-NLS-1$
            return buildPrimitive(PrimGen2Dialog.DISC_NEGATIVE, quality, segments);
        }

        // Substitute truncated n-discs
        if ("tndis".equals(name)) { //$NON-NLS-1$
            return buildPrimitive(PrimGen2Dialog.DISC_NEGATIVE_TRUNCATED, quality, segments);
        }

        // Substitute cylinders
        if ("cyli".equals(name)) { //$NON-NLS-1$
            return buildPrimitive(PrimGen2Dialog.CYLINDER, quality, segments);
        }

        return List.of();
    }

    private static List<String> substitutePrimitivesTori(PrimitiveKey key, String name, int quality) {

        // TODO Needs implementation!

        if (name.startsWith("t")) { //$NON-NLS-1$

        }

        return List.of();
    }

    private static List<String> buildPrimitive(int type, int divisions, int segments) {
        return buildPrimitive(type, divisions, segments, 0);
    }

    private static List<String> buildPrimitive(int type, int divisions, int segments, int size) {
        final String source = PrimGen2Dialog.buildPrimitiveSource(type, divisions, segments, 0, 0, 0, 1, size, true, 0, "Primitive Substitution", "LDPartEditor"); //$NON-NLS-1$ //$NON-NLS-2$
        return Arrays.asList(source.split("\n")); //$NON-NLS-1$
    }


    private record PrimitiveKey(String shortFilename, int primitiveSubstitutionQuality) {}
    private record PrimitiveFraction(int upper, int lower, boolean hasFraction) {}
}

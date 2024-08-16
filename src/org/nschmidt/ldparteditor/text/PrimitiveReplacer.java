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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nschmidt.ldparteditor.dialog.primgen2.PrimGen2Dialog;
import org.nschmidt.ldparteditor.logger.NLogger;

public enum PrimitiveReplacer {
    INSTANCE;

    private static final String PREFIX_8 = "8" + File.separator; //$NON-NLS-1$
    private static final String PREFIX_48 = "48" + File.separator; //$NON-NLS-1$
    private static final Map<PrimitiveKey, List<String>> GENERATED_PRIMITIVES_CACHE = new HashMap<>();

    public static void clearCache() {
        GENERATED_PRIMITIVES_CACHE.clear();
    }

    public static List<String> substitutePrimitives(String shortFilename, List<String> lines, int primitiveSubstitutionQuality) {
        final PrimitiveKey key = new PrimitiveKey(shortFilename, primitiveSubstitutionQuality);
        final List<String> cachedResult = GENERATED_PRIMITIVES_CACHE.get(key);
        if (cachedResult != null) {
            NLogger.debug(PrimitiveReplacer.class, "Cache hit for : {0}", shortFilename); //$NON-NLS-1$
            return cachedResult;
        }

        NLogger.debug(PrimitiveReplacer.class, "Checking potential primitive for substitution (quality {0}) : {1}", primitiveSubstitutionQuality, shortFilename); //$NON-NLS-1$

        final boolean isHiQuality = shortFilename.startsWith(PREFIX_48);

        if (shortFilename.startsWith(PREFIX_8) || primitiveSubstitutionQuality <= 48 && isHiQuality) {
            NLogger.debug(PrimitiveReplacer.class, "Skipping primitive (low-quality or reduced hi-quality) : {0}", shortFilename); //$NON-NLS-1$
            return List.of();
        }

        if (isHiQuality) {
            shortFilename = shortFilename.substring(3);
        }

        final List<String> value = substitutePrimitivesParseFraction(shortFilename, lines, primitiveSubstitutionQuality);
        GENERATED_PRIMITIVES_CACHE.put(key, value);
        return value;
    }

    private static List<String> substitutePrimitivesParseFraction(String name, List<String> lines, int quality) {
        int length = name.length();

        if (name.endsWith(".dat")) { //$NON-NLS-1$
            name = name.substring(0, length - 4);
            length -= 4;
        }

        boolean hasFraction = true;
        int upper = 0;
        int lower = 0;
        int i = 0;
        while (i < length) {
            if (!Character.isDigit(name.charAt(i))) break;
            i++;
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
            int gcd = PrimGen2Dialog.gcd(lower, upper);
            upper = upper / gcd;
            lower = lower / gcd;

            final PrimitiveFraction fraction = new PrimitiveFraction(upper, lower, hasFraction);
            return substitutePrimitivesWithFraction(name, fraction, quality);
        }

        return substitutePrimitivesTori(name, lines, quality);
    }

    private static List<String> substitutePrimitivesWithFraction(String name, PrimitiveFraction fraction,
            int quality) {
        int segments = quality;

        for (int i = 1; i < 100; i++) {
            if (fraction.lower * (i + 1) > quality) {
                segments = fraction.upper * i;
                quality = fraction.lower * i;
                break;
            }
        }

        final List<String> sphereResult = substituteEightSphere(name, quality, segments, fraction);
        if (!sphereResult.isEmpty()) {
            return sphereResult;
        }

        final List<String> simpleResult = substituteSimplePrimitivesWithFraction(name, quality, segments);
        if (!simpleResult.isEmpty()) {
            return simpleResult;
        }

        final List<String> ringResult = substituteRingPrimitivesWithFraction(name, quality, segments);
        if (!ringResult.isEmpty()) {
            return ringResult;
        }

        final List<String> coneResult = substituteConePrimitivesWithFraction(name, quality, segments);
        if (!coneResult.isEmpty()) {
            return coneResult;
        }

        return List.of();
    }

    public static List<String> substituteEightSphere(String name, int quality, int segments,
            PrimitiveFraction fraction) {
        if ("sphe".equals(name) && fraction.upper == 1 && fraction.lower == 8) { //$NON-NLS-1$
            return buildPrimitive(PrimGen2Dialog.EIGHT_SPHERE, quality, segments);
        }

        return List.of();
    }

    private static List<String> substituteConePrimitivesWithFraction(String name, int quality, final int segments) {
        if (name.startsWith("con") && name.length() > 3) { //$NON-NLS-1$
            try {
                final int size = Integer.parseInt(name.substring(3));
                return buildPrimitive(PrimGen2Dialog.CONE, quality, segments, size);
            } catch (NumberFormatException nfe) {
                NLogger.debug(PrimitiveReplacer.class, nfe);
            }
        }

        return List.of();
    }

    private static List<String> substituteRingPrimitivesWithFraction(String name, int quality, final int segments) {
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

    private static List<String> substituteSimplePrimitivesWithFraction(String name, int quality, final int segments) {
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

        if ("cyli2".equals(name)) { //$NON-NLS-1$
            return buildPrimitive(PrimGen2Dialog.CYLINDER_WITHOUT_CONDLINES, quality, segments);
        }

        // Substitute cylinders sloped (truncated by an angled plane)
        if ("cyls".equals(name)) { //$NON-NLS-1$
            return buildPrimitive(PrimGen2Dialog.CYLINDER_SLOPED, quality, segments);
        }

        if ("cyls2".equals(name)) { //$NON-NLS-1$
            return buildPrimitive(PrimGen2Dialog.CYLINDER_SLOPED_CONVEX, quality, segments);
        }

        if ("cylh".equals(name)) { //$NON-NLS-1$
            return buildPrimitive(PrimGen2Dialog.CYLINDER_SLOPED_HELICAL, quality, segments);
        }

        if ("edgh".equals(name)) { //$NON-NLS-1$
            return buildPrimitive(PrimGen2Dialog.CYLINDER_SLOPED_HELICAL_EDGE, quality, segments);
        }

        // Substitute circular disc segments
        if ("chrd".equals(name)) { //$NON-NLS-1$
            return buildPrimitive(PrimGen2Dialog.CHORD, quality, segments);
        }

        // Substitute tangential ring segments
        if ("tang".equals(name)) { //$NON-NLS-1$
            return buildPrimitive(PrimGen2Dialog.TANGENTIAL_RING_SEGMENT, quality, segments);
        }

        return List.of();
    }

    private static List<String> substitutePrimitivesTori(String name, List<String> lines, int quality) {
        if (name.startsWith("t") || name.startsWith("r")) { //$NON-NLS-1$ //$NON-NLS-2$
            String toriName = name;
            boolean mixedMode = false;
            if (toriName.startsWith("tm") || name.startsWith("rm")) { //$NON-NLS-1$ //$NON-NLS-2$
                toriName = toriName.substring(0, 1) + toriName.substring(2);
                mixedMode = true;
            }

            final int length = toriName.length();
            if (length == 8 && validToriName(toriName)) {
                try {
                    double major = 1;
                    double minor = 1;
                    int realDivisions = quality;
                    int realSegments = quality;
                    for (String line : lines) {
                        if (line.startsWith("0 // Major Radius: ")) { //$NON-NLS-1$
                            major = Double.parseDouble(line.substring(19).trim());
                        } else if (line.startsWith("0 // Tube(Minor) Radius: ")) { //$NON-NLS-1$
                            minor = Double.parseDouble(line.substring(25).trim());
                        } else if (line.startsWith("0 // Segments(Sweep): ") && line.indexOf('/', 4) != -1 && line.indexOf('=') != -1) { //$NON-NLS-1$
                            final String[] sweep = line.substring(22, line.indexOf('=')).trim().split("/"); //$NON-NLS-1$
                            if (sweep.length == 2) {
                                realSegments = Math.max(Integer.parseInt(sweep[0]), 1);
                                realDivisions = Math.max(Integer.parseInt(sweep[1]), 1);

                                int gcd = PrimGen2Dialog.gcd(realDivisions, realSegments);
                                realSegments = realSegments / gcd;
                                realDivisions = realDivisions / gcd;

                                for (int i = 1; i < 100; i++) {
                                    if (realDivisions * (i + 1) > quality) {
                                        realDivisions *= i;
                                        realSegments *= i;
                                        break;
                                    }
                                }
                            }
                        }

                        if (line.startsWith("4") || line.startsWith("5")) break; //$NON-NLS-1$ //$NON-NLS-2$
                    }

                    if (Math.abs(major) < 0.0001) major = 1.0;
                    if (Math.abs(minor) < 0.0001) minor = 1.0;

                    int toriType = 0;
                    switch (toriName.substring(3, 4)) {
                        case "i": //$NON-NLS-1$
                            toriType = 0;
                            break;
                        case "o": //$NON-NLS-1$
                            toriType = 1;
                            break;
                        case "q": //$NON-NLS-1$
                            toriType = 2;
                            break;
                        default:
                            return List.of();
                    }

                    NLogger.debug(PrimitiveReplacer.class, "Primitive {0} is a torus (major: {1}, minor: {2}, fraction: {3}/{4}, mixed: {5}).", name, major, minor, realSegments, realDivisions, mixedMode); //$NON-NLS-1$

                    final String source = PrimGen2Dialog.buildPrimitiveSource(PrimGen2Dialog.TORUS, realDivisions, realSegments, quality, major, minor, 1, 0, true, toriType, "Primitive Substitution", "LDPartEditor"); //$NON-NLS-1$ //$NON-NLS-2$
                    return Arrays.asList(source.split("\n")); //$NON-NLS-1$
                } catch (NumberFormatException nfe) {
                    NLogger.debug(PrimitiveReplacer.class, nfe);
                }
            }
        }

        return List.of();
    }

    private static boolean validToriName(String name) {
        if (!"unit".equalsIgnoreCase(name.substring(4))) {//$NON-NLS-1$
            for (int i = 4; i < 8; i++) {
                if (!Character.isDigit(name.charAt(i))) return false;
            }
        }

        return Character.isDigit(name.charAt(1)) && Character.isDigit(name.charAt(2));
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

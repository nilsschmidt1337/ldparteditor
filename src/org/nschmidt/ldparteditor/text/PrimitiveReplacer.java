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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        return substitutePrimitives(shortFilename, primitiveSubstitutionQuality, isHiQuality);
    }

    private static List<String> substitutePrimitives(String shortFilename, int primitiveSubstitutionQuality, boolean isHiQuality) {
        // TODO Needs implementation!
        return List.of();
    }

    private record PrimitiveKey(String shortFilename, int primitiveSubstitutionQuality) {}
}

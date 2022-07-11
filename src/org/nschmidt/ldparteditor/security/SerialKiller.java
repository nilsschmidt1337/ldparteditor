/* MIT - License

Copyright (c) 2012 - this year, Nils Schmidt

This is a simplified look-ahead Java deserialization class based on Luca Carettoni's SerialKiller.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.nschmidt.ldparteditor.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nschmidt.ldparteditor.logger.NLogger;

public class SerialKiller extends ObjectInputStream {

    private static final boolean PROFILING = false;
    private static final List<Pattern> WHITELIST = Arrays.asList(
            "org.nschmidt.ldparteditor.workbench.Editor3DWindowState", //$NON-NLS-1$
            "\\[I", //$NON-NLS-1$
            "\\[Lorg.lwjgl.util.vector.Matrix4f;", //$NON-NLS-1$
            "org.lwjgl.util.vector.Matrix4f", //$NON-NLS-1$
            "org.lwjgl.util.vector.Matrix", //$NON-NLS-1$
            "java.util.ArrayList", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.workbench.Composite3DState", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.enumtype.Perspective", //$NON-NLS-1$
            "java.lang.Enum", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.workbench.WindowState", //$NON-NLS-1$
            "org.eclipse.swt.graphics.Rectangle", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.workbench.EditorTextWindowState", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.workbench.UserSettingState", //$NON-NLS-1$
            "\\[F", //$NON-NLS-1$
            "java.math.BigDecimal", //$NON-NLS-1$
            "java.lang.Number", //$NON-NLS-1$
            "java.math.BigInteger", //$NON-NLS-1$
            "\\[B", //$NON-NLS-1$
            "\\[Ljava.lang.String;", //$NON-NLS-1$
            "\\[Lorg.nschmidt.ldparteditor.enumtype.Task;", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.enumtype.Task", //$NON-NLS-1$
            "\\[Lorg.nschmidt.ldparteditor.enumtype.TextTask;", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.enumtype.TextTask", //$NON-NLS-1$
            "java.util.Locale", //$NON-NLS-1$
            "java.util.concurrent.atomic.AtomicBoolean", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.data.GColour", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.workbench.PrimitiveCache", //$NON-NLS-1$
            "java.util.HashMap", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.data.PGData", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.data.PGData2", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.data.PGData3", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.data.PGData4", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.data.PGData5", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.data.PGDataBFC", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.data.BFC", //$NON-NLS-1$
            "org.nschmidt.ldparteditor.data.PGTimestamp") //$NON-NLS-1$
            .stream()
            .map(Pattern::compile)
            .toList();

    public SerialKiller(final InputStream inputStream) throws IOException {
        super(inputStream);
    }

    @Override
    protected Class<?> resolveClass(final ObjectStreamClass serialInput) throws IOException, ClassNotFoundException {
        boolean safeClass = false;

        for (Pattern whitePattern : WHITELIST) {
            Matcher whiteMatcher = whitePattern.matcher(serialInput.getName());

            if (whiteMatcher.find()) {
                safeClass = true;

                if (PROFILING) {
                    NLogger.debug(SerialKiller.class, String.format("Whitelist match: '%s'", serialInput.getName())); //$NON-NLS-1$
                }

                break;
            }
        }

        if (!safeClass) {
            if (PROFILING) {
                NLogger.error(SerialKiller.class, String.format("Class would be blocked by whitelist. No match found for '%s'", serialInput.getName())); //$NON-NLS-1$
            } else {
                throw new InvalidClassException(serialInput.getName(), "Class blocked from deserialization.)"); //$NON-NLS-1$
            }
        }

        return super.resolveClass(serialInput);
    }
}
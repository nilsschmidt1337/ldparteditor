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

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.nschmidt.ldparteditor.logger.NLogger;

import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;

/**
 * Evaluates simple expressions 
 */
public enum Evaluator {
    INSTANCE;
    
    private static JShell js = JShell.create();
    private static final StringBuilder sb = new StringBuilder();
    private static final Pattern dotLetter = Pattern.compile("\\.\\D", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

    static String eval(Map<String, String> constants, String rawExpr) {
        if (constants.isEmpty()) {
            js.close();
            js = JShell.create();
            staticImportMathFunctions("PI", "toDegrees", "toRadians", "sin", "cos", "tan", "round", "abs", "signum", "asin", "acos", "atan", "atan2", "log", "log", "sqrt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$)
        }
        
        // Don't allow other method calls
        rawExpr = dotLetter.matcher(rawExpr).replaceAll("~$0"); //$NON-NLS-1$
        rawExpr = rawExpr.replace("deg(", "toDegrees("); //$NON-NLS-1$ //$NON-NLS-2$
        rawExpr = rawExpr.replace("rad(", "toRadians("); //$NON-NLS-1$ //$NON-NLS-2$
        rawExpr = replaceInvalidCharSequences(rawExpr, "{", "}", "while(", "for("); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        final String expr = rawExpr;
        
        try {
            if (js == null) return expr;
            
            for (Map.Entry<String, String> entry : constants.entrySet()) {
                final String name = entry.getKey();
                final String value = entry.getValue();
                addVariable(name, value);
            }
            
            // Don't allow multiple lines of code (e.g. "int i = 0; i++;")
            final String result = js.eval(js.sourceCodeAnalysis().analyzeCompletion(expr).source())
                    .stream()
                    .map(SnippetEvent::value)
                    .map(s -> s.replace("\"", "")) //$NON-NLS-1$ //$NON-NLS-2$
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(expr);
            NLogger.debug(Evaluator.class, expr);
            NLogger.debug(Evaluator.class, result);
            return result;
        } catch (Exception e) {
            NLogger.debug(Evaluator.class, expr);
            NLogger.debug(Evaluator.class, e);
            return "NaN"; //$NON-NLS-1$
        }
    }

    static void staticImportMathFunctions(String... operators) {
        for (String op : operators) {
            js.eval("import static java.lang.Math."+ op + ";"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    static String replaceInvalidCharSequences(String rawExpr, String... invalid) {
        for (String str : invalid) {
            rawExpr = rawExpr.replace(str, ""); //$NON-NLS-1$
        }
        return rawExpr;
    }

    private static void addVariable(final String name, final String value) {
        if (name.charAt(0) == '-') return;
        
        sb.setLength(0);
        try {
            double d = Double.parseDouble(value);
            if (d != Double.NaN) {
                sb.append("double "); //$NON-NLS-1$
                sb.append(name);
                sb.append("="); //$NON-NLS-1$
                sb.append(value);
            } else {
                sb.append("java.lang.String "); //$NON-NLS-1$
                sb.append(name);
                sb.append("=\""); //$NON-NLS-1$
                sb.append(value);
                sb.append("\""); //$NON-NLS-1$
            }
        } catch (NumberFormatException nfe) {
            sb.append("java.lang.String "); //$NON-NLS-1$
            sb.append(name);
            sb.append("=\""); //$NON-NLS-1$
            sb.append(value);
            sb.append("\""); //$NON-NLS-1$
        }
        
        sb.append(";"); //$NON-NLS-1$
        
        js.eval(js.sourceCodeAnalysis().analyzeCompletion(sb.toString()).source());
    }
}

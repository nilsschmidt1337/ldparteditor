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
package org.nschmidt.ldparteditor.main;

import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.splash.SplashScreen;

/**
 * The main class, which launches the Splash Screen
 *
 * @author nils
 *
 */
// Nothing more to do here..
public class LDPartEditor {

    /**
     * Program entry point
     *
     * @param args
     *            startup arguments
     */
    public static void main(String[] args) {
        NLogger.setDEBUG(args.length == 1 && "DEBUG".equals(args[0])); //$NON-NLS-1$
        NLogger.init();
        new SplashScreen().run();
        NLogger.flushErrorStream();
    }

}

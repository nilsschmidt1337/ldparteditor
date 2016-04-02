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
package org.nschmidt.ldparteditor.helpers;

import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * Provides simple stop-watch functions.
 *
 * @author nils
 *
 */
public enum StopWatch {
    INSTANCE;

    private static long duration;
    private static long start;
    private static boolean running;

    /**
     * Starts the watch
     */
    public static void start() {
        if (!running) {
            start = System.currentTimeMillis();
            running = true;
        }
    }

    /**
     * Resets and starts the watch
     */
    public static void restart() {
        reset();
        start();
    }

    /**
     * Stops the watch
     */
    public static void stop() {
        if (running) {
            duration += System.currentTimeMillis() - start;
            running = false;
        }
    }

    /**
     * Resets the watch
     */
    public static void reset() {
        stop();
        duration = 0;
    }

    /**
     * @return The duration in milliseconds
     */
    public static long getDuration() {
        if (running) {
            return duration + System.currentTimeMillis() - start;
        } else {
            return duration;
        }
    }

    public static void printDuration() {
        stop();
        NLogger.debug(StopWatch.class, "Duration: " + getDuration()); //$NON-NLS-1$
        start();
    }
}

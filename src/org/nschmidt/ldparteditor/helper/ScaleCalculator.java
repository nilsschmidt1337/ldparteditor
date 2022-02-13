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
package org.nschmidt.ldparteditor.helper;

import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public enum ScaleCalculator {
    INSTANCE;
    
    @SuppressWarnings("java:S3011")
    public static double getViewportScaleFactor(final Composite cmp) {
        // When it is not possible to calculate the scale factor then
        // use the old value to allow a manual override.
        final double oldFactor = WorkbenchManager.getUserSettingState().getViewportScaleFactor();
        
        // I know that accessing a package private method "computeSizeInPixels" is against
        // the best practices (only use a public API), but I need this hack.
        Method packagePrivateMethod;
        try {
            packagePrivateMethod = Composite.class.getDeclaredMethod("computeSizeInPixels", int.class, int.class, boolean.class); //$NON-NLS-1$
        } catch (NoSuchMethodException e) {
            // The method is not available on Mac OS X
            NLogger.debug(ScaleCalculator.class, e);
            return oldFactor;
        } catch (SecurityException e) {
            NLogger.error(ScaleCalculator.class, e);
            return oldFactor;
        }
        

        final Point sizeInPixels;
        try {
            packagePrivateMethod.setAccessible(true);
            sizeInPixels = (Point) packagePrivateMethod.invoke(cmp, -1, -1, false);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InaccessibleObjectException  e) {
            NLogger.error(ScaleCalculator.class, e);
            return oldFactor;
        }
        
        final Point sizeScaled = cmp.computeSize(-1,-1, false);
        
        double scaledSize = sizeScaled.x;
        double pixelSize = sizeInPixels.x;
        if (sizeScaled.x == 0) {
            if (sizeScaled.y == 0) {
                return oldFactor;
            } else {
                scaledSize = sizeScaled.y;
                pixelSize = sizeInPixels.y;
            }
        }
        
        return Math.max(pixelSize / scaledSize, 1.0);
    }
}

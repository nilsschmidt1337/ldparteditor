/* The MIT License (MIT)

Copyright (c) 2015 Kai Burjack

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. */
package org.lwjgl.opengl.swt;

import org.eclipse.swt.widgets.Composite;
import org.lwjgl.opengl.swt.GLData.API;

abstract class AbstractPlatformGLCanvas implements PlatformGLCanvas {

    protected static boolean atLeast32(int major, int minor) {
        return major == 3 && minor >= 2 || major > 3;
    }

    protected static boolean atLeast30(int major, int minor) {
        return major == 3 && minor >= 0 || major > 3;
    }

    protected static boolean validVersionGL(int major, int minor) {
        return (major == 0 && minor == 0) || // unspecified gets highest supported version on Nvidia
               (major >= 1 && minor >= 0) &&
               (major != 1 || minor <= 5) &&
               (major != 2 || minor <= 1) &&
               (major != 3 || minor <= 3) &&
               (major != 4 || minor <= 5);
    }

    protected static boolean validVersionGLES(int major, int minor) {
        return (major == 0 && minor == 0) || // unspecified gets 1.1 on Nvidia
               (major >= 1 && minor >= 0) &&
               (major != 1 || minor <= 1) &&
               (major != 2 || minor <= 0);
    }

    /**
     * Validate the given {@link GLData} and throw an exception on validation error.
     * 
     * @param attribs
     *            the {@link GLData} to validate
     */
    public static void validateAttributes(GLData attribs) {
        if (attribs.alphaSize < 0) {
            throw new IllegalArgumentException("Alpha bits cannot be less than 0"); //$NON-NLS-1$
        }
        if (attribs.redSize < 0) {
            throw new IllegalArgumentException("Red bits cannot be less than 0"); //$NON-NLS-1$
        }
        if (attribs.greenSize < 0) {
            throw new IllegalArgumentException("Green bits cannot be less than 0"); //$NON-NLS-1$
        }
        if (attribs.blueSize < 0) {
            throw new IllegalArgumentException("Blue bits cannot be less than 0"); //$NON-NLS-1$
        }
        if (attribs.stencilSize < 0) {
            throw new IllegalArgumentException("Stencil bits cannot be less than 0"); //$NON-NLS-1$
        }
        if (attribs.depthSize < 0) {
            throw new IllegalArgumentException("Depth bits cannot be less than 0"); //$NON-NLS-1$
        }
        if (attribs.forwardCompatible && !atLeast30(attribs.majorVersion, attribs.minorVersion)) {
            throw new IllegalArgumentException("Forward-compatibility is only defined for OpenGL version 3.0 and above"); //$NON-NLS-1$
        }
        if (attribs.samples < 0) {
            throw new IllegalArgumentException("Invalid samples count"); //$NON-NLS-1$
        }
        if (attribs.profile != null && !atLeast32(attribs.majorVersion, attribs.minorVersion)) {
            throw new IllegalArgumentException("Context profiles are only defined for OpenGL version 3.2 and above"); //$NON-NLS-1$
        }
        if (attribs.api == null) {
            throw new IllegalArgumentException("Unspecified client API"); //$NON-NLS-1$
        }
        if (attribs.api == API.GL && !validVersionGL(attribs.majorVersion, attribs.minorVersion)) {
            throw new IllegalArgumentException("Invalid OpenGL version"); //$NON-NLS-1$
        }
        if (attribs.api == API.GLES && !validVersionGLES(attribs.majorVersion, attribs.minorVersion)) {
            throw new IllegalArgumentException("Invalid OpenGL ES version"); //$NON-NLS-1$
        }
        if (!attribs.doubleBuffer && attribs.swapInterval != null) {
            throw new IllegalArgumentException("Swap interval set but not using double buffering"); //$NON-NLS-1$
        }
        if (attribs.colorSamplesNV < 0) {
            throw new IllegalArgumentException("Invalid color samples count"); //$NON-NLS-1$
        }
        if (attribs.colorSamplesNV > attribs.samples) {
            throw new IllegalArgumentException("Color samples greater than number of (coverage) samples"); //$NON-NLS-1$
        }
        if (attribs.swapGroupNV < 0) {
            throw new IllegalArgumentException("Invalid swap group"); //$NON-NLS-1$
        }
        if (attribs.swapBarrierNV < 0) {
            throw new IllegalArgumentException("Invalid swap barrier"); //$NON-NLS-1$
        }
        if ((attribs.swapGroupNV > 0 || attribs.swapBarrierNV > 0) && !attribs.doubleBuffer) {
            throw new IllegalArgumentException("Swap group or barrier requested but not using double buffering"); //$NON-NLS-1$
        }
        if (attribs.swapBarrierNV > 0 && attribs.swapGroupNV == 0) {
            throw new IllegalArgumentException("Swap barrier requested but no valid swap group set"); //$NON-NLS-1$
        }
        if (attribs.loseContextOnReset && !attribs.robustness) {
            throw new IllegalArgumentException("Lose context notification requested but not using robustness"); //$NON-NLS-1$
        }
        if (attribs.contextResetIsolation && !attribs.robustness) {
            throw new IllegalArgumentException("Context reset isolation requested but not using robustness"); //$NON-NLS-1$
        }
    }

	public int checkStyle(Composite parent, int style) {
		return style;
	}

	public void resetStyle(Composite parent) {}
}

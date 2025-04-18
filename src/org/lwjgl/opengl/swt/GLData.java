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

/**
 * Drop-in replacement for SWT's {@link org.eclipse.swt.opengl.GLData} class.
 */
public class GLData {

    /*
     * The following fields are taken from SWT's original GLData
     */

    /**
     * Whether to use double-buffering. It defaults to <code>true</code>.
     */
    public boolean doubleBuffer = true;
    /**
     * Whether to use different LEFT and RIGHT backbuffers for stereo rendering. It defaults to <code>false</code>.
     */
    public boolean stereo;
    /**
     * The number of bits for the red color channel. It defaults to 8.
     */
    public int redSize = 8;
    /**
     * The number of bits for the green color channel. It defaults to 8.
     */
    public int greenSize = 8;
    /**
     * The number of bits for the blue color channel. It defaults to 8.
     */
    public int blueSize = 8;
    /**
     * The number of bits for the alpha color channel. It defaults to 8.
     */
    public int alphaSize = 8;
    /**
     * The number of bits for the depth channel. It defaults to 24.
     */
    public int depthSize = 24;
    /**
     * The number of bits for the stencil channel. It defaults to 0.
     */
    public int stencilSize;
    /**
     * The number of bits for the red accumulator color channel. It defaults to 0.
     */
    public int accumRedSize;
    /**
     * The number of bits for the green accumulator color channel. It defaults to 0.
     */
    public int accumGreenSize;
    /**
     * The number of bits for the blue accumulator color channel. It defaults to 0.
     */
    public int accumBlueSize;
    /**
     * The number of bits for the alpha accumulator color channel. It defaults to 0.
     */
    public int accumAlphaSize;
    /**
     * This is ignored. It will implicitly be 1 if {@link #samples} is set to a value greater than or equal to 1.
     */
    public int sampleBuffers;
    /**
     * The number of (coverage) samples for multisampling. Multisampling will only be requested for a value greater than or equal to 1.
     */
    public int samples;
    /**
     * The {@link GLCanvas} whose context objects should be shared with the context created using <code>this</code> GLData.
     */
    public GLCanvas shareContext;

    /*
     * New fields not in SWT's GLData
     */

    public static enum Profile {
        CORE, COMPATIBILITY;
    }

    public static enum API {
        GL, GLES;
    }

    public static enum ReleaseBehavior {
        NONE, FLUSH;
    }

    /**
     * The major GL context version to use. It defaults to 0 for "not specified".
     */
    public int majorVersion;
    /**
     * The minor GL context version to use. If {@link #majorVersion} is 0 this field is unused.
     */
    public int minorVersion;
    /**
     * Whether a forward-compatible context should be created. This has only an effect when ({@link #majorVersion}.{@link #minorVersion}) is at least 3.2.
     */
    public boolean forwardCompatible;
    /**
     * The profile to use. This is only valid when ({@link #majorVersion}.{@link #minorVersion}) is at least 3.0.
     */
    public Profile profile;
    /**
     * The client API to use. It defaults to {@link API#GL OpenGL for Desktop}.
     */
    public API api = API.GL;
    /**
     * Whether a debug context should be requested.
     */
    public boolean debug;
    /**
     * Set the swap interval. It defaults to <code>null</code> for "not specified".
     */
    public Integer swapInterval;
    /**
     * Whether to use sRGB color space.
     */
    public boolean sRGB;
    /**
     * Whether to use a floating point pixel format.
     */
    public boolean pixelFormatFloat;
    /**
     * Specify the behavior on context switch. Defaults to <code>null</code> for "not specified".
     */
    public ReleaseBehavior contextReleaseBehavior;
    /**
     * The number of color samples per pixel. This is only valid when {@link #samples} is at least 1.
     */
    public int colorSamplesNV;
    /**
     * The swap group index. Use this to synchronize buffer swaps across multiple windows on the same system.
     */
    public int swapGroupNV;
    /**
     * The swap barrier index. Use this to synchronize buffer swaps across multiple systems. This requires a Nvidia G-Sync card.
     */
    public int swapBarrierNV;
    /**
     * Whether robust buffer access should be used.
     */
    public boolean robustness;
    /**
     * When {@link #robustness} is <code>true</code> then this specifies whether a GL_LOSE_CONTEXT_ON_RESET_ARB reset notification is sent, as described by GL_ARB_robustness.
     */
    public boolean loseContextOnReset;
    /**
     * When {@link #robustness} is <code>true</code> and {@link #loseContextOnReset} is <code>true</code> then this specifies whether a graphics reset only affects
     * the current application and no other application in the system.
     */
    public boolean contextResetIsolation;
    /**
     * Whether to use a "no error" context, as described in
     * <a href="https://www.khronos.org/registry/OpenGL/extensions/ARB/ARB_create_context_no_error.txt">ARB_create_context_no_error</a>
     * and <a href="https://www.khronos.org/registry/OpenGL/extensions/KHR/KHR_no_error.txt">KHR_no_error</a>.
     * The default is <code>false</code>.
     */
    public boolean noErrorContext;

}

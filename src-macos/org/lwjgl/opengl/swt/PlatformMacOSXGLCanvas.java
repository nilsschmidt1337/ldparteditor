package org.lwjgl.opengl.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.cocoa.NSNotificationCenter;
import org.eclipse.swt.internal.cocoa.NSOpenGLContext;
import org.eclipse.swt.internal.cocoa.NSOpenGLPixelFormat;
import org.eclipse.swt.internal.cocoa.NSView;
import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.widgets.Listener;
import org.lwjgl.opengl.swt.GLData.Profile;

/**
 * OSX-specific implementation of methods for GLCanvas.
 * 
 * @author Thomas Ibanez
 */
class PlatformMacOSXGLCanvas extends AbstractPlatformGLCanvas {

	private NSOpenGLContext context;
	private NSOpenGLPixelFormat pixelFormat;

	private static final int MAX_ATTRIB = 32;
	private static final String GLCONTEXT_KEY = "org.eclipse.swt.internal.cocoa.glcontext";

	private NSView view;

	static final int NSOpenGLPFAOpenGLProfile = 99;
	static final int NSOpenGLProfileVersion3_2Core = 0x3200;
	static final int NSOpenGLProfileVersionLegacy = 0x1000;
	static final int NSOpenGLProfileVersion4_1Core = 0x4100;

	@Override
	/*
	 * IMPORTANT: NSOpenGL/CoreOpenGL only supports specifying the total number
	 * of bits in the size of the color component -> effective.redSize,
	 * effective.blueSize and effective.greenSize won't be set !
	 * 
	 * IMPORTANT: NSOpenGL/CoreOpenGL only supports specifying the total number
	 * of bits in the size of the color accumulator component. ->
	 * effective.accumRedSize, effective.accumBlueSize, effective.accumGreenSize
	 * and effective.accumAlphaSize won't be set !
	 */
	public long create(GLCanvas canvas, GLData data, GLData effective) {

		if (data == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		this.view = canvas.view;

		int attrib[] = new int[MAX_ATTRIB];
		int pos = 0;

		if (data.doubleBuffer)
			attrib[pos++] = OS.NSOpenGLPFADoubleBuffer;

		if (data.stereo)
			attrib[pos++] = OS.NSOpenGLPFAStereo;

		/*
		 * Feature in Cocoa: NSOpenGL/CoreOpenGL only supports specifying the
		 * total number of bits in the size of the color component. If
		 * specified, the color size is the sum of the red, green and blue
		 * values in the GLData.
		 */
		if ((data.redSize + data.blueSize + data.greenSize) > 0) {
			attrib[pos++] = OS.NSOpenGLPFAColorSize;
			attrib[pos++] = data.redSize + data.greenSize + data.blueSize;
		}

		if (data.alphaSize > 0) {
			attrib[pos++] = OS.NSOpenGLPFAAlphaSize;
			attrib[pos++] = data.alphaSize;
		}

		if (data.depthSize > 0) {
			attrib[pos++] = OS.NSOpenGLPFADepthSize;
			attrib[pos++] = data.depthSize;
		}

		if (data.stencilSize > 0) {
			attrib[pos++] = OS.NSOpenGLPFAStencilSize;
			attrib[pos++] = data.stencilSize;
		}

		if (data.profile == Profile.CORE) {
			attrib[pos++] = NSOpenGLPFAOpenGLProfile;
			attrib[pos++] = NSOpenGLProfileVersion3_2Core;
		}
		if (data.profile == Profile.COMPATIBILITY) {
			attrib[pos++] = NSOpenGLPFAOpenGLProfile;
			attrib[pos++] = NSOpenGLProfileVersionLegacy;
		} else {
			if (data.majorVersion >= 4) {
				attrib[pos++] = NSOpenGLPFAOpenGLProfile;
				attrib[pos++] = NSOpenGLProfileVersion4_1Core;
			} else if (data.majorVersion >= 3) {
				attrib[pos++] = NSOpenGLPFAOpenGLProfile;
				attrib[pos++] = NSOpenGLProfileVersion3_2Core;
			} else {
				attrib[pos++] = NSOpenGLPFAOpenGLProfile;
				attrib[pos++] = NSOpenGLProfileVersionLegacy;
			}
		}

		/*
		 * Feature in Cocoa: NSOpenGL/CoreOpenGL only supports specifying the
		 * total number of bits in the size of the color accumulator component.
		 * If specified, the color size is the sum of the red, green, blue and
		 * alpha accum values in the GLData.
		 */
		if ((data.accumRedSize + data.accumBlueSize + data.accumGreenSize) > 0) {
			attrib[pos++] = OS.NSOpenGLPFAAccumSize;
			attrib[pos++] = data.accumRedSize + data.accumGreenSize + data.accumBlueSize + data.accumAlphaSize;
		}

		if (data.sampleBuffers > 0) {
			attrib[pos++] = OS.NSOpenGLPFASampleBuffers;
			attrib[pos++] = data.sampleBuffers;
		}

		if (data.samples > 0) {
			attrib[pos++] = OS.NSOpenGLPFASamples;
			attrib[pos++] = data.samples;
		}

		attrib[pos++] = 0;

		pixelFormat = (NSOpenGLPixelFormat) new NSOpenGLPixelFormat().alloc();

		if (pixelFormat == null) {
			canvas.dispose();
			SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
		}
		pixelFormat.initWithAttributes(attrib);

		NSOpenGLContext ctx = data.shareContext != null ? new NSOpenGLContext(data.shareContext.context) : null;
		context = (NSOpenGLContext) new NSOpenGLContext().alloc();
		if (context == null) {
			canvas.dispose();
			SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
		}
		context = context.initWithFormat(pixelFormat, ctx);
		if (data.swapInterval != null && data.swapInterval.intValue() > 0)
			context.setValues(new int[] { data.swapInterval.intValue() }, OS.NSOpenGLCPSwapInterval);

		context.setValues(new int[] { -1 }, OS.NSOpenGLCPSurfaceOrder);
		canvas.setData(GLCONTEXT_KEY, context);
		NSNotificationCenter.defaultCenter().addObserver(view, OS.sel_updateOpenGLContext_, OS.NSViewGlobalFrameDidChangeNotification, view);

		Listener listener = event -> {
			switch (event.type) {

				case SWT.Dispose:
					canvas.setData(GLCONTEXT_KEY, null);
					NSNotificationCenter.defaultCenter().removeObserver(view);

					if (context != null) {
						context.clearDrawable();
						context.release();
					}
					context = null;
					if (pixelFormat != null)
						pixelFormat.release();
					pixelFormat = null;
					break;
			}
		};
		canvas.addListener(SWT.Dispose, listener);

		long[] longptr = new long[1];
		pixelFormat.getValues(longptr, OS.NSOpenGLPFAAlphaSize, 0);
		effective.alphaSize = (int) longptr[0];
		pixelFormat.getValues(longptr, OS.NSOpenGLPFADepthSize, 0);
		effective.depthSize = (int) longptr[0];
		pixelFormat.getValues(longptr, OS.NSOpenGLPFAStencilSize, 0);
		effective.stencilSize = (int) longptr[0];
		pixelFormat.getValues(longptr, OS.NSOpenGLPFADoubleBuffer, 0);
		effective.doubleBuffer = longptr[0] == 1;
		pixelFormat.getValues(longptr, OS.NSOpenGLPFAStereo, 0);
		effective.stereo = longptr[0] == 1;

		pixelFormat.getValues(longptr, NSOpenGLPFAOpenGLProfile, 0);
		if (longptr[0] == NSOpenGLProfileVersion3_2Core) {
			effective.majorVersion = 3;
			effective.minorVersion = 2;
			effective.profile = Profile.CORE;
		} else if (longptr[0] == NSOpenGLProfileVersionLegacy) {
			effective.profile = Profile.COMPATIBILITY;
		} else if (longptr[0] == NSOpenGLProfileVersion4_1Core) {
			effective.majorVersion = 4;
			effective.minorVersion = 1;
			effective.profile = Profile.CORE;
		}

		pixelFormat.getValues(longptr, OS.NSOpenGLPFASampleBuffers, 0);
		data.sampleBuffers = (int) longptr[0];

		pixelFormat.getValues(longptr, OS.NSOpenGLPFASamples, 0);
		data.samples = (int) longptr[0];

		return context.id;
	}

	@Override
	public boolean isCurrent(long context) {
		NSOpenGLContext current = NSOpenGLContext.currentContext();
		return current != null && current.id == context;
	}

	@Override
	public boolean makeCurrent(GLCanvas canvas, long context) {
		new NSOpenGLContext(context).makeCurrentContext();
		return true;
	}

	@Override
	public boolean deleteContext(GLCanvas canvas, long context) {
		canvas.setData(GLCONTEXT_KEY, null);
		NSNotificationCenter.defaultCenter().removeObserver(view);
		NSOpenGLContext ctx = new NSOpenGLContext(context);
		if (ctx != null) {
			ctx.clearDrawable();
			ctx.release();
		}
		ctx = null;
		if (pixelFormat != null)
			pixelFormat.release();
		pixelFormat = null;
		return true;
	}

	@Override
	public boolean swapBuffers(GLCanvas canvas) {
		new NSOpenGLContext(canvas.context).flushBuffer();
		return true;
	}

	@Override
	public boolean delayBeforeSwapNV(GLCanvas canvas, float seconds) {
		// It seems that there's no support for this on OSX 
		//https://github.com/LWJGL/lwjgl3/commit/c82cf46050ea6e1b891756cfec392c66186e87b1
		return false;
	}
}

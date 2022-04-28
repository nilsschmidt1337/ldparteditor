package org.lwjgl.opengl.swt;

import static org.lwjgl.opengl.GLX.*;
import static org.lwjgl.opengl.GLX13.*;
import static org.lwjgl.opengl.GLXARBContextFlushControl.*;
import static org.lwjgl.opengl.GLXARBCreateContext.*;
import static org.lwjgl.opengl.GLXARBCreateContextNoError.*;
import static org.lwjgl.opengl.GLXARBCreateContextProfile.*;
import static org.lwjgl.opengl.GLXARBCreateContextRobustness.*;
import static org.lwjgl.opengl.GLXARBMultisample.*;
import static org.lwjgl.opengl.GLXARBRobustnessApplicationIsolation.*;
import static org.lwjgl.opengl.GLXEXTCreateContextES2Profile.*;
import static org.lwjgl.opengl.GLXEXTFramebufferSRGB.*;
import static org.lwjgl.opengl.GLXNVDelayBeforeSwap.*;
import static org.lwjgl.opengl.GLXNVMultisampleCoverage.*;

import java.nio.IntBuffer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.internal.gtk.GDK;
import org.eclipse.swt.internal.gtk.GTK;
import org.eclipse.swt.internal.gtk.GdkWindowAttr;

import org.eclipse.swt.widgets.Listener;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLXCapabilities;
import org.lwjgl.opengl.KHRNoError;
import org.lwjgl.opengl.swt.GLData.API;
import org.lwjgl.opengl.swt.GLData.Profile;
import org.lwjgl.opengl.swt.GLData.ReleaseBehavior;
import org.lwjgl.system.linux.XVisualInfo;

/**
 * Linux-specific implementation of methods for GLCanvas.
 * 
 * @author Joshua Slack
 */
class PlatformLinuxGLCanvas extends AbstractPlatformGLCanvas {

	@Override
	public long create(GLCanvas canvas, GLData data, GLData effective) {

		// Validate context attributes
		validateAttributes(data);

		// make sure our canvas has resources assigned
		GTK.gtk_widget_realize(canvas.handle);
		
		// grab handles to our window/display
		long window = GTK.gtk_widget_get_window(canvas.handle);
		long xDisplay = gdk_x11_display_get_xdisplay(window);

		// generate a list of config options for our frame buffer from the supplied data
        IntBuffer attribList = BufferUtils.createIntBuffer(64);
        populateFBConfigAttribs(data, attribList);
        
        // ask for matching frame buffer configs
		PointerBuffer fbCfg = glXChooseFBConfig(xDisplay, 0, attribList);
		if (fbCfg == null || !fbCfg.hasRemaining()) {
			canvas.dispose();
			throw new SWTException("Unable to find matching FB Config");
		}

		// convert our fbconfig to a visualinfo so we can apply it to the widget
		XVisualInfo viz = glXGetVisualFromFBConfig(xDisplay, fbCfg.get(0));
		
		// grab our default screen for the default display
		long screen = GDK.gdk_screen_get_default();
		
		// ask the screen for a GdkVisual that matches the given info
		long gdkvisual = GDK.gdk_x11_screen_lookup_visual(screen, (int) viz.visualid());

		// put together attributes for a new window using the visual
		GdkWindowAttr winAttrs = new GdkWindowAttr();
		winAttrs.width = 1;
		winAttrs.height = 1;
		winAttrs.event_mask = GDK.GDK_KEY_PRESS_MASK | GDK.GDK_KEY_RELEASE_MASK | GDK.GDK_FOCUS_CHANGE_MASK
				| GDK.GDK_POINTER_MOTION_MASK | GDK.GDK_BUTTON_PRESS_MASK | GDK.GDK_BUTTON_RELEASE_MASK
				| GDK.GDK_ENTER_NOTIFY_MASK | GDK.GDK_LEAVE_NOTIFY_MASK | GDK.GDK_EXPOSURE_MASK
				| GDK.GDK_POINTER_MOTION_HINT_MASK;
		winAttrs.window_type = GDK.GDK_WINDOW_CHILD;
		winAttrs.visual = gdkvisual;

		// create the new window - this gives us a glxdrawable too
		canvas.glWindow = GDK.gdk_window_new(window, winAttrs, GDK.GDK_WA_VISUAL);
		
		// sets the user data as the widget that owns the window - historical
		// see: https://developer.gnome.org/gdk3/stable/gdk3-Windows.html#gdk-window-set-user-data
		GDK.gdk_window_set_user_data(canvas.glWindow, canvas.handle);
		
		// get the X id of the new window and call to show it
		canvas.xWindow = GDK.gdk_x11_window_get_xid(canvas.glWindow);
		GDK.gdk_window_show(canvas.glWindow);

		// context generation time - put we'll use our fbconfig here to get a core compatible context
		// start by generating our list of attributes
		attribList.rewind();
		GLXCapabilities caps = GL.getCapabilitiesGLX();
		populateContextAttribs(data, attribList, caps);

		// create the context... pass our display, fbconfig, attributes and any shared context
		long share = data.shareContext != null ? data.shareContext.context : 0;
		long context = glXCreateContextAttribsARB(xDisplay, fbCfg.get(0), share, true, attribList);
		if (context == 0) throw new SWTException("Unable to create context");

		// Set up SWT event listeners to handle disposal and resize
		Listener listener = event -> {
			switch (event.type) {
			case SWT.Resize:
				Rectangle clientArea = DPIUtil.autoScaleUp(canvas.getClientArea());
				GDK.gdk_window_move(canvas.glWindow, clientArea.x, clientArea.y);
				GDK.gdk_window_resize(canvas.glWindow, clientArea.width, clientArea.height);
				break;
			case SWT.Dispose:
				deleteContext(canvas, context);
				break;
			}
		};
		canvas.addListener(SWT.Resize, listener);
		canvas.addListener(SWT.Dispose, listener);
		
		// Done!  Return our context.
		return context;
	}

	private void populateFBConfigAttribs(GLData data, IntBuffer attribList) {
		if (data.redSize > 0) attribList.put(GLX_RED_SIZE).put(data.redSize);
        if (data.greenSize > 0) attribList.put(GLX_GREEN_SIZE).put(data.greenSize);
        if (data.blueSize > 0) attribList.put(GLX_BLUE_SIZE).put(data.blueSize);
        if (data.alphaSize > 0) attribList.put(GLX_ALPHA_SIZE).put(data.alphaSize);
        
        if (data.depthSize > 0) attribList.put(GLX_DEPTH_SIZE).put(data.depthSize);
        if (data.stencilSize > 0) attribList.put(GLX_STENCIL_SIZE).put(data.stencilSize);
        
        if (data.doubleBuffer) attribList.put(GLX_DOUBLEBUFFER).put(1);
        if (data.stereo) attribList.put(GLX_STEREO).put(1);
        if (data.sRGB) attribList.put(GLX_FRAMEBUFFER_SRGB_CAPABLE_EXT).put(1);

        if (data.accumRedSize > 0) attribList.put(GLX_ACCUM_RED_SIZE).put(data.accumRedSize);
        if (data.accumGreenSize > 0) attribList.put(GLX_ACCUM_GREEN_SIZE).put(data.accumGreenSize);
        if (data.accumBlueSize > 0) attribList.put(GLX_ACCUM_BLUE_SIZE).put(data.accumBlueSize);
        if (data.accumAlphaSize > 0) attribList.put(GLX_ACCUM_ALPHA_SIZE).put(data.accumAlphaSize);
        
        if (data.samples > 0) {
        	attribList.put(GLX_SAMPLE_BUFFERS_ARB).put(1);
        	attribList.put(GLX_SAMPLES_ARB).put(data.samples);
            if (data.colorSamplesNV > 0) {
                attribList.put(GLX_COLOR_SAMPLES_NV).put(data.colorSamplesNV);
            }
        }
        
        attribList.put(0);
        attribList.flip();
	}
	
	private void populateContextAttribs(GLData data, IntBuffer attribList, GLXCapabilities caps) {
		attribList.put(GLX_CONTEXT_MAJOR_VERSION_ARB).put(data.majorVersion);
		attribList.put(GLX_CONTEXT_MINOR_VERSION_ARB).put(data.minorVersion);
		
		
        int profile = 0;
        if (data.api == API.GL) {
            if (data.profile == Profile.COMPATIBILITY) {
                profile = GLX_CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB;
            } else if (data.profile == Profile.CORE) {
                profile = GLX_CONTEXT_CORE_PROFILE_BIT_ARB;
            }
        } else if (data.api == API.GLES) {
            if (!caps.GLX_EXT_create_context_es2_profile) {
                throw new SWTException("OpenGL ES API requested but GLX_EXT_create_context_es2_profile is unavailable");
            }
            profile = GLX_CONTEXT_ES2_PROFILE_BIT_EXT;
        }
        if (profile > 0) {
            if (!caps.GLX_ARB_create_context_profile) {
                throw new SWTException("OpenGL profile requested but GLX_ARB_create_context_profile is unavailable");
            }
            attribList.put(GLX_CONTEXT_PROFILE_MASK_ARB).put(profile);
        }

        // Context Flags
        int contextFlags = 0;
        if (data.debug) contextFlags |= GLX_CONTEXT_DEBUG_BIT_ARB;
        if (data.forwardCompatible) contextFlags |= GLX_CONTEXT_FORWARD_COMPATIBLE_BIT_ARB;
        if (data.noErrorContext) {
            contextFlags |= KHRNoError.GL_CONTEXT_FLAG_NO_ERROR_BIT_KHR;
            attribList.put(GLX_CONTEXT_OPENGL_NO_ERROR_ARB).put(1);
        }
        if (data.robustness) {
            // Check for GLX_ARB_create_context_robustness
            if (!caps.GLX_ARB_create_context_robustness) {
                throw new SWTException("Context with robust buffer access requested but GLX_ARB_create_context_robustness is unavailable");
            }
            contextFlags |= GLX_CONTEXT_ROBUST_ACCESS_BIT_ARB;
            if (data.loseContextOnReset) {
                attribList.put(GLX_CONTEXT_RESET_NOTIFICATION_STRATEGY_ARB).put(
                        GLX_LOSE_CONTEXT_ON_RESET_ARB);
                // Note: GLX_NO_RESET_NOTIFICATION_ARB is default behaviour and need not be specified.
            }
            if (data.contextResetIsolation) {
                // Check for GLX_ARB_robustness_application_isolation or GLX_ARB_robustness_share_group_isolation
                if (!caps.GLX_ARB_robustness_application_isolation && !caps.GLX_ARB_robustness_share_group_isolation) {
                    throw new SWTException(
                            "Robustness isolation requested but neither GLX_ARB_robustness_application_isolation nor GLX_ARB_robustness_share_group_isolation available");
                }
                contextFlags |= GLX_CONTEXT_RESET_ISOLATION_BIT_ARB;
            }
        }
        if (contextFlags > 0) attribList.put(GLX_CONTEXT_FLAGS_ARB).put(contextFlags);
        
        // Release behavior
        if (data.contextReleaseBehavior != null) {
            if (!caps.GLX_ARB_context_flush_control) {
                throw new SWTException("Context release behavior requested but GLX_ARB_context_flush_control is unavailable");
            }
            if (data.contextReleaseBehavior == ReleaseBehavior.NONE)
                attribList.put(GLX_CONTEXT_RELEASE_BEHAVIOR_ARB).put(GLX_CONTEXT_RELEASE_BEHAVIOR_NONE_ARB);
            else if (data.contextReleaseBehavior == ReleaseBehavior.FLUSH)
                attribList.put(GLX_CONTEXT_RELEASE_BEHAVIOR_ARB).put(GLX_CONTEXT_RELEASE_BEHAVIOR_FLUSH_ARB);
        }
        attribList.put(0);
        attribList.flip();
    }

	@Override
	public boolean isCurrent(long context) {
		return glXGetCurrentContext () == context;
	}

	@Override
	public boolean makeCurrent(GLCanvas canvas, long context) {
		long window = GTK.gtk_widget_get_window(canvas.handle);
		long xDisplay = gdk_x11_display_get_xdisplay(window);
		return glXMakeCurrent(xDisplay, canvas.xWindow, context);
	}

	@Override
	public boolean deleteContext(GLCanvas canvas, long context) {
		long window = GTK.gtk_widget_get_window(canvas.handle);
		long xDisplay = gdk_x11_display_get_xdisplay(window);
		if (context != 0) {
			if (glXGetCurrentContext() == context) {
				glXMakeCurrent(xDisplay, 0, 0);
			}
			glXDestroyContext(xDisplay, context);
			canvas.context = 0;
		}
		if (canvas.glWindow != 0) {
			GDK.gdk_window_destroy(canvas.glWindow);
			canvas.glWindow = 0;
		}
		return true;
	}

	@Override
	public boolean swapBuffers(GLCanvas canvas) {
		long window = GTK.gtk_widget_get_window(canvas.handle);
		long xDisplay = gdk_x11_display_get_xdisplay(window);
		glXSwapBuffers(xDisplay, canvas.xWindow);
		return false;
	}

	@Override
	public boolean delayBeforeSwapNV(GLCanvas canvas, float seconds) {
		long window = GTK.gtk_widget_get_window(canvas.handle);
		long xDisplay = gdk_x11_display_get_xdisplay(window);
        return glXDelayBeforeSwapNV(xDisplay, canvas.xWindow, seconds);
	}

	private long gdk_x11_display_get_xdisplay(long window) {
		long display = GDK.gdk_window_get_display(window);
		return GDK.gdk_x11_display_get_xdisplay(display);
	}
}

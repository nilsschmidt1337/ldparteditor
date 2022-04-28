package org.lwjgl.vulkan.swt;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.vulkan.KHRWin32Surface.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.widgets.Composite;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkWin32SurfaceCreateInfoKHR;

public class PlatformWin32VKCanvas implements PlatformVKCanvas {
    private static final String USE_OWNDC_KEY = "org.eclipse.swt.internal.win32.useOwnDC";

    public int checkStyle(Composite parent, int style) {
        // Somehow we need to temporarily set 'org.eclipse.swt.internal.win32.useOwnDC' to true or else context creation on Windows fails...
        if (parent != null) {
        	// Removed windows version check - SWT dropped support for windows before Vista
        	// https://github.com/eclipse/eclipse.platform.swt/commit/84c9e305cb087110cb300a5e58f86583cf80914d#diff-bb4584995e162b851fafacf3b046cc35
            parent.getDisplay().setData(USE_OWNDC_KEY, Boolean.TRUE);
        }
        return style;
    }

    public void resetStyle(Composite parent) {
        parent.getDisplay().setData(USE_OWNDC_KEY, Boolean.FALSE);
    }

    @Override
    public long create(Composite composite, VKData data) {
        VkWin32SurfaceCreateInfoKHR sci = VkWin32SurfaceCreateInfoKHR.callocStack()
		        .sType(VK_STRUCTURE_TYPE_WIN32_SURFACE_CREATE_INFO_KHR)
		        .hinstance(OS.GetModuleHandle(null))
		        .hwnd(composite.handle);
        LongBuffer pSurface = stackMallocLong(1);
        int err = vkCreateWin32SurfaceKHR(data.instance, sci, null, pSurface);
        long surface = pSurface.get(0);
        if (err != VK_SUCCESS) {
            throw new SWTException("Calling vkCreateWin32SurfaceKHR failed with error: " + err);
        }
        return surface;
    }

    public boolean getPhysicalDevicePresentationSupport(VkPhysicalDevice physicalDevice, int queueFamily) {
        return vkGetPhysicalDeviceWin32PresentationSupportKHR(physicalDevice, queueFamily);
    }

}

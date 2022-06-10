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
    private static final String USE_OWNDC_KEY = "org.eclipse.swt.internal.win32.useOwnDC"; //$NON-NLS-1$

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
            throw new SWTException("Calling vkCreateWin32SurfaceKHR failed with error: " + err); //$NON-NLS-1$
        }
        return surface;
    }

    public boolean getPhysicalDevicePresentationSupport(VkPhysicalDevice physicalDevice, int queueFamily) {
        return vkGetPhysicalDeviceWin32PresentationSupportKHR(physicalDevice, queueFamily);
    }

}

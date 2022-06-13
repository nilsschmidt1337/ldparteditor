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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.lwjgl.system.Platform;
import org.lwjgl.vulkan.VkPhysicalDevice;

/**
 * A SWT {@link Canvas} that supports to be drawn on using Vulkan.
 * 
 * @author Kai Burjack
 */
public class VKCanvas extends Canvas {
    private static PlatformVKCanvas platformCanvas;
    static {
        String platformClassName;
        switch (Platform.get()) {
        case WINDOWS:
            platformClassName = "org.lwjgl.vulkan.swt.PlatformWin32VKCanvas"; //$NON-NLS-1$
            break;
        default:
            throw new AssertionError("NYI"); //$NON-NLS-1$
        }
        try {
            @SuppressWarnings("unchecked")
            Class<? extends PlatformVKCanvas> clazz = (Class<? extends PlatformVKCanvas>) VKCanvas.class.getClassLoader().loadClass(platformClassName);
            platformCanvas = clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Platform-specific VKCanvas class not found: " + platformClassName); //$NON-NLS-1$
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new AssertionError("Could not instantiate platform-specific VKCanvas class: " + platformClassName); //$NON-NLS-1$
        }
    }

    /**
     * The Vulkan surface handle for this {@link VKCanvas}.
     */
    public long surface;

    /**
     * Create a {@link VKCanvas} widget using the attributes described in the supplied {@link VKData} object.
     *
     * @param parent
     *            a parent composite widget
     * @param style
     *            the bitwise OR'ing of widget styles
     * @param data
     *            the necessary data to create a VKCanvas
     */
    public VKCanvas(Composite parent, int style, VKData data) {
        super(parent, platformCanvas.checkStyle(parent, style));
        if (Platform.get() == Platform.WINDOWS) {
            platformCanvas.resetStyle(parent);
        }
        if (data == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        surface = platformCanvas.create(this, data);
    }

    /**
     * Determine whether there is presentation support for the given {@link VkPhysicalDevice} in a command queue of the specified
     * <code>queueFamiliy</code>.
     * 
     * @param physicalDevice
     *            the Vulkan {@link VkPhysicalDevice}
     * @param queueFamily
     *            the command queue family
     * @return <code>true</code> of <code>false</code>
     */
    public boolean getPhysicalDevicePresentationSupport(VkPhysicalDevice physicalDevice, int queueFamily) {
        return platformCanvas.getPhysicalDevicePresentationSupport(physicalDevice, queueFamily);
    }

}

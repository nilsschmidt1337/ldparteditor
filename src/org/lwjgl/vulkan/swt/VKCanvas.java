package org.lwjgl.vulkan.swt;

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
            platformClassName = "org.lwjgl.vulkan.swt.PlatformWin32VKCanvas";
            break;
        default:
            throw new AssertionError("NYI");
        }
        try {
            @SuppressWarnings("unchecked")
            Class<? extends PlatformVKCanvas> clazz = (Class<? extends PlatformVKCanvas>) VKCanvas.class.getClassLoader().loadClass(platformClassName);
            platformCanvas = clazz.newInstance();
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Platform-specific VKCanvas class not found: " + platformClassName);
        } catch (InstantiationException e) {
            throw new AssertionError("Could not instantiate platform-specific VKCanvas class: " + platformClassName);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Could not instantiate platform-specific VKCanvas class: " + platformClassName);
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

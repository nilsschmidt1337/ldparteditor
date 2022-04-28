package org.lwjgl.vulkan.swt;

import org.eclipse.swt.widgets.Composite;
import org.lwjgl.vulkan.VkPhysicalDevice;

public interface PlatformVKCanvas {

    int checkStyle(Composite parent, int style);

    void resetStyle(Composite parent);

    long create(Composite composite, VKData data);

    boolean getPhysicalDevicePresentationSupport(VkPhysicalDevice physicalDevice, int queueFamily);

}

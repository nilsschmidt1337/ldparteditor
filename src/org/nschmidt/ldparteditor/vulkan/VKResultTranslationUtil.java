/*
Copyright (c) 2012-present Lightweight Java Game Library All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 - Neither the name Lightweight Java Game Library nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/
package org.nschmidt.ldparteditor.vulkan;

import static org.lwjgl.vulkan.EXTDebugReport.VK_ERROR_VALIDATION_FAILED_EXT;
import static org.lwjgl.vulkan.KHRDisplaySwapchain.VK_ERROR_INCOMPATIBLE_DISPLAY_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_ERROR_NATIVE_WINDOW_IN_USE_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_ERROR_SURFACE_LOST_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_SUBOPTIMAL_KHR;
import static org.lwjgl.vulkan.VK10.VK_ERROR_DEVICE_LOST;
import static org.lwjgl.vulkan.VK10.VK_ERROR_EXTENSION_NOT_PRESENT;
import static org.lwjgl.vulkan.VK10.VK_ERROR_FEATURE_NOT_PRESENT;
import static org.lwjgl.vulkan.VK10.VK_ERROR_FORMAT_NOT_SUPPORTED;
import static org.lwjgl.vulkan.VK10.VK_ERROR_INCOMPATIBLE_DRIVER;
import static org.lwjgl.vulkan.VK10.VK_ERROR_INITIALIZATION_FAILED;
import static org.lwjgl.vulkan.VK10.VK_ERROR_LAYER_NOT_PRESENT;
import static org.lwjgl.vulkan.VK10.VK_ERROR_MEMORY_MAP_FAILED;
import static org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY;
import static org.lwjgl.vulkan.VK10.VK_ERROR_OUT_OF_HOST_MEMORY;
import static org.lwjgl.vulkan.VK10.VK_ERROR_TOO_MANY_OBJECTS;
import static org.lwjgl.vulkan.VK10.VK_EVENT_RESET;
import static org.lwjgl.vulkan.VK10.VK_EVENT_SET;
import static org.lwjgl.vulkan.VK10.VK_INCOMPLETE;
import static org.lwjgl.vulkan.VK10.VK_NOT_READY;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_TIMEOUT;

/**
 * Translates a Vulkan {@code VkResult} value to a String describing the result.
 */
public enum VKResultTranslationUtil {
    INSTANCE;

    /**
     * Translates a Vulkan {@code VkResult} value to a String describing the result.
     *
     * @param result
     *            the {@code VkResult} value
     *
     * @return the result description
     */
    public static String translateVulkanResult(int result) {
        switch (result) {
        // Success codes
        case VK_SUCCESS:
            return "Command successfully completed."; //$NON-NLS-1$
        case VK_NOT_READY:
            return "A fence or query has not yet completed."; //$NON-NLS-1$
        case VK_TIMEOUT:
            return "A wait operation has not completed in the specified time."; //$NON-NLS-1$
        case VK_EVENT_SET:
            return "An event is signaled."; //$NON-NLS-1$
        case VK_EVENT_RESET:
            return "An event is unsignaled."; //$NON-NLS-1$
        case VK_INCOMPLETE:
            return "A return array was too small for the result."; //$NON-NLS-1$
        case VK_SUBOPTIMAL_KHR:
            return "A swapchain no longer matches the surface properties exactly, but can still be used to present to the surface successfully."; //$NON-NLS-1$

            // Error codes
        case VK_ERROR_OUT_OF_HOST_MEMORY:
            return "A host memory allocation has failed."; //$NON-NLS-1$
        case VK_ERROR_OUT_OF_DEVICE_MEMORY:
            return "A device memory allocation has failed."; //$NON-NLS-1$
        case VK_ERROR_INITIALIZATION_FAILED:
            return "Initialization of an object could not be completed for implementation-specific reasons."; //$NON-NLS-1$
        case VK_ERROR_DEVICE_LOST:
            return "The logical or physical device has been lost."; //$NON-NLS-1$
        case VK_ERROR_MEMORY_MAP_FAILED:
            return "Mapping of a memory object has failed."; //$NON-NLS-1$
        case VK_ERROR_LAYER_NOT_PRESENT:
            return "A requested layer is not present or could not be loaded."; //$NON-NLS-1$
        case VK_ERROR_EXTENSION_NOT_PRESENT:
            return "A requested extension is not supported."; //$NON-NLS-1$
        case VK_ERROR_FEATURE_NOT_PRESENT:
            return "A requested feature is not supported."; //$NON-NLS-1$
        case VK_ERROR_INCOMPATIBLE_DRIVER:
            return "The requested version of Vulkan is not supported by the driver or is otherwise incompatible for implementation-specific reasons."; //$NON-NLS-1$
        case VK_ERROR_TOO_MANY_OBJECTS:
            return "Too many objects of the type have already been created."; //$NON-NLS-1$
        case VK_ERROR_FORMAT_NOT_SUPPORTED:
            return "A requested format is not supported on this device."; //$NON-NLS-1$
        case VK_ERROR_SURFACE_LOST_KHR:
            return "A surface is no longer available."; //$NON-NLS-1$
        case VK_ERROR_NATIVE_WINDOW_IN_USE_KHR:
            return "The requested window is already connected to a VkSurfaceKHR, or to some other non-Vulkan API."; //$NON-NLS-1$
        case VK_ERROR_OUT_OF_DATE_KHR:
            return "A surface has changed in such a way that it is no longer compatible with the swapchain, and further presentation requests using the " //$NON-NLS-1$
                    + "swapchain will fail. Applications must query the new surface properties and recreate their swapchain if they wish to continue" + "presenting to the surface."; //$NON-NLS-1$ //$NON-NLS-2$
        case VK_ERROR_INCOMPATIBLE_DISPLAY_KHR:
            return "The display used by a swapchain does not use the same presentable image layout, or is incompatible in a way that prevents sharing an" + " image."; //$NON-NLS-1$ //$NON-NLS-2$
        case VK_ERROR_VALIDATION_FAILED_EXT:
            return "A validation layer found an error."; //$NON-NLS-1$
        default:
            return String.format("%s [%d]", "Unknown", Integer.valueOf(result)); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}

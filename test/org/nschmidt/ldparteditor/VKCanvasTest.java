/* MIT - License

Copyright (c) 2012 - this year, Nils Schmidt

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.nschmidt.ldparteditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.swt.VKCanvas;
import org.lwjgl.vulkan.swt.VKData;
import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_ERROR_EXTENSION_NOT_PRESENT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME;

import static org.lwjgl.vulkan.KHRSurface.VK_KHR_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.EXTMetalSurface.VK_EXT_METAL_SURFACE_EXTENSION_NAME;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;

@SuppressWarnings("java:S5960")
public class VKCanvasTest {
    
    @Test
    public void testInstanceCreation() {
        final Display display = new Display();
        final Shell sh = new Shell(display);
        sh.setSize(1024, 768);
        sh.open();
        sh.setActive();
        
        int i = 0;
        while (!sh.isDisposed() && i < 5) { 
            if (!display.readAndDispatch()) 
             { display.sleep();} 
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            i++;
        }
        
        final Composite cmp = sh;
        final VKData data = new VKData();
        
        ByteBuffer VK_EXT_DEBUG_REPORT_EXTENSION = memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
        ByteBuffer VK_EXT_DEBUG_UTILS_EXTENSION = memUTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
        ByteBuffer VK_KHR_SURFACE_EXTENSION = memUTF8(VK_KHR_SURFACE_EXTENSION_NAME);
        ByteBuffer VK_EXT_METAL_SURFACE_EXTENSION = memUTF8(VK_EXT_METAL_SURFACE_EXTENSION_NAME);
        
        PointerBuffer ppEnabledExtensionNames = memAllocPointer(4);
        ppEnabledExtensionNames.put(VK_EXT_DEBUG_REPORT_EXTENSION);
        ppEnabledExtensionNames.put(VK_EXT_DEBUG_UTILS_EXTENSION);
        ppEnabledExtensionNames.put(VK_KHR_SURFACE_EXTENSION);
        ppEnabledExtensionNames.put(VK_EXT_METAL_SURFACE_EXTENSION);
        ppEnabledExtensionNames.flip();
        
        VkApplicationInfo appInfo = VkApplicationInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(memUTF8("Vulkan Demo")) //$NON-NLS-1$
                .pEngineName(memUTF8("LDPE ENGINE")) //$NON-NLS-1$
                .apiVersion(VK_MAKE_VERSION(1, 1, 0));
        
        VkInstanceCreateInfo pCreateInfo = VkInstanceCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pNext(0)
                .pApplicationInfo(appInfo)
                .ppEnabledExtensionNames(ppEnabledExtensionNames);
        PointerBuffer pInstance = memAllocPointer(1);
        
        int err = vkCreateInstance(pCreateInfo, null, pInstance);
    
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to create VkInstance: " + err); //$NON-NLS-1$
        }
        
        VkInstance handle = new VkInstance(pInstance.get(0), pCreateInfo);
        
        pCreateInfo.free();
        memFree(pInstance);
        memFree(appInfo.pApplicationName());
        memFree(appInfo.pEngineName());
        appInfo.free();
        
        data.instance = handle;
        final VKCanvas cut = new VKCanvas(new Composite(cmp, SWT.NONE), SWT.NONE, data);
    }
}

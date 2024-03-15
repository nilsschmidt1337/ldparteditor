package org.lwjgl.vulkan.swt;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Composite;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkAllocationCallbacks;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.vulkan.EXTMetalSurface.*;
import org.lwjgl.vulkan.VkPhysicalDevice;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkMetalSurfaceCreateInfoEXT;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.*;
import org.lwjgl.system.libffi.FFICIF;
import org.lwjgl.system.libffi.LibFFI;
import org.lwjgl.system.macosx.MacOSXLibrary;
import org.lwjgl.system.macosx.ObjCRuntime;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkMetalSurfaceCreateInfoEXT;
import org.lwjgl.vulkan.VkPhysicalDevice;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.EXTMetalSurface.*;
import static org.lwjgl.vulkan.KHRSurface.VK_ERROR_NATIVE_WINDOW_IN_USE_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class PlatformMacOSXVKCanvas implements PlatformVKCanvas {

    @Override
    public int checkStyle(Composite parent, int style) {
        return style;
    }

    @Override
    public void resetStyle(Composite parent) {
        // NOP
    }

    @Override
    public long create(Composite composite, VKData data) {
        long metalLayer = composite.getParent().getParent().view.layer().id;//createMTKView(0, 0, 1024, 768);
        
        final VkMetalSurfaceCreateInfoEXT ci = VkMetalSurfaceCreateInfoEXT.calloc()
                .sType(VK_STRUCTURE_TYPE_METAL_SURFACE_CREATE_INFO_EXT)
                .pLayer(PointerBuffer.create(metalLayer, 1));
        final VkAllocationCallbacks cb = null;
        final LongBuffer pSurface = LongBuffer.allocate(1);
        int err =  vkCreateMetalSurfaceEXT(data.instance, ci, cb, pSurface);
        long surface = pSurface.get(0);
        if (err != VK_SUCCESS) {
            throw new SWTException("Calling vkCreateMetalSurfaceEXT failed with error: " + err); //$NON-NLS-1$
        }
        return surface;
    }

    @Override
    public boolean getPhysicalDevicePresentationSupport(VkPhysicalDevice physicalDevice, int queueFamily) {
        // TODO Auto-generated method stub
        return false;
    }
    
    /**
     * Creates the native Metal view.
     * <p>
     * Because {@link JNI} does not provide a method signature for {@code PPDDDDPP},
     * we have to construct a call interface ourselves via {@link LibFFI}.
     * <p>
     * {@code
     * id<MTLDevice> device = MTLCreateSystemDefaultDevice();
     * MTKView *view = [[MTKView alloc] initWithFrame:frame device:device]; // frame is from a GCRectMake();
     * surfaceLayers.layer = view.layer; // jawt platform object
     * return view.layer;
     * }
     *
     * @param x            x position of the window
     * @param y            y position of the window
     * @param width        window width
     * @param height       window height
     * @return pointer to a native window handle
     */
    private static long createMTKView(int x, int y, int width, int height) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SharedLibrary metalKit = MacOSXLibrary.create("/System/Library/Frameworks/MetalKit.framework"); //$NON-NLS-1$
            SharedLibrary metal = MacOSXLibrary.create("/System/Library/Frameworks/Metal.framework"); //$NON-NLS-1$
            long objc_msgSend = ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend"); //$NON-NLS-1$
            metalKit.getFunctionAddress("MTKView"); // loads the MTKView class or something (required, somehow) //$NON-NLS-1$

            // id<MTLDevice> device = MTLCreateSystemDefaultDevice();
            long device = JNI.invokeP(metal.getFunctionAddress("MTLCreateSystemDefaultDevice")); //$NON-NLS-1$


            PointerBuffer argumentTypes = BufferUtils.createPointerBuffer(7) // 4 arguments, one of them an array of 4 doubles
                    .put(0, LibFFI.ffi_type_pointer) // MTKView*
                    .put(1, LibFFI.ffi_type_pointer) // initWithFrame:
                    .put(2, LibFFI.ffi_type_double) // CGRect
                    .put(3, LibFFI.ffi_type_double) // CGRect
                    .put(4, LibFFI.ffi_type_double) // CGRect
                    .put(5, LibFFI.ffi_type_double) // CGRect
                    .put(6, LibFFI.ffi_type_pointer); // device*

            // Prepare the call interface
            FFICIF cif = FFICIF.malloc(stack);
            int status = LibFFI.ffi_prep_cif(cif, LibFFI.FFI_DEFAULT_ABI, LibFFI.ffi_type_pointer, argumentTypes);
            if (status != LibFFI.FFI_OK) {
                throw new IllegalStateException("ffi_prep_cif failed: " + status); //$NON-NLS-1$
            }

            // An array of pointers that point to the actual argument values.
            PointerBuffer arguments = stack.mallocPointer(7);

            // Storage for the actual argument values.
            ByteBuffer values = stack.malloc(
                            Pointer.POINTER_SIZE +     // MTKView*
                            Pointer.POINTER_SIZE +     // initWithFrame*
                            Double.BYTES * 4 +         // CGRect (4 doubles)
                            Pointer.POINTER_SIZE       // device*
            );

            // MTKView *view = [MTKView alloc];
            long mtkView = JNI.invokePPP(
                    ObjCRuntime.objc_getClass("MTKView"), //$NON-NLS-1$
                    ObjCRuntime.sel_getUid("alloc"), //$NON-NLS-1$
                    objc_msgSend);

            // Set up the argument buffers by inserting pointers

            // MTKView*
            arguments.put(MemoryUtil.memAddress(values));
            PointerBuffer.put(values, mtkView);

            // initWithFrame*
            arguments.put(MemoryUtil.memAddress(values));
            PointerBuffer.put(values, ObjCRuntime.sel_getUid("initWithFrame:")); //$NON-NLS-1$

            // frame
            arguments.put(MemoryUtil.memAddress(values));
            values.putDouble(x);
            arguments.put(MemoryUtil.memAddress(values));
            values.putDouble(y);
            arguments.put(MemoryUtil.memAddress(values));
            values.putDouble(width);
            arguments.put(MemoryUtil.memAddress(values));
            values.putDouble(height);

            // device*
            arguments.put(MemoryUtil.memAddress(values));
            values.putLong(device);

            arguments.flip();
            values.flip();

            // [view initWithFrame:rect device:device];
            // Returns itself, we just need to know if it's NULL
            LongBuffer pMTKView = stack.mallocLong(1);
            LibFFI.ffi_call(cif, objc_msgSend, MemoryUtil.memByteBuffer(pMTKView), arguments);
            if (pMTKView.get(0) == MemoryUtil.NULL) {
                throw new IllegalStateException("[MTKView initWithFrame:device:] returned null."); //$NON-NLS-1$
            }


            // layer = view.layer;
            long layer = JNI.invokePPP(mtkView,
                    ObjCRuntime.sel_getUid("layer"), //$NON-NLS-1$
                    objc_msgSend);
            
            // return layer;
            return layer;
        }
    }

}

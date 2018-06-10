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
package org.nschmidt.ldparteditor.composites.primitive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.FloatBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.wb.swt.SWTResourceManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.BFC;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.PGData;
import org.nschmidt.ldparteditor.data.PGData1;
import org.nschmidt.ldparteditor.data.PGData2;
import org.nschmidt.ldparteditor.data.PGData3;
import org.nschmidt.ldparteditor.data.PGData4;
import org.nschmidt.ldparteditor.data.PGData5;
import org.nschmidt.ldparteditor.data.PGDataBFC;
import org.nschmidt.ldparteditor.data.PGDataInit;
import org.nschmidt.ldparteditor.data.PGDataProxy;
import org.nschmidt.ldparteditor.data.PGTimestamp;
import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.dnd.MyDummyTransfer2;
import org.nschmidt.ldparteditor.dnd.MyDummyType2;
import org.nschmidt.ldparteditor.enums.MouseButton;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.Rule;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRendererPrimitives;
import org.nschmidt.ldparteditor.opengl.OpenGLRendererPrimitives20;
import org.nschmidt.ldparteditor.opengl.OpenGLRendererPrimitives33;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public class CompositePrimitive extends Composite {

    /** The {@linkplain OpenGLRendererPrimitives} instance */
    private final OpenGLRendererPrimitives openGL = WorkbenchManager.getUserSettingState().getOpenGLVersion() == 20 ? new OpenGLRendererPrimitives20(this) : new OpenGLRendererPrimitives33(this);

    /** the {@linkplain GLCanvas} */
    final GLCanvas canvas;
    final GLCapabilities capabilities;

    /** The view zoom level */
    private float zoom = WorkbenchManager.getEditor3DWindowState().getPrimitiveZoom();//(float) Math.pow(10.0d, 3f / 10 - 3);
    private float zoom_exponent = WorkbenchManager.getEditor3DWindowState().getPrimitiveZoomExponent(); // 3f;

    /** The transformation matrix of the view */
    private final Matrix4f viewport_matrix = new Matrix4f();
    /** The inverse transformation matrix of the view */
    private Matrix4f viewport_matrix_inv = new Matrix4f();
    /** The translation matrix of the view */
    private final Matrix4f viewport_translation = new Matrix4f();

    private final Matrix4f viewport_rotation = new Matrix4f();

    private volatile ArrayList<Primitive> primitives = new ArrayList<Primitive>();
    private Primitive selectedPrimitive = null;
    private Primitive focusedPrimitive = null;
    private int mouse_button_pressed;
    private final Vector2f old_mouse_position = new Vector2f();
    private final Vector2f mouse_position = new Vector2f();
    /** The old translation matrix of the view [NOT PUBLIC YET] */
    private final Matrix4f old_viewport_translation = new Matrix4f();
    private final Matrix4f old_viewport_rotation = new Matrix4f();

    /** Resolution of the viewport at n% zoom */
    private float viewport_pixel_per_ldu;

    private float rotationWidth = 300f;

    private volatile AtomicBoolean dontRefresh = new AtomicBoolean(false);
    private volatile AtomicBoolean hasDrawn = new AtomicBoolean(false);
    private volatile AtomicBoolean stopDraw = new AtomicBoolean(true);
    private volatile Lock loadingLock = new ReentrantLock();

    private final KeyStateManager keyboard = new KeyStateManager(this);
    private float maxY = 0f;

    private boolean doingDND;

    private ArrayList<Primitive> searchResults = new ArrayList<Primitive>();
    private static HashMap<String, PGData> cache = new HashMap<String, PGData>();
    private static HashMap<PGTimestamp, ArrayList<String>> fileCache = new HashMap<PGTimestamp, ArrayList<String>>();
    private static HashSet<PGTimestamp> fileCacheHits = new HashSet<PGTimestamp>();

    public CompositePrimitive(Composite parent) {
        super(parent, I18n.I18N_NON_BIDIRECT() | SWT.BORDER);
        {
            float[] rpf = new float[] {
                    -0.7071f, 0.5f, 0.5f, 0,
                    0, 0.7071f, -0.7071f, 0,
                    -0.7071f, -0.5f, -0.5f, 0,
                    0, 0, 0, 1 };
            FloatBuffer fb = BufferUtils.createFloatBuffer(16);
            fb.put(rpf);
            fb.flip();
            this.viewport_rotation.load(fb);
        }

        this.viewport_pixel_per_ldu = this.zoom * View.PIXEL_PER_LDU;

        this.setLayout(new FillLayout());
        GLData data = new GLData();
        data.doubleBuffer = true;
        data.depthSize = 24;
        data.alphaSize = 8;
        data.blueSize = 8;
        data.redSize = 8;
        data.greenSize = 8;
        data.stencilSize = 8;
        if (WorkbenchManager.getUserSettingState().isAntiAliasing()) {
            data.sampleBuffers = 1;
            data.samples = 4;
        }
        canvas = new GLCanvas(this, I18n.I18N_NON_BIDIRECT(), data);
        canvas.setCurrent();
        if (WorkbenchManager.getUserSettingState().getOpenGLVersion() == 20) {
            capabilities = GL.createCapabilities();
        } else {
            capabilities = GL.createCapabilities(true);
        }
        canvas.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_HAND));
        canvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

        this.setBackgroundMode(SWT.INHERIT_FORCE);

        Transfer[] types = new Transfer[] { MyDummyTransfer2.getInstance() };
        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

        final DragSource source = new DragSource(canvas, operations);
        source.setTransfer(types);
        source.addDragListener(new DragSourceListener() {
            @Override
            public void dragStart(DragSourceEvent event) {
                event.doit = true;
                setDoingDND(true);
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = new MyDummyType2();
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                setDoingDND(false);
            }
        });

        GL.setCapabilities(capabilities);
        canvas.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                canvas.getCursor().dispose();
            }
        });
        // MARK Resize
        canvas.addListener(SWT.Resize, event -> {
            canvas.setCurrent();
            GL.setCapabilities(capabilities);
            Display.getCurrent().timerExec(500, new Runnable() {
                @Override
                public void run() {
                    openGL.drawScene(-1, -1);
                }
            });
        });

        canvas.addListener(SWT.MouseDown, event -> mouseDown(event));

        canvas.addListener(SWT.MouseDoubleClick, event -> {
            mouse_button_pressed = event.button;
            old_mouse_position.set(event.x, event.y);
            switch (event.button) {
            case MouseButton.LEFT:
                setSelectedPrimitive(getFocusedPrimitive());
                if (getSelectedPrimitive() != null) getSelectedPrimitive().toggle();
                break;
            case MouseButton.MIDDLE:
                break;
            case MouseButton.RIGHT:
                Matrix4f.load(getTranslation(), old_viewport_translation);
                break;
            default:
            }
            openGL.drawScene(event.x, event.y);
            Editor3DWindow.getWindow().regainFocus();
        });

        canvas.addListener(SWT.KeyDown, event -> keyboard.setStates(event.keyCode, SWT.KeyDown, event));

        canvas.addListener(SWT.KeyUp, event -> keyboard.setStates(event.keyCode, SWT.KeyUp, event));

        canvas.addListener(SWT.MouseMove, event -> {
            canvas.forceFocus();
            if (!stopDraw()) dontRefresh.set(true);
            {
                Object[] messageArguments = {KeyStateManager.getTaskKeymap().get(Task.MMB)};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.LOCALE);
                formatter.applyPattern(Cocoa.isCocoa ? I18n.E3D_RotateViewHintMac : I18n.E3D_RotateViewHint);
                String tooltipText = formatter.format(messageArguments);
                if (!tooltipText.equals(canvas.getToolTipText())) {
                    canvas.setToolTipText(tooltipText);
                }
            }
            mouse_position.set(event.x, event.y);
            switch (mouse_button_pressed) {
            case MouseButton.LEFT:
                break;
            case MouseButton.MIDDLE:
                float rx = 0;
                float ry = 0;
                rx = (event.x - old_mouse_position.x) / rotationWidth * (float) Math.PI;
                ry = (old_mouse_position.y - event.y) / rotationWidth * (float) Math.PI;
                Vector4f xAxis4f_rotation = new Vector4f(1.0f, 0, 0, 1.0f);
                Vector4f yAxis4f_rotation = new Vector4f(0, 1.0f, 0, 1.0f);
                Matrix4f ovr_inverse = Matrix4f.invert(old_viewport_rotation, null);
                Matrix4f.transform(ovr_inverse, xAxis4f_rotation, xAxis4f_rotation);
                Matrix4f.transform(ovr_inverse, yAxis4f_rotation, yAxis4f_rotation);
                Vector3f xAxis3f_rotation = new Vector3f(xAxis4f_rotation.x, xAxis4f_rotation.y, xAxis4f_rotation.z);
                Vector3f yAxis3f_rotation = new Vector3f(yAxis4f_rotation.x, yAxis4f_rotation.y, yAxis4f_rotation.z);
                Matrix4f.rotate(rx, yAxis3f_rotation, old_viewport_rotation, viewport_rotation);
                Matrix4f.rotate(ry, xAxis3f_rotation, viewport_rotation, viewport_rotation);
                break;
            case MouseButton.RIGHT:
                float dx = 0;
                float dy = 0;
                dx = (event.x - old_mouse_position.x) / viewport_pixel_per_ldu;
                dy = (event.y - old_mouse_position.y) / viewport_pixel_per_ldu;
                Vector4f xAxis4f_translation = new Vector4f(dx, 0, 0, 1.0f);
                Vector4f yAxis4f_translation = new Vector4f(0, dy, 0, 1.0f);
                Vector3f xAxis3 = new Vector3f(xAxis4f_translation.x, xAxis4f_translation.y, xAxis4f_translation.z);
                Vector3f yAxis3 = new Vector3f(yAxis4f_translation.x, yAxis4f_translation.y, yAxis4f_translation.z);
                Matrix4f.load(old_viewport_translation, viewport_translation);
                Matrix4f.translate(xAxis3, old_viewport_translation, viewport_translation);
                Matrix4f.translate(yAxis3, viewport_translation, viewport_translation);

                // if (viewport_translation.m30 > 0f) viewport_translation.m30 = 0f;

                viewport_translation.m30 = 0f;
                if (viewport_translation.m31 > 0f) viewport_translation.m31 = 0f;
                if (-viewport_translation.m31 > maxY) viewport_translation.m31 = -maxY;
                break;
            default:
            }
            openGL.drawScene(event.x, event.y);
        });

        canvas.addListener(SWT.MouseUp, event -> mouseUp(event));
        canvas.addListener(SWT.Paint, event -> openGL.drawScene(-1, -1));
        canvas.addListener(SWT.MouseVerticalWheel, event -> {

            if (Cocoa.checkCtrlOrCmdPressed(event.stateMask)) {
                if (event.count < 0)
                    zoomIn();
                else
                    zoomOut();
            } else {
                float dy = 0;

                Matrix4f.load(getTranslation(), old_viewport_translation);

                if (event.count < 0) {
                    dy = -17f /  viewport_pixel_per_ldu;
                } else {
                    dy = 17f /  viewport_pixel_per_ldu;
                }

                Vector4f yAxis4f_translation = new Vector4f(0, dy, 0, 1.0f);
                Vector3f yAxis3 = new Vector3f(yAxis4f_translation.x, yAxis4f_translation.y, yAxis4f_translation.z);
                Matrix4f.load(old_viewport_translation, viewport_translation);
                Matrix4f.translate(yAxis3, old_viewport_translation, viewport_translation);

                if (viewport_translation.m31 > 0f) viewport_translation.m31 = 0f;
                if (-viewport_translation.m31 > maxY) viewport_translation.m31 = -maxY;
            }

            openGL.drawScene(event.x, event.y);


        });

        openGL.init();
        Display.getCurrent().timerExec(10, new Runnable() {

            private int quickDrawnFrames = 10;

            @Override
            public void run() {
                try {
                    openGL.drawScene(-1, -1);
                    hasDrawn.set(true);
                    if (dontRefresh.get()) {
                        Editor3DWindow.getWindow().updatePrimitiveLabel(null);
                        return;
                    }
                    if (stopDraw.get()) {
                        Display.getCurrent().timerExec(100, this);
                    } else if (quickDrawnFrames > 0) {
                        Display.getCurrent().timerExec(100, this);
                        quickDrawnFrames--;
                    } else {
                        Editor3DWindow.getWindow().updatePrimitiveLabel(null);
                        Display.getCurrent().timerExec(3000, this);
                    }
                } catch (SWTException consumed) {}
            }
        });
    }

    public void mouseUp(Event event) {
        mouse_button_pressed = 0;
        switch (event.button) {
        case MouseButton.LEFT:
            break;
        case MouseButton.MIDDLE:
            break;
        case MouseButton.RIGHT:
            break;
        default:
        }
    }

    public void mouseDown(Event event) {
        mouse_button_pressed = event.button;
        old_mouse_position.set(event.x, event.y);
        switch (event.button) {
        case MouseButton.LEFT:
            setSelectedPrimitive(getFocusedPrimitive());
            break;
        case MouseButton.MIDDLE:
            Matrix4f.load(getRotation(), old_viewport_rotation);
            break;
        case MouseButton.RIGHT:
            Matrix4f.load(getTranslation(), old_viewport_translation);
            break;
        default:
        }
        openGL.drawScene(event.x, event.y);
        Editor3DWindow.getWindow().regainFocus();
    }

    public GLCanvas getCanvas() {
        return canvas;
    }

    public GLCapabilities getCapabilities() {
        return capabilities;
    }

    /**
     * @return the view zoom level exponent
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Set the view zoom level exponent
     *
     * @param zoom
     *            value between -10.0 and 10.0
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public float getZoom_exponent() {
        return zoom_exponent;
    }

    public void setZoom_exponent(float zoom_exponent) {
        this.zoom_exponent = zoom_exponent;
    }

    /**
     * @return The translation matrix of the view
     */
    public Matrix4f getTranslation() {
        return viewport_translation;
    }

    public Matrix4f getRotation() {
        return viewport_rotation;
    }

    /**
     * @return The transformation matrix of the viewport which was last drawn
     */
    public Matrix4f getViewport() {
        return viewport_matrix;
    }

    public Matrix4f getViewport_Inverse() {
        return viewport_matrix_inv;
    }

    /**
     * Sets the transformation matrix of the viewport
     *
     * @param matrix
     *            the matrix to set.
     */
    public void setViewport(Matrix4f matrix) {
        viewport_matrix.load(matrix);
        viewport_matrix_inv = (Matrix4f) matrix.invert();
    }

    public void setViewport2(Matrix4f[] m) {
        viewport_matrix.load(m[0]);
        viewport_translation.setIdentity(); // .load(m[1]); // Don't load the translation (issue #566)
        viewport_rotation.load(m[2]);
        viewport_matrix_inv = (Matrix4f) m[0].invert();
    }

    public Matrix4f[] getViewport2() {
        return new Matrix4f[]{viewport_matrix, viewport_translation, viewport_rotation};
    }

    public ArrayList<Primitive> getPrimitives() {
        return primitives;
    }

    /**
     * Zooming in
     */
    public void zoomIn() {
        float old = getZoom();
        zoom_exponent++;
        if (zoom_exponent > 20) {
            zoom_exponent = 20;
        }
        setZoom((float) Math.pow(10.0d, zoom_exponent / 10 - 3));
        this.viewport_pixel_per_ldu = this.zoom * View.PIXEL_PER_LDU;
        adjustTranslate(old, getZoom());
    }

    /**
     * Zooming out
     */
    public void zoomOut() {
        float old = getZoom();
        zoom_exponent--;
        if (zoom_exponent < 3) {
            zoom_exponent = 3;
        }
        setZoom((float) Math.pow(10.0d, zoom_exponent / 10 - 3));
        this.viewport_pixel_per_ldu = this.zoom * View.PIXEL_PER_LDU;
        adjustTranslate(old, getZoom());
    }

    private void adjustTranslate(float old, float zoom2) {
        float dx = 0;
        float dy = 0;
        dx = 0f / viewport_pixel_per_ldu;
        dy = 0f / viewport_pixel_per_ldu;
        Vector4f xAxis4f_translation = new Vector4f(dx, 0, 0, 1.0f);
        Vector4f yAxis4f_translation = new Vector4f(0, dy, 0, 1.0f);
        Vector3f xAxis3 = new Vector3f(xAxis4f_translation.x, xAxis4f_translation.y, xAxis4f_translation.z);
        Vector3f yAxis3 = new Vector3f(yAxis4f_translation.x, yAxis4f_translation.y, yAxis4f_translation.z);

        Matrix4f.load(old_viewport_translation, viewport_translation);
        Matrix4f.translate(xAxis3, old_viewport_translation, viewport_translation);
        Matrix4f.translate(yAxis3, viewport_translation, viewport_translation);

        viewport_translation.m30 = 0f;
        if (viewport_translation.m13 > 0f) viewport_translation.m13 = 0f;
        if (-viewport_translation.m31 > maxY) viewport_translation.m31 = -maxY;
    }

    public float getViewport_pixel_per_ldu() {
        return viewport_pixel_per_ldu;
    }

    public void setViewport_pixel_per_ldu(float viewport_pixel_per_ldu) {
        this.viewport_pixel_per_ldu = viewport_pixel_per_ldu;

    }

    public Primitive getSelectedPrimitive() {
        return selectedPrimitive;
    }

    public void setSelectedPrimitive(Primitive selectedPrimitive) {
        this.selectedPrimitive = selectedPrimitive;
        Editor3DWindow.getWindow().updatePrimitiveLabel(selectedPrimitive);
    }

    public Primitive getFocusedPrimitive() {
        return focusedPrimitive;
    }

    public void setFocusedPrimitive(Primitive focusedPrimitive) {
        this.focusedPrimitive = focusedPrimitive;
        Editor3DWindow.getWindow().updatePrimitiveLabel(focusedPrimitive);
    }

    public float getMaxY() {
        return maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

    public void loadPrimitives() {
        try {
            new ProgressMonitorDialog(Editor3DWindow.getWindow().getShell()).run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(I18n.E3D_LoadingPrimitives, IProgressMonitor.UNKNOWN);
                    load(true);
                }
            });
        } catch (InvocationTargetException consumed) {
            load(false);
        } catch (InterruptedException consumed) {
            load(false);
        }
    }

    public void load(boolean waitForRenderer) {
        // Pause primitive renderer
        if (!stopDraw.get() && !dontRefresh.get() && waitForRenderer) {
            stopDraw.set(true);
            hasDrawn.set(false);
            while (!hasDrawn.get()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            hasDrawn.set(false);
            while (!hasDrawn.get()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        stopDraw.set(true);

        CompletableFuture.runAsync(() -> {

            loadingLock.lock();

            try {
                setFocusedPrimitive(null);
                setSelectedPrimitive(null);
                primitives.clear();

                ArrayList<String> searchPaths = new ArrayList<String>();
                String ldrawPath = WorkbenchManager.getUserSettingState().getLdrawFolderPath();
                if (ldrawPath != null) {
                    searchPaths.add(ldrawPath + File.separator + "p" + File.separator); //$NON-NLS-1$
                    searchPaths.add(ldrawPath + File.separator + "P" + File.separator); //$NON-NLS-1$
                    searchPaths.add(ldrawPath + File.separator + "p" + File.separator + "48" + File.separator); //$NON-NLS-1$ //$NON-NLS-2$
                    searchPaths.add(ldrawPath + File.separator + "P" + File.separator + "48" + File.separator); //$NON-NLS-1$ //$NON-NLS-2$
                    searchPaths.add(ldrawPath + File.separator + "p" + File.separator + "8" + File.separator); //$NON-NLS-1$ //$NON-NLS-2$
                    searchPaths.add(ldrawPath + File.separator + "P" + File.separator + "8" + File.separator); //$NON-NLS-1$ //$NON-NLS-2$
                }
                String unofficial = WorkbenchManager.getUserSettingState().getUnofficialFolderPath();
                if (unofficial != null) {
                    searchPaths.add(unofficial + File.separator + "p" + File.separator); //$NON-NLS-1$
                    searchPaths.add(unofficial + File.separator + "P" + File.separator); //$NON-NLS-1$
                    searchPaths.add(unofficial + File.separator + "p" + File.separator + "48" + File.separator); //$NON-NLS-1$ //$NON-NLS-2$
                    searchPaths.add(unofficial + File.separator + "P" + File.separator + "48" + File.separator); //$NON-NLS-1$ //$NON-NLS-2$
                    searchPaths.add(unofficial + File.separator + "p" + File.separator + "8" + File.separator); //$NON-NLS-1$ //$NON-NLS-2$
                    searchPaths.add(unofficial + File.separator + "P" + File.separator + "8" + File.separator); //$NON-NLS-1$ //$NON-NLS-2$
                }
                String project = Project.getProjectPath();
                if (project != null) {
                    searchPaths.add(project + File.separator + "p" + File.separator); //$NON-NLS-1$
                    searchPaths.add(project + File.separator + "P" + File.separator); //$NON-NLS-1$
                    searchPaths.add(project + File.separator + "p" + File.separator + "48" + File.separator); //$NON-NLS-1$ //$NON-NLS-2$
                    searchPaths.add(project + File.separator + "P" + File.separator + "48" + File.separator); //$NON-NLS-1$ //$NON-NLS-2$
                    searchPaths.add(project + File.separator + "p" + File.separator + "8" + File.separator); //$NON-NLS-1$ //$NON-NLS-2$
                    searchPaths.add(project + File.separator + "P" + File.separator + "8" + File.separator); //$NON-NLS-1$ //$NON-NLS-2$
                }

                HashMap<String, Primitive> titleMap = new HashMap<String, Primitive>();
                HashMap<String, String> leavesTitleMap = new HashMap<String, String>();
                HashMap<String, Primitive> primitiveMap = new HashMap<String, Primitive>();
                HashMap<String, Primitive> categoryMap = new HashMap<String, Primitive>();
                HashMap<String, Primitive> leavesMap = new HashMap<String, Primitive>();
                HashMap<String, ArrayList<PrimitiveRule>> leavesRulesMap = new HashMap<String, ArrayList<PrimitiveRule>>();
                final String lowResSuffix = File.separator + "8" + File.separator; //$NON-NLS-1$
                final String hiResSuffix = File.separator + "48" + File.separator; //$NON-NLS-1$
                try {

                    // Creating the categories / Rules
                    // "primitive_rules.txt" is not stored in the AppData\LDPartEditor folder on Windows
                    // It is considered to be "read-only" by the application.
                    File rulesFile = new File("primitive_rules.txt"); //$NON-NLS-1$
                    if (rulesFile.exists() && rulesFile.isFile()) {
                        UTF8BufferedReader reader = null;
                        try {
                            reader = new UTF8BufferedReader(rulesFile.getAbsolutePath());
                            String line ;
                            while ((line = reader.readLine()) != null) {
                                NLogger.debug(getClass(), "Primitive Rule__{0}", line); //$NON-NLS-1$
                                line = line.trim();
                                if (line.startsWith("%")) continue; //$NON-NLS-1$
                                String[] data_segments = line.trim().split(Pattern.quote(";")); //$NON-NLS-1$
                                for (int i = 0; i < data_segments.length; i++) {
                                    data_segments[i] = data_segments[i].trim();
                                }
                                if (data_segments.length > 1)
                                {
                                    String[] tree_segments = data_segments[0].split(Pattern.quote("|")); //$NON-NLS-1$
                                    String catID = ""; //$NON-NLS-1$
                                    final int maxDepth = tree_segments.length;
                                    int depth = 0;
                                    for (String s : tree_segments) {
                                        depth++;
                                        String before = catID;
                                        catID = catID + "|" + s; //$NON-NLS-1$
                                        NLogger.debug(getClass(), "Category       __{0}", catID); //$NON-NLS-1$
                                        if (maxDepth < 2) {
                                            // MARK Parse rules I
                                            ArrayList<PrimitiveRule> rules = new ArrayList<PrimitiveRule>();
                                            for (int i = 1; i < data_segments.length; i++) {
                                                data_segments[i] = data_segments[i].replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                                data_segments[i] = data_segments[i].replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$
                                                int searchIndex = 0;
                                                boolean hasAnd = false;
                                                boolean hasNot = false;
                                                if (data_segments[i].startsWith("AND ")) { //$NON-NLS-1$
                                                    searchIndex = 4;
                                                    hasAnd = true;
                                                }
                                                if (data_segments[i].startsWith("OR ", searchIndex)) { //$NON-NLS-1$
                                                    searchIndex += 3;
                                                    hasAnd = false;
                                                }
                                                if (data_segments[i].startsWith("NOT ", searchIndex)) { //$NON-NLS-1$
                                                    searchIndex += 4;
                                                    hasNot = true;
                                                }
                                                String criteria = ""; //$NON-NLS-1$
                                                if (data_segments[i].startsWith("Order by ", searchIndex)) { //$NON-NLS-1$
                                                    searchIndex += 9;
                                                    if (data_segments[i].startsWith("fraction", searchIndex)) { //$NON-NLS-1$
                                                        rules.add(new PrimitiveRule(Rule.FILENAME_ORDER_BY_FRACTION));
                                                    } else if (data_segments[i].startsWith("last number", searchIndex)) { //$NON-NLS-1$
                                                        rules.add(new PrimitiveRule(Rule.FILENAME_ORDER_BY_LASTNUMBER));
                                                    } else if (data_segments[i].startsWith("alphabet", searchIndex)) { //$NON-NLS-1$
                                                        rules.add(new PrimitiveRule(Rule.FILENAME_ORDER_BY_ALPHABET_WO_NUMBERS));
                                                    }
                                                } else if (data_segments[i].startsWith("Filename ", searchIndex)) { //$NON-NLS-1$
                                                    searchIndex += 9;
                                                    if (data_segments[i].startsWith("starts with ", searchIndex)) { //$NON-NLS-1$
                                                        try {
                                                            criteria = data_segments[i].substring(searchIndex + 13, data_segments[i].length() - 1);
                                                            rules.add(new PrimitiveRule(Rule.FILENAME_STARTS_WITH, criteria, hasAnd, hasNot));
                                                        } catch (IndexOutOfBoundsException consumed) {}
                                                    } else if (data_segments[i].startsWith("ends with ", searchIndex)) { //$NON-NLS-1$
                                                        try {
                                                            criteria = data_segments[i].substring(searchIndex + 11, data_segments[i].length() - 1);
                                                            rules.add(new PrimitiveRule(Rule.FILENAME_ENDS_WITH, criteria, hasAnd, hasNot));
                                                        } catch (IndexOutOfBoundsException consumed) {}
                                                    } else if (data_segments[i].startsWith("contains ", searchIndex)) { //$NON-NLS-1$
                                                        try {
                                                            criteria = data_segments[i].substring(searchIndex + 10, data_segments[i].length() - 1);
                                                            rules.add(new PrimitiveRule(Rule.FILENAME_CONTAINS, criteria, hasAnd, hasNot));
                                                        } catch (IndexOutOfBoundsException consumed) {}
                                                    } else if (data_segments[i].startsWith("matches ", searchIndex)) { //$NON-NLS-1$
                                                        try {
                                                            criteria = data_segments[i].substring(searchIndex + 9, data_segments[i].length() - 1);
                                                            rules.add(new PrimitiveRule(Rule.FILENAME_MATCHES, criteria, hasAnd, hasNot));
                                                        } catch (IndexOutOfBoundsException consumed) {}
                                                    }
                                                } else if (data_segments[i].startsWith("Starts with ", searchIndex)) { //$NON-NLS-1$
                                                    try {
                                                        criteria = data_segments[i].substring(searchIndex + 13, data_segments[i].length() - 1);
                                                        rules.add(new PrimitiveRule(Rule.STARTS_WITH, criteria, hasAnd, hasNot));
                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                } else if (data_segments[i].startsWith("Ends with ", searchIndex)) { //$NON-NLS-1$
                                                    try {
                                                        criteria = data_segments[i].substring(searchIndex + 11, data_segments[i].length() - 1);
                                                        rules.add(new PrimitiveRule(Rule.ENDS_WITH, criteria, hasAnd, hasNot));
                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                } else if (data_segments[i].startsWith("Contains ", searchIndex)) { //$NON-NLS-1$
                                                    try {
                                                        criteria = data_segments[i].substring(searchIndex + 10, data_segments[i].length() - 1);
                                                        rules.add(new PrimitiveRule(Rule.CONTAINS, criteria, hasAnd, hasNot));
                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                } else if (data_segments[i].startsWith("Matches ", searchIndex)) { //$NON-NLS-1$
                                                    try {
                                                        criteria = data_segments[i].substring(searchIndex + 9, data_segments[i].length() - 1);
                                                        rules.add(new PrimitiveRule(Rule.MATCHES, criteria, hasAnd, hasNot));
                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                } else if (data_segments[i].startsWith("Title ", searchIndex)) { //$NON-NLS-1$
                                                    try {
                                                        criteria = data_segments[i].substring(searchIndex + 7, data_segments[i].length() - 1);
                                                        leavesTitleMap.put(catID, criteria);
                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                }
                                            }
                                            leavesRulesMap.put(catID, rules);
                                            Primitive firstParent = new Primitive(true);
                                            firstParent.setName(s.trim());
                                            categoryMap.put(catID, firstParent);
                                            leavesMap.put(catID, firstParent);
                                            primitives.add(firstParent);
                                        } else {
                                            if (!categoryMap.containsKey(catID)) {
                                                if (categoryMap.containsKey(before)) {
                                                    Primitive parent = categoryMap.get(before);
                                                    Primitive child = new Primitive(true);
                                                    child.setName(s.trim());
                                                    parent.getCategories().add(child);
                                                    categoryMap.put(catID, child);
                                                    if (depth == maxDepth) {
                                                        // MARK Parse rules II
                                                        ArrayList<PrimitiveRule> rules = new ArrayList<PrimitiveRule>();
                                                        for (int i = 1; i < data_segments.length; i++) {
                                                            data_segments[i] = data_segments[i].replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                                            data_segments[i] = data_segments[i].replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$
                                                            int searchIndex = 0;
                                                            boolean hasAnd = false;
                                                            boolean hasNot = false;
                                                            if (data_segments[i].startsWith("AND ")) { //$NON-NLS-1$
                                                                searchIndex = 4;
                                                                hasAnd = true;
                                                            }
                                                            if (data_segments[i].startsWith("OR ", searchIndex)) { //$NON-NLS-1$
                                                                searchIndex += 3;
                                                                hasAnd = false;
                                                            }
                                                            if (data_segments[i].startsWith("NOT ", searchIndex)) { //$NON-NLS-1$
                                                                searchIndex += 4;
                                                                hasNot = true;
                                                            }

                                                            String criteria = ""; //$NON-NLS-1$
                                                            if (data_segments[i].startsWith("Order by ", searchIndex)) { //$NON-NLS-1$
                                                                searchIndex += 9;
                                                                if (data_segments[i].startsWith("fraction", searchIndex)) { //$NON-NLS-1$
                                                                    rules.add(new PrimitiveRule(Rule.FILENAME_ORDER_BY_FRACTION));
                                                                } else if (data_segments[i].startsWith("last number", searchIndex)) { //$NON-NLS-1$
                                                                    rules.add(new PrimitiveRule(Rule.FILENAME_ORDER_BY_LASTNUMBER));
                                                                } else if (data_segments[i].startsWith("alphabet", searchIndex)) { //$NON-NLS-1$
                                                                    rules.add(new PrimitiveRule(Rule.FILENAME_ORDER_BY_ALPHABET_WO_NUMBERS));
                                                                }
                                                            } else if (data_segments[i].startsWith("Filename ", searchIndex)) { //$NON-NLS-1$
                                                                searchIndex += 9;
                                                                if (data_segments[i].startsWith("starts with ", searchIndex)) { //$NON-NLS-1$
                                                                    try {
                                                                        criteria = data_segments[i].substring(searchIndex + 13, data_segments[i].length() - 1);
                                                                        rules.add(new PrimitiveRule(Rule.FILENAME_STARTS_WITH, criteria, hasAnd, hasNot));
                                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                                } else if (data_segments[i].startsWith("ends with ", searchIndex)) { //$NON-NLS-1$
                                                                    try {
                                                                        criteria = data_segments[i].substring(searchIndex + 11, data_segments[i].length() - 1);
                                                                        rules.add(new PrimitiveRule(Rule.FILENAME_ENDS_WITH, criteria, hasAnd, hasNot));
                                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                                } else if (data_segments[i].startsWith("contains ", searchIndex)) { //$NON-NLS-1$
                                                                    try {
                                                                        criteria = data_segments[i].substring(searchIndex + 10, data_segments[i].length() - 1);
                                                                        rules.add(new PrimitiveRule(Rule.FILENAME_CONTAINS, criteria, hasAnd, hasNot));
                                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                                } else if (data_segments[i].startsWith("matches ", searchIndex)) { //$NON-NLS-1$
                                                                    try {
                                                                        criteria = data_segments[i].substring(searchIndex + 9, data_segments[i].length() - 1);
                                                                        rules.add(new PrimitiveRule(Rule.FILENAME_MATCHES, criteria, hasAnd, hasNot));
                                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                                }
                                                            } else if (data_segments[i].startsWith("Starts with ", searchIndex)) { //$NON-NLS-1$
                                                                try {
                                                                    criteria = data_segments[i].substring(searchIndex + 13, data_segments[i].length() - 1);
                                                                    rules.add(new PrimitiveRule(Rule.STARTS_WITH, criteria, hasAnd, hasNot));
                                                                } catch (IndexOutOfBoundsException consumed) {}
                                                            } else if (data_segments[i].startsWith("Ends with ", searchIndex)) { //$NON-NLS-1$
                                                                try {
                                                                    criteria = data_segments[i].substring(searchIndex + 11, data_segments[i].length() - 1);
                                                                    rules.add(new PrimitiveRule(Rule.ENDS_WITH, criteria, hasAnd, hasNot));
                                                                } catch (IndexOutOfBoundsException consumed) {}
                                                            } else if (data_segments[i].startsWith("Contains ", searchIndex)) { //$NON-NLS-1$
                                                                try {
                                                                    criteria = data_segments[i].substring(searchIndex + 10, data_segments[i].length() - 1);
                                                                    rules.add(new PrimitiveRule(Rule.CONTAINS, criteria, hasAnd, hasNot));
                                                                } catch (IndexOutOfBoundsException consumed) {}
                                                            } else if (data_segments[i].startsWith("Matches ", searchIndex)) { //$NON-NLS-1$
                                                                try {
                                                                    criteria = data_segments[i].substring(searchIndex + 9, data_segments[i].length() - 1);
                                                                    rules.add(new PrimitiveRule(Rule.MATCHES, criteria, hasAnd, hasNot));
                                                                } catch (IndexOutOfBoundsException consumed) {}
                                                            } else if (data_segments[i].startsWith("Title ", searchIndex)) { //$NON-NLS-1$
                                                                try {
                                                                    criteria = data_segments[i].substring(searchIndex + 7, data_segments[i].length() - 1);
                                                                    leavesTitleMap.put(catID, criteria);
                                                                } catch (IndexOutOfBoundsException consumed) {}
                                                            }
                                                        }
                                                        leavesMap.put(catID, child);
                                                        leavesRulesMap.put(catID, rules);
                                                    }
                                                } else {
                                                    Primitive firstParent = new Primitive(true);
                                                    firstParent.setName(s.trim());
                                                    categoryMap.put(catID, firstParent);
                                                    primitives.add(firstParent);
                                                }
                                            }
                                        }
                                    }
                                }
                                // line = line.replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        } catch (LDParsingException e) {
                        } catch (FileNotFoundException e) {
                        } catch (UnsupportedEncodingException e) {
                        } finally {
                            try {
                                if (reader != null)
                                    reader.close();
                            } catch (LDParsingException e1) {
                            }
                        }
                    }

                    boolean isUppercase = false;
                    boolean isEmpty = true;
                    for (String folderPath : searchPaths) {
                        File libFolder = new File(folderPath);
                        if (!libFolder.isDirectory()) {
                            isUppercase = !isUppercase;
                            continue;
                        }
                        UTF8BufferedReader reader = null;
                        File[] files = libFolder.listFiles();
                        if (files == null) {
                            isUppercase = !isUppercase;
                            continue;
                        }
                        if (isUppercase && !isEmpty) {
                            isUppercase = false;
                            continue;
                        }
                        isEmpty = true;
                        isUppercase = !isUppercase;

                        HashMap<PGTimestamp, PGTimestamp> hotMap = new HashMap<PGTimestamp, PGTimestamp>();
                        for (PGTimestamp ts : fileCache.keySet()) {
                            hotMap.put(ts, ts);
                        }

                        for (File f : files) {
                            final String fileName = f.getName();
                            if (f.isFile() && fileName.matches(".*.dat")) { //$NON-NLS-1$
                                final String path = f.getAbsolutePath();
                                PGTimestamp new_ts = new PGTimestamp(path, f.lastModified());
                                PGTimestamp ts = hotMap.get(new_ts);
                                ArrayList<String> filedata;
                                if (ts != null && ts.isHot() && fileCache.containsKey(ts)) {
                                    filedata = fileCache.get(ts);
                                    fileCacheHits.add(ts);
                                    final int s = filedata.size();
                                    Primitive newPrimitive = new Primitive();
                                    ArrayList<PGData> data = new ArrayList<PGData>();
                                    String description = ""; //$NON-NLS-1$
                                    PGData gd = null;
                                    if (s > 0) {
                                        String line = filedata.get(0);
                                        data.add(new PGDataInit());
                                        if (line.trim().startsWith("0")) { //$NON-NLS-1$
                                            description = line.trim();
                                            if (description.length() > 2) {
                                                description = description.substring(1).trim();
                                                if (description.startsWith("~")) continue; //$NON-NLS-1$
                                            }
                                        } else if ((gd = parseLine(line, 0, View.ID, new HashSet<String>(), hotMap)) != null) {
                                            data.add(gd);
                                        }
                                        final HashSet<String> set = new HashSet<String>();
                                        for (int i = 1; i < s; i++) {
                                            gd = parseLine(filedata.get(i), 0, View.ID, set, hotMap);
                                            if (gd != null && gd.type() != 0) {
                                                data.add(gd);
                                            }
                                            set.clear();
                                        }
                                    }
                                    newPrimitive.setGraphicalData(data);
                                    primitiveMap.put(path, newPrimitive);
                                    if (folderPath.endsWith(hiResSuffix)) {
                                        newPrimitive.setName("48\\" + fileName); //$NON-NLS-1$
                                    } else if (folderPath.endsWith(lowResSuffix)) {
                                        newPrimitive.setName("8\\" + fileName); //$NON-NLS-1$
                                    } else {
                                        newPrimitive.setName(fileName);
                                    }
                                    newPrimitive.setDescription(description);
                                    newPrimitive.calculateZoom();
                                    titleMap.put(newPrimitive.getName(), newPrimitive);
                                    isEmpty = false;
                                } else {
                                    filedata = new ArrayList<String>();
                                    if (ts != null) {
                                        fileCache.remove(ts);
                                    }
                                    try {
                                        Primitive newPrimitive = new Primitive();
                                        ArrayList<PGData> data = new ArrayList<PGData>();
                                        String description = ""; //$NON-NLS-1$
                                        reader = new UTF8BufferedReader(path);
                                        String line;
                                        line = reader.readLine();
                                        if (line != null) {
                                            filedata.add(line);
                                            data.add(new PGDataInit());
                                            PGData gd;
                                            if (line.trim().startsWith("0")) { //$NON-NLS-1$
                                                description = line.trim();
                                                if (description.length() > 2) {
                                                    description = description.substring(1).trim();
                                                    if (description.startsWith("~")) continue; //$NON-NLS-1$
                                                }
                                            } else if ((gd = parseLine(line, 0, View.ID, new HashSet<String>(), hotMap)) != null) {
                                                data.add(gd);
                                            }
                                            final HashSet<String> set = new HashSet<String>();
                                            while ((line = reader.readLine()) != null) {
                                                gd = parseLine(line, 0, View.ID, set, hotMap);
                                                if (gd != null && gd.type() != 0) {
                                                    filedata.add(line);
                                                    data.add(gd);
                                                }
                                                set.clear();
                                            }
                                            newPrimitive.setGraphicalData(data);
                                            primitiveMap.put(path, newPrimitive);
                                            if (folderPath.endsWith(hiResSuffix)) {
                                                newPrimitive.setName("48\\" + fileName); //$NON-NLS-1$
                                            } else if (folderPath.endsWith(lowResSuffix)) {
                                                newPrimitive.setName("8\\" + fileName); //$NON-NLS-1$
                                            } else {
                                                newPrimitive.setName(fileName);
                                            }
                                            newPrimitive.setDescription(description);
                                            newPrimitive.calculateZoom();
                                            titleMap.put(newPrimitive.getName(), newPrimitive);
                                            isEmpty = false;
                                        }
                                    } catch (LDParsingException e) {
                                    } catch (FileNotFoundException e) {
                                    } catch (UnsupportedEncodingException e) {
                                    } finally {
                                        try {
                                            if (reader != null)
                                                reader.close();
                                        } catch (LDParsingException e1) {
                                        }
                                    }
                                    new_ts = new PGTimestamp(path, f.lastModified());
                                    fileCache.put(new_ts, filedata);
                                    fileCacheHits.add(new_ts);
                                }
                            }
                        }
                    }
                } catch (SecurityException consumed) {

                }

                // Clear superflous cache data
                {
                    HashSet<PGTimestamp> toRemove = new HashSet<PGTimestamp>();
                    for (PGTimestamp t : fileCache.keySet()) {
                        if (!fileCacheHits.contains(t)) {
                            toRemove.add(t);
                        }
                    }
                    for (PGTimestamp t : toRemove) {
                        if (fileCache.containsKey(t)) {
                            fileCache.get(t).clear();
                        }
                        fileCache.remove(t);
                    }
                    fileCacheHits.clear();
                }

                // Set category titles
                for (String catKey : leavesMap.keySet()) {
                    String key = leavesTitleMap.get(catKey);
                    if (key == null) continue;
                    Primitive title = titleMap.get(key);
                    if (title == null) continue;
                    Primitive cat = leavesMap.get(catKey);
                    cat.setZoom(title.getZoom());
                    cat.setGraphicalData(title.getGraphicalData());
                }
                // Check which primitves belong in which category
                for (String key : primitiveMap.keySet()) {
                    final Primitive p = primitiveMap.get(key);
                    boolean matched = false;
                    for (String catKey : leavesMap.keySet()) {
                        ArrayList<PrimitiveRule> rules = leavesRulesMap.get(catKey);
                        if (rules.isEmpty()) continue;
                        boolean andCummulative = true;
                        boolean orWasPrevious = true;
                        int index = 0;
                        for (PrimitiveRule r : rules) {
                            if (r.isFunction()) continue;
                            boolean match = r.matches(p) ^ r.isNot();
                            // OR *match* -> Criteria is valid
                            if (!match && !r.isAnd()) {
                                if (index + 1 < rules.size()) {
                                    if (rules.get(index + 1).isAnd()) {
                                        andCummulative = false;
                                        continue;
                                    }
                                }
                            }
                            if (match && !r.isAnd()) {
                                if (index + 1 < rules.size()) {
                                    if (!rules.get(index + 1).isAnd()) {
                                        matched = true;
                                        Primitive cat = leavesMap.get(catKey);
                                        cat.getCategories().add(p);
                                        break;
                                    } else {
                                        andCummulative = true;
                                        continue;
                                    }
                                } else {
                                    matched = true;
                                    Primitive cat = leavesMap.get(catKey);
                                    cat.getCategories().add(p);
                                    break;
                                }
                            }
                            if (r.isAnd()) {
                                andCummulative = andCummulative && match;
                            } else {
                                if (andCummulative && !orWasPrevious) {
                                    matched = true;
                                    Primitive cat = leavesMap.get(catKey);
                                    cat.getCategories().add(p);
                                    break;
                                }
                                andCummulative = true;
                            }
                            orWasPrevious = !r.isAnd();
                            index++;
                        }
                        if (matched) break;
                        if (andCummulative) {
                            int j = 1;
                            while (rules.get(rules.size() - j).isFunction()) {
                                j++;
                                if (j > rules.size()) {
                                    j--;
                                    break;
                                }
                            }
                            if (rules.get(rules.size() - j).isAnd()) {
                                matched = true;
                                Primitive cat = leavesMap.get(catKey);
                                cat.getCategories().add(p);
                                break;
                            }
                        }
                    }
                    if (!matched) {
                        primitives.add(p);
                    }
                }
                // Sort the categories
                for (String catKey : leavesMap.keySet()) {
                    Primitive cat = leavesMap.get(catKey);
                    ArrayList<PrimitiveRule> rules = leavesRulesMap.get(catKey);
                    boolean hasSpecialOrder = false;
                    for (PrimitiveRule rule : rules) {
                        final Rule r = rule.getRule();
                        switch (r) {
                        case FILENAME_ORDER_BY_ALPHABET_WO_NUMBERS:
                        case FILENAME_ORDER_BY_FRACTION:
                        case FILENAME_ORDER_BY_LASTNUMBER:
                            hasSpecialOrder = true;
                            cat.sort(r);
                            break;
                        default:
                            break;
                        }
                    }
                    if (!hasSpecialOrder) {
                        cat.sort(Rule.FILENAME_ORDER_BY_ALPHABET);
                    }
                }
                searchResults.clear();
                Collections.sort(primitives);
                stopDraw.set(false);
            } finally {
                loadingLock.unlock();
            }
        });
    }

    // What follows now is a very minimalistic DAT file parser (<500LOC)

    private static final Pattern WHITESPACE = Pattern.compile("\\s+"); //$NON-NLS-1$
    // private static final PGDataBFC PLACEHOLDER = new PGDataBFC(BFC.PLACEHOLDER);

    public static PGData parseLine(String line, int depth, Matrix4f productMatrix, Set<String> alreadyParsed, HashMap<PGTimestamp, PGTimestamp> hotMap) {

        PGData result = null;

        // Cache Access
        if ((result = cache.get(line)) != null) {
            switch (result.type()) {
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return new PGDataProxy(result);
            }
        }

        final String[] data_segments = WHITESPACE.split(line.trim());
        // Get the linetype
        int linetype = 0;
        char c;
        if (!(data_segments.length > 2 && data_segments[0].length() == 1 && Character.isDigit(c = data_segments[0].charAt(0)))) {
            return null;
        }
        linetype = Character.getNumericValue(c);
        // Parse the line according to its type

        switch (linetype) {
        case 0:
            result = parse_Comment(line, data_segments[1]);
            break;
        case 1:
            return parse_Reference(data_segments, depth, productMatrix, alreadyParsed, hotMap);
        case 2:
            result = parse_Line(data_segments);
            break;
        case 3:
            result = parse_Triangle(data_segments);
            break;
        case 4:
            result = parse_Quad(data_segments);
            break;
        case 5:
            result = parse_Condline(data_segments);
            break;
        }
        if (result == null) {
            return null;
        }
        cache.put(line, result);
        return result;
    }

    private static PGData parse_Comment(String line, String bfc) {
        if ("BFC".equals(bfc)) { //$NON-NLS-1$
            line = WHITESPACE.matcher(line).replaceAll(" ").trim(); //$NON-NLS-1$
            if (line.startsWith("INVERTNEXT", 6)) { //$NON-NLS-1$
                return new PGDataBFC(BFC.INVERTNEXT);
            } else if (line.startsWith("CERTIFY CCW", 6)) { //$NON-NLS-1$
                return new PGDataBFC(BFC.CCW_CLIP);
            } else if (line.startsWith("CERTIFY CW", 6)) { //$NON-NLS-1$
                return new PGDataBFC(BFC.CW_CLIP);
            } else if (line.startsWith("CERTIFY", 6)) { //$NON-NLS-1$
                return new PGDataBFC(BFC.CCW_CLIP);
            } else if (line.startsWith("NOCERTIFY", 6)) { //$NON-NLS-1$
                return new PGDataBFC(BFC.NOCERTIFY);
            } else if (line.startsWith("CCW", 6)) { //$NON-NLS-1$
                return new PGDataBFC(BFC.CCW);
            } else if (line.startsWith("CW", 6)) { //$NON-NLS-1$
                return new PGDataBFC(BFC.CW);
            } else if (line.startsWith("NOCLIP", 6)) { //$NON-NLS-1$
                return new PGDataBFC(BFC.NOCLIP);
            } else if (line.startsWith("CLIP CCW", 6)) { //$NON-NLS-1$
                return new PGDataBFC(BFC.CCW_CLIP);
            } else if (line.startsWith("CLIP CW", 6)) { //$NON-NLS-1$
                return new PGDataBFC(BFC.CW_CLIP);
            } else if (line.startsWith("CCW CLIP", 6)) { //$NON-NLS-1$
                return new PGDataBFC(BFC.CCW_CLIP);
            } else if (line.startsWith("CW CLIP", 6)) { //$NON-NLS-1$
                return new PGDataBFC(BFC.CW_CLIP);
            } else if (line.startsWith("CLIP", 6)) { //$NON-NLS-1$
                return new PGDataBFC(BFC.CLIP);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static PGData parse_Reference(String[] data_segments, int depth, Matrix4f productMatrix, Set<String> alreadyParsed, HashMap<PGTimestamp, PGTimestamp> hotMap) {
        if (data_segments.length < 15) {
            return null;
        } else {
            Matrix4f tMatrix = new Matrix4f();
            float det = 0;
            try {
                tMatrix.m30 = Float.parseFloat(data_segments[2]);
                tMatrix.m31 = Float.parseFloat(data_segments[3]);
                tMatrix.m32 = Float.parseFloat(data_segments[4]);
                tMatrix.m00 = Float.parseFloat(data_segments[5]);
                tMatrix.m10 = Float.parseFloat(data_segments[6]);
                tMatrix.m20 = Float.parseFloat(data_segments[7]);
                tMatrix.m01 = Float.parseFloat(data_segments[8]);
                tMatrix.m11 = Float.parseFloat(data_segments[9]);
                tMatrix.m21 = Float.parseFloat(data_segments[10]);
                tMatrix.m02 = Float.parseFloat(data_segments[11]);
                tMatrix.m12 = Float.parseFloat(data_segments[12]);
                tMatrix.m22 = Float.parseFloat(data_segments[13]);
            } catch (NumberFormatException nfe) {
                return null;
            }
            tMatrix.m33 = 1f;
            // [WARNING] Check file existance
            boolean fileExists = true;
            StringBuilder sb = new StringBuilder();
            for (int s = 14; s < data_segments.length - 1; s++) {
                sb.append(data_segments[s]);
                sb.append(" "); //$NON-NLS-1$
            }
            sb.append(data_segments[data_segments.length - 1]);
            String shortFilename = sb.toString();
            shortFilename = shortFilename.toLowerCase(Locale.ENGLISH);
            try {
                shortFilename = shortFilename.replaceAll("s\\\\", "S" + File.separator).replaceAll("\\\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } catch (Exception e) {
                // Workaround for windows OS / JVM BUG
                shortFilename = shortFilename.replace("s\\", "S" + File.separator).replace("\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            if (alreadyParsed.contains(shortFilename)) {
                return null;
            } else {
                alreadyParsed.add(shortFilename);
            }
            String shortFilename2 = shortFilename.startsWith("S" + File.separator) ? "s" + shortFilename.substring(1) : shortFilename; //$NON-NLS-1$ //$NON-NLS-2$
            File fileToOpen = null;
            String[] prefix = new String[]{Project.getProjectPath(), WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), WorkbenchManager.getUserSettingState().getLdrawFolderPath()};
            String[] middle = new String[]{File.separator + "PARTS", File.separator + "parts", File.separator + "P", File.separator + "p"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            String[] suffix = new String[]{File.separator + shortFilename, File.separator + shortFilename2};
            for (int a1 = 0; a1 < prefix.length; a1++) {
                String s1 = prefix[a1];
                for (int a2 = 0; a2 < middle.length; a2++) {
                    String s2 = middle[a2];
                    for (int a3 = 0; a3 < suffix.length; a3++) {
                        String s3 = suffix[a3];
                        File f = new File(s1 + s2 + s3);
                        fileExists = f.exists() && f.isFile();
                        if (fileExists) {
                            fileToOpen = f;
                            break;
                        }
                    }
                    if (fileExists) break;
                }
                if (fileExists) break;
            }

            ArrayList<String> lines = null;
            String absoluteFilename = null;
            // MARK Virtual file check for project files...
            boolean isVirtual = false;
            for (DatFile df : Project.getUnsavedFiles()) {
                String fn = df.getNewName();
                for (int a1 = 0; a1 < prefix.length; a1++) {
                    String s1 = prefix[a1];
                    for (int a2 = 0; a2 < middle.length; a2++) {
                        String s2 = middle[a2];
                        for (int a3 = 0; a3 < suffix.length; a3++) {
                            String s3 = suffix[a3];
                            if (fn.equals(s1 + s2 + s3)) {
                                lines = new ArrayList<String>();
                                lines.addAll(Arrays.asList(df.getText().split(StringHelper.getLineDelimiter())));
                                absoluteFilename = fn;
                                isVirtual = true;
                                break;
                            }
                        }
                        if (isVirtual) break;
                    }
                    if (isVirtual) break;
                }
                if (isVirtual) break;
            }
            if (isVirtual) {
                det = tMatrix.determinant();
                Matrix4f destMatrix = new Matrix4f();
                Matrix4f.mul(productMatrix, tMatrix, destMatrix);
                final PGData1 result = new PGData1(tMatrix, lines, depth, det < 0,
                        destMatrix, alreadyParsed, hotMap);
                alreadyParsed.remove(shortFilename);
                return result;
            } else if (!fileExists) {
                return null;
            } else {
                absoluteFilename = fileToOpen.getAbsolutePath();
                PGTimestamp new_ts = new PGTimestamp(absoluteFilename, fileToOpen.lastModified());
                PGTimestamp ts = hotMap.get(new_ts);
                if (ts != null && ts.isHot() && fileCache.containsKey(ts)) {
                    lines = fileCache.get(ts);
                    fileCacheHits.add(ts);
                } else {
                    lines = new ArrayList<String>();
                    if (ts != null) {
                        fileCache.remove(ts);
                    }
                    UTF8BufferedReader reader = null;
                    String line = null;
                    try {
                        reader = new UTF8BufferedReader(absoluteFilename);
                        while (true) {
                            line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            lines.add(line);
                        }
                    } catch (FileNotFoundException e1) {
                        return null;
                    } catch (LDParsingException e1) {
                        return null;
                    } catch (UnsupportedEncodingException e1) {
                        return null;
                    } finally {
                        try {
                            if (reader != null)
                                reader.close();
                        } catch (LDParsingException e1) {
                        }
                    }
                    fileCache.put(new_ts, lines);
                    fileCacheHits.add(new_ts);
                }
                det = tMatrix.determinant();
                Matrix4f destMatrix = new Matrix4f();
                Matrix4f.mul(productMatrix, tMatrix, destMatrix);
                final PGData1 result = new PGData1(tMatrix, lines, depth, det < 0,
                        destMatrix, alreadyParsed, hotMap);
                alreadyParsed.remove(shortFilename);
                return result;
            }
        }
    }

    private static PGData parse_Line(String[] data_segments) {
        if (data_segments.length != 8) {
            return null;
        } else {
            final PGData result;
            try {
                result = new PGData2(
                        Float.parseFloat(data_segments[2]), Float.parseFloat(data_segments[3]), Float.parseFloat(data_segments[4]),
                        Float.parseFloat(data_segments[5]), Float.parseFloat(data_segments[6]), Float.parseFloat(data_segments[7]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            return result;
        }
    }

    private static PGData parse_Triangle(String[] data_segments) {
        if (data_segments.length != 11) {
            return null;
        } else {
            final PGData result;
            try {
                result = new PGData3(
                        Float.parseFloat(data_segments[2]), Float.parseFloat(data_segments[3]), Float.parseFloat(data_segments[4]),
                        Float.parseFloat(data_segments[5]), Float.parseFloat(data_segments[6]), Float.parseFloat(data_segments[7]),
                        Float.parseFloat(data_segments[8]), Float.parseFloat(data_segments[9]), Float.parseFloat(data_segments[10]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            return result;
        }
    }

    private static PGData parse_Quad(String[] data_segments) {
        if (data_segments.length != 14) {
            return null;
        } else {
            final PGData result;
            try {
                result = new PGData4(
                        Float.parseFloat(data_segments[2]), Float.parseFloat(data_segments[3]), Float.parseFloat(data_segments[4]),
                        Float.parseFloat(data_segments[5]), Float.parseFloat(data_segments[6]), Float.parseFloat(data_segments[7]),
                        Float.parseFloat(data_segments[8]), Float.parseFloat(data_segments[9]), Float.parseFloat(data_segments[10]),
                        Float.parseFloat(data_segments[11]), Float.parseFloat(data_segments[12]), Float.parseFloat(data_segments[13]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            return result;
        }
    }

    private static PGData parse_Condline(String[] data_segments) {
        if (data_segments.length != 14) {
            return null;
        } else {
            final PGData result;
            try {
                result = new PGData5(
                        Float.parseFloat(data_segments[2]), Float.parseFloat(data_segments[3]), Float.parseFloat(data_segments[4]),
                        Float.parseFloat(data_segments[5]), Float.parseFloat(data_segments[6]), Float.parseFloat(data_segments[7]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            return result;
        }
    }

    public float getRotationWidth() {
        return rotationWidth;
    }

    public void setRotationWidth(float rotationWidth) {
        this.rotationWidth = rotationWidth;
    }

    public boolean isDoingDND() {
        return doingDND;
    }

    public void setDoingDND(boolean doingDND) {
        this.doingDND = doingDND;
    }

    public OpenGLRendererPrimitives getOpenGL() {
        return openGL;
    }

    public void scroll(boolean down) {

        float dy = 0;

        Matrix4f.load(getTranslation(), old_viewport_translation);

        if (down) {
            dy = -37f /  viewport_pixel_per_ldu;
        } else {
            dy = 37f /  viewport_pixel_per_ldu;
        }

        Vector4f yAxis4f_translation = new Vector4f(0, dy, 0, 1.0f);
        Vector3f yAxis3 = new Vector3f(yAxis4f_translation.x, yAxis4f_translation.y, yAxis4f_translation.z);
        Matrix4f.load(old_viewport_translation, viewport_translation);
        Matrix4f.translate(yAxis3, old_viewport_translation, viewport_translation);

        if (viewport_translation.m31 > 0f) viewport_translation.m31 = 0f;
        if (-viewport_translation.m31 > maxY) viewport_translation.m31 = -maxY;

        openGL.drawScene(-1, -1);
    }

    public void setSearchResults(ArrayList<Primitive> results) {
        searchResults = results;
    }

    public ArrayList<Primitive> getSearchResults() {
        return searchResults;
    }

    public void collapseAll( ) {
        for (Primitive p : primitives) {
            p.collapse();
        }
    }

    public static HashMap<String, PGData> getCache() {
        return cache;
    }

    public static void setCache(HashMap<String, PGData> cache) {
        CompositePrimitive.cache = cache;
    }

    public static HashMap<PGTimestamp, ArrayList<String>> getFileCache() {
        return fileCache;
    }

    public static void setFileCache(HashMap<PGTimestamp, ArrayList<String>> file_cache) {
        CompositePrimitive.fileCache = file_cache;
    }

    public boolean stopDraw() {
        return stopDraw.get();
    }

    public void disableRefresh() {
        dontRefresh.set(true);
    }

    public Vector2f getMousePosition() {
        return mouse_position;
    }
}

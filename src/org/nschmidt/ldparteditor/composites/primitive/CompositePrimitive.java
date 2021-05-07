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

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.nio.FloatBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
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
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
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
import org.nschmidt.ldparteditor.dnd.PrimitiveDragAndDropTransfer;
import org.nschmidt.ldparteditor.dnd.PrimitiveDragAndDropType;
import org.nschmidt.ldparteditor.enums.MouseButton;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.Rule;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.helpers.LDPartEditorException;
import org.nschmidt.ldparteditor.helpers.composite3d.MouseActions;
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
    private final GLCanvas canvas;
    private final GLCapabilities capabilities;

    /** The view zoom level */
    private float zoom = WorkbenchManager.getEditor3DWindowState().getPrimitiveZoom();
    private float zoomExponent = WorkbenchManager.getEditor3DWindowState().getPrimitiveZoomExponent();

    /** The transformation matrix of the view */
    private final Matrix4f viewportMatrix = new Matrix4f();
    /** The inverse transformation matrix of the view */
    private Matrix4f viewportMatrixInv = new Matrix4f();
    /** The translation matrix of the view */
    private final Matrix4f viewportTranslation = new Matrix4f();

    private final Matrix4f viewportRotation = new Matrix4f();

    private volatile List<Primitive> primitives = new ArrayList<>();
    private Primitive selectedPrimitive = null;
    private Primitive focusedPrimitive = null;
    private int mouseButtonPressed;
    private final Vector2f oldMousePosition = new Vector2f();
    private final Vector2f mousePosition = new Vector2f();
    /** The old translation matrix of the view [NOT PUBLIC YET] */
    private final Matrix4f oldViewportTranslation = new Matrix4f();
    private final Matrix4f oldViewportRotation = new Matrix4f();

    /** Resolution of the viewport at n% zoom */
    private float viewportPixelPerLDU;

    private float rotationWidth = 300f;

    private volatile AtomicBoolean dontRefresh = new AtomicBoolean(false);
    private volatile AtomicBoolean hasDrawn = new AtomicBoolean(false);
    private volatile AtomicBoolean stopDraw = new AtomicBoolean(true);
    private volatile Lock loadingLock = new ReentrantLock();

    private final KeyStateManager keyboard = new KeyStateManager(this);
    private float maxY = 0f;

    private boolean doingDND;

    private List<Primitive> searchResults = new ArrayList<>();
    private static Map<String, PGData> cache = new HashMap<>();
    private static Map<PGTimestamp, List<String>> fileCache = new HashMap<>();
    private static Set<PGTimestamp> fileCacheHits = new HashSet<>();

    public CompositePrimitive(Composite parent) {
        super(parent, I18n.noBiDirectionalTextStyle() | SWT.BORDER);
        {
            float[] rpf = new float[] {
                    -0.7071f, 0.5f, 0.5f, 0,
                    0, 0.7071f, -0.7071f, 0,
                    -0.7071f, -0.5f, -0.5f, 0,
                    0, 0, 0, 1 };
            FloatBuffer fb = BufferUtils.createFloatBuffer(16);
            fb.put(rpf);
            fb.flip();
            this.viewportRotation.load(fb);
        }

        this.viewportPixelPerLDU = this.zoom * View.PIXEL_PER_LDU;

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
        canvas = new GLCanvas(this, I18n.noBiDirectionalTextStyle(), data);
        canvas.setCurrent();
        if (WorkbenchManager.getUserSettingState().getOpenGLVersion() == 20) {
            capabilities = GL.createCapabilities();
        } else {
            capabilities = GL.createCapabilities(true);
        }
        canvas.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_HAND));
        canvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

        this.setBackgroundMode(SWT.INHERIT_FORCE);

        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

        final DragSource source = new DragSource(canvas, operations);
        widgetUtil(source).setTransfer(PrimitiveDragAndDropTransfer.getInstance());
        source.addDragListener(new DragSourceListener() {
            @Override
            public void dragStart(DragSourceEvent event) {
                event.doit = true;
                setDoingDND(true);
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = new PrimitiveDragAndDropType();
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                setDoingDND(false);
            }
        });

        GL.setCapabilities(capabilities);
        canvas.addDisposeListener(e ->
            canvas.getCursor().dispose()
        );
        // MARK Resize
        canvas.addListener(SWT.Resize, event -> {
            canvas.setCurrent();
            GL.setCapabilities(capabilities);
            Display.getCurrent().timerExec(500, () ->
                openGL.drawScene(-1, -1)
            );
        });

        canvas.addListener(SWT.MouseDown, this::mouseDown);

        canvas.addListener(SWT.MouseDoubleClick, event -> {
            mouseButtonPressed = event.button;
            oldMousePosition.set(event.x, event.y);
            switch (event.button) {
            case MouseButton.LEFT:
                setSelectedPrimitive(getFocusedPrimitive());
                if (getSelectedPrimitive() != null) getSelectedPrimitive().toggle();
                break;
            case MouseButton.MIDDLE:
                break;
            case MouseButton.RIGHT:
                Matrix4f.load(getTranslation(), oldViewportTranslation);
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
                final Object[] messageArguments;
                final boolean hasDefaultMouseButtonLayout;
                hasDefaultMouseButtonLayout = WorkbenchManager.getUserSettingState().getMouseButtonLayout() == MouseActions.MOUSE_LAYOUT_DEFAULT;
                if (hasDefaultMouseButtonLayout) {
                    messageArguments = new Object[]{KeyStateManager.getTaskKeymap().get(Task.MMB)};
                } else {
                    messageArguments = new Object[]{KeyStateManager.getTaskKeymap().get(Task.RMB)};
                }

                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.getLocale());
                formatter.applyPattern(Cocoa.IS_COCOA ? I18n.E3D_ROTATE_VIEW_HINT_MAC :
                    hasDefaultMouseButtonLayout ? I18n.E3D_ROTATE_VIEW_HINT_MIDDLE_MOUSE : I18n.E3D_ROTATE_VIEW_HINT_RIGHT_MOUSE);
                String tooltipText = formatter.format(messageArguments);
                if (!tooltipText.equals(canvas.getToolTipText())) {
                    canvas.setToolTipText(tooltipText);
                }
            }
            mousePosition.set(event.x, event.y);
            switch (mouseButtonPressed) {
            case MouseButton.LEFT:
                break;
            case MouseButton.MIDDLE:
                float rx = 0;
                float ry = 0;
                rx = (event.x - oldMousePosition.x) / rotationWidth * (float) Math.PI;
                ry = (oldMousePosition.y - event.y) / rotationWidth * (float) Math.PI;
                Vector4f xAxis4fRotation = new Vector4f(1.0f, 0, 0, 1.0f);
                Vector4f yAxis4fRotation = new Vector4f(0, 1.0f, 0, 1.0f);
                Matrix4f ovrInverse = Matrix4f.invert(oldViewportRotation, null);
                Matrix4f.transform(ovrInverse, xAxis4fRotation, xAxis4fRotation);
                Matrix4f.transform(ovrInverse, yAxis4fRotation, yAxis4fRotation);
                Vector3f xAxis3fRotation = new Vector3f(xAxis4fRotation.x, xAxis4fRotation.y, xAxis4fRotation.z);
                Vector3f yAxis3fRotation = new Vector3f(yAxis4fRotation.x, yAxis4fRotation.y, yAxis4fRotation.z);
                Matrix4f.rotate(rx, yAxis3fRotation, oldViewportRotation, viewportRotation);
                Matrix4f.rotate(ry, xAxis3fRotation, viewportRotation, viewportRotation);
                break;
            case MouseButton.RIGHT:
                float dx = 0;
                float dy = 0;
                dx = (event.x - oldMousePosition.x) / viewportPixelPerLDU;
                dy = (event.y - oldMousePosition.y) / viewportPixelPerLDU;
                Vector4f xAxis4fTranslation = new Vector4f(dx, 0, 0, 1.0f);
                Vector4f yAxis4fTranslation = new Vector4f(0, dy, 0, 1.0f);
                Vector3f xAxis3 = new Vector3f(xAxis4fTranslation.x, xAxis4fTranslation.y, xAxis4fTranslation.z);
                Vector3f yAxis3 = new Vector3f(yAxis4fTranslation.x, yAxis4fTranslation.y, yAxis4fTranslation.z);
                Matrix4f.load(oldViewportTranslation, viewportTranslation);
                Matrix4f.translate(xAxis3, oldViewportTranslation, viewportTranslation);
                Matrix4f.translate(yAxis3, viewportTranslation, viewportTranslation);

                viewportTranslation.m30 = 0f;
                if (viewportTranslation.m31 > 0f) viewportTranslation.m31 = 0f;
                if (-viewportTranslation.m31 > maxY) viewportTranslation.m31 = -maxY;
                break;
            default:
            }
            openGL.drawScene(event.x, event.y);
        });

        canvas.addListener(SWT.MouseUp, this::mouseUp);
        canvas.addListener(SWT.Paint, event -> openGL.drawScene(-1, -1));
        canvas.addListener(SWT.MouseVerticalWheel, event -> {

            if (Cocoa.checkCtrlOrCmdPressed(event.stateMask)) {
                if (event.count < 0)
                    zoomIn();
                else
                    zoomOut();
            } else {
                float dy = 0;

                Matrix4f.load(getTranslation(), oldViewportTranslation);

                if (event.count < 0) {
                    dy = -17f /  viewportPixelPerLDU;
                } else {
                    dy = 17f /  viewportPixelPerLDU;
                }

                Vector4f yAxis4fTranslation = new Vector4f(0, dy, 0, 1.0f);
                Vector3f yAxis3 = new Vector3f(yAxis4fTranslation.x, yAxis4fTranslation.y, yAxis4fTranslation.z);
                Matrix4f.load(oldViewportTranslation, viewportTranslation);
                Matrix4f.translate(yAxis3, oldViewportTranslation, viewportTranslation);

                if (viewportTranslation.m31 > 0f) viewportTranslation.m31 = 0f;
                if (-viewportTranslation.m31 > maxY) viewportTranslation.m31 = -maxY;
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
        mouseButtonPressed = 0;
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
        reMapMouseEvent(event);
        mouseButtonPressed = event.button;
        oldMousePosition.set(event.x, event.y);
        switch (event.button) {
        case MouseButton.LEFT:
            setSelectedPrimitive(getFocusedPrimitive());
            break;
        case MouseButton.MIDDLE:
            Matrix4f.load(getRotation(), oldViewportRotation);
            break;
        case MouseButton.RIGHT:
            Matrix4f.load(getTranslation(), oldViewportTranslation);
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

    public float getZoomExponent() {
        return zoomExponent;
    }

    public void setZoomExponent(float zoomExponent) {
        this.zoomExponent = zoomExponent;
    }

    /**
     * @return The translation matrix of the view
     */
    public Matrix4f getTranslation() {
        return viewportTranslation;
    }

    public Matrix4f getRotation() {
        return viewportRotation;
    }

    /**
     * @return The transformation matrix of the viewport which was last drawn
     */
    public Matrix4f getViewport() {
        return viewportMatrix;
    }

    public Matrix4f getViewportInverse() {
        return viewportMatrixInv;
    }

    /**
     * Sets the transformation matrix of the viewport
     *
     * @param matrix
     *            the matrix to set.
     */
    public void setViewport(Matrix4f matrix) {
        viewportMatrix.load(matrix);
        viewportMatrixInv = (Matrix4f) matrix.invert();
    }

    public void setViewport2(Matrix4f[] m) {
        viewportMatrix.load(m[0]);
        viewportTranslation.setIdentity(); // .load(m[1]); // Don't load the translation (issue #566)
        viewportRotation.load(m[2]);
        viewportMatrixInv = (Matrix4f) m[0].invert();
    }

    public Matrix4f[] getViewport2() {
        return new Matrix4f[]{viewportMatrix, viewportTranslation, viewportRotation};
    }

    public List<Primitive> getPrimitives() {
        return primitives;
    }

    /**
     * Zooming in
     */
    public void zoomIn() {
        zoomExponent++;
        if (zoomExponent > 20) {
            zoomExponent = 20;
        }
        setZoom((float) Math.pow(10.0d, zoomExponent / 10 - 3));
        this.viewportPixelPerLDU = this.zoom * View.PIXEL_PER_LDU;
        adjustTranslate();
    }

    /**
     * Zooming out
     */
    public void zoomOut() {
        zoomExponent--;
        if (zoomExponent < 3) {
            zoomExponent = 3;
        }
        setZoom((float) Math.pow(10.0d, zoomExponent / 10 - 3));
        this.viewportPixelPerLDU = this.zoom * View.PIXEL_PER_LDU;
        adjustTranslate();
    }

    private void adjustTranslate() {
        float dx = 0;
        float dy = 0;
        dx = 0f / viewportPixelPerLDU;
        dy = 0f / viewportPixelPerLDU;
        Vector4f xAxis4fTranslation = new Vector4f(dx, 0, 0, 1.0f);
        Vector4f yAxis4fTranslation = new Vector4f(0, dy, 0, 1.0f);
        Vector3f xAxis3 = new Vector3f(xAxis4fTranslation.x, xAxis4fTranslation.y, xAxis4fTranslation.z);
        Vector3f yAxis3 = new Vector3f(yAxis4fTranslation.x, yAxis4fTranslation.y, yAxis4fTranslation.z);

        Matrix4f.load(oldViewportTranslation, viewportTranslation);
        Matrix4f.translate(xAxis3, oldViewportTranslation, viewportTranslation);
        Matrix4f.translate(yAxis3, viewportTranslation, viewportTranslation);

        viewportTranslation.m30 = 0f;
        if (viewportTranslation.m13 > 0f) viewportTranslation.m13 = 0f;
        if (-viewportTranslation.m31 > maxY) viewportTranslation.m31 = -maxY;
    }

    public float getViewportPixelPerLDU() {
        return viewportPixelPerLDU;
    }

    public void setViewportPixelPerLDU(float viewportPixelPerLDU) {
        this.viewportPixelPerLDU = viewportPixelPerLDU;

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
                    monitor.beginTask(I18n.E3D_LOADING_PRIMITIVES, IProgressMonitor.UNKNOWN);
                    load(true);
                }
            });
        } catch (InvocationTargetException consumed) {
            load(false);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new LDPartEditorException(ie);
        }
    }

    private void load(boolean waitForRenderer) {
        // Pause primitive renderer
        if (!stopDraw.get() && !dontRefresh.get() && waitForRenderer) {
            stopDraw.set(true);
            hasDrawn.set(false);
            while (!hasDrawn.get()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LDPartEditorException(ie);
                }
            }
            hasDrawn.set(false);
            while (!hasDrawn.get()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LDPartEditorException(ie);
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

                List<String> searchPaths = new ArrayList<>();
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

                Map<String, Primitive> titleMap = new HashMap<>();
                Map<String, String> leavesTitleMap = new HashMap<>();
                Map<String, Primitive> primitiveMap = new HashMap<>();
                Map<String, Primitive> categoryMap = new HashMap<>();
                Map<String, Primitive> leavesMap = new HashMap<>();
                Map<String, List<PrimitiveRule>> leavesRulesMap = new HashMap<>();
                final String lowResSuffix = File.separator + "8" + File.separator; //$NON-NLS-1$
                final String hiResSuffix = File.separator + "48" + File.separator; //$NON-NLS-1$
                try {

                    // Creating the categories / Rules
                    // "primitive_rules.txt" is not stored in the AppData\LDPartEditor folder on Windows
                    // It is considered to be "read-only" by the application.
                    File rulesFile = new File("primitive_rules.txt"); //$NON-NLS-1$
                    if (rulesFile.exists() && rulesFile.isFile()) {
                        try (UTF8BufferedReader reader = new UTF8BufferedReader(rulesFile.getAbsolutePath())) {
                            String line ;
                            while ((line = reader.readLine()) != null) {
                                NLogger.debug(getClass(), "Primitive Rule__{0}", line); //$NON-NLS-1$
                                line = line.trim();
                                if (line.startsWith("%")) continue; //$NON-NLS-1$
                                String[] dataSegments = line.trim().split(Pattern.quote(";")); //$NON-NLS-1$
                                for (int i = 0; i < dataSegments.length; i++) {
                                    dataSegments[i] = dataSegments[i].trim();
                                }
                                if (dataSegments.length > 1)
                                {
                                    String[] treeSegments = dataSegments[0].split(Pattern.quote("|")); //$NON-NLS-1$
                                    final int maxDepth = treeSegments.length;
                                    final StringBuilder sb = new StringBuilder();
                                    String catID = sb.toString();
                                    int depth = 0;
                                    for (String s : treeSegments) {
                                        depth++;
                                        String before = catID;
                                        sb.append('|');
                                        sb.append(s);
                                        catID = sb.toString();
                                        NLogger.debug(getClass(), "Category       __{0}", catID); //$NON-NLS-1$
                                        if (maxDepth < 2) {
                                            // MARK Parse rules I
                                            List<PrimitiveRule> rules = new ArrayList<>();
                                            for (int i = 1; i < dataSegments.length; i++) {
                                                dataSegments[i] = dataSegments[i].replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                                dataSegments[i] = dataSegments[i].replace('_', ' ');
                                                int searchIndex = 0;
                                                boolean hasAnd = false;
                                                boolean hasNot = false;
                                                if (dataSegments[i].startsWith("AND ")) { //$NON-NLS-1$
                                                    searchIndex = 4;
                                                    hasAnd = true;
                                                }
                                                if (dataSegments[i].startsWith("OR ", searchIndex)) { //$NON-NLS-1$
                                                    searchIndex += 3;
                                                    hasAnd = false;
                                                }
                                                if (dataSegments[i].startsWith("NOT ", searchIndex)) { //$NON-NLS-1$
                                                    searchIndex += 4;
                                                    hasNot = true;
                                                }
                                                String criteria = ""; //$NON-NLS-1$
                                                if (dataSegments[i].startsWith("Order by ", searchIndex)) { //$NON-NLS-1$
                                                    searchIndex += 9;
                                                    if (dataSegments[i].startsWith("fraction", searchIndex)) { //$NON-NLS-1$
                                                        rules.add(new PrimitiveRule(Rule.FILENAME_ORDER_BY_FRACTION));
                                                    } else if (dataSegments[i].startsWith("last number", searchIndex)) { //$NON-NLS-1$
                                                        rules.add(new PrimitiveRule(Rule.FILENAME_ORDER_BY_LASTNUMBER));
                                                    } else if (dataSegments[i].startsWith("alphabet", searchIndex)) { //$NON-NLS-1$
                                                        rules.add(new PrimitiveRule(Rule.FILENAME_ORDER_BY_ALPHABET_WO_NUMBERS));
                                                    }
                                                } else if (dataSegments[i].startsWith("Filename ", searchIndex)) { //$NON-NLS-1$
                                                    searchIndex += 9;
                                                    if (dataSegments[i].startsWith("starts with ", searchIndex)) { //$NON-NLS-1$
                                                        try {
                                                            criteria = dataSegments[i].substring(searchIndex + 13, dataSegments[i].length() - 1);
                                                            rules.add(new PrimitiveRule(Rule.FILENAME_STARTS_WITH, criteria, hasAnd, hasNot));
                                                        } catch (IndexOutOfBoundsException consumed) {}
                                                    } else if (dataSegments[i].startsWith("ends with ", searchIndex)) { //$NON-NLS-1$
                                                        try {
                                                            criteria = dataSegments[i].substring(searchIndex + 11, dataSegments[i].length() - 1);
                                                            rules.add(new PrimitiveRule(Rule.FILENAME_ENDS_WITH, criteria, hasAnd, hasNot));
                                                        } catch (IndexOutOfBoundsException consumed) {}
                                                    } else if (dataSegments[i].startsWith("contains ", searchIndex)) { //$NON-NLS-1$
                                                        try {
                                                            criteria = dataSegments[i].substring(searchIndex + 10, dataSegments[i].length() - 1);
                                                            rules.add(new PrimitiveRule(Rule.FILENAME_CONTAINS, criteria, hasAnd, hasNot));
                                                        } catch (IndexOutOfBoundsException consumed) {}
                                                    } else if (dataSegments[i].startsWith("matches ", searchIndex)) { //$NON-NLS-1$
                                                        try {
                                                            criteria = dataSegments[i].substring(searchIndex + 9, dataSegments[i].length() - 1);
                                                            rules.add(new PrimitiveRule(Rule.FILENAME_MATCHES, criteria, hasAnd, hasNot));
                                                        } catch (IndexOutOfBoundsException consumed) {}
                                                    }
                                                } else if (dataSegments[i].startsWith("Starts with ", searchIndex)) { //$NON-NLS-1$
                                                    try {
                                                        criteria = dataSegments[i].substring(searchIndex + 13, dataSegments[i].length() - 1);
                                                        rules.add(new PrimitiveRule(Rule.STARTS_WITH, criteria, hasAnd, hasNot));
                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                } else if (dataSegments[i].startsWith("Ends with ", searchIndex)) { //$NON-NLS-1$
                                                    try {
                                                        criteria = dataSegments[i].substring(searchIndex + 11, dataSegments[i].length() - 1);
                                                        rules.add(new PrimitiveRule(Rule.ENDS_WITH, criteria, hasAnd, hasNot));
                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                } else if (dataSegments[i].startsWith("Contains ", searchIndex)) { //$NON-NLS-1$
                                                    try {
                                                        criteria = dataSegments[i].substring(searchIndex + 10, dataSegments[i].length() - 1);
                                                        rules.add(new PrimitiveRule(Rule.CONTAINS, criteria, hasAnd, hasNot));
                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                } else if (dataSegments[i].startsWith("Matches ", searchIndex)) { //$NON-NLS-1$
                                                    try {
                                                        criteria = dataSegments[i].substring(searchIndex + 9, dataSegments[i].length() - 1);
                                                        rules.add(new PrimitiveRule(Rule.MATCHES, criteria, hasAnd, hasNot));
                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                } else if (dataSegments[i].startsWith("Title ", searchIndex)) { //$NON-NLS-1$
                                                    try {
                                                        criteria = dataSegments[i].substring(searchIndex + 7, dataSegments[i].length() - 1);
                                                        leavesTitleMap.put(catID, criteria);
                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                }
                                            }
                                            leavesRulesMap.put(catID, rules);
                                            Primitive firstParent = Primitive.createPrimitiveCategory();
                                            firstParent.setName(s.trim());
                                            categoryMap.put(catID, firstParent);
                                            leavesMap.put(catID, firstParent);
                                            primitives.add(firstParent);
                                        } else {
                                            if (!categoryMap.containsKey(catID)) {
                                                if (categoryMap.containsKey(before)) {
                                                    Primitive parent = categoryMap.get(before);
                                                    Primitive child = Primitive.createPrimitiveCategory();
                                                    child.setName(s.trim());
                                                    parent.getCategories().add(child);
                                                    categoryMap.put(catID, child);
                                                    if (depth == maxDepth) {
                                                        // MARK Parse rules II
                                                        List<PrimitiveRule> rules = new ArrayList<>();
                                                        for (int i = 1; i < dataSegments.length; i++) {
                                                            dataSegments[i] = dataSegments[i].replaceAll("\\s+", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
                                                            dataSegments[i] = dataSegments[i].replace('_', ' ');
                                                            int searchIndex = 0;
                                                            boolean hasAnd = false;
                                                            boolean hasNot = false;
                                                            if (dataSegments[i].startsWith("AND ")) { //$NON-NLS-1$
                                                                searchIndex = 4;
                                                                hasAnd = true;
                                                            }
                                                            if (dataSegments[i].startsWith("OR ", searchIndex)) { //$NON-NLS-1$
                                                                searchIndex += 3;
                                                                hasAnd = false;
                                                            }
                                                            if (dataSegments[i].startsWith("NOT ", searchIndex)) { //$NON-NLS-1$
                                                                searchIndex += 4;
                                                                hasNot = true;
                                                            }

                                                            String criteria = ""; //$NON-NLS-1$
                                                            if (dataSegments[i].startsWith("Order by ", searchIndex)) { //$NON-NLS-1$
                                                                searchIndex += 9;
                                                                if (dataSegments[i].startsWith("fraction", searchIndex)) { //$NON-NLS-1$
                                                                    rules.add(new PrimitiveRule(Rule.FILENAME_ORDER_BY_FRACTION));
                                                                } else if (dataSegments[i].startsWith("last number", searchIndex)) { //$NON-NLS-1$
                                                                    rules.add(new PrimitiveRule(Rule.FILENAME_ORDER_BY_LASTNUMBER));
                                                                } else if (dataSegments[i].startsWith("alphabet", searchIndex)) { //$NON-NLS-1$
                                                                    rules.add(new PrimitiveRule(Rule.FILENAME_ORDER_BY_ALPHABET_WO_NUMBERS));
                                                                }
                                                            } else if (dataSegments[i].startsWith("Filename ", searchIndex)) { //$NON-NLS-1$
                                                                searchIndex += 9;
                                                                if (dataSegments[i].startsWith("starts with ", searchIndex)) { //$NON-NLS-1$
                                                                    try {
                                                                        criteria = dataSegments[i].substring(searchIndex + 13, dataSegments[i].length() - 1);
                                                                        rules.add(new PrimitiveRule(Rule.FILENAME_STARTS_WITH, criteria, hasAnd, hasNot));
                                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                                } else if (dataSegments[i].startsWith("ends with ", searchIndex)) { //$NON-NLS-1$
                                                                    try {
                                                                        criteria = dataSegments[i].substring(searchIndex + 11, dataSegments[i].length() - 1);
                                                                        rules.add(new PrimitiveRule(Rule.FILENAME_ENDS_WITH, criteria, hasAnd, hasNot));
                                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                                } else if (dataSegments[i].startsWith("contains ", searchIndex)) { //$NON-NLS-1$
                                                                    try {
                                                                        criteria = dataSegments[i].substring(searchIndex + 10, dataSegments[i].length() - 1);
                                                                        rules.add(new PrimitiveRule(Rule.FILENAME_CONTAINS, criteria, hasAnd, hasNot));
                                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                                } else if (dataSegments[i].startsWith("matches ", searchIndex)) { //$NON-NLS-1$
                                                                    try {
                                                                        criteria = dataSegments[i].substring(searchIndex + 9, dataSegments[i].length() - 1);
                                                                        rules.add(new PrimitiveRule(Rule.FILENAME_MATCHES, criteria, hasAnd, hasNot));
                                                                    } catch (IndexOutOfBoundsException consumed) {}
                                                                }
                                                            } else if (dataSegments[i].startsWith("Starts with ", searchIndex)) { //$NON-NLS-1$
                                                                try {
                                                                    criteria = dataSegments[i].substring(searchIndex + 13, dataSegments[i].length() - 1);
                                                                    rules.add(new PrimitiveRule(Rule.STARTS_WITH, criteria, hasAnd, hasNot));
                                                                } catch (IndexOutOfBoundsException consumed) {}
                                                            } else if (dataSegments[i].startsWith("Ends with ", searchIndex)) { //$NON-NLS-1$
                                                                try {
                                                                    criteria = dataSegments[i].substring(searchIndex + 11, dataSegments[i].length() - 1);
                                                                    rules.add(new PrimitiveRule(Rule.ENDS_WITH, criteria, hasAnd, hasNot));
                                                                } catch (IndexOutOfBoundsException consumed) {}
                                                            } else if (dataSegments[i].startsWith("Contains ", searchIndex)) { //$NON-NLS-1$
                                                                try {
                                                                    criteria = dataSegments[i].substring(searchIndex + 10, dataSegments[i].length() - 1);
                                                                    rules.add(new PrimitiveRule(Rule.CONTAINS, criteria, hasAnd, hasNot));
                                                                } catch (IndexOutOfBoundsException consumed) {}
                                                            } else if (dataSegments[i].startsWith("Matches ", searchIndex)) { //$NON-NLS-1$
                                                                try {
                                                                    criteria = dataSegments[i].substring(searchIndex + 9, dataSegments[i].length() - 1);
                                                                    rules.add(new PrimitiveRule(Rule.MATCHES, criteria, hasAnd, hasNot));
                                                                } catch (IndexOutOfBoundsException consumed) {}
                                                            } else if (dataSegments[i].startsWith("Title ", searchIndex)) { //$NON-NLS-1$
                                                                try {
                                                                    criteria = dataSegments[i].substring(searchIndex + 7, dataSegments[i].length() - 1);
                                                                    leavesTitleMap.put(catID, criteria);
                                                                } catch (IndexOutOfBoundsException consumed) {}
                                                            }
                                                        }
                                                        leavesMap.put(catID, child);
                                                        leavesRulesMap.put(catID, rules);
                                                    }
                                                } else {
                                                    Primitive firstParent = Primitive.createPrimitiveCategory();
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
                        } catch (LDParsingException | FileNotFoundException e) {
                            NLogger.error(CompositePrimitive.class, e);
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
                        final File[] files = libFolder.listFiles();
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

                        Map<PGTimestamp, PGTimestamp> hotMap = new HashMap<>();
                        for (PGTimestamp ts : fileCache.keySet()) {
                            hotMap.put(ts, ts);
                        }

                        for (File f : files) {
                            final String fileName = f.getName();
                            if (f.isFile() && fileName.matches(".*.dat")) { //$NON-NLS-1$
                                final String path = f.getAbsolutePath();
                                PGTimestamp newTs = new PGTimestamp(path, f.lastModified());
                                PGTimestamp ts = hotMap.get(newTs);
                                List<String> filedata;
                                if (ts != null && ts.isHot() && fileCache.containsKey(ts)) {
                                    filedata = fileCache.get(ts);
                                    fileCacheHits.add(ts);
                                    final int s = filedata.size();
                                    Primitive newPrimitive = Primitive.createPrimitive();
                                    List<PGData> data = new ArrayList<>();
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
                                        } else if ((gd = parseLine(line, 0, View.ID, new HashSet<>(), hotMap)) != null) {
                                            data.add(gd);
                                        }
                                        final Set<String> set = new HashSet<>();
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
                                    filedata = new ArrayList<>();
                                    if (ts != null) {
                                        fileCache.remove(ts);
                                    }
                                    try (UTF8BufferedReader reader = new UTF8BufferedReader(path)) {
                                        Primitive newPrimitive = Primitive.createPrimitive();
                                        List<PGData> data = new ArrayList<>();
                                        String description = ""; //$NON-NLS-1$
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
                                            } else if ((gd = parseLine(line, 0, View.ID, new HashSet<>(), hotMap)) != null) {
                                                data.add(gd);
                                            }
                                            final Set<String> set = new HashSet<>();
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
                                    } catch (LDParsingException | FileNotFoundException e) {
                                        NLogger.error(CompositePrimitive.class, e);
                                    }
                                    newTs = new PGTimestamp(path, f.lastModified());
                                    fileCache.put(newTs, filedata);
                                    fileCacheHits.add(newTs);
                                }
                            }
                        }
                    }
                } catch (SecurityException consumed) {

                }

                // Clear superflous cache data
                {
                    Set<PGTimestamp> toRemove = new HashSet<>();
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
                for (Entry<String, Primitive> entry : leavesMap.entrySet()) {
                    String catKey = entry.getKey();
                    String key = leavesTitleMap.get(catKey);
                    if (key == null) continue;
                    Primitive title = titleMap.get(key);
                    if (title == null) continue;
                    Primitive cat = entry.getValue();
                    cat.setZoom(title.getZoom());
                    cat.setGraphicalData(title.getGraphicalData());
                }
                // Check which primitves belong in which category
                for (final Primitive p : primitiveMap.values()) {
                    boolean matched = false;
                    for (Entry<String, Primitive> entry : leavesMap.entrySet()) {
                        final String catKey = entry.getKey();
                        List<PrimitiveRule> rules = leavesRulesMap.get(catKey);
                        if (rules.isEmpty()) continue;
                        boolean andCummulative = true;
                        boolean orWasPrevious = true;
                        int index = 0;
                        for (PrimitiveRule r : rules) {
                            if (r.isFunction()) continue;
                            boolean match = r.matches(p) ^ r.isNot();
                            // OR *match* -> Criteria is valid
                            if (!match && !r.isAnd() && index + 1 < rules.size() && rules.get(index + 1).isAnd()) {
                                andCummulative = false;
                                continue;
                            }
                            if (match && !r.isAnd()) {
                                if (index + 1 < rules.size()) {
                                    if (!rules.get(index + 1).isAnd()) {
                                        matched = true;
                                        Primitive cat = entry.getValue();
                                        cat.getCategories().add(p);
                                        break;
                                    } else {
                                        andCummulative = true;
                                        continue;
                                    }
                                } else {
                                    matched = true;
                                    Primitive cat = entry.getValue();
                                    cat.getCategories().add(p);
                                    break;
                                }
                            }
                            if (r.isAnd()) {
                                andCummulative = andCummulative && match;
                            } else {
                                if (andCummulative && !orWasPrevious) {
                                    matched = true;
                                    Primitive cat = entry.getValue();
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
                                Primitive cat = entry.getValue();
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
                for (Entry<String, Primitive> entry : leavesMap.entrySet()) {
                    String catKey = entry.getKey();
                    Primitive cat = entry.getValue();
                    List<PrimitiveRule> rules = leavesRulesMap.get(catKey);
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

    public static PGData parseLine(String line, int depth, Matrix4f productMatrix, Set<String> alreadyParsed, Map<PGTimestamp, PGTimestamp> hotMap) {

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
            default:
                break;
            }
        }

        final String[] dataSegments = WHITESPACE.split(line.trim());
        // Get the linetype
        int linetype = 0;
        char c;
        if (!(dataSegments.length > 2 && dataSegments[0].length() == 1 && Character.isDigit(c = dataSegments[0].charAt(0)))) {
            return null;
        }
        linetype = Character.getNumericValue(c);
        // Parse the line according to its type

        switch (linetype) {
        case 0:
            result = parseComment(line, dataSegments[1]);
            break;
        case 1:
            return parseReference(dataSegments, depth, productMatrix, alreadyParsed, hotMap);
        case 2:
            result = parseLine(dataSegments);
            break;
        case 3:
            result = parseTriangle(dataSegments);
            break;
        case 4:
            result = parseQuad(dataSegments);
            break;
        case 5:
            result = parseCondline(dataSegments);
            break;
        default:
            break;
        }
        if (result == null) {
            return null;
        }
        cache.put(line, result);
        return result;
    }

    private static PGData parseComment(String line, String bfc) {
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

    private static PGData parseReference(String[] dataSegments, int depth, Matrix4f productMatrix, Set<String> alreadyParsed, Map<PGTimestamp, PGTimestamp> hotMap) {
        if (dataSegments.length < 15) {
            return null;
        } else {
            Matrix4f tMatrix = new Matrix4f();
            float det = 0;
            try {
                tMatrix.m30 = Float.parseFloat(dataSegments[2]);
                tMatrix.m31 = Float.parseFloat(dataSegments[3]);
                tMatrix.m32 = Float.parseFloat(dataSegments[4]);
                tMatrix.m00 = Float.parseFloat(dataSegments[5]);
                tMatrix.m10 = Float.parseFloat(dataSegments[6]);
                tMatrix.m20 = Float.parseFloat(dataSegments[7]);
                tMatrix.m01 = Float.parseFloat(dataSegments[8]);
                tMatrix.m11 = Float.parseFloat(dataSegments[9]);
                tMatrix.m21 = Float.parseFloat(dataSegments[10]);
                tMatrix.m02 = Float.parseFloat(dataSegments[11]);
                tMatrix.m12 = Float.parseFloat(dataSegments[12]);
                tMatrix.m22 = Float.parseFloat(dataSegments[13]);
            } catch (NumberFormatException nfe) {
                return null;
            }
            tMatrix.m33 = 1f;
            // [WARNING] Check file existance
            boolean fileExists = true;
            StringBuilder sb = new StringBuilder();
            for (int s = 14; s < dataSegments.length - 1; s++) {
                sb.append(dataSegments[s]);
                sb.append(" "); //$NON-NLS-1$
            }
            sb.append(dataSegments[dataSegments.length - 1]);
            String shortFilename = sb.toString();
            shortFilename = shortFilename.toLowerCase(Locale.ENGLISH);
            shortFilename = shortFilename.replace("s\\", "S" + File.separator).replace("\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

            List<String> lines = null;
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
                                lines = new ArrayList<>();
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
                PGTimestamp newTs = new PGTimestamp(absoluteFilename, fileToOpen.lastModified());
                PGTimestamp ts = hotMap.get(newTs);
                if (ts != null && ts.isHot() && fileCache.containsKey(ts)) {
                    lines = fileCache.get(ts);
                    fileCacheHits.add(ts);
                } else {
                    lines = new ArrayList<>();
                    if (ts != null) {
                        fileCache.remove(ts);
                    }
                    String line = null;
                    try (UTF8BufferedReader reader = new UTF8BufferedReader(absoluteFilename)) {
                        while (true) {
                            line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            lines.add(line);
                        }
                    } catch (FileNotFoundException | LDParsingException e1) {
                        NLogger.error(CompositePrimitive.class, e1);
                        return null;
                    }
                    fileCache.put(newTs, lines);
                    fileCacheHits.add(newTs);
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

    private static PGData parseLine(String[] dataSegments) {
        if (dataSegments.length != 8) {
            return null;
        } else {
            final PGData result;
            try {
                result = new PGData2(
                        Float.parseFloat(dataSegments[2]), Float.parseFloat(dataSegments[3]), Float.parseFloat(dataSegments[4]),
                        Float.parseFloat(dataSegments[5]), Float.parseFloat(dataSegments[6]), Float.parseFloat(dataSegments[7]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            return result;
        }
    }

    private static PGData parseTriangle(String[] dataSegments) {
        if (dataSegments.length != 11) {
            return null;
        } else {
            final PGData result;
            try {
                result = new PGData3(
                        Float.parseFloat(dataSegments[2]), Float.parseFloat(dataSegments[3]), Float.parseFloat(dataSegments[4]),
                        Float.parseFloat(dataSegments[5]), Float.parseFloat(dataSegments[6]), Float.parseFloat(dataSegments[7]),
                        Float.parseFloat(dataSegments[8]), Float.parseFloat(dataSegments[9]), Float.parseFloat(dataSegments[10]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            return result;
        }
    }

    private static PGData parseQuad(String[] dataSegments) {
        if (dataSegments.length != 14) {
            return null;
        } else {
            final PGData result;
            try {
                result = new PGData4(
                        Float.parseFloat(dataSegments[2]), Float.parseFloat(dataSegments[3]), Float.parseFloat(dataSegments[4]),
                        Float.parseFloat(dataSegments[5]), Float.parseFloat(dataSegments[6]), Float.parseFloat(dataSegments[7]),
                        Float.parseFloat(dataSegments[8]), Float.parseFloat(dataSegments[9]), Float.parseFloat(dataSegments[10]),
                        Float.parseFloat(dataSegments[11]), Float.parseFloat(dataSegments[12]), Float.parseFloat(dataSegments[13]));
            } catch (NumberFormatException nfe) {
                return null;
            }
            return result;
        }
    }

    private static PGData parseCondline(String[] dataSegments) {
        if (dataSegments.length != 14) {
            return null;
        } else {
            final PGData result;
            try {
                result = new PGData5(
                        Float.parseFloat(dataSegments[2]), Float.parseFloat(dataSegments[3]), Float.parseFloat(dataSegments[4]),
                        Float.parseFloat(dataSegments[5]), Float.parseFloat(dataSegments[6]), Float.parseFloat(dataSegments[7]));
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

        Matrix4f.load(getTranslation(), oldViewportTranslation);

        if (down) {
            dy = -37f /  viewportPixelPerLDU;
        } else {
            dy = 37f /  viewportPixelPerLDU;
        }

        Vector4f yAxis4fTranslation = new Vector4f(0, dy, 0, 1.0f);
        Vector3f yAxis3 = new Vector3f(yAxis4fTranslation.x, yAxis4fTranslation.y, yAxis4fTranslation.z);
        Matrix4f.load(oldViewportTranslation, viewportTranslation);
        Matrix4f.translate(yAxis3, oldViewportTranslation, viewportTranslation);

        if (viewportTranslation.m31 > 0f) viewportTranslation.m31 = 0f;
        if (-viewportTranslation.m31 > maxY) viewportTranslation.m31 = -maxY;

        openGL.drawScene(-1, -1);
    }

    public void setSearchResults(List<Primitive> results) {
        searchResults = results;
    }

    public List<Primitive> getSearchResults() {
        return searchResults;
    }

    public void collapseAll( ) {
        for (Primitive p : primitives) {
            p.collapse();
        }
    }

    public static Map<String, PGData> getCache() {
        return cache;
    }

    public static void setCache(Map<String, PGData> cache) {
        CompositePrimitive.cache = cache;
    }

    public static Map<PGTimestamp, List<String>> getFileCache() {
        return fileCache;
    }

    public static void setFileCache(Map<PGTimestamp, List<String>> fileCache) {
        CompositePrimitive.fileCache = fileCache;
    }

    public boolean stopDraw() {
        return stopDraw.get();
    }

    public void disableRefresh() {
        dontRefresh.set(true);
    }

    public Vector2f getMousePosition() {
        return mousePosition;
    }

    public Rectangle getScaledBounds() {
        final double factor = WorkbenchManager.getUserSettingState().getViewportScaleFactor();
        final Rectangle bounds = super.getBounds();
        return new Rectangle(bounds.x, bounds.y, (int) (bounds.width * factor), (int) (bounds.height * factor));
    }

    private void reMapMouseEvent(Event event) {
        final int mouseButtonLayout = WorkbenchManager.getUserSettingState().getMouseButtonLayout();
        if (mouseButtonLayout == MouseActions.MOUSE_LAYOUT_SWITCH_ROTATE_AND_TRANSLATE) {
            if (event.button == MouseButton.MIDDLE) {
                event.button = MouseButton.RIGHT;
            } else if (event.button == MouseButton.RIGHT) {
                event.button = MouseButton.MIDDLE;
            }
        }
    }
}

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

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.swt.SWTResourceManager;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.dnd.MyDummyTransfer2;
import org.nschmidt.ldparteditor.dnd.MyDummyType2;
import org.nschmidt.ldparteditor.enums.MouseButton;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.opengl.OpenGLRendererPrimitives;

public class CompositePrimitive extends Composite {

    /** The {@linkplain OpenGLRenderer} instance */
    private final OpenGLRendererPrimitives openGL = new OpenGLRendererPrimitives(this);

    /** the {@linkplain GLCanvas} */
    final GLCanvas canvas;

    /** The view zoom level */
    private float zoom = 0.0039810717f;
    private float zoom_exponent = 6f; // Start with 0.4% zoom

    /** The transformation matrix of the view */
    private final Matrix4f viewport_matrix = new Matrix4f();
    /** The inverse transformation matrix of the view */
    private Matrix4f viewport_matrix_inv = new Matrix4f();
    /** The translation matrix of the view */
    private final Matrix4f viewport_translation = new Matrix4f();

    private ArrayList<Primitive> primitives = new ArrayList<Primitive>();
    private Primitive selectedPrimitive = null;
    private Primitive focusedPrimitive = null;
    private int mouse_button_pressed;
    private final Vector2f old_mouse_position = new Vector2f();
    /** The old translation matrix of the view [NOT PUBLIC YET] */
    private final Matrix4f old_viewport_translation = new Matrix4f();

    /** Resolution of the viewport at n% zoom */
    private float viewport_pixel_per_ldu;

    public CompositePrimitive(Composite parent) {
        super(parent, I18n.I18N_NON_BIDIRECT() | SWT.BORDER);
        // TODO Auto-generated constructor stub

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
        canvas = new GLCanvas(this, I18n.I18N_NON_BIDIRECT(), data);
        canvas.setCurrent();
        canvas.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_HAND));
        canvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

        this.setBackgroundMode(SWT.INHERIT_FORCE);

        for(int i = 0; i < 10; i++) {
            primitives.add(new Primitive());
        }
        setSelectedPrimitive(primitives.get(0));

        Transfer[] types = new Transfer[] { MyDummyTransfer2.getInstance() };
        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

        final DragSource source = new DragSource(canvas, operations);
        source.setTransfer(types);
        source.addDragListener(new DragSourceListener() {
            @Override
            public void dragStart(DragSourceEvent event) {
                event.doit = true;
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = new MyDummyType2();
            }

            @Override
            public void dragFinished(DragSourceEvent event) {

            }
        });

        try {
            GLContext.useContext(canvas);
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
        // MARK Resize
        canvas.addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                canvas.setCurrent();
                try {
                    GLContext.useContext(canvas);
                } catch (LWJGLException e) {
                    e.printStackTrace();
                }
                Display.getCurrent().timerExec(500, new Runnable() {
                    @Override
                    public void run() {
                        openGL.drawScene(10, 10);
                    }
                });
            }
        });

        canvas.addListener(SWT.MouseDown, new Listener() {
            @Override
            // MARK MouseDown
            public void handleEvent(Event event) {
                mouse_button_pressed = event.button;
                old_mouse_position.set(event.x, event.y);
                switch (event.button) {
                case MouseButton.LEFT:
                    setSelectedPrimitive(getFocusedPrimitive());
                    break;
                case MouseButton.MIDDLE:
                    break;
                case MouseButton.RIGHT:
                    Matrix4f.load(getTranslation(), old_viewport_translation);
                    break;
                default:
                }
                openGL.drawScene(event.x, event.y);
            }
        });

        canvas.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            // MARK MouseDown
            public void handleEvent(Event event) {
                mouse_button_pressed = event.button;
                old_mouse_position.set(event.x, event.y);
                switch (event.button) {
                case MouseButton.LEFT:
                    setSelectedPrimitive(getFocusedPrimitive());
                    break;
                case MouseButton.MIDDLE:
                    break;
                case MouseButton.RIGHT:
                    Matrix4f.load(getTranslation(), old_viewport_translation);
                    break;
                default:
                }
                openGL.drawScene(event.x, event.y);
            }
        });

        canvas.addListener(SWT.MouseMove, new Listener() {
            @Override
            // MARK MouseMove
            public void handleEvent(Event event) {
                openGL.drawScene(event.x, event.y);

                switch (mouse_button_pressed) {
                case MouseButton.LEFT:
                    break;
                case MouseButton.MIDDLE:
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
                    break;
                default:
                }
            }
        });

        canvas.addListener(SWT.MouseUp, new Listener() {
            @Override
            // MARK MouseUp
            public void handleEvent(Event event) {
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
        });

        canvas.addListener(SWT.MouseVerticalWheel, new Listener() {
            @Override
            // MARK MouseVerticalWheel
            public void handleEvent(Event event) {

                if ((event.stateMask & SWT.CTRL) == SWT.CTRL) {
                    if (event.count < 0)
                        zoomIn();
                    else
                        zoomOut();
                } else {
                    float dy = 0;

                    Matrix4f.load(getTranslation(), old_viewport_translation);

                    if (event.count < 0) {
                        dy = -11f /  viewport_pixel_per_ldu;
                    } else {
                        dy = 11f /  viewport_pixel_per_ldu;
                    }

                    Vector4f yAxis4f_translation = new Vector4f(0, dy, 0, 1.0f);
                    Vector3f yAxis3 = new Vector3f(yAxis4f_translation.x, yAxis4f_translation.y, yAxis4f_translation.z);
                    Matrix4f.load(old_viewport_translation, viewport_translation);
                    Matrix4f.translate(yAxis3, old_viewport_translation, viewport_translation);

                    if (viewport_translation.m31 > 0f) viewport_translation.m31 = 0f;
                }

                openGL.drawScene(event.x, event.y);


            }
        });

        openGL.init();
        Display.getCurrent().timerExec(3000, new Runnable() {
            @Override
            public void run() {
                openGL.drawScene(10, 10);
            }
        });
    }
    // FIXME Needs implementation!

    public GLCanvas getCanvas() {
        return canvas;
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

    /**
     * @return The translation matrix of the view
     */
    public Matrix4f getTranslation() {
        return viewport_translation;
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
        GData.CACHE_viewByProjection.clear();
        viewport_matrix.load(matrix);
        viewport_matrix_inv = (Matrix4f) matrix.invert();
    }

    public ArrayList<Primitive> getPrimitives() {
        return primitives;
    }

    public void setPrimitives(ArrayList<Primitive> primitives) {
        this.primitives = primitives;
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
    }

    public Primitive getFocusedPrimitive() {
        return focusedPrimitive;
    }

    public void setFocusedPrimitive(Primitive focusedPrimitive) {
        this.focusedPrimitive = focusedPrimitive;
    }

}

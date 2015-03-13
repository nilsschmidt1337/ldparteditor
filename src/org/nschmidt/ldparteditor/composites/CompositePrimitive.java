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
package org.nschmidt.ldparteditor.composites;

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
import org.nschmidt.ldparteditor.dnd.MyDummyTransfer2;
import org.nschmidt.ldparteditor.dnd.MyDummyType2;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.opengl.OpenGLRendererPrimitives;

public class CompositePrimitive extends Composite {

    /** The {@linkplain OpenGLRenderer} instance */
    private final OpenGLRendererPrimitives openGL = new OpenGLRendererPrimitives(this);

    /** the {@linkplain GLCanvas} */
    final GLCanvas canvas;

    public CompositePrimitive(Composite parent) {
        super(parent, I18n.I18N_NON_BIDIRECT() | SWT.BORDER);
        // TODO Auto-generated constructor stub

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
            }
        });
    }
    // FIXME Needs implementation!

}

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
package org.nschmidt.ldparteditor.state;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.swt.SWT;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.composites.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

/**
 * Stores information about keys pressed on the keyboard and triggers keyboard
 * events.
 *
 * @author nils
 *
 */
public class KeyStateManager {

    /** The 3D Composite */
    private final Composite3D c3d;

    private final CompositePrimitive cp;

    /** A set of all keys which are pressed at the moment */
    private HashSet<Integer> pressedKeyCodes = new HashSet<Integer>();

    /** Indicates that SHIFT is pressed */
    private boolean shiftPressed;
    /** Indicates that CTRL is pressed */
    private boolean ctrlPressed;
    /** Indicates that ALT is pressed */
    private boolean altPressed;

    public KeyStateManager(Composite3D c3d) {
        this.c3d = c3d;
        this.cp = null;
    }

    public KeyStateManager(CompositePrimitive cp) {
        this.c3d = null;
        this.cp = cp;
    }



    /**
     * @return {@code true} if SHIFT is pressed
     */
    public boolean isShiftPressed() {
        return shiftPressed;
    }

    /**
     * @return {@code true} if CTRL is pressed
     */
    public boolean isCtrlPressed() {
        return ctrlPressed;
    }

    /**
     * @return {@code true} if ALT is pressed
     */
    public boolean isAltPressed() {
        return altPressed;
    }

    /**
     * Sets the state of released and pressed keys and triggers eventually a
     * function
     *
     * @param keyCode
     *            the Key Code
     * @param keyEventType
     *            the event type can be {@code SWT.KeyDown} or {@code SWT.KeyUp}
     */
    public void setStates(int keyCode, int keyEventType) {
        if (cp == null) {
            // Logic for Composite3D
            if (keyEventType == SWT.KeyDown && !pressedKeyCodes.contains(keyCode)) {
                NLogger.debug(KeyStateManager.class, "[Key (" + keyCode + ") down]"); //$NON-NLS-1$ //$NON-NLS-2$
                setKeyState(keyCode, true);
                final DatFile df = c3d.getLockableDatFileReference();
                final Editor3DWindow win = Editor3DWindow.getWindow();
                final VertexManager vm = df.getVertexManager();

                if (keyCode == SWT.ESC) {
                    // c3d.getModifier().closeView();
                    if (df.getObjVertex1() == null && df.getObjVertex2() == null && df.getObjVertex3() == null && df.getObjVertex4() == null) {
                        win.disableAddAction();
                    }
                    df.setObjVertex1(null);
                    df.setObjVertex2(null);
                    df.setObjVertex3(null);
                    df.setObjVertex4(null);
                    df.setNearestObjVertex1(null);
                    df.setNearestObjVertex2(null);
                    vm.clearSelection();
                }
                if (keyCode == SWT.DEL) {
                    vm.delete(win.isMovingAdjacentData(), true);
                }

                if (keyCode == 'c' && ctrlPressed && !(altPressed || shiftPressed)) {
                    vm.copy();
                } else if (keyCode == 'x' && ctrlPressed && !(altPressed || shiftPressed)) {
                    vm.copy();
                    vm.delete(win.isMovingAdjacentData(), true);
                }else if (keyCode == 'v' && ctrlPressed && !(altPressed || shiftPressed)) {
                    vm.paste();
                    win.setMovingAdjacentData(false);
                }

                if (keyCode == '1' && !(altPressed || shiftPressed || ctrlPressed)) {
                    win.setWorkingAction(WorkingMode.SELECT);
                } else if (keyCode == '2' && !(altPressed || shiftPressed || ctrlPressed)) {
                    win.setWorkingAction(WorkingMode.MOVE);
                } else if (keyCode == '3' && !(altPressed || shiftPressed || ctrlPressed)) {
                    win.setWorkingAction(WorkingMode.ROTATE);
                } else if (keyCode == '4' && !(altPressed || shiftPressed || ctrlPressed)) {
                    win.setWorkingAction(WorkingMode.SCALE);
                } else if (keyCode == 'c' && !(altPressed || shiftPressed || ctrlPressed)) {
                    win.setWorkingAction(WorkingMode.COMBINED);
                }
                pressedKeyCodes.add(keyCode);
            } else if (keyEventType == SWT.KeyUp) {
                NLogger.debug(KeyStateManager.class, "[Key (" + keyCode + ") up]"); //$NON-NLS-1$ //$NON-NLS-2$
                pressedKeyCodes.remove(keyCode);
                setKeyState(keyCode, false);
            }

            // Synchronise key state with other composites
            ArrayList<OpenGLRenderer> r = Editor3DWindow.getRenders();
            for (OpenGLRenderer renderer : r) {
                renderer.getC3D().getKeys().synchronise(this);
            }
        } else {
            // Logic for CompositePrimitive
            if (keyEventType == SWT.KeyDown && !pressedKeyCodes.contains(keyCode)) {
                NLogger.debug(KeyStateManager.class, "[Key (" + keyCode + ") down]"); //$NON-NLS-1$ //$NON-NLS-2$
                if (keyCode == SWT.PAGE_UP || keyCode == SWT.UP) {
                    cp.scroll(false);
                } else if (keyCode == SWT.PAGE_DOWN || keyCode == SWT.DOWN) {
                    cp.scroll(true);
                } else {
                    pressedKeyCodes.add(keyCode);
                }
                setKeyState(keyCode, true);
            } else if (keyEventType == SWT.KeyUp) {
                NLogger.debug(KeyStateManager.class, "[Key (" + keyCode + ") up]"); //$NON-NLS-1$ //$NON-NLS-2$
                pressedKeyCodes.remove(keyCode);
                setKeyState(keyCode, false);
            }
        }
    }

    /**
     * Sets the state of released and pressed keys
     *
     * @param keyCode
     *            the code to evaluate
     * @param isPressed
     *            {@code true} on {@code SWT.KeyDown} events
     */
    public void setKeyState(int keyCode, boolean isPressed) {
        switch (keyCode) {
        case SWT.ALT:
            altPressed = isPressed;
            break;
        case SWT.SHIFT:
            shiftPressed = isPressed;
            break;
        case SWT.CTRL:
            ctrlPressed = isPressed;
            break;
        }
    }

    public void synchronise(KeyStateManager ksm) {
        this.altPressed = ksm.altPressed;
        this.shiftPressed = ksm.shiftPressed;
        this.ctrlPressed = ksm.ctrlPressed;
    }
}

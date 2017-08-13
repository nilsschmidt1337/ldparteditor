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
package org.nschmidt.ldparteditor.workbench;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.eclipse.swt.graphics.Rectangle;
import org.nschmidt.ldparteditor.enums.Colour;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.StringHelper;

/**
 * The manager for loading or creating a default workbench
 *
 * @author nils
 *
 */
public enum WorkbenchManager {
    INSTANCE;

    /** The reference to the 3D editor */
    private static Editor3DWindow editor3DWindow;
    /** The window state of the 3D editor */
    private static Editor3DWindowState editor3DWindowState;
    /** The window state of the text editor */
    private static EditorTextWindowState editorTextWindowState;
    /** The state of the user setting */
    private static UserSettingState userSettingState;
    /** The primitive cache */
    private static PrimitiveCache primitiveCache;

    /**
     * Writes a default config.gz file
     */
    public static void createDefaultWorkbench() {
        primitiveCache = new PrimitiveCache();
        userSettingState = new UserSettingState();
        editor3DWindowState = new Editor3DWindowState();
        editor3DWindowState.setWindowState(new WindowState());
        WindowState w_3d = editor3DWindowState.getWindowState();
        w_3d.setCentered(true);
        w_3d.setMaximized(false);
        w_3d.setSizeAndPosition(new Rectangle(0, 0, 1024, 768));
        editorTextWindowState = new EditorTextWindowState();
        editorTextWindowState.setWindowState(new WindowState());
        WindowState w_text = editorTextWindowState.getWindowState();
        w_text.setCentered(true);
        w_text.setMaximized(false);
        w_text.setSizeAndPosition(new Rectangle(0, 0, 1024, 768));
        Colour.saveDefaultColours();
        saveWorkbench();
    }

    /**
     * Loads the workbench from config.gz
     */
    public static void loadWorkbench() {
        ObjectInputStream configFileStream = null;
        try {
            File configGzFile = new File("config.gz"); //$NON-NLS-1$
            if (configGzFile.exists()) {
                configFileStream = new ObjectInputStream(new GZIPInputStream(new FileInputStream("config.gz"))); //$NON-NLS-1$
                WorkbenchManager.editor3DWindowState = (Editor3DWindowState) configFileStream.readObject();
                WorkbenchManager.editorTextWindowState = (EditorTextWindowState) configFileStream.readObject();
                WorkbenchManager.userSettingState = (UserSettingState) configFileStream.readObject();
                WorkbenchManager.userSettingState.loadShortkeys();
                if (WorkbenchManager.userSettingState.getOpenGLVersion() == 0) {
                    WorkbenchManager.userSettingState.setOpenGLVersion(20);
                }
                Manipulator.setSnap(WorkbenchManager.userSettingState.getMedium_move_snap(), WorkbenchManager.userSettingState.getMedium_rotate_snap(),
                        WorkbenchManager.userSettingState.getMedium_scale_snap());
                try {
                    WorkbenchManager.primitiveCache = (PrimitiveCache) configFileStream.readObject();
                } catch (Exception e) {
                    NLogger.error(WorkbenchManager.class, e);
                }
                configFileStream.close();
            }
        } catch (FileNotFoundException e) {
            NLogger.error(WorkbenchManager.class, e);
        } catch (IOException e) {
            NLogger.error(WorkbenchManager.class, e);
        } catch (ClassNotFoundException e) {
            NLogger.error(WorkbenchManager.class, e);
        } catch (Exception e) {
            NLogger.error(WorkbenchManager.class, e);
        } finally {
            if (configFileStream != null) {
                try {
                    configFileStream.close();
                } catch (IOException e) {
                    NLogger.error(WorkbenchManager.class, e);
                }
            }
        }

        // TODO New values, which were not included in the state before, have to be initialized!
        if (WorkbenchManager.userSettingState.getFuzziness3D() == null) {
            WorkbenchManager.userSettingState.setFuzziness3D(new BigDecimal("0.001")); //$NON-NLS-1$
        }

        if (WorkbenchManager.userSettingState.getFuzziness2D() == 0) {
            WorkbenchManager.userSettingState.setFuzziness2D(7);
        }
    }

    /**
     * Saves the workbench to config.gz
     */
    public static void saveWorkbench() {
        ObjectOutputStream configFileStream = null;
        try {
            File configGzFile = new File("config.gz"); //$NON-NLS-1$
            configGzFile.delete();
            configFileStream = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream("config.gz"))); //$NON-NLS-1$
            if (WorkbenchManager.editor3DWindow == null) {
                // Write defaults here..
                configFileStream.writeObject(WorkbenchManager.editor3DWindowState);
            } else {
                // Write the data which was last set
                Editor3DWindow win3D = WorkbenchManager.editor3DWindow;
                Editor3DWindowState state3D = WorkbenchManager.editor3DWindow.getEditor3DWindowState();
                state3D.getWindowState().setCentered(false);
                state3D.getWindowState().setMaximized(win3D.getShell().getMaximized());
                state3D.getWindowState().setSizeAndPosition(win3D.getShell().getBounds());
                configFileStream.writeObject(state3D);
            }
            configFileStream.writeObject(WorkbenchManager.editorTextWindowState);
            if (WorkbenchManager.userSettingState != null) {
                WorkbenchManager.userSettingState.saveShortkeys();
                WorkbenchManager.userSettingState.saveColours();
            }
            configFileStream.writeObject(WorkbenchManager.userSettingState);
            try {
                configFileStream.writeObject(WorkbenchManager.primitiveCache);
            } catch (Exception e) {
                NLogger.error(WorkbenchManager.class, e);
            }
            configFileStream.close();
        } catch (SecurityException se) {
            NLogger.error(WorkbenchManager.class, se);
        } catch (FileNotFoundException fe) {
            NLogger.error(WorkbenchManager.class, fe);
        } catch (IOException ie) {
            NLogger.error(WorkbenchManager.class, ie);
        } catch (Exception e) {
            NLogger.error(WorkbenchManager.class, e);
        } finally {
            if (configFileStream != null) {
                try {
                    configFileStream.close();
                } catch (IOException e) {
                    NLogger.error(WorkbenchManager.class, e);
                }
            }
        }
    }

    /**
     * @return The serializable window state of the Editor3DWindow
     */
    public static Editor3DWindowState getEditor3DWindowState() {
        return editor3DWindowState;
    }

    /**
     * @param editor3DWindowState
     *            The serializable window state of the Editor3DWindow
     */
    public static void setEditor3DWindowState(Editor3DWindowState editor3DWindowState) {
        WorkbenchManager.editor3DWindowState = editor3DWindowState;
    }

    /**
     * @return The ApplicationWindow of the 3D editor
     */
    public static Editor3DWindow getEditor3DWindow() {
        return editor3DWindow;
    }

    /**
     * @param editor3dWindow
     *            Sets the ApplicationWindow of the 3D editor
     */
    public static void setEditor3DWindow(Editor3DWindow editor3dWindow) {
        WorkbenchManager.editor3DWindow = editor3dWindow;
    }

    /**
     * @return The serializable window state of the EditorTextWindow
     */
    public static EditorTextWindowState getEditorTextWindowState() {
        return editorTextWindowState;
    }

    /**
     * @param editorTextWindowState
     *            The serializable window state of the EditorTextWindow
     */
    public static void setEditorTextWindowState(EditorTextWindowState editorTextWindowState) {
        WorkbenchManager.editorTextWindowState = editorTextWindowState;
    }

    /**
     * @return The serializable state of the UserSettingState
     */
    public static UserSettingState getUserSettingState() {
        return userSettingState;
    }

    /**
     * @param editor3DWindowState
     *            The serializable state of the UserSettingState
     */
    public static void setUserSettingState(UserSettingState userSettingState) {
        WorkbenchManager.userSettingState = userSettingState;
    }

    public static PrimitiveCache getPrimitiveCache() {
        return primitiveCache;
    }

    public static void setPrimitiveCache(PrimitiveCache primitiveCache) {
        WorkbenchManager.primitiveCache = primitiveCache;
    }

    public static String getDefaultFileHeader() {
        final StringBuilder sb = new StringBuilder();
        sb.append("0 "); //$NON-NLS-1$
        sb.append(StringHelper.getLineDelimiter());
        sb.append("0 Name: new.dat"); //$NON-NLS-1$
        sb.append(StringHelper.getLineDelimiter());
        String ldrawName = WorkbenchManager.getUserSettingState().getLdrawUserName();
        if (ldrawName == null || ldrawName.isEmpty()) {
            sb.append("0 Author: " + WorkbenchManager.getUserSettingState().getRealUserName()); //$NON-NLS-1$
        } else {
            sb.append("0 Author: " + WorkbenchManager.getUserSettingState().getRealUserName() + " [" + WorkbenchManager.getUserSettingState().getLdrawUserName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        sb.append(StringHelper.getLineDelimiter());
        sb.append("0 !LDRAW_ORG Unofficial_Part"); //$NON-NLS-1$
        sb.append(StringHelper.getLineDelimiter());
        String license = WorkbenchManager.getUserSettingState().getLicense();
        if (license == null || license.isEmpty()) {
            sb.append("0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt"); //$NON-NLS-1$
        } else {
            sb.append(license);
        }
        sb.append(StringHelper.getLineDelimiter());
        sb.append(StringHelper.getLineDelimiter());
        sb.append("0 BFC CERTIFY CCW"); //$NON-NLS-1$
        sb.append(StringHelper.getLineDelimiter());
        sb.append(StringHelper.getLineDelimiter());
        return sb.toString();
    }
}

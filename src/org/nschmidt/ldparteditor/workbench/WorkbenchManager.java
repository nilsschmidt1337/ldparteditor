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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.eclipse.swt.graphics.Rectangle;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.helper.Manipulator;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.security.SerialKiller;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.win32appdata.AppData;

/**
 * The manager for loading or creating a default workbench
 *
 */
public enum WorkbenchManager {
    INSTANCE;

    public static final String SETTINGS_GZ = AppData.getPath() + "settings.gz"; //$NON-NLS-1$

    /** The window state of the 3D editor */
    private static Editor3DWindowState editor3DWindowState;
    /** The window state of the text editor */
    private static EditorTextWindowState editorTextWindowState;
    /** The state of the user setting */
    private static UserSettingState userSettingState;
    /** The primitive cache */
    private static PrimitiveCache primitiveCache;
    /**
     * Writes a default WorkbenchManager.SETTINGS_GZ file
     */
    public static void createDefaultWorkbench() {
        primitiveCache = new PrimitiveCache();
        userSettingState = new UserSettingState();
        editor3DWindowState = new Editor3DWindowState();
        editor3DWindowState.setWindowState(new WindowState());
        WindowState windowStateOf3dEditor = editor3DWindowState.getWindowState();
        windowStateOf3dEditor.setCentered(true);
        windowStateOf3dEditor.setMaximized(false);
        windowStateOf3dEditor.setSizeAndPosition(new Rectangle(0, 0, 1024, 768));
        editorTextWindowState = new EditorTextWindowState();
        editorTextWindowState.setWindowState(new WindowState());
        WindowState windowStateOfTextEditor = editorTextWindowState.getWindowState();
        windowStateOfTextEditor.setCentered(true);
        windowStateOfTextEditor.setMaximized(false);
        windowStateOfTextEditor.setSizeAndPosition(new Rectangle(0, 0, 1024, 768));
        saveWorkbench(WorkbenchManager.SETTINGS_GZ);
    }

    /**
     * Loads the workbench from WorkbenchManager.SETTINGS_GZ
     */
    public static boolean loadWorkbench(String path) {
        File settingsGzFile = new File(path);
        if (settingsGzFile.exists()) {
            try (ObjectInputStream settingsFileStream = new SerialKiller(new GZIPInputStream(new FileInputStream(path)))){
                WorkbenchManager.editor3DWindowState = (Editor3DWindowState) settingsFileStream.readObject();
                WorkbenchManager.editorTextWindowState = (EditorTextWindowState) settingsFileStream.readObject();
                WorkbenchManager.userSettingState = (UserSettingState) settingsFileStream.readObject();
                WorkbenchManager.userSettingState.loadShortkeys();
                if (WorkbenchManager.userSettingState.getOpenGLVersion() == 0) {
                    WorkbenchManager.userSettingState.setOpenGLVersion(20);
                }
                Manipulator.setSnap(WorkbenchManager.userSettingState.getMediumMoveSnap(), WorkbenchManager.userSettingState.getMediumRotateSnap(),
                        WorkbenchManager.userSettingState.getMediumScaleSnap());
                try {
                    WorkbenchManager.primitiveCache = (PrimitiveCache) settingsFileStream.readObject();
                } catch (Exception e) {
                    WorkbenchManager.primitiveCache = new PrimitiveCache();
                    NLogger.debug(WorkbenchManager.class, "Could not load the primitive cache."); //$NON-NLS-1$
                    NLogger.debug(WorkbenchManager.class, e);
                }
            } catch (Exception e) {
                NLogger.error(WorkbenchManager.class, e);
                return false;
            }
        }

        // New values, which were not included in the state before, have to be initialized!
        if (WorkbenchManager.userSettingState.getFuzziness3D() == null) {
            WorkbenchManager.userSettingState.setFuzziness3D(new BigDecimal("0.001")); //$NON-NLS-1$
        }

        if (WorkbenchManager.userSettingState.getFuzziness2D() == 0) {
            WorkbenchManager.userSettingState.setFuzziness2D(7);
        }

        if (Math.abs(WorkbenchManager.userSettingState.getCoplanarityAngleWarning() - 0.0) < 0.001) {
            WorkbenchManager.userSettingState.setCoplanarityAngleWarning(1.0);
        }

        if (Math.abs(WorkbenchManager.userSettingState.getCoplanarityAngleError() - 0.0) < 0.001) {
            WorkbenchManager.userSettingState.setCoplanarityAngleError(3.0);
        }

        if (Math.abs(WorkbenchManager.userSettingState.getViewportScaleFactor() - 0.0) < 0.001) {
            WorkbenchManager.userSettingState.setViewportScaleFactor(1.0);
        }
        
        WorkbenchManager.userSettingState.setDataFileSizeLimit(Math.clamp(WorkbenchManager.userSettingState.getDataFileSizeLimit(), 45, 1024_000));

        Threshold.coplanarityAngleWarning = WorkbenchManager.userSettingState.getCoplanarityAngleWarning();
        Threshold.coplanarityAngleError = WorkbenchManager.userSettingState.getCoplanarityAngleError();

        return true;
    }

    /**
     * Saves the workbench to WorkbenchManager.SETTINGS_GZ
     */
    public static boolean saveWorkbench(String path) {
        final File settingsGzFile = new File(path);
        try {
            if (Files.deleteIfExists(settingsGzFile.toPath())) {
                NLogger.debug(WorkbenchManager.class, "Deleted the old settings file at {0}", settingsGzFile.toPath()); //$NON-NLS-1$
            }
        } catch (IOException ex) {
            NLogger.error(WorkbenchManager.class, ex);
        }

        try (ObjectOutputStream settingsFileStream = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(path)))) {
            Editor3DWindow editor3DWindow = Editor3DWindow.getWindow();

            if (editor3DWindow == null) {
                // Write defaults here..
                settingsFileStream.writeObject(WorkbenchManager.editor3DWindowState);
            } else {
                // Write the data which was last set
                Editor3DWindow win3D = editor3DWindow;
                Editor3DWindowState state3D = WorkbenchManager.getEditor3DWindowState();
                state3D.getWindowState().setCentered(false);
                state3D.getWindowState().setMaximized(win3D.getShell().getMaximized());
                state3D.getWindowState().setSizeAndPosition(win3D.getShell().getBounds());
                settingsFileStream.writeObject(state3D);
            }
            settingsFileStream.writeObject(WorkbenchManager.editorTextWindowState);
            if (WorkbenchManager.userSettingState != null) {
                WorkbenchManager.userSettingState.saveShortkeys();
                WorkbenchManager.userSettingState.saveColours();
            }
            settingsFileStream.writeObject(WorkbenchManager.userSettingState);
            try {
                settingsFileStream.writeObject(WorkbenchManager.primitiveCache);
            } catch (Exception e) {
                NLogger.error(WorkbenchManager.class, e);
            }
        } catch (Exception ex) {
            NLogger.error(WorkbenchManager.class, ex);
            return false;
        }
        return true;
    }

    /**
     * @return The serializable window state of the Editor3DWindow
     */
    public static Editor3DWindowState getEditor3DWindowState() {
        return editor3DWindowState;
    }

    /**
     * @return The serializable window state of the EditorTextWindow
     */
    public static EditorTextWindowState getEditorTextWindowState() {
        return editorTextWindowState;
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
            sb.append("0 !LICENSE Licensed under CC BY 4.0 : see CAreadme.txt"); //$NON-NLS-1$
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

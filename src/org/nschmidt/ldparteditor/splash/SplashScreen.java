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
package org.nschmidt.ldparteditor.splash;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.nschmidt.ldparteditor.composites.ToolItemDrawLocation;
import org.nschmidt.ldparteditor.composites.ToolItemDrawMode;
import org.nschmidt.ldparteditor.composites.ToolItemState;
import org.nschmidt.ldparteditor.composites.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.PGData;
import org.nschmidt.ldparteditor.data.PGTimestamp;
import org.nschmidt.ldparteditor.dialogs.startup.StartupDialog;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.FileHelper;
import org.nschmidt.ldparteditor.helpers.ProgressHelper;
import org.nschmidt.ldparteditor.helpers.ShellHelper;
import org.nschmidt.ldparteditor.helpers.Version;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.win32appdata.AppData;
import org.nschmidt.ldparteditor.workbench.PrimitiveCache;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * The Splash Screen of the application (shows the status of loading the
 * workbench..)
 *
 * @author nils
 *
 */
public class SplashScreen extends ApplicationWindow {

    /** The progress bar shows the loading progress */
    private ProgressBar bar;
    /** The label shows the loading progress info text */
    private Label label;
    /** The current display */
    private Display display;

    /** The error code enumeration */
    private enum ReturnType {
        NO_ERROR, MKDIR_READ_ERROR, WRITE_ERROR, READ_ERROR, OPENGL_ERROR
    }

    /**
     * Creates a new instance of the splash screen
     */
    public SplashScreen() {
        super(null);
    }

    /**
     * Brings a new instance of this Splash Screen to run
     */
    public void run() {

        final File configGzFile = new File(WorkbenchManager.CONFIG_GZ);

        // Load the workbench here if WorkbenchManager.CONFIG_GZ exists
        if (configGzFile.exists()) {
            WorkbenchManager.loadWorkbench(WorkbenchManager.CONFIG_GZ);
            if (WorkbenchManager.getUserSettingState() == null || WorkbenchManager.getUserSettingState().isResetOnStart()) {
                // Do a reset, if needed.
                configGzFile.delete();
            }
        }
        if (!configGzFile.exists()) {
            WorkbenchManager.createDefaultWorkbench();
            // This is our first time we use the application.
            // Start a little wizard for the user dependent properties.
            if (new StartupDialog(Display.getDefault().getActiveShell()).open() == IDialogConstants.OK_ID) {
                // Save the changes, which are made by the user
                WorkbenchManager.saveWorkbench(WorkbenchManager.CONFIG_GZ);
            } else {
                configGzFile.delete();
                // Oops..
                // Kill the editors alive flag (the editor did not open. It is dead.)
                Editor3DWindow.getAlive().set(false);
                // Dispose all resources (never delete this!)
                ResourceManager.dispose();
                // Dispose the display (never delete this, too!)
                Display.getCurrent().dispose();
                return;
            }
        }

        // Set the locale (never delete this!)
        try {
            Locale.setDefault(WorkbenchManager.getUserSettingState().getLocale());
        } catch (SecurityException se) {

        }

        this.setShellStyle(SWT.ON_TOP);
        this.create();

        final Shell sh = this.getShell();
        sh.setText(I18n.SPLASH_Title);
        sh.setBounds(0, 0, 390, 240);
        ShellHelper.centerShellOnPrimaryScreen(sh);
        this.open();

        String openGLerrorRootCause = "---???---"; //$NON-NLS-1$

        // Check OpenGL version
        final boolean[] openGLerror = new boolean[1];
        try {
            Shell sh2 = new Shell();
            Composite comp = new Composite(sh2, SWT.NONE);
            GLData data = new GLData();
            data.doubleBuffer = true;
            data.depthSize = 24;
            data.alphaSize = 8;
            data.blueSize = 8;
            data.redSize = 8;
            data.greenSize = 8;
            data.stencilSize = 8;
            final GLCanvas canvas = new GLCanvas(comp, SWT.NONE, data);
            canvas.setCurrent();
            GL.createCapabilities();
            String glVersion = GL11.glGetString(GL11.GL_VERSION);
            if (glVersion == null || glVersion.length() < 4) {
                glVersion = "0.0.0"; //$NON-NLS-1$
            }
            int major = Integer.parseInt(glVersion.substring(0, 1));
            int minor = Integer.parseInt(glVersion.substring(2, 3));
            // Don't check legacy OpenGL compatibility from the stone age
            // openGLerror[0] = major < 2;
            openGLerror[0] = false;
            if (WorkbenchManager.getUserSettingState().isOpenGL33Engine() && (major > 3 || major == 3 && minor > 2)) {
                WorkbenchManager.getUserSettingState().setOpenGLVersion(33);
            } else if (WorkbenchManager.getUserSettingState().isVulkanEngine() && NLogger.DEBUG) {
                // FIXME I have to implement a SWT VKCanvas first!
                // see https://github.com/httpdigest/lwjgl3-swt
                // WorkbenchManager.getUserSettingState().setOpenGLVersion(100);
                WorkbenchManager.getUserSettingState().setOpenGLVersion(20);
            } else {
                WorkbenchManager.getUserSettingState().setOpenGLVersion(20);
            }
        } catch (Exception e) {
            NLogger.error(getClass(), e);
            openGLerrorRootCause = e.getMessage() + "\n" + NLogger.getStackTrace(e); //$NON-NLS-1$
            openGLerror[0] = true;
        }

        display = Display.getCurrent();

        // This is the return value from the following thread
        final ReturnType[] threadReturn = new ReturnType[1];
        // Start the program initialization in a new thread
        new Thread() {

            @Override
            public void run() {

                if (openGLerror[0]) {
                    threadReturn[0] = ReturnType.OPENGL_ERROR;
                    return;
                }

                // Initialize startup tasks
                ProgressHelper.queueTask(I18n.SPLASH_CheckPlugIn);
                ProgressHelper.queueTask(I18n.SPLASH_LoadWorkbench);

                // Check if there is the /plugin folder and create it if not
                dequeueTask();
                try {
                    File plugInFolder = new File("plugin"); //$NON-NLS-1$
                    if (!plugInFolder.exists()) {
                        plugInFolder.mkdir();
                    }
                } catch (SecurityException s) {
                    // Return with a warning
                    threadReturn[0] = ReturnType.MKDIR_READ_ERROR;
                    return;
                }

                // Now check the workbench
                dequeueTask();
                try {
                    if (!configGzFile.exists()) {
                        // Return with a warning
                        threadReturn[0] = ReturnType.WRITE_ERROR;
                        return;
                    }
                } catch (SecurityException s) {
                    threadReturn[0] = ReturnType.READ_ERROR;
                    return;
                }

                // Load the primitive cache
                if (WorkbenchManager.getPrimitiveCache() == null) {
                    WorkbenchManager.setPrimitiveCache(new PrimitiveCache());
                }
                HashMap<String, PGData> pcache = WorkbenchManager.getPrimitiveCache().getPrimitiveCache();
                if (pcache != null) {
                    CompositePrimitive.setCache(pcache);
                }
                HashMap<PGTimestamp, ArrayList<String>> pfcache = WorkbenchManager.getPrimitiveCache().getPrimitiveFileCache();
                if (pfcache != null) {
                    CompositePrimitive.setFileCache(pfcache);
                }

                // Check if the config file fot the 3D editor layout was moved to the AppData\LDPartEditor folder on Windows
                // This file is NOT part of the standard installation. Only for the advanced users.
                String pathToLayout3Dconfig = AppData.getPath() + "layout_3D_editor.cfg"; //$NON-NLS-1$
                try {
                    File layout3Dconfig = new File(pathToLayout3Dconfig);
                    if (!layout3Dconfig.exists()) {
                        pathToLayout3Dconfig = "layout_3D_editor.cfg"; //$NON-NLS-1$
                    }
                } catch (SecurityException consumed) {}

                // Load the toolItem state for the 3D editor
                UTF8BufferedReader reader = null;
                String line = null;
                try {
                    ArrayList<ToolItemState> states = WorkbenchManager.getUserSettingState().getToolItemConfig3D();
                    if (states == null) {
                        states = new ArrayList<ToolItemState>();
                        WorkbenchManager.getUserSettingState().setToolItemConfig3D(states);
                    }
                    // "layout_3D_editor.cfg" is not stored in the AppData\LDPartEditor folder on Windows
                    // It is considered to be "read-only" by the application.
                    reader = new UTF8BufferedReader(pathToLayout3Dconfig);
                    states.clear();
                    while (true) {
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        final String[] data_segments = Pattern.compile(";").split(line.trim()); //$NON-NLS-1$
                        if (data_segments.length > 3) {
                            for (int i = 0; i < 4; i++) {
                                data_segments[i] = data_segments[i].trim();
                            }
                            ToolItemDrawMode mode = ToolItemDrawMode.HORIZONTAL;
                            ToolItemDrawLocation location = ToolItemDrawLocation.NORTH;
                            if ("EAST".equals(data_segments[1])) { //$NON-NLS-1$
                                location = ToolItemDrawLocation.EAST;
                            } else if ("WEST".equals(data_segments[1])) { //$NON-NLS-1$
                                location = ToolItemDrawLocation.WEST;
                            }
                            if ("VERTICAL".equals(data_segments[2])) { //$NON-NLS-1$
                                mode = ToolItemDrawMode.VERTICAL;
                            } else if ("DROPDWNMNU".equals(data_segments[2])) { //$NON-NLS-1$
                                mode = ToolItemDrawMode.DROP_DOWN;
                            }
                            if ("NO_LABEL".equals(data_segments[3])) { //$NON-NLS-1$
                                data_segments[3] = ""; //$NON-NLS-1$
                            }
                            states.add(new ToolItemState(data_segments[0], location, mode, data_segments[3]));
                        }
                    }
                } catch (FileNotFoundException consumed) {
                } catch (LDParsingException e1) {
                    NLogger.error(getClass(), e1);
                } catch (UnsupportedEncodingException e1) {
                    NLogger.error(getClass(), e1);
                } finally {
                    try {
                        if (reader != null)
                            reader.close();
                    } catch (LDParsingException e1) {
                    }
                }

                // Finish it.
                display.syncExec(new Runnable() {
                    @Override
                    public void run() {
                        ProgressHelper.finishQueue(bar, label);
                    }
                });
                threadReturn[0] = ReturnType.NO_ERROR;
            }
        }.start();
        // Wait until the splash screen worker thread has done the job..
        while (threadReturn[0] == null) {
            display.readAndDispatch();
            /*
             * if (!display.readAndDispatch()) { // display.sleep(); <- The
             * display never wakes up.. :( }
             */
        }
        // Close the splash..
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
        }
        close();
        while (!bar.isDisposed()) {
            if (!Display.getCurrent().readAndDispatch()) {
                Display.getCurrent().sleep();
            }
        }
        // ..and dispose the splash image, because it is not needed anymore
        ResourceManager.disposeImage("imgSplash.png"); //$NON-NLS-1$
        // Check if we were ran into trouble..
        if (threadReturn[0] != ReturnType.NO_ERROR) {
            MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            switch (threadReturn[0]) {
            case MKDIR_READ_ERROR:
                // Show a warning message, that the program cannot be used,
                // because the user
                // has no rights to create a directory in the application folder
                // or to read from the application folder
                messageBox.setText(I18n.DIALOG_Error);
                messageBox.setMessage(I18n.SPLASH_NoMkDirNoRead);
                break;
            case WRITE_ERROR:
                // Show a warning message, that the program cannot be used,
                // because the configuration
                // cannot be saved in/loaded from the application folder
                messageBox.setText(I18n.DIALOG_Error);
                messageBox.setMessage(I18n.SPLASH_NoWrite);
                break;
            case READ_ERROR:
                // Show a warning message, that the program cannot be used,
                // because the user
                // has no rights to read from the application folder
                messageBox.setText(I18n.DIALOG_Error);
                messageBox.setMessage(I18n.SPLASH_NoRead);
                break;
            case OPENGL_ERROR:
                // Show a warning message, that the program cannot be started,
                // because the OpenGL version 2.1
                // is not supported by the graphics card.
                messageBox.setText(I18n.DIALOG_Error);
                messageBox.setMessage(I18n.SPLASH_InvalidOpenGLVersion + openGLerrorRootCause);
                break;
            case NO_ERROR:
                // No error occurs, all fine :)
                break;
            }
            messageBox.open();
            // Oops..
            // Dispose all resources (never delete this!)
            ResourceManager.dispose();
            // Dispose the display (never delete this, too!)
            Display.getCurrent().dispose();
        } else {
            // Everything's under control..
            // Prepare the default project folder
            try {
                File projectFolder = new File(Project.DEFAULT_PROJECT_PATH);
                if (projectFolder.exists()) {
                    FileHelper.deleteDirectory(projectFolder);
                }
                // Well, it should be possible to create this w/o trouble at this time.
                projectFolder.mkdir();
            } catch (SecurityException consumed) {
            }

            String ldcPath = WorkbenchManager.getUserSettingState().getLdConfigPath();
            try {
                if (ldcPath == null || !new File(ldcPath).exists()) {
                    ldcPath = WorkbenchManager.getUserSettingState().getLdrawFolderPath() + File.separator + "ldconfig.ldr"; //$NON-NLS-1$
                    if (!new File(ldcPath).exists()) {
                        ldcPath = WorkbenchManager.getUserSettingState().getLdrawFolderPath() + File.separator + "LDConfig.ldr"; //$NON-NLS-1$
                    }
                    if (!new File(ldcPath).exists()) {
                        ldcPath = WorkbenchManager.getUserSettingState().getLdrawFolderPath() + File.separator + "ldconfig.ldr"; //$NON-NLS-1$
                    }
                    WorkbenchManager.getUserSettingState().setLdConfigPath(ldcPath);
                }
            } catch (SecurityException se) {
                ldcPath = WorkbenchManager.getUserSettingState().getLdrawFolderPath() + File.separator + "ldconfig.ldr"; //$NON-NLS-1$
                WorkbenchManager.getUserSettingState().setLdConfigPath(ldcPath);
            }
            View.loadLDConfig(ldcPath);

            // Finally, open the editor window!

            if (NLogger.DEBUG) {
                DatFile fileToEdit = new DatFile(Project.getProjectPath() + File.separator + "parts" + File.separator + "new.dat"); //$NON-NLS-1$ //$NON-NLS-2$
                Project.setFileToEdit(fileToEdit);
            }
            new Editor3DWindow().run();
        }
    }

    @Override
    protected Control createContents(final Composite parent) {

        final Color bgColor = SWTResourceManager.getColor(SWT.COLOR_WHITE);
        final Composite frame = new Composite(parent, SWT.NONE);
        final FormData titleData = new FormData();
        final FormData statusData = new FormData();
        final FormLayout layout = new FormLayout();
        final Label titleLabel = new Label(frame, SWT.NONE);
        final Label statusLabel = new Label(frame, SWT.NONE);
        final ProgressBar bar = new ProgressBar(frame, SWT.NONE);
        final FormData barData = new FormData();

        bar.setMaximum(100);

        titleLabel.setText(Version.getApplicationName() + " " + Version.getVersion() + "\n" + Version.getStage() + "\n(C) " + Version.getDevelopmentLead()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        titleLabel.setBackground(bgColor);
        bar.setBackground(bgColor);
        statusLabel.setBackground(bgColor);

        frame.setLayout(layout);
        frame.setBackgroundImage(ResourceManager.getImage("imgSplash.png")); //$NON-NLS-1$

        barData.left = new FormAttachment(15, 5);
        barData.right = new FormAttachment(100, -5);
        barData.bottom = new FormAttachment(100, -5);
        bar.setLayoutData(barData);

        titleData.left = new FormAttachment(45, 5);
        titleData.right = new FormAttachment(100, 0);
        titleData.top = new FormAttachment(50, 0);
        titleLabel.setLayoutData(titleData);

        statusData.left = new FormAttachment(15, 5);
        statusData.right = new FormAttachment(100, 0);
        statusData.top = new FormAttachment(80, 0);
        statusLabel.setLayoutData(statusData);

        this.bar = bar;
        this.label = statusLabel;

        return frame;
    }

    /**
     * Updates the task information on the splash screen via {@code syncExec()}
     * and {@code ProgressHelper.dequeueTask()}
     */
    private void dequeueTask() {
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                ProgressHelper.dequeueTask(bar, label);
            }
        });
    }

}

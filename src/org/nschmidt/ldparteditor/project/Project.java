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
package org.nschmidt.ldparteditor.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.helpers.Version;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;

/**
 * The static project class
 *
 * @author nils
 *
 */
public enum Project {
    INSTANCE;

    private static String projectName = "default"; //$NON-NLS-1$
    private static String projectPath = new File("project").getAbsolutePath(); //$NON-NLS-1$
    private static String tempProjectName = "default"; //$NON-NLS-1$
    private static String tempProjectPath = new File("project").getAbsolutePath(); //$NON-NLS-1$
    private static boolean defaultProject = true;

    /** A set of all open EditorTextWindow instances */
    private static HashSet<EditorTextWindow> openTextWindows = new HashSet<EditorTextWindow>();
    /** A set of all absolute filenames, which are not saved */
    private static final HashSet<DatFile> unsavedFiles = new HashSet<DatFile>();
    /** A set of all absolute filenames, which were parsed */
    private static final HashSet<DatFile> parsedFiles = new HashSet<DatFile>();
    /** The file which is currently displayed in the 3D editor */
    private static DatFile fileToEdit = new DatFile(getProjectPath() + File.separator + "PARTS" + File.separator + "new.dat"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Creates the new project file structure
     */
    public static void create(boolean withFolders) {
        if (withFolders) resetEditor();
        setDefaultProject(false);
        createFileStructure(withFolders);
        updateEditor();

        Editor3DWindow.getWindow().getProjectParts().getItems().clear();
        Editor3DWindow.getWindow().getProjectParts().setData(new ArrayList<DatFile>());

        Editor3DWindow.getWindow().getProjectSubparts().getItems().clear();
        Editor3DWindow.getWindow().getProjectSubparts().setData(new ArrayList<DatFile>());

        Editor3DWindow.getWindow().getProjectPrimitives().getItems().clear();
        Editor3DWindow.getWindow().getProjectPrimitives().setData(new ArrayList<DatFile>());

        Editor3DWindow.getWindow().getProjectPrimitives48().getItems().clear();
        Editor3DWindow.getWindow().getProjectPrimitives48().setData(new ArrayList<DatFile>());

        Editor3DWindow.getWindow().getShell().update();
        Editor3DWindow.getWindow().getProjectParts().getParent().build();
        Editor3DWindow.getWindow().getProjectParts().getParent().redraw();
        Editor3DWindow.getWindow().getProjectParts().getParent().update();
        // TODO Needs implementation!
    }

    /**
     * Saves the whole project
     */
    @SuppressWarnings("unchecked")
    public static boolean save() {
        HashSet<DatFile> projectFiles = new HashSet<DatFile>();
        if (isDefaultProject()) {
            // Linked project parts need a new path, because they were copied to a new directory
            String defaultPrefix = new File("project").getAbsolutePath() + File.separator; //$NON-NLS-1$
            String projectPrefix = new File(projectPath).getAbsolutePath() + File.separator;
            Editor3DWindow.getWindow().getProjectParts().getParentItem().setData(projectPath);
            projectFiles.addAll((ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectParts().getData());
            projectFiles.addAll((ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectSubparts().getData());
            projectFiles.addAll((ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives().getData());
            projectFiles.addAll((ArrayList<DatFile>) Editor3DWindow.getWindow().getProjectPrimitives48().getData());
            for (DatFile df : projectFiles) {
                boolean isUnsaved = Project.getUnsavedFiles().contains(df);
                boolean isParsed = Project.getParsedFiles().contains(df);
                Project.getParsedFiles().remove(df);
                Project.getUnsavedFiles().remove(df);
                String newName = df.getNewName();
                String oldName = df.getOldName();

                if (!newName.startsWith(projectPrefix) && newName.startsWith(defaultPrefix)) {
                    df.setNewName(projectPrefix + newName.substring(defaultPrefix.length()));
                }
                if (!oldName.startsWith(projectPrefix) && oldName.startsWith(defaultPrefix)) {
                    df.setOldName(projectPrefix + oldName.substring(defaultPrefix.length()));
                }
                if (isUnsaved) Project.addUnsavedFile(df);
                if (isParsed) Project.getParsedFiles().add(df);
            }
        }
        try {
            if (isDefaultProject()) {
                copyFolder(new File("project"), new File(projectPath)); //$NON-NLS-1$
                for (DatFile df : projectFiles) {
                    df.updateLastModified();
                }
            }
            setDefaultProject(false);
            return true;
        } catch (Exception ex) {
            return false;
        }

    }

    public static void copyFolder(File src, File dest)
            throws IOException{

        if(src.isDirectory()){

            //if directory not exists, create it
            if(!dest.exists()){
                dest.mkdir();
                NLogger.debug(Project.class, "Directory copied from "  //$NON-NLS-1$
                        + src + "  to " + dest); //$NON-NLS-1$
            }

            //list all the directory contents
            String files[] = src.list();

            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFolder(srcFile,destFile);
            }

        }else{
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0){
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
            NLogger.debug(Project.class, "File copied from " + src + " to " + dest); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static void deleteFolder(File src)
            throws IOException{
        if(src.isDirectory()){
            File files[] = src.listFiles();
            for (File file : files) {
                deleteFolder(file);
            }
            src.delete();
            NLogger.debug(Project.class, "Directory deleted " + src); //$NON-NLS-1$
        }else{
            NLogger.debug(Project.class, "File deleted " + src); //$NON-NLS-1$
            src.delete();
        }
    }

    /**
     * Creates the default project datastructure
     */
    public static void createDefault() {
        resetEditor();
        setDefaultProject(true);
        setProjectPath(new File("project").getAbsolutePath()); //$NON-NLS-1$
        createFileStructure(false);
        Editor3DWindow.getWindow().getShell().setText(Version.getApplicationName());
        Editor3DWindow.getWindow().getShell().update();
    }

    private static void createFileStructure(boolean makeProjectRoot) {
        try {
            if (makeProjectRoot) {
                File projectFolder = new File(getProjectPath());
                projectFolder.mkdir();
            }
            File partsFolder = new File(getProjectPath() + File.separator + "PARTS"); //$NON-NLS-1$
            partsFolder.mkdir();
            File subpartsFolder = new File(getProjectPath() + File.separator + "PARTS" + File.separator + "S"); //$NON-NLS-1$ //$NON-NLS-2$
            subpartsFolder.mkdir();
            File primitivesFolder = new File(getProjectPath() + File.separator + "P"); //$NON-NLS-1$
            primitivesFolder.mkdir();
            File primitives48Folder = new File(getProjectPath() + File.separator + "P" + File.separator + "48"); //$NON-NLS-1$ //$NON-NLS-2$
            primitives48Folder.mkdir();
            File texturesFolder = new File(getProjectPath() + File.separator + "TEXTURES"); //$NON-NLS-1$
            texturesFolder.mkdir();
        } catch (SecurityException consumed) {
        }
    }

    private static void resetEditor() {
        HashSet<EditorTextWindow> openWindows = new HashSet<EditorTextWindow>();
        openWindows.addAll(openTextWindows);
        for (EditorTextWindow txtwin : openWindows) {
            txtwin.getShell().close();
        }
    }

    public static void updateEditor() {
        // Update the window text of the 3D editor
        Editor3DWindow.getWindow().getProjectParts().getParentItem().setText(getProjectName());
        Editor3DWindow.getWindow().getShell().setText(getProjectName() + " - " + Version.getApplicationName()); //$NON-NLS-1$
    }

    /**
     * @return {@code true} if the project is the default project
     */
    public static boolean isDefaultProject() {
        return defaultProject;
    }

    /**
     * @param defaultProject
     *            {@code true} if the project is the default project
     */
    private static void setDefaultProject(boolean defaultProject) {
        Project.defaultProject = defaultProject;
    }

    /**
     * @return the path of the project
     */
    public static String getProjectPath() {
        return projectPath;
    }

    /**
     * @param projectPath
     *            the new project path
     */
    public static void setProjectPath(String projectPath) {
        Project.projectPath = projectPath;
    }

    /**
     * @return the name of the project
     */
    public static String getProjectName() {
        return projectName;
    }

    /**
     * @param projectName
     *            the new project name
     */
    public static void setProjectName(String projectName) {
        Project.projectName = projectName;
    }

    /**
     * @return the open text editor windows from the current project
     */
    public static HashSet<EditorTextWindow> getOpenTextWindows() {
        return openTextWindows;
    }

    /**
     * @return all absolut filenames, which are not saved
     */
    public static HashSet<DatFile> getUnsavedFiles() {
        return unsavedFiles;
    }

    public static void addUnsavedFile(DatFile file) {
        unsavedFiles.add(file);
    }

    public static void removeUnsavedFile(DatFile file) {
        unsavedFiles.remove(file);
    }

    /**
     * @return all absolut filenames, which were parsed
     */
    public static HashSet<DatFile> getParsedFiles() {
        return parsedFiles;
    }

    /**
     * @return the fileToEdit
     */
    public static DatFile getFileToEdit() {
        return fileToEdit;
    }

    /**
     * @param fileToEdit
     *            the fileToEdit to set
     */
    public static void setFileToEdit(DatFile fileToEdit) {
        Project.fileToEdit = fileToEdit;
    }

    public static String getTempProjectName() {
        return tempProjectName;
    }

    public static void setTempProjectName(String tempProjectName) {
        Project.tempProjectName = tempProjectName;
    }

    public static String getTempProjectPath() {
        return tempProjectPath;
    }

    public static void setTempProjectPath(String tempProjectPath) {
        Project.tempProjectPath = tempProjectPath;
    }

}
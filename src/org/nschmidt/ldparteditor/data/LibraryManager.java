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
package org.nschmidt.ldparteditor.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.widgets.TreeItem;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * This manager reads the local library contents of the following folders: <br>
 * <br>
 * <ul>
 * <li>The official parts folder (e.g. D:\LDRAW\)</li>
 * <li>The unofficial parts folder (e.g. D:\LDRAW\Unofficial\)</li>
 * <li>The current project</li>
 * </ul>
 *
 * @author nils TODO Needs documentation
 */
public class LibraryManager {

    // TODO Needs error handling!

    /**
     * Reads all paths to project library parts case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static void readProjectParts(TreeItem treeItem) {
        readLibraryFolder(Project.getProjectPath(), "PARTS", "", treeItem, false, false, DatType.PART); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Reads all paths to project library parts case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static void readProjectPartsParent(TreeItem treeItem) {
        readLibraryFolder(Project.getProjectPath(), "", "", treeItem, false, false, DatType.PART); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Reads all paths to project library subparts case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static void readProjectSubparts(TreeItem treeItem) {
        readLibraryFolder(Project.getProjectPath(), "PARTS", "S", treeItem, false, false, DatType.SUBPART); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Reads all paths to project library primitives case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static void readProjectPrimitives(TreeItem treeItem) {
        readLibraryFolder(Project.getProjectPath(), "P", "", treeItem, true, false, DatType.PRIMITIVE); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Reads all paths to project library hi-res primirives case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static void readProjectHiResPrimitives(TreeItem treeItem) {
        readLibraryFolder(Project.getProjectPath(), "P", "48", treeItem, true, false, DatType.PRIMITIVE48); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Reads all paths to unofficial library parts case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static void readUnofficialParts(TreeItem treeItem) {
        readLibraryFolder(WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), "PARTS", "", treeItem, false, false, DatType.PART); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Reads all paths to unofficial library subparts case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static void readUnofficialSubparts(TreeItem treeItem) {
        readLibraryFolder(WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), "PARTS", "S", treeItem, false, false, DatType.SUBPART); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Reads all paths to unofficial library primitives case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static void readUnofficialPrimitives(TreeItem treeItem) {
        readLibraryFolder(WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), "P", "", treeItem, true, false, DatType.PRIMITIVE); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Reads all paths to unofficial library hi-res primirives case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static void readUnofficialHiResPrimitives(TreeItem treeItem) {
        readLibraryFolder(WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), "P", "48", treeItem, true, false, DatType.PRIMITIVE48); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Reads all paths to official library parts case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static void readOfficialParts(TreeItem treeItem) {
        readLibraryFolder(WorkbenchManager.getUserSettingState().getLdrawFolderPath(), "PARTS", "", treeItem, false, true, DatType.PART); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Reads all paths to official library subparts case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static void readOfficialSubparts(TreeItem treeItem) {
        readLibraryFolder(WorkbenchManager.getUserSettingState().getLdrawFolderPath(), "PARTS", "S", treeItem, false, true, DatType.SUBPART); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Reads all paths to official library primitives case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static void readOfficialPrimitives(TreeItem treeItem) {
        readLibraryFolder(WorkbenchManager.getUserSettingState().getLdrawFolderPath(), "P", "", treeItem, true, true, DatType.PRIMITIVE); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Reads all paths to official library hi-res primitives case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static void readOfficialHiResPrimitives(TreeItem treeItem) {
        readLibraryFolder(WorkbenchManager.getUserSettingState().getLdrawFolderPath(), "P", "48", treeItem, true, true, DatType.PRIMITIVE48); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Synchronises all paths to project library objects case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     * @return
     */
    public static int[] syncProjectElements(TreeItem treeItem) {
        return syncLibraryFolder(Project.getProjectPath(), "", "", treeItem, false, false, DatType.PART); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Synchronises all paths to unofficial library parts case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static int[] syncUnofficialParts(TreeItem treeItem) {
        return syncLibraryFolder(WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), "PARTS", "", treeItem, false, false, DatType.PART); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Synchronises all paths to unofficial library subparts case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static int[] syncUnofficialSubparts(TreeItem treeItem) {
        return syncLibraryFolder(WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), "PARTS", "S", treeItem, false, false, DatType.SUBPART); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Synchronises all paths to unofficial library primitives case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static int[] syncUnofficialPrimitives(TreeItem treeItem) {
        return syncLibraryFolder(WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), "P", "", treeItem, true, false, DatType.PRIMITIVE); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Synchronises all paths to unofficial library hi-res primirives case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static int[] syncUnofficialHiResPrimitives(TreeItem treeItem) {
        return syncLibraryFolder(WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), "P", "48", treeItem, true, false, DatType.PRIMITIVE48); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Synchronises all paths to official library parts case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static int[] syncOfficialParts(TreeItem treeItem) {
        return syncLibraryFolder(WorkbenchManager.getUserSettingState().getLdrawFolderPath(), "PARTS", "", treeItem, false, true, DatType.PART); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Synchronises all paths to official library subparts case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static int[] syncOfficialSubparts(TreeItem treeItem) {
        return syncLibraryFolder(WorkbenchManager.getUserSettingState().getLdrawFolderPath(), "PARTS", "S", treeItem, false, true, DatType.SUBPART); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Synchronises all paths to official library primitives case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static int[] syncOfficialPrimitives(TreeItem treeItem) {
        return syncLibraryFolder(WorkbenchManager.getUserSettingState().getLdrawFolderPath(), "P", "", treeItem, true, true, DatType.PRIMITIVE); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Synchronises all paths to official library hi-res primitives case insensitive <br>
     * <b>NOTE:</b> The base path is still case sensitive!
     *
     * @param treeItem
     *            the target {@code TreeItem} from the Parts Tree of the 3D
     *            editor.
     */
    public static int[] syncOfficialHiResPrimitives(TreeItem treeItem) {
        return syncLibraryFolder(WorkbenchManager.getUserSettingState().getLdrawFolderPath(), "P", "48", treeItem, true, true, DatType.PRIMITIVE48); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * This is a helper class, which provides a comparator for DAT file names.
     *
     * @author nils
     *
     */
    private static class DatFileName implements Comparable<DatFileName> {
        /** The DAT file name to compare */
        private final String name;
        private final String description;
        private final boolean comparePrimitives;

        /**
         * Creates a DAT file name object
         *
         * @param name
         *            the DAT file name (e.g. 973p7u.dat)
         */
        public DatFileName(String name, String description, boolean comparePrimitives) {
            this.name = name;
            this.description = description;
            this.comparePrimitives = comparePrimitives;
        }

        public String getName() {
            return this.name;
        }

        public String getDescription() {
            return this.description;
        }

        @Override
        public int compareTo(DatFileName other) {

            String[] segs_this = this.name.split(Pattern.quote(".")); //$NON-NLS-1$
            String[] segs_other = other.name.split(Pattern.quote(".")); //$NON-NLS-1$

            // Special cases: unknown parts numbers "u[Number]" and unknown
            // stickers "s[Number]"
            if (segs_this[0].charAt(0) == 'u' && segs_other[0].charAt(0) == 'u' || segs_this[0].charAt(0) == 's' && segs_other[0].charAt(0) == 's') {
                segs_this[0] = segs_this[0].substring(1, segs_this[0].length());
                segs_other[0] = segs_other[0].substring(1, segs_other[0].length());
            }

            // Special cases: Primitive fractions
            if (this.comparePrimitives && other.comparePrimitives) {
                if (segs_this[0].length() > 2 && segs_other[0].length() > 2 && (segs_this[0].charAt(1) == '-' || segs_this[0].charAt(2) == '-')
                        && (segs_other[0].charAt(1) == '-' || segs_other[0].charAt(2) == '-')) {
                    String upper_this = ""; //$NON-NLS-1$
                    String upper_other = ""; //$NON-NLS-1$
                    String lower_this = ""; //$NON-NLS-1$
                    String lower_other = ""; //$NON-NLS-1$
                    String suffix_this = ""; //$NON-NLS-1$
                    String suffix_other = ""; //$NON-NLS-1$
                    boolean readUpper = true;
                    int charCount = 0;
                    char[] chars_this = segs_this[0].toCharArray();
                    for (char c : chars_this) {
                        if (Character.isDigit(c)) {
                            if (readUpper) {
                                upper_this = upper_this + c;
                            } else {
                                lower_this = lower_this + c;
                            }
                        } else {
                            if (readUpper) {
                                readUpper = false;
                            } else {
                                suffix_this = segs_this[0].substring(charCount, segs_this[0].length());
                                break;
                            }
                        }
                        charCount++;
                    }
                    readUpper = true;
                    charCount = 0;
                    char[] chars_other = segs_other[0].toCharArray();
                    for (char c : chars_other) {
                        if (Character.isDigit(c)) {
                            if (readUpper) {
                                upper_other = upper_other + c;
                            } else {
                                lower_other = lower_other + c;
                            }
                        } else {
                            if (readUpper) {
                                readUpper = false;
                            } else {
                                suffix_other = segs_other[0].substring(charCount, segs_other[0].length());
                                break;
                            }
                        }
                        charCount++;
                    }
                    try {

                        float fraction_this = Float.parseFloat(upper_this) / Float.parseFloat(lower_this);
                        float fraction_other = Float.parseFloat(upper_other) / Float.parseFloat(lower_other);

                        if (!suffix_this.equals(suffix_other)) {
                            return suffix_this.compareTo(suffix_other);
                        } else {
                            if (fraction_this > fraction_other) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    } catch (NumberFormatException consumed) {
                    }
                }
            }

            String num_this = ""; //$NON-NLS-1$
            String num_other = ""; //$NON-NLS-1$
            char[] chars_this = segs_this[0].toCharArray();
            for (char c : chars_this) {
                if (Character.isDigit(c)) {
                    num_this = num_this + c;
                } else {
                    break;
                }
            }
            char[] chars_other = segs_other[0].toCharArray();
            for (char c : chars_other) {
                if (Character.isDigit(c)) {
                    num_other = num_other + c;
                } else {
                    break;
                }
            }
            if (num_this.isEmpty() || num_other.isEmpty() || num_this.equals(num_other)) {
                return this.name.compareTo(other.name);
            } else {
                int int_this = Integer.parseInt(num_this, 10);
                int int_other = Integer.parseInt(num_other, 10);
                if (int_this == int_other) {
                    return 0;
                } else {
                    if (int_this > int_other) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
        }

        @Override
        public int hashCode() {
            return name == null ? 0 : name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DatFileName other = (DatFileName) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }
    }

    /**
     * Reads the contents (DAT files) from the folder case insensitive and sorts
     * the entries alphabetically and by number
     *
     * @param basePath
     *            this path was already validated before
     * @param suffix1
     *            the case insensitive name of the first subfolder
     * @param suffix2
     *            the case insensitive name of the second subfolder
     * @param treeItem
     *            the target {@code TreeItem} which lists all DAT files from the
     *            folder
     * @param isPrimitiveFolder
     *            {@code true} if the folder contains primitives
     */
    private static void readLibraryFolder(String basePath, String suffix1, String suffix2, TreeItem treeItem, boolean isPrimitiveFolder, boolean isReadOnlyFolder, DatType type) {
        String folderPath = basePath;
        boolean canSearch = true;
        File baseFolder = new File(basePath);
        if (suffix1.isEmpty()) {
            HashMap<DatFileName, TreeItem> parentMap = new HashMap<DatFileName, TreeItem>();
            HashMap<DatFileName, DatType> typeMap = new HashMap<DatFileName, DatType>();
            ArrayList<DatFileName> datFiles = new ArrayList<DatFileName>();
            File libFolder = new File(folderPath);
            UTF8BufferedReader reader = null;
            StringBuilder titleSb = new StringBuilder();
            for (File f : libFolder.listFiles()) {
                if (f.isFile() && f.getName().matches(".*.dat")) { //$NON-NLS-1$
                    titleSb.setLength(0);
                    try {
                        reader = new UTF8BufferedReader(f.getAbsolutePath());
                        String title = reader.readLine();
                        if (title != null) {
                            title = title.trim();
                            if (title.length() > 0) {
                                titleSb.append(" -"); //$NON-NLS-1$
                                titleSb.append(title.substring(1));
                            }
                        }
                        // Detect type
                        while (true) {
                            String typ = reader.readLine();
                            if (typ != null) {
                                typ = typ.trim();
                                if (!typ.startsWith("0")) { //$NON-NLS-1$
                                    break;
                                } else {
                                    int i1 = typ.indexOf("!LDRAW_ORG"); //$NON-NLS-1$
                                    if (i1 > -1) {
                                        int i2;
                                        i2 = typ.indexOf("Subpart"); //$NON-NLS-1$
                                        if (i2 > -1 && i1 < i2) {
                                            type = DatType.SUBPART;
                                            break;
                                        }
                                        i2 = typ.indexOf("Part"); //$NON-NLS-1$
                                        if (i2 > -1 && i1 < i2) {
                                            type = DatType.PART;
                                            break;
                                        }
                                        i2 = typ.indexOf("48_Primitive"); //$NON-NLS-1$
                                        if (i2 > -1 && i1 < i2) {
                                            type = DatType.PRIMITIVE48;
                                            break;
                                        }
                                        i2 = typ.indexOf("Primitive"); //$NON-NLS-1$
                                        if (i2 > -1 && i1 < i2) {
                                            type = DatType.PRIMITIVE;
                                            break;
                                        }
                                    }
                                }
                            } else {
                                break;
                            }
                        }
                        // Change treeItem according to type
                        switch (type) {
                        case PART:
                            treeItem = Editor3DWindow.getWindow().getProjectParts();
                            break;
                        case SUBPART:
                            treeItem = Editor3DWindow.getWindow().getProjectSubparts();
                            break;
                        case PRIMITIVE:
                            treeItem = Editor3DWindow.getWindow().getProjectPrimitives();
                            break;
                        case PRIMITIVE48:
                            treeItem = Editor3DWindow.getWindow().getProjectPrimitives48();
                            break;
                        default:
                            break;
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
                    DatFileName name = new DatFileName(f.getName(), titleSb.toString(), type == DatType.PRIMITIVE || type == DatType.PRIMITIVE48);
                    datFiles.add(name);
                    parentMap.put(name, treeItem);
                    typeMap.put(name, type);
                }
            }
            // Sort the file list
            Collections.sort(datFiles);
            // Create the file entries
            for (DatFileName dat : datFiles) {
                TreeItem finding = new TreeItem(parentMap.get(dat), SWT.NONE);
                // Save the path
                DatFile path = new DatFile(folderPath + File.separator + dat.getName(), dat.getDescription(), isReadOnlyFolder, typeMap.get(dat));
                finding.setData(path);
                // Set the filename
                if (Project.getUnsavedFiles().contains(path)) {
                    // Insert asterisk if the file was modified
                    finding.setText("* " + dat.getName() + dat.getDescription()); //$NON-NLS-1$
                } else {
                    finding.setText(dat.getName() + dat.getDescription());
                }
            }
        } else {
            for (File sub : baseFolder.listFiles()) {
                // Check if the sub-folder exist
                if (sub.isDirectory() && sub.getName().equalsIgnoreCase(suffix1)) {
                    folderPath = folderPath + File.separator + sub.getName();
                    if (!suffix2.equals("")) { //$NON-NLS-1$
                        // We can not search now. It is not guaranteed that the
                        // sub-sub-folder exist (e.g. D:\LDRAW\PARTS\S)
                        canSearch = false;
                        File subFolder = new File(basePath + File.separator + sub.getName());
                        for (File subsub : subFolder.listFiles()) {
                            if (subsub.isDirectory() && subsub.getName().equalsIgnoreCase(suffix2)) {
                                folderPath = folderPath + File.separator + subsub.getName();
                                canSearch = true;
                                break;
                            }
                        }
                    }
                    if (canSearch) {
                        // Do the search for DAT files
                        ArrayList<DatFileName> datFiles = new ArrayList<DatFileName>();
                        File libFolder = new File(folderPath);
                        UTF8BufferedReader reader = null;
                        StringBuilder titleSb = new StringBuilder();
                        for (File f : libFolder.listFiles()) {
                            if (f.isFile() && f.getName().matches(".*.dat")) { //$NON-NLS-1$
                                titleSb.setLength(0);
                                try {
                                    reader = new UTF8BufferedReader(f.getAbsolutePath());
                                    String title = reader.readLine();
                                    if (title != null) {
                                        title = title.trim();
                                        if (title.startsWith("0 ~Moved to")) continue; //$NON-NLS-1$
                                        if (title.length() > 0) {
                                            titleSb.append(" -"); //$NON-NLS-1$
                                            titleSb.append(title.substring(1));
                                        }
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
                                datFiles.add(new DatFileName(f.getName(), titleSb.toString(), isPrimitiveFolder));
                            }
                        }
                        // Sort the file list
                        Collections.sort(datFiles);
                        // Create the file entries
                        for (DatFileName dat : datFiles) {
                            TreeItem finding = new TreeItem(treeItem, SWT.NONE);
                            // Save the path
                            DatFile path = new DatFile(folderPath + File.separator + dat.getName(), dat.getDescription(), isReadOnlyFolder, type);
                            finding.setData(path);
                            // Set the filename
                            if (Project.getUnsavedFiles().contains(path)) {
                                // Insert asterisk if the file was modified
                                finding.setText("* " + dat.getName() + dat.getDescription()); //$NON-NLS-1$
                            } else {
                                finding.setText(dat.getName() + dat.getDescription());
                            }
                        }
                    }
                    break;
                }
            }
        }
    }


    /**
     * Synchronises the contents (DAT files) from the folder case insensitive and sorts
     * the entries alphabetically and by number
     *
     * @param basePath
     *            this path was already validated before
     * @param suffix1
     *            the case insensitive name of the first subfolder
     * @param suffix2
     *            the case insensitive name of the second subfolder
     * @param treeItem
     *            the target {@code TreeItem} which lists all DAT files from the
     *            folder
     * @param isPrimitiveFolder
     *            {@code true} if the folder contains primitives
     * @return An array which contains how many files were added [0], deleted [1], and can't be replaced [2]
     */
    private static int[] syncLibraryFolder(String basePath, String suffix1, String suffix2, TreeItem treeItem, boolean isPrimitiveFolder, boolean isReadOnlyFolder, DatType type) {

        int[] result = new int[3];

        // FIXME Needs impl.!
        HashMap<String, TreeItem> parentMap = new HashMap<String, TreeItem>();
        HashMap<String, DatType> typeMap = new HashMap<String, DatType>();
        HashMap<String, DatFileName> dfnMap = new HashMap<String, DatFileName>();
        HashSet<String> locked = new HashSet<String>();
        HashSet<String> loaded = new HashSet<String>();
        HashMap<String, DatFile> existingMap = new HashMap<String, DatFile>();

        HashMap<String, HashSet<Composite3D>> openIn3DMap = new HashMap<String, HashSet<Composite3D>>();
        HashMap<String, CompositeTab> openInTextMap = new HashMap<String, CompositeTab>();

        HashMap<String, TreeItem> newParentMap = new HashMap<String, TreeItem>();
        HashMap<String, DatType> newTypeMap = new HashMap<String, DatType>();
        HashMap<String, DatFileName> newDfnMap = new HashMap<String, DatFileName>();

        if (suffix1.isEmpty()) {

            // Sync project root.

            // 1. Read and store all unsaved project files, since we want to keep them in the project

            final TreeItem treeItem_ProjectParts = treeItem.getItems().get(0);
            final TreeItem treeItem_ProjectSubparts = treeItem.getItems().get(1);
            final TreeItem treeItem_ProjectPrimitives = treeItem.getItems().get(2);
            final TreeItem treeItem_ProjectPrimitives48 = treeItem.getItems().get(3);

            readVirtualDataFromFolder(result, parentMap, typeMap, locked, loaded, existingMap, dfnMap, treeItem_ProjectParts, openIn3DMap, openInTextMap, true);
            readVirtualDataFromFolder(result, parentMap, typeMap, locked, loaded, existingMap, dfnMap, treeItem_ProjectSubparts, openIn3DMap, openInTextMap, true);
            readVirtualDataFromFolder(result, parentMap, typeMap, locked, loaded, existingMap, dfnMap, treeItem_ProjectPrimitives, openIn3DMap, openInTextMap, true);
            readVirtualDataFromFolder(result, parentMap, typeMap, locked, loaded, existingMap, dfnMap, treeItem_ProjectPrimitives48, openIn3DMap, openInTextMap, true);

            // 3. Clear all project trees
            treeItem_ProjectParts.getItems().clear();
            treeItem_ProjectSubparts.getItems().clear();
            treeItem_ProjectPrimitives.getItems().clear();
            treeItem_ProjectPrimitives48.getItems().clear();

            // 4. Scan for new files

            readActualDataFromFolder(result, basePath, null, "", "", locked, loaded, newParentMap, newTypeMap, newDfnMap); //$NON-NLS-1$ //$NON-NLS-2$

            readActualDataFromFolder(result, basePath, DatType.PART, "PARTS", "", locked, loaded, newParentMap, newTypeMap, newDfnMap); //$NON-NLS-1$ //$NON-NLS-2$
            readActualDataFromFolder(result, basePath, DatType.SUBPART, "PARTS", "S", locked, loaded, newParentMap, newTypeMap, newDfnMap); //$NON-NLS-1$ //$NON-NLS-2$
            readActualDataFromFolder(result, basePath, DatType.PRIMITIVE, "P", "", locked, loaded, newParentMap, newTypeMap, newDfnMap); //$NON-NLS-1$ //$NON-NLS-2$
            readActualDataFromFolder(result, basePath, DatType.PRIMITIVE48, "P", "48", locked, loaded, newParentMap, newTypeMap, newDfnMap); //$NON-NLS-1$ //$NON-NLS-2$


            // 5. Rebuilt the trees

        } else {
            // FIXME Needs Implementation!
        }

        return result;
    }


    private static void readVirtualDataFromFolder(
            int[] result,
            HashMap<String, TreeItem> parentMap,
            HashMap<String, DatType> typeMap,
            HashSet<String> locked,
            HashSet<String> loaded,
            HashMap<String, DatFile> existingMap,
            HashMap<String, DatFileName> dfnMap,
            final TreeItem treeItem,
            HashMap<String, HashSet<Composite3D>> openIn3DMap,
            HashMap<String, CompositeTab> openInTextMap,
            boolean checkForUnsaved
            ) {

        for (TreeItem ti : treeItem.getItems()) {
            DatFile df = (DatFile) ti.getData();
            if (checkForUnsaved && Project.getUnsavedFiles().contains(df)) {
                final String old = df.getOldName();
                locked.add(df.getNewName());
                locked.add(df.getOldName());
                parentMap.put(old, treeItem);
                typeMap.put(old, df.getType());
                existingMap.put(old, df);
                dfnMap.put(old, new DatFileName(new File(df.getNewName()).getName(), df.getDescription(), df.getType() == DatType.PRIMITIVE || df.getType() == DatType.PRIMITIVE48));
            } else {
                if (!new File(df.getOldName()).exists()) {
                    // 2. Check which "saved" files are not on the disk anymore (only for the statistic)
                    result[1] = result[1] + 1;
                } else {

                    // 2.5 Check which "saved" files are on the disk (only for the statistic) items in this set will not count for add
                    final String old = df.getOldName();
                    loaded.add(df.getNewName());
                    loaded.add(old);

                    // 3. Displayed, but unmodified files (Text+3D) need a remapping
                    HashSet<Composite3D> c3ds = new HashSet<Composite3D>();
                    for (OpenGLRenderer r : Editor3DWindow.getRenders()) {
                        Composite3D c3d = r.getC3D();
                        if (df.equals(c3d.getLockableDatFileReference())) {
                            c3ds.add(c3d);
                        }
                    }
                    for (EditorTextWindow w : Project.getOpenTextWindows()) {
                        for (CTabItem t : w.getTabFolder().getItems()) {
                            CompositeTab tab = (CompositeTab) t;
                            if (df.equals(tab.getState().getFileNameObj())) {
                                openInTextMap.put(old, tab);
                            }
                        }
                    }
                    openIn3DMap.put(old, c3ds);
                }
            }
        }
    }

    private static void readActualDataFromFolder(
            int[] result,
            String basePath,
            DatType type,
            String prefix1,
            String prefix2,
            HashSet<String> locked,
            HashSet<String> loaded,
            HashMap<String, TreeItem> newParentMap,
            HashMap<String, DatType> newTypeMap,
            HashMap<String, DatFileName> newDfnMap) {

        // FIXME Auto-generated method stub

        final File baseFolder = new File(basePath);

        if (prefix1.isEmpty() && prefix2.isEmpty()) {
            UTF8BufferedReader reader = null;
            StringBuilder titleSb = new StringBuilder();
            for (File f : baseFolder.listFiles()) {
                if (f.isFile() && f.getName().matches(".*.dat")) { //$NON-NLS-1$
                    final String path = f.getAbsolutePath();
                    if (locked.contains(path)) {
                        // File is locked by LPE, so don't parse it twice
                        result[2] = result[2] + 1;
                        continue;
                    }
                    if (!loaded.contains(path)) {
                        // The file is new
                        result[0] = result[0] + 1;
                    }
                    titleSb.setLength(0);
                    TreeItem treeItem = Editor3DWindow.getWindow().getProjectParts();
                    try {
                        reader = new UTF8BufferedReader(path);
                        String title = reader.readLine();
                        if (title != null) {
                            title = title.trim();
                            if (title.length() > 0) {
                                titleSb.append(" -"); //$NON-NLS-1$
                                titleSb.append(title.substring(1));
                            }
                        }
                        // Detect type
                        while (true) {
                            String typ = reader.readLine();
                            if (typ != null) {
                                typ = typ.trim();
                                if (!typ.startsWith("0")) { //$NON-NLS-1$
                                    break;
                                } else {
                                    int i1 = typ.indexOf("!LDRAW_ORG"); //$NON-NLS-1$
                                    if (i1 > -1) {
                                        int i2;
                                        i2 = typ.indexOf("Subpart"); //$NON-NLS-1$
                                        if (i2 > -1 && i1 < i2) {
                                            type = DatType.SUBPART;
                                            break;
                                        }
                                        i2 = typ.indexOf("Part"); //$NON-NLS-1$
                                        if (i2 > -1 && i1 < i2) {
                                            type = DatType.PART;
                                            break;
                                        }
                                        i2 = typ.indexOf("48_Primitive"); //$NON-NLS-1$
                                        if (i2 > -1 && i1 < i2) {
                                            type = DatType.PRIMITIVE48;
                                            break;
                                        }
                                        i2 = typ.indexOf("Primitive"); //$NON-NLS-1$
                                        if (i2 > -1 && i1 < i2) {
                                            type = DatType.PRIMITIVE;
                                            break;
                                        }
                                    }
                                }
                            } else {
                                break;
                            }
                        }
                        // Change treeItem according to type
                        switch (type) {
                        case PART:
                            treeItem = Editor3DWindow.getWindow().getProjectParts();
                            break;
                        case SUBPART:
                            treeItem = Editor3DWindow.getWindow().getProjectSubparts();
                            break;
                        case PRIMITIVE:
                            treeItem = Editor3DWindow.getWindow().getProjectPrimitives();
                            break;
                        case PRIMITIVE48:
                            treeItem = Editor3DWindow.getWindow().getProjectPrimitives48();
                            break;
                        default:
                            break;
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

                    newDfnMap.put(path, new DatFileName(f.getName(), titleSb.toString(), type == DatType.PRIMITIVE || type == DatType.PRIMITIVE48));
                    newParentMap.put(path, treeItem);
                    newTypeMap.put(path, type);
                }
            }
        } else {



        }

    }
}

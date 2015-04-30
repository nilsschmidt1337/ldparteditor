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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.dialogs.overwrite.OverwriteDialog;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.helpers.composite3d.TreeData;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.References;
import org.nschmidt.ldparteditor.widgets.TreeItem;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;


/**
 * @author nils
 *
 */
public enum ReferenceParser {
    INSTANCE;


    private static DatFile dfToParse = null;
    private static boolean skipOverwriteQuestion = false;
    private static boolean cancelled = false;

    /*

    Diese Funktion soll schauen, ob Abhängigkeiten vom Ursprungsmodell df zu Kind-/ und/oder Eltern-Modellen vorhanden sind

    Die Funktion gibt eine Matrix zurück mit dem folgenden Format

    ArrayList<DatFile>[0] = Zu entfernende Ziel-Parts
    ArrayList<DatFile>[1] = Zu entfernende Ziel-Subparts
    ArrayList<DatFile>[2] = Zu entfernende Ziel-Primitives
    ArrayList<DatFile>[3] = Zu entfernende Ziel-Primitives48
    ArrayList<DatFile>[4] = Zu entfernende Ziel-Primitives8

    ArrayList<DatFile>[5] = Zu erstellende Ziel-Parts (mit Abhängigkeit zu df)
    ArrayList<DatFile>[6] = Zu erstellende Ziel-Subparts (mit Abhängigkeit zu df)
    ArrayList<DatFile>[7] = Zu erstellende Ziel-Primitives (mit Abhängigkeit zu df)
    ArrayList<DatFile>[8] = Zu erstellende Ziel-Primitives48 (mit Abhängigkeit zu df)
    ArrayList<DatFile>[9] = Zu erstellende Ziel-Primitives8 (mit Abhängigkeit zu df)


    Wenn Dateien zu entfernen sind, soll zusätzlich gefragt werden, ob der Löschvorgang gewünscht ist.


     */
    public static ArrayList<ArrayList<DatFile>> checkForReferences(DatFile df, References refMode, TreeItem origin, TreeItem target, TreeItem secondSource) {
        ArrayList<ArrayList<DatFile>> result = new ArrayList<ArrayList<DatFile>>();
        for (int i = 0; i < 10; i++) {
            result.add(new ArrayList<DatFile>());
        }

        skipOverwriteQuestion = false;
        cancelled = false;

        final Editor3DWindow win = Editor3DWindow.getWindow();

        Set<String> alreadyParsed = new HashSet<String>();
        Set<DatFile> alreadyParsed2 = new HashSet<DatFile>();

        alreadyParsed.add(df.getShortName());
        alreadyParsed2.add(df);

        if (refMode == References.REQUIRED || refMode == References.REQUIRED_AND_RELATED) {
            List<String> dfSource = df.getSource();
            for (String line : dfSource) {
                dfToParse = df;
                DatFile ref = parseLine(line, alreadyParsed, true);
                if (ref != null && !alreadyParsed2.contains(ref)) {
                    alreadyParsed.add(ref.getShortName());
                    alreadyParsed2.add(ref);

                    DatType type = ref.getType();

                    TreeData td = win.getDatFileTreeData(ref);
                    Set<TreeItem> locs = td.getLocationsWithSameShortFilenames();

                    // We can simply copy it to the unofficial lib if "locs" is empty, because it is not located in another
                    if (type == DatType.PART) {
                        result.get(5).add(getUnofficialDatFileFromPath(ref));
                    } else if (type == DatType.SUBPART) {
                        result.get(6).add(getUnofficialDatFileFromPath(ref));
                    } else if (type == DatType.PRIMITIVE) {
                        result.get(7).add(getUnofficialDatFileFromPath(ref));
                    } else if (type == DatType.PRIMITIVE48) {
                        result.get(8).add(getUnofficialDatFileFromPath(ref));
                    } else if (type == DatType.PRIMITIVE8) {
                        result.get(9).add(getUnofficialDatFileFromPath(ref));
                    }

                    if (!locs.isEmpty()) {
                        for(Iterator<TreeItem> it = locs.iterator(); it.hasNext();) {
                            TreeItem item = it.next();
                            TreeItem parent = item.getParentItem();
                            if (parent.equals(win.getUnsaved())) {
                                continue;
                            }
                            boolean attemptingOverwrite = false;
                            if (parent.equals(win.getUnofficialParts())) {
                                result.get(0).add((DatFile) item.getData());
                                attemptingOverwrite = true;
                            } else if (parent.equals(win.getUnofficialSubparts())) {
                                result.get(1).add((DatFile) item.getData());
                                attemptingOverwrite = true;
                            } else if (parent.equals(win.getUnofficialPrimitives())) {
                                result.get(2).add((DatFile) item.getData());
                                attemptingOverwrite = true;
                            } else if (parent.equals(win.getUnofficialPrimitives48())) {
                                result.get(3).add((DatFile) item.getData());
                                attemptingOverwrite = true;
                            } else if (parent.equals(win.getUnofficialPrimitives8())) {
                                result.get(4).add((DatFile) item.getData());
                                attemptingOverwrite = true;
                            }
                            if (attemptingOverwrite && !skipOverwriteQuestion) {
                                OverwriteDialog od = new OverwriteDialog(ref.getShortName());
                                int result2 = od.open();
                                if (result2 == IDialogConstants.CANCEL_ID) {
                                    return result;
                                } else if (result2 == IDialogConstants.SKIP_ID) {
                                    skipOverwriteQuestion = true;
                                } else if (result2  == IDialogConstants.NO_ID) {
                                    for(int i = 0; i < 10; i++) {
                                        result.get(i).remove(item.getData());
                                    }
                                }
                            }
                        }
                    }

                    NLogger.debug(ReferenceParser.class, ref.getShortName());

                    recursiveParseREQUIRED(win, ref, result, alreadyParsed, alreadyParsed2);
                    if (cancelled) return result;
                }

            }

        }
        if (refMode == References.REQUIRED_AND_RELATED) {
            // TODO Needs implementation!


            // 1. Gather a FULL list of DatFiles which are located within the base path of the selected DatFile A

            // 2. Parse every DatFile B (except  A) to check if it refers to A  [means: parsed Line == DatFile A]

            // 3. If so, check existance of B inside the unofficial folder.
            // 3.1. If it exists, add it to the delete list


            // 1. Gather a full list..
            ArrayList<TreeItem> childs = origin.getItems();
            for (TreeItem child : childs) {
                @SuppressWarnings("unchecked")
                ArrayList<DatFile> entries = (ArrayList<DatFile>) child.getData();
                for (DatFile df2 : entries) {
                    // 2. Parse every DatFile
                    if (df2.equals(df)) continue;
                    ArrayList<String> source = df2.getSource();
                    for (String line : source) {
                        DatFile ref = parseLine(line, alreadyParsed, false);
                        // if (ref != null) NLogger.debug(ReferenceParser.class, ref.getShortName());
                        if (ref != null && ref.getShortName().equals(df.getShortName()) && !alreadyParsed2.contains(df2)) {
                            alreadyParsed.add(df2.getShortName());
                            alreadyParsed2.add(df2);

                            DatType type = df2.getType();

                            TreeData td = win.getDatFileTreeData(df2);
                            Set<TreeItem> locs = td.getLocationsWithSameShortFilenames();

                            // We can simply copy it to the unofficial lib if "locs" is empty, because it is not located in another
                            if (type == DatType.PART) {
                                result.get(5).add(getUnofficialDatFileFromPath(df2));
                            } else if (type == DatType.SUBPART) {
                                result.get(6).add(getUnofficialDatFileFromPath(df2));
                            } else if (type == DatType.PRIMITIVE) {
                                result.get(7).add(getUnofficialDatFileFromPath(df2));
                            } else if (type == DatType.PRIMITIVE48) {
                                result.get(8).add(getUnofficialDatFileFromPath(df2));
                            } else if (type == DatType.PRIMITIVE8) {
                                result.get(9).add(getUnofficialDatFileFromPath(df2));
                            }

                            if (!locs.isEmpty()) {
                                for(Iterator<TreeItem> it = locs.iterator(); it.hasNext();) {
                                    TreeItem item = it.next();
                                    TreeItem parent2 = item.getParentItem();
                                    if (parent2.equals(win.getUnsaved())) {
                                        continue;
                                    }
                                    boolean attemptingOverwrite = false;
                                    if (parent2.equals(win.getUnofficialParts())) {
                                        result.get(0).add((DatFile) item.getData());
                                        attemptingOverwrite = true;
                                    } else if (parent2.equals(win.getUnofficialSubparts())) {
                                        result.get(1).add((DatFile) item.getData());
                                        attemptingOverwrite = true;
                                    } else if (parent2.equals(win.getUnofficialPrimitives())) {
                                        result.get(2).add((DatFile) item.getData());
                                        attemptingOverwrite = true;
                                    } else if (parent2.equals(win.getUnofficialPrimitives48())) {
                                        result.get(3).add((DatFile) item.getData());
                                        attemptingOverwrite = true;
                                    } else if (parent2.equals(win.getUnofficialPrimitives8())) {
                                        result.get(4).add((DatFile) item.getData());
                                        attemptingOverwrite = true;
                                    }
                                    if (attemptingOverwrite && !skipOverwriteQuestion) {
                                        OverwriteDialog od = new OverwriteDialog(df2.getShortName());
                                        int result2 = od.open();
                                        if (result2 == IDialogConstants.CANCEL_ID) {
                                            return result;
                                        } else if (result2 == IDialogConstants.SKIP_ID) {
                                            skipOverwriteQuestion = true;
                                        } else if (result2  == IDialogConstants.NO_ID) {
                                            for(int i = 0; i < 10; i++) {
                                                result.get(i).remove(item.getData());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static void recursiveParseREQUIRED(Editor3DWindow win, DatFile df, ArrayList<ArrayList<DatFile>> result, Set<String> alreadyParsed, Set<DatFile> alreadyParsed2) {
        List<String> dfSource = df.getSource();
        for (String line : dfSource) {
            dfToParse = df;
            DatFile ref = parseLine(line, alreadyParsed, true);
            if (ref != null && !alreadyParsed2.contains(ref)) {
                alreadyParsed.add(ref.getShortName());
                alreadyParsed2.add(ref);

                DatType type = ref.getType();

                TreeData td = win.getDatFileTreeData(ref);
                Set<TreeItem> locs = td.getLocationsWithSameShortFilenames();

                // We can simply copy it to the unofficial lib if "locs" is empty, because it is not located in another
                if (type == DatType.PART) {
                    result.get(5).add(getUnofficialDatFileFromPath(ref));
                } else if (type == DatType.SUBPART) {
                    result.get(6).add(getUnofficialDatFileFromPath(ref));
                } else if (type == DatType.PRIMITIVE) {
                    result.get(7).add(getUnofficialDatFileFromPath(ref));
                } else if (type == DatType.PRIMITIVE48) {
                    result.get(8).add(getUnofficialDatFileFromPath(ref));
                } else if (type == DatType.PRIMITIVE8) {
                    result.get(9).add(getUnofficialDatFileFromPath(ref));
                }

                if (!locs.isEmpty()) {
                    for(Iterator<TreeItem> it = locs.iterator(); it.hasNext();) {
                        TreeItem item = it.next();
                        TreeItem parent = item.getParentItem();
                        if (parent.equals(win.getUnsaved())) {
                            continue;
                        }
                        boolean attemptingOverwrite = false;
                        if (parent.equals(win.getUnofficialParts())) {
                            result.get(0).add((DatFile) item.getData());
                            attemptingOverwrite = true;
                        } else if (parent.equals(win.getUnofficialSubparts())) {
                            result.get(1).add((DatFile) item.getData());
                            attemptingOverwrite = true;
                        } else if (parent.equals(win.getUnofficialPrimitives())) {
                            result.get(2).add((DatFile) item.getData());
                            attemptingOverwrite = true;
                        } else if (parent.equals(win.getUnofficialPrimitives48())) {
                            result.get(3).add((DatFile) item.getData());
                            attemptingOverwrite = true;
                        } else if (parent.equals(win.getUnofficialPrimitives8())) {
                            result.get(4).add((DatFile) item.getData());
                            attemptingOverwrite = true;
                        }
                        if (attemptingOverwrite && !skipOverwriteQuestion) {
                            OverwriteDialog od = new OverwriteDialog(ref.getShortName());
                            int result2 = od.open();
                            if (result2 == IDialogConstants.CANCEL_ID) {
                                cancelled = true;
                                return;
                            } else if (result2 == IDialogConstants.SKIP_ID) {
                                skipOverwriteQuestion = true;
                            } else if (result2  == IDialogConstants.NO_ID) {
                                for(int i = 0; i < 10; i++) {
                                    result.get(i).remove(item.getData());
                                }
                            }
                        }

                    }
                }

                NLogger.debug(ReferenceParser.class, ref.getShortName());

                recursiveParseREQUIRED(win, ref, result, alreadyParsed, alreadyParsed2);
                if (cancelled) return;
            }
        }
    }

    private static DatFile getUnofficialDatFileFromPath(DatFile dfo) {
        String path = dfo.getNewName();
        if (path.startsWith(WorkbenchManager.getUserSettingState().getLdrawFolderPath())) {
            path = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + path.substring(WorkbenchManager.getUserSettingState().getLdrawFolderPath().length());
        } else if (path.startsWith(Project.getProjectPath())) {
            path = WorkbenchManager.getUserSettingState().getUnofficialFolderPath() + path.substring(Project.getProjectPath().length());
        }
        DatFile df = new DatFile(path);
        df.setText(dfo.getSourceText());
        df.setType(dfo.getType());
        return df;
    }

    private static DatFile parseLine(String line, Set<String> alreadyParsed, boolean showNotFoundWarning) {
        String[] data_segments = line.trim().split("\\s+"); //$NON-NLS-1$
        char c;
        if (!(data_segments.length > 0 && data_segments[0].length() == 1 && Character.isDigit(c = data_segments[0].charAt(0)))) {
            return null;
        }
        int linetype = Character.getNumericValue(c);
        // Parse the line according to its type
        switch (linetype) {
        case 0:
            return parse_Reference(data_segments, alreadyParsed, true, showNotFoundWarning);
        case 1:
            return parse_Reference(data_segments, alreadyParsed, false, showNotFoundWarning);
        default:
            return null;
        }

    }

    private static DatFile parse_Reference(String[] data_segments, Set<String> alreadyParsed, boolean couldBeTEXMAP, boolean showNotFoundWarning) {
        final int texmapOffset = couldBeTEXMAP ? 2 : 0;
        if (data_segments.length >= 15 + texmapOffset) {
            if (couldBeTEXMAP && !"!:".equals(data_segments[1])) { //$NON-NLS-1$
                return null;
            }
            // Check file existance
            boolean fileExists = true;
            StringBuilder sb = new StringBuilder();
            for (int s = 14 + texmapOffset; s < data_segments.length - 1; s++) {
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
                shortFilename = shortFilename.replace("s\\\\", "S" + File.separator).replace("\\\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            if (showNotFoundWarning) {
                if (alreadyParsed.contains(shortFilename)) {
                    return null;
                } else {
                    alreadyParsed.add(shortFilename);
                }
            }
            String shortFilename2 = shortFilename.startsWith("S" + File.separator) ? "s" + shortFilename.substring(1) : shortFilename; //$NON-NLS-1$ //$NON-NLS-2$

            File fileToOpen = null;
            DatType dt = null;

            String[] prefix = new String[]{WorkbenchManager.getUserSettingState().getLdrawFolderPath(), WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), Project.getProjectPath()};
            String[] middle = new String[]{File.separator + "PARTS", File.separator + "parts", File.separator + "P", File.separator + "p"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            String[] suffix = new String[]{File.separator + shortFilename, File.separator + shortFilename2};

            for (int a1 = 0; a1 < prefix.length; a1++) {
                if (a1 == 1) continue;
                String s1 = prefix[a1];
                for (int a2 = 0; a2 < middle.length; a2++) {
                    String s2 = middle[a2];
                    switch (a2) {
                    case 0:
                    case 1:
                        if (shortFilename.startsWith("S" + File.separator) || shortFilename2.startsWith("s" + File.separator)) { //$NON-NLS-1$ //$NON-NLS-2$
                            dt = DatType.SUBPART;
                        } else {
                            dt = DatType.PART;
                        }
                        break;
                    case 2:
                    case 3:
                        if (shortFilename.startsWith("48")) { //$NON-NLS-1$
                            dt = DatType.PRIMITIVE48;
                        } else if (shortFilename.startsWith("8")) { //$NON-NLS-1$
                            dt = DatType.PRIMITIVE8;
                        } else {
                            dt = DatType.PRIMITIVE;
                        }
                    default:
                        break;
                    }
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
            for (DatFile df : Project.getUnsavedFiles()) {
                for (int a1 = 0; a1 < prefix.length; a1++) {
                    if (a1 == 1) continue;
                    String s1 = prefix[a1];
                    for (int a2 = 0; a2 < middle.length; a2++) {
                        String s2 = middle[a2];
                        switch (a2) {
                        case 0:
                        case 1:
                            if (shortFilename.startsWith("S" + File.separator) || shortFilename2.startsWith("s" + File.separator)) { //$NON-NLS-1$ //$NON-NLS-2$
                                dt = DatType.SUBPART;
                            } else {
                                dt = DatType.PART;
                            }
                            break;
                        case 2:
                        case 3:
                            if (shortFilename.startsWith("48")) { //$NON-NLS-1$
                                dt = DatType.PRIMITIVE48;
                            } else if (shortFilename.startsWith("8")) { //$NON-NLS-1$
                                dt = DatType.PRIMITIVE8;
                            } else {
                                dt = DatType.PRIMITIVE;
                            }
                        default:
                            break;
                        }
                        for (int a3 = 0; a3 < suffix.length; a3++) {
                            String s3 = suffix[a3];
                            if (df.getNewName().equals(s1 + s2 + s3)) {
                                return df;
                            }
                        }
                    }
                }
            }
            if (fileExists) {
                DatFile result = new DatFile(fileToOpen.getAbsolutePath());
                result.setType(dt);
                return result;
            } else {
                String s1 = prefix[1];
                for (int a2 = 0; a2 < middle.length; a2++) {
                    String s2 = middle[a2];
                    for (int a3 = 0; a3 < suffix.length; a3++) {
                        String s3 = suffix[a3];
                        File f = new File(s1 + s2 + s3);
                        fileExists = f.exists() && f.isFile();
                        if (fileExists) break;
                    }
                    if (fileExists) break;
                }
                for (DatFile df : Project.getUnsavedFiles()) {
                    for (int a2 = 0; a2 < middle.length; a2++) {
                        String s2 = middle[a2];
                        for (int a3 = 0; a3 < suffix.length; a3++) {
                            String s3 = suffix[a3];
                            if (df.getNewName().equals(s1 + s2 + s3)) {
                                fileExists = true;
                                break;
                            }
                        }
                        if (fileExists) break;
                    }
                }
                if (!fileExists && showNotFoundWarning) {
                    MessageBox messageBoxError = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_WARNING | SWT.OK);
                    messageBoxError.setText(I18n.DIALOG_NotFoundRequiredTitle);
                    Object[] messageArguments = {shortFilename, dfToParse.getShortName()};
                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                    formatter.setLocale(MyLanguage.LOCALE);
                    formatter.applyPattern(I18n.DIALOG_NotFoundRequired);
                    messageBoxError.setMessage(formatter.format(messageArguments));
                    messageBoxError.open();
                }
            }
        }
        return null;
    }
}

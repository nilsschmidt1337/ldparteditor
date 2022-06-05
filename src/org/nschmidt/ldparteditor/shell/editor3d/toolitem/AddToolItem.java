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
package org.nschmidt.ldparteditor.shell.editor3d.toolitem;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.data.BFC;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GDataBFC;
import org.nschmidt.ldparteditor.data.ParsingResult;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.OpenInWhat;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editormeta.EditorMetaWindow;
import org.nschmidt.ldparteditor.shell.searchnreplace.SearchWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.UTF8PrintWriter;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public class AddToolItem extends ToolItem {

    private static final EditorMetaWindow metaWindow = new EditorMetaWindow();

    private static boolean addingSomething = false;
    private static boolean addingVertices = false;
    private static boolean addingLines = false;
    private static boolean addingTriangles = false;
    private static boolean addingQuads = false;
    private static boolean addingCondlines = false;
    private static boolean addingDistance = false;
    private static boolean addingProtractor = false;
    private static boolean addingSubfiles = false;

    private static final NButton[] btnAddCommentPtr = new NButton[1];
    private static final NButton[] btnAddVertexPtr = new NButton[1];
    private static final NButton[] btnAddPrimitivePtr = new NButton[1];
    private static final NButton[] btnAddLinePtr = new NButton[1];
    private static final NButton[] btnAddTrianglePtr = new NButton[1];
    private static final NButton[] btnAddQuadPtr = new NButton[1];
    private static final NButton[] btnAddCondlinePtr = new NButton[1];
    private static final NButton[] btnAddDistancePtr = new NButton[1];
    private static final NButton[] btnAddProtractorPtr = new NButton[1];

    public AddToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createWidgets(this);
        addListeners();
    }

    private static void createWidgets(AddToolItem addToolItem) {
        NButton btnAddComment = new NButton(addToolItem, Cocoa.getStyle());
        AddToolItem.btnAddCommentPtr[0] = btnAddComment;
        KeyStateManager.addTooltipText(btnAddComment, I18n.E3D_ADD_COMMENT, Task.ADD_COMMENTS);
        btnAddComment.setImage(ResourceManager.getImage("icon16_addcomment.png")); //$NON-NLS-1$

        NButton btnAddVertex = new NButton(addToolItem, SWT.TOGGLE | Cocoa.getStyle());
        AddToolItem.btnAddVertexPtr[0] = btnAddVertex;
        KeyStateManager.addTooltipText(btnAddVertex, I18n.E3D_ADD_VERTEX, Task.ADD_VERTEX);
        btnAddVertex.setImage(ResourceManager.getImage("icon16_addvertex.png")); //$NON-NLS-1$

        NButton btnAddPrimitive = new NButton(addToolItem, SWT.TOGGLE | Cocoa.getStyle());
        AddToolItem.btnAddPrimitivePtr[0] = btnAddPrimitive;
        btnAddPrimitive.setToolTipText(I18n.E3D_ADD_SUBPART);
        btnAddPrimitive.setImage(ResourceManager.getImage("icon16_addprimitive.png")); //$NON-NLS-1$

        NButton btnAddLine = new NButton(addToolItem, SWT.TOGGLE | Cocoa.getStyle());
        AddToolItem.btnAddLinePtr[0] = btnAddLine;
        KeyStateManager.addTooltipText(btnAddLine, I18n.E3D_ADD_LINE, Task.ADD_LINE);
        btnAddLine.setImage(ResourceManager.getImage("icon16_addline.png")); //$NON-NLS-1$

        NButton btnAddTriangle = new NButton(addToolItem, SWT.TOGGLE | Cocoa.getStyle());
        AddToolItem.btnAddTrianglePtr[0] = btnAddTriangle;
        KeyStateManager.addTooltipText(btnAddTriangle, I18n.E3D_ADD_TRIANGLE, Task.ADD_TRIANGLE);
        btnAddTriangle.setImage(ResourceManager.getImage("icon16_addtriangle.png")); //$NON-NLS-1$

        NButton btnAddQuad = new NButton(addToolItem, SWT.TOGGLE | Cocoa.getStyle());
        AddToolItem.btnAddQuadPtr[0] = btnAddQuad;
        KeyStateManager.addTooltipText(btnAddQuad, I18n.E3D_ADD_QUAD, Task.ADD_QUAD);
        btnAddQuad.setImage(ResourceManager.getImage("icon16_addquad.png")); //$NON-NLS-1$

        NButton btnAddCondline = new NButton(addToolItem, SWT.TOGGLE | Cocoa.getStyle());
        AddToolItem.btnAddCondlinePtr[0] = btnAddCondline;
        KeyStateManager.addTooltipText(btnAddCondline, I18n.E3D_ADD_CONDLINE, Task.ADD_CONDLINE);
        btnAddCondline.setImage(ResourceManager.getImage("icon16_addcondline.png")); //$NON-NLS-1$

        NButton btnAddDistance = new NButton(addToolItem, SWT.TOGGLE | Cocoa.getStyle());
        AddToolItem.btnAddDistancePtr[0] = btnAddDistance;
        KeyStateManager.addTooltipText(btnAddDistance, I18n.E3D_ADD_DISTANCE, Task.ADD_DISTANCE);
        btnAddDistance.setImage(ResourceManager.getImage("icon16_adddistance.png")); //$NON-NLS-1$

        NButton btnAddProtractor = new NButton(addToolItem, SWT.TOGGLE | Cocoa.getStyle());
        AddToolItem.btnAddProtractorPtr[0] = btnAddProtractor;
        KeyStateManager.addTooltipText(btnAddProtractor, I18n.E3D_ADD_PROTRACTOR, Task.ADD_PROTRACTOR);
        btnAddProtractor.setImage(ResourceManager.getImage("icon16_addprotractor.png")); //$NON-NLS-1$
    }

    private static void addListeners() {
        widgetUtil(btnAddCommentPtr[0]).addSelectionListener(e -> {
            if (!metaWindow.isOpened()) {
                metaWindow.run();
            } else {
                metaWindow.open();
            }
        });
        widgetUtil(btnAddVertexPtr[0]).addSelectionListener(e -> {
            resetAddState();
            clickSingleBtn(btnAddVertexPtr[0]);
            setAddingVertices(btnAddVertexPtr[0].getSelection());
            setAddingSomething(isAddingVertices());
            Editor3DWindow.getWindow().regainFocus();
        });
        widgetUtil(btnAddPrimitivePtr[0]).addSelectionListener(e -> {

            resetAddState();
            setAddingSubfiles(btnAddPrimitivePtr[0].getSelection());

            clickSingleBtn(btnAddPrimitivePtr[0]);

            if (Project.getFileToEdit() != null) {
                final boolean readOnly = Project.getFileToEdit().isReadOnly();
                final VertexManager vm = Project.getFileToEdit().getVertexManager();

                if (!vm.getSelectedData().isEmpty() || !vm.getSelectedVertices().isEmpty()) {

                    final boolean insertSubfileFromSelection;
                    final boolean cutTheSelection;

                    MessageBox messageBox1 = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                    messageBox1.setText(I18n.E3D_SUBFILE_FROM_SELECTION);
                    messageBox1.setMessage(I18n.E3D_SUBFILE_FROM_SELECTION_QUESTION);
                    int result1 = messageBox1.open();
                    insertSubfileFromSelection = result1 == SWT.YES;
                    if (result1 != SWT.NO && result1 != SWT.YES) {
                        resetAddState();
                        btnAddPrimitivePtr[0].setSelection(false);
                        setAddingSubfiles(false);
                        addingSomething = false;
                        Editor3DWindow.getWindow().regainFocus();
                        return;
                    }

                    if (insertSubfileFromSelection) {
                        if (!readOnly) {
                            MessageBox messageBox2 = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                            messageBox2.setText(I18n.E3D_SUBFILE_FROM_SELECTION);
                            messageBox2.setMessage(I18n.E3D_SUBFILE_FROM_SELECTION_QUESTION_CUT);
                            int result2 = messageBox2.open();
                            cutTheSelection = result2 == SWT.YES;
                            if (result2 != SWT.NO && result2 != SWT.YES) {
                                resetAddState();
                                btnAddPrimitivePtr[0].setSelection(false);
                                setAddingSubfiles(false);
                                addingSomething = false;
                                Editor3DWindow.getWindow().regainFocus();
                                return;
                            }
                        } else {
                            cutTheSelection = false;
                        }

                        vm.addSnapshot();
                        vm.copy();
                        vm.extendClipboardContent(cutTheSelection);

                        FileDialog fd = new FileDialog(Editor3DWindow.getWindow().getShell(), SWT.SAVE);
                        fd.setText(I18n.E3D_SAVE_DAT_FILE_AS);

                        File f1 = new File(Project.getFileToEdit().getNewName()).getParentFile();
                        if (f1.exists()) {
                            fd.setFilterPath(f1.getAbsolutePath());
                        } else {
                            fd.setFilterPath(Project.getLastVisitedPath());
                        }

                        String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                        fd.setFilterExtensions(filterExt);
                        String[] filterNames = {I18n.E3D_LDRAW_SOURCE_FILE, I18n.E3D_ALL_FILES};
                        fd.setFilterNames(filterNames);

                        while (true) {
                            try {
                                String selected = fd.open();
                                if (selected != null) {

                                    if (Editor3DWindow.getWindow().isFileNameAllocated(selected, new DatFile(selected), true)) {
                                        MessageBox messageBox3 = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
                                        messageBox3.setText(I18n.DIALOG_ALREADY_ALLOCATED_NAME_TITLE);
                                        messageBox3.setMessage(I18n.DIALOG_ALREADY_ALLOCATED_NAME);

                                        int result3 = messageBox3.open();

                                        if (result3 == SWT.CANCEL) {
                                            break;
                                        } else if (result3 == SWT.RETRY) {
                                            continue;
                                        }
                                    }

                                    SearchWindow sw = Editor3DWindow.getWindow().getSearchWindow();
                                    if (sw != null) {
                                        sw.setTextComposite(null);
                                        sw.setScopeToAll();
                                    }


                                    boolean hasIOerror = false;
                                    try (UTF8PrintWriter r = new UTF8PrintWriter(selected)) {

                                        String typeSuffix = ""; //$NON-NLS-1$
                                        String folderPrefix = ""; //$NON-NLS-1$
                                        String subfilePrefix = ""; //$NON-NLS-1$
                                        String path = new File(selected).getParent();

                                        if (path.endsWith(File.separator + "S") || path.endsWith(File.separator + "s")) { //$NON-NLS-1$ //$NON-NLS-2$
                                            typeSuffix = "Unofficial_Subpart"; //$NON-NLS-1$
                                            folderPrefix = "s\\"; //$NON-NLS-1$
                                            subfilePrefix = "~"; //$NON-NLS-1$
                                        } else if (path.endsWith(File.separator + "P" + File.separator + "48") || path.endsWith(File.separator + "p" + File.separator + "48")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                            typeSuffix = "Unofficial_48_Primitive"; //$NON-NLS-1$
                                            folderPrefix = "48\\"; //$NON-NLS-1$
                                        } else if (path.endsWith(File.separator + "P" + File.separator + "8") || path.endsWith(File.separator + "p" + File.separator + "8")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                            typeSuffix = "Unofficial_8_Primitive"; //$NON-NLS-1$
                                            folderPrefix = "8\\"; //$NON-NLS-1$
                                        } else if (path.endsWith(File.separator + "P") || path.endsWith(File.separator + "p")) { //$NON-NLS-1$ //$NON-NLS-2$
                                            typeSuffix = "Unofficial_Primitive"; //$NON-NLS-1$
                                        }

                                        r.println("0 " + subfilePrefix); //$NON-NLS-1$
                                        r.println("0 Name: " + folderPrefix + new File(selected).getName()); //$NON-NLS-1$
                                        String ldrawName = WorkbenchManager.getUserSettingState().getLdrawUserName();
                                        if (ldrawName == null || ldrawName.isEmpty()) {
                                            r.println("0 Author: " + WorkbenchManager.getUserSettingState().getRealUserName()); //$NON-NLS-1$
                                        } else {
                                            r.println("0 Author: " + WorkbenchManager.getUserSettingState().getRealUserName() + " [" + WorkbenchManager.getUserSettingState().getLdrawUserName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        }
                                        r.println("0 !LDRAW_ORG " + typeSuffix); //$NON-NLS-1$
                                        String license = WorkbenchManager.getUserSettingState().getLicense();
                                        if (license == null || license.isEmpty()) {
                                            r.println("0 !LICENSE Licensed under CC BY 4.0 : see CAreadme.txt"); //$NON-NLS-1$
                                        } else {
                                            r.println(license);
                                        }
                                        r.println(""); //$NON-NLS-1$

                                        BFC bfcType = BFC.NOCERTIFY;
                                        GData g1 = Project.getFileToEdit().getDrawChainStart();
                                        while ((g1 = g1.getNext()) != null) {
                                            if (g1.type() == 6) {
                                                BFC bfc = ((GDataBFC) g1).getType();
                                                switch (bfc) {
                                                case CCW_CLIP:
                                                    bfcType = bfc;
                                                    r.println("0 BFC CERTIFY CCW"); //$NON-NLS-1$
                                                    break;
                                                case CW_CLIP:
                                                    bfcType = bfc;
                                                    r.println("0 BFC CERTIFY CW"); //$NON-NLS-1$
                                                    break;
                                                default:
                                                    break;
                                                }
                                                if (bfcType != BFC.NOCERTIFY) break;
                                            }
                                        }

                                        if (bfcType == BFC.NOCERTIFY) {
                                            r.println("0 BFC NOCERTIFY"); //$NON-NLS-1$
                                        }

                                        r.println(""); //$NON-NLS-1$
                                        r.println(vm.getClipboardText());
                                        r.flush();
                                    } catch (Exception ex1) {
                                        hasIOerror = true;
                                    }

                                    if (hasIOerror) {
                                        MessageBox messageBoxError = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
                                        messageBoxError.setText(I18n.DIALOG_ERROR);
                                        messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                                        messageBoxError.open();
                                    } else {

                                        if (cutTheSelection) {
                                            // Insert a reference to the subfile in the old file
                                            Set<String> alreadyParsed = new HashSet<>();
                                            alreadyParsed.add(Project.getFileToEdit().getShortName());
                                            final GColour col16 = LDConfig.getColour16();
                                            List<ParsingResult> subfileLine = DatParser
                                                    .parseLine(
                                                            "1 16 0 0 0 1 0 0 0 1 0 0 0 1 s\\" + new File(selected).getName(), -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, Project.getFileToEdit(), false, alreadyParsed); //$NON-NLS-1$
                                            GData1 gd1 = (GData1) subfileLine.get(0).getGraphicalData();
                                            if (gd1 != null) {
                                                if (InsertAtCursorPositionToolItem.isInsertingAtCursorPosition()) {
                                                    Project.getFileToEdit().insertAfterCursor(gd1);
                                                } else {
                                                    Set<GData> sd = vm.getSelectedData();
                                                    GData g2 = Project.getFileToEdit().getDrawChainStart();
                                                    GData whereToInsert = null;
                                                    while ((g2 = g2.getNext()) != null) {
                                                        if (sd.contains(g2)) {
                                                            whereToInsert = g2.getBefore();
                                                            break;
                                                        }
                                                    }
                                                    if (whereToInsert == null) {
                                                        whereToInsert = Project.getFileToEdit().getDrawChainTail();
                                                    }
                                                    Project.getFileToEdit().insertAfter(whereToInsert, gd1);
                                                }
                                            }
                                            vm.delete(false, true);
                                        }

                                        DatFile df = Editor3DWindow.getWindow().openDatFile(OpenInWhat.EDITOR_TEXT_AND_3D, selected, false);
                                        if (df != null) {
                                            NewOpenSaveProjectToolItem.addRecentFile(df);
                                            final File f2 = new File(df.getNewName());
                                            if (f2.getParentFile() != null) {
                                                Project.setLastVisitedPath(f2.getParentFile().getAbsolutePath());
                                            }
                                        }
                                    }
                                    Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                                }
                            } catch (Exception ex2) {
                                NLogger.error(AddToolItem.class, ex2);
                            }
                            break;
                        }
                        resetAddState();
                        btnAddPrimitivePtr[0].setSelection(false);
                        setAddingSubfiles(false);
                        addingSomething = false;
                        Editor3DWindow.getWindow().regainFocus();
                        return;
                    }
                }
            }
            setAddingSomething(isAddingSubfiles());
            Editor3DWindow.getWindow().regainFocus();
        });
        widgetUtil(btnAddLinePtr[0]).addSelectionListener(e -> {
            resetAddState();
            setAddingLines(btnAddLinePtr[0].getSelection());
            setAddingSomething(isAddingLines());
            clickSingleBtn(btnAddLinePtr[0]);
            Editor3DWindow.getWindow().regainFocus();
        });
        widgetUtil(btnAddTrianglePtr[0]).addSelectionListener(e -> {
            resetAddState();
            setAddingTriangles(btnAddTrianglePtr[0].getSelection());
            setAddingSomething(isAddingTriangles());
            clickSingleBtn(btnAddTrianglePtr[0]);
            Editor3DWindow.getWindow().regainFocus();
        });
        widgetUtil(btnAddQuadPtr[0]).addSelectionListener(e -> {
            resetAddState();
            setAddingQuads(btnAddQuadPtr[0].getSelection());
            setAddingSomething(isAddingQuads());
            clickSingleBtn(btnAddQuadPtr[0]);
            Editor3DWindow.getWindow().regainFocus();
        });
        widgetUtil(btnAddCondlinePtr[0]).addSelectionListener(e -> {
            resetAddState();
            setAddingCondlines(btnAddCondlinePtr[0].getSelection());
            setAddingSomething(isAddingCondlines());
            clickSingleBtn(btnAddCondlinePtr[0]);
            Editor3DWindow.getWindow().regainFocus();
        });
        widgetUtil(btnAddDistancePtr[0]).addSelectionListener(e -> {
            resetAddState();
            setAddingDistance(btnAddDistancePtr[0].getSelection());
            setAddingSomething(isAddingDistance());
            clickSingleBtn(btnAddDistancePtr[0]);
            Editor3DWindow.getWindow().regainFocus();
        });
        widgetUtil(btnAddProtractorPtr[0]).addSelectionListener(e -> {
            resetAddState();
            setAddingProtractor(btnAddProtractorPtr[0].getSelection());
            setAddingSomething(isAddingProtractor());
            clickSingleBtn(btnAddProtractorPtr[0]);
            Editor3DWindow.getWindow().regainFocus();
        });
    }

    public static boolean isAddingSomething() {
        return addingSomething;
    }

    private static void setAddingSomething(boolean addingSomething) {
        AddToolItem.addingSomething = addingSomething;
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
            renderer.getC3D().getLockableDatFileReference().getVertexManager().clearSelection();
        }
    }

    public static boolean isAddingVertices() {
        return addingVertices;
    }

    private static void setAddingVertices(boolean addingVertices) {
        AddToolItem.addingVertices = addingVertices;
    }

    public static boolean isAddingLines() {
        return addingLines;
    }

    private static void setAddingLines(boolean addingLines) {
        AddToolItem.addingLines = addingLines;
    }

    public static boolean isAddingTriangles() {
        return addingTriangles;
    }

    private static void setAddingTriangles(boolean addingTriangles) {
        AddToolItem.addingTriangles = addingTriangles;
    }

    public static boolean isAddingQuads() {
        return addingQuads;
    }

    private static void setAddingQuads(boolean addingQuads) {
        AddToolItem.addingQuads = addingQuads;
    }

    public static boolean isAddingCondlines() {
        return addingCondlines;
    }

    private static void setAddingCondlines(boolean addingCondlines) {
        AddToolItem.addingCondlines = addingCondlines;
    }

    public static boolean isAddingSubfiles() {
        return addingSubfiles;
    }

    private static void setAddingSubfiles(boolean addingSubfiles) {
        AddToolItem.addingSubfiles = addingSubfiles;
    }

    public static boolean isAddingDistance() {
        return addingDistance;
    }

    private static void setAddingDistance(boolean addingDistance) {
        AddToolItem.addingDistance = addingDistance;
    }

    public static boolean isAddingProtractor() {
        return addingProtractor;
    }

    private static void setAddingProtractor(boolean addingProtractor) {
        AddToolItem.addingProtractor = addingProtractor;
    }

    public static void disableAddAction() {
        addingSomething = false;
        addingVertices = false;
        addingLines = false;
        addingTriangles = false;
        addingQuads = false;
        addingCondlines = false;
        addingSubfiles = false;
        addingDistance = false;
        addingProtractor = false;
        btnAddVertexPtr[0].setSelection(false);
        btnAddLinePtr[0].setSelection(false);
        btnAddTrianglePtr[0].setSelection(false);
        btnAddQuadPtr[0].setSelection(false);
        btnAddCondlinePtr[0].setSelection(false);
        btnAddDistancePtr[0].setSelection(false);
        btnAddProtractorPtr[0].setSelection(false);
        btnAddPrimitivePtr[0].setSelection(false);

        for (DatFile df : Project.getOpenedFiles()) {
            final VertexManager vm2 = df.getVertexManager();
            final Vertex[] vertices = new Vertex[] {df.getObjVertex1(), df.getObjVertex2(), df.getObjVertex3(), df.getObjVertex4()};
            for (Vertex v : vertices) {
                if (v != null) {
                    vm2.getSelectedVertices().remove(v);
                }
            }

            df.setObjVertex1(null);
            df.setObjVertex2(null);
            df.setObjVertex3(null);
            df.setObjVertex4(null);
            df.setNearestObjVertex1(null);
            df.setNearestObjVertex2(null);
        }
    }

    public static void unselectAddSubfile() {
        resetAddState();
        btnAddPrimitivePtr[0].setSelection(false);
        setAddingSubfiles(false);
        setAddingSomething(false);
    }

    private static void resetAddState() {
        setAddingSubfiles(false);
        setAddingVertices(false);
        setAddingLines(false);
        setAddingTriangles(false);
        setAddingQuads(false);
        setAddingCondlines(false);
        setAddingDistance(false);
        setAddingProtractor(false);
        for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
            Composite3D c3d = renderer.getC3D();
            DatFile df = c3d.getLockableDatFileReference();
            df.setObjVertex1(null);
            df.setObjVertex2(null);
            df.setObjVertex3(null);
            df.setObjVertex4(null);
            df.setNearestObjVertex1(null);
            df.setNearestObjVertex2(null);
        }
    }

    public static void setAddState(int type) {
        if (isAddingSomething()) {
            resetAddState();
            btnAddVertexPtr[0].setSelection(false);
            btnAddLinePtr[0].setSelection(false);
            btnAddTrianglePtr[0].setSelection(false);
            btnAddQuadPtr[0].setSelection(false);
            btnAddCondlinePtr[0].setSelection(false);
            btnAddDistancePtr[0].setSelection(false);
            btnAddProtractorPtr[0].setSelection(false);
            btnAddPrimitivePtr[0].setSelection(false);
            setAddingSomething(false);
        }
        switch (type) {
        case 0:
            btnAddCommentPtr[0].notifyListeners(SWT.Selection, new Event());
            break;
        case 1:
            setAddingVertices(!isAddingVertices());
            btnAddVertexPtr[0].setSelection(isAddingVertices());
            setAddingSomething(isAddingVertices());
            clickSingleBtn(btnAddVertexPtr[0]);
            break;
        case 2:
            setAddingLines(!isAddingLines());
            btnAddLinePtr[0].setSelection(isAddingLines());
            setAddingSomething(isAddingLines());
            clickSingleBtn(btnAddLinePtr[0]);
            break;
        case 3:
            setAddingTriangles(!isAddingTriangles());
            btnAddTrianglePtr[0].setSelection(isAddingTriangles());
            setAddingSomething(isAddingTriangles());
            clickSingleBtn(btnAddTrianglePtr[0]);
            break;
        case 4:
            setAddingQuads(!isAddingQuads());
            btnAddQuadPtr[0].setSelection(isAddingQuads());
            setAddingSomething(isAddingQuads());
            clickSingleBtn(btnAddQuadPtr[0]);
            break;
        case 5:
            setAddingCondlines(!isAddingCondlines());
            btnAddCondlinePtr[0].setSelection(isAddingCondlines());
            setAddingSomething(isAddingCondlines());
            clickSingleBtn(btnAddCondlinePtr[0]);
            break;
        case 6:
            setAddingDistance(!isAddingDistance());
            btnAddDistancePtr[0].setSelection(isAddingDistance());
            setAddingSomething(isAddingDistance());
            clickSingleBtn(btnAddDistancePtr[0]);
            break;
        case 7:
            setAddingProtractor(!isAddingProtractor());
            btnAddProtractorPtr[0].setSelection(isAddingProtractor());
            setAddingSomething(isAddingProtractor());
            clickSingleBtn(btnAddProtractorPtr[0]);
            break;
        default:
            break;
        }
    }
}

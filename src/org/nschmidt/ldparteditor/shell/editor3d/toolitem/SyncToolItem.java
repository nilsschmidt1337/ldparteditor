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

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.LibraryManager;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.compositetext.SubfileCompiler;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.widget.NButton;

public class SyncToolItem extends ToolItem {

    private static final NButton[] btnSyncPtr = new NButton[1];

    public SyncToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createWidgets(this);
        addListeners();
    }

    private static void createWidgets(SyncToolItem syncToolItem) {
        NButton btnSync = new NButton(syncToolItem, Cocoa.getStyle());
        btnSyncPtr[0] = btnSync;
        btnSync.setToolTipText(I18n.E3D_SYNC_FOLDERS);
        btnSync.setImage(ResourceManager.getImage("icon16_sync.png")); //$NON-NLS-1$
    }

    private static void addListeners() {
        widgetUtil(btnSyncPtr[0]).addSelectionListener(e -> {
            final Editor3DWindow win = Editor3DWindow.getWindow();
            win.resetSearch();
            int[][] stats = new int[15][3];
            stats[0] = LibraryManager.syncProjectElements(win.getProject());
            stats[5] = LibraryManager.syncUnofficialParts(win.getUnofficialParts());
            stats[6] = LibraryManager.syncUnofficialSubparts(win.getUnofficialSubparts());
            stats[7] = LibraryManager.syncUnofficialPrimitives(win.getUnofficialPrimitives());
            stats[8] = LibraryManager.syncUnofficialHiResPrimitives(win.getUnofficialPrimitives48());
            stats[9] = LibraryManager.syncUnofficialLowResPrimitives(win.getUnofficialPrimitives8());
            stats[10] = LibraryManager.syncOfficialParts(win.getOfficialParts());
            stats[11] = LibraryManager.syncOfficialSubparts(win.getOfficialSubparts());
            stats[12] = LibraryManager.syncOfficialPrimitives(win.getOfficialPrimitives());
            stats[13] = LibraryManager.syncOfficialHiResPrimitives(win.getOfficialPrimitives48());
            stats[14] = LibraryManager.syncOfficialLowResPrimitives(win.getOfficialPrimitives8());

            int additions = 0;
            int deletions = 0;
            int conflicts = 0;
            for (int[] folderStat : stats) {
                additions += folderStat[0];
                deletions += folderStat[1];
                conflicts += folderStat[2];
            }

            win.getSearchText().setText(" "); //$NON-NLS-1$
            win.getSearchText().setText(""); //$NON-NLS-1$

            Set<DatFile> dfs = new HashSet<>();
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                dfs.add(renderer.getC3D().getLockableDatFileReference());
            }

            for (EditorTextWindow w1 : Project.getOpenTextWindows()) {
                for (CTabItem t1 : w1.getTabFolder().getItems()) {
                    DatFile txtDat1 = ((CompositeTab) t1).getState().getFileNameObj();
                    if (txtDat1 != null) {
                        dfs.add(txtDat1);
                    }
                }
            }

            for (DatFile df : dfs) {
                SubfileCompiler.compile(df, false, false);
            }

            for (EditorTextWindow w2 : Project.getOpenTextWindows()) {
                for (CTabItem t2 : w2.getTabFolder().getItems()) {
                    DatFile txtDat2 = ((CompositeTab) t2).getState().getFileNameObj();
                    if (txtDat2 != null) {
                        ((CompositeTab) t2).parseForErrorAndHints();
                        ((CompositeTab) t2).getTextComposite().redraw();

                        ((CompositeTab) t2).getState().getTab().setText(((CompositeTab) t2).getState().getFilenameWithStar());
                    }
                }
            }

            win.updateTreeUnsavedEntries();
            win.getPartsTree().getTree().showItem(win.getPartsTree().getTree().getItem(0));

            MessageBox messageBox = new MessageBox(win.getShell(), SWT.ICON_INFORMATION | SWT.OK);
            messageBox.setText(I18n.DIALOG_SYNC_TITLE);

            Object[] messageArguments = {additions, deletions, conflicts};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.DIALOG_SYNC);
            messageBox.setMessage(formatter.format(messageArguments));

            messageBox.open();
            win.regainFocus();
        });
    }
}

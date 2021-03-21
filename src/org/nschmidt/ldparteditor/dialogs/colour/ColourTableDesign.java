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
package org.nschmidt.ldparteditor.dialogs.colour;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widgets.Tree;
import org.nschmidt.ldparteditor.widgets.TreeColumn;
import org.nschmidt.ldparteditor.widgets.TreeItem;

/**
 * @author nils
 *
 */
class ColourTableDesign extends ApplicationWindow {

    private final GColour[] refCol;

    protected ColourTableDesign(Shell parentShell, final GColour[] refCol) {
        super(parentShell);
        this.refCol = refCol;
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite cmp_container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;
        cmp_container.setLayout(gridLayout);

        final Set<String> names = new TreeSet<>(View.getNameMap().values());
        final Tree tree = new Tree(cmp_container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, names.size());
        tree.setLinesVisible(true);
        tree.setHeaderVisible(true);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        TreeColumn trclmn_Description = new TreeColumn(tree, SWT.NONE);
        trclmn_Description.setWidth(300);
        trclmn_Description.setText(I18n.COLOUR_Description);

        TreeColumn trclmn_Number = new TreeColumn(tree, SWT.NONE);
        trclmn_Number.setWidth(60);

        TreeColumn trclmn_Colour = new TreeColumn(tree, SWT.NONE);
        trclmn_Colour.setWidth(100);
        trclmn_Colour.setText(I18n.COLOUR_Colour);

        Set<Integer> ldConfIndices = View.getColourMap().keySet();
        Map<String, Integer> nameToIndex = new HashMap<>();
        for (Integer index : ldConfIndices) {
            nameToIndex.put(View.getLDConfigColourName(index), index);
        }
        for (String name : names) {
            TreeItem trtm_Colour = new TreeItem(tree);
            Integer id = nameToIndex.get(name);
            trtm_Colour.setText(new String[] { name, id + "", ""  }); //$NON-NLS-1$ //$NON-NLS-2$
            trtm_Colour.setVisible(true);
            final GColour gColour2 = View.getLDConfigColour(id);
            final Color col = SWTResourceManager.getColor((int) (gColour2.getR() * 255f), (int) (gColour2.getG() * 255f), (int) (gColour2.getB() * 255f));
            trtm_Colour.setData(new Object[] {gColour2, col});
        }

        tree.build();

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                final TreeItem selection;
                if (tree.getSelectionCount() == 1 && (selection = tree.getSelection()[0]).getData() != null) {
                    refCol[0] = (GColour) ((Object[]) selection.getData())[0];
                    Display.getCurrent().timerExec(100, new Runnable() {
                        @Override
                        public void run() {
                            close();
                        }
                    });
                }
            }
        });

        cmp_container.pack();

        getShell().addShellListener(new ShellAdapter() {
            @Override
            public void shellActivated(ShellEvent e) {
                updateColours(tree);
                tree.redraw();
            }
        });

        return cmp_container;
    }

    private void updateColours(Tree tree) {
        for(TreeItem ti : tree.getItems()) {
            updateColoursHelper(ti);
        }
    }

    private void updateColoursHelper(TreeItem ti) {
        org.eclipse.swt.widgets.TreeItem key = ti.getParent().getMapInv().get(ti);
        if (key != null && ti.getData() != null && ((Object[]) ti.getData()).length == 2) {
            key.setBackground(2, (Color) ((Object[]) ti.getData())[1]);
        }
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }
}

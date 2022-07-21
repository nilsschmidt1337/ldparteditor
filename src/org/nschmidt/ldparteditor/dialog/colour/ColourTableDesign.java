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
package org.nschmidt.ldparteditor.dialog.colour;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.widget.Tree;
import org.nschmidt.ldparteditor.widget.TreeColumn;
import org.nschmidt.ldparteditor.widget.TreeItem;

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
        final ColourTableDesign me = this;
        Composite cmpContainer = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;
        cmpContainer.setLayout(gridLayout);

        final SortedSet<String> names = new TreeSet<>(LDConfig.getNameMap().values());
        final Tree tree = new Tree(cmpContainer, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, names.size());
        tree.setLinesVisible(true);
        tree.setHeaderVisible(true);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        TreeColumn trclmnDescription = new TreeColumn(tree, SWT.NONE);
        trclmnDescription.setWidth(300);
        trclmnDescription.setText(I18n.COLOUR_DESCRIPTION);

        TreeColumn trclmnNumber = new TreeColumn(tree, SWT.NONE);
        trclmnNumber.setWidth(60);

        TreeColumn trclmnColour = new TreeColumn(tree, SWT.NONE);
        trclmnColour.setWidth(100);
        trclmnColour.setText(I18n.COLOUR_COLOUR);

        Set<Integer> ldConfIndices = LDConfig.getColourMap().keySet();
        Map<String, Integer> nameToIndex = new HashMap<>();
        for (Integer index : ldConfIndices) {
            nameToIndex.put(LDConfig.getColourName(index), index);
        }
        for (String name : names) {
            TreeItem trtmColour = new TreeItem(tree);
            Integer id = nameToIndex.get(name);
            trtmColour.setText(new String[] { name, id + "", ""  }); //$NON-NLS-1$ //$NON-NLS-2$
            trtmColour.setVisible(true);
            final GColour gColour2 = LDConfig.getColour(id);
            final Color col = SWTResourceManager.getColor((int) (gColour2.getR() * 255f), (int) (gColour2.getG() * 255f), (int) (gColour2.getB() * 255f));
            trtmColour.setData(new Object[] {gColour2, col});
        }

        tree.build();

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                final TreeItem selection;
                if (tree.getSelectionCount() == 1 && (selection = tree.getSelection()[0]).getData() != null) {
                    refCol[0] = (GColour) ((Object[]) selection.getData())[0];
                    Display.getCurrent().timerExec(100, me::close);
                }
            }
        });

        cmpContainer.pack();

        getShell().addShellListener(new ShellAdapter() {
            @Override
            public void shellActivated(ShellEvent e) {
                updateColours(tree);
                tree.redraw();
            }
        });

        return cmpContainer;
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
}

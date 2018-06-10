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
package org.nschmidt.ldparteditor.widgets;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionListener;

/**
 * @author nils
 *
 */
public class Tree {

    private final HashMap<TreeItem, org.eclipse.swt.widgets.TreeItem> SWTFromTi = new HashMap<TreeItem, org.eclipse.swt.widgets.TreeItem>();
    private final org.eclipse.swt.widgets.Tree tree;
    private final ArrayList<TreeItem> items = new ArrayList<TreeItem>();
    private final HashMap<org.eclipse.swt.widgets.TreeItem, TreeItem> tiFromSWT = new HashMap<org.eclipse.swt.widgets.TreeItem, TreeItem>();
    private org.eclipse.swt.widgets.TreeItem[] itemsSWT;

    int counter;

    /**
     * @param parent
     * @param columnCount
     *
     */
    public Tree(Composite parent, int columnCount, int initialSize) {
        tree = new org.eclipse.swt.widgets.Tree(parent, columnCount);
        setItemsSWT(new org.eclipse.swt.widgets.TreeItem[initialSize]);
        for (int i = 0; i < initialSize; i++) {
            itemsSWT[i] = new org.eclipse.swt.widgets.TreeItem(tree, SWT.NONE);
            ;
        }
    }

    public TreeItem[] getSelection() {
        int length = tree.getSelection().length;
        TreeItem[] result = new TreeItem[length];
        org.eclipse.swt.widgets.TreeItem[] sel = tree.getSelection();
        for (int i = 0; i < length; i++) {
            result[i] = tiFromSWT.get(sel[i]);
        }
        return result;
    }

    public void addSelectionListener(WidgetSelectionListener selectionListener) {
        tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                selectionListener.widgetSelected(e);
            }
        });
    }

    public void addMouseListener(MouseAdapter mouseAdapter) {
        tree.addMouseListener(mouseAdapter);
    }

    public int getSelectionCount() {
        return tree.getSelectionCount();
    }

    public void setLinesVisible(boolean b) {
        tree.setLinesVisible(b);

    }

    public void setHeaderVisible(boolean b) {
        tree.setHeaderVisible(b);
    }

    public void addListener(int eventType, Listener listener) {
        tree.addListener(eventType, listener);
    }

    public void addMenuDetectListener(MenuDetectListener mdl) {
        tree.addMenuDetectListener(mdl);
    }

    public void setLayoutData(GridData layoutData) {
        tree.setLayoutData(layoutData);
    }

    public void setMenu(Menu menu) {
        tree.setMenu(menu);
    }

    public void redraw() {
        tree.redraw();
    }

    public void update() {
        tree.update();
    }

    public void build() {
        counter = 0;
        tiFromSWT.clear();
        SWTFromTi.clear();
        for (TreeItem t : items) {
            t.build(0);
        }
        int size = tree.getItemCount();
        int cols = tree.getColumnCount();
        if (cols == 0) {
            for (int i = counter; i < size; i++) {
                org.eclipse.swt.widgets.TreeItem t = tree.getItem(i);
                if ("".equals(t.getText()))break; //$NON-NLS-1$
                t.setImage((Image) null);
                t.setText(""); //$NON-NLS-1$
            }
        } else {
            for (int i = counter; i < size; i++) {
                org.eclipse.swt.widgets.TreeItem t = tree.getItem(i);
                if ("".equals(t.getText(0)))break; //$NON-NLS-1$
                t.setImage((Image) null);
                for (int j = 0; j < cols; j++)
                    t.setText(j, ""); //$NON-NLS-1$
            }
        }
    }

    public HashMap<org.eclipse.swt.widgets.TreeItem, TreeItem> getMap() {
        return tiFromSWT;
    }

    public HashMap<TreeItem, org.eclipse.swt.widgets.TreeItem> getMapInv() {
        return SWTFromTi;
    }

    public org.eclipse.swt.widgets.Tree getTree() {
        return tree;
    }

    /**
     * @return the items
     */
    public ArrayList<TreeItem> getItems() {
        return items;
    }

    /**
     * @return the itemsSWT
     */
    public org.eclipse.swt.widgets.TreeItem[] getItemsSWT() {
        return itemsSWT;
    }

    /**
     * @param itemsSWT
     *            the itemsSWT to set
     */
    public void setItemsSWT(org.eclipse.swt.widgets.TreeItem[] itemsSWT) {
        this.itemsSWT = itemsSWT;
    }

    public void setSelection(TreeItem treeItem) {
        if (SWTFromTi.containsKey(treeItem)) {
            tree.select(SWTFromTi.get(treeItem));
            tree.showSelection();
        }
    }
}

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
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.enums.View;

/**
 * @author nils
 *
 */
class ColourTableDesign extends Dialog {

    final ScrolledComposite[] scmp = new ScrolledComposite[1];
    final Table[] tbl_Colours = new Table[1];
    final Text[] txt_Search = new Text[1];
    final GColour[] refCol;
    final TreeSet<String>[] names;

    @SuppressWarnings("unchecked")
    protected ColourTableDesign(Shell parentShell, final GColour[] refCol) {
        super(parentShell);
        this.refCol = refCol;
        this.names = new TreeSet[1];
    }


    private int bestWidth = 0;

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        Composite cmp_container = parent; // (Composite) super.createDialogArea(parent);
        cmp_container.setLayout(new GridLayout());

        final ScrolledComposite composite = new ScrolledComposite(cmp_container, SWT.V_SCROLL);
        this.scmp[0] = composite;
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final Table table = new Table(composite, SWT.NO_SCROLL | SWT.FULL_SELECTION);
        table.setLayout(new GridLayout());
        this.tbl_Colours[0] = table;
        table.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                refCol[0] = (GColour) table.getSelection()[0].getData();
                parent.getShell().getParent().getShell().close();
            }
        });
        table.setHeaderVisible(false);

        table.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
			}

			@Override
			public void focusGained(FocusEvent e) {
                composite.forceFocus();
			}
		});

        composite.setContent(table);
        composite.setExpandHorizontal(true);
        composite.setExpandVertical(true);
        composite.setAlwaysShowScrollBars(true);

        {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setWidth(table.getItemHeight());
        }
        {
            @SuppressWarnings("unused")
            TableColumn column = new TableColumn(table, SWT.NONE);
        }
        {
            @SuppressWarnings("unused")
            TableColumn column = new TableColumn(table, SWT.NONE);
        }
        Set<Integer> ldConfIndices = View.getColourMap().keySet();
        names[0] = new TreeSet<String>(View.getNameMap().values());
        HashMap<String, Integer> nameToIndex = new HashMap<String, Integer>();
        for (Integer index : ldConfIndices) {
            nameToIndex.put(View.getLDConfigColourName(index), index);
        }
        int longestNameLenght = 0;
        String longestName = "                      "; //$NON-NLS-1$
        for (String name : names[0]) {
            TableItem item = new TableItem(table, SWT.NONE);
            Integer id = nameToIndex.get(name);
            final GColour gColour2 = View.getLDConfigColour(id);
            item.setData(gColour2);
            final Color col = SWTResourceManager.getColor((int) (gColour2.getR() * 255f), (int) (gColour2.getG() * 255f), (int) (gColour2.getB() * 255f));
            item.setBackground(0, col);
            item.setText(1, name + "   "); //$NON-NLS-1$
            item.setText(2, id + ""); //$NON-NLS-1$

            int l = name.length();
            if (l > longestNameLenght) {
                longestNameLenght = l;
                longestName = name;
            }
        }

        {
            Label tmp = new Label(cmp_container, SWT.NONE);
            GC gc = new GC(tmp);
            Point size = gc.textExtent(longestName + "           99999"); //$NON-NLS-1$
            bestWidth = size.x + table.getItemHeight()+ ((GridLayout) cmp_container.getLayout()).horizontalSpacing * 4;
            gc.dispose ();
            tmp.dispose();
        }

        table.getColumn(1).pack();
        table.getColumn(2).pack();

        Text txt_Search = new Text(cmp_container, SWT.NONE);
        this.txt_Search[0] = txt_Search;
        txt_Search.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));

        cmp_container.layout();
        composite.setMinSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return cmp_container;
    }


    /**
     * Create contents of the button bar (no buttons)
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(bestWidth, bestWidth * 2);
    }

}

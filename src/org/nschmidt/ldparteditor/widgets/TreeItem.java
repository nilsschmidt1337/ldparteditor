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
import java.util.Arrays;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;

/**
 * @author nils
 *
 */
public class TreeItem {

    private String[] text;
    private Object data;
    private ArrayList<TreeItem> items = new ArrayList<TreeItem>();
    private Tree parent;
    private TreeItem parentItem;
    private Image image;
    private boolean visible;
    private boolean shown;

    /**
     * @param parent
     * @param style
     */
    public TreeItem(Tree parent, int style) {
        this.parent = parent;
        this.parent.getItems().add(this);
        this.visible = true;
        this.shown = true;
    }

    /**
     * @param parentItem
     * @param style
     */
    public TreeItem(TreeItem parentItem, int style) {
        this.parent = parentItem.getParent();
        this.parentItem = parentItem;
        this.parentItem.getItems().add(this);
        this.visible = true;
        this.shown = true;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text[0];
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText(String text) {
        this.text = new String[] { text };
    }

    /**
     * @return the data
     */
    public Object getData() {
        return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * @return the image
     */
    public Image getImage() {
        return image;
    }

    /**
     * @param image
     *            the image to set
     */
    public void setImage(Image image) {
        this.image = image;
    }

    public ArrayList<TreeItem> getItems() {
        return items;
    }

    public void removeAll() {
        items.clear();
    }

    /**
     * ONLY FOR PARSER MECHANICS!
     *
     * @param start
     * @param end
     */
    public void removeWithinPosition(StyledText compositeText, int startOffset, int endOffset, int length) {

        ArrayList<TreeItem> nl = new ArrayList<TreeItem>(items.size() + 100);

        for (TreeItem t : items) {
            try {
                int o = (Integer) t.getData();
                int lstart = compositeText.getLineAtOffset(startOffset) + 1;
                int litem = 0;

                if (o > -1 && o < startOffset && (litem = compositeText.getLineAtOffset(o) + 1) != lstart) {
                    t.setText("line " + litem + "   [" + o + "]", 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ I18N Needs translation!
                    nl.add(t);
                }

                // else if (o == startOffset) {

                // - > Parse it!

                // }
                else if (o >= endOffset || o > startOffset && o + length >= endOffset) {

                    int newOffset = o + length;
                    int lnew = compositeText.getLineAtOffset(Math.max(newOffset, 0)) + 1;

                    if (newOffset > endOffset && lstart != lnew) {
                        t.setData(newOffset);
                        t.setText("line " + lnew + "   [" + newOffset + "]", 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ I18N Needs translation!
                        nl.add(t);
                    }
                }
            } catch (IllegalArgumentException iae) {
                // Skip if the index was out bounds
            }
        }
        items = nl;
    }

    /**
     * ONLY FOR PARSER MECHANICS!
     */
    public void sortItems() {
        if (items.size() > 0) {
            ArrayList<TreeItem> nl = new ArrayList<TreeItem>(items.size() + 100);
            HashMap<Integer, ArrayList<TreeItem>> itemsPerLine = new HashMap<Integer, ArrayList<TreeItem>>();
            SortedSet<Integer> lines = new TreeSet<Integer>();
            for (TreeItem t : items) {
                Integer litem = (Integer) t.getData();
                if (!lines.contains(litem)) {
                    itemsPerLine.put(litem, new ArrayList<TreeItem>());
                    lines.add(litem);
                }
                itemsPerLine.get(litem).add(t);
            }
            for (Integer l : lines) {
                nl.addAll(itemsPerLine.get(l));
            }
            items = nl;
        }
    }

    private void setText(String text, int i) {
        this.text[i] = text;
    }

    public Tree getParent() {
        return parent;
    }

    public TreeItem getParentItem() {
        return parentItem;
    }

    public void setText(String[] strings) {
        text = strings;
    }

    public String getText(int i) {
        return text[i];
    }

    public void dispose() {

    }

    public void build(int step) {
        org.eclipse.swt.widgets.TreeItem[] itemsSWT = parent.getItemsSWT();
        if (parent.counter > itemsSWT.length - 1) {
            int doubleLength = itemsSWT.length * 2;
            parent.setItemsSWT(Arrays.copyOf(itemsSWT, doubleLength));
            itemsSWT = parent.getItemsSWT();
            org.eclipse.swt.widgets.Tree tree = parent.getTree();
            for (int i = parent.counter; i < doubleLength; i++) {
                itemsSWT[i] = new org.eclipse.swt.widgets.TreeItem(tree, SWT.NONE);
            }
        }
        org.eclipse.swt.widgets.TreeItem t = parent.getItemsSWT()[parent.counter];
        parent.counter++;
        parent.getMap().put(t, this);
        parent.getMapInv().put(this, t);
        t.setImage(image);
        StringBuilder offset = new StringBuilder();
        if (visible && items.size() > 0) {
            switch (step) {
            case 0:
                offset.append("⇣ "); //$NON-NLS-1$
                break;
            case 1:
                offset.append(" ⇣ "); //$NON-NLS-1$
                break;
            case 2:
                offset.append("    "); //$NON-NLS-1$
                break;
            default:
                break;
            }
            offset.append(text[0]);
            String tmp = text[0];
            text[0] = offset.toString();
            t.setText(text);
            text[0] = tmp;
            for (TreeItem ti : items) {
                if (ti.isShown()) ti.build(step + 1);
            }
        } else {
            switch (step) {
            case 0:
                offset.append("⇢ "); //$NON-NLS-1$
                break;
            case 1:
                offset.append(" ⇢ "); //$NON-NLS-1$
                break;
            case 2:
                offset.append("    "); //$NON-NLS-1$
                break;
            default:
                break;
            }
            offset.append(text[0]);
            String tmp = text[0];
            text[0] = offset.toString();
            t.setText(text);
            text[0] = tmp;
        }
    }

    /**
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible
     *            the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isShown() {
        return shown;
    }

    public void setShown(boolean shown) {
        this.shown = shown;
    }

}

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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.GColour;

/**
 * @author nils
 *
 */
class ColourTableDialog extends ColourTableDesign {

    protected ColourTableDialog(Shell parentShell, final GColour[] refCol) {
        super(parentShell, refCol);
    }

    @Override
    public int open() {
        super.create();
        txt_Search[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String criteria = ".*" + txt_Search[0].getText() + ".*"; //$NON-NLS-1$//$NON-NLS-2$
                String criteria2 =  txt_Search[0].getText() + ".*"; //$NON-NLS-1$
                String criteria3 =  txt_Search[0].getText();
                try {
                    criteria.matches(criteria);
                    criteria2.matches(criteria2);
                    criteria3.matches(criteria3);
                } catch (Exception ex) {
                    criteria = ".*"; //$NON-NLS-1$
                    criteria2 = ".*"; //$NON-NLS-1$
                }
                int index = 0;
                int count = names[0].size();
                for (index = 0; index < count; index++) {
                    GColour c = (GColour) tbl_Colours[0].getItem(index).getData();
                    if ((c.getColourNumber() + "").matches(criteria3)) { //$NON-NLS-1$
                        tbl_Colours[0].select(index);
                        scmp[0].getVerticalBar().setSelection(index * tbl_Colours[0].getItemHeight());
                        Event event = new Event();
                        scmp[0].getVerticalBar().notifyListeners(SWT.Selection, event);
                        return;
                    }
                }
                index = 0;
                for (String name : names[0]) {
                    if (name.matches(criteria3)) {
                        tbl_Colours[0].select(index);
                        scmp[0].getVerticalBar().setSelection(index * tbl_Colours[0].getItemHeight());
                        Event event = new Event();
                        scmp[0].getVerticalBar().notifyListeners(SWT.Selection, event);
                        return;
                    }
                    index++;
                }
                index = 0;
                for (String name : names[0]) {
                    GColour c = (GColour) tbl_Colours[0].getItem(index).getData();
                    if ((c.getColourNumber() + " " + name).matches(criteria2)) { //$NON-NLS-1$
                        tbl_Colours[0].select(index);
                        scmp[0].getVerticalBar().setSelection(index * tbl_Colours[0].getItemHeight());
                        Event event = new Event();
                        scmp[0].getVerticalBar().notifyListeners(SWT.Selection, event);
                        return;
                    }
                    index++;
                }
                index = 0;
                for (String name : names[0]) {
                    GColour c = (GColour) tbl_Colours[0].getItem(index).getData();
                    if ((c.getColourNumber() + " " + name).matches(criteria)) { //$NON-NLS-1$
                        tbl_Colours[0].select(index);
                        scmp[0].getVerticalBar().setSelection(index * tbl_Colours[0].getItemHeight());
                        Event event = new Event();
                        scmp[0].getVerticalBar().notifyListeners(SWT.Selection, event);
                        break;
                    }
                    index++;
                }
            }
        });
        return super.open();
    }

}

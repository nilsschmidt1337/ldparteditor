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
package org.nschmidt.ldparteditor.dialogs.txt2dat;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.composite3d.Txt2DatSettings;

/**
 *
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. It overrides the {@code open()} method to invoke the
 * listener definitions ;)
 *
 * @author nils
 *
 */
public class Txt2DatDialog extends Txt2DatDesign {

    /**
     * Create the dialog.
     *
     * @param parentShell
     * @param es
     */
    public Txt2DatDialog(Shell parentShell, Txt2DatSettings ts) {
        super(parentShell, ts);
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        spn_interpolateFlatness[0].addValueChangeListener(spn -> ts.setInterpolateFlatness(spn.getValue()));
        spn_flatness[0].addValueChangeListener(spn -> ts.setFlatness(spn.getValue()));
        spn_fontHeight[0].addValueChangeListener(spn -> ts.setFontHeight(spn.getValue()));
        spn_deltaAngle[0].addValueChangeListener(spn -> ts.setDeltaAngle(spn.getValue()));
        WidgetUtil(btn_chooseFont[0]).addSelectionListener(e -> {
            final FontDialog fd = new FontDialog(getShell());
            final FontData data = ts.getFontData();
            fd.setFontList(new FontData[]{data});
            if (ts.getRGB() != null) {
                fd.setRGB(ts.getRGB());
            }
            ts.setFontData(fd.open());
            ts.setRGB(fd.getRGB());
        });
        txt_text[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                ts.setText(txt_text[0].getText());
            }
        });
        return super.open();
    }

}

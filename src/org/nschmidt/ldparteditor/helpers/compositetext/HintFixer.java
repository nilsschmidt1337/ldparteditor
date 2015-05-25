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
package org.nschmidt.ldparteditor.helpers.compositetext;

import java.io.File;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.DatType;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.text.HeaderState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * @author nils
 *
 */
final class HintFixer {

    public static String fix(int lineNumber, String sort, String line, String text, DatFile datFile) {
        HeaderState h = HeaderState.state();
        DatType type = datFile.getType();
        int s = Integer.parseInt(sort, 16);
        switch (s) {
        case 0: // The title is missing
            text = QuickFixer.insertBeforeLine(1, "0 " + I18n.HINTFIXER_Title, text); //$NON-NLS-1$
            break;
        case 1: // Invalid header line
            text = QuickFixer.setLine(lineNumber + 1, "<rm>", text); //$NON-NLS-1$
            break;
        case 16: // The filename is missing
            int l = 1;
            if (h.hasTITLE())
                l = h.getLineTITLE();
            if (type == DatType.SUBPART)
                text = QuickFixer.insertAfterLine(l, "0 Name: s\\\\" + new File(datFile.getNewName()).getName() + "<br>", text); //$NON-NLS-1$ //$NON-NLS-2$
            else if (type == DatType.PRIMITIVE8)
                text = QuickFixer.insertAfterLine(l, "0 Name: 8\\\\" + new File(datFile.getNewName()).getName() + "<br>", text); //$NON-NLS-1$ //$NON-NLS-2$
            else if (type == DatType.PRIMITIVE48)
                text = QuickFixer.insertAfterLine(l, "0 Name: 48\\\\" + new File(datFile.getNewName()).getName() + "<br>", text); //$NON-NLS-1$ //$NON-NLS-2$
            else
                text = QuickFixer.insertAfterLine(l, "0 Name: " + new File(datFile.getNewName()).getName() + "<br>", text); //$NON-NLS-1$ //$NON-NLS-2$
            break;
        case 32: // The author name is missing
            l = 1;
            if (h.hasTITLE())
                l = h.getLineTITLE();
            if (h.hasNAME())
                l = h.getLineNAME();
            String ldrawName = WorkbenchManager.getUserSettingState().getLdrawUserName();
            if (ldrawName == null || ldrawName.isEmpty()) {
                text = QuickFixer.insertAfterLine(l, "0 Author: " + WorkbenchManager.getUserSettingState().getRealUserName() + "<br>", text); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                text = QuickFixer.insertAfterLine(l,
                        "0 Author: " + WorkbenchManager.getUserSettingState().getRealUserName() + " [" + WorkbenchManager.getUserSettingState().getLdrawUserName() + "]<br>", text); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            break;
        case 48: // The part type information is missing
            l = 1;
            if (h.hasTITLE())
                l = h.getLineTITLE();
            if (h.hasNAME())
                l = h.getLineNAME();
            if (h.hasAUTHOR())
                l = h.getLineAUTHOR();
            if (type == DatType.SUBPART)
                text = QuickFixer.insertAfterLine(l, "0 !LDRAW_ORG Unofficial_Subpart<br>", text); //$NON-NLS-1$
            else if (type == DatType.PRIMITIVE)
                text = QuickFixer.insertAfterLine(l, "0 !LDRAW_ORG Unofficial_Primitive<br>", text); //$NON-NLS-1$
            else if (type == DatType.PRIMITIVE8)
                text = QuickFixer.insertAfterLine(l, "0 !LDRAW_ORG Unofficial_8_Primitive<br>", text); //$NON-NLS-1$
            else if (type == DatType.PRIMITIVE48)
                text = QuickFixer.insertAfterLine(l, "0 !LDRAW_ORG Unofficial_48_Primitive<br>", text); //$NON-NLS-1$
            else
                text = QuickFixer.insertAfterLine(l, "0 !LDRAW_ORG Unofficial_Part<br>", text); //$NON-NLS-1$
            break;
        case 64: // The license information is missing
            l = 1;
            if (h.hasTITLE())
                l = h.getLineTITLE();
            if (h.hasNAME())
                l = h.getLineNAME();
            if (h.hasAUTHOR())
                l = h.getLineAUTHOR();
            if (h.hasTYPE())
                l = h.getLineTYPE();
            String license = WorkbenchManager.getUserSettingState().getLicense();
            if (license == null || license.isEmpty()) {
                text = QuickFixer.insertAfterLine(l, "0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt <br>", text); //$NON-NLS-1$
            } else {
                text = QuickFixer.insertAfterLine(l, license + "<br>", text); //$NON-NLS-1$
            }
            break;
        case 96: // The BFC information is missing
            l = 1;
            if (h.hasTITLE())
                l = h.getLineTITLE();
            if (h.hasNAME())
                l = h.getLineNAME();
            if (h.hasAUTHOR())
                l = h.getLineAUTHOR();
            if (h.hasTYPE())
                l = h.getLineTYPE();
            if (h.hasLICENSE())
                l = h.getLineLICENSE();
            text = QuickFixer.insertAfterLine(l, "<br>0 BFC CERTIFY CCW<br>", text); //$NON-NLS-1$
            break;
        default:
            break;
        }
        return text;
    }

}

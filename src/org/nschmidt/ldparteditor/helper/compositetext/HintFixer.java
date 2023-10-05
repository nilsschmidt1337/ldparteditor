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
package org.nschmidt.ldparteditor.helper.compositetext;

import java.io.File;
import java.math.BigDecimal;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.DatType;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.text.HeaderState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

enum HintFixer {
    INSTANCE;

    public static String fix(int lineNumber, String sort, String line, String text, DatFile datFile, HeaderState h) {
        DatType type = datFile.getType();
        int s = Integer.parseInt(sort, 16);
        int l = 0;
        switch (s) {
        case 0: // The title is missing
            text = QuickFixer.insertAfterLine(0, "0 " + I18n.HINTFIXER_TITLE + "<br>", text); //$NON-NLS-1$ //$NON-NLS-2$
            break;
        case 1: // Invalid header line
            text = QuickFixer.setLine(lineNumber + 1, "<rm>", text); //$NON-NLS-1$
            break;
        case 16: // The filename is missing
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
        case 66: // CSG parts are needed to be shown to enable optimization
            EditorTextWindow.openIn3D(datFile);
            break;
        case 48: // The part type information is missing
            if (h.hasTITLE())
                l = h.getLineTITLE();
            if (h.hasNAME())
                l = h.getLineNAME();
            if (h.hasAUTHOR())
                l = h.getLineAUTHOR();
            if (!h.hasTYPE() && h.getLineTYPE() > -1) {
                l = h.getLineTYPE();
                text = QuickFixer.setLine(l, "<rm>", text); //$NON-NLS-1$
            }
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
        case 49: // The "Unofficial_" prefix is missing from the part type
            if (h.hasTYPE() && !h.hasUNOFFICIAL() && h.getLineTYPE() > -1) {
                l = h.getLineTYPE();
                final String typeLine = QuickFixer.getLine(l - 1, text);
                text = QuickFixer.setLine(l, typeLine.replace("!LDRAW_ORG ", "!LDRAW_ORG Unofficial_"), text); //$NON-NLS-1$ //$NON-NLS-2$
            }
            break;
        case 50: // There is an invalid UPDATE in the part type
            if (h.hasTYPE() && h.getLineTYPE() > -1) {
                l = h.getLineTYPE();
                final String typeLine = QuickFixer.getLine(l - 1, text);
                final int updateIndex = typeLine.indexOf("UPDATE"); //$NON-NLS-1$
                if (updateIndex > -1) {
                    text = QuickFixer.setLine(l, typeLine.substring(0, updateIndex).trim(), text);
                }
            }
            break;
        case 64: // The license information is missing
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
                text = QuickFixer.insertAfterLine(l, "0 !LICENSE Licensed under CC BY 4.0 : see CAreadme.txt <br>", text); //$NON-NLS-1$
            } else {
                text = QuickFixer.insertAfterLine(l, license + "<br>", text); //$NON-NLS-1$
            }
            break;
        case 96: // The BFC information is missing
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
        case 192: // The comment has just one slash.
            {
                StringBuilder sb = new StringBuilder();
                String trimmedLine = line.trim();
                String[] dataSegments = trimmedLine.split("\\s+"); //$NON-NLS-1$
                boolean hasFixedSlash = false;
                for (String seg : dataSegments) {
                    if (!seg.trim().equals("")) {  //$NON-NLS-1$
                        if (!hasFixedSlash && "/".equals(seg)) { //$NON-NLS-1$
                            sb.append("//"); //$NON-NLS-1$
                            hasFixedSlash = true;
                        } else {
                            sb.append(seg);
                        }
                    }
                    sb.append(" "); //$NON-NLS-1$
                }
                text = QuickFixer.setLine(lineNumber + 1, sb.toString().trim(), text);
            }
            break;
        case 254: // There are numbers with scientific notation
            {
                StringBuilder sb = new StringBuilder();
                String[] dataSegments = line.trim().split(" "); //$NON-NLS-1$
                int count = 0;
                for (String seg : dataSegments) {
                    if (!seg.trim().equals("")) {  //$NON-NLS-1$
                        count++;
                        try {
                            BigDecimal number = new BigDecimal(seg);
                            if (seg.toUpperCase().contains("E") && count < 15) { //$NON-NLS-1$
                                sb.append(MathHelper.bigDecimalToString(number));
                            } else {
                                sb.append(seg);
                            }
                        } catch (NumberFormatException nfe) {
                            sb.append(seg);
                        }
                    }
                    sb.append(" "); //$NON-NLS-1$
                }
                text = QuickFixer.setLine(lineNumber + 1, sb.toString().trim(), text);
            }
            break;
        default:
            break;
        }

        return text;
    }
}

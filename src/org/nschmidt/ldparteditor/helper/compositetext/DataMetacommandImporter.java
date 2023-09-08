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
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.text.StringHelper;

/**
 * Exports !DATA meta-commands to a file (PNG only)
 */
public enum DataMetacommandImporter {
    INSTANCE;

    /**
     * Imports PNG files into !DATA meta-commands
     */
    public static void importPng(StyledText cText, int lineNumber, DatFile datFile, Shell shell) {
        
        if (datFile.isReadOnly())
            return;
        
        // Now try to load the image
        FileDialog fd = new FileDialog(shell, SWT.OPEN);
        fd.setText(I18n.E3D_OPEN);
        fd.setOverwrite(true);

        File f = new File(datFile.getNewName()).getParentFile();
        if (f != null && f.exists()) {
            fd.setFilterPath(f.getAbsolutePath());
        } else {
            fd.setFilterPath(Project.getLastVisitedPath());
        }

        String[] filterExt = { "*.png", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
        fd.setFilterExtensions(filterExt);
        String[] filterNames = {I18n.E3D_PORTABLE_NETWORK_GRAPHICS, I18n.E3D_ALL_FILES};
        fd.setFilterNames(filterNames);
        
        String selected = fd.open();
        
        if (selected == null) {
            return;
        }
        
        final File selectedFile = new File(selected);
        long fileSize = selectedFile.length();
        
        final boolean noSelection = fileSize == 0 || fileSize > 45_000;
        

        String text = cText.getText();
        String text2 = text;
        int c = cText.getCaretOffset();

        // Set the identifiers for each line
        text2 = "<L1>" + text2; //$NON-NLS-1$
        if (text2.contains("\r\n")) { //$NON-NLS-1$ Windows line termination
            text2 = text2.replace("\r\n", "#!%"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (text2.contains("\n")) { //$NON-NLS-1$ Linux/Mac line termination
            text2 = text2.replace("\n", "#!%"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!text2.endsWith("#!%")) { //$NON-NLS-1$
            text2 = text2 + "#!%"; //$NON-NLS-1$
        }
        {
            int state = 0;
            int l = 1;
            StringBuilder sb = new StringBuilder();
            for (char ch : text2.toCharArray()) {
                if (state == 0 && ch == '#') {
                    state++;
                } else if (state == 1 && ch == '!') {
                    state++;
                } else if (state == 2 && ch == '%') {
                    state = 0;
                    sb.append("</L" + l + "><L" + (l + 1) + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    l++;
                } else {
                    sb.append(ch);
                }
            }
            text2 = sb.toString();
        }
        
        if (!noSelection) {
            NLogger.debug(DataMetacommandImporter.class, "Importing at line: {0}", lineNumber); //$NON-NLS-1$
            text2 = importPng(lineNumber, text2, selectedFile, shell);
            cText.setText(restoreLineTermination(text2));
            int tl = cText.getText().length();
            try {
                cText.setSelection(Math.min(c, tl));
            } catch (Exception e) {
                cText.setSelection(0);
            }
        } else {
            MessageBox messageBoxError = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
            messageBoxError.setText(I18n.DIALOG_WARNING);
            messageBoxError.setMessage(I18n.EDITORTEXT_DATA_ERROR_IMPORT);
            messageBoxError.open();
        }
    }

    private static String importPng(int lineNumber, String text2, File selectedFile, Shell shell) {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("0 !DATA " + selectedFile.getName() + "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            final byte[] binary = Files.readAllBytes(selectedFile.toPath());
            final String encodedString = Base64.getEncoder().encodeToString(binary);
            
            final int encodedStringLength = encodedString.length();
            final int chunks = 200;
            for (int i = 0; i < encodedStringLength; i += chunks) {
                final String lineString = encodedString.substring(i, Math.min(encodedStringLength, i + chunks));
                sb.append("0 !: " + lineString + "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            sb.append("<br>"); //$NON-NLS-1$
        } catch (IOException ioe) {
            NLogger.debug(DataMetacommandImporter.class, ioe);
            MessageBox messageBoxError = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            messageBoxError.setText(I18n.DIALOG_ERROR);
            messageBoxError.setMessage(I18n.EDITORTEXT_DATA_ERROR_IMPORT);
            messageBoxError.open();
        }
        
        text2 = insertBeforeLine(lineNumber, sb.toString(), text2);
        return text2;
    }
    
    private static String restoreLineTermination(String text) {
        text = text.replaceAll("<L[0-9]+><rm></L[0-9]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
        text = text.replaceAll("</L[0-9]+>", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
        text = text.replaceAll("<L[0-9]+>", ""); //$NON-NLS-1$ //$NON-NLS-2$
        final int tl = text.length();
        if (tl > 3) text = text.substring(0, tl - 4);
        return text.replace("<br>", StringHelper.getLineDelimiter()); //$NON-NLS-1$
    }
    
    private static String insertBeforeLine(int line, String textToInsert, String text) {
        return text.replaceFirst("<L" + line + ">", textToInsert + "<L" + line + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}

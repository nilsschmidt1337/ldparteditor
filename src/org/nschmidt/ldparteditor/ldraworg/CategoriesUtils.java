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
package org.nschmidt.ldparteditor.ldraworg;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.UTF8PrintWriter;
import org.nschmidt.ldparteditor.win32appdata.AppData;

public enum CategoriesUtils {
    INSTANCE;

    public static void downloadCategories() {
        final Editor3DWindow win = Editor3DWindow.getWindow();
        final List<String> categories = new ArrayList<>();

        try {
            URL go = new URL("http://www.ldraw.org/library/tracker/ref/catkeyfaq/"); //$NON-NLS-1$
            URLConnection uc = go.openConnection();
            try (BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(
                                    uc.getInputStream()))) {
                String inputLine;
                int[] state = new int[]{0};
                boolean[] parse = new boolean[]{false};
                final StringBuffer sb = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    inputLine.chars().forEach((c) -> {
                        if (parse[0] && c != '<') {
                            sb.append((char) c);
                        }
                        switch (c) {
                        case '<':
                            if (parse[0]) {
                                String category = sb.toString().trim();
                                if (category.length() > 0) {
                                    categories.add(category);
                                }
                            }
                            parse[0] = false;
                            state[0] = 1;
                            sb.setLength(0);
                            break;
                        case 'l':
                            if (state[0] == 1) {
                                state[0] = 2;
                            } else {
                                state[0] = 0;
                            }
                            break;
                        case 'i':
                            if (state[0] == 2) {
                                state[0] = 3;
                            } else {
                                state[0] = 0;
                            }
                            break;
                        case '>':
                            if (state[0] == 3) {
                                parse[0] = true;
                            }
                            state[0] = 0;
                            break;
                        default:
                            if (!parse[0]) {
                                state[0] = 0;
                            }
                            break;
                        }
                    });
                }
            }
        } catch (IOException ioe) {
            MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.OK);
            messageBoxError.setText(I18n.DIALOG_Error);
            messageBoxError.setMessage(I18n.E3D_CantConnectToLDrawOrg);
            messageBoxError.open();
            return;
        }

        Object[] messageArguments = {AppData.getPath() + "categories.txt", categories.size()}; //$NON-NLS-1$
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.LOCALE);
        formatter.applyPattern(I18n.E3D_ReplaceCategories);

        MessageBox messageBoxReplace = new MessageBox(win.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        messageBoxReplace.setText(I18n.DIALOG_Info);
        messageBoxReplace.setMessage(formatter.format(messageArguments));
        if (messageBoxReplace.open() != SWT.YES) {
            return;
        }

        try (UTF8PrintWriter out = new UTF8PrintWriter(AppData.getPath() + "categories.txt")) { //$NON-NLS-1$
            for (String line : categories) {
                out.println(line);
            }
            out.flush();
        } catch (FileNotFoundException | UnsupportedEncodingException ldpe) {
            MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.OK);
            messageBoxError.setText(I18n.DIALOG_Error);
            messageBoxError.setMessage(I18n.E3D_FileWasReplacedError);
            messageBoxError.open();
            return;
        }

        MessageBox messageBoxDone = new MessageBox(win.getShell(), SWT.ICON_INFORMATION | SWT.OK);
        messageBoxDone.setText(I18n.DIALOG_Info);
        messageBoxDone.setMessage(I18n.E3D_FileWasReplaced);
        messageBoxDone.open();
    }
}

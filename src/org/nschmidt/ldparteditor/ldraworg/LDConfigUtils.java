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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.compositetext.SubfileCompiler;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.UTF8PrintWriter;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public enum LDConfigUtils {
    INSTANCE;

    public static void downloadLDConfig() {

        final Editor3DWindow win = Editor3DWindow.getWindow();
        final List<String> lines = new ArrayList<>();

        try {
            URL go = new URL("https://www.ldraw.org/library/official/ldconfig.ldr");//$NON-NLS-1$
            URLConnection uc = go.openConnection();
            try (BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(
                                    uc.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    lines.add(inputLine);
                }
            }
        } catch (IOException ioe) {
            MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.OK);
            messageBoxError.setText(I18n.DIALOG_ERROR);
            messageBoxError.setMessage(I18n.E3D_CANT_CONNECT_TO_LDRAW_ORG);
            messageBoxError.open();
            return;
        }

        final String ldconfig = WorkbenchManager.getUserSettingState().getLdConfigPath();

        Object[] messageArguments = {ldconfig, lines.size()};
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.locale);
        formatter.applyPattern(I18n.E3D_REPLACE_LDCONFIG);

        MessageBox messageBoxReplace = new MessageBox(win.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        messageBoxReplace.setText(I18n.DIALOG_INFO);
        messageBoxReplace.setMessage(formatter.format(messageArguments));
        if (messageBoxReplace.open() != SWT.YES) {
            return;
        }

        try (UTF8PrintWriter out = new UTF8PrintWriter(ldconfig)) {
            for (String line : lines) {
                out.println(line);
            }
            out.flush();
        } catch (FileNotFoundException | UnsupportedEncodingException ldpe) {
            MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.OK);
            messageBoxError.setText(I18n.DIALOG_ERROR);
            messageBoxError.setMessage(I18n.E3D_FILE_WAS_REPLACED_ERROR);
            messageBoxError.open();
            return;
        }

        reloadLDConfig(ldconfig);

        MessageBox messageBoxDone = new MessageBox(win.getShell(), SWT.ICON_INFORMATION | SWT.OK);
        messageBoxDone.setText(I18n.DIALOG_INFO);
        messageBoxDone.setMessage(I18n.E3D_FILE_WAS_REPLACED);
        messageBoxDone.open();
    }

    public static void reloadLDConfig(String path) {
        if (path != null && View.loadLDConfig(path)) {
            View.overrideColour16();
            GData.CACHE_warningsAndErrors.clear();
            WorkbenchManager.getUserSettingState().setLdConfigPath(path);
            Set<DatFile> dfs = new HashSet<>();
            for (OpenGLRenderer renderer : Editor3DWindow.renders) {
                dfs.add(renderer.getC3D().getLockableDatFileReference());
            }
            for (DatFile df : dfs) {
                df.getVertexManager().addSnapshot();
                SubfileCompiler.compile(df, false, false);
            }
        }
    }
}

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GDataBinary;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.helper.math.HashBiMap;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

import de.matthiasmann.twl.util.PNGDecoder;
import de.matthiasmann.twl.util.PNGDecoder.Format;

/**
 * Exports !DATA meta-commands to a file (PNG only)
 */
public enum DataMetacommandExporter {
    INSTANCE;

    /**
     * Exports selected !DATA meta-commands to a file (PNG only)
     *
     * @param lineStart
     *            start line number to export
     * @param lineEnd
     *            end line number to export
     * @param datFile
     */
    public static void export(int lineStart, int lineEnd, DatFile datFile, Shell shell) {
        HashBiMap<Integer, GData> dpl = datFile.getDrawPerLineNoClone();
        lineEnd++;
        
        boolean noSelection = true;
        for (int line = lineStart; line < lineEnd; line++) {
            final GData data = dpl.getValue(line);
            if (data instanceof GDataBinary dataMetaTag) {
                final byte[] binary = dataMetaTag.loadBinary();
                if (binary.length > 0) {
                    try (InputStream in = new ByteArrayInputStream(binary)) {
                        // Link the PNG decoder to this stream
                        final PNGDecoder decoder = new PNGDecoder(in);

                        // Get the width and height of the texture
                        int width = decoder.getWidth();
                        int height = decoder.getHeight();

                        // Decode the PNG file in a ByteBuffer
                        ByteBuffer buf = ByteBuffer.allocateDirect(4 * width * height);
                        decoder.decode(buf, width * 4, Format.RGBA);
                        
                        // Now try to save the image
                        FileDialog fd = new FileDialog(shell, SWT.SAVE);
                        fd.setText(I18n.E3D_SAVE_AS);
                        fd.setOverwrite(true);

                        File f = new File(datFile.getNewName()).getParentFile();
                        if (f != null && f.exists()) {
                            fd.setFilterPath(f.getAbsolutePath());
                        } else {
                            fd.setFilterPath(Project.getLastVisitedPath());
                        }

                        final String filename = dataMetaTag.toString().substring(8);
                        fd.setFileName(filename);

                        String[] filterExt = { "*.png", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                        fd.setFilterExtensions(filterExt);
                        String[] filterNames = {I18n.E3D_PORTABLE_NETWORK_GRAPHICS, I18n.E3D_ALL_FILES};
                        fd.setFilterNames(filterNames);
                        
                        String selected = fd.open();
                        
                        if (selected != null) {
                            try (OutputStream fos = new FileOutputStream(selected)) {
                                fos.write(binary);
                            }
                        }
                        
                        noSelection = false;
                    } catch (IOException e) {
                        NLogger.debug(DataMetacommandExporter.class, e);
                        MessageBox messageBoxError = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                        messageBoxError.setText(I18n.DIALOG_ERROR);
                        messageBoxError.setMessage(I18n.EDITORTEXT_DATA_ERROR_EXPORT);
                        messageBoxError.open();
                    }
                }
            }
        }
        
        if (noSelection) {
            MessageBox messageBoxError = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
            messageBoxError.setText(I18n.DIALOG_WARNING);
            
            Object[] messageArguments = {WorkbenchManager.getUserSettingState().getDataFileSizeLimit()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.EDITORTEXT_DATA_NO_SELECTION);
            
            messageBoxError.setMessage(formatter.format(messageArguments));
            
            messageBoxError.open();
        }
    }
}

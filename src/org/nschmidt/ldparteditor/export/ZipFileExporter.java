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
package org.nschmidt.ldparteditor.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;

public enum ZipFileExporter {
    INSTANCE;

    private static final String TEXTURES = "textures"; //$NON-NLS-1$
    private static final String P = "p"; //$NON-NLS-1$
    private static final String PARTS = "parts"; //$NON-NLS-1$

    public static void export(String path, DatFile df) {
        // FIXME take all files needed by the current file (recursive) and make a zip-file with the correct folder structure.
        // Only if the files are not referenced from official or unofficial library.
        // TEXMAP PNG images should be considered, too.

        // This can't happen, but its more secure to check if the path really points to a zip-file.
        if (!path.endsWith(".zip")) return; //$NON-NLS-1$

        try (FileSystem zipfs = FileSystems.newFileSystem(Paths.get(path), Map.of("create", "true"))) { //$NON-NLS-1$ //$NON-NLS-2$
            final Path primitivePath = zipfs.getPath(File.separator + P);
            final Path primitiveLowResPath = zipfs.getPath(File.separator + P + File.separator + "8"); //$NON-NLS-1$
            final Path primitiveHiResPath = zipfs.getPath(File.separator + P + File.separator + "48"); //$NON-NLS-1$
            final Path partsPath = zipfs.getPath(File.separator + PARTS);
            final Path subfilesPath = zipfs.getPath(File.separator + PARTS + File.separator + "s"); //$NON-NLS-1$
            final Path texturesPath = zipfs.getPath(File.separator + PARTS + File.separator + TEXTURES);
            Files.createDirectories(primitivePath);
            Files.createDirectories(primitiveLowResPath);
            Files.createDirectories(primitiveHiResPath);
            Files.createDirectories(partsPath);
            Files.createDirectories(subfilesPath);
            Files.createDirectories(texturesPath);

            Path pathInZipfile = zipfs.getPath(File.separator + PARTS + File.separator + "someDatFile.dat"); //$NON-NLS-1$
            Files.writeString(pathInZipfile, "0 Title\n0 Name: Test\n", StandardOpenOption.CREATE, StandardOpenOption.WRITE); //$NON-NLS-1$

            Path pngPathInZipfile = zipfs.getPath(File.separator + PARTS + File.separator + TEXTURES + File.separator + "somePngFile.png"); //$NON-NLS-1$
            Files.write(pngPathInZipfile, new byte[0], StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException ioe) {
            NLogger.error(ZipFileExporter.class, ioe);
            MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            messageBox.setText(I18n.DIALOG_ERROR);
            messageBox.setMessage(I18n.E3D_ZIP_ERROR);
            messageBox.open();
            return;
        }

        MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_INFORMATION);
        messageBox.setText(I18n.DIALOG_INFO);
        messageBox.setMessage(I18n.E3D_ZIP_CREATED);
        messageBox.open();
    }
}

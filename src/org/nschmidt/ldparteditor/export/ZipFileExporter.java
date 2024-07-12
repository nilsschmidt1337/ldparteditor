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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.DatType;
import org.nschmidt.ldparteditor.data.GTexture;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.FileHelper;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;

public enum ZipFileExporter {
    INSTANCE;

    private static final String TEXTURES = "textures"; //$NON-NLS-1$
    private static final String TEXTURES_UPPERCASE = "TEXTURES"; //$NON-NLS-1$
    private static final String P = "p"; //$NON-NLS-1$
    private static final String PARTS = "parts"; //$NON-NLS-1$
    private static final String PARTS_UPPERCASE = "PARTS"; //$NON-NLS-1$

    public static void export(String pathString, DatFile df) {
        final Path path = Paths.get(pathString);
        // FIXME take all files needed by the current file (recursive) and make a zip-file with the correct folder structure.
        // Only if the files are not referenced from official or unofficial library.
        // TEXMAP PNG / LPE PNG images should be considered, too.

        // This can't happen, but its more secure to check if the path really points to a zip-file.
        if (!path.toString().endsWith(".zip") || Files.isDirectory(path)) return; //$NON-NLS-1$

        Set<PngImage> texmapImages;
        Set<PngImage> lpePngImages;
        Set<LDrawFile> files;

        try {
            texmapImages = findTexmapImages(df);
            lpePngImages = findLpePngImages(df);
            files = findFiles(df);

            // Delete the original file
            Files.deleteIfExists(path);
        } catch (IOException ioe) {
            logExceptionAndShowDialog(ioe);
            return;
        }

        try (FileSystem zipfs = FileSystems.newFileSystem(path, Map.of("create", "true"))) { //$NON-NLS-1$ //$NON-NLS-2$
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

            for (LDrawFile file : files) {
                final String prefix;
                switch (file.type) {
                case PART:
                    prefix = PARTS + File.separator;
                    break;
                case SUBPART:
                    prefix = PARTS + File.separator + "s" + File.separator; //$NON-NLS-1$
                    break;
                case PRIMITIVE:
                    prefix = P + File.separator;
                    break;
                case PRIMITIVE48:
                    prefix = P + File.separator + "48" + File.separator; //$NON-NLS-1$
                    break;
                case PRIMITIVE8:
                    prefix = P + File.separator + "8" + File.separator; //$NON-NLS-1$
                    break;
                default:
                    prefix = ""; //$NON-NLS-1$
                    break;
                }

                Path pathInZipfile = zipfs.getPath(File.separator + prefix + new File(file.fileName).getName());
                Files.writeString(pathInZipfile, file.content, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }

            for (PngImage texmapImage : texmapImages) {
                Path pngPathInZipfile = zipfs.getPath(File.separator + PARTS + File.separator + TEXTURES + File.separator + texmapImage.fileName);
                Files.write(pngPathInZipfile, texmapImage.data, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }

            for (PngImage pngImage : lpePngImages) {
                Path pngPathInZipfile = zipfs.getPath(File.separator + pngImage.fileName);
                Files.write(pngPathInZipfile, pngImage.data, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }
        } catch (IOException ioe) {
            logExceptionAndShowDialog(ioe);
            return;
        }

        MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_INFORMATION);
        messageBox.setText(I18n.DIALOG_INFO);
        messageBox.setMessage(I18n.E3D_ZIP_CREATED);
        messageBox.open();
    }

    private static void logExceptionAndShowDialog(IOException ioe) {
        NLogger.error(ZipFileExporter.class, ioe);
        MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
        messageBox.setText(I18n.DIALOG_ERROR);
        messageBox.setMessage(I18n.E3D_ZIP_ERROR);
        messageBox.open();
    }

    private static Set<PngImage> findTexmapImages(DatFile df) throws IOException {
        final Set<PngImage> result = new HashSet<>();
        final List<String> images = GTexture.getUsedTexMapImages(df.getDrawChainStart());

        for (String filename : images) {
            // Check folders
            File fileToOpen;
            String pPartsLTex = Project.getProjectPath() + File.separator + PARTS + File.separator + filename;
            String pPartsLTexU = Project.getProjectPath() + File.separator + PARTS  + File.separator + TEXTURES_UPPERCASE + File.separator + filename;
            String pPartsLTexL = Project.getProjectPath() + File.separator + PARTS  + File.separator + TEXTURES + File.separator + filename;
            String pPartsUTex = Project.getProjectPath() + File.separator + PARTS_UPPERCASE + File.separator + filename;
            String pPartsUTexU = Project.getProjectPath() + File.separator + PARTS_UPPERCASE  + File.separator + TEXTURES_UPPERCASE + File.separator + filename;
            String pPartsUTexL = Project.getProjectPath() + File.separator + PARTS_UPPERCASE  + File.separator + TEXTURES + File.separator + filename;
            String pTex = Project.getProjectPath() + File.separator + filename;
            String pTexU = Project.getProjectPath() + File.separator + TEXTURES_UPPERCASE + File.separator + filename;
            String pTexL = Project.getProjectPath() + File.separator + TEXTURES + File.separator + filename;
            String fTex = Project.getProjectPath() + File.separator + filename;
            String fTexU = Project.getProjectPath() + File.separator + TEXTURES_UPPERCASE + File.separator + filename;
            String fTexL = Project.getProjectPath() + File.separator + TEXTURES + File.separator + filename;
            String fPartsLTex = Project.getProjectPath() + File.separator + PARTS + File.separator + filename;
            String fPartsLTexU = Project.getProjectPath() + File.separator + PARTS  + File.separator + TEXTURES_UPPERCASE + File.separator + filename;
            String fPartsLTexL = Project.getProjectPath() + File.separator + PARTS  + File.separator + TEXTURES + File.separator + filename;
            String fPartsUTex = Project.getProjectPath() + File.separator + PARTS_UPPERCASE + File.separator + filename;
            String fPartsUTexU = Project.getProjectPath() + File.separator + PARTS_UPPERCASE  + File.separator + TEXTURES_UPPERCASE + File.separator + filename;
            String fPartsUTexL = Project.getProjectPath() + File.separator + PARTS_UPPERCASE  + File.separator + TEXTURES + File.separator + filename;

            if (df != null && !df.isProjectFile() && !View.DUMMY_DATFILE.equals(df)) {
                File dff = new File(df.getOldName()).getParentFile();
                if (dff != null && dff.exists() && dff.isDirectory()) {
                    String ap = dff.getAbsolutePath();
                    fTex = ap + File.separator + filename;
                    fTexU = ap + File.separator + TEXTURES_UPPERCASE + File.separator + filename;
                    fTexL = ap + File.separator + TEXTURES + File.separator + filename;
                    fPartsLTex = ap + File.separator + PARTS + File.separator + filename;
                    fPartsLTexU = ap + File.separator + PARTS  + File.separator + TEXTURES_UPPERCASE + File.separator + filename;
                    fPartsLTexL = ap + File.separator + PARTS  + File.separator + TEXTURES + File.separator + filename;
                    fPartsUTex = ap + File.separator + PARTS_UPPERCASE + File.separator + filename;
                    fPartsUTexU = ap + File.separator + PARTS_UPPERCASE  + File.separator + TEXTURES_UPPERCASE + File.separator + filename;
                    fPartsUTexL = ap + File.separator + PARTS_UPPERCASE  + File.separator + TEXTURES + File.separator + filename;
                }
            }

            File projectTexture = new File(pTex);
            File projectTextureU = new File(pTexU);
            File projectTextureL = new File(pTexL);
            File projectTexturePartsL = new File(pPartsLTex);
            File projectTextureUPartsL = new File(pPartsLTexU);
            File projectTextureLPartsL = new File(pPartsLTexL);
            File projectTexturePartsU = new File(pPartsUTex);
            File projectTextureUPartsU = new File(pPartsUTexU);
            File projectTextureLPartsU = new File(pPartsUTexL);
            File localTexture = new File(fTex);
            File localTextureU = new File(fTexU);
            File localTextureL = new File(fTexL);
            File localTexturePartsL = new File(fPartsLTex);
            File localTextureUPartsL = new File(fPartsLTexU);
            File localTextureLPartsL = new File(fPartsLTexL);
            File localTexturePartsU = new File(fPartsUTex);
            File localTextureUPartsU = new File(fPartsUTexU);
            File localTextureLPartsU = new File(fPartsUTexL);
            File textureFile = new File(filename);

            boolean fileExists = (
                    (fileToOpen = FileHelper.exist(localTexture)) != null
                    || (fileToOpen = FileHelper.exist(localTextureU)) != null
                    || (fileToOpen = FileHelper.exist(localTextureL)) != null
                    || (fileToOpen = FileHelper.exist(localTexturePartsL)) != null
                    || (fileToOpen = FileHelper.exist(localTextureUPartsL)) != null
                    || (fileToOpen = FileHelper.exist(localTextureLPartsL)) != null
                    || (fileToOpen = FileHelper.exist(localTexturePartsU)) != null
                    || (fileToOpen = FileHelper.exist(localTextureUPartsU)) != null
                    || (fileToOpen = FileHelper.exist(localTextureLPartsU)) != null
                    || (fileToOpen = FileHelper.exist(projectTexture)) != null
                    || (fileToOpen = FileHelper.exist(projectTextureU)) != null
                    || (fileToOpen = FileHelper.exist(projectTextureL)) != null
                    || (fileToOpen = FileHelper.exist(projectTexturePartsL)) != null
                    || (fileToOpen = FileHelper.exist(projectTextureUPartsL)) != null
                    || (fileToOpen = FileHelper.exist(projectTextureLPartsL)) != null
                    || (fileToOpen = FileHelper.exist(projectTexturePartsU)) != null
                    || (fileToOpen = FileHelper.exist(projectTextureUPartsU)) != null
                    || (fileToOpen = FileHelper.exist(projectTextureLPartsU)) != null
                    || (fileToOpen = FileHelper.exist(textureFile)) != null)
                    && fileToOpen.isFile();

            if (fileExists) {
                final byte[] bytes = Files.readAllBytes(fileToOpen.toPath());
                result.add(new PngImage(filename, bytes));
            }
        }

        return result;
    }

    private static Set<PngImage> findLpePngImages(DatFile df) throws IOException {
        final Set<PngImage> result = new HashSet<>();
        final List<String> images = GTexture.getUsedLpePngImages(df.getDrawChainStart());

        for (String filename : images) {
            // Check file existence
            final File fileToOpen;
            final boolean fileExists = (fileToOpen = FileHelper.exist(new File(filename))) != null
                    && fileToOpen.isFile();
            if (fileExists) {
                final byte[] bytes = Files.readAllBytes(fileToOpen.toPath());
                result.add(new PngImage(fileToOpen.getName(), bytes));
            }
        }

        return result;
    }

    private static Set<LDrawFile> findFiles(DatFile df) throws IOException {
        final Set<LDrawFile> result = new HashSet<>();
        // TODO Needs implementation!
        return result;
    }

    private static class PngImage {
        private final String fileName;
        private final byte[] data;
        public PngImage(String fileName, byte[] data) {
            this.fileName = fileName;
            this.data = data;
        }
        @Override
        public int hashCode() {
            return Objects.hash(fileName);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof PngImage))
                return false;
            PngImage other = (PngImage) obj;
            return Objects.equals(fileName, other.fileName);
        }
    }

    private static class LDrawFile {
        private final String fileName = ""; //$NON-NLS-1$
        private final DatType type = DatType.PART;
        private final String content = null;
        @Override
        public int hashCode() {
            return Objects.hash(fileName, type);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof LDrawFile))
                return false;
            LDrawFile other = (LDrawFile) obj;
            return Objects.equals(fileName, other.fileName) && type == other.type;
        }
    }
}

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.BFC;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.DatType;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GTexture;
import org.nschmidt.ldparteditor.data.ReferenceParser;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.FileHelper;
import org.nschmidt.ldparteditor.helper.compositetext.Inliner;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.References;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public enum ZipFileExporter {
    INSTANCE;

    private static final String TEXTURES = "textures"; //$NON-NLS-1$
    private static final String TEXTURES_UPPERCASE = "TEXTURES"; //$NON-NLS-1$
    private static final String P = "p"; //$NON-NLS-1$
    private static final String PARTS = "parts"; //$NON-NLS-1$
    private static final String PARTS_UPPERCASE = "PARTS"; //$NON-NLS-1$

    public static void export(String pathString, DatFile df, Shell shell) {
        final Path path = Paths.get(pathString);
        // Take all files needed by the current file (recursive) and make a zip-file with the correct folder structure.
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
            logExceptionAndShowDialog(ioe, shell);
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
            logExceptionAndShowDialog(ioe, shell);
            return;
        }

        MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
        messageBox.setText(I18n.DIALOG_INFO);
        messageBox.setMessage(I18n.E3D_ZIP_CREATED);
        messageBox.open();
    }

    private static void logExceptionAndShowDialog(IOException ioe, Shell shell) {
        NLogger.error(ZipFileExporter.class, ioe);
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
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

            File projectTexture = new File(Project.getProjectPath() + File.separator + filename);
            File projectTextureU = new File(Project.getProjectPath() + File.separator + TEXTURES_UPPERCASE + File.separator + filename);
            File projectTextureL = new File(Project.getProjectPath() + File.separator + TEXTURES + File.separator + filename);
            File projectTexturePartsL = new File(Project.getProjectPath() + File.separator + PARTS + File.separator + filename);
            File projectTextureUPartsL = new File(Project.getProjectPath() + File.separator + PARTS  + File.separator + TEXTURES_UPPERCASE + File.separator + filename);
            File projectTextureLPartsL = new File(Project.getProjectPath() + File.separator + PARTS  + File.separator + TEXTURES + File.separator + filename);
            File projectTexturePartsU = new File(Project.getProjectPath() + File.separator + PARTS_UPPERCASE + File.separator + filename);
            File projectTextureUPartsU = new File(Project.getProjectPath() + File.separator + PARTS_UPPERCASE  + File.separator + TEXTURES_UPPERCASE + File.separator + filename);
            File projectTextureLPartsU = new File(Project.getProjectPath() + File.separator + PARTS_UPPERCASE  + File.separator + TEXTURES + File.separator + filename);
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

    private static Set<LDrawFile> findFiles(DatFile df) {
        final Set<LDrawFile> result = new HashSet<>();
        final GColour col16 = LDConfig.getColour16();
        final VertexManager vm = df.getVertexManager();
        Inliner.withSubfileReference = true;
        Inliner.recursively = false;
        Inliner.noComment = false;

        // It is important to use the ReferenceParser here, because it will display a warning if one
        // of the referenced files is missing!
        final List<List<DatFile>> refs = ReferenceParser.checkForReferences(df, References.REQUIRED_UNSAVED, null, true);
        refs.get(0).add(df);

        NLogger.debug(ZipFileExporter.class, "List references:"); //$NON-NLS-1$
        for (List<DatFile> list : refs) {
            for (DatFile d : list) {
                final GData1 untransformedFile = (GData1) DatParser
                        .parseLine("1 16 0 0 0 1 0 0 0 1 0 0 0 1 " + d.getShortName(), 0, 0, col16.getR(), col16.getG(), col16.getB(), 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, df, false, //$NON-NLS-1$
                                new HashSet<>()).get(0).getGraphicalData();
                final String[] lines = untransformedFile.inlinedString(BFC.NOCERTIFY, col16).split("<br>"); //$NON-NLS-1$
                final String fileSource = Arrays.stream(lines)
                                            .skip(1)
                                            .limit(Math.max(lines.length - 2L, 0L))
                                            .collect(Collectors.joining("\r\n")); //$NON-NLS-1$
                final String filePath = untransformedFile.getName();
                NLogger.debug(ZipFileExporter.class, d.getShortName() + ' ' + d.getType() + '\n' + filePath);
                if (!filePath.startsWith(WorkbenchManager.getUserSettingState().getUnofficialFolderPath())
                   && !filePath.startsWith(WorkbenchManager.getUserSettingState().getLdrawFolderPath())) {
                    NLogger.debug(ZipFileExporter.class, fileSource);
                    result.add(new LDrawFile(new File(d.getNewName()).getName(), d.getType(), fileSource));
                }

                vm.remove(untransformedFile);
            }
        }

        vm.validateState();

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
        private final String fileName;
        private final DatType type;
        private final String content;
        public LDrawFile(String fileName, DatType type, String content) {
            this.fileName = fileName;
            this.type = type;
            this.content = content;
        }
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

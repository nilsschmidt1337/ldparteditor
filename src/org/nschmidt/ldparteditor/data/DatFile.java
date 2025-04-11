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
package org.nschmidt.ldparteditor.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTabState;
import org.nschmidt.ldparteditor.data.colour.GCChrome;
import org.nschmidt.ldparteditor.data.colour.GCMatteMetal;
import org.nschmidt.ldparteditor.data.colour.GCMetal;
import org.nschmidt.ldparteditor.enumtype.Axis;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helper.math.HashBiMap;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeSortedMap;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.AddToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.InsertAtCursorPositionToolItem;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.text.UTF8PrintWriter;
import org.nschmidt.ldparteditor.widget.TreeItem;

/**
 * The DAT file class
 */
public final class DatFile {

    private static final Pattern pattern = Pattern.compile("\r?\n|\r"); //$NON-NLS-1$

    private final boolean readOnly;
    private final boolean fromPartReview;
    private volatile boolean drawSelection = true;

    private final GData drawChainAnchor = new GDataInit(View.DUMMY_REFERENCE);

    private final HashBiMap<Integer, GData> drawPerLine = new HashBiMap<>();
    private final Map<Integer, GData> copyDrawPerLine = new HashMap<>();

    private static final GTexture CUBEMAP_TEXTURE = new GTexture(TexType.PLANAR, "cmap.png", null, 1, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GDataTEX CUBEMAP = new GDataTEX(null, "", TexMeta.NEXT, CUBEMAP_TEXTURE, View.DUMMY_REFERENCE); //$NON-NLS-1$

    private static final GTexture CUBEMAP_MATTE_TEXTURE = new GTexture(TexType.PLANAR, "matte_metal.png", null, 2, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GDataTEX CUBEMAP_MATTE = new GDataTEX(null, "", TexMeta.NEXT, CUBEMAP_MATTE_TEXTURE, View.DUMMY_REFERENCE); //$NON-NLS-1$

    private static final GTexture CUBEMAP_METAL_TEXTURE = new GTexture(TexType.PLANAR, "metal.png", null, 3, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GDataTEX CUBEMAP_METAL = new GDataTEX(null, "", TexMeta.NEXT, CUBEMAP_METAL_TEXTURE, View.DUMMY_REFERENCE); //$NON-NLS-1$

    private final VertexManager vertices = new VertexManager(this);

    private Vertex nearestObjVertex1 = null;
    private Vertex nearestObjVertex2 = null;
    private Vertex objVertex1 = null;
    private Vertex objVertex2 = null;
    private Vertex objVertex3 = null;
    private Vertex objVertex4 = null;

    private boolean virtual;
    private boolean projectFile;

    private boolean optimizingCSG = true;

    private volatile DatType type = DatType.PART;
    private long lastModified = 0;

    private String description;
    private String oldName;
    private String newName;
    private String text = ""; //$NON-NLS-1$
    private String originalText = ""; //$NON-NLS-1$

    private Date lastSavedOpened = new Date();

    private GData drawChainTail = null;

    private Composite3D lastSelectedComposite = null;
    private static Composite3D lastHoveredComposite = null;

    private HistoryManager history = new HistoryManager(this);
    private DuplicateManager duplicate = new DuplicateManager(this);
    private DatHeaderManager datHeader = new DatHeaderManager(this);
    private final BinaryDataRegistry binaryData = new BinaryDataRegistry();

    public static DatFile createDatFileForReview(String path) {
        return new DatFile(path, true);
    }

    public static DatFile createDatFileForReviewInProject(String path, String projectPath) {
        final String basePath = new File(Project.DEFAULT_PROJECT_PATH).getAbsolutePath() + File.separator;
        String filePathInProject = path.substring(basePath.length());
        filePathInProject = projectPath + filePathInProject;
        DatFile df = new DatFile(filePathInProject, true);
        df.setVirtual(false);
        return df;
    }

    private DatFile(String path, boolean fromPartReview) {
        this.projectFile = true;
        this.oldName = path;
        this.newName = path;
        this.readOnly = false;
        this.fromPartReview = fromPartReview;
        this.setVirtual(true);
        this.setType(DatType.PART);
    }

    public DatFile(String path) {
        this.projectFile = true;
        this.oldName = path;
        this.newName = path;
        this.readOnly = false;
        this.fromPartReview = false;
        this.setVirtual(true);
        this.setType(DatType.PART);
    }

    public DatFile(String path, String description, boolean isReadOnly, DatType type) {
        this.projectFile = false;
        this.description = description;
        this.oldName = path;
        this.newName = path;
        this.readOnly = isReadOnly;
        this.fromPartReview = false;
        this.setVirtual(false);
        this.setType(type);
    }

    /**
     * Draw the DAT file on the Composite3D This method is not intended for
     * preview renderings, since its too mighty for it
     *
     * @param c3d
     */
    public synchronized void draw(Composite3D c3d) {


        GDataCSG.resetCSG(this, c3d.getManipulator().isModified());

        GData data2draw = drawChainAnchor;
        int renderMode = c3d.getRenderMode();
        if (!c3d.isDrawingSolidMaterials() && renderMode != 5)
            vertices.drawGL20(c3d);

        if (AddToolItem.isAddingCondlines())
            renderMode = 6;
        switch (renderMode) {
        case -1: // Wireframe
            data2draw.drawGL20Wireframe(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawGL20Wireframe(c3d);
            }
            break;
        case 0: // No BFC
            data2draw.drawGL20(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawGL20(c3d);
            }
            break;
        case 1: // Random Colours
            data2draw.drawGL20RandomColours(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawGL20RandomColours(c3d);
            }
            break;
        case 2: // Front-Backface BFC
            data2draw.drawGL20BFC(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                if (GData.accumClip == 0) {
                    data2draw.drawGL20BFC(c3d);
                } else {
                    data2draw.drawGL20(c3d);
                }
            }
            break;
        case 3: // Backface only BFC
            data2draw.drawGL20BFCbackOnly(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                if (GData.accumClip == 0) {
                    data2draw.drawGL20BFCbackOnly(c3d);
                } else {
                    data2draw.drawGL20(c3d);
                }
            }
            break;
        case 4: // Real BFC
            data2draw.drawGL20BFCcolour(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                if (GData.accumClip == 0) {
                    data2draw.drawGL20BFCcolour(c3d);
                } else {
                    data2draw.drawGL20(c3d);
                }
            }
            break;
        case 5: // Real BFC with texture mapping
            c3d.clearRaytraceSkipSet();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            data2draw.drawGL20BFCtextured(c3d);
            GDataInit.resetBfcState();
            data2draw.drawGL20BFCtextured(c3d);
            CUBEMAP.drawGL20BFCtextured(c3d);
            new GData3(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(1,1,0), View.DUMMY_REFERENCE, new GColour(0, 0, 0, 0, 0, new GCChrome()), true).drawGL20BFCtextured(c3d.getComposite3D());
            CUBEMAP_MATTE.drawGL20BFCtextured(c3d);
            new GData3(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(1,1,0), View.DUMMY_REFERENCE, new GColour(0, 0, 0, 0, 0, new GCMatteMetal()), true).drawGL20BFCtextured(c3d.getComposite3D());
            CUBEMAP_METAL.drawGL20BFCtextured(c3d);
            new GData3(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(1,1,0), View.DUMMY_REFERENCE, new GColour(0, 0, 0, 0, 0, new GCMetal()), true).drawGL20BFCtextured(c3d.getComposite3D());

            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawGL20BFCtextured(c3d);
            }

            c3d.provideRaytraceSkipSet();

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 2);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 4);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 8);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 16);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            break;
        case 6: // Special mode for "Add condlines"
            data2draw.drawGL20WhileAddCondlines(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawGL20WhileAddCondlines(c3d);
            }
            break;
        case 7: // Special mode for coplanar quads
            data2draw.drawGL20CoplanarityHeatmap(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawGL20CoplanarityHeatmap(c3d);
            }
            break;
        case 8: // Special mode for convexity
            data2draw.drawGL20ConvexityHeatmap(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawGL20ConvexityHeatmap(c3d);
            }
            break;
        default:
            break;
        }

        GDataCSG.finishCacheCleanup(c3d.getLockableDatFileReference());

        if (c3d.isDrawingSolidMaterials() && renderMode != 5)
            vertices.showHidden();
    }

    synchronized void getBFCorientationMap(Map<GData, BFC> bfcMap) {
        GDataCSG.resetCSG(this, false);
        GData data2draw = drawChainAnchor;
        data2draw.getBFCorientationMap(bfcMap);
        while ((data2draw = data2draw.getNext()) != null) {
            data2draw.getBFCorientationMap(bfcMap);
        }
    }

    /**
     * @return the real filename from the file stored on disk
     */
    public String getOldName() {
        return oldName;
    }

    /**
     * Sets the real filename of the file stored on disk
     *
     * @param oldName
     *            the real filename
     */
    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    /**
     * @return the new filename from the file to be stored. It's typically the
     *         same as the old name.
     */
    public String getNewName() {
        return newName;
    }

    /**
     * Sets the new filename for the file to be stored
     *
     * @param newName
     *            the new filename
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        description = d;
    }

    /**
     * @return {@code true} if the file is read-only
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * @return the text content of this dat file
     */
    public String getText() {
        final boolean modified = vertices.isModified();
        if (modified || Project.getUnsavedFiles().contains(this)) {
            if (modified) {
                StringBuilder sb = new StringBuilder();
                GData data2draw = drawChainAnchor;
                while ((data2draw = data2draw.getNext()) != null && data2draw.getNext() != null) {
                    sb.append(data2draw.toString());
                    sb.append(StringHelper.getLineDelimiter());
                }
                if (data2draw == null) {
                    vertices.setModified(false, true);
                } else {
                    sb.append(data2draw.toString());
                    text = sb.toString();
                }
            }
            final GData descriptionline = drawChainAnchor.getNext();
            if (descriptionline != null) {
                String descr = descriptionline.toString();
                if (descr.length() > 1)
                    descr = descr.substring(2);
                description = " - " + descr; //$NON-NLS-1$
            }
        } else {
            parseForData(false);
        }
        return text;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String ot) {
        setLastSavedOpened(new Date());
        originalText = ot;
    }

    /**
     * @param text
     *            the text content of this dat file to set
     */
    public void setText(String text) {
        final GData descriptionline = drawChainAnchor.getNext();
        if (descriptionline != null) {
            String descr = descriptionline.toString();
            if (descr.length() > 1)
                descr = descr.substring(2);
            description = " - " + descr; //$NON-NLS-1$
        }
        this.text = text;
    }

    /**
     * This function reads the contents from the hard-drive if the file was not loaded before.
     * <br> The list will be empty if the file can't be read or can't be found
     * @param unsaved indicates if it should also return unsaved content
     * @return a list of codelines from this DAT file.
     */
    public List<String> getSource(boolean unsaved) {
        List<String> result = new ArrayList<>();
        if (originalText.isEmpty() && new File(this.getOldName()).exists() && (!unsaved || !Project.getUnsavedFiles().contains(this))) {
            try (UTF8BufferedReader reader = new UTF8BufferedReader(this.getOldName())) {
                while (true) {
                    String line2 = reader.readLine();
                    if (line2 == null) {
                        break;
                    }
                    result.add(line2);
                }
            } catch (FileNotFoundException | LDParsingException e) {
                NLogger.error(DatFile.class, e);
            }
        } else {
            GData data2draw = drawChainAnchor;
            while ((data2draw = data2draw.getNext()) != null) {
                result.add(data2draw.toString());
            }
        }
        return result;
    }

    /**
     * Parses the opened dat file for errors and correct data (in realtime, only
     * when opened in text editor)
     */
    public void parseForErrorAndData(StyledText compositeText, int startOffsetPos, int endOffsetPos, int length, String replacedText, TreeItem hints, TreeItem warnings, TreeItem errors,
            TreeItem duplicates, Label problemCount) {

        Set<String> alreadyParsed = new HashSet<>();
        alreadyParsed.add(getShortName());

        GData anchorData = drawChainAnchor;
        GData targetData = null;

        long start = System.currentTimeMillis();

        int startLine = compositeText.getLineAtOffset(startOffsetPos);
        int startOffset = compositeText.getOffsetAtLine(startLine);

        int endLine = compositeText.getLineAtOffset(endOffsetPos);
        int endOffset = compositeText.getOffsetAtLine(endLine) + compositeText.getLine(endLine).length();

        startLine++;
        endLine++;

        boolean tailRemoved = false;

        // Dispose overwritten content (and so the connected 3D info)
        final int rlength = replacedText.length();
        if (rlength > 0) {

            // Difficult, because the old text was overwritten >= 1 old line
            // change

            final int newLineCount = endLine - startLine + 1;
            final int affectedOldLineCount = StringHelper.countOccurences(StringHelper.getLineDelimiter(), replacedText) + 1;
            final int oldEndLine = startLine + affectedOldLineCount - 1;

            // Set the anchor
            GData linkedDraw = drawPerLine.getValue(startLine);
            if (linkedDraw != null) {
                GData newAnchor = linkedDraw.getBefore();
                if (newAnchor != null)
                    anchorData = newAnchor;
            }

            // Set the target
            GData linkedDraw2 = drawPerLine.getValue(oldEndLine);
            if (linkedDraw2 != null) {
                targetData = linkedDraw2.getNext();
            }

            // Remove overwritten content
            int actionStartLine = startLine;
            for (int i = 0; i < affectedOldLineCount; i++) {
                boolean vertexRemoved = vertices.remove(drawPerLine.getValue(actionStartLine));
                tailRemoved |= vertexRemoved;
                drawPerLine.removeByKey(actionStartLine);
                actionStartLine++;
            }

            if (affectedOldLineCount != newLineCount && !drawPerLine.isEmpty()) {
                // Update references at the tail
                int diff = newLineCount - affectedOldLineCount;
                actionStartLine = oldEndLine + 1;
                GData data;

                while ((data = drawPerLine.getValue(actionStartLine)) != null) {
                    copyDrawPerLine.put(actionStartLine + diff, data);
                    drawPerLine.removeByKey(actionStartLine);
                    actionStartLine++;
                }
                for (Entry<Integer, GData> entry : copyDrawPerLine.entrySet()) {
                    drawPerLine.put(entry.getKey(), entry.getValue());
                }
                copyDrawPerLine.clear();
            }

        } else if (length > 0) {
            // Easy, because only new text was inserted = 1 old line change
            int newLineCount = endLine - startLine;

            // Insertion within one line

            if (startLine == endLine) {
                // The target data is the next data from the old line
                GData linkedDraw = drawPerLine.getValue(startLine);
                if (linkedDraw != null) {
                    targetData = linkedDraw.getNext();
                    // And the anchor data is the data before the old line
                    GData newAnchor = linkedDraw.getBefore();
                    if (newAnchor != null)
                        anchorData = newAnchor;

                    // And the old line data has to be removed
                    tailRemoved = vertices.remove(drawPerLine.getValue(startLine)) || tailRemoved;
                    drawPerLine.removeByKey(startLine);
                }
            } else {
                // The target data is the next data from the old line
                GData linkedDraw = drawPerLine.getValue(startLine);
                if (linkedDraw != null) {
                    targetData = linkedDraw.getNext();
                    // And the anchor data is the data before the old line
                    GData newAnchor = linkedDraw.getBefore();
                    if (newAnchor != null) {
                        anchorData = newAnchor;
                    }

                    // And the old line data has to be moved
                    tailRemoved = vertices.remove(drawPerLine.getValue(startLine)) || tailRemoved;
                    drawPerLine.removeByKey(startLine);

                    int lcount = compositeText.getLineCount() - newLineCount + 1;
                    for (int i = startLine + 1; i < lcount; i++) {
                        copyDrawPerLine.put(i + newLineCount, drawPerLine.getValue(i));
                        drawPerLine.removeByKey(i);
                    }
                    for (Entry<Integer, GData> entry : copyDrawPerLine.entrySet()) {
                        drawPerLine.put(entry.getKey(), entry.getValue());
                    }
                    copyDrawPerLine.clear();
                }
            }

        }
        NLogger.debug(getClass(), "Time after OpenGL data change: {0} ms", System.currentTimeMillis() - start); //$NON-NLS-1$

        warnings.removeWithinPosition(compositeText, startOffset, endOffset, length - rlength);
        errors.removeWithinPosition(compositeText, startOffset, endOffset, length - rlength);

        int offset = compositeText.getLineDelimiter().length();
        int position = startOffset;

        List<ParsingResult> results;
        final GColour col16 = LDConfig.getColour16();

        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();
        GData.CACHE_warningsAndErrors.clear();

        String line;
        GData gdata;
        for (int lineNumber = startLine; lineNumber < endLine + 1; lineNumber++) {

            line = compositeText.getLine(lineNumber - 1);

            if (isNotBlank(line)) {

                results = DatParser.parseLine(line, lineNumber, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, this, false, alreadyParsed);

                gdata = results.get(0).getGraphicalData();
                if (gdata == null) {
                    gdata = new GData0(line, View.DUMMY_REFERENCE);
                } else {
                    gdata.setText(line);
                    GData.CACHE_warningsAndErrors.put(gdata, results);
                }

                anchorData.setNext(gdata);
                anchorData = gdata;
                drawPerLine.put(lineNumber, gdata);

                for (ParsingResult result : results) {
                    switch (result.getResultType()) {
                    case WARN: // Warning
                    {

                        Object[] messageArguments = {lineNumber, position};
                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                        formatter.setLocale(MyLanguage.getLocale());
                        formatter.applyPattern(I18n.DATFILE_LINE);

                        TreeItem trtmNewTreeitem = new TreeItem(warnings);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_warning.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        trtmNewTreeitem.setText(new String[] { result.getMessage(), formatter.format(messageArguments), result.getType() });
                        trtmNewTreeitem.setData(position);
                    }
                    break;
                    case ERROR: // Error
                    {

                        Object[] messageArguments = {lineNumber, position};
                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                        formatter.setLocale(MyLanguage.getLocale());
                        formatter.applyPattern(I18n.DATFILE_LINE);

                        TreeItem trtmNewTreeitem = new TreeItem(errors);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_error.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        trtmNewTreeitem.setText(new String[] { result.getMessage(), formatter.format(messageArguments), result.getType() });
                        trtmNewTreeitem.setData(position);
                    }
                    break;
                    default: // Hint
                        break;
                    }
                }
            } else {
                gdata = new GData0(line, View.DUMMY_REFERENCE);
                anchorData.setNext(gdata);
                anchorData = gdata;
                drawPerLine.put(lineNumber, gdata);
            }
            position += line.length() + offset;
        }

        anchorData.setNext(targetData);

        // Check BFC INVERTNEXT
        for (Iterator<TreeItem> it = errors.getItems().iterator(); it.hasNext();) {
            TreeItem ti = it.next();
            final String tiText = ti.getText(0);
            if (tiText.equals( I18n.DATPARSER_INVALID_INVERT_NEXT) || tiText.equals(I18n.DATPARSER_INVALID_INVERT_NEXT_FLAT)) {
                it.remove();
            }
        }
        GData gd = drawChainAnchor;
        int lineNumber = 1;
        while ((gd = gd.next) != null)
        {
            if (gd.type() == 6 && ((GDataBFC) gd).type == BFC.INVERTNEXT) {
                boolean validState = false;
                GData g = gd.next;
                while (g != null && g.type() < 2) {
                    validState = g.type() == 1;
                    if (validState || !g.toString().trim().isEmpty()) {
                        break;
                    }
                    g = g.next;
                }

                if (g != null && g.type() == 0 && g.toString().trim().startsWith("1 ")) { //$NON-NLS-1$
                    // It's okay, if it is formally an invalid reference line (which falls back to a comment (type = 0))
                } else if (validState) {
                    final Axis flatAxis;
                    if ((flatAxis = getVertexManager().isFlatOnAxis((GData1) g)) != Axis.NONE) {
                        position = compositeText.getOffsetAtLine(lineNumber - 1);
                        Object[] messageArguments = {lineNumber, position};
                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                        formatter.setLocale(MyLanguage.getLocale());
                        formatter.applyPattern(I18n.DATFILE_LINE);

                        TreeItem trtmNewTreeitem = new TreeItem(errors);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_error.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        switch (flatAxis) {
                        case X:
                            trtmNewTreeitem.setText(new String[] { I18n.DATPARSER_INVALID_INVERT_NEXT_FLAT, formatter.format(messageArguments), "[E0A] " + I18n.DATPARSER_SYNTAX_ERROR }); //$NON-NLS-1$
                            break;
                        case Y:
                            trtmNewTreeitem.setText(new String[] { I18n.DATPARSER_INVALID_INVERT_NEXT_FLAT, formatter.format(messageArguments), "[E0B] " + I18n.DATPARSER_SYNTAX_ERROR }); //$NON-NLS-1$
                            break;
                        case Z:
                            trtmNewTreeitem.setText(new String[] { I18n.DATPARSER_INVALID_INVERT_NEXT_FLAT, formatter.format(messageArguments), "[E0C] " + I18n.DATPARSER_SYNTAX_ERROR }); //$NON-NLS-1$
                            break;
                        case NONE:
                        default:
                            trtmNewTreeitem.setText(new String[] { I18n.DATPARSER_INVALID_INVERT_NEXT_FLAT, formatter.format(messageArguments), "[E0D] " + I18n.DATPARSER_SYNTAX_ERROR }); //$NON-NLS-1$
                            break;
                        }
                        trtmNewTreeitem.setData(position);
                    }

                } else {

                    position = compositeText.getOffsetAtLine(lineNumber - 1);
                    Object[] messageArguments = {lineNumber, position};
                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                    formatter.setLocale(MyLanguage.getLocale());
                    formatter.applyPattern(I18n.DATFILE_LINE);

                    TreeItem trtmNewTreeitem = new TreeItem(errors);
                    trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_error.png")); //$NON-NLS-1$
                    trtmNewTreeitem.setVisible(false);
                    trtmNewTreeitem.setText(new String[] { I18n.DATPARSER_INVALID_INVERT_NEXT, formatter.format(messageArguments), "[E0D] " + I18n.DATPARSER_SYNTAX_ERROR }); //$NON-NLS-1$
                    trtmNewTreeitem.setData(position);
                }
            }
            lineNumber++;
        }

        // Get tail
        if (tailRemoved || drawChainTail == null) {
            drawChainTail = anchorData;
        }

        duplicate.pushDuplicateCheck(drawChainAnchor);
        updateDuplicatesErrors(compositeText, duplicates);
        datHeader.pushDatHeaderCheck(drawChainAnchor, compositeText, hints, warnings, errors, duplicates, problemCount);
        updateDatHeaderHints(compositeText, hints);

        warnings.sortItems();
        errors.sortItems();
        hints.getParent().build();
        DatParser.triggerPngImageUpdate();

        NLogger.debug(getClass(), "Total time to parse: {0} ms", System.currentTimeMillis() - start); //$NON-NLS-1$
        vertices.validateState();
        addHistory(compositeText.getText(), startOffsetPos + length, 0 , compositeText.getTopIndex());
        NLogger.debug(getClass(), "Total time to parse + validate + backup history: {0} ms", System.currentTimeMillis() - start); //$NON-NLS-1$
    }

    /**
     * Parses the opened dat file for errors and correct data (in realtime, only
     * when opened in text editor)
     */
    public void parseForError(StyledText compositeText, int startOffsetPos, int endOffsetPos, int length, String replacedText, TreeItem hints, TreeItem warnings, TreeItem errors, TreeItem duplicates, Label problemCount, boolean unselectBgPicture) {

        if (compositeText.getText().isEmpty()) {
            duplicate.pushDuplicateCheck(drawChainAnchor);
            updateDuplicatesErrors(compositeText, duplicates);
            datHeader.pushDatHeaderCheck(drawChainAnchor, compositeText, hints, warnings, errors, duplicates, problemCount);
            updateDatHeaderHints(compositeText, hints);
            return;
        }

        Set<String> alreadyParsed = new HashSet<>();
        alreadyParsed.add(getShortName());

        long start = System.currentTimeMillis();

        int startLine = compositeText.getLineAtOffset(startOffsetPos);
        int startOffset = compositeText.getOffsetAtLine(startLine);

        int endLine = compositeText.getLineAtOffset(endOffsetPos);
        int endOffset = compositeText.getOffsetAtLine(endLine) + compositeText.getLine(endLine).length();

        startLine++;
        endLine++;

        int rlength = replacedText.length();

        warnings.removeWithinPosition(compositeText, startOffset, endOffset, length - rlength);
        errors.removeWithinPosition(compositeText, startOffset, endOffset, length - rlength);

        int offset = StringHelper.getLineDelimiter().length();
        int position = startOffset;

        List<ParsingResult> results;
        final GColour col16 = LDConfig.getColour16();

        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();

        String line;
        for (int lineNumber = startLine; lineNumber < endLine + 1; lineNumber++) {

            line = compositeText.getLine(lineNumber - 1);

            if (isNotBlank(line)) {

                GData gd = drawPerLine.getValue(lineNumber);
                results = GData.CACHE_warningsAndErrors.get(gd);
                if (results == null) {
                    results = DatParser.parseLine(line, lineNumber, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, this, true, alreadyParsed);
                    GData.CACHE_warningsAndErrors.put(gd, results);
                }

                for (ParsingResult result : results) {
                    switch (result.getResultType()) {
                    case WARN: // Warning
                    {

                        Object[] messageArguments = {lineNumber, position};
                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                        formatter.setLocale(MyLanguage.getLocale());
                        formatter.applyPattern(I18n.DATFILE_LINE);

                        TreeItem trtmNewTreeitem = new TreeItem(warnings);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_warning.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        trtmNewTreeitem.setText(new String[] { result.getMessage(), formatter.format(messageArguments), result.getType() });
                        trtmNewTreeitem.setData(position);
                    }
                    break;
                    case ERROR: // Error
                    {

                        Object[] messageArguments = {lineNumber, position};
                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                        formatter.setLocale(MyLanguage.getLocale());
                        formatter.applyPattern(I18n.DATFILE_LINE);

                        TreeItem trtmNewTreeitem = new TreeItem(errors);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_error.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        trtmNewTreeitem.setText(new String[] { result.getMessage(), formatter.format(messageArguments), result.getType() });
                        trtmNewTreeitem.setData(position);
                    }
                    break;
                    default: // Hint
                        break;
                    }
                }
            }
            position += line.length() + offset;
        }

        // Check BFC INVERTNEXT
        for (Iterator<TreeItem> it = errors.getItems().iterator(); it.hasNext();) {
            TreeItem ti = it.next();
            final String tiText = ti.getText(0);
            if (tiText.equals( I18n.DATPARSER_INVALID_INVERT_NEXT) || tiText.equals(I18n.DATPARSER_INVALID_INVERT_NEXT_FLAT)) {
                it.remove();
            }
        }
        GData gd = drawChainAnchor;
        int lineNumber = 1;
        while ((gd = gd.next) != null)
        {
            if (gd.type() == 6 && ((GDataBFC) gd).type == BFC.INVERTNEXT) {
                boolean validState = false;
                GData g = gd.next;
                while (g != null && g.type() < 2) {
                    validState = g.type() == 1;
                    if (validState || !g.toString().trim().isEmpty()) {
                        break;
                    }
                    g = g.next;
                }

                if (g != null && g.type() == 0 && g.toString().trim().startsWith("1 ")) { //$NON-NLS-1$
                    // It's okay, if it is formally an invalid reference line (which falls back to a comment (type = 0))
                } else if (validState) {
                    final Axis flatAxis;
                    if ((flatAxis = getVertexManager().isFlatOnAxis((GData1) g)) != Axis.NONE) {
                        position = compositeText.getOffsetAtLine(lineNumber - 1);
                        Object[] messageArguments = {lineNumber, position};
                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                        formatter.setLocale(MyLanguage.getLocale());
                        formatter.applyPattern(I18n.DATFILE_LINE);

                        TreeItem trtmNewTreeitem = new TreeItem(errors);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_error.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        switch (flatAxis) {
                        case X:
                            trtmNewTreeitem.setText(new String[] { I18n.DATPARSER_INVALID_INVERT_NEXT_FLAT, formatter.format(messageArguments), "[E0A] " + I18n.DATPARSER_SYNTAX_ERROR }); //$NON-NLS-1$
                            break;
                        case Y:
                            trtmNewTreeitem.setText(new String[] { I18n.DATPARSER_INVALID_INVERT_NEXT_FLAT, formatter.format(messageArguments), "[E0B] " + I18n.DATPARSER_SYNTAX_ERROR }); //$NON-NLS-1$
                            break;
                        case Z:
                            trtmNewTreeitem.setText(new String[] { I18n.DATPARSER_INVALID_INVERT_NEXT_FLAT, formatter.format(messageArguments), "[E0C] " + I18n.DATPARSER_SYNTAX_ERROR }); //$NON-NLS-1$
                            break;
                        case NONE:
                        default:
                            trtmNewTreeitem.setText(new String[] { I18n.DATPARSER_INVALID_INVERT_NEXT_FLAT, formatter.format(messageArguments), "[E0D] " + I18n.DATPARSER_SYNTAX_ERROR }); //$NON-NLS-1$
                            break;
                        }trtmNewTreeitem.setData(position);
                    }

                } else {

                    position = compositeText.getOffsetAtLine(lineNumber - 1);
                    Object[] messageArguments = {lineNumber, position};
                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                    formatter.setLocale(MyLanguage.getLocale());
                    formatter.applyPattern(I18n.DATFILE_LINE);

                    TreeItem trtmNewTreeitem = new TreeItem(errors);
                    trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_error.png")); //$NON-NLS-1$
                    trtmNewTreeitem.setVisible(false);
                    trtmNewTreeitem.setText(new String[] { I18n.DATPARSER_INVALID_INVERT_NEXT, formatter.format(messageArguments), "[E0D] " + I18n.DATPARSER_SYNTAX_ERROR }); //$NON-NLS-1$
                    trtmNewTreeitem.setData(position);
                }
            }
            lineNumber++;
        }

        if (unselectBgPicture) {
            vertices.setSelectedBgPicture(null);
            vertices.setSelectedBgPictureIndex(0);
            Editor3DWindow.getWindow().updateBgPictureTab();
        }

        duplicate.pushDuplicateCheck(drawChainAnchor);
        updateDuplicatesErrors(compositeText, duplicates);
        datHeader.pushDatHeaderCheck(drawChainAnchor, compositeText, hints, warnings, errors, duplicates, problemCount);
        updateDatHeaderHints(compositeText, hints);

        warnings.sortItems();
        errors.sortItems();
        hints.getParent().build();
        NLogger.debug(getClass(), "Total time to parse (error check only): {0} ms", System.currentTimeMillis() - start); //$NON-NLS-1$
        vertices.validateState();
        NLogger.debug(getClass(), "Total time to parse + validate: {0} ms", System.currentTimeMillis() - start); //$NON-NLS-1$
    }

    public boolean updateDuplicatesErrors(StyledText compositeText, TreeItem duplicates) {
        if (!duplicates.getItems().isEmpty() || !GData.CACHE_duplicates.isEmpty()) {
            int position;
            duplicates.getItems().clear();
            Set<GData> entriesToRemove = new HashSet<>();
            SortedMap<Integer, ParsingResult> results = new TreeMap<>();

            for (Entry<GData, ParsingResult> entry : GData.CACHE_duplicates.entrySet()) {
                Integer lineNumber2 = drawPerLine.getKey(entry.getKey());
                if (lineNumber2 == null) {
                    entriesToRemove.add(entry.getKey());
                } else {
                    results.put(lineNumber2, entry.getValue());
                }
            }

            for (Entry<Integer, ParsingResult> entry : results.entrySet()) {
                int lineNumber2 = entry.getKey();
                ParsingResult result = entry.getValue();
                try {
                    position = compositeText.getOffsetAtLine(lineNumber2 - 1);
                } catch (IllegalArgumentException iae) {
                    continue;
                }
                Object[] messageArguments = {lineNumber2, position};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.getLocale());
                formatter.applyPattern(I18n.DATFILE_LINE);

                TreeItem trtmNewTreeitem = new TreeItem(duplicates);
                trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_duplicate.png")); //$NON-NLS-1$
                trtmNewTreeitem.setVisible(false);
                trtmNewTreeitem.setText(new String[] { result.getMessage(), formatter.format(messageArguments), result.getType() });
                trtmNewTreeitem.setData(position);
            }
            for (GData gd2 : entriesToRemove) {
                GData.CACHE_duplicates.remove(gd2);
            }
            compositeText.update();
            compositeText.redraw();
            return true;
        }
        return false;
    }

    synchronized boolean updateDatHeaderHints(StyledText compositeText, TreeItem headerHints) {
        ThreadsafeSortedMap<Integer, List<ParsingResult>> cachedHeaderHints = datHeader.cachedHeaderHints;
        if (!cachedHeaderHints.isEmpty()) {
            int position = 0;

            final Integer firstKey = cachedHeaderHints.firstKey();
            List<ParsingResult> allParsingResults = cachedHeaderHints.get(firstKey);
            if (allParsingResults.isEmpty()) {
                if (!headerHints.getItems().isEmpty()) {
                    headerHints.getItems().clear();
                    return true;
                }
                return false;
            }

            headerHints.getItems().clear();

            SortedMap<Integer, List<ParsingResult>> results = new TreeMap<>();
            for (ParsingResult entry : allParsingResults) {
                Integer lineNumber = entry.getLineNumber();
                List<ParsingResult> results2 = new ArrayList<>();
                results.putIfAbsent(lineNumber, results2);
                results2 = results.get(lineNumber);
                results2.add(entry);
            }

            for (Entry<Integer, List<ParsingResult>> entry : results.entrySet()) {

                final int lineNumber2 = entry.getKey();
                final boolean isLineBoundHint = lineNumber2 > 0;
                List<ParsingResult> parsingResults = entry.getValue();
                try {
                    if (isLineBoundHint) {
                        position = compositeText.getOffsetAtLine(lineNumber2 - 1);
                    }
                } catch (IllegalArgumentException iae) {
                    continue;
                }

                if (isLineBoundHint) {
                    for (ParsingResult result : parsingResults) {
                        Object[] messageArguments = {lineNumber2, position};
                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                        formatter.setLocale(MyLanguage.getLocale());
                        formatter.applyPattern(I18n.DATFILE_LINE);
                        TreeItem trtmNewTreeitem = new TreeItem(headerHints);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        trtmNewTreeitem.setText(new String[] { result.getMessage(), formatter.format(messageArguments), result.getType() });
                        trtmNewTreeitem.setData(position);
                    }
                } else {
                    for (ParsingResult result : parsingResults) {
                        TreeItem trtmNewTreeitem = new TreeItem(headerHints);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        trtmNewTreeitem.setText(new String[] { result.getMessage(), "---", result.getType() }); //$NON-NLS-1$
                        trtmNewTreeitem.setData(result.getLineNumber());
                    }
                }
            }
            compositeText.update();
            compositeText.redraw();
            return true;
        }
        return false;
    }

    private boolean isNotBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return false;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }


    void parseForChanges(String[] lines) {
        final boolean tmpDrawSelection = isDrawSelection();
        setDrawSelection(false);

        Project.getParsedFiles().add(this);
        Project.addOpenedFile(this);

        Set<String> alreadyParsed = new HashSet<>();
        alreadyParsed.add(getShortName());

        final GColour col16 = LDConfig.getColour16();

        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();
        GData.CACHE_warningsAndErrors.clear();


        GData oldG = drawChainAnchor.next;
        GData newG;
        int lineNumber = 1;
        int oldLineCount = drawPerLine.size();
        drawChainTail = drawChainAnchor;

        for (String line : lines) {
            if (oldG == null) {
                if (isNotBlank(line)) {
                    List<ParsingResult> results = DatParser.parseLine(line, lineNumber, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, this, false, alreadyParsed);
                    newG = results.get(0).getGraphicalData();
                    if (newG == null) {
                        newG = new GData0(line, View.DUMMY_REFERENCE);
                    } else {
                        newG.setText(line);
                    }
                } else {
                    newG = new GData0(line, View.DUMMY_REFERENCE);
                }
                drawPerLine.put(lineNumber, newG);
            } else {
                String oldS = oldG.toString();
                if (!line.equals(oldS)) {
                    GData next = oldG.next;
                    vertices.remove(oldG);
                    oldG = next;
                    if (isNotBlank(line)) {
                        List<ParsingResult> results = DatParser.parseLine(line, lineNumber, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, this, false, alreadyParsed);
                        newG = results.get(0).getGraphicalData();
                        if (newG == null) {
                            newG = new GData0(line, View.DUMMY_REFERENCE);
                        } else {
                            newG.setText(line);
                        }
                    } else {
                        newG = new GData0(line, View.DUMMY_REFERENCE);
                    }
                    drawPerLine.put(lineNumber, newG);
                } else {
                    drawPerLine.put(lineNumber, oldG);
                    oldG = oldG.next;
                }
            }
            lineNumber++;
        }

        for (int l = lines.length + 1; l <= oldLineCount; l++) {
            vertices.remove(drawPerLine.getValue(l));
            drawPerLine.removeByKey(l);
        }

        {
            GData previous = drawChainAnchor;
            for (int l = 1; l <= lines.length; l++) {
                previous.setNext(drawPerLine.getValue(l));
                previous = drawPerLine.getValue(l);
            }
            drawChainTail = previous;
            drawChainTail.setNext(null);
        }

        vertices.validateState();
        setDrawSelection(tmpDrawSelection);
    }


    public void parseForData(boolean addHistory) {
        final boolean tmpDrawSelection = isDrawSelection();
        setDrawSelection(false);

        Project.getParsedFiles().add(this);
        Project.addOpenedFile(this);

        Set<String> alreadyParsed = new HashSet<>();
        alreadyParsed.add(getShortName());

        String[] lines;
        if (Project.getUnsavedFiles().contains(this) || View.DUMMY_DATFILE.equals(this)) {
            lines = pattern.split(text, -1);
            if (lines.length == 0) {
                lines = new String[]{""}; //$NON-NLS-1$
            }
        } else {
            StringBuilder sb = new StringBuilder();
            List<String> lines2 = new ArrayList<>(4096);
            try (UTF8BufferedReader reader = new UTF8BufferedReader(this.getOldName())) {
                String line = reader.readLine();
                if (line != null) {
                    sb.append(line);
                    lines2.add(line);
                    while (true) {
                        String line2 = reader.readLine();
                        if (line2 == null) {
                            break;
                        }
                        sb.append(StringHelper.getLineDelimiter());
                        sb.append(line2);
                        lines2.add(line2);
                    }
                } else {
                    lines2.add(""); //$NON-NLS-1$
                }

                lastModified = new File(getOldName()).lastModified();

            } catch (FileNotFoundException | LDParsingException e) {
                NLogger.error(DatFile.class, e);
            }
            lines = lines2.toArray(new String[lines2.size()]);
            setLastSavedOpened(new Date());
            originalText = sb.toString();
            text = originalText;
        }

        GData anchorData = drawChainAnchor;
        GData targetData = null;

        List<ParsingResult> results;
        final GColour col16 = LDConfig.getColour16();

        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();
        drawPerLine.clear();
        vertices.clear(); // The vertex structure needs a re-build

        GData gdata;
        int lineNumber = 1;
        for (String line : lines) {

            if (isNotBlank(line)) {
                results = DatParser.parseLine(line, lineNumber, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, this, false, alreadyParsed);

                gdata = results.get(0).getGraphicalData();
                if (gdata == null) {
                    gdata = new GData0(line, View.DUMMY_REFERENCE);
                } else {
                    gdata.setText(line);
                }

                anchorData.setNext(gdata);
                anchorData = gdata;
                drawPerLine.put(lineNumber, gdata);

            } else {
                gdata = new GData0(line, View.DUMMY_REFERENCE);
                anchorData.setNext(gdata);
                anchorData = gdata;
                drawPerLine.put(lineNumber, gdata);
            }
            lineNumber++;
        }

        anchorData.setNext(targetData);
        drawChainTail = anchorData;

        final GData descriptionline = drawChainAnchor.getNext();
        if (descriptionline != null) {
            String descr = descriptionline.toString();
            if (descr.length() > 1)
                descr = descr.substring(2);
            description = " - " + descr; //$NON-NLS-1$
        }

        if (addHistory) addHistory();
        setDrawSelection(tmpDrawSelection);
    }

    public HashBiMap<Integer, GData> getDrawPerLine() {
        return drawPerLine.copy();
    }

    public HashBiMap<Integer, GData> getDrawPerLineNoClone() {
        return drawPerLine;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (oldName == null ? 0 : oldName.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DatFile other = (DatFile) obj;
        if (oldName == null) {
            if (other.oldName != null)
                return false;
        } else if (!oldName.equals(other.oldName))
            return false;
        return true;
    }

    /**
     * @return the type (1 = Part, 2 = Subpart, 3 = Primitive, 4 =
     *         Hi-Res-Primitive)
     */
    public DatType getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set (1 = Part, 2 = Subpart, 3 = Primitive, 4 =
     *            Hi-Res-Primitive)
     */
    public void setType(DatType type) {
        this.type = type;
    }

    public VertexManager getVertexManager() {
        return vertices;
    }

    public GData getDrawChainTail() {
        if (drawChainTail == null) {
            GData gd = drawChainAnchor;
            do {
                drawChainTail = gd;
            } while ((gd = gd.getNext()) != null);
        }
        return drawChainTail;
    }

    public GData getDrawChainStart() {
        return drawChainAnchor;
    }

    public void setDrawChainTail(GData drawChainTail) {
        this.drawChainTail = drawChainTail;
    }

    public boolean isVirtual() {
        return virtual;
    }

    private void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    public void addToTailOrInsertAfterCursor(GData gdata) {
        if (InsertAtCursorPositionToolItem.isInsertingAtCursorPosition()) {
            insertAfterCursor(gdata);
        } else {
            addToTail(gdata);
        }
    }

    public void insertAfterCursor(GData gdata) {
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            for (CTabItem t : w.getTabFolder().getItems()) {
                CompositeTabState state = ((CompositeTab) t).getState();
                if (this.equals(state.getFileNameObj())) {
                    StyledText st = ((CompositeTab) t).getTextComposite();
                    int s1 = st.getSelectionRange().x;
                    if (s1 > -1) {
                        int line = st.getLineAtOffset(s1) + 1;
                        GData target = null;
                        target = drawPerLine.getValue(line);
                        if (target != null) {
                            boolean doReplace = false;
                            boolean insertEmptyLine = true;
                            if (target.type() == 0) {
                                doReplace = !StringHelper.isNotBlank(target.toString());
                            }
                            if (doReplace) {
                                GData next = target.getNext();
                                if (next != null && next.type() == 0) {
                                    insertEmptyLine = StringHelper.isNotBlank(next.toString());
                                }
                                replaceComment(target, gdata);
                                if (insertEmptyLine) insertAfter(gdata, new GData0("", View.DUMMY_REFERENCE)); //$NON-NLS-1$
                            } else  {
                                insertAfter(target, gdata);
                                insertAfter(gdata, new GData0("", View.DUMMY_REFERENCE)); //$NON-NLS-1$
                            }
                            state.setSync(true);
                            try {
                                if (doReplace) {
                                    if (insertEmptyLine) {
                                        int offset = st.getOffsetAtLine(line - 1);
                                        st.setSelection(offset, offset + target.toString().length());
                                        st.insert(gdata.toString() + StringHelper.getLineDelimiter());
                                        offset += StringHelper.getLineDelimiter().length() + gdata.toString().length();
                                        st.setSelection(offset, offset);
                                    } else {
                                        int offset = st.getOffsetAtLine(line - 1);
                                        st.setSelection(offset, offset + target.toString().length());
                                        st.insert(gdata.toString());
                                        offset += StringHelper.getLineDelimiter().length() + gdata.toString().length();
                                        st.setSelection(offset, offset);
                                    }
                                } else {
                                    int offset = st.getOffsetAtLine(line - 1) + target.toString().length() + StringHelper.getLineDelimiter().length();
                                    st.setSelection(offset, offset);
                                    st.insert(StringHelper.getLineDelimiter() + gdata.toString());
                                    offset += StringHelper.getLineDelimiter().length() + gdata.toString().length();
                                    st.setSelection(offset, offset);
                                }
                            } catch (IllegalArgumentException consumed) {
                                NLogger.debug(DatFile.class, consumed);
                            }
                            state.setSync(false);
                        }
                        return;
                    }
                }
            }
        }
        addToTail(gdata);
    }

    public void addToTail(GData gdata) {
        Integer lineNumber = drawPerLine.keySet().size() + 1;
        drawPerLine.put(lineNumber, gdata);
        GData tail = drawPerLine.getValue(lineNumber - 1);
        if (tail == null) {
            drawChainTail = null;
            tail = getDrawChainTail();
        }
        tail.setNext(gdata);
        drawChainTail = gdata;
    }

    public void insertAfter(GData target, GData gdata) {
        GData tail = drawPerLine.getValue(drawPerLine.keySet().size());
        if (tail == null) {
            drawChainTail = null;
            tail = getDrawChainTail();
        }
        if (target.equals(tail)) {
            addToTail(gdata);
            return;
        }

        GData next = target.getNext();
        target.setNext(gdata);
        gdata.setNext(next);

        drawPerLine.clear();

        int i = 1;
        for (GData start = drawChainAnchor.getNext(); start != null; start = start.getNext()) {
            drawPerLine.put(i, start);
            i++;
        }
    }

    private void replaceComment(GData target, GData gdata) {
        if (target.type() != 0) return;
        GData tail = drawPerLine.getValue(drawPerLine.keySet().size());
        if (tail == null) {
            drawChainTail = null;
            tail = getDrawChainTail();
        }
        GData next = target.getNext();
        GData before = target.getBefore();
        before.setNext(gdata);
        gdata.setNext(next);

        if (target.equals(tail)) {
            drawChainTail = gdata;
        }
        drawPerLine.put(drawPerLine.getKey(target), gdata);
        target.derefer();
    }

    public Vertex getNearestObjVertex1() {
        return nearestObjVertex1;
    }

    public void setNearestObjVertex1(Vertex nearestObjVertex1) {
        this.nearestObjVertex1 = nearestObjVertex1;
    }

    public Vertex getNearestObjVertex2() {
        return nearestObjVertex2;
    }

    public void setNearestObjVertex2(Vertex nearestObjVertex2) {
        this.nearestObjVertex2 = nearestObjVertex2;
    }

    public Vertex getObjVertex1() {
        return objVertex1;
    }

    public void setObjVertex1(Vertex objVertex1) {
        this.objVertex1 = objVertex1;
    }

    public Vertex getObjVertex2() {
        return objVertex2;
    }

    public void setObjVertex2(Vertex objVertex2) {
        this.objVertex2 = objVertex2;
    }

    public Vertex getObjVertex3() {
        return objVertex3;
    }

    public void setObjVertex3(Vertex objVertex3) {
        this.objVertex3 = objVertex3;
    }

    public Vertex getObjVertex4() {
        return objVertex4;
    }

    public void setObjVertex4(Vertex objVertex4) {
        this.objVertex4 = objVertex4;
    }

    public void disposeData() {
        history.deleteHistory();
        duplicate.deleteDuplicateInfo();
        datHeader.deleteHeaderHints();
        GDataCSG.fullReset(this);
        text = ""; //$NON-NLS-1$
        vertices.setModified(false, true);
        vertices.clear();
        Set<Integer> lineNumbers = drawPerLine.keySet();
        for (Integer lineNumber : lineNumbers) {
            drawPerLine.getValue(lineNumber).derefer();
        }
        drawPerLine.clear();
        copyDrawPerLine.clear();
        drawChainAnchor.setNext(null);
        Project.getParsedFiles().remove(this);
    }

    @Override
    public String toString() {
        return oldName;
    }

    public String getShortName() {
        String shortFilename = new File(newName).getName();
        shortFilename = shortFilename.toLowerCase(Locale.ENGLISH);
        shortFilename = appendTypeInfoToShortName(shortFilename);
        return shortFilename;
    }

    public String getShortNameMixedCase() {
        String shortFilename = new File(newName).getName();
        shortFilename = appendTypeInfoToShortName(shortFilename);
        return shortFilename;
    }

    private String appendTypeInfoToShortName(String shortFilename) {
        shortFilename = shortFilename.replace("\\", File.separator); //$NON-NLS-1$
        if (type.equals(DatType.SUBPART)) {
            shortFilename = "S" + File.separator + shortFilename; //$NON-NLS-1$
        } else if (type.equals(DatType.PRIMITIVE8)) {
            shortFilename = "8" + File.separator + shortFilename; //$NON-NLS-1$
        } else if (type.equals(DatType.PRIMITIVE48)) {
            shortFilename = "48" + File.separator + shortFilename; //$NON-NLS-1$
        }
        return shortFilename;
    }

    public boolean isProjectFile() {
        return projectFile;
    }

    public boolean save() {

        if (readOnly) {
            // Don't save read only files!
            return true;
        }

        text = getText();

        boolean deleteFirst = oldName.equals(newName);

        File newFile = new File(newName);
        if (deleteFirst) {
            File oldFile = new File(oldName);
            if (oldFile.exists()) {
                if (checkFileCollision(oldFile)) {
                    return true;
                }
                try {
                    if (Files.deleteIfExists(oldFile.toPath())) {
                        NLogger.debug(getClass(), "Deleted old file before saving {0}", oldFile.toPath()); //$NON-NLS-1$
                    }
                } catch (IOException e) {
                    NLogger.error(getClass(), e);
                }
            }
        } else {
            if (newFile.exists() && checkFileCollision(newFile)) {
                return true;
            }
            File oldFile = new File(oldName);
            if (oldFile.exists() && oldFile.lastModified() == lastModified) {
                try {
                    if (Files.deleteIfExists(oldFile.toPath())) {
                        NLogger.debug(getClass(), "Deleted old file {0}", oldFile.toPath()); //$NON-NLS-1$
                    }
                } catch (IOException e) {
                    NLogger.error(getClass(), e);
                }
            }
        }
        if (!newFile.getParentFile().exists() && newFile.getParentFile().mkdirs()) {
            NLogger.debug(getClass(), "Created the parent directory for the part file."); //$NON-NLS-1$
        }
        List<String> lines = new ArrayList<>();
        lines.addAll(Arrays.asList(text.split("\r?\n|\r", -1))); //$NON-NLS-1$
        if (!lines.isEmpty()) {
            final int index = lines.size() - 1;
            String lastLine = lines.get(index);
            if (!isNotBlank(lastLine)) {
                lines.remove(index);
            }
        }
        if (lines.isEmpty())
            lines.add(""); //$NON-NLS-1$

        try (UTF8PrintWriter r = new UTF8PrintWriter(newName)) {
            for (String line : lines) {
                r.println(line);
            }

            r.flush();

            if (!deleteFirst) {
                File oldFile = new File(oldName);
                if (Files.deleteIfExists(oldFile.toPath())) {
                    NLogger.debug(getClass(), "Deleted old file {0}", oldFile.toPath()); //$NON-NLS-1$
                }
            }
            // File was saved. It is not virtual anymore.
            setVirtual(false);
            originalText = text;
            oldName = newName;
            setLastSavedOpened(new Date());
            lastModified = new File(getNewName()).lastModified();
            Project.removeUnsavedFile(this);
            Set<EditorTextWindow> windows = new HashSet<>(Project.getOpenTextWindows());
            for (EditorTextWindow win : windows) {
                win.updateTabWithDatfile(this);
            }
            return true;
        } catch (Exception ex) {
            NLogger.error(getClass(), ex);
            return false;
        }
    }

    public boolean saveForced() {
        text = getText();
        File newFile = new File(newName);

        try {
            if (Files.deleteIfExists(newFile.toPath())) {
                NLogger.debug(getClass(), "Deleted new file (forced save) {0}", newFile.toPath()); //$NON-NLS-1$
            }
        } catch (IOException e) {
            NLogger.error(getClass(), e);
        }

        if (!newFile.getParentFile().exists() && newFile.getParentFile().mkdirs()) {
            NLogger.debug(getClass(), "Created the parent directory for the part file (forced save)."); //$NON-NLS-1$
        }

        try (UTF8PrintWriter r = new UTF8PrintWriter(newName)) {
            List<String> lines = new ArrayList<>();
            lines.addAll(Arrays.asList(text.split("\r?\n|\r", -1))); //$NON-NLS-1$
            if (!lines.isEmpty()) {
                final int index = lines.size() - 1;
                String lastLine = lines.get(index);
                if (!isNotBlank(lastLine)) {
                    lines.remove(index);
                }
            }
            if (lines.isEmpty())
                lines.add(""); //$NON-NLS-1$

            for (String line : lines) {
                r.println(line);
            }
            r.flush();
            // File was saved. It is not virtual anymore.
            setVirtual(false);
            originalText = text;
            oldName = newName;
            setLastSavedOpened(new Date());
            lastModified = new File(getNewName()).lastModified();
            Project.removeUnsavedFile(this);
            Set<EditorTextWindow> windows = new HashSet<>(Project.getOpenTextWindows());
            for (EditorTextWindow win : windows) {
                win.updateTabWithDatfile(this);
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean saveAs(String newName) {
        text = getText();
        File newFile = new File(newName);
        if (newFile.exists()) {
            try {
                if (Files.deleteIfExists(newFile.toPath())) {
                    NLogger.debug(getClass(), "Deleted new file before saving {0}", newFile.toPath()); //$NON-NLS-1$
                }
            } catch (IOException ex) {
                NLogger.error(getClass(), ex);
            }
        }
        if (!newFile.getParentFile().exists() && newFile.getParentFile().mkdirs()) {
            NLogger.debug(getClass(), "Created the parent directory for the part file."); //$NON-NLS-1$
        }
        try (UTF8PrintWriter r = new UTF8PrintWriter(newName)) {
            List<String> lines = new ArrayList<>();
            lines.addAll(Arrays.asList(text.split("\r?\n|\r", -1))); //$NON-NLS-1$
            if (!lines.isEmpty()) {
                final int index = lines.size() - 1;
                String lastLine = lines.get(index);
                if (!isNotBlank(lastLine)) {
                    lines.remove(index);
                }
            }
            if (lines.isEmpty())
                lines.add(""); //$NON-NLS-1$

            // Write the new "0 Name: "
            if (lines.size() > 1) {
                final Pattern whitespace = Pattern.compile("\\s+"); //$NON-NLS-1$
                final int maxDetectionLines = Math.min(10, lines.size());

                // 1. Detect the file type
                String folderPrefix = ""; //$NON-NLS-1$
                for (int i = 0; i < maxDetectionLines; i++) {
                    String tLine = whitespace.matcher(lines.get(i)).replaceAll(" ").trim(); //$NON-NLS-1$
                    if (tLine.startsWith("0 !LDRAW_ORG")) { //$NON-NLS-1$
                        String typeSuffix = ""; //$NON-NLS-1$
                        String path = newFile.getParent();

                        if (getType() == DatType.SUBPART || path.endsWith(File.separator + "S") || path.endsWith(File.separator + "s")) { //$NON-NLS-1$ //$NON-NLS-2$
                            typeSuffix = "Unofficial_Subpart"; //$NON-NLS-1$
                            folderPrefix = "s\\"; //$NON-NLS-1$
                        } else if (getType() == DatType.PRIMITIVE48 || path.endsWith(File.separator + "P" + File.separator + "48") || path.endsWith(File.separator + "p" + File.separator + "48")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            typeSuffix = "Unofficial_48_Primitive"; //$NON-NLS-1$
                            folderPrefix = "48\\"; //$NON-NLS-1$
                        } else if (getType() == DatType.PRIMITIVE8 || path.endsWith(File.separator + "P" + File.separator + "8") || path.endsWith(File.separator + "p" + File.separator + "8")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            typeSuffix = "Unofficial_8_Primitive"; //$NON-NLS-1$
                            folderPrefix = "8\\"; //$NON-NLS-1$
                        } else if (getType() == DatType.PRIMITIVE || path.endsWith(File.separator + "P") || path.endsWith(File.separator + "p")) { //$NON-NLS-1$ //$NON-NLS-2$
                            typeSuffix = "Unofficial_Primitive"; //$NON-NLS-1$
                        } else if (tLine.contains("Flexible_Section")) { //$NON-NLS-1$
                            typeSuffix = "Unofficial_Part Flexible_Section"; //$NON-NLS-1$
                        }

                        if (!"".equals(typeSuffix)) { //$NON-NLS-1$
                            lines.set(i, "0 !LDRAW_ORG " + typeSuffix); //$NON-NLS-1$
                        }
                        break;
                    }
                }

                // 2. Set the new name
                for (int i = 0; i < maxDetectionLines; i++) {
                    String tLine = whitespace.matcher(lines.get(i)).replaceAll(" ").trim(); //$NON-NLS-1$
                    if (tLine.startsWith("0 Name:")) { //$NON-NLS-1$
                        lines.set(i, "0 Name: " + folderPrefix + newFile.getName()); //$NON-NLS-1$
                        break;
                    }
                }
            }

            for (String line : lines) {
                r.println(line);
            }
            r.flush();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean checkFileCollision(File theFile) {
        if (theFile.lastModified() > lastModified && lastModified != 0) {
            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.CANCEL | SWT.NO);
            messageBox.setText(I18n.DIALOG_MODIFIED_TITLE);

            Object[] messageArguments = {getShortName(), getLastSavedOpened()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.DIALOG_MODIFIED);
            messageBox.setMessage(formatter.format(messageArguments));

            int result2 = messageBox.open();
            if (result2 == SWT.CANCEL) {
                return true;
            } else if (result2 == SWT.YES) {
                Project.removeUnsavedFile(this);
                parseForData(true);
                Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                Set<EditorTextWindow> windows = new HashSet<>(Project.getOpenTextWindows());
                for (EditorTextWindow win : windows) {
                    win.updateTabWithDatfile(this);
                }
                return true;
            }
        }
        return false;
    }

    public Date getLastSavedOpened() {
        return lastSavedOpened;
    }

    private void setLastSavedOpened(Date lastSavedOpened) {
        this.lastSavedOpened = lastSavedOpened;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public void updateLastModified() {
        if (oldName.equals(newName)) {
            File oldFile = new File(oldName);
            if (oldFile.exists()) {
                lastModified = oldFile.lastModified();
            }
        } else {
            File newFile = new File(newName);
            if (newFile.exists() && checkFileCollision(newFile)) {
                lastModified = newFile.lastModified();
            }
        }
    }

    public String getSourceText() {
        StringBuilder source = new StringBuilder();
        if (originalText.isEmpty()) {
            try (UTF8BufferedReader reader = new UTF8BufferedReader(this.getOldName())) {
                String line = reader.readLine();
                if (line != null) {
                    source.append(line);
                    while (true) {
                        String line2 = reader.readLine();
                        if (line2 == null) {
                            break;
                        }
                        source.append(StringHelper.getLineDelimiter());
                        source.append(line2);
                    }
                }
            } catch (FileNotFoundException | LDParsingException e) {
                NLogger.error(DatFile.class, e);
            }
        } else {
            GData data2draw = drawChainAnchor;
            if ((data2draw = data2draw.getNext()) != null) {
                source.append(data2draw.toString());
            }
            if (data2draw != null) {
                while ((data2draw = data2draw.getNext()) != null) {
                    source.append(StringHelper.getLineDelimiter());
                    source.append(data2draw.toString());
                }
            }
        }
        return source.toString();
    }

    public String getTextDirect() {
        return text;
    }

    public boolean hasNoBackgroundPictures() {
        GData data2draw = drawChainAnchor;
        while ((data2draw = data2draw.getNext()) != null) {
            if (data2draw.type() == 10) return false;
        }
        return true;
    }

    public int getBackgroundPictureCount() {
        int count = 0;
        GData data2draw = drawChainAnchor;
        while ((data2draw = data2draw.getNext()) != null) {
            if (data2draw.type() == 10) count++;
        }
        return count;
    }

    public GDataPNG getBackgroundPicture(int index) {
        int count = 0;
        GData data2draw = drawChainAnchor;
        while ((data2draw = data2draw.getNext()) != null) {
            if (data2draw.type() == 10) {
                if (count == index) return (GDataPNG) data2draw;
                count++;
            }
        }
        return null;
    }

    public Composite3D getLastSelectedComposite() {
        return lastSelectedComposite;
    }

    public void setLastSelectedComposite(Composite3D lastSelectedComposite) {
        if (lastSelectedComposite != null && !lastSelectedComposite.isDisposed()) {
            Editor3DWindow.getWindow().refreshLockedPerspectiveButton(lastSelectedComposite);
        }
        this.lastSelectedComposite = lastSelectedComposite;
    }

    public boolean isDrawSelection() {
        return drawSelection;
    }

    public void setDrawSelection(boolean drawSelection) {
        this.drawSelection = drawSelection;
    }

    public void setProjectFile(boolean projectFile) {
        this.projectFile = projectFile;
    }

    public HistoryManager getHistory() {
        return history;
    }

    public void setHistory(HistoryManager history) {
        this.history = history;
    }

    public DuplicateManager getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(DuplicateManager duplicate) {
        this.duplicate = duplicate;
    }

    public DatHeaderManager getDatHeader() {
        return datHeader;
    }

    public void setDatHeader(DatHeaderManager datHeader) {
        this.datHeader = datHeader;
    }

    public void addHistory() {
        NLogger.debug(getClass(), "Added history entry for {0}", getShortName()); //$NON-NLS-1$
        final long start = System.currentTimeMillis();
        vertices.storeAxisForSlantingMatrixProjector();
        final int objCount = drawPerLine.size();
        GData[] backup = new GData[objCount];
        Map<String, List<Boolean>> backupHiddenData = null;
        Map<String, List<Boolean>> backupSelectedData = null;
        int count = 0;
        GData data2draw = drawChainAnchor.getNext();
        final boolean isBackupHiddenData = !vertices.hiddenData.isEmpty();
        final boolean isBackupSelectedData = !vertices.selectedData.isEmpty();
        if (isBackupHiddenData && isBackupSelectedData) {
            backupHiddenData = new HashMap<>();
            backupSelectedData = new HashMap<>();
            vertices.backupHideShowAndSelectedState(backupHiddenData, backupSelectedData);
        } else {
            if (isBackupHiddenData) {
                backupHiddenData = vertices.backupHideShowState(new HashMap<>());
            } else {
                backupHiddenData = new HashMap<>();
            }
            if (isBackupSelectedData) {
                backupSelectedData = vertices.backupSelectedDataState(new HashMap<>());
            } else {
                backupSelectedData = new HashMap<>();
            }
        }
        while (count < objCount) {
            if (data2draw != null) {
                backup[count] = data2draw;
                data2draw = data2draw.getNext();
            } else {
                backup[count] = new GData0("", false, View.DUMMY_REFERENCE); //$NON-NLS-1$
            }
            count++;
        }

        Vertex[] backupSelectedVertices = vertices.getSelectedVertices().toArray(new Vertex[vertices.getSelectedVertices().size()]);
        Vertex[] backupHiddenVertices = vertices.getHiddenVertices().toArray(new Vertex[vertices.getHiddenVertices().size()]);
        history.pushHistory(
                null,
                -1,
                -1,
                backup,
                backupSelectedData,
                backupHiddenData,
                backupSelectedVertices,
                backupHiddenVertices,
                -1
                );
        NLogger.debug(getClass(), "Total time to backup history: {0} ms", System.currentTimeMillis() - start); //$NON-NLS-1$
    }

    public void addHistory(String text, int selectionStart, int selectionEnd, int topIndex) {
        final long start = System.currentTimeMillis();
        vertices.storeAxisForSlantingMatrixProjector();
        NLogger.debug(getClass(), "Added text editor history entry for {0}", getShortName()); //$NON-NLS-1$
        final int objCount = drawPerLine.size();
        GData[] backup = new GData[objCount];
        int count = 0;
        GData data2draw = drawChainAnchor.getNext();
        while (count < objCount) {
            if (data2draw != null) {
                backup[count] = data2draw;
                data2draw = data2draw.getNext();
            } else {
                backup[count] = new GData0("", false, View.DUMMY_REFERENCE); //$NON-NLS-1$
            }
            count++;
        }
        history.pushHistory(
                text,
                selectionStart,
                selectionEnd,
                backup,
                null,
                null,
                null,
                null,
                topIndex
                );
        NLogger.debug(getClass(), "Total time to backup history: {0} ms", System.currentTimeMillis() - start); //$NON-NLS-1$
    }

    public void undo(boolean focusTextEditor) {
        history.undo(focusTextEditor);
    }

    public void redo(boolean focusTextEditor) {
        history.redo(focusTextEditor);
    }

    public static Composite3D getLastHoveredComposite() {
        return lastHoveredComposite;
    }

    public static void setLastHoveredComposite(Composite3D lastHoveredComposite) {
        DatFile.lastHoveredComposite = lastHoveredComposite;
    }

    public boolean isOptimizingCSG() {
        return optimizingCSG;
    }

    public void setOptimizingCSG(boolean optimizingCSG) {
        this.optimizingCSG = optimizingCSG;
    }

    public boolean isFromPartReview() {
        return fromPartReview;
    }

    public BinaryDataRegistry getBinaryData() {
        return binaryData;
    }
}

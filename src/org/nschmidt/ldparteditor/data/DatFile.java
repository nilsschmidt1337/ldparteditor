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
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.MessageBox;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.colour.GCChrome;
import org.nschmidt.ldparteditor.data.colour.GCMatteMetal;
import org.nschmidt.ldparteditor.data.colour.GCMetal;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helpers.math.HashBiMap;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.text.HeaderState;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.text.UTF8PrintWriter;
import org.nschmidt.ldparteditor.widgets.TreeItem;

/**
 * The DAT file class
 *
 * @author nils
 *
 */
public final class DatFile {

    private static final Pattern pattern = Pattern.compile("\r?\n|\r"); //$NON-NLS-1$

    private final boolean readOnly;
    private boolean drawSelection = true;

    private final GData drawChainAnchor = new GDataInit(View.DUMMY_REFERENCE);

    private final HashBiMap<Integer, GData> drawPerLine = new HashBiMap<Integer, GData>();
    private final HashMap<Integer, GData> copy_drawPerLine = new HashMap<Integer, GData>();

    private static final GTexture CUBEMAP_TEXTURE = new GTexture(TexType.PLANAR, "cmap.png", null, 1, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GDataTEX CUBEMAP = new  GDataTEX(null, "", TexMeta.NEXT, CUBEMAP_TEXTURE); //$NON-NLS-1$

    private static final GTexture CUBEMAP_MATTE_TEXTURE = new GTexture(TexType.PLANAR, "matte_metal.png", null, 2, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GDataTEX CUBEMAP_MATTE = new  GDataTEX(null, "", TexMeta.NEXT, CUBEMAP_MATTE_TEXTURE); //$NON-NLS-1$

    private static final GTexture CUBEMAP_METAL_TEXTURE = new GTexture(TexType.PLANAR, "metal.png", null, 2, new Vector3f(1,0,0), new Vector3f(1,1,0), new Vector3f(1,1,1), 0, 0); //$NON-NLS-1$
    private static final GDataTEX CUBEMAP_METAL = new  GDataTEX(null, "", TexMeta.NEXT, CUBEMAP_METAL_TEXTURE); //$NON-NLS-1$


    private final VertexManager vertices = new VertexManager(this);

    private Vertex nearestObjVertex1 = null;
    private Vertex nearestObjVertex2 = null;
    private Vertex objVertex1 = null;
    private Vertex objVertex2 = null;
    private Vertex objVertex3 = null;
    private Vertex objVertex4 = null;

    private boolean virtual;
    private boolean projectFile;

    private DatType type = DatType.PART;
    private long lastModified = 0;

    private String description;
    private String oldName;
    private String newName;
    private String text = ""; //$NON-NLS-1$
    private String originalText = ""; //$NON-NLS-1$

    private Date lastSavedOpened = new Date();

    private GData drawChainTail = null;

    private Composite3D lastSelectedComposite = null;

    private HistoryManager history = new HistoryManager(this);

    public DatFile(String path) {
        this.projectFile = true;
        this.oldName = path;
        this.newName = path;
        this.readOnly = false;
        this.setVirtual(true);
        this.setType(DatType.PART);
    }

    public DatFile(String path, String description, boolean isReadOnly, DatType type) {
        this.projectFile = false;
        this.description = description;
        this.oldName = path;
        this.newName = path;
        this.readOnly = isReadOnly;
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


        GDataCSG.resetCSG();

        GData data2draw = drawChainAnchor;
        int renderMode = c3d.getRenderMode();
        if (!c3d.isDrawingSolidMaterials() && renderMode != 5)
            vertices.draw(c3d);

        if (Editor3DWindow.getWindow().isAddingCondlines())
            renderMode = 6;
        switch (renderMode) {
        case 0: // No BFC
            data2draw.draw(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.draw(c3d);
            }
            break;
        case 1: // Random Colours
            data2draw.drawRandomColours(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawRandomColours(c3d);
            }
            break;
        case 2: // Front-Backface BFC
            data2draw.drawBFC(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                switch (GData.accumClip) {
                case 0:
                    data2draw.drawBFC(c3d);
                    break;
                default:
                    data2draw.draw(c3d);
                    break;
                }
            }
            break;
        case 3: // Backface only BFC
            data2draw.drawBFC_backOnly(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                switch (GData.accumClip) {
                case 0:
                    data2draw.drawBFC_backOnly(c3d);
                    break;
                default:
                    data2draw.draw(c3d);
                    break;
                }
            }
            break;
        case 4: // Real BFC
            data2draw.drawBFC_Colour(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                switch (GData.accumClip) {
                case 0:
                    data2draw.drawBFC_Colour(c3d);
                    break;
                default:
                    data2draw.draw(c3d);
                    break;
                }
            }
            break;
        case 5: // Real BFC with texture mapping
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            data2draw.drawBFC_Textured(c3d);
            vertices.fillVertexNormalCache(data2draw);
            data2draw.drawBFC_Textured(c3d);
            CUBEMAP.drawBFC_Textured(c3d);
            new GData3(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(1,1,0), View.DUMMY_REFERENCE, new GColour(0, 0, 0, 0, 0, new GCChrome())).drawBFC_Textured(c3d.getComposite3D());
            CUBEMAP_MATTE.drawBFC_Textured(c3d);
            new GData3(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(1,1,0), View.DUMMY_REFERENCE, new GColour(0, 0, 0, 0, 0, new GCMatteMetal())).drawBFC_Textured(c3d.getComposite3D());
            CUBEMAP_METAL.drawBFC_Textured(c3d);
            new GData3(new Vertex(0,0,0), new Vertex(1,0,0), new Vertex(1,1,0), View.DUMMY_REFERENCE, new GColour(0, 0, 0, 0, 0, new GCMetal())).drawBFC_Textured(c3d.getComposite3D());
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawBFC_Textured(c3d);
            }
            vertices.clearVertexNormalCache();
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
            data2draw.drawWhileAddCondlines(c3d);
            while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                data2draw.drawWhileAddCondlines(c3d);
            }
            break;
        default:
            break;
        }
        if (c3d.isDrawingSolidMaterials() && renderMode != 5)
            vertices.showHidden();
    }

    public synchronized void getBFCorientationMap(HashMap<GData, Byte> bfcMap) {
        GDataCSG.resetCSG();
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
            parseForData(true);
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
     * @return a list of codelines from this DAT file.
     * <br> This functions reads the contents from the harddrive if the file was not loaded before.
     * <br> The list will be empty if the file can't be read or can't be found
     */
    public ArrayList<String> getSource() {
        ArrayList<String> result = new ArrayList<String>();
        if (originalText.isEmpty() && new File(this.getOldName()).exists()) {
            try {
                UTF8BufferedReader reader = new UTF8BufferedReader(this.getOldName());
                while (true) {
                    String line2 = reader.readLine();
                    if (line2 == null) {
                        break;
                    }
                    result.add(line2);
                }
                reader.close();
            } catch (FileNotFoundException e) {
            } catch (LDParsingException e) {
            } catch (UnsupportedEncodingException e) {}
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
     *
     * @param compositeText
     * @param hints
     * @param warnings
     * @param errors
     */
    public void parseForErrorAndData(StyledText compositeText, int startOffset_pos, int endOffset_pos, int length, String insertedText, String replacedText, TreeItem hints, TreeItem warnings,
            TreeItem errors) {

        HeaderState.state().setState(HeaderState._99_DONE);

        Set<String> alreadyParsed = new HashSet<String>();
        alreadyParsed.add(getShortName());

        GData anchorData = drawChainAnchor;
        GData targetData = null;

        long start = System.currentTimeMillis();

        int startLine = compositeText.getLineAtOffset(startOffset_pos);
        int startOffset = compositeText.getOffsetAtLine(startLine);

        int endLine = compositeText.getLineAtOffset(endOffset_pos);
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
                tailRemoved = vertices.remove(drawPerLine.getValue(actionStartLine)) | tailRemoved;
                drawPerLine.removeByKey(actionStartLine);
                actionStartLine++;
            }

            if (affectedOldLineCount != newLineCount && !drawPerLine.isEmpty()) {
                // Update references at the tail
                int diff = newLineCount - affectedOldLineCount;
                actionStartLine = oldEndLine + 1;
                GData data;

                while ((data = drawPerLine.getValue(actionStartLine)) != null) {
                    copy_drawPerLine.put(actionStartLine + diff, data);
                    drawPerLine.removeByKey(actionStartLine);
                    actionStartLine++;
                }
                for (Integer i : copy_drawPerLine.keySet()) {
                    GData nd = copy_drawPerLine.get(i);
                    drawPerLine.put(i, nd);
                }
                copy_drawPerLine.clear();
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
                    tailRemoved = vertices.remove(drawPerLine.getValue(startLine)) | tailRemoved;
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
                    tailRemoved = vertices.remove(drawPerLine.getValue(startLine)) | tailRemoved;
                    drawPerLine.removeByKey(startLine);

                    int lcount = compositeText.getLineCount() - newLineCount + 1;
                    for (int i = startLine + 1; i < lcount; i++) {
                        copy_drawPerLine.put(i + newLineCount, drawPerLine.getValue(i));
                        drawPerLine.removeByKey(i);
                    }
                    for (Integer i : copy_drawPerLine.keySet()) {
                        GData nd = copy_drawPerLine.get(i);
                        drawPerLine.put(i, nd);
                    }
                    copy_drawPerLine.clear();
                }
            }

        }
        NLogger.debug(getClass(), "Time after OpenGL data change: " + (System.currentTimeMillis() - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        warnings.removeWithinPosition(compositeText, startOffset, endOffset, length - rlength);
        errors.removeWithinPosition(compositeText, startOffset, endOffset, length - rlength);

        int offset = compositeText.getLineDelimiter().length();
        int position = 0;

        ArrayList<ParsingResult> results;

        position = startOffset;

        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();
        GData.CACHE_warningsAndErrors.clear();

        String line;
        GData gdata;
        for (int lineNumber = startLine; lineNumber < endLine + 1; lineNumber++) {

            line = compositeText.getLine(lineNumber - 1);

            if (isNotBlank(line)) {

                results = DatParser.parseLine(line, lineNumber, 0, 0.5f, 0.5f, 0.5f, 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, this, false, alreadyParsed, true);

                gdata = results.get(0).getGraphicalData();
                if (gdata == null) {
                    gdata = new GData0(line);
                } else {
                    gdata.setText(line);
                    GData.CACHE_warningsAndErrors.put(gdata, results);
                }

                anchorData.setNext(gdata);
                anchorData = gdata;
                drawPerLine.put(lineNumber, gdata);

                for (ParsingResult result : results) {
                    switch (result.getTypeNumber()) {
                    case ResultType.WARN: // Warning
                    {
                        TreeItem trtmNewTreeitem = new TreeItem(warnings, SWT.NONE);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_warning.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        trtmNewTreeitem.setText(new String[] { result.getMessage(), "line " + lineNumber + "   [" + position + "]", result.getType() }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  I18N Needs translation
                        trtmNewTreeitem.setData(position);
                    }
                    break;
                    case ResultType.ERROR: // Error
                    {
                        TreeItem trtmNewTreeitem = new TreeItem(errors, SWT.NONE);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_error.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        trtmNewTreeitem.setText(new String[] { result.getMessage(), "line " + lineNumber + "   [" + position + "]", result.getType() }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ I18N Needs translation
                        trtmNewTreeitem.setData(position);
                    }
                    break;
                    default: // Hint
                        break;
                    }
                }
            } else {
                gdata = new GData0(line);
                anchorData.setNext(gdata);
                anchorData = gdata;
                drawPerLine.put(lineNumber, gdata);
            }
            position += line.length() + offset;
        }

        anchorData.setNext(targetData);

        // Get tail
        if (tailRemoved || drawChainTail == null) {
            drawChainTail = anchorData;
        }
        hints.sortItems();
        warnings.sortItems();
        errors.sortItems();
        hints.getParent().build();
        if (DatParser.isUpatePngImages()) {
            Editor3DWindow.getWindow().updateBgPictureTab();
            DatParser.setUpatePngImages(false);
        }
        NLogger.debug(getClass(), "Total time to parse: " + (System.currentTimeMillis() - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        vertices.validateState();
        NLogger.debug(getClass(), "Total time to parse + validate: " + (System.currentTimeMillis() - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Parses the opened dat file for errors and correct data (in realtime, only
     * when opened in text editor)
     *
     * @param compositeText
     * @param hints
     * @param warnings
     * @param errors
     */
    public void parseForError(StyledText compositeText, int startOffset_pos, int endOffset_pos, int length, String insertedText, String replacedText, TreeItem hints, TreeItem warnings, TreeItem errors, boolean unselectBgPicture) {

        if (compositeText.getText().isEmpty()) {
            return;
        }

        Set<String> alreadyParsed = new HashSet<String>();
        alreadyParsed.add(getShortName());

        long start = System.currentTimeMillis();

        int startLine = compositeText.getLineAtOffset(startOffset_pos);
        int startOffset = compositeText.getOffsetAtLine(startLine);

        int endLine = compositeText.getLineAtOffset(endOffset_pos);
        int endOffset = compositeText.getOffsetAtLine(endLine) + compositeText.getLine(endLine).length();

        startLine++;
        endLine++;

        int rlength = replacedText.length();

        warnings.removeWithinPosition(compositeText, startOffset, endOffset, length - rlength);
        errors.removeWithinPosition(compositeText, startOffset, endOffset, length - rlength);

        int offset = StringHelper.getLineDelimiter().length();
        int position = 0;

        ArrayList<ParsingResult> results;

        position = startOffset;

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
                    results = DatParser.parseLine(line, lineNumber, 0, 0.5f, 0.5f, 0.5f, 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, this, true, alreadyParsed, true);
                    GData.CACHE_warningsAndErrors.put(gd, results);
                }

                for (ParsingResult result : results) {
                    switch (result.getTypeNumber()) {
                    case ResultType.WARN: // Warning
                    {
                        TreeItem trtmNewTreeitem = new TreeItem(warnings, SWT.NONE);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_warning.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        trtmNewTreeitem.setText(new String[] { result.getMessage(), "line " + lineNumber + "   [" + position + "]", result.getType() }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  I18N Needs translation
                        trtmNewTreeitem.setData(position);
                    }
                    break;
                    case ResultType.ERROR: // Error
                    {
                        TreeItem trtmNewTreeitem = new TreeItem(errors, SWT.NONE);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_error.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        trtmNewTreeitem.setText(new String[] { result.getMessage(), "line " + lineNumber + "   [" + position + "]", result.getType() }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ I18N Needs translation
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

        if (unselectBgPicture) {
            vertices.setSelectedBgPicture(null);
            vertices.setSelectedBgPictureIndex(0);
            Editor3DWindow.getWindow().updateBgPictureTab();
        }
        hints.sortItems();
        warnings.sortItems();
        errors.sortItems();
        hints.getParent().build();
        NLogger.debug(getClass(), "Total time to parse (error check only): " + (System.currentTimeMillis() - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        vertices.validateState();
        NLogger.debug(getClass(), "Total time to parse + validate: " + (System.currentTimeMillis() - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private boolean isNotBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return false;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(str.charAt(i)) == false) {
                return true;
            }
        }
        return false;
    }

    public void parseForData(boolean addHistory) {

        Project.getParsedFiles().add(this);

        Set<String> alreadyParsed = new HashSet<String>();
        alreadyParsed.add(getShortName());

        String[] lines;
        if (Project.getUnsavedFiles().contains(this) ) {
            lines = pattern.split(text, -1);
            if (lines.length == 0) {
                lines = new String[]{""}; //$NON-NLS-1$
            }
        } else {
            StringBuilder sb = new StringBuilder();
            ArrayList<String> lines2 = new ArrayList<String>(4096);
            try {
                UTF8BufferedReader reader = new UTF8BufferedReader(this.getOldName());
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
                reader.close();

                lastModified = new File(getOldName()).lastModified();

            } catch (FileNotFoundException e) {
            } catch (LDParsingException e) {
            } catch (UnsupportedEncodingException e) {}
            lines = lines2.toArray(new String[lines2.size()]);
            setLastSavedOpened(new Date());
            originalText = sb.toString();
            text = originalText;
        }

        GData anchorData = drawChainAnchor;
        GData targetData = null;

        ArrayList<ParsingResult> results;

        // Parse header
        {
            HeaderState h = new HeaderState();
            HeaderState.setState(h);
            int lineNumber = 1;
            for (String line : lines) {
                if (isNotBlank(line)) {

                    if (!line.startsWith("0")) { //$NON-NLS-1$
                        break;
                    }

                    results = DatParser.parseLine(line, lineNumber, 0, 0f, 0f, 0f, 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, this, true, alreadyParsed, false);
                }
                lineNumber++;
            }
        }
        HeaderState.state().setState(HeaderState._99_DONE);

        // Clear the cache..
        GData.parsedLines.clear();
        GData.CACHE_parsedFilesSource.clear();
        drawPerLine.clear();
        vertices.clear(); // The vertex structure needs a re-build

        GData gdata;
        int lineNumber = 1;
        for (String line : lines) {

            if (isNotBlank(line)) {
                results = DatParser.parseLine(line, lineNumber, 0, 0.5f, 0.5f, 0.5f, 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, this, false, alreadyParsed, false);

                gdata = results.get(0).getGraphicalData();
                if (gdata == null) {
                    gdata = new GData0(line);
                } else {
                    gdata.setText(line);
                }

                anchorData.setNext(gdata);
                anchorData = gdata;
                drawPerLine.put(lineNumber, gdata);

            } else {
                gdata = new GData0(line);
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
    }

    public void parseForHints(StyledText compositeText, TreeItem hints) {

        Set<String> alreadyParsed = new HashSet<String>();
        alreadyParsed.add(getShortName());

        long start = System.currentTimeMillis();

        HeaderState h = new HeaderState();
        HeaderState.setState(h);

        hints.removeAll();

        int offset = StringHelper.getLineDelimiter().length();
        int position = 0;

        int lc = compositeText.getLineCount();

        ArrayList<ParsingResult> results;

        lc++;

        for (int lineNumber = 1; lineNumber < lc; lineNumber++) {
            String line = compositeText.getLine(lineNumber - 1);
            if (isNotBlank(line)) {

                if (!line.startsWith("0")) { //$NON-NLS-1$
                    HeaderState.state().setState(HeaderState._99_DONE);
                    break;
                }

                results = DatParser.parseLine(line, lineNumber, 0, 0f, 0f, 0f, 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, this, true, alreadyParsed, false);
                for (ParsingResult result : results) {
                    if (result.getTypeNumber() == ResultType.HINT) {
                        TreeItem trtmNewTreeitem = new TreeItem(hints, SWT.NONE);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        trtmNewTreeitem.setText(new String[] { result.getMessage(), "line " + lineNumber + "   [" + position + "]", result.getType() }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  I18N Needs translation
                        trtmNewTreeitem.setData(position);
                    }
                }
            }
            position += line.length() + offset;
        }
        {
            h = HeaderState.state();
            results = new ArrayList<ParsingResult>();
            if (!h.hasTITLE())
                results.add(new ParsingResult("The title is missing\nValid examples\n  " //$NON-NLS-1$
                        + "\"0 Minifig Torso with Riding Jacket Pattern\"(Part) \n  " //$NON-NLS-1$
                        + "\"0 ~Door  1 x  2 x  3 with Hole without Studs\"(Subpart) \n  " //$NON-NLS-1$
                        + "\"0 ~Moved to 3842a\"(Moved to) \n  " //$NON-NLS-1$
                        + "\"0 Technic Engine Cylinder Head\"(Alias) \n  " //$NON-NLS-1$
                        + "\"0 _Rock  1 x  1 Crystal  5 Point Chrome Silver\"(Physical Colour)", "[H00] Header Hint", ResultType.HINT)); //$NON-NLS-1$ //$NON-NLS-2$ // I18N Needs translation!
            if (!h.hasNAME())
                results.add(new ParsingResult("The filename information is missing\nValid examples\n  \"0 Name: 30169.dat\"\n  \"0 Name: s\\30169s01.dat\"", "[H10] Header Hint", ResultType.HINT)); //$NON-NLS-1$ //$NON-NLS-2$ // I18N Needs translation!
            if (!h.hasAUTHOR())
                results.add(new ParsingResult("The author name is missing\nValid examples\n  \"0 Author: John Doe\"\n  \"0 Author: Jane Doe [jjdoe]\"", "[H20] Header Hint", ResultType.HINT)); //$NON-NLS-1$ //$NON-NLS-2$ // I18N Needs translation!
            if (!h.hasTYPE())
                results.add(new ParsingResult("The part type information is missing\nValid example\n  \"0 !LDRAW_ORG Unofficial_Part\"\n", "[H30] Header Hint", ResultType.HINT)); //$NON-NLS-1$ //$NON-NLS-2$ // I18N Needs translation!
            if (!h.hasLICENSE())
                results.add(new ParsingResult(
                        "The license information is missing\nAll valid examples\n  \"0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt\"\n  \"0 !LICENSE Not redistributable : see NonCAreadme.txt\"", "[H40] Header Hint", ResultType.HINT)); //$NON-NLS-1$ //$NON-NLS-2$ // I18N Needs translation!
            if (!h.hasBFC())
                results.add(new ParsingResult(
                        "The Back Face Culling (BFC) information is missing\nAll valid examples\n  \"BFC CERTIFY CCW\"\n  \"BFC CERTIFY CW\"\n  \"BFC NOCERTIFY\"", "[H60] Header Hint", ResultType.HINT)); //$NON-NLS-1$ //$NON-NLS-2$ // I18N Needs translation!

            int fakeLine = -1;
            for (ParsingResult result : results) {
                TreeItem trtmNewTreeitem = new TreeItem(hints, SWT.NONE);
                trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
                trtmNewTreeitem.setText(new String[] { result.getMessage(), "---", result.getType() }); //$NON-NLS-1$
                trtmNewTreeitem.setData(fakeLine);
                trtmNewTreeitem.setVisible(false);
                fakeLine--;
            }
        }
        hints.sortItems();
        HeaderState.state().setState(HeaderState._99_DONE);
        NLogger.debug(getClass(), "Total time to parse header: " + (System.currentTimeMillis() - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

    }

    public HashBiMap<Integer, GData> getDrawPerLine() {
        return drawPerLine.copy();
    }

    public HashBiMap<Integer, GData> getDrawPerLine_NOCLONE() {
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
        text = ""; //$NON-NLS-1$
        vertices.setModified(false, true);
        vertices.clear();
        Set<Integer> lineNumbers = drawPerLine.keySet();
        for (Integer lineNumber : lineNumbers) {
            drawPerLine.getValue(lineNumber).derefer();
        }
        drawPerLine.clear();
        copy_drawPerLine.clear();
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
        try {
            shortFilename = shortFilename.replaceAll("\\\\", File.separator); //$NON-NLS-1$
        } catch (Exception e) {
            // Workaround for windows OS / JVM BUG
            shortFilename = shortFilename.replace("\\\\", File.separator); //$NON-NLS-1$
        }
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

        boolean deleteFirst = oldName.equals(newName);

        try {
            if (deleteFirst) {
                File oldFile = new File(oldName);
                if (oldFile.exists()) {
                    if (checkFileCollision(oldFile)) {
                        return true;
                    }
                    oldFile.delete();
                }
            } else {
                File newFile = new File(newName);
                if (newFile.exists()) {
                    if (checkFileCollision(newFile)) {
                        return true;
                    }
                }
                File oldFile = new File(oldName);
                if (oldFile.exists()) {
                    if (oldFile.lastModified() == lastModified) {
                        oldFile.delete();
                    }
                }
            }
            UTF8PrintWriter r = new UTF8PrintWriter(newName);
            ArrayList<String> lines = new ArrayList<String>();
            lines.addAll(Arrays.asList(text.split("\r?\n|\r", -1))); //$NON-NLS-1$

            if (lines.isEmpty())
                lines.add(""); //$NON-NLS-1$

            for (String line : lines) {
                r.println(line);
            }
            r.flush();
            r.close();
            if (!deleteFirst) {
                File oldFile = new File(oldName);
                if (oldFile.exists()) oldFile.delete();
            }
            // File was saved. It is not virtual anymore.
            setVirtual(false);
            originalText = text;
            oldName = newName;
            setLastSavedOpened(new Date());
            lastModified = new File(getNewName()).lastModified();
            Project.removeUnsavedFile(this);
            HashSet<EditorTextWindow> windows = new HashSet<EditorTextWindow>(Project.getOpenTextWindows());
            for (EditorTextWindow win : windows) {
                win.updateTabWithDatfile(this);
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean saveForced() {
        try {
            File newFile = new File(newName);
            if (newFile.exists()) {
                newFile.delete();
            }
            UTF8PrintWriter r = new UTF8PrintWriter(newName);
            ArrayList<String> lines = new ArrayList<String>();
            lines.addAll(Arrays.asList(text.split("\r?\n|\r", -1))); //$NON-NLS-1$

            if (lines.isEmpty())
                lines.add(""); //$NON-NLS-1$

            for (String line : lines) {
                r.println(line);
            }
            r.flush();
            r.close();
            // File was saved. It is not virtual anymore.
            setVirtual(false);
            originalText = text;
            oldName = newName;
            setLastSavedOpened(new Date());
            lastModified = new File(getNewName()).lastModified();
            Project.removeUnsavedFile(this);
            HashSet<EditorTextWindow> windows = new HashSet<EditorTextWindow>(Project.getOpenTextWindows());
            for (EditorTextWindow win : windows) {
                win.updateTabWithDatfile(this);
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean checkFileCollision(File theFile) {
        if (theFile.lastModified() > lastModified) {
            MessageBox messageBox = new MessageBox(Editor3DWindow.getWindow().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.CANCEL | SWT.NO);
            messageBox.setText(I18n.DIALOG_ModifiedTitle);

            Object[] messageArguments = {getShortName(), getLastSavedOpened()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(View.LOCALE);
            formatter.applyPattern(I18n.DIALOG_Modified);
            messageBox.setMessage(formatter.format(messageArguments));

            int result2 = messageBox.open();
            if (result2 == SWT.CANCEL) {
                return true;
            } else if (result2 == SWT.YES) {
                Project.removeUnsavedFile(this);
                parseForData(true);
                Editor3DWindow.getWindow().updateTree_unsavedEntries();
                HashSet<EditorTextWindow> windows = new HashSet<EditorTextWindow>(Project.getOpenTextWindows());
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
            if (newFile.exists()) {
                if (checkFileCollision(newFile)) {
                    lastModified = newFile.lastModified();
                }
            }
        }
    }

    public String getSourceText() {
        StringBuilder source = new StringBuilder();
        if (originalText.isEmpty()) {
            try {
                UTF8BufferedReader reader = new UTF8BufferedReader(this.getOldName());
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
                reader.close();
            } catch (FileNotFoundException e) {
            } catch (LDParsingException e) {
            } catch (UnsupportedEncodingException e) {}
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

    public void addHistory() {
        NLogger.debug(getClass(), "Added history entry for " + getShortName()); //$NON-NLS-1$
        final long start = System.currentTimeMillis();
        final int objCount = drawPerLine.size();
        GData[] backup = new GData[objCount];
        boolean[] backupSelection = new boolean[objCount];
        int count = 0;
        GData data2draw = drawChainAnchor;
        Set<GData> sd = vertices.getSelectedData();
        while (count < objCount) {
            data2draw = data2draw.getNext();
            backup[count] = data2draw;
            backupSelection[count] = sd.contains(data2draw);
            count++;
        }
        Vertex[] backupSelectedVertices = vertices.getSelectedVertices().toArray(new Vertex[vertices.getSelectedVertices().size()]);
        history.pushHistory(
                null,
                -1,
                -1,
                backup,
                backupSelection,
                backupSelectedVertices,
                -1
                );
        NLogger.debug(getClass(), "Total time to backup history: " + (System.currentTimeMillis() - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void addHistory(String text, int selectionStart, int selectionEnd, int topIndex) {
        final long start = System.currentTimeMillis();
        NLogger.debug(getClass(), "Added history entry for " + getShortName()); //$NON-NLS-1$
        history.pushHistory(
                text,
                selectionStart,
                selectionEnd,
                null,
                null,
                null,
                topIndex
                );
        NLogger.debug(getClass(), "Total time to backup history: " + (System.currentTimeMillis() - start) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void undo() {
        history.undo();
    }

    public void redo() {
        history.redo();
    }
}

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
package org.nschmidt.ldparteditor.composites.compositetab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.MessageFormat;

import org.eclipse.jface.window.ApplicationWindow;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;

/**
 * @author nils
 *
 */
public class CompositeTabState {

    /** The current {@linkplain ApplicationWindow} of the composite tab */
    final ApplicationWindow[] window = new ApplicationWindow[1];
    /** The current {@linkplain CompositeTabFolder} of the composite tab */
    final CompositeTabFolder[] folder = new CompositeTabFolder[1];
    /** The DatFile object */
    private DatFile datFileObj = View.DUMMY_DATFILE;
    /** The filename of the file, which is displayed by this tab */
    String filename = I18n.EDITORTEXT_NewFile;
    /**
     * The filename of the file, which is displayed by this tab when the content
     * is modified
     */
    private String filenameWithStar = I18n.EDITORTEXT_NewFile + "*"; //$NON-NLS-1$
    /** The line index of the caret [NOT PUBLIC YET] */
    int currentLineIndex = 0;
    /** The caret position (needed for vertex replacement and so on) */
    int currentCaretPositionLine = 0;
    int currentCaretPositionChar = 0;
    int currentCaretTopIndex = 0;
    /** The tab reference */
    private CompositeTab tab;

    private boolean sync = false;
    private boolean replacingVertex = false;
    private float replaceEpsilon = 0.0001f;
    private BigDecimal toReplaceX = BigDecimal.ONE;
    private BigDecimal toReplaceY = new BigDecimal(2);
    private BigDecimal toReplaceZ = new BigDecimal(3);

    private int oldLineIndex = -1;
    private boolean doingPaste = false;

    public DatFile getFileNameObj() {
        return datFileObj;
    }

    public void setFileNameObj(DatFile fileNameObj) {
        filename = new File(fileNameObj.getNewName()).getName();
        setFilenameWithStar(filename + "*"); //$NON-NLS-1$
        if (fileNameObj.isReadOnly()) {
            filename = filename + " " + I18n.EDITORTEXT_ReadOnly; //$NON-NLS-1$
            setFilenameWithStar(getFilenameWithStar() + " " + I18n.EDITORTEXT_ReadOnly); //$NON-NLS-1$
            getTab().getTextComposite().setEditable(false);
        } else {
            getTab().getTextComposite().setEditable(true);
        }
        this.datFileObj = fileNameObj;
        if (Project.getUnsavedFiles().contains(fileNameObj) || !fileNameObj.getOldName().equals(fileNameObj.getNewName())) {
            this.datFileObj.getVertexManager().setModified_NoSync();
            getTab().getTextComposite().setText(this.datFileObj.getText());
            getTab().setText(getFilenameWithStar());
        } else if (View.DUMMY_DATFILE.equals(fileNameObj)) {
            getTab().getTextComposite().setText(""); //$NON-NLS-1$
            getTab().setText(filename);
        } else {

            UTF8BufferedReader reader = null;
            try {
                StringBuilder sb = new StringBuilder();
                reader = new UTF8BufferedReader(fileNameObj.getOldName());
                String line = reader.readLine();
                if (line != null) {
                    sb.append(line);
                    while (true) {
                        String line2 = reader.readLine();
                        if (line2 == null) {
                            break;
                        }
                        sb.append(StringHelper.getLineDelimiter());
                        sb.append(line2);
                    }
                }
                String originalText = sb.toString();

                fileNameObj.setOriginalText(originalText);
                fileNameObj.parseForData(true);
                this.datFileObj.getVertexManager().setVertexToReplace(null);
                this.datFileObj.getVertexManager().setModified_NoSync();
                getTab().getTextComposite().setText(originalText);
                datFileObj.setText(getTab().getTextComposite().getText());
                getTab().setText(filename);
                datFileObj.setLastModified(new File(datFileObj.getOldName()).lastModified());
            } catch (FileNotFoundException e) {
                Object[] messageArguments = {fileNameObj.getOldName()};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.LOCALE);
                formatter.applyPattern(I18n.COMPOSITETAB_FileNotFound);
                getTab().getTextComposite().setText(formatter.format(messageArguments));
                datFileObj.setText(""); //$NON-NLS-1$
                datFileObj.setOriginalText(""); //$NON-NLS-1$
                getTab().setText(getFilenameWithStar());
                getTab().getTextComposite().setEditable(false);
            } catch (LDParsingException e) {
                Object[] messageArguments = {fileNameObj.getOldName()};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.LOCALE);
                formatter.applyPattern(I18n.COMPOSITETAB_FileReadError);
                getTab().getTextComposite().setText(formatter.format(messageArguments));
                datFileObj.setText(""); //$NON-NLS-1$
                datFileObj.setOriginalText(""); //$NON-NLS-1$
                getTab().getTextComposite().setEditable(false);
                NLogger.error(getClass(), e);
            } catch (UnsupportedEncodingException e) {
                Object[] messageArguments = {fileNameObj.getOldName()};
                MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                formatter.setLocale(MyLanguage.LOCALE);
                formatter.applyPattern(I18n.COMPOSITETAB_FileEncodingError);
                getTab().getTextComposite().setText(formatter.format(messageArguments));
                datFileObj.setText(""); //$NON-NLS-1$
                datFileObj.setOriginalText(""); //$NON-NLS-1$
                getTab().getTextComposite().setEditable(false);
            } finally {
                try {
                    if (reader != null)
                        reader.close();
                } catch (LDParsingException e1) {
                }
            }
        }
        getTab().getTextComposite().forceFocus();
    }

    public boolean isReplacingVertex() {
        return replacingVertex;
    }

    public void setReplacingVertex(boolean replacingVertex) {
        this.replacingVertex = replacingVertex;
    }

    public float getReplaceEpsilon() {
        return replaceEpsilon;
    }

    public void setReplaceEpsilon(float replaceEpsilon) {
        this.replaceEpsilon = replaceEpsilon;
    }

    public BigDecimal getToReplaceX() {
        return toReplaceX;
    }

    public void setToReplaceX(BigDecimal toReplaceX) {
        this.toReplaceX = toReplaceX;
    }

    public BigDecimal getToReplaceY() {
        return toReplaceY;
    }

    public void setToReplaceY(BigDecimal toReplaceY) {
        this.toReplaceY = toReplaceY;
    }

    public BigDecimal getToReplaceZ() {
        return toReplaceZ;
    }

    public void setToReplaceZ(BigDecimal toReplaceZ) {
        this.toReplaceZ = toReplaceZ;
    }

    public CompositeTab getTab() {
        return tab;
    }

    public void setTab(CompositeTab tab) {
        this.tab = tab;
    }

    public String getFilenameWithStar() {
        return filenameWithStar;
    }

    public void setFilenameWithStar(String filenameWithStar) {
        this.filenameWithStar = filenameWithStar;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public int getOldLineIndex() {
        return oldLineIndex;
    }

    public void setOldLineIndex(int oldLineIndex) {
        this.oldLineIndex = oldLineIndex;
    }

    public ApplicationWindow getWindow() {
        return this.window[0];
    }

    public boolean isDoingPaste() {
        return doingPaste;
    }

    public void setDoingPaste(boolean doingPaste) {
        this.doingPaste = doingPaste;
    }
}

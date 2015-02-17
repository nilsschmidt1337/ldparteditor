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

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.StringHelper;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;

/**
 * @author nils
 *
 */
public class CompositeTabState {

    /** The current {@linkplain EditorTextWindow} of the composite tab */
    final EditorTextWindow[] window = new EditorTextWindow[1];
    /** The DatFile object */
    private DatFile datFileObj;
    /** The filename of the file, which is displayed by this tab */
    String filename = "(new file)"; //$NON-NLS-1$ I18N Needs translation?
    /**
     * The filename of the file, which is displayed by this tab when the content
     * is modified
     */
    String filenameWithStar = "(new file)*"; //$NON-NLS-1$ I18N Needs translation?
    /** The line index of the caret [NOT PUBLIC YET] */
    int currentLineIndex = 0;
    /** The caret position (needed for vertex replacement and so on) */
    int currentCaretPositionLine = 0;
    int currentCaretPositionChar = 0;
    int currentCaretTopIndex = 0;
    /** The tab reference */
    CompositeTab tab;

    private boolean replacingVertex = false;
    private float replaceEpsilon = 0.0001f;
    private BigDecimal toReplaceX = BigDecimal.ONE;
    private BigDecimal toReplaceY = new BigDecimal(2);
    private BigDecimal toReplaceZ = new BigDecimal(3);

    public DatFile getFileNameObj() {
        return datFileObj;
    }

    public void setFileNameObj(DatFile fileNameObj) {
        filename = new File(fileNameObj.getNewName()).getName();
        filenameWithStar = filename + "*"; //$NON-NLS-1$
        if (fileNameObj.isReadOnly()) {
            filename = filename + " (read-only)"; //$NON-NLS-1$ I18N Needs translation?
            filenameWithStar = filenameWithStar + " (read-only)"; //$NON-NLS-1$ I18N Needs translation?
            tab.getTextComposite().setEditable(false);
        }
        this.datFileObj = fileNameObj;
        if (Project.getUnsavedFiles().contains(fileNameObj) || !fileNameObj.getOldName().equals(fileNameObj.getNewName())) {
            NLogger.debug(getClass(), filenameWithStar);
            this.datFileObj.getVertexManager().setModified(true);
            tab.getTextComposite().setText(this.datFileObj.getText());
            tab.setText(filenameWithStar);
        } else {

            try {
                StringBuilder sb = new StringBuilder();
                UTF8BufferedReader reader = new UTF8BufferedReader(fileNameObj.getOldName());
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
                if (datFileObj.getDrawChainTail() != null) {
                    this.datFileObj.getVertexManager().setVertexToReplace(null);
                    this.datFileObj.getVertexManager().setModified(true);
                }
                tab.getTextComposite().setText(originalText);
                datFileObj.setText(tab.getTextComposite().getText());
                tab.setText(filename);
                datFileObj.setLastModified(new File(datFileObj.getOldName()).lastModified());
            } catch (FileNotFoundException e) {
                tab.getTextComposite().setText("The file was not found: " + fileNameObj.getOldName()); //$NON-NLS-1$ I18N Needs translation!
                datFileObj.setText(""); //$NON-NLS-1$
                datFileObj.setOriginalText(""); //$NON-NLS-1$
                tab.setText(filenameWithStar);
                tab.getTextComposite().setEditable(false);
            } catch (LDParsingException e) {
                tab.getTextComposite().setText("The file cannot be read: " + fileNameObj.getOldName()); //$NON-NLS-1$ I18N Needs translation!
                datFileObj.setText(""); //$NON-NLS-1$
                datFileObj.setOriginalText(""); //$NON-NLS-1$
                tab.getTextComposite().setEditable(false);
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                tab.getTextComposite().setText("The file has no UTF-8 encoding: " + fileNameObj.getOldName()); //$NON-NLS-1$ I18N Needs translation!
                datFileObj.setText(""); //$NON-NLS-1$
                datFileObj.setOriginalText(""); //$NON-NLS-1$
                tab.getTextComposite().setEditable(false);
            }
        }
        tab.getTextComposite().forceFocus();
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

}

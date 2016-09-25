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
package org.nschmidt.ldparteditor.text;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.wb.swt.SWTResourceManager;
import org.lwjgl.util.vector.Matrix3f;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.enums.Colour;
import org.nschmidt.ldparteditor.enums.DatKeyword;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Provides functions to format a line of text according to its contents Note:
 * This is not the parser. The parser will be triggered by the dat-file save
 * event!
 *
 * @author nils
 *
 */
public class SyntaxFormatter {

    private StyledText compositeText;

    private final Vector3d start = new Vector3d();
    private final Vector3d end = new Vector3d();
    private final Vector3d vertexA = new Vector3d();
    private final Vector3d vertexB = new Vector3d();
    private final Vector3d vertexC = new Vector3d();
    private final Vector3d vertexD = new Vector3d();
    private final Vector3d vertexA2 = new Vector3d();
    private final Vector3d vertexB2 = new Vector3d();
    private final Vector3d vertexC2 = new Vector3d();
    private final Vector3d vertexD2 = new Vector3d();
    private final Vector3d controlI = new Vector3d();
    private final Vector3d controlII = new Vector3d();

    public SyntaxFormatter(StyledText compositeText) {
        this.compositeText = compositeText;
    }

    /**
     * Formats the updated line
     *
     * @param e
     *            the triggered line style event data
     * @param replaceVertex
     * @param vz
     * @param vy
     * @param vx
     * @param replaceEpsilon
     * @param isSelected
     */
    public void format(LineStyleEvent e,
            BigDecimal VX, BigDecimal VY, BigDecimal VZ,
            float replaceEpsilon, boolean replaceVertex, boolean isSelected, boolean isDuplicate,
            DatFile df) {
        
        ArrayList<StyleRange> styles = new ArrayList<StyleRange>();
        
        if (isDuplicate) {
            StyleRange errStyleRange = new StyleRange();
            errStyleRange.start = e.lineOffset;
            errStyleRange.length = e.lineText.length();
            errStyleRange.underline = true;
            errStyleRange.underlineColor = Colour.line_error_underline[0];
            errStyleRange.underlineStyle = SWT.UNDERLINE_ERROR;
            styles.add(errStyleRange);
            e.styles = styles.toArray(new StyleRange[0]);
            return;
        }

        float vx = VX.floatValue();
        float vy = VY.floatValue();
        float vz = VZ.floatValue();
        
        String[] text_segments = e.lineText.split(" "); //$NON-NLS-1$

        // Get the linetype
        int linetype = 0;
        for (String segment : text_segments) {
            if (segment.isEmpty()) {
                continue;
            } else {
                try {
                    linetype = Integer.parseInt(segment);
                    break;
                } catch (NumberFormatException nfe) {
                    StyleRange errStyleRange = new StyleRange();
                    errStyleRange.start = e.lineOffset;
                    errStyleRange.length = e.lineText.length();
                    errStyleRange.underline = true;
                    errStyleRange.underlineColor = Colour.line_error_underline[0];
                    errStyleRange.underlineStyle = SWT.UNDERLINE_ERROR;
                    styles.add(errStyleRange);
                    e.styles = styles.toArray(new StyleRange[0]);
                    return;
                }
            }
        }
        // Format the line according to its type
        switch (linetype) {
        case 0:
            formatComment(styles, e, text_segments, vx, vy, vz, replaceVertex, replaceEpsilon);
            break;
        case 1:
            formatReference(styles, e, text_segments, df);
            break;
        case 2:
            formatLine(styles, e, text_segments, vx, vy, vz, replaceVertex, replaceEpsilon);
            break;
        case 3:
            formatTriangle(styles, e, text_segments, vx, vy, vz, replaceVertex, replaceEpsilon);
            break;
        case 4:
            formatQuad(styles, e, text_segments, vx, vy, vz, replaceVertex, replaceEpsilon);
            break;
        case 5:
            formatCondline(styles, e, text_segments, vx, vy, vz, replaceVertex, replaceEpsilon);
            break;
        default:
            // Mark unknown linetypes as error
            StyleRange errStyleRange = new StyleRange();
            errStyleRange.start = e.lineOffset;
            errStyleRange.length = e.lineText.length();
            errStyleRange.underline = true;
            errStyleRange.underlineColor = Colour.line_error_underline[0];
            errStyleRange.underlineStyle = SWT.UNDERLINE_ERROR;
            styles.add(errStyleRange);
            break;
        }
        if (isSelected) {
            for (StyleRange sr : styles) {
                if (sr.background == null) {
                    sr.background = Colour.line_highlight_selected_background[0];
                } else if (replaceVertex) {
                    sr.fontStyle = SWT.BOLD;
                }
            }
        } else if (replaceVertex) {
            for (StyleRange sr : styles) {
                sr.fontStyle = SWT.BOLD;
            }
        }
        e.styles = styles.toArray(new StyleRange[0]);
    }

    /**
     * Extends the style range of the text with a error-underline-style.
     *
     * @param range
     *            the reference to the style range
     */
    private void setErrorStyle(StyleRange range) {
        range.underline = true;
        range.underlineStyle = SWT.UNDERLINE_ERROR;
        range.underlineColor = Colour.line_error_underline[0];
        range.length = range.length - 1;
    }

    /**
     * Extends the style range of the text with a boxing-style.
     *
     * @param range
     *            the reference to the style range
     */
    private void setBorderStyle(StyleRange range) {
        range.borderStyle = SWT.BORDER_SOLID;
        range.borderColor = Colour.line_box_font[0];
        range.length = range.length;
    }

    /**
     * Extends the style range of the text with a warning-underline-style.
     *
     * @param range
     *            the reference to the style range
     */
    private void setWarningStyle(StyleRange range) {
        range.underline = true;
        range.underlineStyle = SWT.UNDERLINE_SQUIGGLE;
        range.underlineColor = Colour.line_warning_underline[0];
        range.length = range.length - 1;
    }

    /**
     * Validates the colour argument and highlights possible errors
     *
     * @param range
     *            the corresponding style range of the colour number
     * @param arg
     *            the argument data
     */
    private void validateColour(StyleRange range, String arg) {
        int colourValue;
        GColour colour;
        try {
            colourValue = Integer.parseInt(arg);
            if (colourValue < 0) {
                setErrorStyle(range);
            } else if (!View.hasLDConfigColour(colourValue)) {
                setErrorStyle(range);
            } else {
                colour = View.getLDConfigColour(colourValue);
                final int r = (int) (colour.getR() * 255);
                final int g = (int) (colour.getG() * 255);
                final int b = (int) (colour.getB() * 255);
                range.background = SWTResourceManager.getColor(r, g, b);
                final int s = ((r + g + b) / 3 + 128) % 255;
                range.foreground = SWTResourceManager.getColor(s, s, s);
                range.borderColor = SWTResourceManager.getColor(0, 0, 0);
                range.borderStyle = SWT.BORDER_SOLID;
                range.length = range.length - 1;
            }
        } catch (NumberFormatException nfe) {
            if (arg.length() == 9 && arg.substring(0, 3).equals("0x2")) { //$NON-NLS-1$
                try {
                    final int r = Integer.parseInt(arg.substring(3, 5), 16);
                    final int g = Integer.parseInt(arg.substring(5, 7), 16);
                    final int b = Integer.parseInt(arg.substring(7, 9), 16);
                    range.background = SWTResourceManager.getColor(r, g, b);
                    final int s = ((r + g + b) / 3 + 128) % 255;
                    range.foreground = SWTResourceManager.getColor(s, s, s);
                    range.borderColor = SWTResourceManager.getColor(0, 0, 0);
                    range.borderStyle = SWT.BORDER_SOLID;
                    range.length = range.length - 1;
                } catch (NumberFormatException nfe2) {
                    setErrorStyle(range);
                }
            } else {
                setErrorStyle(range);
            }
        }
    }

    /**
     * Formats comment lines Note: No error parsing will be done here
     *
     * @param styles
     *            the list of {@linkplain StyleRange}s from the text line
     * @param e
     *            the {@linkplain LineStyleEvent}
     * @param replaceVertex
     * @param vz
     * @param vy
     * @param vx
     * @param replaceEpsilon
     */
    private void formatComment(ArrayList<StyleRange> styles, LineStyleEvent e, String[] text_segments, float vx, float vy, float vz, boolean replaceVertex, float replaceEpsilon) {
        // All work done here..
        int offset = e.lineOffset;

        StyleRange commentStyleRange = new StyleRange();
        commentStyleRange.start = offset;
        commentStyleRange.length = e.lineText.length();
        commentStyleRange.foreground = Colour.line_comment_font[0];
        styles.add(commentStyleRange);

        if (offset == 0)
            return; // We are on the first line. Do not highlight other KEYWORDS in these line

        for (String segment : text_segments) {
            if (segment.isEmpty() || segment.equals("0")) { //$NON-NLS-1$
                offset++;
                continue;
            } else if (segment.equals("//") || segment.equals("Author:")) { //$NON-NLS-1$ //$NON-NLS-2$
                return; // We got a real comment or a author entry here. Do not
                // highlight other KEYWORDS in these lines
            } else if (segment.equals("!HISTORY") || segment.equals("!HELP")) { //$NON-NLS-1$ //$NON-NLS-2$
                // We got a history/help entry here. Do not highlight other
                // KEYWORDS in these lines
                StyleRange metaStyleRange = new StyleRange();
                metaStyleRange.start = offset;
                metaStyleRange.length = segment.length();
                metaStyleRange.fontStyle = SWT.BOLD | SWT.ITALIC;
                metaStyleRange.foreground = Colour.line_comment_font[0];
                styles.add(metaStyleRange);
                return;
            } else {
                break;
            }
        }

        // We got a header entry or meta command here..
        if (replaceVertex) {
            int checkForVertexCounter = 0;
            float tx = 0f;
            float ty = 0f;
            float tz = 0f;
            offset = e.lineOffset;
            for (String segment : text_segments) {
                if (segment.equals("")) { //$NON-NLS-1$
                    offset += 1;
                } else {
                    int segLength = segment.length();
                    if (DatKeyword.getKeywords().contains(segment)) {
                        StyleRange metaStyleRange = new StyleRange();
                        metaStyleRange.start = offset;
                        metaStyleRange.length = segment.length();
                        metaStyleRange.fontStyle = SWT.BOLD | SWT.ITALIC;
                        metaStyleRange.foreground = Colour.line_comment_font[0];
                        switch (checkForVertexCounter) {
                        case 0:
                            if (segment.equals("!LPE"))checkForVertexCounter++; //$NON-NLS-1$
                            break;
                        case 1:
                            if (segment.equals("VERTEX"))checkForVertexCounter++; //$NON-NLS-1$
                            break;
                        }
                        styles.add(metaStyleRange);
                    } else {
                        StyleRange metaStyleRange = new StyleRange();
                        metaStyleRange.start = offset;
                        metaStyleRange.length = segment.length();
                        metaStyleRange.foreground = Colour.line_comment_font[0];
                        switch (checkForVertexCounter) {
                        case 0:
                            if (segment.equals("!LPE"))checkForVertexCounter++; //$NON-NLS-1$
                            break;
                        case 1:
                            if (segment.equals("VERTEX"))checkForVertexCounter++; //$NON-NLS-1$
                            break;
                        case 2:
                            try {
                                tx = Float.parseFloat(segment);
                                checkForVertexCounter++;
                            } catch (NumberFormatException nfe) {
                            }
                            break;
                        case 3:
                            try {
                                ty = Float.parseFloat(segment);
                                checkForVertexCounter++;
                            } catch (NumberFormatException nfe) {
                            }
                            break;
                        case 4:
                            try {
                                tz = Float.parseFloat(segment);
                                checkForVertexCounter++;
                            } catch (NumberFormatException nfe) {
                            }
                            break;
                        default:
                            break;
                        }
                        styles.add(metaStyleRange);
                    }
                    offset += segLength + 1;
                }
            }
            if (checkForVertexCounter == 5 && Math.abs(vx - tx) < replaceEpsilon && Math.abs(vy - ty) < replaceEpsilon && Math.abs(vz - tz) < replaceEpsilon) {
                setBorderStyle(styles.get(4));
                setBorderStyle(styles.get(5));
                setBorderStyle(styles.get(6));
            }
        } else {
            offset = e.lineOffset;
            for (String segment : text_segments) {
                if (segment.equals("")) { //$NON-NLS-1$
                    offset += 1;
                } else {
                    int segLength = segment.length();
                    if (DatKeyword.getKeywords().contains(segment)) {
                        StyleRange metaStyleRange = new StyleRange();
                        metaStyleRange.start = offset;
                        metaStyleRange.length = segment.length();
                        metaStyleRange.fontStyle = SWT.BOLD | SWT.ITALIC;
                        metaStyleRange.foreground = Colour.line_comment_font[0];
                        styles.add(metaStyleRange);
                    }
                    offset += segLength + 1;
                }
            }
        }
    }

    /**
     * Formats reference lines and highlights syntax errors
     *
     * @param styles
     *            the list of {@linkplain StyleRange}s from the text line
     * @param e
     *            the {@linkplain LineStyleEvent}
     */
    private void formatReference(ArrayList<StyleRange> styles, LineStyleEvent e, String[] text_segments, DatFile datFile) {
        int offset = e.lineOffset;

        StyleRange lineStyleRange = new StyleRange();
        lineStyleRange.start = offset;
        lineStyleRange.length = e.lineText.length();
        lineStyleRange.foreground = Colour.line_primary_font[0];
        styles.add(lineStyleRange);

        boolean parseError = false;
        int segment_number = 0;
        for (int i = 0; i < text_segments.length; i++) {
            String segment = text_segments[i];
            if (segment.isEmpty()) {
                offset++;
                continue;
            } else {
                int segmentLength = segment.length();
                StyleRange segmentStyleRange = new StyleRange();
                segmentStyleRange.start = offset;
                segmentStyleRange.length = segmentLength;
                switch (segment_number) {
                case 0: // type
                    segmentStyleRange.foreground = compositeText.getForeground();
                    break;
                case 1: // colour
                    segmentStyleRange.foreground = Colour.line_colourAttr_font[0];
                    break;
                case 2: // *fall through*
                case 3:
                case 4: // x,y,z offset
                    segmentStyleRange.foreground = Colour.line_secondary_font[0];
                    break;
                case 5: // *fall through*
                case 6:
                case 7: // 1st matrix row
                    segmentStyleRange.foreground = Colour.line_primary_font[0];
                    break;
                case 8: // *fall through*
                case 9:
                case 10: // 2nd matrix row
                    segmentStyleRange.foreground = Colour.line_secondary_font[0];
                    break;
                case 11: // *fall through*
                case 12:
                case 13: // 3rd matrix row
                    segmentStyleRange.foreground = Colour.line_primary_font[0];
                    break;
                default: // dat file reference
                    segmentStyleRange.foreground = compositeText.getForeground();
                    segmentStyleRange.fontStyle = SWT.BOLD;
                    break;
                }
                styles.add(segmentStyleRange);
                offset += segmentLength + 1;
                segment_number++;
            }
        }
        String[] data_segments = e.lineText.replaceAll("\\s+", " ").trim().split(" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // [ERROR] Check less argument count
        if (data_segments.length < 15) {
            parseError = true;
        } else {
            // [ERROR] Check colour
            validateColour(styles.get(2), data_segments[1]);
            // [ERROR] Check singularity
            Matrix3f tMatrix = new Matrix3f();
            while (true) {
                boolean numberError = false;
                // Offset
                try {
                    Float.parseFloat(data_segments[2]);
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(3));
                    numberError = true;
                }
                try {
                    Float.parseFloat(data_segments[3]);
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(4));
                    numberError = true;
                }
                try {
                    Float.parseFloat(data_segments[4]);
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(5));
                    numberError = true;
                }
                // First row
                try {
                    tMatrix.m00 = Float.parseFloat(data_segments[5]);
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(6));
                    numberError = true;
                }
                try {
                    tMatrix.m01 = Float.parseFloat(data_segments[6]);
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(7));
                    numberError = true;
                }
                try {
                    tMatrix.m02 = Float.parseFloat(data_segments[7]);
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(8));
                    numberError = true;
                }
                // Second row
                try {
                    tMatrix.m10 = Float.parseFloat(data_segments[8]);
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(9));
                    numberError = true;
                }
                try {
                    tMatrix.m11 = Float.parseFloat(data_segments[9]);
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(10));
                    numberError = true;
                }
                try {
                    tMatrix.m12 = Float.parseFloat(data_segments[10]);
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(11));
                    numberError = true;
                }
                // Third row
                try {
                    tMatrix.m20 = Float.parseFloat(data_segments[11]);
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(12));
                    numberError = true;
                }
                try {
                    tMatrix.m21 = Float.parseFloat(data_segments[12]);
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(13));
                    numberError = true;
                }
                try {
                    tMatrix.m22 = Float.parseFloat(data_segments[13]);
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(14));
                    numberError = true;
                }
                if (numberError)
                    break;
                float det = Math.abs(tMatrix.determinant());
                parseError = det < Threshold.singularity_determinant;
                break;
            }
            // [WARNING] Check file existance
            boolean fileExists = true;
            StringBuilder sb = new StringBuilder();
            for (int s = 14; s < data_segments.length - 1; s++) {
                sb.append(data_segments[s]);
                sb.append(" "); //$NON-NLS-1$
            }
            sb.append(data_segments[data_segments.length - 1]);
            String shortFilename = sb.toString();
            boolean isLowercase = shortFilename.equals(shortFilename.toLowerCase(Locale.ENGLISH));
            shortFilename = shortFilename.toLowerCase(Locale.ENGLISH);
            try {
                shortFilename = shortFilename.replaceAll("s\\\\", "S" + File.separator).replaceAll("\\\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } catch (Exception ex) {
                // Workaround for windows OS / JVM BUG
                shortFilename = shortFilename.replace("s\\", "S" + File.separator).replace("\\", File.separator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            String shortFilename2 = shortFilename.startsWith("S" + File.separator) ? "s" + shortFilename.substring(1) : shortFilename; //$NON-NLS-1$ //$NON-NLS-2$
            String shortFilename3 = shortFilename.startsWith("S" + File.separator) ? shortFilename.substring(2) : shortFilename; //$NON-NLS-1$
            String[] prefix ;
            if (datFile != null && !datFile.isProjectFile() && !View.DUMMY_DATFILE.equals(datFile)) {
                File dff = new File(datFile.getOldName()).getParentFile();
                if (dff != null && dff.exists() && dff.isDirectory()) {
                    prefix = new String[]{dff.getAbsolutePath(), Project.getProjectPath(), WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), WorkbenchManager.getUserSettingState().getLdrawFolderPath()};
                } else {
                    prefix = new String[]{Project.getProjectPath(), WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), WorkbenchManager.getUserSettingState().getLdrawFolderPath()};
                }
            } else {
                prefix = new String[]{Project.getProjectPath(), WorkbenchManager.getUserSettingState().getUnofficialFolderPath(), WorkbenchManager.getUserSettingState().getLdrawFolderPath()};
            }
            String[] middle = new String[]{File.separator + "PARTS", File.separator + "parts", File.separator + "P", File.separator + "p"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            String[] suffix = new String[]{File.separator + shortFilename, File.separator + shortFilename2, File.separator + shortFilename3};
            for (int a1 = 0; a1 < prefix.length; a1++) {
                String s1 = prefix[a1];
                for (int a2 = 0; a2 < middle.length; a2++) {
                    String s2 = middle[a2];
                    for (int a3 = 0; a3 < suffix.length; a3++) {
                        String s3 = suffix[a3];
                        File f = new File(s1 + s2 + s3);
                        fileExists = f.exists() && f.isFile();
                        if (fileExists) break;
                    }
                    if (fileExists) break;
                }
                if (fileExists) break;
            }
            if (!fileExists) {
                for (DatFile df : Project.getUnsavedFiles()) {
                    String fn = df.getNewName();
                    for (int a1 = 0; a1 < prefix.length; a1++) {
                        String s1 = prefix[a1];
                        for (int a2 = 0; a2 < middle.length; a2++) {
                            String s2 = middle[a2];
                            for (int a3 = 0; a3 < suffix.length; a3++) {
                                String s3 = suffix[a3];
                                if (fn.equals(s1 + s2 + s3)) {
                                    fileExists = true;
                                    break;
                                }
                            }
                            if (fileExists) break;
                        }
                        if (fileExists) break;
                    }
                    if (fileExists) break;
                }
            }
            // [WARNING] Check spaces in dat file name
            if (!fileExists || data_segments.length > 15 || !isLowercase) {
                for (int s = 15; s < styles.size(); s++) {
                    setWarningStyle(styles.get(s));
                }
            }
        }
        if (parseError) {
            for (StyleRange style : styles) {
                style.underline = true;
                style.underlineStyle = SWT.UNDERLINE_ERROR;
                style.underlineColor = Colour.line_error_underline[0];
            }
        }
    }

    /**
     * Formats "line lines" and highlights syntax errors
     *
     * @param styles
     *            the list of {@linkplain StyleRange}s from the text line
     * @param e
     *            the {@linkplain LineStyleEvent}
     * @param replaceVertex
     * @param vz
     * @param vy
     * @param vx
     * @param replaceEpsilon
     */
    private void formatLine(ArrayList<StyleRange> styles, LineStyleEvent e, String[] text_segments, float vx, float vy, float vz, boolean replaceVertex, float replaceEpsilon) {
        // All work done here..
        int offset = e.lineOffset;

        StyleRange lineStyleRange = new StyleRange();
        lineStyleRange.start = offset;
        lineStyleRange.length = e.lineText.length();
        lineStyleRange.foreground = Colour.line_primary_font[0];
        styles.add(lineStyleRange);

        boolean parseError = false;
        int segment_number = 0;
        for (int i = 0; i < text_segments.length; i++) {
            String segment = text_segments[i];
            if (segment.isEmpty()) {
                offset++;
                continue;
            } else {
                int segmentLength = segment.length();
                StyleRange segmentStyleRange = new StyleRange();
                segmentStyleRange.start = offset;
                segmentStyleRange.length = segmentLength;
                switch (segment_number) {
                case 0: // type
                    segmentStyleRange.foreground = compositeText.getForeground();
                    break;
                case 1: // colour
                    segmentStyleRange.foreground = Colour.line_colourAttr_font[0];
                    break;
                case 2: // *fall through*
                case 3:
                case 4: // start vertex
                    segmentStyleRange.foreground = Colour.line_secondary_font[0];
                    break;
                case 5: // *fall through*
                case 6:
                case 7: // end vertex
                    segmentStyleRange.foreground = Colour.line_primary_font[0];
                    break;
                default: // error
                    break;
                }
                styles.add(segmentStyleRange);
                offset += segmentLength + 1;
                segment_number++;
            }
        }
        String[] data_segments = e.lineText.replaceAll("\\s+", " ").trim().split(" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // [ERROR] Check argument count
        if (data_segments.length != 8) {
            parseError = true;
        } else {
            // [ERROR] Check colour
            validateColour(styles.get(2), data_segments[1]);
            // [ERROR] Check identical vertices
            while (true) {
                boolean numberError = false;
                // Start vertex
                try {
                    start.setX(new BigDecimal(data_segments[2], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(3));
                    numberError = true;
                }
                try {
                    start.setY(new BigDecimal(data_segments[3], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(4));
                    numberError = true;
                }
                try {
                    start.setZ(new BigDecimal(data_segments[4], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(5));
                    numberError = true;
                }
                // End vertex
                try {
                    end.setX(new BigDecimal(data_segments[5], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(6));
                    numberError = true;
                }
                try {
                    end.setY(new BigDecimal(data_segments[6], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(7));
                    numberError = true;
                }
                try {
                    end.setZ(new BigDecimal(data_segments[7], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(8));
                    numberError = true;
                }
                if (numberError)
                    break;
                if (replaceVertex) {
                    if (Math.abs(vx - start.X.floatValue()) < replaceEpsilon && Math.abs(vy - start.Y.floatValue()) < replaceEpsilon && Math.abs(vz - start.Z.floatValue()) < replaceEpsilon) {
                        setBorderStyle(styles.get(3));
                        setBorderStyle(styles.get(4));
                        setBorderStyle(styles.get(5));
                    }
                    if (Math.abs(vx - end.X.floatValue()) < replaceEpsilon && Math.abs(vy - end.Y.floatValue()) < replaceEpsilon && Math.abs(vz - end.Z.floatValue()) < replaceEpsilon) {
                        setBorderStyle(styles.get(6));
                        setBorderStyle(styles.get(7));
                        setBorderStyle(styles.get(8));
                    }
                }
                parseError = Vector3d.sub(start, end).length().compareTo(Threshold.identical_vertex_distance) < 0;
                break;
            }

        }
        if (parseError) {
            for (StyleRange style : styles) {
                style.underline = true;
                style.underlineStyle = SWT.UNDERLINE_ERROR;
                style.underlineColor = Colour.line_error_underline[0];
            }
        }
    }

    /**
     * Formats triangle lines and highlights syntax errors
     *
     * @param styles
     *            the list of {@linkplain StyleRange}s from the text line
     * @param e
     *            the {@linkplain LineStyleEvent}
     * @param replaceVertex
     * @param vz
     * @param vy
     * @param vx
     * @param replaceEpsilon
     */
    private void formatTriangle(ArrayList<StyleRange> styles, LineStyleEvent e, String[] text_segments, float vx, float vy, float vz, boolean replaceVertex, float replaceEpsilon) {
        // All work done here..
        int offset = e.lineOffset;

        StyleRange lineStyleRange = new StyleRange();
        lineStyleRange.start = offset;
        lineStyleRange.length = e.lineText.length();
        lineStyleRange.foreground = Colour.line_primary_font[0];
        styles.add(lineStyleRange);

        boolean parseError = false;
        int segment_number = 0;
        for (int i = 0; i < text_segments.length; i++) {
            String segment = text_segments[i];
            if (segment.isEmpty()) {
                offset++;
                continue;
            } else {
                int segmentLength = segment.length();
                StyleRange segmentStyleRange = new StyleRange();
                segmentStyleRange.start = offset;
                segmentStyleRange.length = segmentLength;
                switch (segment_number) {
                case 0: // type
                    segmentStyleRange.foreground = compositeText.getForeground();
                    break;
                case 1: // colour
                    segmentStyleRange.foreground = Colour.line_colourAttr_font[0];
                    break;
                case 2: // *fall through*
                case 3:
                case 4: // 1st vertex
                    segmentStyleRange.foreground = Colour.line_secondary_font[0];
                    break;
                case 5: // *fall through*
                case 6:
                case 7: // 2nd vertex
                    segmentStyleRange.foreground = Colour.line_primary_font[0];
                    break;
                case 8: // *fall through*
                case 9:
                case 10: // 3rd vertex
                    segmentStyleRange.foreground = Colour.line_secondary_font[0];
                    break;
                default: // error
                    break;
                }
                styles.add(segmentStyleRange);
                offset += segmentLength + 1;
                segment_number++;
            }
        }
        String[] data_segments = e.lineText.replaceAll("\\s+", " ").trim().split(" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // [ERROR] Check argument count
        if (data_segments.length != 11) {
            parseError = true;
        } else {
            // [ERROR] Check colour
            validateColour(styles.get(2), data_segments[1]);
            // [ERROR] Check identical vertices
            while (true) {
                boolean numberError = false;
                // 1st vertex
                try {
                    vertexA.setX(new BigDecimal(data_segments[2], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(3));
                    numberError = true;
                }
                try {
                    vertexA.setY(new BigDecimal(data_segments[3], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(4));
                    numberError = true;
                }
                try {
                    vertexA.setZ(new BigDecimal(data_segments[4], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(5));
                    numberError = true;
                }
                // 2nd vertex
                try {
                    vertexB.setX(new BigDecimal(data_segments[5], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(6));
                    numberError = true;
                }
                try {
                    vertexB.setY(new BigDecimal(data_segments[6], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(7));
                    numberError = true;
                }
                try {
                    vertexB.setZ(new BigDecimal(data_segments[7], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(8));
                    numberError = true;
                }
                // 3rd vertex
                try {
                    vertexC.setX(new BigDecimal(data_segments[8], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(9));
                    numberError = true;
                }
                try {
                    vertexC.setY(new BigDecimal(data_segments[9], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(10));
                    numberError = true;
                }
                try {
                    vertexC.setZ(new BigDecimal(data_segments[10], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(11));
                    numberError = true;
                }
                if (numberError)
                    break;
                if (replaceVertex) {
                    if (Math.abs(vx - vertexA.X.floatValue()) < replaceEpsilon && Math.abs(vy - vertexA.Y.floatValue()) < replaceEpsilon && Math.abs(vz - vertexA.Z.floatValue()) < replaceEpsilon) {
                        setBorderStyle(styles.get(3));
                        setBorderStyle(styles.get(4));
                        setBorderStyle(styles.get(5));
                    }
                    if (Math.abs(vx - vertexB.X.floatValue()) < replaceEpsilon && Math.abs(vy - vertexB.Y.floatValue()) < replaceEpsilon && Math.abs(vz - vertexB.Z.floatValue()) < replaceEpsilon) {
                        setBorderStyle(styles.get(6));
                        setBorderStyle(styles.get(7));
                        setBorderStyle(styles.get(8));
                    }
                    if (Math.abs(vx - vertexC.X.floatValue()) < replaceEpsilon && Math.abs(vy - vertexC.Y.floatValue()) < replaceEpsilon && Math.abs(vz - vertexC.Z.floatValue()) < replaceEpsilon) {
                        setBorderStyle(styles.get(9));
                        setBorderStyle(styles.get(10));
                        setBorderStyle(styles.get(11));
                    }
                }
                
                Vector3d.sub(vertexA, vertexC, vertexA2);
                Vector3d.sub(vertexB, vertexC, vertexB2);
                Vector3d.sub(vertexB, vertexA, vertexC2);
                
                double angle = Vector3d.angle(vertexA2, vertexB2);
                double sumAngle = angle;
                parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;
                
                if (!parseError) {
                    vertexA2.negate();
                    angle = Vector3d.angle(vertexA2, vertexC2);
                    sumAngle = sumAngle + angle;
                    parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;
                }
                
                if (!parseError) {
                    angle = 180.0 - sumAngle;
                    parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;
                }
                
                parseError = parseError || vertexA2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                parseError = parseError || vertexB2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                parseError = parseError || vertexC2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                break;
            }
        }
        if (parseError) {
            for (StyleRange style : styles) {
                style.underline = true;
                style.underlineStyle = SWT.UNDERLINE_ERROR;
                style.underlineColor = Colour.line_error_underline[0];
            }
        }
    }

    /**
     * Formats quad lines and highlights syntax errors
     *
     * @param styles
     *            the list of {@linkplain StyleRange}s from the text line
     * @param e
     *            the {@linkplain LineStyleEvent}
     * @param replaceVertex
     * @param vz
     * @param vy
     * @param vx
     * @param replaceEpsilon
     */
    private void formatQuad(ArrayList<StyleRange> styles, LineStyleEvent e, String[] text_segments, float vx, float vy, float vz, boolean replaceVertex, float replaceEpsilon) {
        // All work done here..
        int offset = e.lineOffset;

        StyleRange lineStyleRange = new StyleRange();
        lineStyleRange.start = offset;
        lineStyleRange.length = e.lineText.length();
        lineStyleRange.foreground = Colour.line_primary_font[0];
        styles.add(lineStyleRange);

        boolean parseError = false;
        boolean parseWarning = false;
        int segment_number = 0;
        for (int i = 0; i < text_segments.length; i++) {
            String segment = text_segments[i];
            if (segment.isEmpty()) {
                offset++;
                continue;
            } else {
                int segmentLength = segment.length();
                StyleRange segmentStyleRange = new StyleRange();
                segmentStyleRange.start = offset;
                segmentStyleRange.length = segmentLength;
                switch (segment_number) {
                case 0: // type
                    segmentStyleRange.foreground = Colour.line_quad_font[0];
                    break;
                case 1: // colour
                    segmentStyleRange.foreground = Colour.line_colourAttr_font[0];
                    break;
                case 2: // *fall through*
                case 3:
                case 4: // 1st vertex
                    segmentStyleRange.foreground = Colour.line_secondary_font[0];
                    break;
                case 5: // *fall through*
                case 6:
                case 7: // 2nd vertex
                    segmentStyleRange.foreground = Colour.line_primary_font[0];
                    break;
                case 8: // *fall through*
                case 9:
                case 10: // 3rd vertex
                    segmentStyleRange.foreground = Colour.line_secondary_font[0];
                    break;
                case 11: // *fall through*
                case 12:
                case 13: // 4th vertex
                    segmentStyleRange.foreground = Colour.line_primary_font[0];
                    break;
                default: // error
                    break;
                }
                styles.add(segmentStyleRange);
                offset += segmentLength + 1;
                segment_number++;
            }
        }
        String[] data_segments = e.lineText.replaceAll("\\s+", " ").trim().split(" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // [ERROR] Check argument count
        if (data_segments.length != 14) {
            parseError = true;
        } else {
            // [ERROR] Check colour
            validateColour(styles.get(2), data_segments[1]);
            // [ERROR] Check hourglass, concave form, coplanarity & identical
            // vertices
            while (true) {
                boolean numberError = false;
                // 1st vertex
                try {
                    vertexA.setX(new BigDecimal(data_segments[2], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(3));
                    numberError = true;
                }
                try {
                    vertexA.setY(new BigDecimal(data_segments[3], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(4));
                    numberError = true;
                }
                try {
                    vertexA.setZ(new BigDecimal(data_segments[4], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(5));
                    numberError = true;
                }
                // 2nd vertex
                try {
                    vertexB.setX(new BigDecimal(data_segments[5], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(6));
                    numberError = true;
                }
                try {
                    vertexB.setY(new BigDecimal(data_segments[6], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(7));
                    numberError = true;
                }
                try {
                    vertexB.setZ(new BigDecimal(data_segments[7], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(8));
                    numberError = true;
                }
                // 3rd vertex
                try {
                    vertexC.setX(new BigDecimal(data_segments[8], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(9));
                    numberError = true;
                }
                try {
                    vertexC.setY(new BigDecimal(data_segments[9], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(10));
                    numberError = true;
                }
                try {
                    vertexC.setZ(new BigDecimal(data_segments[10], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(11));
                    numberError = true;
                }
                // 4th vertex
                try {
                    vertexD.setX(new BigDecimal(data_segments[11], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(12));
                    numberError = true;
                }
                try {
                    vertexD.setY(new BigDecimal(data_segments[12], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(13));
                    numberError = true;
                }
                try {
                    vertexD.setZ(new BigDecimal(data_segments[13], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(14));
                    numberError = true;
                }
                if (numberError)
                    break;
                if (replaceVertex) {
                    if (Math.abs(vx - vertexA.X.floatValue()) < replaceEpsilon && Math.abs(vy - vertexA.Y.floatValue()) < replaceEpsilon && Math.abs(vz - vertexA.Z.floatValue()) < replaceEpsilon) {
                        setBorderStyle(styles.get(3));
                        setBorderStyle(styles.get(4));
                        setBorderStyle(styles.get(5));
                    }
                    if (Math.abs(vx - vertexB.X.floatValue()) < replaceEpsilon && Math.abs(vy - vertexB.Y.floatValue()) < replaceEpsilon && Math.abs(vz - vertexB.Z.floatValue()) < replaceEpsilon) {
                        setBorderStyle(styles.get(6));
                        setBorderStyle(styles.get(7));
                        setBorderStyle(styles.get(8));
                    }
                    if (Math.abs(vx - vertexC.X.floatValue()) < replaceEpsilon && Math.abs(vy - vertexC.Y.floatValue()) < replaceEpsilon && Math.abs(vz - vertexC.Z.floatValue()) < replaceEpsilon) {
                        setBorderStyle(styles.get(9));
                        setBorderStyle(styles.get(10));
                        setBorderStyle(styles.get(11));
                    }
                    if (Math.abs(vx - vertexD.X.floatValue()) < replaceEpsilon && Math.abs(vy - vertexD.Y.floatValue()) < replaceEpsilon && Math.abs(vz - vertexD.Z.floatValue()) < replaceEpsilon) {
                        setBorderStyle(styles.get(12));
                        setBorderStyle(styles.get(13));
                        setBorderStyle(styles.get(14));
                    }
                }
                Vector3d[] normals = new Vector3d[4];
                float[] normalDirections = new float[4];
                Vector3d[] lineVectors = new Vector3d[4];
                int cnc = 0;
                lineVectors[0] = Vector3d.sub(vertexB, vertexA);
                lineVectors[1] = Vector3d.sub(vertexC, vertexB);
                lineVectors[2] = Vector3d.sub(vertexD, vertexC);
                lineVectors[3] = Vector3d.sub(vertexA, vertexD);
                normals[0] = Vector3d.cross(lineVectors[0], lineVectors[1]);
                normals[1] = Vector3d.cross(lineVectors[1], lineVectors[2]);
                normals[2] = Vector3d.cross(lineVectors[2], lineVectors[3]);
                normals[3] = Vector3d.cross(lineVectors[3], lineVectors[0]);
                for (int i = 0; i < 4; i++) {
                    normalDirections[i] = MathHelper.directionOfVectors(normals[0], normals[i]);
                    if (normalDirections[i] < 0)
                        cnc++;
                }
                parseError = cnc > 0 && cnc < 4;
                // if (cnc == 2) {
                // // Hourglass
                // } else if (cnc == 1 || cnc == 3) {
                // // Concave
                // }
                double angle;
                angle = Vector3d.angle(normals[0], normals[2]);
                // Coplanarity
                parseWarning = angle > Threshold.coplanarity_angle_warning;
                parseError = parseError || angle > Threshold.coplanarity_angle_error;

                Vector3d.sub(vertexB, vertexA, vertexA2);
                Vector3d.sub(vertexB, vertexC, vertexB2);
                Vector3d.sub(vertexD, vertexC, vertexC2);
                Vector3d.sub(vertexD, vertexA, vertexD2);
                
                // Collinearity
                angle = Vector3d.angle(vertexA2, vertexD2);
                double sumAngle = angle;
                parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;
                
                if (!parseError) {
                    angle = Vector3d.angle(vertexB2, vertexC2);
                    sumAngle = sumAngle + angle;
                    parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;
                }
                
                if (!parseError) {
                    vertexA2.negate();
                    vertexB2.negate();
                    angle = Vector3d.angle(vertexA2, vertexB2);
                    sumAngle = sumAngle + angle;
                    parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;
                                                
                }
                
                if (!parseError) {
                    angle = 360.0 - sumAngle;
                    parseError = angle < Threshold.collinear_angle_minimum || angle > Threshold.collinear_angle_maximum;
                }              
                
                parseError = parseError || vertexA2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                parseError = parseError || vertexB2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                parseError = parseError || vertexC2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                parseError = parseError || vertexD2.length().compareTo(Threshold.identical_vertex_distance) < 0;
                break;
            }
        }
        if (parseError) {
            for (StyleRange style : styles) {
                style.underline = true;
                style.underlineStyle = SWT.UNDERLINE_ERROR;
                style.underlineColor = Colour.line_error_underline[0];
            }
        } else {
            if (parseWarning) {
                for (StyleRange style : styles) {
                    style.underline = true;
                    style.underlineStyle = SWT.UNDERLINE_ERROR;
                    style.underlineColor = Colour.line_warning_underline[0];
                }
            }
        }
    }

    /**
     * Formats conditional lines and highlights syntax errors
     *
     * @param styles
     *            the list of {@linkplain StyleRange}s from the text line
     * @param e
     *            the {@linkplain LineStyleEvent}
     * @param replaceVertex
     * @param vz
     * @param vy
     * @param vx
     * @param replaceEpsilon
     */
    private void formatCondline(ArrayList<StyleRange> styles, LineStyleEvent e, String[] text_segments, float vx, float vy, float vz, boolean replaceVertex, float replaceEpsilon) {
        // All work done here..
        int offset = e.lineOffset;

        StyleRange lineStyleRange = new StyleRange();
        lineStyleRange.start = offset;
        lineStyleRange.length = e.lineText.length();
        lineStyleRange.foreground = Colour.line_primary_font[0];
        styles.add(lineStyleRange);

        boolean parseError = false;
        boolean parseWarning = false;
        int segment_number = 0;
        for (int i = 0; i < text_segments.length; i++) {
            String segment = text_segments[i];
            if (segment.isEmpty()) {
                offset++;
                continue;
            } else {
                int segmentLength = segment.length();
                StyleRange segmentStyleRange = new StyleRange();
                segmentStyleRange.start = offset;
                segmentStyleRange.length = segmentLength;
                switch (segment_number) {
                case 0: // type
                    segmentStyleRange.foreground = compositeText.getForeground();
                    break;
                case 1: // colour
                    segmentStyleRange.foreground = Colour.line_colourAttr_font[0];
                    break;
                case 2: // *fall through*
                case 3:
                case 4: // start vertex
                    segmentStyleRange.foreground = Colour.line_secondary_font[0];
                    break;
                case 5: // *fall through*
                case 6:
                case 7: // end vertex
                    segmentStyleRange.foreground = Colour.line_primary_font[0];
                    break;
                case 8: // *fall through*
                case 9:
                case 10: // control vertex I
                    segmentStyleRange.foreground = Colour.line_secondary_font[0];
                    break;
                case 11: // *fall through*
                case 12:
                case 13: // control vertex II
                    segmentStyleRange.foreground = Colour.line_primary_font[0];
                    break;
                default: // error
                    break;
                }
                styles.add(segmentStyleRange);
                offset += segmentLength + 1;
                segment_number++;
            }
        }
        String[] data_segments = e.lineText.replaceAll("\\s+", " ").trim().split(" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // [ERROR] Check argument count
        if (data_segments.length != 14) {
            parseError = true;
        } else {
            // [ERROR] Check colour
            validateColour(styles.get(2), data_segments[1]);
            // [ERROR] Check identical vertices
            while (true) {
                boolean numberError = false;
                // start vertex
                try {
                    start.setX(new BigDecimal(data_segments[2], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(3));
                    numberError = true;
                }
                try {
                    start.setY(new BigDecimal(data_segments[3], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(4));
                    numberError = true;
                }
                try {
                    start.setZ(new BigDecimal(data_segments[4], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(5));
                    numberError = true;
                }
                // end vertex
                try {
                    end.setX(new BigDecimal(data_segments[5], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(6));
                    numberError = true;
                }
                try {
                    end.setY(new BigDecimal(data_segments[6], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(7));
                    numberError = true;
                }
                try {
                    end.setZ(new BigDecimal(data_segments[7], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(8));
                    numberError = true;
                }
                // control vertex I
                try {
                    controlI.setX(new BigDecimal(data_segments[8], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(9));
                    numberError = true;
                }
                try {
                    controlI.setY(new BigDecimal(data_segments[9], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(10));
                    numberError = true;
                }
                try {
                    controlI.setZ(new BigDecimal(data_segments[10], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(11));
                    numberError = true;
                }
                // control vertex II
                try {
                    controlII.setX(new BigDecimal(data_segments[11], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(12));
                    numberError = true;
                }
                try {
                    controlII.setY(new BigDecimal(data_segments[12], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(13));
                    numberError = true;
                }
                try {
                    controlII.setZ(new BigDecimal(data_segments[13], Threshold.mc));
                } catch (NumberFormatException nfe) {
                    setErrorStyle(styles.get(14));
                    numberError = true;
                }
                if (numberError)
                    break;
                if (replaceVertex) {
                    if (Math.abs(vx - start.X.floatValue()) < replaceEpsilon && Math.abs(vy - start.Y.floatValue()) < replaceEpsilon && Math.abs(vz - start.Z.floatValue()) < replaceEpsilon) {
                        setBorderStyle(styles.get(3));
                        setBorderStyle(styles.get(4));
                        setBorderStyle(styles.get(5));
                    }
                    if (Math.abs(vx - end.X.floatValue()) < replaceEpsilon && Math.abs(vy - end.Y.floatValue()) < replaceEpsilon && Math.abs(vz - end.Z.floatValue()) < replaceEpsilon) {
                        setBorderStyle(styles.get(6));
                        setBorderStyle(styles.get(7));
                        setBorderStyle(styles.get(8));
                    }
                    if (Math.abs(vx - controlI.X.floatValue()) < replaceEpsilon && Math.abs(vy - controlI.Y.floatValue()) < replaceEpsilon && Math.abs(vz - controlI.Z.floatValue()) < replaceEpsilon) {
                        setBorderStyle(styles.get(9));
                        setBorderStyle(styles.get(10));
                        setBorderStyle(styles.get(11));
                    }
                    if (Math.abs(vx - controlII.X.floatValue()) < replaceEpsilon && Math.abs(vy - controlII.Y.floatValue()) < replaceEpsilon
                            && Math.abs(vz - controlII.Z.floatValue()) < replaceEpsilon) {
                        setBorderStyle(styles.get(12));
                        setBorderStyle(styles.get(13));
                        setBorderStyle(styles.get(14));
                    }
                }
                Vector3d.sub(start, end, start);
                Vector3d.sub(controlI, controlII, controlI);
                parseError = parseError || start.length().compareTo(Threshold.identical_vertex_distance) < 0;
                parseError = parseError || controlI.length().compareTo(Threshold.identical_vertex_distance) < 0;
                break;
            }
        }
        if (parseError) {
            for (StyleRange style : styles) {
                style.underline = true;
                style.underlineStyle = SWT.UNDERLINE_ERROR;
                style.underlineColor = Colour.line_error_underline[0];
            }
        } else {
            if (parseWarning) {
                for (StyleRange style : styles) {
                    style.underline = true;
                    style.underlineStyle = SWT.UNDERLINE_ERROR;
                    style.underlineColor = Colour.line_warning_underline[0];
                }
            }
        }
    }
}

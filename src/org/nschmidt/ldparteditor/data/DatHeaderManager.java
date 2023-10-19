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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeSortedMap;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.HeaderState;
import org.nschmidt.ldparteditor.widget.TreeItem;

/**
 * "Call me Mike"
 * ;)
 */
public class DatHeaderManager {

    private DatFile df;

    private boolean hasNoThread = true;
    private volatile AtomicBoolean isRunning = new AtomicBoolean(true);
    private Thread worker = null;

    private volatile Queue<Object[]> workQueue = new ConcurrentLinkedQueue<>();

    private volatile HeaderState state = new HeaderState();

    DatHeaderManager(DatFile df) {
        this.df = df;
    }

    volatile ThreadsafeSortedMap<Integer, List<ParsingResult>> cachedHeaderHints = new ThreadsafeSortedMap<>(); // Cleared

    void pushDatHeaderCheck(GData data, StyledText compositeText, TreeItem hints, TreeItem warnings, TreeItem errors, TreeItem duplicates, Label problemCount) {
        if (df.isReadOnly()) return;
        if (hasNoThread || !worker.isAlive()) {
            hasNoThread = false;
            worker = new Thread(this::checkDatHeader);
            worker.start();
        }

        while (!workQueue.offer(new Object[]{data, hints, warnings, errors, duplicates, compositeText, problemCount})) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }
        }
    }

    private void checkDatHeader() {
        while (isRunning.get() && Editor3DWindow.getAlive().get()) {
            try {
                Object[] newEntry = workQueue.poll();
                if (newEntry != null) {
                    NLogger.debug(getClass(), "Started DATHeader check..."); //$NON-NLS-1$

                    final List<ParsingResult> allHints = new ArrayList<>();
                    int headerState = HeaderState.H00_TITLE;
                    final HeaderState h = new HeaderState();

                    GData gd = (GData) newEntry[0];

                    boolean hasCSG = false;

                    {
                        GData gd2 = gd;
                        while ((gd = gd.next) != null) {
                            if (gd.type() == 8) {
                                GDataCSG csg = (GDataCSG) gd;
                                if (csg.wasNotCompiled()) {
                                    hasCSG = true;
                                    break;
                                }
                            }
                        }
                        gd = gd2;
                    }

                    final TreeItem treeItemHints = (TreeItem) newEntry[1];
                    final TreeItem treeItemWarnings = (TreeItem) newEntry[2];
                    final TreeItem treeItemErrors = (TreeItem) newEntry[3];
                    final TreeItem treeItemDuplicates = (TreeItem) newEntry[4];
                    final StyledText styledTextComposite = (StyledText) newEntry[5];
                    final Label lblProblemCount = (Label) newEntry[6];
                    final GData firstEntry = gd.next;
                    int lineNumber = 0;
                    boolean[] registered = new boolean[]{false};
                    while ((gd = gd.next) != null) {

                        lineNumber += 1;

                        registered[0] = false;
                        int type = gd.type();
                        String trimmedLine = gd.toString().trim();
                        String[] dataSegments = trimmedLine.split("\\s+"); //$NON-NLS-1$

                        // Remove double spaces (essential for complex types)
                        String normalizedLine;
                        if (type > 6 || type < 2) {
                            StringBuilder normalized = new StringBuilder();
                            for (String string : dataSegments) {
                                normalized.append(string);
                                normalized.append(" "); //$NON-NLS-1$
                            }
                            normalizedLine = normalized.toString().trim();
                        } else {
                            normalizedLine = trimmedLine;
                        }

                        if (!isNotBlank(trimmedLine)) {
                            continue;
                        }

                        final boolean isCommentOrBfcMeta = type == 0 || type == 6;
                        if (!isCommentOrBfcMeta) {
                            break;
                        }

                        while (isCommentOrBfcMeta) {

                            // HeaderState._01_NAME
                            if (headerState == HeaderState.H01_NAME) {
                                // I expect that this line is a valid Name
                                if (normalizedLine.startsWith("0 Name: ") && normalizedLine.length() > 12 && normalizedLine.endsWith(".dat")) { //$NON-NLS-1$ //$NON-NLS-2$
                                    h.setLineNAME(lineNumber);
                                    h.setHasNAME(true);
                                    headerState = HeaderState.H02_AUTHOR;
                                    break;
                                } else { // Its something else..
                                    headerState = HeaderState.H02_AUTHOR;
                                }
                            } else {
                                // I don't expect that this line is a valid Name
                                if (normalizedLine.startsWith("0 Name: ") && normalizedLine.length() > 12 && normalizedLine.endsWith(".dat")) { //$NON-NLS-1$ //$NON-NLS-2$
                                    // Its duplicated
                                    if (h.hasNAME()) {
                                        registerHeaderHint(lineNumber, "11", I18n.DATPARSER_DUPLICATED_FILENAME, registered, allHints); //$NON-NLS-1$
                                    } else {
                                        if (headerState > HeaderState.H01_NAME) { // Its misplaced
                                            registerHeaderHint(lineNumber, "12", I18n.DATPARSER_MISPLACED_FILENAME, registered, allHints); //$NON-NLS-1$
                                        }
                                        h.setLineNAME(lineNumber);
                                        h.setHasNAME(true);
                                        headerState = HeaderState.H02_AUTHOR;
                                    }
                                    break;
                                }
                            }

                            // HeaderState._02_AUTHOR
                            if (headerState == HeaderState.H02_AUTHOR) {
                                // I expect that this line is a valid Author
                                if (normalizedLine.startsWith("0 Author:")) { //$NON-NLS-1$
                                    String author = normalizedLine.substring(9).trim();
                                    int liL = author.lastIndexOf('[');
                                    int liR = author.lastIndexOf(']');
                                    boolean indexBrL = author.indexOf('[') == liL;
                                    boolean indexBrR = author.indexOf(']') == liR;
                                    if (author.length() > 0 && author.indexOf("[]") == -1 && indexBrL && indexBrR && (liL == -1 || author.indexOf(" [") != -1) && liL <= liR && 0 < liL * liR) { //$NON-NLS-1$ //$NON-NLS-2$
                                        h.setLineAUTHOR(lineNumber);
                                        h.setHasAUTHOR(true);
                                        headerState = HeaderState.H03_TYPE;
                                        break;
                                    } else { // Its something else..
                                        headerState = HeaderState.H03_TYPE;
                                    }
                                } else { // Its something else..
                                    headerState = HeaderState.H03_TYPE;
                                }
                            } else {
                                // I don't expect that this line is a valid Author
                                if (normalizedLine.startsWith("0 Author:")) { //$NON-NLS-1$
                                    String author = normalizedLine.substring(9).trim();
                                    int liL = author.lastIndexOf('[');
                                    int liR = author.lastIndexOf(']');
                                    boolean indexBrL = author.indexOf('[') == liL;
                                    boolean indexBrR = author.indexOf(']') == liR;
                                    if (author.length() > 0 && author.indexOf("[]") == -1 && indexBrL && indexBrR && (liL == -1 || author.indexOf(" [") != -1) && liL <= liR && 0 < liL * liR) { //$NON-NLS-1$ //$NON-NLS-2$
                                        // Its duplicated
                                        if (h.hasAUTHOR()) {
                                            registerHeaderHint(lineNumber, "21", I18n.DATPARSER_DUPLICATED_AUTHOR, registered, allHints); //$NON-NLS-1$
                                        } else {
                                            if (headerState > HeaderState.H02_AUTHOR) { // Its misplaced
                                                registerHeaderHint(lineNumber, "22", I18n.DATPARSER_MISPLACED_AUTHOR, registered, allHints); //$NON-NLS-1$
                                            }
                                            h.setLineAUTHOR(lineNumber);
                                            h.setHasAUTHOR(true);
                                            headerState = HeaderState.H03_TYPE;
                                        }
                                        break;
                                    }
                                }
                            }

                            // HeaderState._03_TYPE
                            if (headerState == HeaderState.H03_TYPE) {
                                // I expect that this line is a valid Type
                                final boolean isPart = "0 !LDRAW_ORG Unofficial_Part".equals(normalizedLine) //$NON-NLS-1$
                                        || "0 !LDRAW_ORG Unofficial_Part Flexible_Section".equals(normalizedLine) //$NON-NLS-1$
                                        || "0 !LDRAW_ORG Unofficial_Shortcut".equals(normalizedLine) //$NON-NLS-1$
                                        || "0 !LDRAW_ORG Unofficial_Shortcut Alias".equals(normalizedLine) //$NON-NLS-1$
                                        || "0 !LDRAW_ORG Unofficial_Shortcut Physical_Colour".equals(normalizedLine) //$NON-NLS-1$
                                        || "0 !LDRAW_ORG Unofficial_Shortcut Physical_Colour Alias".equals(normalizedLine) //$NON-NLS-1$
                                        || "0 !LDRAW_ORG Unofficial_Part Alias".equals(normalizedLine) //$NON-NLS-1$
                                        || "0 !LDRAW_ORG Unofficial_Part Physical_Colour".equals(normalizedLine); //$NON-NLS-1$
                                final boolean isSubPart = "0 !LDRAW_ORG Unofficial_Subpart".equals(normalizedLine); //$NON-NLS-1$
                                final boolean isPrimitive = "0 !LDRAW_ORG Unofficial_Primitive".equals(normalizedLine); //$NON-NLS-1$
                                final boolean isPrimitive48 = "0 !LDRAW_ORG Unofficial_48_Primitive".equals(normalizedLine); //$NON-NLS-1$
                                final boolean isPrimitive8 = "0 !LDRAW_ORG Unofficial_8_Primitive".equals(normalizedLine); //$NON-NLS-1$
                                if (isPart
                                        || isSubPart
                                        || isPrimitive
                                        || isPrimitive48
                                        || isPrimitive8) {
                                    h.setLineTYPE(lineNumber);
                                    h.setHasTYPE(true);
                                    h.setHasUNOFFICIAL(true);
                                    h.setHasUPDATE(false);
                                    headerState = HeaderState.H04_LICENSE;
                                    if (isPart) df.setType(DatType.PART);
                                    if (isSubPart) df.setType(DatType.SUBPART);
                                    if (isPrimitive) df.setType(DatType.PRIMITIVE);
                                    if (isPrimitive48) df.setType(DatType.PRIMITIVE48);
                                    if (isPrimitive8) df.setType(DatType.PRIMITIVE8);
                                    break;
                                } else if ("0 !LDRAW_ORG".equals(normalizedLine)) { //$NON-NLS-1$
                                    h.setLineTYPE(lineNumber);
                                    h.setHasTYPE(false);
                                    h.setHasUNOFFICIAL(true);
                                    h.setHasUPDATE(false);
                                    headerState = HeaderState.H04_LICENSE;
                                    break;
                                } else if (normalizedLine.startsWith("0 !LDRAW_ORG ")) { //$NON-NLS-1$
                                    if (!normalizedLine.contains("Unofficial_")) { //$NON-NLS-1$
                                        h.setLineTYPE(lineNumber);
                                        h.setHasTYPE(true);
                                        h.setHasUNOFFICIAL(false);
                                        h.setHasUPDATE(false);
                                        headerState = HeaderState.H04_LICENSE;
                                        break;
                                    } else if (normalizedLine.contains("UPDATE")) { //$NON-NLS-1$
                                        h.setLineTYPE(lineNumber);
                                        h.setHasTYPE(true);
                                        h.setHasUNOFFICIAL(true);
                                        h.setHasUPDATE(true);
                                        headerState = HeaderState.H04_LICENSE;
                                        break;
                                    } else { // Its something else..
                                        headerState = HeaderState.H04_LICENSE;
                                    }
                                } else { // Its something else..
                                    headerState = HeaderState.H04_LICENSE;
                                }
                            } else {
                                // I don't expect that this line is a valid Type
                                if (normalizedLine.startsWith("0 !LDRAW_ORG")) { //$NON-NLS-1$
                                    // Its duplicated
                                    if (h.hasTYPE() || h.getLineTYPE() > -1) {
                                        registerHeaderHint(lineNumber, "33", I18n.DATPARSER_DUPLICATED_TYPE, registered, allHints); //$NON-NLS-1$
                                    } else {
                                        if (headerState > HeaderState.H03_TYPE) { // Its misplaced
                                            registerHeaderHint(lineNumber, "34", I18n.DATPARSER_MISPLACED_TYPE, registered, allHints); //$NON-NLS-1$
                                        }
                                        h.setLineTYPE(lineNumber);
                                        h.setHasTYPE(true);
                                        headerState = HeaderState.H04_LICENSE;
                                    }
                                    break;
                                }
                            }

                            // HeaderState._04_LICENSE
                            if (headerState == HeaderState.H04_LICENSE) {
                                // I expect that this line is a valid License
                                if (isValidLicenseLine(normalizedLine)) {
                                    h.setLineLICENSE(lineNumber);
                                    h.setHasLICENSE(true);
                                    headerState = HeaderState.H05_OPTIONAL_HELP;
                                    break;
                                } else { // Its something else..
                                    headerState = HeaderState.H05_OPTIONAL_HELP;
                                }
                            } else {
                                // I don't expect that this line is a valid License
                                if (isValidLicenseLine(normalizedLine)) {
                                    // Its duplicated
                                    if (h.hasLICENSE()) {
                                        registerHeaderHint(lineNumber, "41", I18n.DATPARSER_DUPLICATED_LICENSE, registered, allHints); //$NON-NLS-1$
                                    } else {
                                        if (headerState > HeaderState.H04_LICENSE) { // Its misplaced
                                            registerHeaderHint(lineNumber, "42", I18n.DATPARSER_MISPLACED_LICENSE, registered, allHints); //$NON-NLS-1$
                                        }
                                        h.setLineLICENSE(lineNumber);
                                        h.setHasLICENSE(true);
                                        headerState = HeaderState.H05_OPTIONAL_HELP;
                                    }
                                    break;
                                }
                            }

                            // HeaderState._05o_HELP
                            if (headerState == HeaderState.H05_OPTIONAL_HELP) {
                                // I expect that this line is a valid Help
                                if (normalizedLine.startsWith("0 !HELP")) { //$NON-NLS-1$
                                    if (h.hasHELP()) {
                                        h.setLineHELPend(lineNumber);
                                    } else {
                                        h.setLineHELPstart(lineNumber);
                                    }
                                    h.setHasHELP(true);
                                    headerState = HeaderState.H05_OPTIONAL_HELP;
                                    break;
                                } else { // Its something else..
                                    headerState = HeaderState.H06_BFC;
                                }
                            } else {
                                // I don't expect that this line is a valid Help
                                if (normalizedLine.startsWith("0 !HELP")) { //$NON-NLS-1$
                                    // Its duplicated
                                    if (h.hasHELP()) {
                                        registerHeaderHint(lineNumber, "51", I18n.DATPARSER_SPLIT_HELP, registered, allHints); //$NON-NLS-1$
                                    } else {
                                        if (headerState > HeaderState.H05_OPTIONAL_HELP) { // Its misplaced
                                            registerHeaderHint(lineNumber, "52", I18n.DATPARSER_MISPLACED_HELP, registered, allHints); //$NON-NLS-1$
                                        }
                                        h.setLineHELPstart(lineNumber);
                                        h.setHasHELP(true);
                                        headerState = HeaderState.H05_OPTIONAL_HELP;
                                    }
                                    break;
                                }
                            }

                            // HeaderState._06_BFC
                            if (headerState == HeaderState.H06_BFC) {
                                // I expect that this line is a valid BFC Statement
                                if ("0 BFC CERTIFY CCW".equals(normalizedLine) //$NON-NLS-1$
                                        || "0 BFC CERTIFY CW".equals(normalizedLine) //$NON-NLS-1$
                                        || "0 BFC NOCERTIFY".equals(normalizedLine)) { //$NON-NLS-1$
                                    h.setLineBFC(lineNumber);
                                    h.setHasBFC(true);
                                    headerState = HeaderState.H07_OPTIONAL_CATEGORY;
                                    break;
                                } else { // Its something else..
                                    headerState = HeaderState.H07_OPTIONAL_CATEGORY;
                                }
                            } else {
                                // I don't expect that this line is a valid BFC Statement
                                if ("0 BFC CERTIFY CCW".equals(normalizedLine) //$NON-NLS-1$
                                        || "0 BFC CERTIFY CW".equals(normalizedLine) //$NON-NLS-1$
                                        || "0 BFC NOCERTIFY".equals(normalizedLine)) { //$NON-NLS-1$
                                    // Its duplicated
                                    if (h.hasBFC()) {
                                        registerHeaderHint(lineNumber, "61", I18n.DATPARSER_DUPLICATED_BFC, registered, allHints); //$NON-NLS-1$
                                    } else {
                                        if (headerState > HeaderState.H06_BFC) { // Its misplaced
                                            registerHeaderHint(lineNumber, "62", I18n.DATPARSER_MISPLACED_BFC_0, registered, allHints); //$NON-NLS-1$
                                        }
                                        h.setLineBFC(lineNumber);
                                        h.setHasBFC(true);
                                        headerState = HeaderState.H07_OPTIONAL_CATEGORY;
                                    }
                                    break;
                                }
                            }

                            // HeaderState._07o_CATEGORY
                            if (headerState == HeaderState.H07_OPTIONAL_CATEGORY) {
                                // I expect that this line is a valid Category
                                if (normalizedLine.startsWith("0 !CATEGORY ")) { //$NON-NLS-1$
                                    h.setHasCATEGORY(true);
                                    headerState = HeaderState.H08_OPTIONAL_KEYWORDS;
                                    break;
                                } else { // Its something else..
                                    headerState = HeaderState.H08_OPTIONAL_KEYWORDS;
                                }
                            } else {
                                // I don't expect that this line is a valid Category
                                if (normalizedLine.startsWith("0 !CATEGORY ")) { //$NON-NLS-1$
                                    // Its duplicated
                                    if (h.hasCATEGORY()) {
                                        registerHeaderHint(lineNumber, "71", I18n.DATPARSER_DUPLICATED_CATEGORY, registered, allHints); //$NON-NLS-1$
                                    } else {
                                        if (headerState > HeaderState.H07_OPTIONAL_CATEGORY) { // Its misplaced
                                            registerHeaderHint(lineNumber, "72", I18n.DATPARSER_MISPLACED_CATEGORY, registered, allHints); //$NON-NLS-1$
                                        }
                                        h.setHasCATEGORY(true);
                                        headerState = HeaderState.H08_OPTIONAL_KEYWORDS;
                                    }
                                    break;
                                }
                            }

                            // HeaderState._08o_KEYWORDS
                            if (headerState == HeaderState.H08_OPTIONAL_KEYWORDS) {
                                // I expect that this line is a valid Keyword
                                if (normalizedLine.startsWith("0 !KEYWORDS ")) { //$NON-NLS-1$
                                    h.setHasKEYWORDS(true);
                                    headerState = HeaderState.H08_OPTIONAL_KEYWORDS;
                                    break;
                                } else { // Its something else..
                                    headerState = HeaderState.H09_OPTIONAL_CMDLINE;
                                }
                            } else {
                                // I don't expect that this line is a valid Keyword
                                if (normalizedLine.startsWith("0 !KEYWORDS ")) { //$NON-NLS-1$
                                    // Its duplicated
                                    if (h.hasKEYWORDS()) {
                                        registerHeaderHint(lineNumber, "81", I18n.DATPARSER_SPLIT_KEYWORD, registered, allHints); //$NON-NLS-1$
                                    } else {
                                        if (headerState > HeaderState.H08_OPTIONAL_KEYWORDS) { // Its misplaced
                                            registerHeaderHint(lineNumber, "82", I18n.DATPARSER_MISPLACED_KEYWORD, registered, allHints); //$NON-NLS-1$
                                        }
                                        h.setHasKEYWORDS(true);
                                        headerState = HeaderState.H08_OPTIONAL_KEYWORDS;
                                    }
                                    break;
                                }
                            }

                            // HeaderState._09o_CMDLINE
                            if (headerState == HeaderState.H09_OPTIONAL_CMDLINE) {
                                // I expect that this line is a valid Command Line
                                if (normalizedLine.startsWith("0 !CMDLINE ")) { //$NON-NLS-1$
                                    h.setHasCMDLINE(true);
                                    headerState = HeaderState.H10_OPTIONAL_HISTORY;
                                    break;
                                } else { // Its something else..
                                    headerState = HeaderState.H10_OPTIONAL_HISTORY;
                                }
                            } else {
                                // I don't expect that this line is a valid Command Line
                                if (normalizedLine.startsWith("0 !CMDLINE ")) { //$NON-NLS-1$
                                    // Its duplicated
                                    if (h.hasCMDLINE()) {
                                        registerHeaderHint(lineNumber, "91", I18n.DATPARSER_DUPLICATED_COMMAND_LINE, registered, allHints); //$NON-NLS-1$
                                    } else {
                                        if (headerState > HeaderState.H09_OPTIONAL_CMDLINE) { // Its misplaced
                                            registerHeaderHint(lineNumber, "92", I18n.DATPARSER_MISPLACED_COMMAND_LINE, registered, allHints); //$NON-NLS-1$
                                        }
                                        h.setHasCMDLINE(true);
                                        headerState = HeaderState.H10_OPTIONAL_HISTORY;
                                    }
                                    break;
                                }
                            }

                            // HeaderState._10o_HISTORY TODO Needs better validation
                            if (headerState == HeaderState.H10_OPTIONAL_HISTORY) {
                                // I expect that this line is a valid History Entry
                                if (normalizedLine.startsWith("0 !HISTORY ") && normalizedLine.length() > 20) { //$NON-NLS-1$
                                    if (h.hasHISTORY()) {
                                        final String lh = h.getLastHistoryEntry();
                                        if (lh != null && normalizedLine.substring(0, "0 !HISTORY YYYY-MM-DD".length()).compareTo(lh) < 0) { //$NON-NLS-1$
                                            registerHeaderHint(lineNumber, "A3", I18n.DATPARSER_HISTORY_WRONG_ORDER, registered, allHints); //$NON-NLS-1$
                                        }
                                    } else {
                                        h.setLastHistoryEntry(normalizedLine.substring(0, "0 !HISTORY YYYY-MM-DD".length())); //$NON-NLS-1$
                                    }
                                    h.setHasHISTORY(true);
                                    headerState = HeaderState.H10_OPTIONAL_HISTORY;
                                    break;
                                } else { // Its something else..
                                    headerState = HeaderState.H11_OPTIONAL_COMMENT;
                                }
                            } else {
                                // I don't expect that this line is a valid History Entry
                                if (normalizedLine.startsWith("0 !HISTORY ")) { //$NON-NLS-1$
                                    // Its duplicated
                                    if (h.hasHISTORY()) {
                                        registerHeaderHint(lineNumber, "A1", I18n.DATPARSER_SPLIT_HISTORY, registered, allHints); //$NON-NLS-1$
                                    } else {
                                        if (headerState > HeaderState.H10_OPTIONAL_HISTORY) { // Its misplaced
                                            registerHeaderHint(lineNumber, "A2", I18n.DATPARSER_MISPLACED_HISTORY, registered, allHints); //$NON-NLS-1$
                                        }
                                        h.setHasHISTORY(true);
                                        headerState = HeaderState.H10_OPTIONAL_HISTORY;
                                    }
                                    break;
                                }
                            }

                            // HeaderState._11o_COMMENT
                            if (headerState == HeaderState.H11_OPTIONAL_COMMENT) {
                                // I expect that this line is a valid Comment
                                if (normalizedLine.startsWith("0 // ")) { //$NON-NLS-1$
                                    h.setHasCOMMENT(true);
                                    headerState = HeaderState.H11_OPTIONAL_COMMENT;
                                    break;
                                } else {// Its something else..
                                    headerState = HeaderState.H12_OPTIONAL_BFC2;
                                }
                            } else {
                                // I don't expect that this line is a valid Comment
                                if (normalizedLine.startsWith("0 // ")) { //$NON-NLS-1$
                                    // Its duplicated
                                    if (h.hasCOMMENT()) {
                                        registerHeaderHint(lineNumber, "B1", I18n.DATPARSER_SPLIT_COMMMENT, registered, allHints); //$NON-NLS-1$
                                    } else {
                                        if (headerState > HeaderState.H11_OPTIONAL_COMMENT) { // Its misplaced
                                            registerHeaderHint(lineNumber, "B2", I18n.DATPARSER_MISPLACED_COMMENT, registered, allHints); //$NON-NLS-1$
                                        }
                                        h.setHasCOMMENT(true);
                                        headerState = HeaderState.H11_OPTIONAL_COMMENT;
                                    }
                                    break;
                                }
                            }

                            // HeaderState._12o_BFC2
                            if (headerState == HeaderState.H12_OPTIONAL_BFC2) {
                                // I expect that this line is a valid BFC Statement
                                if (normalizedLine.startsWith("0 BFC") && (normalizedLine.equals("0 BFC CW") //$NON-NLS-1$ //$NON-NLS-2$
                                        || normalizedLine.equals("0 BFC CCW") //$NON-NLS-1$
                                        || normalizedLine.equals("0 BFC CW CLIP") //$NON-NLS-1$
                                        || normalizedLine.equals("0 BFC CCW CLIP") //$NON-NLS-1$
                                        || normalizedLine.equals("0 BFC CLIP CW") //$NON-NLS-1$
                                        || normalizedLine.equals("0 BFC CLIP CCW") //$NON-NLS-1$
                                        || normalizedLine.equals("0 BFC NOCLIP") //$NON-NLS-1$
                                        || normalizedLine.equals("0 BFC INVERTNEXT"))) { //$NON-NLS-1$
                                    h.setHasBFC2(true);
                                    headerState = HeaderState.H12_OPTIONAL_BFC2;
                                    break;
                                }
                            } else {
                                // I don't expect that this line is a valid BFC Statement
                                if (normalizedLine.startsWith("0 BFC") && (normalizedLine.equals("0 BFC CW") //$NON-NLS-1$ //$NON-NLS-2$
                                        || normalizedLine.equals("0 BFC CCW") //$NON-NLS-1$
                                        || normalizedLine.equals("0 BFC CW CLIP") //$NON-NLS-1$
                                        || normalizedLine.equals("0 BFC CCW CLIP") //$NON-NLS-1$
                                        || normalizedLine.equals("0 BFC CLIP CW") //$NON-NLS-1$
                                        || normalizedLine.equals("0 BFC CLIP CCW") //$NON-NLS-1$
                                        || normalizedLine.equals("0 BFC NOCLIP") //$NON-NLS-1$
                                        || normalizedLine.equals("0 BFC INVERTNEXT"))) { //$NON-NLS-1$
                                    // Its duplicated
                                    if (h.hasBFC2()) {
                                        registerHeaderHint(lineNumber, "C1", I18n.DATPARSER_SPLIT_BFC, registered, allHints); //$NON-NLS-1$
                                    } else {
                                        if (headerState > HeaderState.H12_OPTIONAL_BFC2) { // Its misplaced
                                            registerHeaderHint(lineNumber, "C2", I18n.DATPARSER_MISPLACED_BFC, registered, allHints); //$NON-NLS-1$
                                        }
                                        h.setHasBFC2(true);
                                        headerState = HeaderState.H12_OPTIONAL_BFC2;
                                    }
                                    break;
                                }
                            }

                            if (h.hasTITLE()) {
                                // registerHint(gd, lineNumber, "01", I18n.DATPARSER_InvalidHeader, registered); //$NON-NLS-1$
                            } else {
                                h.setLineTITLE(lineNumber);
                                h.setHasTITLE(true);
                                if (headerState != HeaderState.H00_TITLE) {
                                    registerHeaderHint(lineNumber, "02", I18n.DATPARSER_MISPLACED_TITLE, registered, allHints); //$NON-NLS-1$
                                } else {
                                    headerState = HeaderState.H01_NAME;
                                }
                            }
                            break;
                        }
                    }
                    
                    if (gd != null) {
                        // Check on numbers with scientific notation
                        if (gd.type() > 0 && gd.type() < 6) {
                            gd = gd.before;
                            lineNumber -= 1;
                        }
                        int index = 0;
                        while (gd != null && (gd = gd.next) != null) {
                            lineNumber += 1;
                            int type = gd.type();
                            if (type > 0 && type < 6) {
                                String trimmedLine = gd.toString().trim();
                                if (type == 1 && -1 != (index = trimmedLine.indexOf(((GData1) gd).shortName))) trimmedLine = trimmedLine.substring(0, index);
                                String[] dataSegments = trimmedLine.split("\\s+"); //$NON-NLS-1$
                                for (int i = 2; i < dataSegments.length; i++) {
                                    if (type == 2 && i == 2 && !((GData2) gd).isLine) continue;
                                    String string = dataSegments[i];
                                    if (string.toUpperCase().contains("E")) { //$NON-NLS-1$
                                        registerFormatHint(lineNumber, "FE", I18n.DATPARSER_INVALID_NUMBER_FORMAT, registered, allHints); //$NON-NLS-1$
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    // Check on wrong comment lines with just one slash
                    gd = firstEntry;
                    if (gd != null) {
                        lineNumber = 0;
                        gd = gd.before;
                        while (gd != null && (gd = gd.next) != null) {
                            lineNumber += 1;
                            int type = gd.type();
                            if (type == 0) {
                                
                                String trimmedLine = gd.toString().stripLeading();
                                String[] dataSegments = trimmedLine.split("\\s+"); //$NON-NLS-1$

                                // Remove double spaces (essential for complex types)
                                String normalizedLine;
                                StringBuilder normalized = new StringBuilder();
                                for (String string : dataSegments) {
                                    normalized.append(string);
                                    normalized.append(" "); //$NON-NLS-1$
                                }
                                normalizedLine = normalized.toString().stripLeading();

                                if (normalizedLine.startsWith("0 / ")) { //$NON-NLS-1$
                                    normalizedLine = normalizedLine.trim();
                                    normalizedLine = normalizedLine.substring(0, Math.min(normalizedLine.length(), 16)) + (normalizedLine.length() > 16 ? "..." : ""); //$NON-NLS-1$ //$NON-NLS-2$
                                    Object[] messageArguments = {normalizedLine};
                                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                    formatter.setLocale(MyLanguage.getLocale());
                                    formatter.applyPattern(I18n.DATPARSER_INVALID_COMMENT);
                                    registerFormatHint(lineNumber, "C0", formatter.format(messageArguments), registered, allHints); //$NON-NLS-1$
                                }
                            }
                        }
                    }

                    if (firstEntry != null) {
                        registered[0] = false;
                        int r1 = 0;
                        int r2 = 0;
                        int r3 = 0;
                        int r4 = 0;
                        int r5 = 0;
                        int r6 = 0;
                        if (!h.hasTITLE()) {
                            r1 = 1;
                            if (!h.hasNAME()) {
                                r2 = 1;
                                if (!h.hasAUTHOR()) {
                                    r3 = 1;
                                    if (!h.hasTYPE() || !h.hasUNOFFICIAL() || h.hasUPDATE()) {
                                        r4 = 1;
                                        if (!h.hasLICENSE()) {
                                            r5 = 1;
                                            if (!h.hasBFC()) {
                                                r6 = 1;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!h.hasTITLE()) {
                            registerHeaderHint(-60 * r1 + -1 * (1 - r1), "00", I18n.DATFILE_MISSING_TITLE, registered, allHints); //$NON-NLS-1$
                        }
                        if (!h.hasNAME()) {
                            registerHeaderHint(-50 * r2 + -2 * (1 - r2), "10", I18n.DATFILE_MISSING_FILENAME, registered, allHints); //$NON-NLS-1$
                        }
                        if (!h.hasAUTHOR()) {
                            registerHeaderHint(-40 * r3 + -3 * (1 - r3), "20", I18n.DATFILE_MISSING_AUTHOR, registered, allHints); //$NON-NLS-1$
                        }
                        if (!h.hasTYPE()) {
                            registerHeaderHint(-30 * r4 + -4 * (1 - r4), "30", I18n.DATFILE_MISSING_PART_TYPE, registered, allHints); //$NON-NLS-1$
                        } else if (!h.hasUNOFFICIAL()) {
                            registerHeaderHint(-30 * r4 + -4 * (1 - r4), "31", I18n.DATFILE_MISSING_UNOFFICIAL, registered, allHints); //$NON-NLS-1$
                        } else if (h.hasUPDATE()) {
                            registerHeaderHint(-30 * r4 + -4 * (1 - r4), "32", I18n.DATFILE_INVALID_UPDATE, registered, allHints); //$NON-NLS-1$
                        }
                        if (!h.hasLICENSE()) {
                            registerHeaderHint(-20 * r5 + -5 * (1 - r5), "40", I18n.DATFILE_MISSING_LICENSE, registered, allHints); //$NON-NLS-1$
                        }
                        if (!h.hasBFC()) {
                            registerHeaderHint(-10 * r6 + -6 * (1 - r6), "60", I18n.DATFILE_MISSING_BFC, registered, allHints); //$NON-NLS-1$
                        }

                        if (hasCSG) {
                            registerHeaderHint(-999, "42", I18n.DATFILE_CSG_TO_COMPILE, registered, allHints); //$NON-NLS-1$
                        }
                    }

                    final boolean doWait = !allHints.isEmpty();

                    state = h;
                    int firstKey = cachedHeaderHints.isEmpty() ? Integer.MAX_VALUE : cachedHeaderHints.firstKey();
                    firstKey -= 1;
                    cachedHeaderHints.put(firstKey, allHints);
                    if (cachedHeaderHints.size() > 3) {
                        cachedHeaderHints.remove(cachedHeaderHints.lastKey());
                    }

                    if (doWait) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new LDPartEditorException(ie);
                        }
                    }

                    Display.getDefault().syncExec(() -> {
                        try {
                            if (df.updateDatHeaderHints(styledTextComposite, treeItemHints)) {
                                int errorCount = treeItemErrors.getItems().size();
                                int warningCount = treeItemWarnings.getItems().size();
                                int hintCount = treeItemHints.getItems().size();
                                int duplicateCount = treeItemDuplicates.getItems().size();
                                String errors = errorCount == 1 ? I18n.EDITORTEXT_ERROR : I18n.EDITORTEXT_ERRORS;
                                String warnings = warningCount == 1 ? I18n.EDITORTEXT_WARNING : I18n.EDITORTEXT_WARNINGS;
                                String hints = hintCount == 1 ? I18n.EDITORTEXT_OTHER : I18n.EDITORTEXT_OTHERS;
                                String duplicates = duplicateCount == 1 ? I18n.EDITORTEXT_DUPLICATE : I18n.EDITORTEXT_DUPLICATES;
                                lblProblemCount.setText(errorCount + " " + errors + ", " + warningCount + " " + warnings + ", " + hintCount + " " + hints + ", " + duplicateCount + " " + duplicates); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                                org.nschmidt.ldparteditor.widget.Tree tree = treeItemHints.getParent();
                                tree.build();
                                lblProblemCount.getParent().layout();
                                lblProblemCount.getParent().redraw();
                                lblProblemCount.getParent().update();
                                tree.redraw();
                                tree.update();
                                lblProblemCount.redraw();
                                lblProblemCount.update();
                            }
                        } catch (Exception ex) {
                            // The text editor widget could be disposed
                            NLogger.debug(getClass(), "Uncritical DatHeaderManager Exception:"); //$NON-NLS-1$
                            NLogger.debug(getClass(), ex);
                        }
                    });
                }
                if (workQueue.isEmpty()) Thread.sleep(200);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            } catch (Exception e) {
                // We want to know what can go wrong here
                // because it SHOULD be avoided!!
                NLogger.error(getClass(), "The DatHeaderManager cycle was throwing an exception :("); //$NON-NLS-1$
                NLogger.error(getClass(), e);
            }
        }
        cachedHeaderHints.clear();
    }

    private boolean isValidLicenseLine(String normalizedLine) {
        return "0 !LICENSE Licensed under CC BY 4.0 : see CAreadme.txt".equals(normalizedLine) //$NON-NLS-1$
            || "0 !LICENSE Licensed under CC BY 2.0 and CC BY 4.0 : see CAreadme.txt".equals(normalizedLine) //$NON-NLS-1$
            || "0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt".equals(normalizedLine) //$NON-NLS-1$
            || "0 !LICENSE Not redistributable : see NonCAreadme.txt".equals(normalizedLine); //$NON-NLS-1$
    }

    void deleteHeaderHints() {
        isRunning.set(false);
    }

    public void setDatFile(DatFile df) {
        this.df = df;
    }

    public HeaderState getState() {
        return state;
    }

    private void registerHeaderHint(int lineNumber, String errno, String message, boolean[] registered, List<ParsingResult> allHints) {
        registerHint(lineNumber, errno, new Object[]{}, message, I18n.DATPARSER_HEADER_HINT, registered, allHints);
    }
    
    private void registerFormatHint(int lineNumber, String errno, String message, boolean[] registered, List<ParsingResult> allHints) {
        registerHint(lineNumber, errno, new Object[]{}, message, I18n.DATPARSER_FORMAT_HINT, registered, allHints);
    }

    private void registerHint(int lineNumber, String errno, Object[] args, String message, String typeDescription, boolean[] registered, List<ParsingResult> allHints) {
        registered[0] = true;
        Object[] messageArguments = args;
        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
        formatter.setLocale(MyLanguage.getLocale());
        formatter.applyPattern(message);
        allHints.add(new ParsingResult(formatter.format(messageArguments), "[H" + errno + "] " + typeDescription, ResultType.HINT, lineNumber)); //$NON-NLS-1$ //$NON-NLS-2$
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
}

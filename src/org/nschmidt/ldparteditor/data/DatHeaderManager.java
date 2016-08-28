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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.HeaderState;
import org.nschmidt.ldparteditor.widgets.TreeItem;

/**
 * "Call me Mike"
 * ;)
 * @author nils
 *
 */
public class DatHeaderManager {

    private DatFile df;

    private boolean hasNoThread = true;
    private volatile AtomicBoolean isRunning = new AtomicBoolean(true);

    private volatile Queue<Object[]> workQueue = new ConcurrentLinkedQueue<Object[]>();

    private volatile HeaderState state = new HeaderState();

    public DatHeaderManager(DatFile df) {
        this.df = df;
    }

    volatile ThreadsafeTreeMap<Integer, ArrayList<ParsingResult>> CACHE_headerHints = new ThreadsafeTreeMap<Integer, ArrayList<ParsingResult>>(); // Cleared

    public void pushDatHeaderCheck(GData data, StyledText compositeText, TreeItem hints, TreeItem warnings, TreeItem errors, TreeItem duplicates, Label problemCount) {
        if (df.isReadOnly()) return;
        if (hasNoThread) {
            hasNoThread = false;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    while (isRunning.get() && Editor3DWindow.getAlive().get()) {
                        try {
                            Object[] newEntry = workQueue.poll();
                            if (newEntry != null) {
                                NLogger.debug(getClass(), "Started DATHeader check..."); //$NON-NLS-1$

                                final ArrayList<ParsingResult> allHints = new ArrayList<ParsingResult>();
                                int headerState = HeaderState._00_TITLE;
                                final HeaderState h = new HeaderState();
                                state = h;

                                GData gd = (GData) newEntry[0];
                                final TreeItem treeItem_Hints = (TreeItem) newEntry[1];
                                final TreeItem treeItem_Warnings = (TreeItem) newEntry[2];
                                final TreeItem treeItem_Errors = (TreeItem) newEntry[3];
                                final TreeItem treeItem_Duplicates = (TreeItem) newEntry[4];
                                final StyledText styledText_Composite = (StyledText) newEntry[5];
                                final Label lbl_ProblemCount = (Label) newEntry[6];
                                final GData firstEntry = gd.next;
                                int lineNumber = 0;
                                boolean[] registered = new boolean[]{false};
                                while ((gd = gd.next) != null) {

                                    lineNumber += 1;

                                    registered[0] = false;
                                    int type = gd.type();
                                    String trimmedLine = gd.toString().trim();
                                    String[] data_segments = trimmedLine.split("\\s+"); //$NON-NLS-1$

                                    // Remove double spaces (essential for complex types)
                                    String normalizedLine;
                                    if (type > 6 || type < 2) {
                                        StringBuilder normalized = new StringBuilder();
                                        for (String string : data_segments) {
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

                                    // FIXME Needs implementation!

                                    while (type == 0 || type == 6) {

                                        // HeaderState._01_NAME
                                        if (headerState == HeaderState._01_NAME) {
                                            // I expect that this line is a valid Name
                                            if (normalizedLine.startsWith("0 Name: ") && normalizedLine.length() > 12 && normalizedLine.endsWith(".dat")) { //$NON-NLS-1$ //$NON-NLS-2$
                                                h.setLineNAME(lineNumber);
                                                h.setHasNAME(true);
                                                headerState = HeaderState._02_AUTHOR;
                                                break;
                                            } else { // Its something else..
                                                headerState = HeaderState._02_AUTHOR;
                                            }
                                        } else {
                                            // I don't expect that this line is a valid Name
                                            if (normalizedLine.startsWith("0 Name: ") && normalizedLine.length() > 12 && normalizedLine.endsWith(".dat")) { //$NON-NLS-1$ //$NON-NLS-2$
                                                // Its duplicated
                                                if (h.hasNAME()) {
                                                    registerHint(gd, lineNumber, "11", I18n.DATPARSER_DuplicatedFilename, registered, allHints); //$NON-NLS-1$
                                                } else {
                                                    if (headerState > HeaderState._01_NAME) { // Its misplaced
                                                        registerHint(gd, lineNumber, "12", I18n.DATPARSER_MisplacedFilename, registered, allHints); //$NON-NLS-1$
                                                    }
                                                    h.setLineNAME(lineNumber);
                                                    h.setHasNAME(true);
                                                    headerState = HeaderState._02_AUTHOR;
                                                }
                                                break;
                                            }
                                        }

                                        // HeaderState._02_AUTHOR
                                        if (headerState == HeaderState._02_AUTHOR) {
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
                                                    headerState = HeaderState._03_TYPE;
                                                    break;
                                                } else { // Its something else..
                                                    headerState = HeaderState._03_TYPE;
                                                }
                                            } else { // Its something else..
                                                headerState = HeaderState._03_TYPE;
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
                                                        registerHint(gd, lineNumber, "21", I18n.DATPARSER_DuplicatedAuthor, registered, allHints); //$NON-NLS-1$
                                                    } else {
                                                        if (headerState > HeaderState._02_AUTHOR) { // Its misplaced
                                                            registerHint(gd, lineNumber, "22", I18n.DATPARSER_MisplacedAuthor, registered, allHints); //$NON-NLS-1$
                                                        }
                                                        h.setLineAUTHOR(lineNumber);
                                                        h.setHasAUTHOR(true);
                                                        headerState = HeaderState._03_TYPE;
                                                    }
                                                    break;
                                                }
                                            }
                                        }

                                        // HeaderState._03_TYPE
                                        if (headerState == HeaderState._03_TYPE) {
                                            // I expect that this line is a valid Type
                                            if ("0 !LDRAW_ORG Unofficial_Part".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Subpart".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Primitive".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_48_Primitive".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_8_Primitive".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Shortcut".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Shortcut Alias".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Shortcut Physical_Colour".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Shortcut Physical_Colour Alias".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Part Alias".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Part Physical_Colour".equals(normalizedLine)) { //$NON-NLS-1$
                                                h.setLineTYPE(lineNumber);
                                                h.setHasTYPE(true);
                                                headerState = HeaderState._04_LICENSE;
                                                break;
                                            } else { // Its something else..
                                                headerState = HeaderState._04_LICENSE;
                                            }
                                        } else {
                                            // I don't expect that this line is a valid Type
                                            if ("0 !LDRAW_ORG Unofficial_Part".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Subpart".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Primitive".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_8_Primitive".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_48_Primitive".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Shortcut".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Shortcut Alias".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Shortcut Physical_Colour".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Shortcut Physical_Colour Alias".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Part Alias".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LDRAW_ORG Unofficial_Part Physical_Colour".equals(normalizedLine)) { //$NON-NLS-1$
                                                // Its duplicated
                                                if (h.hasTYPE()) {
                                                    registerHint(gd, lineNumber, "31", I18n.DATPARSER_DuplicatedType, registered, allHints); //$NON-NLS-1$
                                                } else {
                                                    if (headerState > HeaderState._03_TYPE) { // Its misplaced
                                                        registerHint(gd, lineNumber, "32", I18n.DATPARSER_MisplacedType, registered, allHints); //$NON-NLS-1$
                                                    }
                                                    h.setLineTYPE(lineNumber);
                                                    h.setHasTYPE(true);
                                                    headerState = HeaderState._04_LICENSE;
                                                }
                                                break;
                                            }
                                        }

                                        // HeaderState._04_LICENSE
                                        if (headerState == HeaderState._04_LICENSE) {
                                            // I expect that this line is a valid License
                                            if ("0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LICENSE Not redistributable : see NonCAreadme.txt".equals(normalizedLine)) { //$NON-NLS-1$
                                                h.setLineLICENSE(lineNumber);
                                                h.setHasLICENSE(true);
                                                headerState = HeaderState._05o_HELP;
                                                break;
                                            } else { // Its something else..
                                                headerState = HeaderState._05o_HELP;
                                            }
                                        } else {
                                            // I don't expect that this line is a valid License
                                            if ("0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 !LICENSE Not redistributable : see NonCAreadme.txt".equals(normalizedLine)) { //$NON-NLS-1$
                                                // Its duplicated
                                                if (h.hasLICENSE()) {
                                                    registerHint(gd, lineNumber, "41", I18n.DATPARSER_DuplicatedLicense, registered, allHints); //$NON-NLS-1$
                                                } else {
                                                    if (headerState > HeaderState._04_LICENSE) { // Its misplaced
                                                        registerHint(gd, lineNumber, "42", I18n.DATPARSER_MisplacedLicense, registered, allHints); //$NON-NLS-1$
                                                    }
                                                    h.setLineLICENSE(lineNumber);
                                                    h.setHasLICENSE(true);
                                                    headerState = HeaderState._05o_HELP;
                                                }
                                                break;
                                            }
                                        }

                                        // HeaderState._05o_HELP
                                        if (headerState == HeaderState._05o_HELP) {
                                            // I expect that this line is a valid Help
                                            if (normalizedLine.startsWith("0 !HELP")) { //$NON-NLS-1$
                                                if (h.hasHELP()) {
                                                    h.setLineHELP_end(lineNumber);
                                                } else {
                                                    h.setLineHELP_start(lineNumber);
                                                }
                                                h.setHasHELP(true);
                                                headerState = HeaderState._05o_HELP;
                                                break;
                                            } else { // Its something else..
                                                headerState = HeaderState._06_BFC;
                                            }
                                        } else {
                                            // I don't expect that this line is a valid Help
                                            if (normalizedLine.startsWith("0 !HELP")) { //$NON-NLS-1$
                                                // Its duplicated
                                                if (h.hasHELP()) {
                                                    registerHint(gd, lineNumber, "51", I18n.DATPARSER_SplitHelp, registered, allHints); //$NON-NLS-1$
                                                } else {
                                                    if (headerState > HeaderState._05o_HELP) { // Its misplaced
                                                        registerHint(gd, lineNumber, "52", I18n.DATPARSER_MisplacedHelp, registered, allHints); //$NON-NLS-1$
                                                    }
                                                    h.setLineHELP_start(lineNumber);
                                                    h.setHasHELP(true);
                                                    headerState = HeaderState._05o_HELP;
                                                }
                                                break;
                                            }
                                        }

                                        // HeaderState._06_BFC
                                        if (headerState == HeaderState._06_BFC) {
                                            // I expect that this line is a valid BFC Statement
                                            if ("0 BFC CERTIFY CCW".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 BFC CERTIFY CW".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 BFC NOCERTIFY".equals(normalizedLine)) { //$NON-NLS-1$
                                                h.setLineBFC(lineNumber);
                                                h.setHasBFC(true);
                                                headerState = HeaderState._07o_CATEGORY;
                                                break;
                                            } else { // Its something else..
                                                headerState = HeaderState._07o_CATEGORY;
                                            }
                                        } else {
                                            // I don't expect that this line is a valid BFC Statement
                                            if ("0 BFC CERTIFY CCW".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 BFC CERTIFY CW".equals(normalizedLine) //$NON-NLS-1$
                                                    || "0 BFC NOCERTIFY".equals(normalizedLine)) { //$NON-NLS-1$
                                                // Its duplicated
                                                if (h.hasBFC()) {
                                                    registerHint(gd, lineNumber, "61", I18n.DATPARSER_DuplicatedBFC, registered, allHints); //$NON-NLS-1$
                                                } else {
                                                    if (headerState > HeaderState._06_BFC) { // Its misplaced
                                                        registerHint(gd, lineNumber, "62", I18n.DATPARSER_MisplacedBFC0, registered, allHints); //$NON-NLS-1$
                                                    }
                                                    h.setLineBFC(lineNumber);
                                                    h.setHasBFC(true);
                                                    headerState = HeaderState._07o_CATEGORY;
                                                }
                                                break;
                                            }
                                        }

                                        // HeaderState._07o_CATEGORY
                                        if (headerState == HeaderState._07o_CATEGORY) {
                                            // I expect that this line is a valid Category
                                            if (normalizedLine.startsWith("0 !CATEGORY ")) { //$NON-NLS-1$
                                                h.setHasCATEGORY(true);
                                                headerState = HeaderState._08o_KEYWORDS;
                                                break;
                                            } else { // Its something else..
                                                headerState = HeaderState._08o_KEYWORDS;
                                            }
                                        } else {
                                            // I don't expect that this line is a valid Category
                                            if (normalizedLine.startsWith("0 !CATEGORY ")) { //$NON-NLS-1$
                                                // Its duplicated
                                                if (h.hasCATEGORY()) {
                                                    registerHint(gd, lineNumber, "71", I18n.DATPARSER_DuplicatedCategory, registered, allHints); //$NON-NLS-1$
                                                } else {
                                                    if (headerState > HeaderState._07o_CATEGORY) { // Its misplaced
                                                        registerHint(gd, lineNumber, "72", I18n.DATPARSER_MisplacedCategory, registered, allHints); //$NON-NLS-1$
                                                    }
                                                    h.setHasCATEGORY(true);
                                                    headerState = HeaderState._08o_KEYWORDS;
                                                }
                                                break;
                                            }
                                        }

                                        // HeaderState._08o_KEYWORDS
                                        if (headerState == HeaderState._08o_KEYWORDS) {
                                            // I expect that this line is a valid Keyword
                                            if (normalizedLine.startsWith("0 !KEYWORDS ")) { //$NON-NLS-1$
                                                h.setHasHELP(true);
                                                headerState = HeaderState._08o_KEYWORDS;
                                                break;
                                            } else { // Its something else..
                                                headerState = HeaderState._09o_CMDLINE;
                                            }
                                        } else {
                                            // I don't expect that this line is a valid Keyword
                                            if (normalizedLine.startsWith("0 !KEYWORDS ")) { //$NON-NLS-1$
                                                // Its duplicated
                                                if (h.hasKEYWORDS()) {
                                                    registerHint(gd, lineNumber, "81", I18n.DATPARSER_SplitKeyword, registered, allHints); //$NON-NLS-1$
                                                } else {
                                                    if (headerState > HeaderState._08o_KEYWORDS) { // Its misplaced
                                                        registerHint(gd, lineNumber, "82", I18n.DATPARSER_MisplacedKeyword, registered, allHints); //$NON-NLS-1$
                                                    }
                                                    h.setHasKEYWORDS(true);
                                                    headerState = HeaderState._08o_KEYWORDS;
                                                }
                                                break;
                                            }
                                        }

                                        // HeaderState._09o_CMDLINE
                                        if (headerState == HeaderState._09o_CMDLINE) {
                                            // I expect that this line is a valid Command Line
                                            if (normalizedLine.startsWith("0 !CMDLINE ")) { //$NON-NLS-1$
                                                h.setHasCMDLINE(true);
                                                headerState = HeaderState._10o_HISTORY;
                                                break;
                                            } else { // Its something else..
                                                headerState = HeaderState._10o_HISTORY;
                                            }
                                        } else {
                                            // I don't expect that this line is a valid Command Line
                                            if (normalizedLine.startsWith("0 !CMDLINE ")) { //$NON-NLS-1$
                                                // Its duplicated
                                                if (h.hasCMDLINE()) {
                                                    registerHint(gd, lineNumber, "91", I18n.DATPARSER_DuplicatedCommandLine, registered, allHints); //$NON-NLS-1$
                                                } else {
                                                    if (headerState > HeaderState._09o_CMDLINE) { // Its misplaced
                                                        registerHint(gd, lineNumber, "92", I18n.DATPARSER_MisplacedCommandLine, registered, allHints); //$NON-NLS-1$
                                                    }
                                                    h.setHasCMDLINE(true);
                                                    headerState = HeaderState._10o_HISTORY;
                                                }
                                                break;
                                            }
                                        }

                                        // HeaderState._10o_HISTORY TODO Needs better validation
                                        if (headerState == HeaderState._10o_HISTORY) {
                                            // I expect that this line is a valid History Entry
                                            if (normalizedLine.startsWith("0 !HISTORY ") && normalizedLine.length() > 20) { //$NON-NLS-1$
                                                if (h.hasHISTORY()) {
                                                    final String lh = h.getLastHistoryEntry();
                                                    if (lh != null && normalizedLine.substring(0, "0 !HISTORY YYYY-MM-DD".length()).compareTo(lh) == -1) { //$NON-NLS-1$
                                                        registerHint(gd, lineNumber, "A3", I18n.DATPARSER_HistoryWrongOrder, registered, allHints); //$NON-NLS-1$
                                                    }
                                                } else {
                                                    h.setLastHistoryEntry(normalizedLine.substring(0, "0 !HISTORY YYYY-MM-DD".length())); //$NON-NLS-1$
                                                }
                                                h.setHasHISTORY(true);
                                                headerState = HeaderState._10o_HISTORY;
                                                break;
                                            } else { // Its something else..
                                                headerState = HeaderState._11o_COMMENT;
                                            }
                                        } else {
                                            // I don't expect that this line is a valid History Entry
                                            if (normalizedLine.startsWith("0 !HISTORY ")) { //$NON-NLS-1$
                                                // Its duplicated
                                                if (h.hasHISTORY()) {
                                                    registerHint(gd, lineNumber, "A1", I18n.DATPARSER_SplitHistory, registered, allHints); //$NON-NLS-1$
                                                } else {
                                                    if (headerState > HeaderState._10o_HISTORY) { // Its misplaced
                                                        registerHint(gd, lineNumber, "A2", I18n.DATPARSER_MisplacedHistory, registered, allHints); //$NON-NLS-1$
                                                    }
                                                    h.setHasHISTORY(true);
                                                    headerState = HeaderState._10o_HISTORY;
                                                }
                                                break;
                                            }
                                        }

                                        // HeaderState._11o_COMMENT
                                        if (headerState == HeaderState._11o_COMMENT) {
                                            // I expect that this line is a valid Comment
                                            if (normalizedLine.startsWith("0 // ")) { //$NON-NLS-1$
                                                h.setHasCOMMENT(true);
                                                headerState = HeaderState._11o_COMMENT;
                                                break;
                                            } else {// Its something else..
                                                headerState = HeaderState._12o_BFC2;
                                            }
                                        } else {
                                            // I don't expect that this line is a valid Comment
                                            if (normalizedLine.startsWith("0 // ")) { //$NON-NLS-1$
                                                // Its duplicated
                                                if (h.hasCOMMENT()) {
                                                    registerHint(gd, lineNumber, "B1", I18n.DATPARSER_SplitCommment, registered, allHints); //$NON-NLS-1$
                                                } else {
                                                    if (headerState > HeaderState._11o_COMMENT) { // Its misplaced
                                                        registerHint(gd, lineNumber, "B2", I18n.DATPARSER_MisplacedComment, registered, allHints); //$NON-NLS-1$
                                                    }
                                                    h.setHasCOMMENT(true);
                                                    headerState = HeaderState._11o_COMMENT;
                                                }
                                                break;
                                            }
                                        }

                                        // HeaderState._12o_BFC2
                                        if (headerState == HeaderState._12o_BFC2) {
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
                                                headerState = HeaderState._12o_BFC2;
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
                                                    registerHint(gd, lineNumber, "C1", I18n.DATPARSER_SplitBFC, registered, allHints); //$NON-NLS-1$
                                                } else {
                                                    if (headerState > HeaderState._12o_BFC2) { // Its misplaced
                                                        registerHint(gd, lineNumber, "C2", I18n.DATPARSER_MisplacedBFC, registered, allHints); //$NON-NLS-1$
                                                    }
                                                    h.setHasBFC2(true);
                                                    headerState = HeaderState._12o_BFC2;
                                                }
                                                break;
                                            }
                                        }

                                        if (h.hasTITLE()) {
                                            // registerHint(gd, lineNumber, "01", I18n.DATPARSER_InvalidHeader, registered); //$NON-NLS-1$
                                        } else {
                                            h.setLineTITLE(lineNumber);
                                            h.setHasTITLE(true);
                                            if (headerState != HeaderState._00_TITLE) {
                                                registerHint(gd, lineNumber, "02", I18n.DATPARSER_MisplacedTitle, registered, allHints); //$NON-NLS-1$
                                            } else {
                                                headerState = HeaderState._01_NAME;
                                            }
                                        }
                                        break;
                                    }
                                }

                                if (firstEntry != null) {
                                    registered[0] = false;
                                    if (!h.hasTITLE()) {
                                        registerHint(firstEntry, -6, "00", I18n.DATFILE_MissingTitle, registered, allHints); //$NON-NLS-1$
                                    }
                                    if (!h.hasNAME()) {
                                        registerHint(firstEntry, -5, "10", I18n.DATFILE_MissingFileName, registered, allHints); //$NON-NLS-1$
                                    }
                                    if (!h.hasAUTHOR()) {
                                        registerHint(firstEntry, -4, "20", I18n.DATFILE_MissingAuthor, registered, allHints); //$NON-NLS-1$
                                    }
                                    if (!h.hasTYPE()) {
                                        registerHint(firstEntry, -3, "30", I18n.DATFILE_MissingPartType, registered, allHints); //$NON-NLS-1$
                                    }
                                    if (!h.hasLICENSE()) {
                                        registerHint(firstEntry, -2, "40", I18n.DATFILE_MissingLicense, registered, allHints); //$NON-NLS-1$
                                    }
                                    if (!h.hasBFC()) {
                                        registerHint(firstEntry, -1, "60", I18n.DATFILE_MissingBFC, registered, allHints); //$NON-NLS-1$
                                    }
                                }

                                int firstKey = CACHE_headerHints.isEmpty() ? Integer.MAX_VALUE : CACHE_headerHints.firstKey();
                                firstKey -= 1;
                                CACHE_headerHints.put(firstKey, allHints);
                                if (CACHE_headerHints.size() > 3) {
                                    CACHE_headerHints.remove(CACHE_headerHints.lastKey());
                                }

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                }

                                Display.getDefault().asyncExec(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if (df.updateDatHeaderHints(styledText_Composite, hints)) {
                                                int errorCount = treeItem_Errors.getItems().size();
                                                int warningCount = treeItem_Warnings.getItems().size();
                                                int hintCount = treeItem_Hints.getItems().size();
                                                int duplicateCount = treeItem_Duplicates.getItems().size();
                                                String errors = errorCount == 1 ? I18n.EDITORTEXT_Error : I18n.EDITORTEXT_Errors;
                                                String warnings = warningCount == 1 ? I18n.EDITORTEXT_Warning : I18n.EDITORTEXT_Warnings;
                                                String hints = hintCount == 1 ? I18n.EDITORTEXT_Other : I18n.EDITORTEXT_Others;
                                                String duplicates = duplicateCount == 1 ? I18n.EDITORTEXT_Duplicate : I18n.EDITORTEXT_Duplicates;
                                                lbl_ProblemCount.setText(errorCount + " " + errors + ", " + warningCount + " " + warnings + ", " + hintCount + " " + hints + ", " + duplicateCount + " " + duplicates); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                                                lbl_ProblemCount.setText("TEST"); //$NON-NLS-1$
                                                treeItem_Hints.getParent().build();
                                                lbl_ProblemCount.getParent().layout();
                                                lbl_ProblemCount.getParent().redraw();
                                                lbl_ProblemCount.getParent().update();
                                            }
                                        } catch (Exception ex) {
                                            // The text editor widget could be disposed
                                            NLogger.debug(getClass(), ex);
                                        }
                                    }
                                });
                            }
                            if (workQueue.isEmpty()) Thread.sleep(200);
                        } catch (InterruptedException e) {
                            // We want to know what can go wrong here
                            // because it SHOULD be avoided!!

                            NLogger.error(getClass(), "The DatHeaderManager cycle was interruped [InterruptedException]! :("); //$NON-NLS-1$
                            NLogger.error(getClass(), e);
                        } catch (Exception e) {
                            NLogger.error(getClass(), "The DatHeaderManager cycle was throwing an exception :("); //$NON-NLS-1$
                            NLogger.error(getClass(), e);
                        }
                    }
                    CACHE_headerHints.clear();
                }

                private void registerHint(GData gd, int lineNumber, String errno, String message, boolean[] registered, ArrayList<ParsingResult> allHints) {
                    registerHint(gd, lineNumber, errno, new Object[]{}, message, registered, allHints);
                }

                private void registerHint(GData gd, int lineNumber, String errno, Object[] args, String message, boolean[] registered, ArrayList<ParsingResult> allHints) {
                    registered[0] = true;
                    Object[] messageArguments = args;
                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                    formatter.setLocale(MyLanguage.LOCALE);
                    formatter.applyPattern(message);
                    allHints.add(new ParsingResult(formatter.format(messageArguments), "[H" + errno + "] " + I18n.DATPARSER_HeaderHint, lineNumber)); //$NON-NLS-1$ //$NON-NLS-2$
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

            }).start();
        }

        while (!workQueue.offer(new Object[]{data, hints, warnings, errors, duplicates, compositeText, problemCount})) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {}
        }
    }

    public void deleteHeaderHints() {
        isRunning.set(false);
    }

    public void setDatFile(DatFile df) {
        this.df = df;
    }

    public HeaderState getState() {
        return state;
    }
}

/*
 result.add(new ParsingResult(I18n.DATPARSER_MultipleBFC, "[W01] " + I18n.DATPARSER_Warning, ResultType.WARN)); //$NON-NLS-1$
 result.add(new ParsingResult(new GData0(line)));
 */

/*

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

                if (!line.trim().startsWith("0")) { //$NON-NLS-1$
                    HeaderState.state().setState(HeaderState._99_DONE);
                    break;
                }

                results = DatParser.parseLine(line, lineNumber, 0, 0f, 0f, 0f, 1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, this, true, alreadyParsed, false);
                for (ParsingResult result : results) {
                    if (result.getTypeNumber() == ResultType.HINT) {

                        Object[] messageArguments = {lineNumber, position};
                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                        formatter.setLocale(MyLanguage.LOCALE);
                        formatter.applyPattern(I18n.DATFILE_Line);

                        TreeItem trtmNewTreeitem = new TreeItem(hints, SWT.NONE);
                        trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
                        trtmNewTreeitem.setVisible(false);
                        trtmNewTreeitem.setText(new String[] { result.getMessage(), formatter.format(messageArguments), result.getType() });
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
                results.add(new ParsingResult(I18n.DATFILE_MissingTitle, "[H00] " + I18n.DATFILE_HeaderHint, ResultType.HINT)); //$NON-NLS-1$
            if (!h.hasNAME())
                results.add(new ParsingResult(I18n.DATFILE_MissingFileName, "[H10] " + I18n.DATFILE_HeaderHint, ResultType.HINT)); //$NON-NLS-1$
            if (!h.hasAUTHOR())
                results.add(new ParsingResult(I18n.DATFILE_MissingAuthor, "[H20] " + I18n.DATFILE_HeaderHint, ResultType.HINT)); //$NON-NLS-1$
            if (!h.hasTYPE())
                results.add(new ParsingResult(I18n.DATFILE_MissingPartType, "[H30] " + I18n.DATFILE_HeaderHint, ResultType.HINT)); //$NON-NLS-1$
            if (!h.hasLICENSE())
                results.add(new ParsingResult(I18n.DATFILE_MissingLicense, "[H40] " + I18n.DATFILE_HeaderHint, ResultType.HINT)); //$NON-NLS-1$
            if (!h.hasBFC())
                results.add(new ParsingResult(I18n.DATFILE_MissingBFC, "[H60] " + I18n.DATFILE_HeaderHint, ResultType.HINT)); //$NON-NLS-1$

            int fakeLine = -6;
            for (ParsingResult result : results) {
                TreeItem trtmNewTreeitem = new TreeItem(hints, SWT.NONE);
                trtmNewTreeitem.setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
                trtmNewTreeitem.setText(new String[] { result.getMessage(), "---", result.getType() }); //$NON-NLS-1$
                trtmNewTreeitem.setData(fakeLine);
                trtmNewTreeitem.setVisible(false);
                fakeLine++;
            }
        }
        hints.sortItems();
        HeaderState.state().setState(HeaderState._99_DONE);
        NLogger.debug(getClass(), "Total time to parse header: {0} ms", System.currentTimeMillis() - start); //$NON-NLS-1$

    }
 */
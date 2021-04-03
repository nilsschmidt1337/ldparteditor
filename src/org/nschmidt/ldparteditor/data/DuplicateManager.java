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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.helpers.LDPartEditorException;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

public class DuplicateManager {

    private DatFile df;

    private boolean hasNoThread = true;
    private volatile AtomicBoolean isRunning = new AtomicBoolean(true);
    private Thread worker = null;

    private volatile Queue<GData> workQueue = new ConcurrentLinkedQueue<>();

    DuplicateManager(DatFile df) {
        this.df = df;
    }

    public void pushDuplicateCheck(GData data) {
        if (df.isReadOnly()) return;
        if (hasNoThread || !worker.isAlive()) {
            hasNoThread = false;
            worker = new Thread(new Runnable() {
                @Override
                public void run() {

                    while (isRunning.get() && Editor3DWindow.getAlive().get()) {
                        try {
                            GData newEntry = workQueue.poll();
                            if (newEntry != null) {
                                NLogger.debug(getClass(), "Started duplicate check..."); //$NON-NLS-1$

                                final HashSet<GData> allKeys = new HashSet<>();
                                String lastCommentLine = null;
                                final HashMap<String, Integer> lines = new HashMap<>();

                                allKeys.addAll(GData.CACHE_duplicates.threadsafeKeySet());
                                GData gd = newEntry;
                                int lineNumber = 1;
                                while ((gd = gd.next) != null) {
                                    boolean[] registered = new boolean[]{false};
                                    int type = gd.type();
                                    String trimmedLine = gd.toString().trim();
                                    String[] data_segments = trimmedLine.split("\\s+"); //$NON-NLS-1$

                                    // Remove double spaces (essential for complex types)
                                    String normalizedLine;
                                    if (type > 6 || type < 2) {
                                        StringBuilder normalized = new StringBuilder();
                                        int i = 0;
                                        for (String string : data_segments) {
                                            if (i != 1) {
                                                normalized.append(string);
                                                normalized.append(" "); //$NON-NLS-1$
                                            }
                                            i++;
                                        }
                                        normalizedLine = normalized.toString().trim();
                                    } else {
                                        normalizedLine = trimmedLine;
                                    }

                                    switch (type) {
                                    case 0:
                                        break;
                                    case 1:

                                        GData invertNextData = gd.getBefore();
                                        normalizedLine = normalizedLine + ((GData1) gd).shortName;
                                        // Check if a INVERTNEXT is present
                                        while (invertNextData != null && invertNextData.type() != 1 && (invertNextData.type() != 6 || ((GDataBFC) invertNextData).type != BFC.INVERTNEXT)) {
                                            invertNextData = invertNextData.getBefore();
                                        }
                                        if (invertNextData != null && invertNextData.type() == 6) {
                                            normalizedLine = "IV" + normalizedLine; //$NON-NLS-1$
                                            if (lines.containsKey(normalizedLine)) {
                                                lastCommentLine = "0 BFC INVERTNEXT"; //$NON-NLS-1$
                                                registerDuplicate(gd, lines.get(normalizedLine), registered);
                                            } else {
                                                lastCommentLine = null;
                                                lines.put(normalizedLine, lineNumber);
                                            }
                                        } else {
                                            normalizedLine = "NIV" + normalizedLine; //$NON-NLS-1$
                                            if (lines.containsKey(normalizedLine)) {
                                                registerDuplicate(gd, lines.get(normalizedLine), registered);
                                            } else {
                                                lastCommentLine = null;
                                                lines.put(normalizedLine, lineNumber);
                                            }
                                        }
                                        break;
                                    case 2:
                                    {
                                        if (!((GData2) gd).isLine) {
                                            break;
                                        }
                                        if (lines.containsKey(normalizedLine)) {
                                            registerDuplicate(gd, lines.get(normalizedLine), registered);
                                        } else {
                                            lines.put(normalizedLine, lineNumber);
                                            StringBuilder normalized1 = new StringBuilder();

                                            normalized1.append(data_segments[0]);
                                            normalized1.append(" "); //$NON-NLS-1$

                                            normalized1.append(data_segments[2]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[3]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[4]);
                                            normalized1.append(" "); //$NON-NLS-1$

                                            normalized1.append(data_segments[5]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[6]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[7]);

                                            String normalizedLine1 = normalized1.toString().trim();

                                            if (lines.containsKey(normalizedLine1)) {
                                                registerDuplicate(gd, lines.get(normalizedLine1), registered);
                                            } else {
                                                lines.put(normalizedLine1, lineNumber);
                                                lastCommentLine = null;

                                                StringBuilder normalized2 = new StringBuilder();
                                                normalized2.append(data_segments[0]);
                                                normalized2.append(" "); //$NON-NLS-1$

                                                normalized2.append(data_segments[5]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[6]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[7]);
                                                normalized2.append(" "); //$NON-NLS-1$

                                                normalized2.append(data_segments[2]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[3]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[4]);

                                                String normalizedLine2 = normalized2.toString().trim();

                                                if (lines.containsKey(normalizedLine2)) {
                                                    registerDuplicate(gd, lines.get(normalizedLine2), registered);
                                                } else {
                                                    lines.put(normalizedLine2, lineNumber);
                                                    lastCommentLine = null;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                    case 3:
                                    {
                                        if (!((GData3) gd).isTriangle) {
                                            break;
                                        }
                                        if (lines.containsKey(normalizedLine)) {
                                            registerDuplicate(gd, lines.get(normalizedLine), registered);
                                        } else {
                                            lines.put(normalizedLine, lineNumber);
                                            StringBuilder normalized1 = new StringBuilder();

                                            normalized1.append(data_segments[0]);
                                            normalized1.append(" "); //$NON-NLS-1$

                                            normalized1.append(data_segments[2]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[3]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[4]);
                                            normalized1.append(" "); //$NON-NLS-1$

                                            normalized1.append(data_segments[5]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[6]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[7]);
                                            normalized1.append(" "); //$NON-NLS-1$

                                            normalized1.append(data_segments[8]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[9]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[10]);

                                            String normalizedLine1 = normalized1.toString().trim();

                                            if (lines.containsKey(normalizedLine1)) {
                                                registerDuplicate(gd, lines.get(normalizedLine1), registered);
                                            } else {
                                                lines.put(normalizedLine1, lineNumber);
                                                StringBuilder normalized2 = new StringBuilder();

                                                normalized2.append(data_segments[0]);
                                                normalized2.append(" "); //$NON-NLS-1$

                                                normalized2.append(data_segments[8]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[9]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[10]);
                                                normalized2.append(" "); //$NON-NLS-1$

                                                normalized2.append(data_segments[2]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[3]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[4]);
                                                normalized2.append(" "); //$NON-NLS-1$

                                                normalized2.append(data_segments[5]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[6]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[7]);

                                                String normalizedLine2 = normalized2.toString().trim();

                                                if (lines.containsKey(normalizedLine2)) {
                                                    registerDuplicate(gd, lines.get(normalizedLine2), registered);
                                                } else {
                                                    lines.put(normalizedLine2, lineNumber);
                                                    StringBuilder normalized3 = new StringBuilder();

                                                    normalized3.append(data_segments[0]);
                                                    normalized3.append(" "); //$NON-NLS-1$

                                                    normalized3.append(data_segments[5]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[6]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[7]);
                                                    normalized3.append(" "); //$NON-NLS-1$

                                                    normalized3.append(data_segments[8]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[9]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[10]);
                                                    normalized3.append(" "); //$NON-NLS-1$

                                                    normalized3.append(data_segments[2]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[3]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[4]);

                                                    String normalizedLine3 = normalized3.toString().trim();
                                                    if (lines.containsKey(normalizedLine3)) {
                                                        registerDuplicate(gd, lines.get(normalizedLine3), registered);
                                                    } else {
                                                        lines.put(normalizedLine3, lineNumber);
                                                        lastCommentLine = null;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    break;
                                    case 4:
                                    {
                                        if (lines.containsKey(normalizedLine)) {
                                            registerDuplicate(gd, lineNumber, registered);
                                        } else {
                                            lines.put(normalizedLine, lineNumber);
                                            StringBuilder normalized1 = new StringBuilder();

                                            normalized1.append(data_segments[0]);
                                            normalized1.append(" "); //$NON-NLS-1$

                                            normalized1.append(data_segments[2]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[3]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[4]);
                                            normalized1.append(" "); //$NON-NLS-1$

                                            normalized1.append(data_segments[5]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[6]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[7]);
                                            normalized1.append(" "); //$NON-NLS-1$

                                            normalized1.append(data_segments[8]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[9]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[10]);
                                            normalized1.append(" "); //$NON-NLS-1$

                                            normalized1.append(data_segments[11]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[12]);
                                            normalized1.append(" "); //$NON-NLS-1$
                                            normalized1.append(data_segments[13]);

                                            String normalizedLine1 = normalized1.toString().trim();

                                            if (lines.containsKey(normalizedLine1)) {
                                                registerDuplicate(gd, lines.get(normalizedLine1), registered);
                                            } else {
                                                lines.put(normalizedLine1, lineNumber);
                                                StringBuilder normalized2 = new StringBuilder();

                                                normalized2.append(data_segments[0]);
                                                normalized2.append(" "); //$NON-NLS-1$

                                                normalized2.append(data_segments[11]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[12]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[13]);
                                                normalized2.append(" "); //$NON-NLS-1$

                                                normalized2.append(data_segments[2]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[3]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[4]);
                                                normalized2.append(" "); //$NON-NLS-1$

                                                normalized2.append(data_segments[5]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[6]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[7]);
                                                normalized2.append(" "); //$NON-NLS-1$

                                                normalized2.append(data_segments[8]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[9]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[10]);

                                                String normalizedLine2 = normalized2.toString().trim();

                                                if (lines.containsKey(normalizedLine2)) {
                                                    registerDuplicate(gd, lines.get(normalizedLine2), registered);
                                                } else {
                                                    lines.put(normalizedLine2, lineNumber);
                                                    StringBuilder normalized3 = new StringBuilder();

                                                    normalized3.append(data_segments[0]);
                                                    normalized3.append(" "); //$NON-NLS-1$

                                                    normalized3.append(data_segments[8]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[9]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[10]);
                                                    normalized3.append(" "); //$NON-NLS-1$

                                                    normalized3.append(data_segments[11]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[12]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[13]);
                                                    normalized3.append(" "); //$NON-NLS-1$

                                                    normalized3.append(data_segments[2]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[3]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[4]);
                                                    normalized3.append(" "); //$NON-NLS-1$

                                                    normalized3.append(data_segments[5]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[6]);
                                                    normalized3.append(" "); //$NON-NLS-1$
                                                    normalized3.append(data_segments[7]);

                                                    String normalizedLine3 = normalized3.toString().trim();
                                                    if (lines.containsKey(normalizedLine3)) {
                                                        registerDuplicate(gd, lines.get(normalizedLine3), registered);
                                                    } else {
                                                        lines.put(normalizedLine3, lineNumber);
                                                        StringBuilder normalized4 = new StringBuilder();

                                                        normalized4.append(data_segments[0]);
                                                        normalized4.append(" "); //$NON-NLS-1$

                                                        normalized4.append(data_segments[5]);
                                                        normalized4.append(" "); //$NON-NLS-1$
                                                        normalized4.append(data_segments[6]);
                                                        normalized4.append(" "); //$NON-NLS-1$
                                                        normalized4.append(data_segments[7]);
                                                        normalized4.append(" "); //$NON-NLS-1$

                                                        normalized4.append(data_segments[8]);
                                                        normalized4.append(" "); //$NON-NLS-1$
                                                        normalized4.append(data_segments[9]);
                                                        normalized4.append(" "); //$NON-NLS-1$
                                                        normalized4.append(data_segments[10]);
                                                        normalized4.append(" "); //$NON-NLS-1$

                                                        normalized4.append(data_segments[11]);
                                                        normalized4.append(" "); //$NON-NLS-1$
                                                        normalized4.append(data_segments[12]);
                                                        normalized4.append(" "); //$NON-NLS-1$
                                                        normalized4.append(data_segments[13]);
                                                        normalized4.append(" "); //$NON-NLS-1$

                                                        normalized4.append(data_segments[2]);
                                                        normalized4.append(" "); //$NON-NLS-1$
                                                        normalized4.append(data_segments[3]);
                                                        normalized4.append(" "); //$NON-NLS-1$
                                                        normalized4.append(data_segments[4]);

                                                        String normalizedLine4 = normalized4.toString().trim();
                                                        if (lines.containsKey(normalizedLine4)) {
                                                            registerDuplicate(gd, lines.get(normalizedLine4), registered);
                                                        } else {
                                                            lines.put(normalizedLine4, lineNumber);
                                                            lastCommentLine = null;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    break;
                                    case 5:
                                    {
                                        GData5 gd5 = (GData5) gd;

                                        Vector4f p1 = MathHelper.getNearestPointToLine(gd5.x1, gd5.y1, gd5.z1, gd5.x2, gd5.y2, gd5.z2, gd5.x3, gd5.y3, gd5.z3);
                                        Vector4f p2 = MathHelper.getNearestPointToLine(gd5.x1, gd5.y1, gd5.z1, gd5.x2, gd5.y2, gd5.z2, gd5.x4, gd5.y4, gd5.z4);

                                        Vector3f pa1 = Vector3f.sub(new Vector3f(gd5.x3, gd5.y3, gd5.z3), new Vector3f(p1.x, p1.y, p1.z), null);
                                        Vector3f pa2 = Vector3f.sub(new Vector3f(gd5.x4, gd5.y4, gd5.z4), new Vector3f(p2.x, p2.y, p2.z), null);

                                        float a = (float) (Vector3f.angle(pa1, pa2) / Math.PI * 180.0);


                                        if (a > Threshold.condline_angle_maximum) {
                                            registerInvisibleCondline(gd, registered);
                                        } else {
                                            StringBuilder normalized3 = new StringBuilder();

                                            normalized3.append(data_segments[0]);
                                            normalized3.append(" "); //$NON-NLS-1$

                                            normalized3.append(data_segments[2]);
                                            normalized3.append(" "); //$NON-NLS-1$
                                            normalized3.append(data_segments[3]);
                                            normalized3.append(" "); //$NON-NLS-1$
                                            normalized3.append(data_segments[4]);
                                            normalized3.append(" "); //$NON-NLS-1$

                                            normalized3.append(data_segments[5]);
                                            normalized3.append(" "); //$NON-NLS-1$
                                            normalized3.append(data_segments[6]);
                                            normalized3.append(" "); //$NON-NLS-1$
                                            normalized3.append(data_segments[7]);
                                            normalized3.append(" "); //$NON-NLS-1$

                                            String normalizedLine3 = normalized3.toString().trim();

                                            if (lines.containsKey(normalizedLine3)) {
                                                registerDuplicate(gd, lines.get(normalizedLine3), registered);
                                            } else {
                                                lines.put(normalizedLine3, lineNumber);
                                                StringBuilder normalized2 = new StringBuilder();

                                                normalized2.append(data_segments[0]);
                                                normalized2.append(" "); //$NON-NLS-1$

                                                normalized2.append(data_segments[5]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[6]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[7]);
                                                normalized2.append(" "); //$NON-NLS-1$

                                                normalized2.append(data_segments[2]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[3]);
                                                normalized2.append(" "); //$NON-NLS-1$
                                                normalized2.append(data_segments[4]);
                                                normalized2.append(" "); //$NON-NLS-1$

                                                String normalizedLine2 = normalized2.toString().trim();

                                                if (lines.containsKey(normalizedLine2)) {
                                                    registerDuplicate(gd, lines.get(normalizedLine2), registered);
                                                } else {
                                                    lines.put(normalizedLine2, lineNumber);
                                                    lastCommentLine = null;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                    default:
                                        if (!trimmedLine.isEmpty()) {
                                            if (lastCommentLine != null && lastCommentLine.equals(normalizedLine)) {
                                                registerDuplicate(gd, lineNumber - 1, registered);
                                            } else {
                                                lastCommentLine = normalizedLine;
                                            }
                                        }
                                        break;
                                    }
                                    if (registered[0]) {
                                        allKeys.remove(gd);
                                    }
                                    lineNumber += 1;
                                }

                                for (GData gd2 : allKeys) {
                                    GData.CACHE_duplicates.remove(gd2);
                                }

                            }
                            if (workQueue.isEmpty()) Thread.sleep(100);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new LDPartEditorException(ie);
                        } catch (Exception e) {
                            // We want to know what can go wrong here
                            // because it SHOULD be avoided!!
                            NLogger.error(getClass(), "The DuplicateManager cycle was throwing an exception :("); //$NON-NLS-1$
                            NLogger.error(getClass(), e);
                        }
                    }
                }

                private void registerDuplicate(GData gd, int lineNumber, boolean[] registered) {
                    registered[0] = true;
                    Object[] messageArguments = {lineNumber};
                    MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                    formatter.setLocale(MyLanguage.LOCALE);
                    formatter.applyPattern(I18n.DATPARSER_DUPLICATED_LINES);
                    GData.CACHE_duplicates.put(gd, new ParsingResult(formatter.format(messageArguments), "[E01] " + I18n.DATPARSER_LOGIC_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                }

                private void registerInvisibleCondline(GData gd, boolean[] registered) {
                    registered[0] = true;
                    GData.CACHE_duplicates.put(gd, new ParsingResult(I18n.DATPARSER_INVISIBLE_LINE, "[E01] " + I18n.DATPARSER_LOGIC_ERROR, ResultType.ERROR)); //$NON-NLS-1$
                }
            });
            worker.start();
        }

        while (!workQueue.offer(data)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }
        }
    }

    void deleteDuplicateInfo() {
        isRunning.set(false);
    }

    public void setDatFile(DatFile df) {
        this.df = df;
    }
}

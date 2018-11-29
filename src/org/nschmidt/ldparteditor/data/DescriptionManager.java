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

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.widgets.TreeItem;

public enum DescriptionManager {
    INSTANCE;

    private static Queue<TreeItem> workQueue = new ConcurrentLinkedQueue<TreeItem>();
    private static boolean hasNoThread = true;

    public static synchronized void registerDescription(TreeItem ti) {

        if (hasNoThread) {
            hasNoThread = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (Editor3DWindow.getAlive().get()) {
                        try {
                            final TreeItem newEntry = workQueue.poll();
                            if (newEntry != null) {
                                DatFile df = (DatFile) newEntry.getData();
                                NLogger.debug(getClass(), "Register description for {0}", df.getOldName()); //$NON-NLS-1$
                                UTF8BufferedReader reader = null;
                                final StringBuilder titleSb = new StringBuilder();
                                try {
                                    reader = new UTF8BufferedReader(df.getOldName());
                                    String title = reader.readLine();
                                    if (title != null) {
                                        title = title.trim();
                                        if (title.length() > 0) {
                                            titleSb.append(" -"); //$NON-NLS-1$
                                            titleSb.append(title.substring(1));
                                        }
                                    }
                                } catch (LDParsingException e) {
                                } catch (FileNotFoundException e) {
                                } catch (UnsupportedEncodingException e) {
                                } finally {
                                    try {
                                        if (reader != null)
                                            reader.close();
                                    } catch (LDParsingException e1) {
                                    }
                                }
                                String d = titleSb.toString();
                                newEntry.setText(df.getShortName() + d);
                                df.setDescription(d);
                            } else {
                                Thread.sleep(100);
                            }
                        } catch (InterruptedException e) {
                        } catch (Exception e) {
                            NLogger.debug(getClass(), e);
                        }
                    }
                }
            }).start();
        }

        workQueue.offer(ti);
    }
}

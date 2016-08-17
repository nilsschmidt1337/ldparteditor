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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

public class DuplicateManager {

    private DatFile df;
    
    private boolean hasNoThread = true;
    private volatile AtomicBoolean isRunning = new AtomicBoolean(true);

    private volatile Queue<GData> workQueue = new ConcurrentLinkedQueue<GData>();
    
    public DuplicateManager(DatFile df) {
        this.df = df;
    }
    
    public void pushDuplicateCheck(GData data) {
        if (df.isReadOnly()) return;
        if (hasNoThread) {
            hasNoThread = false;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    while (isRunning.get() && Editor3DWindow.getAlive().get()) {
                        try {
                            GData newEntry = workQueue.poll();
                            if (newEntry != null) {
                                NLogger.debug(getClass(), "Started duplicate check..."); //$NON-NLS-1$
                                
                                // FIXME Needs implementation!
                                
                                GData gd = newEntry;
                                int lineNumber = 1;
                                while ((gd = gd.next) != null) {
                                    if (Math.random() > 0.7) { // Fake "check" just for testing
                                        Object[] messageArguments = {lineNumber};
                                        MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
                                        formatter.setLocale(MyLanguage.LOCALE);
                                        formatter.applyPattern(I18n.DATPARSER_DuplicatedLines);
                                        GData.CACHE_duplicates.put(gd, new ParsingResult(formatter.format(messageArguments), "[E01] " + I18n.DATPARSER_LogicError, ResultType.ERROR)); //$NON-NLS-1$
                                    }
                                    lineNumber += 1;
                                }
                                     
                            }
                            if (workQueue.isEmpty()) Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // We want to know what can go wrong here
                            // because it SHOULD be avoided!!

                            NLogger.error(getClass(), "The DuplicateManager cycle was interruped [InterruptedException]! :("); //$NON-NLS-1$
                            NLogger.error(getClass(), e);
                        } catch (Exception e) {
                            NLogger.error(getClass(), "The DuplicateManager cycle was throwing an exception :("); //$NON-NLS-1$
                            NLogger.error(getClass(), e);
                        }
                    }
                }
            }).start();
        }

        while (!workQueue.offer(data)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {}
        }
    }
    
    public void deleteDuplicate() {
        isRunning.set(false);
    }
    
    public void setDatFile(DatFile df) {
        this.df = df;
    }
}

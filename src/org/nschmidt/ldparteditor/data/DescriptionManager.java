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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.widgets.TreeItem;

public enum DescriptionManager {
    INSTANCE;

    private Queue<TreeItem> workQueue = new ConcurrentLinkedQueue<TreeItem>();
    private boolean hasNoThread = true;

    public synchronized void registerDescription(TreeItem ti) {

        if (hasNoThread) {
            hasNoThread = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (Editor3DWindow.getAlive().get()) {
                        try {
                            TreeItem newEntry = workQueue.poll();
                            if (newEntry != null) {

                            } else {
                                if (workQueue.isEmpty()) Thread.sleep(100);
                            }
                        } catch (InterruptedException e) {
                        } catch (Exception e) {
                            NLogger.debug(getClass(), e);
                        }
                    }
                }
            });
        }

        while (!workQueue.offer(ti)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }

    }
}

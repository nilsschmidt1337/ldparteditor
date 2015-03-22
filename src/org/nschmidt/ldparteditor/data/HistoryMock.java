package org.nschmidt.ldparteditor.data;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

// FIXME MOCKUP!!
public class HistoryMock {

    private boolean hasNoThread = true;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final AtomicInteger action = new AtomicInteger(0);

    private Queue<Object[]> workQueue = new ConcurrentLinkedQueue<Object[]>();

    public void pushHistory(String text) {
        if (hasNoThread) {
            hasNoThread = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning.get() && Editor3DWindow.getAlive().get()) {
                        try {
                            Object[] newEntry = workQueue.poll();
                            if (newEntry != null) {
                                NLogger.debug(getClass(), "Compressing undo/redo data"); //$NON-NLS-1$
                            }
                            Thread.sleep(100);
                        } catch (InterruptedException e) {}
                    }
                    // TODO Cleanup the data here
                }
            }).start();
        }

        workQueue.offer(new Object[]{text});

    }

    public void deleteHistory() {
        isRunning.set(false);
    }
}

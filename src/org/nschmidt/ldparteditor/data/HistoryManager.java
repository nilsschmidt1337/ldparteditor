package org.nschmidt.ldparteditor.data;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.StringHelper;

// FIXME MOCKUP!!
public class HistoryManager {

    private boolean hasNoThread = true;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final AtomicInteger action = new AtomicInteger(0);

    private Queue<Object[]> workQueue = new ConcurrentLinkedQueue<Object[]>();

    public void pushHistory(String text, int selectionStart, int selectionEnd, GData[] data) {
        if (hasNoThread) {
            hasNoThread = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning.get() && Editor3DWindow.getAlive().get()) {
                        try {
                            Object[] newEntry = workQueue.poll();
                            if (newEntry != null) {
                                int[] result;
                                String text = (String) newEntry[0];
                                GData[] data = (GData[]) newEntry[3];
                                if (text != null) {
                                    result = StringHelper.compress(text);
                                } else if (data != null) {
                                    StringBuilder sb = new StringBuilder();
                                    int size = data.length - 1;
                                    final String ld = StringHelper.getLineDelimiter();
                                    for (int i = 0; i < size; i++) {
                                        sb.append(data[i].toString());
                                        sb.append(ld);
                                    }
                                    sb.append(data[size].toString());
                                    result = StringHelper.compress(sb.toString());
                                }
                                NLogger.debug(getClass(), "Compressed undo/redo data"); //$NON-NLS-1$
                            } else {
                                switch (action.get()) {
                                case 1:
                                    // Undo
                                    break;
                                case 2:
                                    // Redo
                                    break;
                                default:
                                    break;
                                }
                                action.set(0);
                            }
                            Thread.sleep(100);
                        } catch (InterruptedException e) {}
                    }
                    // TODO Cleanup the data here
                }
            }).start();
        }

        workQueue.offer(new Object[]{text, selectionStart, selectionEnd, data});

    }

    public void deleteHistory() {
        isRunning.set(false);
    }

    public void undo() {
        action.set(1);
        while (action.get() > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }
    }

    public void redo() {
        action.set(2);
        while (action.get() > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }
    }
}

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
package org.nschmidt.ldparteditor.helper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

/**
 * The helper class for dynamic progress information;
 *
 */
public enum ProgressHelper {
    INSTANCE;

    /** Counts the number of tasks */
    private static int taskCount = 0;
    /** Contains all task descriptions */
    private static List<String> taskList = new ArrayList<>();

    /**
     * Registers a new task to progress on the task queue
     *
     * @param task
     *            the task description
     */
    public static void queueTask(String task) {
        taskList.add(task);
        taskCount++;
    }

    /**
     * Starts a task from the task queue
     *
     * @param bar
     *            the progress bar to update
     * @param label
     *            the label to update
     */
    public static void dequeueTask(ProgressBar bar, Label label) {
        bar.setSelection((taskCount - taskList.size() + 1) * bar.getMaximum() / taskCount);
        label.setText(taskList.get(0));
        taskList.remove(0);
        try {
            Thread.sleep(40);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new LDPartEditorException(ie);
        }
    }

    /**
     * Finishes the task queue
     */
    public static void finishQueue(ProgressBar bar, Label label) {
        bar.setSelection(bar.getMaximum());
        label.setText(""); //$NON-NLS-1$
        resetQueue();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new LDPartEditorException(ie);
        }
    }

    /**
     * Resets the task queue
     */
    private static void resetQueue() {
        taskList.clear();
        taskCount = 0;
    }

}

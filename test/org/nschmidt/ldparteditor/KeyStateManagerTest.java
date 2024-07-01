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
package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.enumtype.TextTask;
import org.nschmidt.ldparteditor.state.KeyStateManager;

@SuppressWarnings("java:S5960")
public class KeyStateManagerTest {

    @Test
    public void testCleanupDuplicatedTask() {
        // This is the original setting
        KeyStateManager.changeKey("105", "I", Task.INSERT_AT_CURSOR); //$NON-NLS-1$ //$NON-NLS-2$
        // Modify the setting to cause a duplicate (with SWAP_WINDING)
        KeyStateManager.changeKey("106", "J", Task.INSERT_AT_CURSOR); //$NON-NLS-1$ //$NON-NLS-2$
        KeyStateManager.cleanupDuplicatedKeys();

        String restoredKeyCode = KeyStateManager.getMapKey(Task.INSERT_AT_CURSOR);
        String restoredKey = KeyStateManager.getTaskKeymap().get(Task.INSERT_AT_CURSOR);
        assertEquals("105", restoredKeyCode); //$NON-NLS-1$
        assertEquals("I", restoredKey); //$NON-NLS-1$
    }

    @Test
    public void testCleanupDuplicatedTextTask() {
        // This is the original setting
        KeyStateManager.changeKey("27", "Esc", TextTask.EDITORTEXT_ESC); //$NON-NLS-1$ //$NON-NLS-2$
        // Modify the setting to cause a duplicate (with EDITORTEXT_INSERT_HISTORY)
        KeyStateManager.changeKey("104+Ctrl", "Ctrl+H", TextTask.EDITORTEXT_ESC); //$NON-NLS-1$ //$NON-NLS-2$
        KeyStateManager.cleanupDuplicatedKeys();

        String restoredKeyCode = KeyStateManager.getMapKey(TextTask.EDITORTEXT_ESC);
        String restoredKey = KeyStateManager.getTextTaskKeymap().get(TextTask.EDITORTEXT_ESC);
        assertEquals("27", restoredKeyCode); //$NON-NLS-1$
        assertEquals("ESC", restoredKey); //$NON-NLS-1$
    }
}

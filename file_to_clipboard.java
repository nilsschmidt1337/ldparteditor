package de.vogella.desktop.clipboard;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class for putting files into the system clipboard
 *
 * @author Lars Vogel
 */

public final class CopyFileToClipboard {

    private CopyFileToClipboard() {
        // Utility class, prevent instantiation
    }

    /**
     * Copy a file into the clipboard
     * Assumes the file exists -> no additional check
     * @param fileName - includes the path
     */

    public static void copytoClipboard(String fileName) {
        Display display = Display.getCurrent();
        Clipboard clipboard = new Clipboard(display);
        String[] data = { fileName };
        clipboard.setContents(new Object[] { data },
                new Transfer[] { FileTransfer.getInstance() });
        clipboard.dispose();
    }
}
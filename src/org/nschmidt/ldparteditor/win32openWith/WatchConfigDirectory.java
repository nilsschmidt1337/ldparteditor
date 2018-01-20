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
package org.nschmidt.ldparteditor.win32openWith;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.nschmidt.ldparteditor.win32openWith.CallState.S0_REQ;
import static org.nschmidt.ldparteditor.win32openWith.CallState.S1_ACK;
import static org.nschmidt.ldparteditor.win32openWith.CallState.S3_DONE;
import static org.nschmidt.ldparteditor.win32openWith.FileActionResult.WILL_OPEN_FILE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.UTF8PrintWriter;
import org.nschmidt.ldparteditor.win32appdata.AppData;

/**
 * Watches the configuration directory to detect if a DAT file should be opened by LDPE
 */
class WatchConfigDirectory {

    private static final String FILE_OPEN_REQUEST = AppData.getPath() + "REQ"; //$NON-NLS-1$

    private final String watchId = new Random().nextLong() + "-" + new Random().nextLong() + "-" + new Random().nextLong(); //$NON-NLS-1$ //$NON-NLS-2$
    private final int token = new Random().nextInt(10);

    private final Path dir;
    private static Path fileToOpen = null;
    private final WatchService watcher;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    public WatchConfigDirectory(Path dir, Path fileToOpen) throws IOException {
        WatchConfigDirectory.fileToOpen = fileToOpen;
        this.dir = dir;
        this.watcher = FileSystems.getDefault().newWatchService();
        this.dir.register(this.watcher, ENTRY_MODIFY);
        cleanupStateFiles();
    }

    public WatchConfigDirectory(Path dir) throws IOException {
        this.dir = dir;
        this.watcher = FileSystems.getDefault().newWatchService();
        this.dir.register(this.watcher, ENTRY_MODIFY);
        cleanupStateFiles();
    }

    public static Path getFileToOpen() {
        return fileToOpen;
    }

    public void waitForCall() {
        while (Editor3DWindow.getAlive().get()) {

            // Wait for the WatchKey
            WatchKey key;
            try {
                key = this.watcher.poll(10, TimeUnit.MILLISECONDS);
            } catch (ClosedWatchServiceException cwse) {
                // TODO Needs logging
                return;
            } catch (InterruptedException ie) {
                // TODO Needs logging
                return;
            }

            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    // ev.context() = file name
                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path child = this.dir.resolve(name);

                    // print out event
                    NLogger.debug(getClass(), "Kind: {0} -> {1}", kind.name(), child); //$NON-NLS-1$
                }

                if (!key.reset()) {
                    break;
                }
            }
        }
    }

    public FileActionResult callAnotherLDPartEditorInstance() {

        // Make sure that there is no pending request anymore
        deleteRequest();

        CallState state = S0_REQ;
        int cycles = 0;

        // Try for up to 2 seconds to call another LDPE instance
        while (state != S3_DONE && cycles < 200) {

            // Create a request
            if (state == S0_REQ) {
                final String reqFile = dir.resolve("REQ").toString(); //$NON-NLS-1$
                try (UTF8PrintWriter writer = new UTF8PrintWriter(reqFile)) {
                    final String pathToOpen = fileToOpen.toString();
                    writer.println(pathToOpen);
                } catch (FileNotFoundException fnfe) {
                    if (cycles == 0) {
                        NLogger.error(getClass(), fnfe);
                    }
                } catch (UnsupportedEncodingException uee) {
                    if (cycles == 0) {
                        NLogger.error(getClass(), uee);
                    }
                }
                state = S1_ACK;
            }

            // Wait for the WatchKey
            WatchKey key;
            try {
                key = this.watcher.poll(10, TimeUnit.MILLISECONDS);
                cycles++;
            } catch (ClosedWatchServiceException cwse) {
                NLogger.error(getClass(), cwse);
                return WILL_OPEN_FILE;
            } catch (InterruptedException ie) {
                NLogger.error(getClass(), ie);
                return WILL_OPEN_FILE;
            }

            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        // Try it again
                        state = S0_REQ;
                        continue;
                    }

                    // ev.context() = file name
                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path child = this.dir.resolve(name);

                    // print out event
                    NLogger.debug(getClass(), "Kind: {0} -> {1}", kind.name(), child); //$NON-NLS-1$
                }

                if (!key.reset()) {
                    break;
                }
            }

            // TODO Needs state machine
        }

        return WILL_OPEN_FILE;
    }

    private void deleteRequest() {
        final File fileOpenRequest = new File(FILE_OPEN_REQUEST);
        if (fileOpenRequest.exists()) {
            fileOpenRequest.delete();
        }
    }

    private void cleanupStateFiles() {
        deleteRequest();
    }
}

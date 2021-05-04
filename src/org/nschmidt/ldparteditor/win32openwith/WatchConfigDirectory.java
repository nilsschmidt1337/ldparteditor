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
package org.nschmidt.ldparteditor.win32openwith;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.nschmidt.ldparteditor.win32openwith.CallState.S0_REQ;
import static org.nschmidt.ldparteditor.win32openwith.CallState.S1_ACK;
import static org.nschmidt.ldparteditor.win32openwith.CallState.S2_SEND;
import static org.nschmidt.ldparteditor.win32openwith.CallState.S3_DONE;
import static org.nschmidt.ldparteditor.win32openwith.FileActionResult.DELEGATED_TO_ANOTHER_INSTANCE;
import static org.nschmidt.ldparteditor.win32openwith.FileActionResult.WILL_OPEN_FILE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.enums.OpenInWhat;
import org.nschmidt.ldparteditor.helpers.LDPartEditorException;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.LDParsingException;
import org.nschmidt.ldparteditor.text.UTF8BufferedReader;
import org.nschmidt.ldparteditor.text.UTF8PrintWriter;
import org.nschmidt.ldparteditor.win32appdata.AppData;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Watches the configuration directory to detect if a DAT file should be opened by LDPartEditor
 */
class WatchConfigDirectory {

    private static final String FILE_OPEN_REQUEST = AppData.getPath() + "REQ"; //$NON-NLS-1$
    private static final String FILE_OPEN_ACKNOWLEDGEMENT = AppData.getPath() + "ACK"; //$NON-NLS-1$
    private static final String FILE_OPEN_SEND = AppData.getPath() + "SEND"; //$NON-NLS-1$
    private static final String FILE_OPEN_DONE = AppData.getPath() + "DONE"; //$NON-NLS-1$

    private final String watchId = new Random().nextLong() + "-" + new Random().nextLong() + "-" + new Random().nextLong(); //$NON-NLS-1$ //$NON-NLS-2$
    private final int token = new Random().nextInt(10);

    private final Path dir;
    private static Path fileToOpen = null;
    private final WatchService watcher;

    private final Lock fileOpenLock = new ReentrantLock();
    private final Lock fileOpenLock2 = new ReentrantLock();

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
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

        // This thread needs to send an asynchronous call to the UI
        // and should keep waiting for other file open calls
        // the calls have to be stored in a queue.

        CallState state = S0_REQ;

        while (Editor3DWindow.getAlive().get()) {

            // Wait for the WatchKey
            WatchKey key;
            try {
                key = this.watcher.poll(10, TimeUnit.MILLISECONDS);
            } catch (ClosedWatchServiceException cwse) {
                NLogger.error(getClass(), cwse);
                return;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }

            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    // ev.context() = file name
                    WatchEvent<Path> ev = cast(event);
                    final String action = ev.context() + ""; //$NON-NLS-1$

                    if (action.startsWith("REQ")) { //$NON-NLS-1$
                        state = S0_REQ;
                        final String reqFile = dir.resolve(action).toString();
                        String reqSignalMsg = null;
                        try (UTF8BufferedReader reader = new UTF8BufferedReader(reqFile)) {
                            reqSignalMsg = reader.readLine();
                        } catch (LDParsingException ldpe) {
                            NLogger.error(getClass(), ldpe);
                        } catch (FileNotFoundException consumed) {
                            // Do nothing if the file was not found
                        } catch (UnsupportedEncodingException uee) {
                            NLogger.error(getClass(), uee);
                        }

                        if (reqSignalMsg != null) {
                            final String ackFile = dir.resolve("ACK" + token).toString(); //$NON-NLS-1$
                            try (UTF8PrintWriter writer = new UTF8PrintWriter(ackFile)) {
                                writer.println(watchId);
                            } catch (FileNotFoundException fnfe) {
                                NLogger.error(getClass(), fnfe);
                            } catch (UnsupportedEncodingException uee) {
                                NLogger.error(getClass(), uee);
                            }
                        }
                    }

                    if (state == S0_REQ && action.startsWith("SEND")) { //$NON-NLS-1$
                        final String reqFile = dir.resolve(action).toString();
                        String targetWatchId = null;
                        String pathToOpen = null;
                        try (UTF8BufferedReader reader = new UTF8BufferedReader(reqFile)) {
                            targetWatchId = reader.readLine();
                            pathToOpen = reader.readLine();
                        } catch (LDParsingException ldpe) {
                            NLogger.error(getClass(), ldpe);
                        } catch (FileNotFoundException consumed) {
                            // Do nothing if the file was not found
                        } catch (UnsupportedEncodingException uee) {
                            NLogger.error(getClass(), uee);
                        }

                        if (targetWatchId != null && pathToOpen != null && watchId.equals(targetWatchId)) {
                            boolean shouldOpenFile = false;
                            final String ackFile = dir.resolve("DONE" + token).toString(); //$NON-NLS-1$
                            try (UTF8PrintWriter writer = new UTF8PrintWriter(ackFile)) {
                                writer.println(watchId);
                                shouldOpenFile = true;
                                state = S3_DONE;
                            } catch (FileNotFoundException fnfe) {
                                NLogger.error(getClass(), fnfe);
                            } catch (UnsupportedEncodingException uee) {
                                NLogger.error(getClass(), uee);
                            }

                            if (shouldOpenFile) {
                                final String path = pathToOpen;
                                CompletableFuture.runAsync(() -> {
                                   try {
                                       fileOpenLock.lock();
                                       // Load file here
                                       final Editor3DWindow win = Editor3DWindow.getWindow();
                                       win.getShell().getDisplay().asyncExec(() -> {
                                           try {
                                               fileOpenLock2.lock();
                                               final boolean syncingTabs = WorkbenchManager.getUserSettingState().isSyncingTabs();
                                               WorkbenchManager.getUserSettingState().setSyncingTabs(false);
                                               try {
                                                   final DatFile df = win.openDatFile(OpenInWhat.EDITOR_TEXT_AND_3D, path, false);
                                                   if (df != null) {
                                                       win.addRecentFile(df);
                                                       final File f = new File(df.getNewName());
                                                       if (f.getParentFile() != null) {
                                                           Project.setLastVisitedPath(f.getParentFile().getAbsolutePath());
                                                       }
                                                   }

                                                   win.updateTreeUnsavedEntries();

                                                   // Hack to bring LDPartEditor to front
                                                   if (!win.getShell().getMinimized())
                                                   {
                                                       win.getShell().setMinimized(true);
                                                   }

                                                   win.getShell().setMinimized(false);
                                                   win.getShell().setActive();
                                               } finally {
                                               }
                                               WorkbenchManager.getUserSettingState().setSyncingTabs(syncingTabs);
                                           } finally {
                                              fileOpenLock2.unlock();
                                           }
                                       });
                                   } finally {
                                       fileOpenLock.unlock();
                                   }
                                });
                            }
                        }
                    }
                }

                if (!key.reset()) {
                    break;
                }
            }
        }
    }

    public FileActionResult callAnotherLDPartEditorInstance() {

        FileActionResult result = WILL_OPEN_FILE;

        // Make sure that there is no pending request anymore
        deleteRequest();

        CallState state = S0_REQ;
        int cycles = 0;
        String targetWatchId = null;

        // Try for up to 2 seconds to call another LDPE instance
        while (state != S3_DONE && cycles < 200) {

            // Create a request
            if (state == S0_REQ) {
                final String reqFile = dir.resolve("REQ" + token).toString(); //$NON-NLS-1$
                try (UTF8PrintWriter writer = new UTF8PrintWriter(reqFile)) {
                    writer.println("REQ"); //$NON-NLS-1$
                    state = S1_ACK;
                } catch (FileNotFoundException consumed) {
                    // Do nothing if the file was not found
                } catch (UnsupportedEncodingException uee) {
                    if (cycles == 0) {
                        NLogger.error(getClass(), uee);
                    }
                }
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
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }

            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        // Try it again
                        state = S0_REQ;
                        targetWatchId = null;
                        continue;
                    }

                    // ev.context() = file name
                    WatchEvent<Path> ev = cast(event);
                    String action = ev.context() + ""; //$NON-NLS-1$

                    if (state == S1_ACK && action.startsWith("ACK")) { //$NON-NLS-1$
                        final String ackFile = dir.resolve(action).toString();
                        try (UTF8BufferedReader reader = new UTF8BufferedReader(ackFile)) {
                            targetWatchId = reader.readLine();
                        } catch (LDParsingException ldpe) {
                            NLogger.error(getClass(), ldpe);
                        } catch (FileNotFoundException consumed) {
                            // Do nothing if the file was not found
                        } catch (UnsupportedEncodingException uee) {
                            NLogger.error(getClass(), uee);
                        }

                        if (targetWatchId != null) {
                            final String sendFile = dir.resolve("SEND" + token).toString(); //$NON-NLS-1$
                            try (UTF8PrintWriter writer = new UTF8PrintWriter(sendFile)) {
                                final String pathToOpen = fileToOpen.toString();
                                writer.println(targetWatchId);
                                writer.println(pathToOpen);
                                state = S2_SEND;
                            } catch (FileNotFoundException consumed) {
                                // Do nothing if the file was not found
                            } catch (UnsupportedEncodingException uee) {
                                NLogger.error(getClass(), uee);
                            }
                        }
                    }

                    if (state == S2_SEND && action.startsWith("DONE")) { //$NON-NLS-1$
                        final String doneFile = dir.resolve(action).toString();
                        String confirmationWatchId = null;
                        try (UTF8BufferedReader reader = new UTF8BufferedReader(doneFile)) {
                            confirmationWatchId = reader.readLine();
                            if (confirmationWatchId != null && confirmationWatchId.equals(targetWatchId)) {
                                state = S3_DONE;
                                result = DELEGATED_TO_ANOTHER_INSTANCE;
                                cleanupStateFiles();
                            }
                        } catch (LDParsingException | UnsupportedEncodingException ex) {
                            NLogger.error(getClass(), ex);
                        } catch (FileNotFoundException consumed) {
                            // Do nothing if the file was not found
                        }
                    }
                }

                if (!key.reset()) {
                    break;
                }
            }
        }

        return result;
    }

    private void deleteRequest() {
        try {
            for (int i = 0; i < 10; i++) {
                final File fileOpenRequest = new File(FILE_OPEN_REQUEST + i);
                if (Files.deleteIfExists(fileOpenRequest.toPath())) {
                    NLogger.debug(getClass(), "Deleted 'REQ' status file {0}", fileOpenRequest.toPath()); //$NON-NLS-1$
                }
            }
        } catch (IOException ex) {
            NLogger.error(getClass(), ex);
        }
    }

    private void cleanupStateFiles() {
        try {
            deleteRequest();
            for (int i = 0; i < 10; i++) {
                final File fileAck = new File(FILE_OPEN_ACKNOWLEDGEMENT + i);
                if (Files.deleteIfExists(fileAck.toPath())) {
                    NLogger.debug(getClass(), "Deleted 'ACK' status file {0}", fileAck.toPath()); //$NON-NLS-1$
                }
                final File fileSend = new File(FILE_OPEN_SEND + i);
                if (Files.deleteIfExists(fileSend.toPath())) {
                    NLogger.debug(getClass(), "Deleted 'SEND' status file {0}", fileSend.toPath()); //$NON-NLS-1$
                }
                final File fileDone = new File(FILE_OPEN_DONE + i);
                if (Files.deleteIfExists(fileDone.toPath())) {
                    NLogger.debug(getClass(), "Deleted 'DONE' status file {0}", fileDone.toPath()); //$NON-NLS-1$
                }
            }
        } catch (IOException ex) {
            NLogger.error(getClass(), ex);
        }
    }
}

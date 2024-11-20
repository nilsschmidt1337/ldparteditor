package org.nschmidt.ldparteditor;


import static org.nschmidt.ldparteditor.StlToDatTest.resourcePath;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.Win32LnkParser;

@SuppressWarnings("java:S5960")
public class Win32LnkParserTest {

    // FIXME Needs implementation!

    @Test
    public void testLinkToFileOnHarddisk() {
        withDebugging(() -> {
            String resPath = resourcePath("3782.jpg-Shortcut.lnk"); //$NON-NLS-1$

            File result = Win32LnkParser.resolveLnkShortcut(new File(resPath));
            // HasLinkTargetIDList is true
            // HasLinkInfo is true

            assertTrue(result.getName().contains("3782.jpg")); //$NON-NLS-1$
        });
    }

    @Test
    public void testLinkToFileOnNetwork() {
        withDebugging(() -> {
            String resPath = resourcePath("3782.jpg-NetworkShortcut.lnk"); //$NON-NLS-1$

            File result = Win32LnkParser.resolveLnkShortcut(new File(resPath));

            assertTrue(result.getName().contains("3782.jpg")); //$NON-NLS-1$
        });
    }

    private static void withDebugging(Runnable op) {
        NLogger.debugging = true;
        try {
            op.run();
        } finally {
            NLogger.debugging = false;
        }
    }
}

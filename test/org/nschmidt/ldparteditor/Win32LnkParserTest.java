package org.nschmidt.ldparteditor;


import static org.nschmidt.ldparteditor.StlToDatTest.resourcePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.Win32LnkParser;

@SuppressWarnings("java:S5960")
public class Win32LnkParserTest {

    private static final String FILENAME = "3782.jpg"; //$NON-NLS-1$

    @Test
    public void testLinkToFileOnHarddisk() {
        withDebugging(() -> {
            String resPath = resourcePath("3782.jpg-Shortcut.lnk"); //$NON-NLS-1$

            String result = Win32LnkParser.resolveLnkShortcut(resPath);
            // HasLinkTargetIDList is true
            // HasLinkInfo is true

            assertTrue(result.endsWith(FILENAME));
            assertTrue(result.contains("G")); //$NON-NLS-1$
        });
    }

    @Test
    public void testLinkToFileOnHarddiskWithUmlauts() {
        withDebugging(() -> {
            String resPath = resourcePath("3782.jpg-UmlautShortcut.lnk"); //$NON-NLS-1$

            String result = Win32LnkParser.resolveLnkShortcut(resPath);

            assertTrue(result.endsWith(FILENAME));
            assertTrue(result.contains("Späße")); //$NON-NLS-1$
            assertTrue(result.contains("G")); //$NON-NLS-1$
        });
    }

    @Test
    public void testLinkToFileOnNetwork() {
        withDebugging(() -> {
            String resPath = resourcePath("3782.jpg-NetworkShortcut.lnk"); //$NON-NLS-1$

            String result = Win32LnkParser.resolveLnkShortcut(resPath);

            assertTrue(result.endsWith(FILENAME));
            assertTrue(result.contains("192.168.9.222")); //$NON-NLS-1$
        });
    }

    @Test
    public void testNoLink() {
        String result = Win32LnkParser.resolveLnkShortcut(FILENAME);
        assertEquals(FILENAME, result);
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

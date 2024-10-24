package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.nschmidt.ldparteditor.text.Win32LnkParser;

@SuppressWarnings("java:S5960")
public class Win32LnkParserTest {

    // FIXME Needs implementation!

    @Test
    public void testLinkToFileOnHarddisk() {
        String resPath = resourcePath("3782.jpg-Shortcut.lnk"); //$NON-NLS-1$

        File result = Win32LnkParser.resolveLnkShortcut(new File(resPath));

        assertTrue(result.getName().contains("3782.jpg")); //$NON-NLS-1$
    }

    @Test
    public void testLinkToFileOnNetwork() {
        String resPath = resourcePath("3782.jpg-NetworkShortcut.lnk"); //$NON-NLS-1$

        File result = Win32LnkParser.resolveLnkShortcut(new File(resPath));

        assertTrue(result.getName().contains("3782.jpg")); //$NON-NLS-1$
    }

    private String resourcePath(String fileName) {
        URL stlToTest = Thread.currentThread().getContextClassLoader().getResource(fileName);
        String resPath = "(none)"; //$NON-NLS-1$
        try {
            resPath = java.nio.file.Paths.get(stlToTest.toURI()).toString();
        } catch (URISyntaxException e) {
            fail("Resource " + fileName + " was not found."); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return resPath;
    }
}

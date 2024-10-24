package org.nschmidt.ldparteditor;

import static org.junit.Assert.assertTrue;
import static org.nschmidt.ldparteditor.StlToDatTest.resourcePath;

import java.io.File;

import org.junit.Test;
import org.nschmidt.ldparteditor.text.Win32LnkParser;

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
}

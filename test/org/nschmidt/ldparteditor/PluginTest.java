package org.nschmidt.ldparteditor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;
import org.nschmidt.ldparteditor.text.UTF8PrintWriter;

import com.sun.tools.javac.Main;

@SuppressWarnings("java:S5960")
public class PluginTest {

    private static final String LPE_PLUGIN_JAVA = "LpePlugin.java"; //$NON-NLS-1$
    private static final String LPE_PLUGIN_CLASS = "LpePlugin.class"; //$NON-NLS-1$

    private static final String FILE_CONTENTS = """
package plugin;

// AUTO GENERATED PLUGIN CLASS
import org.nschmidt.ldparteditor.plugin.Plugin;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;

public class LpePlugin implements Plugin {

    public LpePlugin() {

    }

    @Override
    public String getPlugInAuthor() {
        return "Nils Schmidt [BlackBrick89]"; //$NON-NLS-1$
    }

    @Override
    public String getPlugInName() {
        return "blah"; //$NON-NLS-1$
    }

    @Override
    public void onCanvas(Composite3D c3d) {
        return;
    }

    @Override
    public void onData(DatFile data) {
        return;
    }

}
            """; //$NON-NLS-1$
    
    @Test
    public void pluginTest() throws IOException {
        // List all java files from the plugin directory
        try (UTF8PrintWriter out = new UTF8PrintWriter(LPE_PLUGIN_JAVA)) {
            out.println(FILE_CONTENTS);
            out.flush();
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }

        // Compile the plugin
        Main.compile(new String[] { LPE_PLUGIN_JAVA });
        assertTrue(Files.deleteIfExists(new File(LPE_PLUGIN_JAVA).toPath()));
        assertTrue(Files.deleteIfExists(new File(LPE_PLUGIN_CLASS).toPath()));
    }
}

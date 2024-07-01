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

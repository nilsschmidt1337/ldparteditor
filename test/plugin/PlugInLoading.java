package plugin;

import java.io.File;
import org.nschmidt.ldparteditor.plugin.Plugin;
import com.sun.tools.javac.Main;

class PlugInLoading {

    public void pluginTest() {
        // List all java files from the plugin directory
        File F = new File("plugin"); //$NON-NLS-1$
        String[] inhalt = F.list();
        for (int i = 0; i < inhalt.length; i++) {
            if (inhalt[i].toLowerCase().endsWith(".java")) { //$NON-NLS-1$
                System.out.println(inhalt[i]);
            }
        }

        // Compile the plugin and call a method

        Main.compile(new String[] { "plugin/LpePlugin.java" }); //$NON-NLS-1$
        try {
            Plugin p = (Plugin) Class.forName("LpePlugin").newInstance(); //$NON-NLS-1$
            System.out.println(p.getPlugInName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
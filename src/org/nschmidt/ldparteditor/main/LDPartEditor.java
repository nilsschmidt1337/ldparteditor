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
package org.nschmidt.ldparteditor.main;

import static org.nschmidt.ldparteditor.win32openwith.FileActionResult.DELEGATED_TO_ANOTHER_INSTANCE;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.Attributes;

import org.eclipse.swt.internal.Library;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.splash.SplashScreen;
import org.nschmidt.ldparteditor.win32openwith.TryToOpen;

/**
 * The main class, which launches the Splash Screen
 *
 */
// Nothing more to do here..
public class LDPartEditor {

    /**
     * Program entry point
     *
     * @param args
     *            startup arguments
     */
    public static void main(String[] args) {
        // Initialize the logger
        NLogger.init(args);

        isLoadable();
        
        // Check if LDPartEditor should open a file
        if (args.length > 0 && TryToOpen.file(args[0]) == DELEGATED_TO_ANOTHER_INSTANCE) {
            NLogger.flushErrorStream();
            return;
        }

        // Show the SplashScreen
        new SplashScreen().run();

        // Flush the error stream to write the complete log file
        NLogger.flushErrorStream();
    }
    
    static boolean isLoadable () {
    	URL url = Library.class.getClassLoader ().getResource ("org/eclipse/swt/internal/Library.class"); //$NON-NLS-1$
    	if (!url.getProtocol ().equals ("jar")) { //$NON-NLS-1$
    		/* SWT is presumably running in a development environment */
    		return true;
    	}

    	Attributes attributes = null;
    	try {
    		URLConnection connection = url.openConnection();
    		if (!(connection instanceof JarURLConnection jc)) {
    			/* should never happen for a "jar:" url */
    			NLogger.debug(LDPartEditor.class, "Wrong connection type");
    			return false;
    		}
    		attributes = jc.getMainAttributes();
    	} catch (IOException e) {
    		/* should never happen for a valid SWT jar with the expected manifest values */
    		NLogger.debug(LDPartEditor.class, e);
    		return false;
    	}

    	String os = os ();
    	String arch = arch ();
    	String manifestOS = attributes.getValue ("SWT-OS"); //$NON-NLS-1$
    	String manifestArch = attributes.getValue ("SWT-Arch"); //$NON-NLS-1$
    	
    	NLogger.debug(LDPartEditor.class, "os {0}", os);
    	NLogger.debug(LDPartEditor.class, "arch {0}", arch);
    	NLogger.debug(LDPartEditor.class, "manifestOS {0}", manifestOS);
    	NLogger.debug(LDPartEditor.class, "manifestArch {0}", manifestArch);
    	if (arch.equals (manifestArch) && os.equals (manifestOS)) {
    		return true;
    	}

    	return false;
    }
    
    static String arch() {
    	String osArch = System.getProperty("os.arch"); //$NON-NLS-1$
    	if (osArch.equals ("amd64")) return "x86_64"; //$NON-NLS-1$ $NON-NLS-2$
    	return osArch;
    }

    static String os() {
    	String osName = System.getProperty("os.name"); //$NON-NLS-1$
    	if (osName.equals ("Linux")) return "linux"; //$NON-NLS-1$ $NON-NLS-2$
    	if (osName.equals ("Mac OS X")) return "macosx"; //$NON-NLS-1$ $NON-NLS-2$
    	if (osName.startsWith ("Win")) return "win32"; //$NON-NLS-1$ $NON-NLS-2$
    	return osName;
    }
}

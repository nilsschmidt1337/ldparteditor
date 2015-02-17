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
package org.nschmidt.ldparteditor.helpers;

import org.nschmidt.ldparteditor.i18n.I18n;

/**
 * Provides information about the version of LD Part Editor
 *
 * @author nils
 *
 */
public enum Version {
    INSTANCE;

    /**
     * @return the name of the application ("LD Part Editor")
     */
    public static String getApplicationName() {
        return "LD Part Editor"; //$NON-NLS-1$
    }

    /**
     * @return the contributors to LD Part Editor. Separated by comma.
     */
    public static String getContributors() {
        return I18n.VERSION_Contributors;
    }

    /**
     * @return the lead developer of LD Part Editor (e.g. Nils Schmidt)
     */
    public static String getDevelopmentLead() {
        return I18n.VERSION_DevelopmentLead;
    }

    /**
     * @return the stage of the development (Alpha, Beta, Release Candidate,
     *         Production)
     */
    public static String getStage() {
        return I18n.VERSION_Stage;
    }

    /**
     * @return the members of the test team. Separated by comma.
     */
    public static String getTestTeam() {
        return I18n.VERSION_Testers;
    }

    /**
     * Gets the version number in the format x.y.z, where
     *
     * z stands for the bug fix number of the previous x.y version. e.g. x.y.3
     * is the third bug fix release for version x.y
     *
     * y stands for the feature number of the previous x version. e.g. x.2.1 is
     * the first bug fix for the second new feature release
     *
     * x stands for the stage number of the release. x = 0 means: Alpha / Beta x
     * = 1 means: Official Release Candidate (software should be ready to use) x
     * > 1 means: Production / Stable
     *
     * @return the version number
     */
    public static String getVersion() {
        return I18n.VERSION_Version;
    }
}

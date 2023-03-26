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
package org.nschmidt.ldparteditor.helper.composite3d;

public class IntersectorSettings {
    private int scope = 1;
    private boolean colourise = false;
    private boolean hidingOther = false;
    private boolean ignoreSubfileContent = false;
    public int getScope() {
        return scope;
    }
    public void setScope(int scope) {
        this.scope = scope;
    }
    public boolean isColourise() {
        return colourise;
    }
    public void setColourise(boolean colourise) {
        this.colourise = colourise;
    }
    public boolean isHidingOther() {
        return hidingOther;
    }
    public void setHidingOther(boolean hidingOther) {
        this.hidingOther = hidingOther;
    }
    public boolean isIgnoreSubfileContent() {
        return ignoreSubfileContent;
    }
    public void setIgnoreSubfileContent(boolean ignoreSubfileContent) {
        this.ignoreSubfileContent = ignoreSubfileContent;
    }
}

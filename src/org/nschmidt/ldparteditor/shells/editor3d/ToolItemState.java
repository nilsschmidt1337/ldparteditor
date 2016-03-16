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
package org.nschmidt.ldparteditor.shells.editor3d;

import java.io.Serializable;

/**
 * @author nils
 *
 */
public class ToolItemState implements Serializable {

    private static final long serialVersionUID = -8858673578608054459L;

    private String key = ""; //$NON-NLS-1$
    private ToolItemDrawMode drawMode = ToolItemDrawMode.HORIZONTAL;
    private ToolItemDrawLocation drawLocation = ToolItemDrawLocation.NORTH;
    private String label = ""; //$NON-NLS-1$
    public ToolItemState(String key, ToolItemDrawLocation drawLocation, ToolItemDrawMode drawMode, String label) {
        this.key = key;
        this.drawLocation = drawLocation;
        this.drawMode = drawMode;
        this.label = label;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public ToolItemDrawMode getDrawMode() {
        return drawMode;
    }
    public void setDrawMode(ToolItemDrawMode drawMode) {
        this.drawMode = drawMode;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public ToolItemDrawLocation getDrawLocation() {
        return drawLocation;
    }
    public void setDrawLocation(ToolItemDrawLocation drawLocation) {
        this.drawLocation = drawLocation;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (key == null ? 0 : key.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ToolItemState other = (ToolItemState) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }
}

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
package org.nschmidt.ldparteditor.workbench;

import java.io.Serializable;

import org.nschmidt.ldparteditor.enums.Perspective;

/**
 * @author nils
 *
 */
public class Composite3DState implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean scales = false;
    private Perspective perspective = Perspective.TWO_THIRDS;
    private int renderMode = 0;
    private boolean showLabel = false;
    private boolean showAxis = false;
    private boolean showOrigin = false;
    private boolean showGrid = false;
    private float gridScale = 1f;
    private boolean lights = false;
    private boolean meshlines = false;
    private boolean subfileMeshlines = false;
    private boolean vertices = false;
    private boolean hiddenVertices = false;
    private boolean studLogo = false;
    private int lineMode = 0;
    private boolean alwaysBlackLines = false;
    private boolean anaglyph3d = false;

    private boolean sash = false;
    private boolean vertical = false;
    private int[] weights = null;

    private String path = null;
    private String parentPath = null;

    public Composite3DState() {

    }

    public Composite3DState(boolean vertical) {
        this.sash = true;
        this.vertical = vertical;
    }

    public boolean hasScales() {
        return scales;
    }

    public void setScales(boolean scales) {
        this.scales = scales;
    }

    public boolean isSash() {
        return sash;
    }

    public void setSash(boolean sash) {
        this.sash = sash;
    }

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public Perspective getPerspective() {
        return perspective;
    }

    public void setPerspective(Perspective perspective) {
        this.perspective = perspective;
    }

    public int[] getWeights() {
        return weights;
    }

    public void setWeights(int[] weights) {
        this.weights = weights;
    }

    public int getRenderMode() {
        return renderMode;
    }

    public void setRenderMode(int renderMode) {
        this.renderMode = renderMode;
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

    public boolean isShowAxis() {
        return showAxis;
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
    }

    public boolean isShowOrigin() {
        return showOrigin;
    }

    public void setShowOrigin(boolean showOrigin) {
        this.showOrigin = showOrigin;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public float getGridScale() {
        return gridScale;
    }

    public void setGridScale(float gridScale) {
        this.gridScale = gridScale;
    }

    public boolean isLights() {
        return lights;
    }

    public void setLights(boolean lights) {
        this.lights = lights;
    }

    public boolean isMeshlines() {
        return meshlines;
    }

    public void setMeshlines(boolean meshlines) {
        this.meshlines = meshlines;
    }

    public boolean isSubfileMeshlines() {
        return subfileMeshlines;
    }

    public void setSubfileMeshlines(boolean subfileMeshlines) {
        this.subfileMeshlines = subfileMeshlines;
    }

    public boolean isVertices() {
        return vertices;
    }

    public void setVertices(boolean vertices) {
        this.vertices = vertices;
    }

    public boolean isHiddenVertices() {
        return hiddenVertices;
    }

    public void setHiddenVertices(boolean hiddenVertices) {
        this.hiddenVertices = hiddenVertices;
    }

    public boolean isStudLogo() {
        return studLogo;
    }

    public void setStudLogo(boolean studLogo) {
        this.studLogo = studLogo;
    }

    public int getLineMode() {
        return lineMode;
    }

    public void setLineMode(int lineMode) {
        this.lineMode = lineMode;
    }

    public boolean isAlwaysBlackLines() {
        return alwaysBlackLines;
    }

    public void setAlwaysBlackLines(boolean alwaysBlackLines) {
        this.alwaysBlackLines = alwaysBlackLines;
    }

    public boolean isAnaglyph3d() {
        return anaglyph3d;
    }

    public void setAnaglyph3d(boolean anaglyph3d) {
        this.anaglyph3d = anaglyph3d;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    @Override
    public String toString() {
        return this.parentPath + " -> " + path; //$NON-NLS-1$
    }

}

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

import java.math.BigDecimal;

public class SelectorSettings {

    public static final int EVERYTHING = 0;
    public static final int CONNECTED = 1;
    public static final int TOUCHING = 2;
    private int scope = 0;
    private boolean edgeStop = false;
    private boolean colour = false;
    private boolean type = false;
    private boolean orientation = false;
    private boolean distance = false;
    private boolean hidden = false;
    private boolean noSubfiles = false;
    private boolean wholeSubfiles = false;
    private boolean edgeAdjacency = false;
    private BigDecimal angle = BigDecimal.ZERO;
    private BigDecimal equalDistance = new BigDecimal("0.0001"); //$NON-NLS-1$
    private boolean vertices = true;
    private boolean lines = true;
    private boolean triangles = true;
    private boolean quads = true;
    private boolean condlines = true;
    public int getScope() {
        return scope;
    }
    public void setScope(int scope) {
        this.scope = scope;
    }
    public boolean isEdgeStop() {
        return edgeStop;
    }
    public void setEdgeStop(boolean edgeStop) {
        this.edgeStop = edgeStop;
    }
    public boolean isColour() {
        return colour;
    }
    public void setColour(boolean colour) {
        this.colour = colour;
    }
    public boolean isOrientation() {
        return orientation;
    }
    public void setOrientation(boolean orientation) {
        this.orientation = orientation;
    }
    public boolean isHidden() {
        return hidden;
    }
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    public boolean isNoSubfiles() {
        return noSubfiles;
    }
    public void setNoSubfiles(boolean noSubfiles) {
        this.noSubfiles = noSubfiles;
    }
    public boolean isWholeSubfiles() {
        return wholeSubfiles;
    }
    public void setWholeSubfiles(boolean wholeSubfiles) {
        this.wholeSubfiles = wholeSubfiles;
    }
    public BigDecimal getAngle() {
        return angle;
    }
    public void setAngle(BigDecimal angle) {
        this.angle = angle;
    }
    public BigDecimal getEqualDistance() {
        return equalDistance;
    }
    public void setEqualDistance(BigDecimal equalDistance) {
        this.equalDistance = equalDistance;
    }
    public boolean isDistance() {
        return distance;
    }
    public void setDistance(boolean distance) {
        this.distance = distance;
    }
    public boolean isVertices() {
        return vertices;
    }
    public void setVertices(boolean vertices) {
        this.vertices = vertices;
    }
    public boolean isLines() {
        return lines;
    }
    public void setLines(boolean lines) {
        this.lines = lines;
    }
    public boolean isTriangles() {
        return triangles;
    }
    public void setTriangles(boolean triangles) {
        this.triangles = triangles;
    }
    public boolean isQuads() {
        return quads;
    }
    public void setQuads(boolean quads) {
        this.quads = quads;
    }
    public boolean isCondlines() {
        return condlines;
    }
    public void setCondlines(boolean condlines) {
        this.condlines = condlines;
    }
    public boolean isEdgeAdjacency() {
        return edgeAdjacency;
    }
    public void setEdgeAdjacency(boolean edgeAdjacency) {
        this.edgeAdjacency = edgeAdjacency;
    }
    public boolean isType() {
        return type;
    }
    public void setType(boolean type) {
        this.type = type;
    }
}

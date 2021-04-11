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
package org.nschmidt.ldparteditor.composites;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeSortedMap;
import org.nschmidt.ldparteditor.workbench.Composite3DState;

public class Composite3DViewState {

    private float zoom = 0f;
    private float zoomExponent = 0f;
    private float viewportPixelPerLDU;
    private Vector4f offset = new Vector4f(0, 0, 0, 1f);
    final Composite3DState state = new Composite3DState();
    private boolean negDeterminant = false;

    private final Matrix4f viewportTranslation = new Matrix4f();
    private final Matrix4f viewportRotation = new Matrix4f();
    private final Matrix4f viewportMatrix = new Matrix4f();
    private final Matrix4f viewportMatrixInv = new Matrix4f();
    private final Manipulator manipulator = new Manipulator();

    /** The generator of the viewport space */
    private final Vector4f[] viewportGenerator = new Vector4f[3];
    /** The origin axis coordinates of the viewport */
    private final Vector3f[] viewportOriginAxis = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
    /** The viewport z-Near value */
    private double zNear = 1000000f;
    /** The viewport z-Far value */
    private double zFar = 1000001f;

    private final Map<String, List<Boolean>> hideShowState = new HashMap<>();
    private final Map<String, List<Boolean>> selection = new HashMap<>();

    private final Set<Vertex> hiddenVertices = Collections.newSetFromMap(new ThreadsafeSortedMap<>());
    private final Set<Vertex> selectedVertices = Collections.newSetFromMap(new ThreadsafeSortedMap<>());

    float getZoom() {
        return zoom;
    }

    void setZoom(float zoom) {
        this.zoom = zoom;
    }

    float getZoomExponent() {
        return zoomExponent;
    }

    void setZoomExponent(float zoomExponent) {
        this.zoomExponent = zoomExponent;
    }

    public float getViewportPixelPerLDU() {
        return viewportPixelPerLDU;
    }

    public void setViewportPixelPerLDU(float viewportPixelPerLDU) {
        this.viewportPixelPerLDU = viewportPixelPerLDU;
    }

    public Vector4f getOffset() {
        return offset;
    }

    public void setOffset(Vector4f offset) {
        this.offset = offset;
    }

    boolean hasNegDeterminant() {
        return negDeterminant;
    }

    public void setNegDeterminant(boolean negDeterminant) {
        this.negDeterminant = negDeterminant;
    }

    public Matrix4f getViewportTranslation() {
        return viewportTranslation;
    }

    public Matrix4f getViewportRotation() {
        return viewportRotation;
    }

    public Matrix4f getViewportMatrix() {
        return viewportMatrix;
    }

    public Matrix4f getViewportMatrixInv() {
        return viewportMatrixInv;
    }

    public Vector4f[] getViewportGenerator() {
        return viewportGenerator;
    }

    public Vector3f[] getViewportOriginAxis() {
        return viewportOriginAxis;
    }

    double getzNear() {
        return zNear;
    }

    void setzNear(double zNear) {
        this.zNear = zNear;
    }

    double getzFar() {
        return zFar;
    }

    void setzFar(double zFar) {
        this.zFar = zFar;
    }

    public Manipulator getManipulator() {
        return manipulator;
    }

    public Map<String, List<Boolean>> getHideShowState() {
        return hideShowState;
    }

    public Map<String, List<Boolean>> getSelection() {
        return selection;
    }

    public Set<Vertex> getHiddenVertices() {
        return hiddenVertices;
    }

    public Set<Vertex> getSelectedVertices() {
        return selectedVertices;
    }
}

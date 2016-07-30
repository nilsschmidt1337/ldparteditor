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

import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.workbench.Composite3DState;

public class Composite3DViewState {

    private float zoom = 0f;
    private float zoom_exponent = 0f;
    private float viewport_pixel_per_ldu;
    private Vector4f offset = new Vector4f(0, 0, 0, 1f);
    public final Composite3DState STATE = new Composite3DState();
    private boolean negDeterminant = false;
    
    private final Matrix4f viewport_translation = new Matrix4f();
    private final Matrix4f viewport_rotation = new Matrix4f();
    private final Matrix4f viewport_matrix = new Matrix4f();
    private final Matrix4f viewport_matrix_inv = new Matrix4f();
    private final Manipulator manipulator = new Manipulator();

    /** The generator of the viewport space */
    private final Vector4f[] viewport_generator = new Vector4f[3];
    /** The origin axis coordinates of the viewport */
    private final Vector3f[] viewport_origin_axis = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
    /** The viewport z-Near value */
    private double zNear = 1000000f;
    /** The viewport z-Far value */
    private double zFar = 1000001f;
    
    private final HashMap<String, ArrayList<Boolean>> hideShowState = new HashMap<String, ArrayList<Boolean>>();
    
    float getZoom() {
        return zoom;
    }

    void setZoom(float zoom) {
        this.zoom = zoom;
    }

    float getZoom_exponent() {
        return zoom_exponent;
    }

    void setZoom_exponent(float zoom_exponent) {
        this.zoom_exponent = zoom_exponent;
    }
    
    public float getViewportPixelPerLDU() {
        return viewport_pixel_per_ldu;
    }

    public void setViewportPixelPerLDU(float viewport_pixel_per_ldu) {
        this.viewport_pixel_per_ldu = viewport_pixel_per_ldu;
    }

    public Vector4f getOffset() {
        return offset;
    }

    public void setOffset(Vector4f offset) {
        this.offset = offset;
    }
    
    public boolean hasNegDeterminant() {
        return negDeterminant;
    }

    public void setNegDeterminant(boolean negDeterminant) {
        this.negDeterminant = negDeterminant;
    }

    public Matrix4f getViewport_translation() {
        return viewport_translation;
    }

    public Matrix4f getViewport_rotation() {
        return viewport_rotation;
    }

    public Matrix4f getViewport_matrix() {
        return viewport_matrix;
    }

    public Matrix4f getViewport_matrix_inv() {
        return viewport_matrix_inv;
    }

    public Vector4f[] getViewport_generator() {
        return viewport_generator;
    }

    public Vector3f[] getViewport_origin_axis() {
        return viewport_origin_axis;
    }

    public double getzNear() {
        return zNear;
    }

    public void setzNear(double zNear) {
        this.zNear = zNear;
    }

    public double getzFar() {
        return zFar;
    }

    public void setzFar(double zFar) {
        this.zFar = zFar;
    }

    public Manipulator getManipulator() {
        return manipulator;
    }

    public HashMap<String, ArrayList<Boolean>> getHideShowState() {
        return hideShowState;
    }
}

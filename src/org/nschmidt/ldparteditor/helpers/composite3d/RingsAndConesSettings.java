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
package org.nschmidt.ldparteditor.helpers.composite3d;

import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * Settings for Rings and Cones
 * @author nils
 *
 */
public class RingsAndConesSettings {

    private ArrayList<String> angles = new ArrayList<String>();
    private boolean usingCones = false;
    private boolean usingExistingPrimitives = true;
    private boolean creatingShapeForNoSolution = false;
    private BigDecimal radius1 = BigDecimal.ONE;
    private BigDecimal radius2 = BigDecimal.TEN;
    private int angle = 47;
    public boolean isUsingCones() {
        return usingCones;
    }
    public void setUsingCones(boolean usingCones) {
        this.usingCones = usingCones;
    }
    public boolean isUsingExistingPrimitives() {
        return usingExistingPrimitives;
    }
    public void setUsingExistingPrimitives(boolean usingExistingPrimitives) {
        this.usingExistingPrimitives = usingExistingPrimitives;
    }
    public boolean isCreatingShapeForNoSolution() {
        return creatingShapeForNoSolution;
    }
    public void setCreatingShapeForNoSolution(boolean creatingShapeForNoSolution) {
        this.creatingShapeForNoSolution = creatingShapeForNoSolution;
    }
    public BigDecimal getRadius2() {
        return radius2;
    }
    public void setRadius2(BigDecimal radius2) {
        this.radius2 = radius2;
    }
    public BigDecimal getRadius1() {
        return radius1;
    }
    public void setRadius1(BigDecimal radius1) {
        this.radius1 = radius1;
    }
    public ArrayList<String> getAngles() {
        return angles;
    }
    public void setAngles(ArrayList<String> angles) {
        this.angles = angles;
    }
    public int getAngle() {
        return angle;
    }
    public void setAngle(int angle) {
        this.angle = angle;
    }
}
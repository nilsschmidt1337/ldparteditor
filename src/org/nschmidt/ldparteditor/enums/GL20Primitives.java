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
package org.nschmidt.ldparteditor.enums;

import org.nschmidt.ldparteditor.helpers.GearGL20;
import org.nschmidt.ldparteditor.helpers.SphereGL20;

/**
 * @author nils
 *
 */
public enum GL20Primitives {
    INSTANCE;

    public static final SphereGL20 SPHERE0 = new SphereGL20();
    public static final SphereGL20 SPHERE_INV0 = new SphereGL20();
    public static final SphereGL20 SPHERE1 = new SphereGL20(25f, 8);
    public static final SphereGL20 SPHERE_INV1 = new SphereGL20(-25f, 8);
    public static final SphereGL20 SPHERE2 = new SphereGL20(50f, 8);
    public static final SphereGL20 SPHERE_INV2 = new SphereGL20(-50f, 8);
    public static final SphereGL20 SPHERE3 = new SphereGL20(100f, 8);
    public static final SphereGL20 SPHERE_INV3 = new SphereGL20(-100f, 8);
    public static final SphereGL20 SPHERE4 = new SphereGL20(200f, 8);
    public static final SphereGL20 SPHERE_INV4 = new SphereGL20(-200f, 8);

    public static final GearGL20 GEAR_MENU = new GearGL20(.005f, 16, .005f, .0025f);
    public static final GearGL20 GEAR_MENU_INV = new GearGL20(-.005f, 16, -.005f, -.0025f);

    public static SphereGL20 sphere = new SphereGL20(100f, 8);
    public static SphereGL20 sphereInv = new SphereGL20(-100f, 8);
}

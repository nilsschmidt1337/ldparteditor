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
package org.nschmidt.ldparteditor.dialog.direction;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.math.BigDecimal;

import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enumtype.ManipulatorScope;
import org.nschmidt.ldparteditor.enumtype.WorkingMode;
import org.nschmidt.ldparteditor.helper.Manipulator;
import org.nschmidt.ldparteditor.helper.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.TransformationModeToolItem;

/**
 *
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. It overrides the {@code open()} method to invoke the
 * listener definitions ;)
 *
 * @author nils
 *
 */
public class DirectionDialog extends DirectionDesign {

    private boolean stopCalculations = false;

    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;

    private static final int RHO = 0;
    private static final int THETA = 1;
    private static final int PHI = 2;

    private final Manipulator mani;

    private static Vector3d direction = null;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public DirectionDialog(Shell parentShell, Manipulator mani) {
        super(parentShell);
        this.mani = mani;
        // Start only in global mode, for consistency reasons.
        if (transformationMode == ManipulatorScope.LOCAL) {
            transformationMode = ManipulatorScope.GLOBAL;
            cart = localToGlobal(cart);
            sphe = cartesianToSpherical(cart);
        }
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        widgetUtil(btnMXPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnGlobalPtr[0].getParent());
            btnGlobalPtr[0].setSelection(true);
            BigDecimal[] axis = mani.getAccurateXaxis();
            cart = new double[]{axis[0].doubleValue(), axis[1].doubleValue(), axis[2].doubleValue()};
            sphe = cartesianToSpherical(cart);
            updateValues();
        });
        widgetUtil(btnMYPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnGlobalPtr[0].getParent());
            btnGlobalPtr[0].setSelection(true);
            BigDecimal[] axis = mani.getAccurateYaxis();
            cart = new double[]{axis[0].doubleValue(), axis[1].doubleValue(), axis[2].doubleValue()};
            sphe = cartesianToSpherical(cart);
            updateValues();
        });
        widgetUtil(btnMZPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnGlobalPtr[0].getParent());
            btnGlobalPtr[0].setSelection(true);
            BigDecimal[] axis = mani.getAccurateZaxis();
            cart = new double[]{axis[0].doubleValue(), axis[1].doubleValue(), axis[2].doubleValue()};
            sphe = cartesianToSpherical(cart);
            updateValues();
        });
        widgetUtil(btnLocalPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnLocalPtr[0].getParent());
            btnLocalPtr[0].setSelection(true);
            if (transformationMode != ManipulatorScope.LOCAL) {
                TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE);
                transformationMode = ManipulatorScope.LOCAL;
                cart = globalToLocal(cart);
                sphe = cartesianToSpherical(cart);
            }
            updateValues();
        });
        widgetUtil(btnGlobalPtr[0]).addSelectionListener(e -> {
            WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btnGlobalPtr[0].getParent());
            btnGlobalPtr[0].setSelection(true);
            if (transformationMode != ManipulatorScope.GLOBAL) {
                TransformationModeToolItem.setWorkingAction(WorkingMode.MOVE_GLOBAL);
                transformationMode = ManipulatorScope.GLOBAL;
                cart = localToGlobal(cart);
                sphe = cartesianToSpherical(cart);
            }
            updateValues();
        });
        spnXPtr[0].addValueChangeListener(spn -> {
            if (stopCalculations) return;
            cart = new double[]{spnXPtr[0].getValue().doubleValue(), spnYPtr[0].getValue().doubleValue(), spnZPtr[0].getValue().doubleValue()};
            sphe = cartesianToSpherical(cart);
            updateValues();
        });
        spnYPtr[0].addValueChangeListener(spn -> {
            if (stopCalculations) return;
            cart = new double[]{spnXPtr[0].getValue().doubleValue(), spnYPtr[0].getValue().doubleValue(), spnZPtr[0].getValue().doubleValue()};
            sphe = cartesianToSpherical(cart);
            updateValues();
        });
        spnZPtr[0].addValueChangeListener(spn -> {
            if (stopCalculations) return;
            cart = new double[]{spnXPtr[0].getValue().doubleValue(), spnYPtr[0].getValue().doubleValue(), spnZPtr[0].getValue().doubleValue()};
            sphe = cartesianToSpherical(cart);
            updateValues();
        });
        spnRhoPtr[0].addValueChangeListener(spn -> {
            if (stopCalculations) return;
            sphe = new double[]{spnRhoPtr[0].getValue().doubleValue(), spnThetaPtr[0].getValue().doubleValue(), spnPhiPtr[0].getValue().doubleValue()};
            cart = sphericalToCartesian(sphe);
            updateValues();
        });
        spnThetaPtr[0].addValueChangeListener(spn -> {
            if (stopCalculations) return;
            sphe = new double[]{spnRhoPtr[0].getValue().doubleValue(), spnThetaPtr[0].getValue().doubleValue(), spnPhiPtr[0].getValue().doubleValue()};
            cart = sphericalToCartesian(sphe);
            updateValues();
        });
        spnPhiPtr[0].addValueChangeListener(spn -> {
            if (stopCalculations) return;
            sphe = new double[]{spnRhoPtr[0].getValue().doubleValue(), spnThetaPtr[0].getValue().doubleValue(), spnPhiPtr[0].getValue().doubleValue()};
            cart = sphericalToCartesian(sphe);
            updateValues();
        });
        return super.open();
    }

    public static ManipulatorScope getTransformationMode() {
        return transformationMode;
    }

    private void updateValues() {
        stopCalculations = true;
        spnXPtr[0].setValue(new BigDecimal(cart[X]));
        spnYPtr[0].setValue(new BigDecimal(cart[Y]));
        spnZPtr[0].setValue(new BigDecimal(cart[Z]));
        spnRhoPtr[0].setValue(new BigDecimal(sphe[RHO]));
        spnThetaPtr[0].setValue(new BigDecimal(sphe[THETA]));
        spnPhiPtr[0].setValue(new BigDecimal(sphe[PHI]));
        stopCalculations = false;
    }

    private double[] cartesianToSpherical(double[] cart) {
        double rho = Math.sqrt(
                cart[X] * cart[X] +
                cart[Y] * cart[Y] +
                cart[Z] * cart[Z]
                );
        if (Math.abs(rho) < 0.0001) {
            return new double[]{0.0, 0.0, 0.0};
        }
        double theta = Math.acos(cart[Z] / rho) / Math.PI * 180.0;
        double phi = Math.atan2(cart[Y], cart[X]) / Math.PI * 180.0;
        return new double[]{rho, theta, phi};
    }

    private double[] sphericalToCartesian(double[] sphe) {
        double t = sphe[THETA] * Math.PI / 180.0;
        double p = sphe[PHI] * Math.PI / 180.0;
        double x = sphe[RHO] * Math.sin(t) * Math.cos(p);
        double y = sphe[RHO] * Math.sin(t) * Math.sin(p) ;
        double z = sphe[RHO] * Math.cos(t);
        return new double[]{x, y, z};
    }

    private double[] globalToLocal(double[] global) {
        Vertex vert = new Vertex(new BigDecimal(global[0]), new BigDecimal(global[1]), new BigDecimal(global[2]));
        BigDecimal[] pos = mani.getAccuratePosition();
        Vector3d result = mani.getAccurateRotation().transform(Vector3d.sub(new Vector3d(vert), new Vector3d(pos[0], pos[1], pos[2])));
        return new double[]{result.x.doubleValue(), result.y.doubleValue(), result.z.doubleValue()};
    }

    private double[] localToGlobal(double[] local) {
        Vertex vert = new Vertex(new BigDecimal(local[0]), new BigDecimal(local[1]), new BigDecimal(local[2]));
        BigDecimal[] pos = mani.getAccuratePosition();
        Vector3d result = Vector3d.add(mani.getAccurateRotation().invert().transform(new Vector3d(vert)), new Vector3d(pos[0], pos[1], pos[2]));
        return new double[]{result.x.doubleValue(), result.y.doubleValue(), result.z.doubleValue()};
    }

    private static double[] localToGlobal(double[] local, Manipulator mani) {
        Vertex vert = new Vertex(new BigDecimal(local[0]), new BigDecimal(local[1]), new BigDecimal(local[2]));
        BigDecimal[] pos = mani.getAccuratePosition();
        Vector3d result = Vector3d.add(mani.getAccurateRotation().invert().transform(new Vector3d(vert)), new Vector3d(pos[0], pos[1], pos[2]));
        return new double[]{result.x.doubleValue(), result.y.doubleValue(), result.z.doubleValue()};
    }

    public static Vector3d getDirection() {
        return direction;
    }

    public static boolean calculateDirection(Manipulator mani) {
        double[] result = cart;
        if (getTransformationMode() == ManipulatorScope.LOCAL) {
            result = localToGlobal(cart, mani);
        }
        direction = new Vector3d(new BigDecimal(result[0]), new BigDecimal(result[1]), new BigDecimal(result[2]));
        if (direction.length().compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        direction.normalise(direction);
        return true;
    }
}

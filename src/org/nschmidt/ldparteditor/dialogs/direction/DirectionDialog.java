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
package org.nschmidt.ldparteditor.dialogs.direction;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import java.math.BigDecimal;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composites.ToolItem;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionListener;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

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

    private final int X = 0;
    private final int Y = 1;
    private final int Z = 2;

    private final int RHO = 0;
    private final int THETA = 1;
    private final int PHI = 2;

    private final Manipulator mani;

    static Vector3d direction = null;

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
        WidgetUtil(btn_mX[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn_Global[0].getParent());
                btn_Global[0].setSelection(true);
                BigDecimal[] axis = mani.getAccurateXaxis();
                cart = new double[]{axis[0].doubleValue(), axis[1].doubleValue(), axis[2].doubleValue()};
                sphe = cartesianToSpherical(cart);
                updateValues();
            }
        });
        WidgetUtil(btn_mY[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn_Global[0].getParent());
                btn_Global[0].setSelection(true);
                BigDecimal[] axis = mani.getAccurateYaxis();
                cart = new double[]{axis[0].doubleValue(), axis[1].doubleValue(), axis[2].doubleValue()};
                sphe = cartesianToSpherical(cart);
                updateValues();
            }
        });
        WidgetUtil(btn_mZ[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn_Global[0].getParent());
                btn_Global[0].setSelection(true);
                BigDecimal[] axis = mani.getAccurateZaxis();
                cart = new double[]{axis[0].doubleValue(), axis[1].doubleValue(), axis[2].doubleValue()};
                sphe = cartesianToSpherical(cart);
                updateValues();
            }
        });
        WidgetUtil(btn_Local[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn_Local[0].getParent());
                btn_Local[0].setSelection(true);
                if (transformationMode != ManipulatorScope.LOCAL) {
                    Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE);
                    transformationMode = ManipulatorScope.LOCAL;
                    cart = globalToLocal(cart);
                    sphe = cartesianToSpherical(cart);
                }
                updateValues();
            }
        });
        WidgetUtil(btn_Global[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                WidgetSelectionHelper.unselectAllChildButtons((ToolItem) btn_Global[0].getParent());
                btn_Global[0].setSelection(true);
                if (transformationMode != ManipulatorScope.GLOBAL) {
                    Editor3DWindow.getWindow().setWorkingAction(WorkingMode.MOVE_GLOBAL);
                    transformationMode = ManipulatorScope.GLOBAL;
                    cart = localToGlobal(cart);
                    sphe = cartesianToSpherical(cart);
                }
                updateValues();
            }
        });
        spn_X[0].addValueChangeListener(spn -> {
            if (stopCalculations) return;
            cart = new double[]{spn_X[0].getValue().doubleValue(), spn_Y[0].getValue().doubleValue(), spn_Z[0].getValue().doubleValue()};
            sphe = cartesianToSpherical(cart);
            updateValues();
        });
        spn_Y[0].addValueChangeListener(spn -> {
            if (stopCalculations) return;
            cart = new double[]{spn_X[0].getValue().doubleValue(), spn_Y[0].getValue().doubleValue(), spn_Z[0].getValue().doubleValue()};
            sphe = cartesianToSpherical(cart);
            updateValues();
        });
        spn_Z[0].addValueChangeListener(spn -> {
            if (stopCalculations) return;
            cart = new double[]{spn_X[0].getValue().doubleValue(), spn_Y[0].getValue().doubleValue(), spn_Z[0].getValue().doubleValue()};
            sphe = cartesianToSpherical(cart);
            updateValues();
        });
        spn_Rho[0].addValueChangeListener(spn -> {
            if (stopCalculations) return;
            sphe = new double[]{spn_Rho[0].getValue().doubleValue(), spn_Theta[0].getValue().doubleValue(), spn_Phi[0].getValue().doubleValue()};
            cart = sphericalToCartesian(sphe);
            updateValues();
        });
        spn_Theta[0].addValueChangeListener(spn -> {
            if (stopCalculations) return;
            sphe = new double[]{spn_Rho[0].getValue().doubleValue(), spn_Theta[0].getValue().doubleValue(), spn_Phi[0].getValue().doubleValue()};
            cart = sphericalToCartesian(sphe);
            updateValues();
        });
        spn_Phi[0].addValueChangeListener(spn -> {
            if (stopCalculations) return;
            sphe = new double[]{spn_Rho[0].getValue().doubleValue(), spn_Theta[0].getValue().doubleValue(), spn_Phi[0].getValue().doubleValue()};
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
        spn_X[0].setValue(new BigDecimal(cart[X]));
        spn_Y[0].setValue(new BigDecimal(cart[Y]));
        spn_Z[0].setValue(new BigDecimal(cart[Z]));
        spn_Rho[0].setValue(new BigDecimal(sphe[RHO]));
        spn_Theta[0].setValue(new BigDecimal(sphe[THETA]));
        spn_Phi[0].setValue(new BigDecimal(sphe[PHI]));
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
        return new double[]{result.X.doubleValue(), result.Y.doubleValue(), result.Z.doubleValue()};
    }

    private double[] localToGlobal(double[] local) {
        Vertex vert = new Vertex(new BigDecimal(local[0]), new BigDecimal(local[1]), new BigDecimal(local[2]));
        BigDecimal[] pos = mani.getAccuratePosition();
        Vector3d result = Vector3d.add(mani.getAccurateRotation().invert().transform(new Vector3d(vert)), new Vector3d(pos[0], pos[1], pos[2]));
        return new double[]{result.X.doubleValue(), result.Y.doubleValue(), result.Z.doubleValue()};
    }

    private static double[] localToGlobal(double[] local, Manipulator mani) {
        Vertex vert = new Vertex(new BigDecimal(local[0]), new BigDecimal(local[1]), new BigDecimal(local[2]));
        BigDecimal[] pos = mani.getAccuratePosition();
        Vector3d result = Vector3d.add(mani.getAccurateRotation().invert().transform(new Vector3d(vert)), new Vector3d(pos[0], pos[1], pos[2]));
        return new double[]{result.X.doubleValue(), result.Y.doubleValue(), result.Z.doubleValue()};
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

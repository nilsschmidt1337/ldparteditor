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
package org.nschmidt.ldparteditor.dialog.calibrate;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GDataPNG;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexInfo;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 *
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. It overrides the {@code open()} method to invoke the
 * listener definitions ;)
 */
public class CalibrateDialog extends CalibrateDesign {

    private boolean update = false;
    private final DatFile df;
    private BigDecimal newDistLDU = BigDecimal.ZERO;

    /**
     * Create the dialog.
     */
    public CalibrateDialog(Shell parentShell, DatFile df, Set<VertexInfo> vis) {
        super(parentShell, vis);
        this.df = df;
    }

    @Override
    public int open() {
        super.create();
        getShell().setText(I18n.CALIBRATE_BG_IMAGE);
        // MARK All final listeners will be configured here..
        widgetUtil(btnLDUPtr[0]).addSelectionListener(_ -> {
            if (update) return;
            uncheckAllUnits();
            btnLDUPtr[0].setSelection(true);
        });
        widgetUtil(btnMMPtr[0]).addSelectionListener(_ -> {
            if (update) return;
            uncheckAllUnits();
            btnMMPtr[0].setSelection(true);
        });
        widgetUtil(btnInchPtr[0]).addSelectionListener(_ -> {
            if (update) return;
            uncheckAllUnits();
            btnInchPtr[0].setSelection(true);
        });
        widgetUtil(btnStudPtr[0]).addSelectionListener(_ -> {
            if (update) return;
            uncheckAllUnits();
        });
        spnLDUPtr[0].addValueChangeListener(spn -> {
            newDistLDU = spn.getValue();
            if (update) return;
            uncheckAllUnits();
            update = true;
            spnInchPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_INCH), Threshold.MC));
            spnMMPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_MM), Threshold.MC));
            spnStudPtr[0].setValue(spnLDUPtr[0].getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_STUD), Threshold.MC).setScale(1, RoundingMode.HALF_UP));
            update = false;
            btnLDUPtr[0].setSelection(true);
        });
        spnMMPtr[0].addValueChangeListener(spn -> {
            if (update) return;
            uncheckAllUnits();
            update = true;
            spnLDUPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_MM_TO_LDU), Threshold.MC));
            spnInchPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_MM_TO_INCH), Threshold.MC));
            spnStudPtr[0].setValue(spnLDUPtr[0].getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_STUD), Threshold.MC).setScale(1, RoundingMode.HALF_UP));
            update = false;
            btnMMPtr[0].setSelection(true);
        });
        spnInchPtr[0].addValueChangeListener(spn -> {
            if (update) return;
            uncheckAllUnits();
            update = true;
            spnLDUPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_INCH_TO_LDU), Threshold.MC));
            spnMMPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_INCH_TO_MM), Threshold.MC));
            spnStudPtr[0].setValue(spnLDUPtr[0].getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_STUD), Threshold.MC).setScale(1, RoundingMode.HALF_UP));
            update = false;
            btnInchPtr[0].setSelection(true);
        });
        spnStudPtr[0].addValueChangeListener(spn -> {
            if (update) return;
            uncheckAllUnits();
            update = true;
            spnLDUPtr[0].setValue(spn.getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_STUD_TO_LDU), Threshold.MC));
            spnInchPtr[0].setValue(spnLDUPtr[0].getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_INCH), Threshold.MC));
            spnMMPtr[0].setValue(spnLDUPtr[0].getValue().multiply(new BigDecimal(I18n.UNITS_FACTOR_LDU_TO_MM), Threshold.MC));
            update = false;
            btnStudPtr[0].setSelection(true);
        });

        final BigDecimal initialValue = spnLDUPtr[0].getValue();
        spnLDUPtr[0].setValue(BigDecimal.ZERO);
        spnLDUPtr[0].setValue(initialValue);
        return super.open();
    }

    private void uncheckAllUnits() {
        update = true;
        btnLDUPtr[0].setSelection(false);
        btnMMPtr[0].setSelection(false);
        btnInchPtr[0].setSelection(false);
        btnStudPtr[0].setSelection(false);
        update = false;
    }

    public void performCalibration() {
        final VertexManager vm = df.getVertexManager();
        vm.addSnapshot();
        if (BigDecimal.ZERO.compareTo(newDistLDU) == 0 || BigDecimal.ZERO.compareTo(oldDistLDU) == 0) {
            return;
        }

        final GDataPNG png = vm.getSelectedBgPicture();
        if (png == null || png.parent != View.DUMMY_REFERENCE) return;
        final Matrix4f mf = png.getMatrix();

        final BigDecimal factor = newDistLDU.divide(oldDistLDU, Threshold.MC);

        final Vector4f rawOffset = calibrateOffset(mf, new Vector4f(png.offset.xp.floatValue(), png.offset.yp.floatValue(), png.offset.zp.floatValue(), 1f),
                png.angleA.floatValue(), png.angleB.floatValue(), png.angleC.floatValue(),
                new Vector4f(start.getXf(), start.getYf(), start.getZf(), 1f), factor.floatValue());
        final Vertex offset = new Vertex(
            MathHelper.roundBigDecimalAlways(BigDecimal.valueOf(rawOffset.x)),
            MathHelper.roundBigDecimalAlways(BigDecimal.valueOf(rawOffset.y)),
            MathHelper.roundBigDecimalAlways(BigDecimal.valueOf(rawOffset.z)));

        // Scale adjustment is correct.
        final Vector3d rawScale = new Vector3d(png.scale).scale(factor);
        final Vertex scale = new Vertex(
            MathHelper.roundBigDecimalAlways(rawScale.x),
            MathHelper.roundBigDecimalAlways(rawScale.y),
            MathHelper.roundBigDecimalAlways(rawScale.z));

        final StringBuilder sb = new StringBuilder();
        sb.append("0 !LPE PNG "); //$NON-NLS-1$
        sb.append(' ');
        sb.append(MathHelper.bigDecimalToString(offset.xp));
        sb.append(' ');
        sb.append(MathHelper.bigDecimalToString(offset.yp));
        sb.append(' ');
        sb.append(MathHelper.bigDecimalToString(offset.zp));
        sb.append(' ');
        sb.append(MathHelper.bigDecimalToString(png.angleA));
        sb.append(' ');
        sb.append(MathHelper.bigDecimalToString(png.angleB));
        sb.append(' ');
        sb.append(MathHelper.bigDecimalToString(png.angleC));
        sb.append(' ');
        sb.append(MathHelper.bigDecimalToString(scale.xp));
        sb.append(' ');
        sb.append(MathHelper.bigDecimalToString(scale.yp));
        sb.append(' ');
        sb.append(png.texturePath);
        final GDataPNG newPng = new GDataPNG(sb.toString(), offset, png.angleA, png.angleB, png.angleC, scale, png.texturePath, View.DUMMY_REFERENCE);
        final int oldLine = df.getDrawPerLineNoClone().getKey(png);
        png.getBefore().setNext(newPng);
        newPng.setNext(png.getNext());
        vm.remove(png);
        df.getDrawPerLineNoClone().put(oldLine, newPng);

        NLogger.debug(getClass(), png.toString());
        NLogger.debug(getClass(), newPng.toString());
        vm.validateState();
        vm.setModified(true, true);
    }

    public static Vector4f calibrateOffset(Matrix4f imageMatrix, final Vector4f imageOffset,
            float angleA, float angleB, float angleC,
            final Vector4f pivot, float scale) {

        Matrix4f imageBasis = Matrix4f.setIdentity(new Matrix4f());
        Matrix4f.rotate((float) (angleC / 180.0 * Math.PI), new Vector3f(0f, 0f, 1f), imageBasis, imageBasis);
        Matrix4f.rotate((float) (angleB / 180.0 * Math.PI), new Vector3f(1f, 0f, 0f), imageBasis, imageBasis);
        Matrix4f.rotate((float) (angleA / 180.0 * Math.PI), new Vector3f(0f, 1f, 0f), imageBasis, imageBasis);

        imageBasis = Matrix4f.invert(imageBasis, null);

        final Vector4f realImageOffset = (Vector4f) new Vector4f(imageMatrix.m30, imageMatrix.m31, imageMatrix.m32, 1000f).scale(.001f);
        final Vector4f pivotToImageCenter = Vector4f.sub(pivot, realImageOffset, null);
        final Vector4f pivotToImageCenterScaled = (Vector4f) pivotToImageCenter.scale(scale).scale(.5f);
        final Vector4f offsetDelta = (Vector4f) Matrix4f.transform(imageBasis, pivotToImageCenterScaled, null).negate();

        return Vector4f.add(imageOffset, offsetDelta, null);
    }
}

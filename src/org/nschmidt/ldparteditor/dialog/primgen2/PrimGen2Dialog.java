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
package org.nschmidt.ldparteditor.dialog.primgen2;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.Perspective;
import org.nschmidt.ldparteditor.enumtype.TextTask;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.WidgetSelectionListener;
import org.nschmidt.ldparteditor.helper.composite3d.ViewIdleManager;
import org.nschmidt.ldparteditor.helper.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.text.SyntaxFormatter;
import org.nschmidt.ldparteditor.widget.DecimalValueChangeAdapter;
import org.nschmidt.ldparteditor.widget.IntValueChangeAdapter;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 *
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. It overrides the {@code open()} method to invoke the
 * listener definitions ;)
 */
public class PrimGen2Dialog extends PrimGen2Design {

    private static final int CIRCLE = 0;
    private static final int RING = 1;
    private static final int CONE = 2;
    private static final int TORUS = 3;
    private static final int CYLINDER = 4;
    private static final int DISC = 5;
    private static final int DISC_NEGATIVE = 6;
    private static final int CHORD = 7;

    private boolean doUpdate = false;
    private boolean ok = false;

    private final DecimalFormat decvformat4f = new DecimalFormat(View.NUMBER_FORMAT4F, new DecimalFormatSymbols(MyLanguage.getLocale()));
    private final DecimalFormat decvformat0f = new DecimalFormat(View.NUMBER_FORMAT0F, new DecimalFormatSymbols(MyLanguage.getLocale()));

    private final DecimalFormat decformat4f = new DecimalFormat(View.NUMBER_FORMAT4F, new DecimalFormatSymbols(Locale.ENGLISH));

    private String name = "1-4edge.dat"; //$NON-NLS-1$

    private enum EventType {
        SPN,
        CBO
    }

    private String resPrefix = ""; //$NON-NLS-1$

    /**
     * Create the dialog.
     *
     * @param parentShell
     * @param es
     */
    public PrimGen2Dialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public int open() {
        super.create();

        final DatFile oldDf = Project.getFileToEdit();

        decformat4f.setRoundingMode(RoundingMode.HALF_UP);

        final SyntaxFormatter syntaxFormatter = new SyntaxFormatter(txtDataPtr[0]);

        spnMajorPtr[0].setValue(2);
        spnMinorPtr[0].setValue(BigDecimal.ONE);

        Display.getCurrent().asyncExec(() -> {

            lblStandardPtr[0].setText(I18n.PRIMGEN_STANDARD);

            final StringBuilder sb = new StringBuilder();
            sb.append("0 Circle 0.25\n"); //$NON-NLS-1$
            sb.append("0 Name: 1-4edge.dat\n"); //$NON-NLS-1$

            final UserSettingState user = WorkbenchManager.getUserSettingState();

            String ldrawName = user.getLdrawUserName();
            String realName = user.getRealUserName();
            if (ldrawName == null || ldrawName.isEmpty()) {
                if (realName == null || realName.isEmpty()) {
                    sb.append("0 Author: Primitive Generator 2\n"); //$NON-NLS-1$
                } else {
                    sb.append("0 Author: " + realName + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else {
                if (realName == null || realName.isEmpty()) {
                    sb.append("0 Author: [" + ldrawName + "]\n"); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    sb.append("0 Author: " + realName + " [" + ldrawName + "]\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }

            sb.append("0 !LDRAW_ORG Unofficial_Primitive\n"); //$NON-NLS-1$
            sb.append("0 !LICENSE Licensed under CC BY 4.0 : see CAreadme.txt\n\n"); //$NON-NLS-1$

            sb.append("0 BFC CERTIFY CCW\n\n"); //$NON-NLS-1$

            sb.append("2 24 1 0 0 0.9239 0 0.3827\n"); //$NON-NLS-1$
            sb.append("2 24 0.9239 0 0.3827 0.7071 0 0.7071\n"); //$NON-NLS-1$
            sb.append("2 24 0.7071 0 0.7071 0.3827 0 0.9239\n"); //$NON-NLS-1$
            sb.append("2 24 0.3827 0 0.9239 0 0 1\n"); //$NON-NLS-1$
            sb.append("0 // Build by LDPartEditor (PrimGen 2.X)"); //$NON-NLS-1$

            c3d.setRenderMode(6);
            c3d.getModifier().switchMeshLines(false);
            txtDataPtr[0].setText(sb.toString());
        });

        // MARK All final listeners will be configured here..

        txtDataPtr[0].addLineStyleListener(e -> {
            // So the line will be formated with the syntax formatter from
            // the CompositeText.
            final VertexManager vm = df.getVertexManager();
            final GData data = df.getDrawPerLineNoClone().getValue(txtDataPtr[0].getLineAtOffset(e.lineOffset) + 1);
            boolean isSelected = vm.isSyncWithTextEditor() && vm.getSelectedData().contains(data);
            isSelected = isSelected || vm.isSyncWithTextEditor() && GDataCSG.getSelection(df).contains(data);
            syntaxFormatter.format(e,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    0f, false, isSelected, GData.CACHE_duplicates.containsKey(data), data == null || data.isVisible(), df);
        });

        txtDataPtr[0].addExtendedModifyListener(event -> {
            df.disposeData();
            df.setText(txtDataPtr[0].getText());
            df.parseForData(false);

            if (NLogger.debugging) {
                int c1 = txtDataPtr[0].getLineCount();
                int c2 = txtData2Ptr[0].getLineCount();
                int matches = 0;
                Set<String> lines = new HashSet<>();
                for (int i = 0; i < c2; i++) {
                    String line = txtData2Ptr[0].getLine(i);
                    if (!line.isEmpty() && !line.startsWith("0") && !line.startsWith("5")) { //$NON-NLS-1$ //$NON-NLS-2$
                        lines.add(line);
                    }
                }
                lblStandardPtr[0].setText(""); //$NON-NLS-1$
                for (int i = 0; i < c1; i++) {
                    String line = txtDataPtr[0].getLine(i);
                    if (lines.contains(line)) {
                        matches = matches + 1;
                    } else if (lblStandardPtr[0].getText().isEmpty() && !line.isEmpty() && !line.startsWith("0") && !line.startsWith("5")) { //$NON-NLS-1$ //$NON-NLS-2$
                        lblStandardPtr[0].setText(i + 1 + " " + line); //$NON-NLS-1$
                    }
                }

                lblStandardPtr[0].setText("Coverage: " + matches * 100d / lines.size() + "% " + lblStandardPtr[0].getText()); //$NON-NLS-1$ //$NON-NLS-2$
            }

            Display.getCurrent().asyncExec(c3d.getRenderer()::drawScene);
        });

        txtDataPtr[0].addListener(SWT.KeyDown, event -> {

            final int keyCode = event.keyCode;
            final boolean ctrlPressed = (event.stateMask & SWT.CTRL) != 0;
            final boolean altPressed = (event.stateMask & SWT.ALT) != 0;
            final boolean shiftPressed = (event.stateMask & SWT.SHIFT) != 0;
            StringBuilder sb = new StringBuilder();
            sb.append(keyCode);
            sb.append(ctrlPressed ? "+Ctrl" : ""); //$NON-NLS-1$//$NON-NLS-2$
            sb.append(altPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
            sb.append(shiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
            TextTask task = KeyStateManager.getTextTaskmap().get(sb.toString());

            if (task != null) {
                ViewIdleManager.pause[0].compareAndSet(false, true);
                if (task == TextTask.EDITORTEXT_SELECTALL) {
                    txtDataPtr[0].setSelection(0, txtDataPtr[0].getText().length());
                }
            }
        });

        widgetUtil(mntmDeletePtr[0]).addSelectionListener(e -> {
            final int x = txtDataPtr[0].getSelection().x;
            if (txtDataPtr[0].getSelection().y == x) {
                final int start = txtDataPtr[0].getOffsetAtLine(txtDataPtr[0].getLineAtOffset(x));
                txtDataPtr[0].setSelection(start, start + txtDataPtr[0].getLine(txtDataPtr[0].getLineAtOffset(x)).length());
            }
            final int x2 = txtDataPtr[0].getSelection().x;
            if (txtDataPtr[0].getSelection().y == x2) {
                txtDataPtr[0].forceFocus();
                return;
            }
            txtDataPtr[0].insert(""); //$NON-NLS-1$
            txtDataPtr[0].setSelection(new Point(x, x));
            txtDataPtr[0].forceFocus();
        });
        widgetUtil(mntmCopyPtr[0]).addSelectionListener(e -> txtDataPtr[0].copy());
        widgetUtil(mntmCutPtr[0]).addSelectionListener(e -> txtDataPtr[0].cut());
        widgetUtil(mntmPastePtr[0]).addSelectionListener(e -> txtDataPtr[0].paste());

        btnOkPtr[0].removeListener(SWT.Selection, btnOkPtr[0].getListeners(SWT.Selection)[0]);
        btnCancelPtr[0].removeListener(SWT.Selection, btnCancelPtr[0].getListeners(SWT.Selection)[0]);

        widgetUtil(btnOkPtr[0]).addSelectionListener(e -> 
            savePrimitive(Project.getProjectPath() + File.separator + "p" + File.separator + resPrefix + name) //$NON-NLS-1$
        );

        widgetUtil(btnCancelPtr[0]).addSelectionListener(e -> getShell().close());

        widgetUtil(btnSaveAsPtr[0]).addSelectionListener(e -> 
            savePrimitive(null)
        );

        widgetUtil(btnTopPtr[0]).addSelectionListener(e -> {
            c3d.getPerspectiveCalculator().setPerspective(Perspective.TOP);
            Display.getCurrent().asyncExec(c3d.getRenderer()::drawScene);
        });

        widgetUtil(cmbTypePtr[0]).addSelectionListener(e -> {

            doUpdate = true;

            switch (cmbTypePtr[0].getSelectionIndex()) {
            case CIRCLE, CYLINDER, DISC, DISC_NEGATIVE, CHORD:
                lblMinorPtr[0].setText(I18n.PRIMGEN_MINOR);
                lblMajorPtr[0].setEnabled(false);
                lblMinorPtr[0].setEnabled(false);
                lblSizePtr[0].setEnabled(false);
                lblTorusTypePtr[0].setEnabled(false);
                lblEdgesPerCrossSectionPtr[0].setEnabled(false);
                spnMajorPtr[0].setEnabled(false);
                spnMinorPtr[0].setEnabled(false);
                spnSizePtr[0].setEnabled(false);
                cmbTorusTypePtr[0].setEnabled(false);
                spnEdgesPerCrossSectionsPtr[0] .setEnabled(false);
                spnMinorPtr[0].setNumberFormat(decvformat0f);
                spnSizePtr[0].setNumberFormat(decvformat0f);
                spnSizePtr[0].setValue(BigDecimal.ONE);
                cmbTorusTypePtr[0].select(1);
                break;
            case RING, CONE:
                lblMinorPtr[0].setText(I18n.PRIMGEN_WIDTH);
                lblMajorPtr[0].setEnabled(false);
                lblMinorPtr[0].setEnabled(true);
                lblSizePtr[0].setEnabled(true);
                lblTorusTypePtr[0].setEnabled(false);
                lblEdgesPerCrossSectionPtr[0].setEnabled(false);
                spnMajorPtr[0].setEnabled(false);
                spnMinorPtr[0].setEnabled(true);
                spnSizePtr[0].setEnabled(true);
                cmbTorusTypePtr[0].setEnabled(false);
                spnEdgesPerCrossSectionsPtr[0] .setEnabled(false);
                spnMinorPtr[0].setNumberFormat(decvformat4f);
                spnSizePtr[0].setNumberFormat(decvformat4f);
                spnSizePtr[0].setValue(BigDecimal.ONE);
                cmbTorusTypePtr[0].select(1);
                break;
            case TORUS:
                lblMinorPtr[0].setText(I18n.PRIMGEN_MINOR);
                lblMajorPtr[0].setEnabled(true);
                lblMinorPtr[0].setEnabled(true);
                lblSizePtr[0].setEnabled(false);
                lblTorusTypePtr[0].setEnabled(true);
                lblEdgesPerCrossSectionPtr[0].setEnabled(true);
                spnMajorPtr[0].setEnabled(true);
                spnMinorPtr[0].setEnabled(true);
                spnSizePtr[0].setEnabled(false);
                cmbTorusTypePtr[0].setEnabled(true);
                spnEdgesPerCrossSectionsPtr[0] .setEnabled(true);
                spnMinorPtr[0].setNumberFormat(decvformat0f);
                spnSizePtr[0].setNumberFormat(decvformat4f);
                spnSizePtr[0].setValue(BigDecimal.ONE);
                cmbTorusTypePtr[0].select(1);
                break;
            default:
                break;
            }

            BigDecimal minor = spnMinorPtr[0].getValue();
            BigDecimal size = spnSizePtr[0].getValue();

            spnMinorPtr[0].setValue(BigDecimal.ZERO);
            spnSizePtr[0].setValue(BigDecimal.ZERO);

            spnMinorPtr[0].setValue(minor);
            spnSizePtr[0].setValue(size);


            doUpdate = false;

            rebuildPrimitive(EventType.CBO, cmbTypePtr[0]);
        });

        final WidgetSelectionListener sa = e -> rebuildPrimitive(EventType.CBO, e.widget);

        final IntValueChangeAdapter vca = spn -> rebuildPrimitive(EventType.SPN, spn);
        final DecimalValueChangeAdapter vcad = spn -> rebuildPrimitive(EventType.SPN, spn);

        spnDivisionsPtr[0].addValueChangeListener(vca);
        spnMajorPtr[0].addValueChangeListener(vca);
        spnMinorPtr[0].addValueChangeListener(vcad);
        spnSegmentsPtr[0].addValueChangeListener(vca);
        spnSizePtr[0].addValueChangeListener(vcad);
        spnEdgesPerCrossSectionsPtr[0].addValueChangeListener(vca);
        
        widgetUtil(cmbDivisionsPtr[0]).addSelectionListener(sa);
        widgetUtil(cmbSegmentsPtr[0]).addSelectionListener(sa);
        widgetUtil(cmbTorusTypePtr[0]).addSelectionListener(sa);
        widgetUtil(cmbWindingPtr[0]).addSelectionListener(sa);

        float backup = View.edgeThreshold;
        View.edgeThreshold = 5e6f;
        int result = super.open();
        View.edgeThreshold = backup;
        Project.getUnsavedFiles().remove(df);
        df.disposeData();
        Project.getOpenedFiles().remove(df);
        final boolean syncTabs = WorkbenchManager.getUserSettingState().isSyncingTabs();
        if (!ok && syncTabs || !syncTabs) {
            Project.setFileToEdit(oldDf);
        }
        Editor3DWindow.getWindow().cleanupClosedData();
        Editor3DWindow.getWindow().updateTreeUnsavedEntries();
        return result;
    }

    private void savePrimitive(final String filePath) {
        EditorTextWindow w = null;
        for (EditorTextWindow w2 : Project.getOpenTextWindows()) {
            if (w2.getTabFolder().getItems().length == 0) {
                w = w2;
                break;
            }
        }

        // Project.getParsedFiles().add(df); IS NECESSARY HERE
        Project.getParsedFiles().add(df);
        Project.addOpenedFile(df);
        if (!Project.getOpenTextWindows().isEmpty() && (w != null || !(w = Project.getOpenTextWindows().iterator().next()).isSeperateWindow() || (w != null && WorkbenchManager.getUserSettingState().hasSingleTextWindow()))) {
            w.openNewDatFileTab(df, true);
        } else {
            w = EditorTextWindow.createNewWindowIfRequired(df);
        }

        final boolean doClose = w.saveAs(df, name, filePath);
        w.closeTabWithDatfile(df);

        if (doClose) {
            ok = true;
            getShell().close();
        }
    }

    @Override
    protected void handleShellCloseEvent() {
        c3d.getRenderer().disposeAllTextures();
        super.handleShellCloseEvent();
    }

    private void rebuildPrimitive(EventType et, Widget w) {

        if (doUpdate) {
            return;
        }

        boolean ccw = cmbWindingPtr[0].getSelectionIndex() == 0;
        int torusType = cmbTorusTypePtr[0].getSelectionIndex();

        doUpdate = true;

        if (cmbTorusTypePtr[0].getSelectionIndex() == 0 && spnMajorPtr[0].getValue() <= spnMinorPtr[0].getValue().intValue()) {
            cmbTorusTypePtr[0].select(1);
        }

        if (w != spnDivisionsPtr[0] && w != cmbDivisionsPtr[0] && spnSegmentsPtr[0].getValue() > spnDivisionsPtr[0].getValue()) {
            spnDivisionsPtr[0].setValue(spnSegmentsPtr[0].getValue());
        }

        if (w != spnSegmentsPtr[0] && w != cmbSegmentsPtr[0] && spnSegmentsPtr[0].getValue() > spnDivisionsPtr[0].getValue()) {
            spnSegmentsPtr[0].setValue(spnDivisionsPtr[0].getValue());
        }

        if (et == EventType.CBO) {

            switch (cmbDivisionsPtr[0].getSelectionIndex()) {
            case 0:
                spnDivisionsPtr[0].setValue(8);
                spnEdgesPerCrossSectionsPtr[0].setValue(8);
                break;
            case 1:
                spnDivisionsPtr[0].setValue(16);
                spnEdgesPerCrossSectionsPtr[0].setValue(16);
                break;
            case 2:
                spnDivisionsPtr[0].setValue(48);
                spnEdgesPerCrossSectionsPtr[0].setValue(48);
                break;
            default:
                break;
            }
            switch (cmbSegmentsPtr[0].getSelectionIndex()) {
            case 0:
                spnSegmentsPtr[0].setValue(spnDivisionsPtr[0].getValue() / 4);
                break;
            case 1:
                spnSegmentsPtr[0].setValue(spnDivisionsPtr[0].getValue() / 2);
                break;
            case 2:
                spnSegmentsPtr[0].setValue(spnDivisionsPtr[0].getValue() * 3 / 4);
                break;
            case 3:
                spnSegmentsPtr[0].setValue(spnDivisionsPtr[0].getValue());
                break;
            default:
                break;
            }

        } else {

            switch (spnDivisionsPtr[0].getValue()) {
            case 8:
                cmbDivisionsPtr[0].select(0);
                break;
            case 16:
                cmbDivisionsPtr[0].select(1);
                break;
            case 48:
                cmbDivisionsPtr[0].select(2);
                break;
            default:
                cmbDivisionsPtr[0].select(3);
                break;
            }
            final int QUARTER = spnDivisionsPtr[0].getValue() / 4;
            final int HALF = spnDivisionsPtr[0].getValue() / 2;
            final int THREE_OF_FOUR = spnDivisionsPtr[0].getValue() * 3 / 4;
            final int WHOLE = spnDivisionsPtr[0].getValue();

            if (spnSegmentsPtr[0].getValue() == QUARTER && spnDivisionsPtr[0].getValue() / 4d - QUARTER == 0d) {
                cmbSegmentsPtr[0].select(0);
            } else if (spnSegmentsPtr[0].getValue() == HALF && spnDivisionsPtr[0].getValue() / 2d - HALF == 0d) {
                cmbSegmentsPtr[0].select(1);
            } else if (spnSegmentsPtr[0].getValue() == THREE_OF_FOUR && spnDivisionsPtr[0].getValue() * 3d / 4d - QUARTER == 0d) {
                cmbSegmentsPtr[0].select(2);
            } else if (spnSegmentsPtr[0].getValue() == WHOLE) {
                cmbSegmentsPtr[0].select(3);
            } else {
                cmbSegmentsPtr[0].select(4);
            }

        }

        if (cmbTypePtr[0].getSelectionIndex() == TORUS) {
            spnSizePtr[0].setValue(new BigDecimal(decformat4f.format(spnMinorPtr[0].getValue().intValue() * 1d / spnMajorPtr[0].getValue())));
        }

        doUpdate = false;

        int divisions = spnDivisionsPtr[0].getValue();
        int segments = spnSegmentsPtr[0].getValue();
        int edgesPerCrossSections = spnEdgesPerCrossSectionsPtr[0].getValue();
        int major = spnMajorPtr[0].getValue();
        int minor = spnMinorPtr[0].getValue().intValue();
        double width = spnMinorPtr[0].getValue().doubleValue();
        double size = spnSizePtr[0].getValue().doubleValue();

        int gcd = gcd(divisions, segments);

        int upper = segments / gcd;
        int lower = divisions / gcd;

        if (lower == 2) {
            lower = lower * 2;
            upper = upper * 2;
        } else if (lower == 1) {
            lower = 4;
            upper = 4;
        }

        final String prefix;
        String resolution;
        final String type;

        resPrefix = ""; //$NON-NLS-1$

        if (divisions != 16) {
            if (divisions > 16) {
                resolution = "Hi-Res "; //$NON-NLS-1$
                if (divisions == 48) {
                    resPrefix = "48" + File.separator; //$NON-NLS-1$
                }
            } else {
                resolution = "Lo-Res "; //$NON-NLS-1$
                if (divisions == 8) {
                    resPrefix = "8" + File.separator; //$NON-NLS-1$
                }
            }

            prefix = divisions + "\\"; //$NON-NLS-1$
            type = "0 !LDRAW_ORG Unofficial_" + divisions + "_Primitive\n"; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            prefix = ""; //$NON-NLS-1$
            resolution = ""; //$NON-NLS-1$
            type = "0 !LDRAW_ORG Unofficial_Primitive\n"; //$NON-NLS-1$
        }

        final String suffixWidth;
        final String suffixWidthTitle;
        if (width != 1d) {
            suffixWidth = "w" + removeTrailingZeros(decformat4f.format(width)); //$NON-NLS-1$
            suffixWidthTitle = " Width " + removeTrailingZeros(decformat4f.format(width)); //$NON-NLS-1$
        } else {
            suffixWidth = ""; //$NON-NLS-1$
            suffixWidthTitle = ""; //$NON-NLS-1$
        }

        final StringBuilder sb = new StringBuilder();

        final UserSettingState user = WorkbenchManager.getUserSettingState();

        String ldrawName = user.getLdrawUserName();
        String realName = user.getRealUserName();
        if (ldrawName == null || ldrawName.isEmpty()) {
            if (realName == null || realName.isEmpty()) {
                sb.append("0 Author: Primitive Generator 2\n"); //$NON-NLS-1$
            } else {
                sb.append("0 Author: " + realName + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else {
            if (realName == null || realName.isEmpty()) {
                sb.append("0 Author: [" + ldrawName + "]\n"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                sb.append("0 Author: " + realName + " [" + ldrawName + "]\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }

        sb.append(type);
        sb.append("0 !LICENSE Licensed under CC BY 4.0 : see CAreadme.txt\n\n"); //$NON-NLS-1$

        if (ccw) {
            sb.append("0 BFC CERTIFY CCW\n\n"); //$NON-NLS-1$
        } else {
            sb.append("0 BFC CERTIFY CW\n\n"); //$NON-NLS-1$
        }

        final int pType = cmbTypePtr[0].getSelectionIndex();
        switch (pType) {
        case CIRCLE:
            name = upper + "-" + lower + "edge.dat"; //$NON-NLS-1$ //$NON-NLS-2$
            sb.insert(0, "0 Name: " + prefix + name + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.insert(0, "0 " + resolution + "Circle " + removeTrailingZeros2(decformat4f.format(segments * 1d / divisions)) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            {
                double deltaAngle = Math.PI * 2d / divisions;
                double angle = 0d;
                for(int i = 0; i < segments; i++) {
                    double nextAngle = angle + deltaAngle;
                    double x1 = Math.cos(angle);
                    double z1 = Math.sin(angle);
                    double x2 = Math.cos(nextAngle);
                    double z2 = Math.sin(nextAngle);
                    sb.append("2 24 "); //$NON-NLS-1$
                    sb.append(removeTrailingZeros(decformat4f.format(x1)));
                    sb.append(" 0 "); //$NON-NLS-1$
                    sb.append(removeTrailingZeros(decformat4f.format(z1)));
                    sb.append(" "); //$NON-NLS-1$
                    sb.append(removeTrailingZeros(decformat4f.format(x2)));
                    sb.append(" 0 "); //$NON-NLS-1$
                    sb.append(removeTrailingZeros(decformat4f.format(z2)));
                    sb.append("\n"); //$NON-NLS-1$
                    angle = nextAngle;
                }
            }

            break;
        case RING:
            name = upper + "-" + lower + "ring" + removeTrailingZeros(decformat4f.format(size)) + suffixWidth +  ".dat"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            sb.insert(0, "0 Name: " + prefix + name + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.insert(0, "0 " + resolution + "Ring " + addExtraSpaces1(removeTrailingZeros(decformat4f.format(size))) + " x " + removeTrailingZeros2(decformat4f.format(segments * 1d / divisions)) + suffixWidthTitle + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            sb.append(ring(divisions, segments, size, ccw, width));

            break;
        case CONE:
            name = upper + "-" + lower + "con" + removeTrailingZeros(decformat4f.format(size)) + suffixWidth +  ".dat"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            sb.insert(0, "0 Name: " + prefix + name + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.insert(0, "0 " + resolution + "Cone " + addExtraSpaces1(removeTrailingZeros(decformat4f.format(size))) + " x " + removeTrailingZeros2(decformat4f.format(segments * 1d / divisions)) + suffixWidthTitle + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            sb.append(cone(divisions, segments, size, ccw, width));

            break;
        case TORUS:

        {
            boolean mixed = edgesPerCrossSections != divisions;
            String sweep = decformat4f.format(minor * 1d / major);
            String sweep2 = sweep.replace(".", "").substring(sweep.charAt(0) == '0' ? 1 : 0, Math.min(sweep.charAt(0) == '0' ? 5 : 4, sweep.length())); //$NON-NLS-1$ //$NON-NLS-2$
            String frac = "99"; //$NON-NLS-1$
            // 01=1/1, 02=1/2, 04=1/4, 08=1/8, 16=1/16, 32=1/32, 48=1/48
            if (upper == 2 && lower == 4) {
                frac = "02"; //$NON-NLS-1$
            } else if (upper == 1 && lower < 10) {
                frac = "0" + lower; //$NON-NLS-1$
            } else if (upper == 1 && lower < 100) {
                frac = "" + lower; //$NON-NLS-1$
            } else if (upper == 4 && lower == 4) {
                frac = "01"; //$NON-NLS-1$
            }

            String tt = " Inside  1 x "; //$NON-NLS-1$
            String t = "i"; //$NON-NLS-1$
            String r = "t"; //$NON-NLS-1$
            if (minor >= major) {
                r = "r"; //$NON-NLS-1$
            }
            if (mixed) {
                r = r + "m"; //$NON-NLS-1$
                resolution = "Mixed-Mode "; //$NON-NLS-1$
            }
            if (torusType == 1) {
                tt = " Outside  1 x "; //$NON-NLS-1$
                t = "o"; //$NON-NLS-1$
            } else if (torusType == 2) {
                tt = " Tube  1 x "; //$NON-NLS-1$
                t = "q"; //$NON-NLS-1$
            }

            name = r + frac + t + sweep2 + ".dat"; //$NON-NLS-1$
            sb.insert(0, "0 Name: " + prefix + name + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.insert(0, "0 " + resolution + "Torus" + tt + sweep + " x " + removeTrailingZeros2(decformat4f.format(segments * 1d / divisions)) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            sb.append("0 // Major Radius: " + major + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("0 // Tube(Minor) Radius: " + minor + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("0 // Segments(Sweep): " + segments + "/" + divisions + " = " + removeTrailingZeros3(decformat4f.format(segments * 1d / divisions)) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            sb.append("0 // 1 9 0 0 0 1 0 0 0 1 0 0 0 1 4-4edge.dat\n"); //$NON-NLS-1$
            sb.append("0 // 1 12 1 0 0 " + sweep + " 0 0 0 0 " + sweep + " 0 1 0 4-4edge.dat\n\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            sb.append(torus(divisions, segments, edgesPerCrossSections, torusType, major, minor, ccw));
        }

        break;
        case CYLINDER:
            name = upper + "-" + lower + "cyli.dat"; //$NON-NLS-1$ //$NON-NLS-2$
            sb.insert(0, "0 Name: " + prefix + name + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.insert(0, "0 " + resolution + "Cylinder " + removeTrailingZeros2(decformat4f.format(segments * 1d / divisions)) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            sb.append(cylinder(divisions, segments, ccw));

            break;
        case DISC:
            name = upper + "-" + lower + "disc.dat"; //$NON-NLS-1$ //$NON-NLS-2$
            sb.insert(0, "0 Name: " + prefix + name + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.insert(0, "0 " + resolution + "Disc " + removeTrailingZeros2(decformat4f.format(segments * 1d / divisions)) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            {
                double deltaAngle = Math.PI * 2d / divisions;
                double angle = 0d;
                for(int i = 0; i < segments; i++) {
                    double nextAngle = angle + deltaAngle;
                    double x1 = Math.cos(angle);
                    double z1 = Math.sin(angle);
                    double x2 = Math.cos(nextAngle);
                    double z2 = Math.sin(nextAngle);
                    if (ccw) {
                        sb.append("3 16 0 0 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x1)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z1)));
                        sb.append(" "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x2)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z2)));
                    } else {
                        sb.append("3 16 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x2)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z2)));
                        sb.append(" "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x1)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z1)));
                        sb.append(" 0 0 0"); //$NON-NLS-1$
                    }

                    sb.append("\n"); //$NON-NLS-1$
                    angle = nextAngle;
                }
            }

            break;
        case DISC_NEGATIVE:
            name = upper + "-" + lower + "ndis.dat"; //$NON-NLS-1$ //$NON-NLS-2$
            sb.insert(0, "0 Name: " + prefix + name + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.insert(0, "0 " + resolution + "Disc Negative " + removeTrailingZeros2(decformat4f.format(segments * 1d / divisions)) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            {
                double deltaAngle = Math.PI * 2d / divisions;
                double angle = 0d;
                int s1 = divisions / 4;
                int s2 = s1 * 2;
                int s3 = s1 * 3;
                for(int i = 0; i < segments; i++) {
                    double nextAngle = angle + deltaAngle;
                    double x2 = Math.cos(angle);
                    double z2 = Math.sin(angle);
                    double x1 = Math.cos(nextAngle);
                    double z1 = Math.sin(nextAngle);
                    double x3;
                    double z3;
                    if (i < s1) {
                        x3 = 1d;
                        z3 = 1d;
                    } else if (i < s2) {
                        x3 = -1d;
                        z3 = 1d;
                    } else if (i < s3) {
                        x3 = -1d;
                        z3 = -1d;
                    } else {
                        x3 = 1d;
                        z3 = -1d;
                    }
                    if (ccw) {
                        sb.append("3 16 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x3)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z3)));
                        sb.append(" "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x1)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z1)));
                        sb.append(" "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x2)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z2)));
                    } else {
                        sb.append("3 16 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x2)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z2)));
                        sb.append(" "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x1)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z1)));
                        sb.append(" "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x3)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z3)));
                    }

                    sb.append("\n"); //$NON-NLS-1$
                    angle = nextAngle;
                }
            }

            break;
        case CHORD:
            name = upper + "-" + lower + "chrd.dat"; //$NON-NLS-1$ //$NON-NLS-2$
            sb.insert(0, "0 Name: " + prefix + name + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.insert(0, "0 " + resolution + "Chord " + removeTrailingZeros2(decformat4f.format(segments * 1d / divisions)) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            {
                double deltaAngle = Math.PI * 2d / divisions;
                double angle = deltaAngle;
                segments = segments - 1;
                for(int i = 0; i < segments; i++) {
                    double nextAngle = angle + deltaAngle;
                    double x1 = Math.cos(angle);
                    double z1 = Math.sin(angle);
                    double x2 = Math.cos(nextAngle);
                    double z2 = Math.sin(nextAngle);
                    if (ccw) {
                        sb.append("3 16 1 0 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x1)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z1)));
                        sb.append(" "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x2)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z2)));
                    } else {
                        sb.append("3 16 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x2)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z2)));
                        sb.append(" "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(x1)));
                        sb.append(" 0 "); //$NON-NLS-1$
                        sb.append(removeTrailingZeros(decformat4f.format(z1)));
                        sb.append(" 1 0 0"); //$NON-NLS-1$
                    }

                    sb.append("\n"); //$NON-NLS-1$
                    angle = nextAngle;
                }
            }

            break;
        default:
            break;
        }

        sb.append("0 // Build by LDPartEditor (PrimGen 2.X)"); //$NON-NLS-1$

        if (isOfficialRules(pType, size, divisions, segments, minor, ccw)) {
            lblStandardPtr[0].setText(I18n.PRIMGEN_STANDARD);
        } else {
            lblStandardPtr[0].setText(I18n.PRIMGEN_NON_STANDARD);
        }
        txtDataPtr[0].setText(sb.toString());
    }

    private Object cylinder(int divisions, int segments, boolean ccw) {

        // Crazy Reverse Engineering from Mike's PrimGen2
        // Thanks to Mr. Heidemann! :)

        // DONT TOUCH THIS CODE! It simply works...

        final StringBuilder sb2 = new StringBuilder();
        if (segments > divisions)
        {
            return sb2.toString();
        }
        if (segments > divisions)
        {
            return null;
        }

        int num3 = segments - 1;
        for (int num = 0; num <= num3; num++)
        {
            double objdatLinePoint1X = Math.cos((num + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0);
            double objdatLinePoint1Y = 0.0;
            double objdatLinePoint1Z = Math.sin((num + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0);
            double objdatLinePoint2X = Math.cos(num * (360.0 / divisions) * 3.1415926535897931 / 180.0);
            double objdatLinePoint2Y = 0.0;
            double objdatLinePoint2Z = Math.sin(num * (360.0 / divisions) * 3.1415926535897931 / 180.0);
            double objdatLinePoint3X = objdatLinePoint2X;
            double objdatLinePoint3Y = 1.0;
            double objdatLinePoint3Z = objdatLinePoint2Z;
            double objdatLinePoint4X = objdatLinePoint1X;
            double objdatLinePoint4Y = 1.0;
            double objdatLinePoint4Z = objdatLinePoint1Z;

            sb2.append("4 16 "); //$NON-NLS-1$
            if (ccw) {
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
            } else {
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
            }
            sb2.append("\n"); //$NON-NLS-1$
        }
        sb2.append("0 // conditional lines\n"); //$NON-NLS-1$
        for (int num = 0; num <= segments; num++)
        {
            if (num == divisions)
            {
                break;
            }
            double objdatLinePoint1X = Math.cos(num * (360.0 / divisions) * 3.1415926535897931 / 180.0);
            double objdatLinePoint1Y = 1.0;
            double objdatLinePoint1Z = Math.sin(num * (360.0 / divisions) * 3.1415926535897931 / 180.0);
            double objdatLinePoint2X = Math.cos(num * (360.0 / divisions) * 3.1415926535897931 / 180.0);
            double objdatLinePoint2Y = 0.0;
            double objdatLinePoint2Z = Math.sin(num * (360.0 / divisions) * 3.1415926535897931 / 180.0);
            double objdatLinePoint3X;
            double objdatLinePoint3Y;
            double objdatLinePoint3Z;
            double objdatLinePoint4X;
            double objdatLinePoint4Y;
            double objdatLinePoint4Z;
            if (divisions == segments || num != 0 && num != segments)
            {
                objdatLinePoint3X = Math.cos((num - 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0);
                objdatLinePoint3Y = 1.0;
                objdatLinePoint3Z = Math.sin((num - 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0);
                objdatLinePoint4X = Math.cos((num + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0);
                objdatLinePoint4Y = 1.0;
                objdatLinePoint4Z = Math.sin((num + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0);
            }
            else if (num == 0)
            {
                objdatLinePoint3X = Math.cos((double) num / (double) divisions * 2.0 * 3.1415926535897931);
                objdatLinePoint3Y = 1.0;
                objdatLinePoint3Z = Math.tan((double) (num - 1) / (double) divisions * 2.0 * 3.1415926535897931);
                objdatLinePoint4X = Math.cos((double) (num + 1) / (double) divisions * 2.0 * 3.1415926535897931);
                objdatLinePoint4Y = 1.0;
                objdatLinePoint4Z = Math.sin((double) (num + 1) / (double) divisions * 2.0 * 3.1415926535897931);
            }
            else
            {
                objdatLinePoint3X = Math.cos((double) (num - 1) / (double) divisions * 2.0 * 3.1415926535897931);
                objdatLinePoint3Y = 1.0;
                objdatLinePoint3Z = Math.sin((double) (num - 1) / (double) divisions * 2.0 * 3.1415926535897931);
                objdatLinePoint4X = Math.cos((double) (num + 1) / (double) divisions * 2.0 * 3.1415926535897931) / Math.cos(6.2831853071795862 / divisions);
                objdatLinePoint4Y = 1.0;
                objdatLinePoint4Z = Math.sin((double) (num + 1) / (double) divisions * 2.0 * 3.1415926535897931) / Math.cos(6.2831853071795862 / divisions);
            }
            sb2.append("5 24 "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
            sb2.append("\n"); //$NON-NLS-1$
        }
        return sb2.toString();
    }

    private Object cone(int divisions, int segments, double innerDiameter, boolean ccw, double width) {

        // Crazy Reverse Engineering from Mike's PrimGen2
        // Thanks to Mr. Heidemann! :)

        // DONT TOUCH THIS CODE! It simply works...

        final StringBuilder sb2 = new StringBuilder();
        if (segments > divisions)
        {
            return sb2.toString();
        }
        if (segments > divisions)
        {
            return null;
        }
        int num3 = segments - 1;
        for (int num = 0; num <= num3; num++)
        {
            double objdatLinePoint1X = round4f(Math.cos(num * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
            double objdatLinePoint1Y = 1.0;
            double objdatLinePoint1Z = round4f(Math.sin(num * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
            double objdatLinePoint2X = round4f(Math.cos((num + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
            double objdatLinePoint2Y = 1.0;
            double objdatLinePoint2Z = round4f(Math.sin((num + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
            double objdatLinePoint3X = round4f(Math.cos((num + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (width + innerDiameter);
            double objdatLinePoint3Y = 0.0;
            double objdatLinePoint3Z = round4f(Math.sin((num + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (width + innerDiameter);
            double objdatLinePoint4X = round4f(Math.cos(num * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (width + innerDiameter);
            double objdatLinePoint4Y = 0.0;
            double objdatLinePoint4Z = round4f(Math.sin(num * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (width + innerDiameter);
            sb2.append("4 16 "); //$NON-NLS-1$
            if (ccw) {
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
            } else {
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
            }
            sb2.append("\n"); //$NON-NLS-1$
        }
        sb2.append("0 // conditional lines\n"); //$NON-NLS-1$
        int num4 = segments;
        for (int num = 0; num <= num4; num++)
        {
            if (num == divisions)
            {
                break;
            }
            double objdatLinePoint1X = round4f(Math.cos(num * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
            double objdatLinePoint1Y = 1.0;
            double objdatLinePoint1Z = round4f(Math.sin(num * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
            double objdatLinePoint2X = round4f(Math.cos(num * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (width + innerDiameter);
            double objdatLinePoint2Y = 0.0;
            double objdatLinePoint2Z = round4f(Math.sin(num * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (width + innerDiameter);
            double objdatLinePoint3X;
            double objdatLinePoint3Y;
            double objdatLinePoint3Z;
            double objdatLinePoint4X;
            double objdatLinePoint4Y;
            double objdatLinePoint4Z;
            if (divisions == segments || num != 0 && num != segments)
            {
                objdatLinePoint3X = round4f(Math.cos((num - 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
                objdatLinePoint3Y = 1.0;
                objdatLinePoint3Z = round4f(Math.sin((num - 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
                objdatLinePoint4X = round4f(Math.cos((num + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
                objdatLinePoint4Y = 1.0;
                objdatLinePoint4Z = round4f(Math.sin((num + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
            }
            else if (num == 0)
            {
                objdatLinePoint3X = round4f(Math.cos((double) num / (double) divisions * 2.0 * 3.1415926535897931)) * innerDiameter;
                objdatLinePoint3Y = 1.0;
                objdatLinePoint3Z = round4f(Math.tan((double) (num - 1) / (double) divisions * 2.0 * 3.1415926535897931)) * innerDiameter;
                objdatLinePoint4X = round4f(Math.cos((double) (num + 1) / (double) divisions * 2.0 * 3.1415926535897931)) * innerDiameter;
                objdatLinePoint4Y = 1.0;
                objdatLinePoint4Z = round4f(Math.sin((double) (num + 1) / (double) divisions * 2.0 * 3.1415926535897931)) * innerDiameter;
            }
            else
            {
                objdatLinePoint3X = round4f(Math.cos((double) (num - 1) / (double) divisions * 2.0 * 3.1415926535897931)) * innerDiameter;
                objdatLinePoint3Y = 1.0;
                objdatLinePoint3Z = round4f(Math.sin((double) (num - 1) / (double) divisions * 2.0 * 3.1415926535897931)) * innerDiameter;
                objdatLinePoint4X = round4f(Math.cos((double) (num + 1) / (double) divisions * 2.0 * 3.1415926535897931) / Math.cos(6.2831853071795862 / divisions)) * innerDiameter;
                objdatLinePoint4Y = 1.0;
                objdatLinePoint4Z = round4f(Math.sin((double) (num + 1) / (double) divisions * 2.0 * 3.1415926535897931) / Math.cos(6.2831853071795862 / divisions)) * innerDiameter;
            }

            sb2.append("5 24 "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
            sb2.append("\n"); //$NON-NLS-1$
        }
        return sb2.toString();
    }

    private String ring(int divisions, int segments, double innerDiameter, boolean ccw, double width) {

        // Crazy Reverse Engineering from Mike's PrimGen2
        // Thanks to Mr. Heidemann! :)

        // DONT TOUCH THIS CODE! It simply works...

        final StringBuilder sb2 = new StringBuilder();
        if (segments > divisions)
        {
            return sb2.toString();
        }
        int num3 = segments - 1;
        for (int i = 0; i <= num3; i++)
        {
            double objdatLinePoint1X = round4f(Math.cos(i * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
            double objdatLinePoint1Y = 0.0;
            double objdatLinePoint1Z = round4f(Math.sin(i * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
            double objdatLinePoint2X = round4f(Math.cos((i + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
            double objdatLinePoint2Y = 0.0;
            double objdatLinePoint2Z = round4f(Math.sin((i + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * innerDiameter;
            double objdatLinePoint3X = round4f(Math.cos((i + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (width + innerDiameter);
            double objdatLinePoint3Y = 0.0;
            double objdatLinePoint3Z = round4f(Math.sin((i + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (width + innerDiameter);
            double objdatLinePoint4X = round4f(Math.cos(i * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (width + innerDiameter);
            double objdatLinePoint4Y = 0.0;
            double objdatLinePoint4Z = round4f(Math.sin(i * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (width + innerDiameter);

            sb2.append("4 16 "); //$NON-NLS-1$
            if (!ccw) {
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
            } else {
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
            }
            sb2.append("\n"); //$NON-NLS-1$
        }
        return sb2.toString();
    }

    private String torus(int divisions, int segments, int edgesPerCrossSections, int type, int major, int minor, boolean ccw) {

        // Crazy Reverse Engineering from Mike's PrimGen2
        // Thanks to Mr. Heidemann! :)

        // DONT TOUCH THIS CODE! It simply works...

        final List<Double[]> points = new ArrayList<>();
        final StringBuilder sb2 = new StringBuilder();
        final int INNER = 0;
        final int OUTER = 1;
        final int TUBE = 2;
        int num;
        int num2;
        int num3;
        int num5;
        if (segments > divisions)
        {
            return sb2.toString();
        }
        switch (type)
        {
        case INNER:
            num5 = (int) Math.round(edgesPerCrossSections / 4.0 * 3.0);
            num = edgesPerCrossSections;
            break;
        case OUTER:
            num5 = 0;
            num = (int) Math.round(edgesPerCrossSections / 4.0);
            break;
        default: // case TUBE:
            num5 = 0;
            num = edgesPerCrossSections;
            break;
        }
        int num8 = segments - 1;
        double objdatLinePoint1X;
        double objdatLinePoint1Y;
        double objdatLinePoint1Z;
        double objdatLinePoint2X;
        double objdatLinePoint2Y;
        double objdatLinePoint2Z;
        double objdatLinePoint3X;
        double objdatLinePoint3Y;
        double objdatLinePoint3Z;
        double objdatLinePoint4X;
        double objdatLinePoint4Y;
        double objdatLinePoint4Z;
        
        double ratio = 1d;
        if (edgesPerCrossSections != divisions) {
            ratio = divisions / (double) edgesPerCrossSections;
        }
        
        for (num2 = 0; num2 <= num8; num2++)
        {
            int num9 = num - 1;
            num3 = num5;
            
            if (type != TUBE) {
                num3 -= 1;
                // Add one virtual quad to the segment start
                objdatLinePoint1X = round4f(Math.cos(num2 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * num3 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint1Y = round4f(Math.cos(num3 * (360.0 / edgesPerCrossSections) * 3.1415926535897931 / 180.0)) * minor * 1.0 / major;
                objdatLinePoint1Z = round4f(Math.sin(num2 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * num3 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint2X = round4f(Math.cos((num2 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * num3 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint2Y = round4f(Math.cos(num3 * (360.0 / edgesPerCrossSections) * 3.1415926535897931 / 180.0)) * minor * 1.0 / major;
                objdatLinePoint2Z = round4f(Math.sin((num2 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * num3 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint3X = round4f(Math.cos((num2 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * (num3 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint3Y = round4f(Math.cos((num3 + 1) * (360.0 / edgesPerCrossSections) * 3.1415926535897931 / 180.0)) * minor * 1.0 / major;
                objdatLinePoint3Z = round4f(Math.sin((num2 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * (num3 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint4X = round4f(Math.cos(num2 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * (num3 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint4Y = round4f(Math.cos((num3 + 1) * (360.0 / edgesPerCrossSections) * 3.1415926535897931 / 180.0)) * minor * 1.0 / major;
                objdatLinePoint4Z = round4f(Math.sin(num2 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * (num3 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;

                points.add(new Double[] {objdatLinePoint1X, objdatLinePoint1Y, objdatLinePoint1Z});
                points.add(new Double[] {objdatLinePoint2X, objdatLinePoint2Y, objdatLinePoint2Z});
                points.add(new Double[] {objdatLinePoint3X, objdatLinePoint3Y, objdatLinePoint3Z});
                points.add(new Double[] {objdatLinePoint4X, objdatLinePoint4Y, objdatLinePoint4Z});
                num3 += 1;
            }
            
            while (num3 <= num9)
            {
                objdatLinePoint1X = round4f(Math.cos(num2 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * num3 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint1Y = round4f(Math.cos(num3 * (360.0 / edgesPerCrossSections) * 3.1415926535897931 / 180.0)) * minor * 1.0 / major;
                objdatLinePoint1Z = round4f(Math.sin(num2 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * num3 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint2X = round4f(Math.cos((num2 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * num3 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint2Y = round4f(Math.cos(num3 * (360.0 / edgesPerCrossSections) * 3.1415926535897931 / 180.0)) * minor * 1.0 / major;
                objdatLinePoint2Z = round4f(Math.sin((num2 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * num3 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint3X = round4f(Math.cos((num2 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * (num3 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint3Y = round4f(Math.cos((num3 + 1) * (360.0 / edgesPerCrossSections) * 3.1415926535897931 / 180.0)) * minor * 1.0 / major;
                objdatLinePoint3Z = round4f(Math.sin((num2 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * (num3 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint4X = round4f(Math.cos(num2 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * (num3 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint4Y = round4f(Math.cos((num3 + 1) * (360.0 / edgesPerCrossSections) * 3.1415926535897931 / 180.0)) * minor * 1.0 / major;
                objdatLinePoint4Z = round4f(Math.sin(num2 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * (num3 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;

                points.add(new Double[] {objdatLinePoint1X, objdatLinePoint1Y, objdatLinePoint1Z});
                points.add(new Double[] {objdatLinePoint2X, objdatLinePoint2Y, objdatLinePoint2Z});
                points.add(new Double[] {objdatLinePoint3X, objdatLinePoint3Y, objdatLinePoint3Z});
                points.add(new Double[] {objdatLinePoint4X, objdatLinePoint4Y, objdatLinePoint4Z});
                
                sb2.append("4 16 "); //$NON-NLS-1$
                if (ccw) {
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                } else {
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                }
                sb2.append("\n"); //$NON-NLS-1$

                num3++;
            }
            
            if (type != TUBE) {
                // Add one virtual quad to the segment end
                objdatLinePoint1X = round4f(Math.cos(num2 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * num3 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint1Y = round4f(Math.cos(num3 * (360.0 / edgesPerCrossSections) * 3.1415926535897931 / 180.0)) * minor * 1.0 / major;
                objdatLinePoint1Z = round4f(Math.sin(num2 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * num3 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint2X = round4f(Math.cos((num2 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * num3 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint2Y = round4f(Math.cos(num3 * (360.0 / edgesPerCrossSections) * 3.1415926535897931 / 180.0)) * minor * 1.0 / major;
                objdatLinePoint2Z = round4f(Math.sin((num2 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * num3 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint3X = round4f(Math.cos((num2 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * (num3 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint3Y = round4f(Math.cos((num3 + 1) * (360.0 / edgesPerCrossSections) * 3.1415926535897931 / 180.0)) * minor * 1.0 / major;
                objdatLinePoint3Z = round4f(Math.sin((num2 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * (num3 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint4X = round4f(Math.cos(num2 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * (num3 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;
                objdatLinePoint4Y = round4f(Math.cos((num3 + 1) * (360.0 / edgesPerCrossSections) * 3.1415926535897931 / 180.0)) * minor * 1.0 / major;
                objdatLinePoint4Z = round4f(Math.sin(num2 * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * (major + round4f(Math.sin(ratio * (num3 + 1) * (360.0 / divisions) * 3.1415926535897931 / 180.0)) * minor) * 1.0 / major;

                points.add(new Double[] {objdatLinePoint1X, objdatLinePoint1Y, objdatLinePoint1Z});
                points.add(new Double[] {objdatLinePoint2X, objdatLinePoint2Y, objdatLinePoint2Z});
                points.add(new Double[] {objdatLinePoint3X, objdatLinePoint3Y, objdatLinePoint3Z});
                points.add(new Double[] {objdatLinePoint4X, objdatLinePoint4Y, objdatLinePoint4Z});
            }
        }
        
        sb2.append("0 // conditional lines\n"); //$NON-NLS-1$
        if (type == OUTER) {
            addCondlinesForTorusOutside(points, num, num5, num8, sb2, segments == divisions);
        } else if (type == INNER) {
            addCondlinesForTorusInside(points, num, num5, num8, sb2, segments == divisions);
        } else {
            addCondlinesForTorusTube(points, num, num5, num8, sb2, segments == divisions);
        }
        
        return sb2.toString();
    }

    private void addCondlinesForTorusInside(List<Double[]> points, int num, int num5, int num8, StringBuilder sb2, boolean closed) {
        int pointIndex = 0;
        for (int num2 = 0; num2 <= num8; num2++)
        {
            double objdatLinePoint1X = points.get(pointIndex + 2)[0];
            double objdatLinePoint1Y = points.get(pointIndex + 2)[1];
            double objdatLinePoint1Z = points.get(pointIndex + 2)[2];
            double objdatLinePoint2X = points.get(pointIndex + 3)[0];
            double objdatLinePoint2Y = points.get(pointIndex + 3)[1];
            double objdatLinePoint2Z = points.get(pointIndex + 3)[2];
            double objdatLinePoint3X = points.get(pointIndex + 2)[0];
            double objdatLinePoint3Y = points.get(pointIndex + 1)[1];
            double objdatLinePoint3Z = points.get(pointIndex + 2)[2];
            double objdatLinePoint4X = points.get(pointIndex + 6)[0];
            double objdatLinePoint4Y = points.get(pointIndex + 6)[1];
            double objdatLinePoint4Z = points.get(pointIndex + 6)[2];
            pointIndex += 4;
            
            sb2.append("5 24 "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
            sb2.append("\n"); //$NON-NLS-1$
            
            int num9 = num - 1;
            int num3 = num5;
            while (num3 <= num9)
            {
                if (num3 != num9) {
                    objdatLinePoint1X = points.get(pointIndex + 2)[0];
                    objdatLinePoint1Y = points.get(pointIndex + 2)[1];
                    objdatLinePoint1Z = points.get(pointIndex + 2)[2];
                    objdatLinePoint2X = points.get(pointIndex + 3)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 3)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 3)[2];
                    objdatLinePoint3X = points.get(pointIndex + 1)[0];
                    objdatLinePoint3Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint3Z = points.get(pointIndex + 1)[2];
                    objdatLinePoint4X = points.get(pointIndex + 6)[0];
                    objdatLinePoint4Y = points.get(pointIndex + 6)[1];
                    objdatLinePoint4Z = points.get(pointIndex + 6)[2];
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                
                if (num2 == 0 && !closed) {
                    objdatLinePoint1X = points.get(pointIndex)[0];
                    objdatLinePoint1Y = points.get(pointIndex)[1];
                    objdatLinePoint1Z = points.get(pointIndex)[2];
                    objdatLinePoint2X = points.get(pointIndex + 3)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 3)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 3)[2];
                    objdatLinePoint3X = points.get(pointIndex)[0];
                    objdatLinePoint3Y = points.get(pointIndex)[1];
                    objdatLinePoint3Z = -points.get(pointIndex + 1)[2];
                    objdatLinePoint4X = points.get(pointIndex + 1)[0];
                    objdatLinePoint4Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint4Z = points.get(pointIndex + 1)[2];
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                if (num2 < num8) {
                    objdatLinePoint1X = points.get(pointIndex + 1)[0];
                    objdatLinePoint1Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint1Z = points.get(pointIndex + 1)[2];
                    objdatLinePoint2X = points.get(pointIndex + 2)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 2)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 2)[2];
                    objdatLinePoint3X = points.get(pointIndex)[0];
                    objdatLinePoint3Y = points.get(pointIndex)[1];
                    objdatLinePoint3Z = points.get(pointIndex)[2];
                    objdatLinePoint4X = points.get(pointIndex + (num9 - num5 + 2) * 4 + 2)[0];
                    objdatLinePoint4Y = points.get(pointIndex + (num9 - num5 + 2) * 4 + 2)[1];
                    objdatLinePoint4Z = points.get(pointIndex + (num9 - num5 + 2) * 4 + 2)[2];
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                if (num2 == num8 && !closed) {
                    objdatLinePoint1X = points.get(pointIndex + 1)[0];
                    objdatLinePoint1Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint1Z = points.get(pointIndex + 1)[2];
                    objdatLinePoint2X = points.get(pointIndex + 2)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 2)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 2)[2];
                    objdatLinePoint3X = points.get(pointIndex)[0];
                    objdatLinePoint3Y = points.get(pointIndex)[1];
                    objdatLinePoint3Z = points.get(pointIndex)[2];
                    Vector3d p1 = new Vector3d(BigDecimal.valueOf(objdatLinePoint1X), BigDecimal.valueOf(objdatLinePoint1Y), BigDecimal.valueOf(objdatLinePoint1Z));
                    Vector3d yAxis = new Vector3d(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO);
                    Vector3d crossP = Vector3d.cross(p1, yAxis);
                    Vector3d p4 = Vector3d.add(p1, crossP);
                    objdatLinePoint4X = p4.x.doubleValue();
                    objdatLinePoint4Y = p4.y.doubleValue();
                    objdatLinePoint4Z = p4.z.doubleValue();
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                if (num2 == num8 && closed) {
                    objdatLinePoint1X = points.get(pointIndex + 1)[0];
                    objdatLinePoint1Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint1Z = points.get(pointIndex + 1)[2];
                    objdatLinePoint2X = points.get(pointIndex + 2)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 2)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 2)[2];
                    objdatLinePoint3X = points.get(pointIndex)[0];
                    objdatLinePoint3Y = points.get(pointIndex)[1];
                    objdatLinePoint3Z = points.get(pointIndex)[2];
                    objdatLinePoint4X = points.get((num3 - num5) * 4 + 2)[0];
                    objdatLinePoint4Y = points.get((num3 - num5) * 4 + 2)[1];
                    objdatLinePoint4Z = points.get((num3 - num5) * 4 + 2)[2];
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                num3++;
                pointIndex += 4;
            }
            
            objdatLinePoint1X = points.get(pointIndex + 1)[0];
            objdatLinePoint1Y = points.get(pointIndex + 1)[1];
            objdatLinePoint1Z = points.get(pointIndex + 1)[2];
            objdatLinePoint2X = points.get(pointIndex)[0];
            objdatLinePoint2Y = points.get(pointIndex)[1];
            objdatLinePoint2Z = points.get(pointIndex)[2];
            objdatLinePoint3X = points.get(pointIndex - 3)[0];
            objdatLinePoint3Y = points.get(pointIndex - 3)[1];
            objdatLinePoint3Z = points.get(pointIndex - 3)[2];
            objdatLinePoint4X = points.get(pointIndex + 2)[0];
            objdatLinePoint4Y = points.get(pointIndex + 1)[1];
            objdatLinePoint4Z = points.get(pointIndex + 2)[2];
            
            sb2.append("5 24 "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
            sb2.append("\n"); //$NON-NLS-1$
            pointIndex += 4;
        }
    }

    private void addCondlinesForTorusTube(List<Double[]> points, int num, int num5, int num8, StringBuilder sb2, boolean closed) {
        int pointIndex = 0;
        for (int num2 = 0; num2 <= num8; num2++)
        {
            double objdatLinePoint1X;
            double objdatLinePoint1Y;
            double objdatLinePoint1Z;
            double objdatLinePoint2X;
            double objdatLinePoint2Y;
            double objdatLinePoint2Z;
            double objdatLinePoint3X;
            double objdatLinePoint3Y;
            double objdatLinePoint3Z;
            double objdatLinePoint4X;
            double objdatLinePoint4Y;
            double objdatLinePoint4Z;
            
            int num9 = num - 1;
            int num3 = num5;
            int ringSize = (num9 - num5 + 1) * 4;
            int ringStart = pointIndex;
            while (num3 <= num9)
            {
                objdatLinePoint1X = points.get(pointIndex + 2)[0];
                objdatLinePoint1Y = points.get(pointIndex + 2)[1];
                objdatLinePoint1Z = points.get(pointIndex + 2)[2];
                objdatLinePoint2X = points.get(pointIndex + 3)[0];
                objdatLinePoint2Y = points.get(pointIndex + 3)[1];
                objdatLinePoint2Z = points.get(pointIndex + 3)[2];
                objdatLinePoint3X = points.get(pointIndex + 1)[0];
                objdatLinePoint3Y = points.get(pointIndex + 1)[1];
                objdatLinePoint3Z = points.get(pointIndex + 1)[2];
                objdatLinePoint4X = points.get(ringStart + (pointIndex + 6) % ringSize)[0];
                objdatLinePoint4Y = points.get(ringStart + (pointIndex + 6) % ringSize)[1];
                objdatLinePoint4Z = points.get(ringStart + (pointIndex + 6) % ringSize)[2];
                
                sb2.append("5 24 "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                sb2.append(" "); //$NON-NLS-1$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                sb2.append("\n"); //$NON-NLS-1$
                
                if (num2 == 0 && !closed) {
                    objdatLinePoint1X = points.get(pointIndex)[0];
                    objdatLinePoint1Y = points.get(pointIndex)[1];
                    objdatLinePoint1Z = points.get(pointIndex)[2];
                    objdatLinePoint2X = points.get(pointIndex + 3)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 3)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 3)[2];
                    objdatLinePoint3X = points.get(pointIndex)[0];
                    objdatLinePoint3Y = points.get(pointIndex)[1];
                    objdatLinePoint3Z = -points.get(pointIndex + 1)[2];
                    objdatLinePoint4X = points.get(pointIndex + 1)[0];
                    objdatLinePoint4Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint4Z = points.get(pointIndex + 1)[2];
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                if (num2 < num8) {
                    objdatLinePoint1X = points.get(pointIndex + 1)[0];
                    objdatLinePoint1Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint1Z = points.get(pointIndex + 1)[2];
                    objdatLinePoint2X = points.get(pointIndex + 2)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 2)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 2)[2];
                    objdatLinePoint3X = points.get(pointIndex)[0];
                    objdatLinePoint3Y = points.get(pointIndex)[1];
                    objdatLinePoint3Z = points.get(pointIndex)[2];
                    objdatLinePoint4X = points.get(ringStart + ringSize + (pointIndex + 1) % ringSize)[0];
                    objdatLinePoint4Y = points.get(ringStart + ringSize + (pointIndex + 1) % ringSize)[1];
                    objdatLinePoint4Z = points.get(ringStart + ringSize + (pointIndex + 1) % ringSize)[2];
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                if (num2 == num8 && !closed) {
                    objdatLinePoint1X = points.get(pointIndex + 1)[0];
                    objdatLinePoint1Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint1Z = points.get(pointIndex + 1)[2];
                    objdatLinePoint2X = points.get(pointIndex + 2)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 2)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 2)[2];
                    objdatLinePoint3X = points.get(pointIndex)[0];
                    objdatLinePoint3Y = points.get(pointIndex)[1];
                    objdatLinePoint3Z = points.get(pointIndex)[2];
                    Vector3d p1 = new Vector3d(BigDecimal.valueOf(objdatLinePoint1X), BigDecimal.valueOf(objdatLinePoint1Y), BigDecimal.valueOf(objdatLinePoint1Z));
                    Vector3d yAxis = new Vector3d(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO);
                    Vector3d crossP = Vector3d.cross(p1, yAxis);
                    Vector3d p4 = Vector3d.add(p1, crossP);
                    objdatLinePoint4X = p4.x.doubleValue();
                    objdatLinePoint4Y = p4.y.doubleValue();
                    objdatLinePoint4Z = p4.z.doubleValue();
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                if (num2 == num8 && closed) {
                    objdatLinePoint1X = points.get(pointIndex + 1)[0];
                    objdatLinePoint1Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint1Z = points.get(pointIndex + 1)[2];
                    objdatLinePoint2X = points.get(pointIndex + 2)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 2)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 2)[2];
                    objdatLinePoint3X = points.get(pointIndex)[0];
                    objdatLinePoint3Y = points.get(pointIndex)[1];
                    objdatLinePoint3Z = points.get(pointIndex)[2];
                    objdatLinePoint4X = points.get((num3 - num5) * 4 + 1)[0];
                    objdatLinePoint4Y = points.get((num3 - num5) * 4 + 1)[1];
                    objdatLinePoint4Z = points.get((num3 - num5) * 4 + 1)[2];
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                num3++;
                pointIndex += 4;
            }
        }
    }

    private void addCondlinesForTorusOutside(List<Double[]> points, int num, int num5, int num8, StringBuilder sb2, boolean closed) {
        int pointIndex = 0;
        for (int num2 = 0; num2 <= num8; num2++)
        {
            double objdatLinePoint1X = points.get(pointIndex + 2)[0];
            double objdatLinePoint1Y = points.get(pointIndex + 2)[1];
            double objdatLinePoint1Z = points.get(pointIndex + 2)[2];
            double objdatLinePoint2X = points.get(pointIndex + 3)[0];
            double objdatLinePoint2Y = points.get(pointIndex + 3)[1];
            double objdatLinePoint2Z = points.get(pointIndex + 3)[2];
            double objdatLinePoint3X = points.get(pointIndex + 6)[0];
            double objdatLinePoint3Y = points.get(pointIndex + 6)[1];
            double objdatLinePoint3Z = points.get(pointIndex + 6)[2];
            double objdatLinePoint4X = points.get(pointIndex + 1)[0];
            double objdatLinePoint4Y = points.get(pointIndex + 3)[1];
            double objdatLinePoint4Z = points.get(pointIndex + 1)[2];
            pointIndex += 4;
            
            sb2.append("5 24 "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
            sb2.append("\n"); //$NON-NLS-1$
            
            int num9 = num - 1;
            int num3 = num5;
            while (num3 <= num9)
            {
                if (num3 != num9) {
                    objdatLinePoint1X = points.get(pointIndex + 2)[0];
                    objdatLinePoint1Y = points.get(pointIndex + 2)[1];
                    objdatLinePoint1Z = points.get(pointIndex + 2)[2];
                    objdatLinePoint2X = points.get(pointIndex + 3)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 3)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 3)[2];
                    objdatLinePoint3X = points.get(pointIndex + 1)[0];
                    objdatLinePoint3Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint3Z = points.get(pointIndex + 1)[2];
                    objdatLinePoint4X = points.get(pointIndex + 6)[0];
                    objdatLinePoint4Y = points.get(pointIndex + 6)[1];
                    objdatLinePoint4Z = points.get(pointIndex + 6)[2];
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                
                if (num2 == 0 && !closed) {
                    objdatLinePoint1X = points.get(pointIndex)[0];
                    objdatLinePoint1Y = points.get(pointIndex)[1];
                    objdatLinePoint1Z = points.get(pointIndex)[2];
                    objdatLinePoint2X = points.get(pointIndex + 3)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 3)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 3)[2];
                    objdatLinePoint3X = points.get(pointIndex)[0];
                    objdatLinePoint3Y = points.get(pointIndex)[1];
                    objdatLinePoint3Z = -points.get(pointIndex + 1)[2];
                    objdatLinePoint4X = points.get(pointIndex + 1)[0];
                    objdatLinePoint4Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint4Z = points.get(pointIndex + 1)[2];
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                if (num2 < num8) {
                    objdatLinePoint1X = points.get(pointIndex + 1)[0];
                    objdatLinePoint1Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint1Z = points.get(pointIndex + 1)[2];
                    objdatLinePoint2X = points.get(pointIndex + 2)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 2)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 2)[2];
                    objdatLinePoint3X = points.get(pointIndex)[0];
                    objdatLinePoint3Y = points.get(pointIndex)[1];
                    objdatLinePoint3Z = points.get(pointIndex)[2];
                    objdatLinePoint4X = points.get(pointIndex + (num9 - num5 + 2) * 4 + 2)[0];
                    objdatLinePoint4Y = points.get(pointIndex + (num9 - num5 + 2) * 4 + 2)[1];
                    objdatLinePoint4Z = points.get(pointIndex + (num9 - num5 + 2) * 4 + 2)[2];
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                if (num2 == num8 && !closed) {
                    objdatLinePoint1X = points.get(pointIndex + 1)[0];
                    objdatLinePoint1Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint1Z = points.get(pointIndex + 1)[2];
                    objdatLinePoint2X = points.get(pointIndex + 2)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 2)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 2)[2];
                    objdatLinePoint3X = points.get(pointIndex)[0];
                    objdatLinePoint3Y = points.get(pointIndex)[1];
                    objdatLinePoint3Z = points.get(pointIndex)[2];
                    Vector3d p1 = new Vector3d(BigDecimal.valueOf(objdatLinePoint1X), BigDecimal.valueOf(objdatLinePoint1Y), BigDecimal.valueOf(objdatLinePoint1Z));
                    Vector3d yAxis = new Vector3d(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO);
                    Vector3d crossP = Vector3d.cross(p1, yAxis);
                    Vector3d p4 = Vector3d.add(p1, crossP);
                    objdatLinePoint4X = p4.x.doubleValue();
                    objdatLinePoint4Y = p4.y.doubleValue();
                    objdatLinePoint4Z = p4.z.doubleValue();
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                if (num2 == num8 && closed) {
                    objdatLinePoint1X = points.get(pointIndex + 1)[0];
                    objdatLinePoint1Y = points.get(pointIndex + 1)[1];
                    objdatLinePoint1Z = points.get(pointIndex + 1)[2];
                    objdatLinePoint2X = points.get(pointIndex + 2)[0];
                    objdatLinePoint2Y = points.get(pointIndex + 2)[1];
                    objdatLinePoint2Z = points.get(pointIndex + 2)[2];
                    objdatLinePoint3X = points.get(pointIndex)[0];
                    objdatLinePoint3Y = points.get(pointIndex)[1];
                    objdatLinePoint3Z = points.get(pointIndex)[2];
                    objdatLinePoint4X = points.get((num3 - num5) * 4 + 2)[0];
                    objdatLinePoint4Y = points.get((num3 - num5) * 4 + 2)[1];
                    objdatLinePoint4Z = points.get((num3 - num5) * 4 + 2)[2];
                    
                    sb2.append("5 24 "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
                    sb2.append(" "); //$NON-NLS-1$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
                    sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
                    sb2.append("\n"); //$NON-NLS-1$
                }
                
                num3++;
                pointIndex += 4;
            }
            
            objdatLinePoint1X = points.get(pointIndex + 1)[0];
            objdatLinePoint1Y = points.get(pointIndex + 1)[1];
            objdatLinePoint1Z = points.get(pointIndex + 1)[2];
            objdatLinePoint2X = points.get(pointIndex)[0];
            objdatLinePoint2Y = points.get(pointIndex)[1];
            objdatLinePoint2Z = points.get(pointIndex)[2];
            objdatLinePoint3X = points.get(pointIndex - 3)[0];
            objdatLinePoint3Y = points.get(pointIndex - 3)[1];
            objdatLinePoint3Z = points.get(pointIndex - 3)[2];
            objdatLinePoint4X = points.get(pointIndex + 1)[0];
            objdatLinePoint4Y = points.get(pointIndex + 2)[1];
            objdatLinePoint4Z = points.get(pointIndex + 1)[2];
            
            sb2.append("5 24 "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint1Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint1Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint2Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint2Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint3Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint3Z)));
            sb2.append(" "); //$NON-NLS-1$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4X)));
            sb2.append(" " + removeTrailingZeros(formatDec(objdatLinePoint4Y)) + " "); //$NON-NLS-1$ //$NON-NLS-2$
            sb2.append(removeTrailingZeros(formatDec(objdatLinePoint4Z)));
            sb2.append("\n"); //$NON-NLS-1$
            pointIndex += 4;
        }
    }

    @SuppressWarnings("java:S2111")
    private double round4f(double d) {
        return new BigDecimal(d).setScale(4, RoundingMode.HALF_EVEN).doubleValue();
    }

    private String formatDec(double d) {
        return bigDecimalToString(new BigDecimal(String.valueOf(d)).setScale(8, RoundingMode.HALF_UP).setScale(4, RoundingMode.HALF_UP));
    }

    private String bigDecimalToString(BigDecimal bd) {
        String result;
        if (bd.compareTo(BigDecimal.ZERO) == 0)
            return "0"; //$NON-NLS-1$
        BigDecimal bd2 = bd.stripTrailingZeros();
        result = bd2.toPlainString();
        return result;
    }

    private int gcd(int a, int b) {
        while (b > 0)
        {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private String removeTrailingZeros(String str) {
        StringBuilder sb = new StringBuilder();
        if (str.contains(".")) { //$NON-NLS-1$
            final int len = str.length();
            boolean nonZeroFound = false;
            for (int i = len - 1; i > -1; i--) {
                char c = str.charAt(i);
                if (c == ',') {
                    continue;
                }
                if (!nonZeroFound) {
                    if (c == '0') {
                        // ignore the zero digit
                    } else if (c == '.') {
                        nonZeroFound = true;
                    } else {
                        sb.insert(0, c);
                        nonZeroFound = true;
                    }
                } else {
                    sb.insert(0, c);
                }
            }
            String result = sb.toString();
            if (result.equals("-0")) { //$NON-NLS-1$
                result = "0"; //$NON-NLS-1$
            }
            return result;
        } else {
            return str;
        }
    }

    private String removeTrailingZeros2(String str) {
        StringBuilder sb = new StringBuilder();
        String result = str;
        if (str.contains(".")) { //$NON-NLS-1$
            final int len = str.length();
            boolean nonZeroFound = false;
            for (int i = len - 1; i > -1; i--) {
                char c = str.charAt(i);
                if (c == ',') {
                    continue;
                }
                if (!nonZeroFound) {
                    if (c == '0') {
                        // ignore the zero digit
                    } else if (c == '.') {
                        nonZeroFound = true;
                    } else {
                        sb.insert(0, c);
                        nonZeroFound = true;
                    }
                } else {
                    sb.insert(0, c);
                }
            }
            result = sb.toString();
            if (result.equals("-0")) { //$NON-NLS-1$
                result = "0"; //$NON-NLS-1$
            }
        }
        if (!result.contains(".") && !result.equals("0")) { //$NON-NLS-1$ //$NON-NLS-2$
            result = result + ".0"; //$NON-NLS-1$
        }
        return result;
    }

    private String removeTrailingZeros3(String str) {
        StringBuilder sb = new StringBuilder();
        String result = str;
        if (str.contains(".")) { //$NON-NLS-1$
            final int len = str.length();
            boolean nonZeroFound = false;
            for (int i = len - 1; i > -1; i--) {
                char c = str.charAt(i);
                if (c == ',') {
                    continue;
                }
                if (!nonZeroFound) {
                    if (c == '0') {
                        // ignore the zero digit
                    } else if (c == '.') {
                        nonZeroFound = true;
                    } else {
                        sb.insert(0, c);
                        nonZeroFound = true;
                    }
                } else {
                    sb.insert(0, c);
                }
            }
            result = sb.toString();
            if (result.equals("-0")) { //$NON-NLS-1$
                result = "0"; //$NON-NLS-1$
            }
        }
        int index = -1;
        if (!result.contains(".") && !result.equals("0")) { //$NON-NLS-1$ //$NON-NLS-2$
            result = result + ".00"; //$NON-NLS-1$
        } else if ((index = result.indexOf(".")) != -1 && result.length() - index < 2) { //$NON-NLS-1$
            result = result + "0"; //$NON-NLS-1$
        }
        return result;
    }

    private String addExtraSpaces1(String str) {
        final int len = str.length();
        if (len == 1) {
            return " " + str; //$NON-NLS-1$
        }

        return str;
    }

    private boolean isOfficialRules(int typ, double size, double divisions, double segments, double minor, boolean ccw) {

        // Crazy Reverse Engineering from Mike's PrimGen2
        // Thanks to Mr. Heidemann! :)

        // DONT TOUCH THIS CODE! It simply works...

        if (divisions != 16.0 && divisions != 48.0)
        {
            return false;
        }
        if (!ccw)
        {
            return false;
        }
        switch (typ)
        {
        case CIRCLE, CYLINDER, DISC, DISC_NEGATIVE, CHORD:
            return true;

        case RING, CONE:
            if (size % 1 != 0)
            {
                return false;
            }
            return minor == 1.0;

        case TORUS:
        {
            return divisions / segments % 1 == 0;
        }
        default:
            break;
        }
        return false;
    }
}

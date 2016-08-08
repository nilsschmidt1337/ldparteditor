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
package org.nschmidt.ldparteditor.dialogs.primgen2;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.text.SyntaxFormatter;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

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
public class PrimGen2Dialog extends PrimGen2Design {

    // I18N Needs translation / i18n!
    
    // FIXME Needs implementation (Logic)!
    
    private SyntaxFormatter syntaxFormatter;
    
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
        
        syntaxFormatter = new SyntaxFormatter(txt_data[0]);
        
        spn_major[0].setValue(2);
        spn_minor[0].setValue(1);
        
        Display.getCurrent().asyncExec(new Runnable() {
            @Override
            public void run() {
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
                sb.append("0 !LICENSE Redistributable under CCAL version 2.0 : see CAreadme.txt\n\n"); //$NON-NLS-1$

                sb.append("0 BFC CERTIFY CCW\n\n"); //$NON-NLS-1$
                sb.append("2 24 1 0 0 0.9239 0 0.3827\n"); //$NON-NLS-1$
                sb.append("2 24 0.9239 0 0.3827 0.7071 0 0.7071\n"); //$NON-NLS-1$
                sb.append("2 24 0.7071 0 0.7071 0.3827 0 0.9239\n"); //$NON-NLS-1$
                sb.append("2 24 0.3827 0 0.9239 0 0 1\n"); //$NON-NLS-1$
                sb.append("0 // Build by Primitive Generator 2\n"); //$NON-NLS-1$
                txt_data[0].setText(sb.toString());
            }
        });
        
        // MARK All final listeners will be configured here..
        
        txt_data[0].addLineStyleListener(new LineStyleListener() {
            @Override
            public void lineGetStyle(final LineStyleEvent e) {
                // So the line will be formated with the syntax formatter from
                // the CompositeText.
                final VertexManager vm = df.getVertexManager();
                final GData data = df.getDrawPerLine_NOCLONE().getValue(txt_data[0].getLineAtOffset(e.lineOffset) + 1);
                boolean isSelected = vm.isSyncWithTextEditor() && vm.getSelectedData().contains(data);
                isSelected = isSelected || vm.isSyncWithTextEditor() && GDataCSG.getSelection(df).contains(data);
                syntaxFormatter.format(e,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        0f, false, isSelected, df);
            }
        });
        
        txt_data[0].addExtendedModifyListener(new ExtendedModifyListener() {
            @Override
            public void modifyText(ExtendedModifyEvent event) {
                df.disposeData();
                df.setText(txt_data[0].getText());
                df.parseForData(false);
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        c3d.getRenderer().drawScene();
                    }
                });
            }
        });
        
        btn_ok[0].removeListener(SWT.Selection, btn_ok[0].getListeners(SWT.Selection)[0]);
        btn_cancel[0].removeListener(SWT.Selection, btn_cancel[0].getListeners(SWT.Selection)[0]);
        
        btn_ok[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // FIXME Needs implementation!
                getShell().close();
            }
        });
        
        btn_cancel[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                getShell().close();
            }
        });
        
        cmb_type[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final int CIRCLE = 0;
                final int RING = 1;
                final int CONE = 2;
                final int TORUS = 3;
                final int CYLINDER = 4;
                final int DISC = 5;
                final int DISC_NEGATIVE = 6;
                final int CHORD = 7;
                switch (cmb_type[0].getSelectionIndex()) {
                case CIRCLE:

                    break;
                case RING:

                    break;
                case CONE:

                    break;
                case TORUS:

                    break;
                case CYLINDER:

                    break;
                case DISC:

                    break;
                case DISC_NEGATIVE:

                    break;
                case CHORD:

                    break;
                default:
                    break;
                }
            }
        });
        
        int result = super.open();
        Project.getUnsavedFiles().remove(df);        
        df.disposeData();
        return result;
    } 
    
    @Override
    protected void handleShellCloseEvent() {
        c3d.getRenderer().disposeAllTextures();
        super.handleShellCloseEvent();
    }

}

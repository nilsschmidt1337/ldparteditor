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
package org.nschmidt.ldparteditor.dialogs.edger2;

import org.eclipse.jface.dialogs.DialogTray;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.composite3d.Edger2Settings;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.ValueChangeAdapter;

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
public class EdgerDialog extends EdgerDesign {

    /**
     * Create the dialog.
     *
     * @param parentShell
     * @param es
     */
    public EdgerDialog(Shell parentShell, Edger2Settings es) {
        super(parentShell, es);
    }

    @Override
    public int open() {
        super.create();

        if (NLogger.DEBUG) {
            getShell().addHelpListener(new HelpListener() {
                @Override
                public void helpRequested(HelpEvent e) {
                   try {
                       closeTray();
                   } catch (IllegalStateException ise) {
                       openTray(new DialogTray() {
                           @Override
                           protected Control createContents(Composite parent) {
                               Browser br = new Browser(parent, SWT.NONE);
                               return br;
                           }

                       });
                   }
                }
            });
        }

        // MARK All final listeners will be configured here..
        spn_ac[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                es.setAc(spn.getValue());
            }
        });
        spn_af[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                es.setAf(spn.getValue());
            }
        });
        spn_ae[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                es.setAe(spn.getValue());
            }
        });
        spn_vequ[0].addValueChangeListener(new ValueChangeAdapter() {
            @Override
            public void valueChanged(BigDecimalSpinner spn) {
                es.setEqualDistance(spn.getValue());
            }
        });
        cmb_b[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                es.setExtendedRange(cmb_b[0].getSelectionIndex() == 1);
            }
        });
        cmb_c[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                es.setCondlineOnQuads(cmb_c[0].getSelectionIndex() == 1);
            }
        });
        cmb_u[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                es.setUnmatchedMode(cmb_u[0].getSelectionIndex());
            }
        });
        cmb_scope[0].addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                es.setScope(cmb_scope[0].getSelectionIndex());
            }
        });
        return super.open();
    }

}

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
package org.nschmidt.ldparteditor.widget;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.resource.ResourceManager;

/**
 * @author nils
 *
 */
public class BigDecimalSpinner extends Composite {

    private final NButton[] btnUp = new NButton[1];
    private final NButton[] btnDown = new NButton[1];
    private final Text[] txtVal = new Text[1];
    private final Label[] lblWarn = new Label[1];

    private BigDecimal value;
    private BigDecimal maximum;
    private BigDecimal minimum;
    private DecimalValueChangeAdapter myListener;

    private final BigDecimalSpinner me;
    private java.text.DecimalFormat numberFormat;

    private BigDecimal smallIncrement = new BigDecimal(".0001"); //$NON-NLS-1$
    private BigDecimal largeIncrement = new BigDecimal(".01"); //$NON-NLS-1$

    private volatile AtomicInteger counter = new AtomicInteger();
    private volatile boolean focus = true;
    private volatile boolean forceUpdate = false;
    private volatile boolean selectAll = true;
    private volatile boolean invalidInput = false;

    public BigDecimalSpinner(final Composite parent, int style, String numberFormat) {
        super(parent, style);
        this.numberFormat = new java.text.DecimalFormat(numberFormat, new DecimalFormatSymbols(MyLanguage.getLocale()));
        me = this;
        createContents(parent);
    }

    /**
     * @param parent
     * @param style
     */
    public BigDecimalSpinner(final Composite parent, int style) {
        super(parent, style);
        numberFormat = new java.text.DecimalFormat(View.NUMBER_FORMAT4F, new DecimalFormatSymbols(MyLanguage.getLocale()));
        me = this;
        createContents(parent);
    }

    private void createContents(final Composite parent) {

        GridLayout gl = new GridLayout(5, false);

        gl.marginBottom = 0;
        gl.marginHeight = 0;
        gl.marginLeft = 0;
        gl.marginRight = 0;
        gl.marginTop = 0;
        gl.marginWidth = 0;

        this.setLayout(gl);

        GridData gd1 = new GridData();
        gd1.grabExcessHorizontalSpace = true;
        gd1.horizontalAlignment = SWT.FILL;
        gd1.grabExcessVerticalSpace = true;
        gd1.verticalAlignment = SWT.FILL;

        GridData gd2 = new GridData();
        gd2.grabExcessVerticalSpace = true;
        gd2.verticalAlignment = SWT.FILL;

        GridData gd3 = new GridData();
        gd3.grabExcessVerticalSpace = true;
        gd3.verticalAlignment = SWT.FILL;

        NButton dwn = new NButton(this, SWT.NONE);
        this.btnDown[0] = dwn;
        dwn.setImage(ResourceManager.getImage("icon16_previous.png")); //$NON-NLS-1$
        dwn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
                setValue(value.subtract(largeIncrement));
            }
        });

        Text txt = new Text(this, SWT.BORDER);
        this.txtVal[0] = txt;
        txt.setLayoutData(gd1);
        txt.setText("0"); //$NON-NLS-1$
        txt.addMouseWheelListener(e -> {
            Composite p = parent;
            while (p != null) {
                if (p.getHorizontalBar() != null && p.getHorizontalBar().isVisible() || p.getVerticalBar() != null && p.getVerticalBar().isVisible()) {
                    return;
                }
                p = p.getParent();
            }
            if (e.count > 0) {
                setValue(value.add(smallIncrement));
            } else {
                setValue(value.subtract(smallIncrement));
            }
        });

        txt.addListener(SWT.MouseDown, e -> {
            if (selectAll) {
                txtVal[0].selectAll();
                selectAll = false;
                new Thread( () -> {
                    focus = true;
                    while (focus && !txtVal[0].isDisposed()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new LDPartEditorException(ie);
                        }
                        Display.getDefault().asyncExec(() -> {
                            try {
                                focus = txtVal[0].isFocusControl();
                            } catch (SWTException swte) {
                                NLogger.debug(getClass(), swte);
                                return;
                            }
                        });
                    }
                    selectAll = true;
                }).start();
            }
        });

        final BigDecimal[] oldValue = new BigDecimal[] { BigDecimal.ZERO };
        txt.addModifyListener(e -> {

            if (invalidInput) {
                invalidInput = false;
                return;
            }


            int caret = txtVal[0].getCaretPosition();
            String text = null;
            final String result;
            try {
                numberFormat.setParseBigDecimal(true);
                BigDecimal val = (BigDecimal) numberFormat.parseObject(txtVal[0].getText());

                value = val;
                if (value.compareTo(maximum) > 0 || value.compareTo(minimum) < 0) {
                    oldValue[0] = value;
                    forceUpdate = true;
                    value = value.compareTo(maximum) > 0 ? maximum : value;
                    value = value.compareTo(minimum) < 0 ? minimum : value;
                }

                if (myListener != null)
                    myListener.valueChanged(me);

                boolean differenceBetweenDisplayedAndInput = false;
                if (oldValue[0].compareTo(value) != 0) {
                    oldValue[0] = value;
                    text = numberFormat.format(value);
                    try {
                        BigDecimal val2 = (BigDecimal) numberFormat.parseObject(text);
                        if (val2.compareTo(value) != 0) {
                            differenceBetweenDisplayedAndInput = true;
                        }
                    } catch (ParseException consumed) {}
                }
                if (differenceBetweenDisplayedAndInput) {
                    lblWarn[0].setImage(ResourceManager.getImage("icon16_warning.png")); //$NON-NLS-1$
                    lblWarn[0].setToolTipText("The real value is " + value.toEngineeringString() + " which differs from the displayed number!\nValue between " + minimum.toEngineeringString() + " and " + maximum.toEngineeringString() + "\nYou can input more digits than displayed."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ I18N Needs translation!
                } else {
                    lblWarn[0].setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
                    lblWarn[0].setToolTipText("Value between " + minimum.toEngineeringString() + " and " + maximum.toEngineeringString() + "\nYou can input more digits than displayed."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ I18N Needs translation!
                }
            } catch (ParseException ex) {
                lblWarn[0].setImage(ResourceManager.getImage("icon16_error.png")); //$NON-NLS-1$
                lblWarn[0].setToolTipText("Please insert a valid number."); //$NON-NLS-1$ I18N Needs translation!
                if (!invalidInput) {
                    text = numberFormat.format(value);
                }
            }
            result = text;

            new Thread(() -> {
                final int id = counter.getAndIncrement() + 1;
                focus = true;
                while (focus && counter.compareAndSet(id, id) && !forceUpdate && !txtVal[0].isDisposed()) {
                    Display.getDefault().asyncExec(() -> {
                        try  {
                            focus = txtVal[0].isFocusControl();
                        } catch (SWTException swte1) {
                            NLogger.debug(getClass(), swte1);
                            return;
                        }
                    });
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LDPartEditorException(ie);
                    }
                }
                if (!counter.compareAndSet(id, id) || result == null || txtVal[0].isDisposed()) {
                    return;
                }
                Display.getDefault().asyncExec(() -> {
                    invalidInput = true;
                    forceUpdate = false;
                    try {
                        txtVal[0].setText(result);
                    } catch (SWTException swte2) {
                        NLogger.debug(getClass(), swte2);
                    }
                });
            }).start();

            txtVal[0].setSelection(caret);
        });

        NButton up = new NButton(this, SWT.NONE);
        this.btnUp[0] = up;
        up.setImage(ResourceManager.getImage("icon16_next.png")); //$NON-NLS-1$
        up.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
                setValue(value.add(largeIncrement));
            }
        });

        Label warn = new Label(this, SWT.NONE);
        lblWarn[0] = warn;
        warn.setImage(ResourceManager.getImage("icon16_info.png")); //$NON-NLS-1$
        warn.setToolTipText("You can input more digits than displayed."); //$NON-NLS-1$ I18N Needs translation!

        Label placeholder = new Label(this, SWT.NONE);
        placeholder.setText("  "); //$NON-NLS-1$

        this.layout();
        this.pack();
    }

    public void addValueChangeListener(DecimalValueChangeAdapter vcl) {
        this.myListener = vcl;

    }

    public void setValue(BigDecimal value) {
        this.value = value.compareTo(maximum) > 0 ? maximum : value;
        this.value = value.compareTo(minimum) < 0 ? minimum : value;
        txtVal[0].setText(numberFormat.format(this.value));
        if (myListener != null)
            myListener.valueChanged(this);
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getStringValue() {
        return txtVal[0].getText();
    }

    public void setMinimum(BigDecimal min) {
        minimum = min;
    }

    public void setMaximum(BigDecimal max) {
        maximum = max;
    }

    @Override
    public void setEnabled(boolean enabled) {
        btnDown[0].setEnabled(enabled);
        btnUp[0].setEnabled(enabled);
        txtVal[0].setEditable(enabled);
        txtVal[0].setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public java.text.DecimalFormat getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(java.text.DecimalFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public BigDecimal getSmallIncrement() {
        return smallIncrement;
    }

    public void setSmallIncrement(BigDecimal smallIncrement) {
        this.smallIncrement = smallIncrement;
    }

    public BigDecimal getLargeIncrement() {
        return largeIncrement;
    }

    public void setLargeIncrement(BigDecimal largeIncrement) {
        this.largeIncrement = largeIncrement;
    }
}

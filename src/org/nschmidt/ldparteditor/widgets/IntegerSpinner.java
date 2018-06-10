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
package org.nschmidt.ldparteditor.widgets;

import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.resources.ResourceManager;

/**
 * @author nils
 *
 */
public class IntegerSpinner extends Composite {

    private final NButton[] btn_Up = new NButton[1];
    private final NButton[] btn_Down = new NButton[1];
    private final Text[] txt_val = new Text[1];

    private int value;
    private int maximum;
    private int minimum;
    private IntValueChangeAdapter myListener;

    private final IntegerSpinner me;
    private final java.text.DecimalFormat NUMBER_FORMAT0F = new java.text.DecimalFormat(View.NUMBER_FORMAT0F, new DecimalFormatSymbols(MyLanguage.LOCALE));

    private volatile AtomicInteger counter = new AtomicInteger();
    private volatile boolean focus = true;
    private volatile boolean forceUpdate = false;
    private volatile boolean selectAll = true;
    private volatile boolean invalidInput = false;

    /**
     * @param parent
     * @param style
     */
    public IntegerSpinner(final Composite parent, int style) {
        super(parent, style);
        me = this;
        GridLayout gl = new GridLayout(4, false);

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
        this.btn_Down[0] = dwn;
        dwn.setImage(ResourceManager.getImage("icon16_previous.png")); //$NON-NLS-1$
        dwn.addMouseListener(new MouseListener() {
            @Override
            public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
            }

            @Override
            public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
                setValue(value - 1);
            }

            @Override
            public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
            }
        });

        Text txt = new Text(this, SWT.BORDER);
        this.txt_val[0] = txt;
        txt.setLayoutData(gd1);
        txt.setText("0"); //$NON-NLS-1$
        txt.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent e) {
                Composite p = parent;
                while (p != null) {
                    if (p.getHorizontalBar() != null && p.getHorizontalBar().isVisible() || p.getVerticalBar() != null && p.getVerticalBar().isVisible()) {
                        return;
                    }
                    p = p.getParent();
                }
                if (e.count > 0) {
                    setValue(value + 1);
                } else {
                    setValue(value - 1);
                }
            }
        });

        txt.addListener(SWT.MouseDown, (e) -> {
            if (selectAll) {
                txt_val[0].selectAll();
                selectAll = false;
                new Thread( () -> {
                    focus = true;
                    while (focus && !txt_val[0].isDisposed()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {}
                        Display.getDefault().asyncExec(() -> {
                            try {
                                focus = txt_val[0].isFocusControl();
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

        final int[] oldValue = new int[] { 0 };
        txt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (invalidInput) {
                    invalidInput = false;
                    return;
                }

                int caret = txt_val[0].getCaretPosition();
                String text = null;
                final String result;
                try {
                    Number val = NUMBER_FORMAT0F.parse(txt_val[0].getText());
                    value = val.intValue();
                    if (value > maximum || value < minimum) {
                        oldValue[0] = value;
                        forceUpdate = true;
                        value = Math.min(value, maximum);
                        value = Math.max(value, minimum);
                    }

                    if (myListener != null)
                        myListener.valueChanged(me);

                    if (oldValue[0] != value) {
                        oldValue[0] = value;
                        text = NUMBER_FORMAT0F.format(value);
                    }
                } catch (ParseException ex) {
                    if (!invalidInput) {
                        text = NUMBER_FORMAT0F.format(value);
                    }
                }

                result = text;

                new Thread( () -> {
                    final int id = counter.getAndIncrement() + 1;
                    focus = true;
                    while (focus && counter.compareAndSet(id, id) && !forceUpdate && !txt_val[0].isDisposed()) {
                        Display.getDefault().asyncExec(() -> {
                            try {
                                focus = txt_val[0].isFocusControl();
                            } catch (SWTException swte) {
                                NLogger.debug(getClass(), swte);
                                return;
                            }
                        });
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ie) {}
                    }
                    if (!counter.compareAndSet(id, id) || result == null || txt_val[0].isDisposed()) {
                        return;
                    }
                    Display.getDefault().asyncExec(() -> {
                        invalidInput = true;
                        forceUpdate = false;
                        try {
                            txt_val[0].setText(result);
                        } catch (SWTException swte) {
                            NLogger.debug(getClass(), swte);
                        }
                    });
                }).start();

                txt_val[0].setSelection(caret);
            }
        });

        NButton up = new NButton(this, SWT.NONE);
        this.btn_Up[0] = up;
        up.setImage(ResourceManager.getImage("icon16_next.png")); //$NON-NLS-1$
        up.addMouseListener(new MouseListener() {
            @Override
            public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
            }

            @Override
            public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
                setValue(value + 1);
            }

            @Override
            public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
            }
        });

        Label placeholder = new Label(this, SWT.NONE);
        placeholder.setText("  "); //$NON-NLS-1$

        this.layout();
        this.pack();
    }

    public void addValueChangeListener(IntValueChangeAdapter vcl) {
        this.myListener = vcl;

    }

    public void setValue(int value) {
        this.value = Math.min(value, maximum);
        this.value = Math.max(this.value, minimum);
        txt_val[0].setText(NUMBER_FORMAT0F.format(this.value));
        if (myListener != null)
            myListener.valueChanged(this);
    }

    public int getValue() {
        return value;
    }

    public void setMinimum(int min) {
        minimum = min;
    }

    public void setMaximum(int max) {
        maximum = max;
    }

    @Override
    public void setEnabled(boolean enabled) {
        btn_Down[0].setEnabled(enabled);
        btn_Up[0].setEnabled(enabled);
        txt_val[0].setEditable(enabled);
        txt_val[0].setEnabled(enabled);
        super.setEnabled(enabled);
    }
}

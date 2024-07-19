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
package org.nschmidt.ldparteditor.composite;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.text.DecimalFormatSymbols;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.nschmidt.ldparteditor.enumtype.Font;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.Theming;

public class CompositeScale extends ScalableComposite {

    private final java.text.DecimalFormat numberFormat2f = new java.text.DecimalFormat(View.NUMBER_FORMAT2F, new DecimalFormatSymbols(MyLanguage.getLocale()));

    private final Canvas canvasHorizontal;
    private final Canvas canvasVertical;

    private int posHorizontal;
    private int posVertical;

    public CompositeScale(Composite parentCompositeContainer, Composite3D composite3D, int style) {
        super(parentCompositeContainer, style);
        final CompositeScale me = this;
        this.setParent(parentCompositeContainer);
        GridLayout glCompositeScale = new GridLayout(2, false);
        glCompositeScale.marginHeight = 0;
        glCompositeScale.marginWidth = 0;
        glCompositeScale.verticalSpacing = 0;
        glCompositeScale.horizontalSpacing = 0;
        this.setLayout(glCompositeScale);

        NButton btnOpenContextMenu = new NButton(this, Cocoa.getStyle());
        GridData gdBtnOpenContextMenu = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        gdBtnOpenContextMenu.heightHint = 20;
        gdBtnOpenContextMenu.widthHint = 20;
        btnOpenContextMenu.setLayoutData(gdBtnOpenContextMenu);
        btnOpenContextMenu.setImage(ResourceManager.getImage("icon16_contextMenu.png")); //$NON-NLS-1$
        btnOpenContextMenu.setBounds(0, 0, 20, 20);

        widgetUtil(btnOpenContextMenu).addSelectionListener(e -> {
            PointerInfo a = MouseInfo.getPointerInfo();
            java.awt.Point b = a.getLocation();
            int x = (int) b.getX();
            int y = (int) b.getY();
            if (!me.getComposite3D().getMenu().isDisposed()) {
                me.getComposite3D().getMenu().setLocation(x, y);
                me.getComposite3D().getMenu().setVisible(true);
            }
        });

        canvasHorizontal = new Canvas(this, SWT.BORDER | SWT.NO_BACKGROUND);
        canvasHorizontal.setForeground(Theming.getFgColor());
        canvasHorizontal.setBackground(Theming.getBgColor());
        canvasHorizontal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        canvasHorizontal.setLayout(new FillLayout(SWT.HORIZONTAL));

        Listener paintHorizontalScaleListener = e -> {
            if (e.gc == null)
                return;
            Rectangle rect = canvasHorizontal.getBounds();
            // Create the image to fill the canvas
            Image image = new Image(Display.getCurrent(), rect);
            // Set up the offscreen gc
            GC gc = new GC(image);
            // Draw the background
            gc.setBackground(e.gc.getBackground());
            gc.setForeground(e.gc.getForeground());
            gc.fillRectangle(image.getBounds());

            Composite3D c3d = me.getComposite3D();

            if (c3d.isClassicPerspective()) {
                float width = rect.width;
                float halfwidth = width / 2f;
                float offset = 0;
                float viewportPixelPerLDU = c3d.getZoom() * View.PIXEL_PER_LDU;
                while (viewportPixelPerLDU > 10) {
                    viewportPixelPerLDU = viewportPixelPerLDU / 10f;
                }
                while (viewportPixelPerLDU < 10) {
                    viewportPixelPerLDU = viewportPixelPerLDU * 10f;
                }
                if (viewportPixelPerLDU > 10) {
                    viewportPixelPerLDU = viewportPixelPerLDU / 2f;
                }
                float step = viewportPixelPerLDU;
                int factor = 1;
                gc.setFont(Font.SMALL);
                gc.drawText(I18n.getCurrentUnit(), 0, 0);

                // Draw the ruler
                float twostep = step * 2;
                float tenstep = step * 10;
                // MARK Ruler Perspective Horizontal
                switch (c3d.getPerspectiveIndex()) {
                case FRONT:
                    offset = (int) (c3d.getTranslation().m30 * c3d.getZoom() * View.PIXEL_PER_LDU) - 2f;
                    break;
                case BACK:
                    offset = (int) -(c3d.getTranslation().m30 * c3d.getZoom() * View.PIXEL_PER_LDU) - 2f;
                    factor = -1;
                    break;
                case LEFT:
                    offset = (int) -(c3d.getTranslation().m32 * c3d.getZoom() * View.PIXEL_PER_LDU) - 2f;
                    factor = -1;
                    break;
                case RIGHT:
                    offset = (int) (c3d.getTranslation().m32 * c3d.getZoom() * View.PIXEL_PER_LDU) - 2f;
                    break;
                case TOP:
                    offset = (int) (c3d.getTranslation().m30 * c3d.getZoom() * View.PIXEL_PER_LDU) - 2f;
                    break;
                case BOTTOM:
                    offset = (int) (c3d.getTranslation().m30 * c3d.getZoom() * View.PIXEL_PER_LDU) - 2f;
                    break;
                case TWO_THIRDS:
                    throw new AssertionError();
                }

                float scaleFactor = factor / (View.PIXEL_PER_LDU * c3d.getZoom() * 1000f) * View.unitFactor.floatValue();

                if (offset < -halfwidth) {
                    // "Positive"
                    // Origin
                    float origin1 = (offset + halfwidth) % tenstep;
                    float origin21 = offset + halfwidth;
                    // Big lines
                    for (float x1 = origin1 + tenstep; x1 < width; x1 = x1 + tenstep) {
                        if (x1 > 20) {
                            gc.drawText(numberFormat2f.format((x1 - origin21) * scaleFactor), (int) x1 + 2, -2);
                            gc.drawLine((int) x1, 5, (int) x1, 10);
                        }
                    }
                    // Small lines
                    for (float x2 = origin1 + step; x2 < width; x2 = x2 + twostep) {
                        if (x2 > 20) {
                            gc.drawLine((int) x2, 13, (int) x2, 16);
                        }
                    }
                    // Middle
                    for (float x3 = origin1 + twostep; x3 < width; x3 = x3 + twostep) {
                        if (x3 > 20) {
                            gc.drawLine((int) x3, 10, (int) x3, 16);
                        }
                    }
                } else if (offset > halfwidth) {
                    // "Negative"
                    // Origin
                    float origin3 = width - (halfwidth - offset) % tenstep;
                    float origin22 = offset + halfwidth;
                    // Big lines
                    for (float x4 = origin3 - tenstep; x4 > 20; x4 = x4 - tenstep) {
                        gc.drawText(numberFormat2f.format((x4 - origin22) * scaleFactor), (int) x4 + 2, -2);
                        gc.drawLine((int) x4, 5, (int) x4, 10);
                    }
                    // Small lines
                    for (float x5 = origin3 - step; x5 > 20; x5 = x5 - twostep) {
                        gc.drawLine((int) x5, 13, (int) x5, 16);
                    }
                    // Middle
                    for (float x6 = origin3 - twostep; x6 > 20; x6 = x6 - twostep) {
                        gc.drawLine((int) x6, 10, (int) x6, 16);
                    }
                } else {
                    // Both
                    // Origin
                    float origin4 = offset + halfwidth;
                    if (origin4 > 22) {
                        int originInt = (int) origin4;
                        gc.drawLine(originInt - 1, 0, originInt - 1, 16);
                        gc.drawLine(originInt, 0, originInt, 16);
                        gc.drawLine(originInt + 1, 0, originInt + 1, 16);
                    }
                    // Big lines
                    for (float x7 = origin4 - tenstep; x7 > 20; x7 = x7 - tenstep) {
                        gc.drawText(numberFormat2f.format((x7 - origin4) * scaleFactor), (int) x7 + 2, -2);
                        gc.drawLine((int) x7, 5, (int) x7, 10);
                    }
                    for (float x8 = origin4 + tenstep; x8 < width; x8 = x8 + tenstep) {
                        gc.drawText(numberFormat2f.format((x8 - origin4) * scaleFactor), (int) x8 + 2, -2);
                        gc.drawLine((int) x8, 5, (int) x8, 10);
                    }
                    // Small lines
                    for (float x9 = origin4 - step; x9 > 20; x9 = x9 - twostep) {
                        gc.drawLine((int) x9, 13, (int) x9, 16);
                    }
                    for (float x10 = origin4 + step; x10 < width; x10 = x10 + twostep) {
                        gc.drawLine((int) x10, 13, (int) x10, 16);
                    }
                    // Middle
                    for (float x11 = origin4 - twostep; x11 > 20; x11 = x11 - twostep) {
                        gc.drawLine((int) x11, 10, (int) x11, 16);
                    }
                    for (float x12 = origin4 + twostep; x12 < width; x12 = x12 + twostep) {
                        gc.drawLine((int) x12, 10, (int) x12, 16);
                    }
                }
            }
            // Draw the triangle
            gc.setBackground(Theming.getFgColor());
            gc.fillPolygon(new int[] { posHorizontal - 5, 10, posHorizontal, 16, posHorizontal + 5, 10, posHorizontal - 5, 10 });
            // Draw the offscreen buffer to the screen
            e.gc.drawImage(image, 0, 0);
            // Clean up
            image.dispose();
            gc.dispose();
        };
        canvasHorizontal.addListener(SWT.Resize, paintHorizontalScaleListener);
        canvasHorizontal.addListener(SWT.Paint, paintHorizontalScaleListener);

        canvasVertical = new Canvas(this, SWT.BORDER | SWT.NO_BACKGROUND);
        canvasVertical.setForeground(Theming.getFgColor());
        canvasVertical.setBackground(Theming.getBgColor());
        canvasVertical.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
        canvasVertical.setLayout(new FillLayout(SWT.HORIZONTAL));

        Listener paintVerticalScaleListener = e -> {
            if (e.gc == null)
                return;
            Rectangle rect = canvasVertical.getBounds();
            // Create the image to fill the canvas
            Image image = new Image(Display.getCurrent(), rect);
            // Set up the offscreen gc
            GC gc = new GC(image);
            // Draw the background
            gc.setBackground(e.gc.getBackground());
            gc.setForeground(e.gc.getForeground());
            gc.fillRectangle(image.getBounds());

            Composite3D c3d = me.getComposite3D();

            if (c3d.isClassicPerspective()) {
                float height = rect.height;
                float halfheight = height / 2f;
                float offset = 0;
                float viewportPixelPerLDU = c3d.getZoom() * View.PIXEL_PER_LDU;
                while (viewportPixelPerLDU > 10) {
                    viewportPixelPerLDU = viewportPixelPerLDU / 10f;
                }
                while (viewportPixelPerLDU < 10) {
                    viewportPixelPerLDU = viewportPixelPerLDU * 10f;
                }
                if (viewportPixelPerLDU > 10) {
                    viewportPixelPerLDU = viewportPixelPerLDU / 2f;
                }
                float step = viewportPixelPerLDU;
                int factor = 1;
                gc.setFont(Font.SMALL);

                // Draw the ruler
                float twostep = step * 2;
                float tenstep = step * 10;
                // MARK Ruler Perspective Vertical
                switch (c3d.getPerspectiveIndex()) {
                case FRONT:
                    offset = (int) (c3d.getTranslation().m31 * c3d.getZoom() * View.PIXEL_PER_LDU);
                    break;
                case BACK:
                    offset = (int) (c3d.getTranslation().m31 * c3d.getZoom() * View.PIXEL_PER_LDU);
                    break;
                case LEFT:
                    offset = (int) (c3d.getTranslation().m31 * c3d.getZoom() * View.PIXEL_PER_LDU);
                    break;
                case RIGHT:
                    offset = (int) (c3d.getTranslation().m31 * c3d.getZoom() * View.PIXEL_PER_LDU);
                    break;
                case TOP:
                    offset = (int) -(c3d.getTranslation().m32 * c3d.getZoom() * View.PIXEL_PER_LDU);
                    factor = -1;
                    break;
                case BOTTOM:
                    offset = (int) (c3d.getTranslation().m32 * c3d.getZoom() * View.PIXEL_PER_LDU);
                    break;
                case TWO_THIRDS:
                    throw new AssertionError();
                }

                float scaleFactor = factor / (View.PIXEL_PER_LDU * c3d.getZoom() * 1000f) * View.unitFactor.floatValue();

                if (offset < -halfheight) {
                    // "Positive"
                    // Origin
                    float origin1 = (offset + halfheight) % tenstep;
                    float origin21 = offset + halfheight;
                    // Big lines
                    for (float y1 = origin1 + tenstep; y1 < height; y1 = y1 + tenstep) {
                        Transform tr1 = new Transform(Display.getCurrent());
                        tr1.translate(0, (int) y1);
                        tr1.rotate(-90);
                        gc.setTransform(tr1);
                        gc.drawText(numberFormat2f.format((y1 - origin21) * scaleFactor), 0, 0);
                        gc.setTransform(null);
                        gc.drawLine(5, (int) y1, 10, (int) y1);
                    }
                    // Small lines
                    for (float y2 = origin1 + step; y2 < height; y2 = y2 + twostep) {
                        gc.drawLine(13, (int) y2, 16, (int) y2);
                    }
                    // Middle
                    for (float y3 = origin1 + twostep; y3 < height; y3 = y3 + twostep) {
                        gc.drawLine(10, (int) y3, 16, (int) y3);
                    }
                } else if (offset > halfheight) {
                    // "Negative"
                    // Origin
                    float origin3 = height - (halfheight - offset) % tenstep;
                    float origin22 = offset + halfheight;
                    // Big lines
                    for (float y4 = origin3 - tenstep; y4 > 0; y4 = y4 - tenstep) {
                        Transform tr2 = new Transform(Display.getCurrent());
                        tr2.translate(0, (int) y4);
                        tr2.rotate(-90);
                        gc.setTransform(tr2);
                        gc.drawText(numberFormat2f.format((y4 - origin22) * scaleFactor), 0, 0);
                        gc.setTransform(null);
                        gc.drawLine(5, (int) y4, 10, (int) y4);
                    }
                    // Small lines
                    for (float y5 = origin3 - step; y5 > 0; y5 = y5 - twostep) {
                        gc.drawLine(13, (int) y5, 16, (int) y5);
                    }
                    // Middle
                    for (float y6 = origin3 - twostep; y6 > 0; y6 = y6 - twostep) {
                        gc.drawLine(10, (int) y6, 16, (int) y6);
                    }
                } else {
                    // Both
                    // Origin
                    float origin4 = offset + halfheight;
                    int originInt = (int) origin4;
                    gc.drawLine(0, originInt - 1, 16, originInt - 1);
                    gc.drawLine(0, originInt, 16, originInt);
                    gc.drawLine(0, originInt + 1, 16, originInt + 1);
                    // Big lines
                    for (float y7 = origin4 - tenstep; y7 > 0; y7 = y7 - tenstep) {
                        Transform tr3 = new Transform(Display.getCurrent());
                        tr3.translate(0, (int) y7);
                        tr3.rotate(-90);
                        gc.setTransform(tr3);
                        gc.drawText(numberFormat2f.format((y7 - origin4) * scaleFactor), 0, 0);
                        gc.setTransform(null);
                        gc.drawLine(5, (int) y7, 10, (int) y7);
                    }
                    for (float y8 = origin4 + tenstep; y8 < height; y8 = y8 + tenstep) {
                        Transform tr4 = new Transform(Display.getCurrent());
                        tr4.translate(0, (int) y8);
                        tr4.rotate(-90);
                        gc.setTransform(tr4);
                        gc.drawText(numberFormat2f.format((y8 - origin4) * scaleFactor), 0, 0);
                        gc.setTransform(null);
                        gc.drawLine(5, (int) y8, 10, (int) y8);
                    }
                    // Small lines
                    for (float y9 = origin4 - step; y9 > 0; y9 = y9 - twostep) {
                        gc.drawLine(13, (int) y9, 16, (int) y9);
                    }
                    for (float y10 = origin4 + step; y10 < height; y10 = y10 + twostep) {
                        gc.drawLine(13, (int) y10, 16, (int) y10);
                    }
                    // Middle
                    for (float y11 = origin4 - twostep; y11 > 0; y11 = y11 - twostep) {
                        gc.drawLine(10, (int) y11, 16, (int) y11);
                    }
                    for (float y12 = origin4 + twostep; y12 < height; y12 = y12 + twostep) {
                        gc.drawLine(10, (int) y12, 16, (int) y12);
                    }
                }
            }
            // Draw the triangle
            gc.setBackground(Theming.getFgColor());
            gc.fillPolygon(new int[] { 10, posVertical - 5, 16, posVertical, 10, posVertical + 5, 10, posVertical - 5 });
            // Draw the offscreen buffer to the screen
            e.gc.drawImage(image, 0, 0);
            // Clean up
            image.dispose();
            gc.dispose();
        };
        canvasVertical.addListener(SWT.Resize, paintVerticalScaleListener);
        canvasVertical.addListener(SWT.Paint, paintVerticalScaleListener);

        composite3D.setParent(this);
        composite3D.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    }

    @Override
    public SashForm getSashForm() {
        return (SashForm) this.getParent().getParent();
    }

    @Override
    public Composite3D getComposite3D() {
        return (Composite3D) this.getChildren()[3];
    }

    @Override
    public CompositeContainer getCompositeContainer() {
        return ((ScalableComposite) this.getParent()).getCompositeContainer();
    }

    @Override
    public void redrawScales(int x, int y) {
        posHorizontal = x - 2;
        posVertical = y - 2;
        canvasHorizontal.redraw();
        canvasHorizontal.update();
        canvasVertical.redraw();
        canvasVertical.update();
    }

    @Override
    public void redrawScales() {
        canvasHorizontal.redraw();
        canvasHorizontal.update();
        canvasVertical.redraw();
        canvasVertical.update();
    }
}

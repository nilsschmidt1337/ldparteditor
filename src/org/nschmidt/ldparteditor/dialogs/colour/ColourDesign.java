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
package org.nschmidt.ldparteditor.dialogs.colour;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

import java.text.MessageFormat;
import java.util.TreeSet;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.composites.ToolItem;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.enums.IconSize;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 * This first dialog - shown on startup - asks for mandatory information about
 * the user.
 * <p>
 * Note: This class should not be instantiated, it defines the gui layout and no
 * business logic.
 *
 * @author nils
 *
 */
class ColourDesign extends ApplicationWindow {

    final GColour[] refCol;
    final ColourDesign me;
    final boolean randomColours;

    // Use final only for subclass/listener references!
    final NButton[] btnColourChoose = new NButton[1];
    final NButton[] btnColourTable = new NButton[1];
    final NButton[] btnRandomColours = new NButton[1];

    ColourDesign(Shell parentShell, GColour[] refCol, final boolean randomColours) {
        super(parentShell);
        this.refCol = refCol;
        this.randomColours = randomColours;
        me = this;
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite cmpContainer = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        cmpContainer.setLayout(gridLayout);

        Label lblColourTitle = new Label(cmpContainer, SWT.NONE);
        lblColourTitle.setText(I18n.COLOURDIALOG_COLOUR_TITLE);

        {
            Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
            lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        }

        {
            Label lblEmptyLine = new Label(cmpContainer, SWT.NONE);
            lblEmptyLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        }

        NButton btnPickDirectColour = new NButton(cmpContainer, SWT.NONE);
        btnColourChoose[0] = btnPickDirectColour;
        btnPickDirectColour.setText(I18n.COLOURDIALOG_DIRECT_COLOUR);

        {
            Label lblEmptyLine = new Label(cmpContainer, SWT.NONE);
            lblEmptyLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        }

        if (randomColours) {
            NButton btnRandomColour = new NButton(cmpContainer, SWT.NONE);
            btnRandomColours[0] = btnRandomColour;
            btnRandomColour.setText(I18n.COLOURDIALOG_RANDOM_COLOURS);
            btnRandomColour.setImage(ResourceManager.getImage("icon16_randomColours.png")); //$NON-NLS-1$

            {
                Label lblEmptyLine = new Label(cmpContainer, SWT.NONE);
                lblEmptyLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            }
        }

        Label lblStdColour = new Label(cmpContainer, SWT.NONE);
        lblStdColour.setText(I18n.COLOURDIALOG_STANDARD_COLOURS);

        {
            Label lblSeparator = new Label(cmpContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
            lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        }

        {
            Label lblEmptyLine = new Label(cmpContainer, SWT.NONE);
            lblEmptyLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        }

        NButton btnShowTable = new NButton(cmpContainer, SWT.NONE);
        btnColourTable[0] = btnShowTable;
        btnShowTable.setText(I18n.COLOURDIALOG_SHOW_COLOUR_TABLE);

        {
            Label lblEmptyLine = new Label(cmpContainer, SWT.NONE);
            lblEmptyLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        }

        TreeSet<Integer> ldConfIndices = new TreeSet<>(View.getColourMap().keySet());

        int counter = 0;
        ToolItem toolItemColours = new ToolItem(cmpContainer, Cocoa.getStyle(), true);
        for (Integer index : ldConfIndices) {
            if (counter == 17) {
                toolItemColours = new ToolItem(cmpContainer, Cocoa.getStyle(), true);
                counter = 0;
            }
            final GColour gColour2 = View.getLDConfigColour(index);
            addColorButton(toolItemColours, gColour2);
            counter++;
        }
        cmpContainer.pack();
        return cmpContainer;
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }

    private void addColorButton(ToolItem toolItemColours, GColour gColour) {
        int cn = gColour.getColourNumber();
        if (cn != -1 && View.hasLDConfigColour(cn)) {
            gColour = View.getLDConfigColour(cn);
        }
        final int imgSize = IconSize.getImageSizeFromIconSize();
        final GColour[] gColour2 = new GColour[] { gColour };
        final Color[] col = new Color[1];
        col[0] = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));

        final NButton btnCol = new NButton(toolItemColours, Cocoa.getStyle());
        btnCol.setData(gColour);
        int num = gColour2[0].getColourNumber();
        if (!View.hasLDConfigColour(num)) {
            num = -1;
        }
        if (num != -1) {

            Object[] messageArguments = {num, View.getLDConfigColourName(num)};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.locale);
            formatter.applyPattern(I18n.EDITORTEXT_COLOUR_1);

            btnCol.setToolTipText(formatter.format(messageArguments));
        } else {

            StringBuilder colourBuilder = new StringBuilder();
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

            Object[] messageArguments = {colourBuilder.toString()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.locale);
            formatter.applyPattern(I18n.EDITORTEXT_COLOUR_2);

            btnCol.setToolTipText(formatter.format(messageArguments));
        }

        btnCol.setImage(ResourceManager.getImage("icon16_fullTransparent.png")); //$NON-NLS-1$

        widgetUtil(btnCol).addSelectionListener(e -> {
            refCol[0] = (GColour) btnCol.getData();
            me.close();
        });
        final Point size = btnCol.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        final int x = Math.round(size.x / 5f);
        final int y = Math.round(size.y / 5f);
        final int w = Math.round(size.x * (3f / 5f));
        final int h = Math.round(size.y * (3f / 5f));
        btnCol.addPaintListener(e -> {
            e.gc.setBackground(col[0]);
            e.gc.fillRectangle(x, y, w, h);
            if (gColour2[0].getA() == 1f) {
                e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
            } else {
                e.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
            }
        });
    }
}

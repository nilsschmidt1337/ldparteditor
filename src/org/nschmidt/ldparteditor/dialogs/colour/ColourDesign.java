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

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import java.text.MessageFormat;
import java.util.TreeSet;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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
    final NButton[] btn_colourChoose = new NButton[1];
    final NButton[] btn_colourTable = new NButton[1];
    final NButton[] btn_randomColours = new NButton[1];

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
        Composite cmp_container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        cmp_container.setLayout(gridLayout);

        Label lbl_colourTitle = new Label(cmp_container, SWT.NONE);
        lbl_colourTitle.setText(I18n.COLOURDIALOG_ColourTitle);

        {
            Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
            lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        }

        {
            Label lbl_emptyLine = new Label(cmp_container, SWT.NONE);
            lbl_emptyLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        }

        NButton btn_pickDirectColour = new NButton(cmp_container, SWT.NONE);
        btn_colourChoose[0] = btn_pickDirectColour;
        btn_pickDirectColour.setText(I18n.COLOURDIALOG_DirectColour);

        {
            Label lbl_emptyLine = new Label(cmp_container, SWT.NONE);
            lbl_emptyLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        }

        if (randomColours) {
            NButton btn_randomColour = new NButton(cmp_container, SWT.NONE);
            btn_randomColours[0] = btn_randomColour;
            btn_randomColour.setText(I18n.COLOURDIALOG_RandomColours);
            btn_randomColour.setImage(ResourceManager.getImage("icon16_randomColours.png")); //$NON-NLS-1$

            {
                Label lbl_emptyLine = new Label(cmp_container, SWT.NONE);
                lbl_emptyLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            }
        }

        Label lbl_stdColour = new Label(cmp_container, SWT.NONE);
        lbl_stdColour.setText(I18n.COLOURDIALOG_StandardColours);

        {
            Label lbl_separator = new Label(cmp_container, SWT.SEPARATOR | SWT.HORIZONTAL);
            lbl_separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        }

        {
            Label lbl_emptyLine = new Label(cmp_container, SWT.NONE);
            lbl_emptyLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        }

        NButton btn_showTable = new NButton(cmp_container, SWT.NONE);
        btn_colourTable[0] = btn_showTable;
        btn_showTable.setText(I18n.COLOURDIALOG_ShowColourTable);

        {
            Label lbl_emptyLine = new Label(cmp_container, SWT.NONE);
            lbl_emptyLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        }

        TreeSet<Integer> ldConfIndices = new TreeSet<Integer>(View.getColourMap().keySet());

        int counter = 0;
        ToolItem toolItem_Colours = new ToolItem(cmp_container, Cocoa.getStyle(), true);
        for (Integer index : ldConfIndices) {
            if (counter == 17) {
                toolItem_Colours = new ToolItem(cmp_container, Cocoa.getStyle(), true);
                counter = 0;
            }
            final GColour gColour2 = View.getLDConfigColour(index);
            addColorButton(toolItem_Colours, gColour2);
            counter++;
        }
        cmp_container.pack();
        return cmp_container;
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();
    }

    private void addColorButton(ToolItem toolItem_Colours, GColour gColour) {
        int cn = gColour.getColourNumber();
        if (cn != -1 && View.hasLDConfigColour(cn)) {
            gColour = View.getLDConfigColour(cn);
        }
        final int imgSize = IconSize.getImageSizeFromIconSize();
        final GColour[] gColour2 = new GColour[] { gColour };
        final Color[] col = new Color[1];
        col[0] = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));

        final NButton btn_Col = new NButton(toolItem_Colours, Cocoa.getStyle());
        btn_Col.setData(gColour);
        int num = gColour2[0].getColourNumber();
        if (!View.hasLDConfigColour(num)) {
            num = -1;
        }
        if (num != -1) {

            Object[] messageArguments = {num, View.getLDConfigColourName(num)};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.LOCALE);
            formatter.applyPattern(I18n.EDITORTEXT_Colour1);

            btn_Col.setToolTipText(formatter.format(messageArguments));
        } else {

            StringBuilder colourBuilder = new StringBuilder();
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

            Object[] messageArguments = {colourBuilder.toString()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.LOCALE);
            formatter.applyPattern(I18n.EDITORTEXT_Colour2);

            btn_Col.setToolTipText(formatter.format(messageArguments));
        }

        btn_Col.setImage(ResourceManager.getImage("icon16_fullTransparent.png")); //$NON-NLS-1$

        WidgetUtil(btn_Col).addXSelectionListener(e -> {
            refCol[0] = (GColour) btn_Col.getData();
            me.close();
        });
        final Point size = btn_Col.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        final int x = Math.round(size.x / 5f);
        final int y = Math.round(size.y / 5f);
        final int w = Math.round(size.x * (3f / 5f));
        final int h = Math.round(size.y * (3f / 5f));
        btn_Col.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                e.gc.setBackground(col[0]);
                e.gc.fillRectangle(x, y, w, h);
                if (gColour2[0].getA() == 1f) {
                    e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                } else {
                    e.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                }
            }
        });
    }
}

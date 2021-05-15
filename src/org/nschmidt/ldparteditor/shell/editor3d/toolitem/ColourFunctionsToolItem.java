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
package org.nschmidt.ldparteditor.shell.editor3d.toolitem;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enumtype.IconSize;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.widget.NButton;

public class ColourFunctionsToolItem extends ToolItem {

    private static final NButton[] btnLastUsedColourPtr = new NButton[1];
    private static final NButton[] btnPipettePtr = new NButton[1];
    private static final NButton[] btnDecolourPtr = new NButton[1];

    private static GColour lastUsedColour = LDConfig.getColour16();

    public ColourFunctionsToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createWidgets(this);
        addListeners();
    }

    static NButton getLastUsedColourBtn() {
        return btnLastUsedColourPtr[0];
    }

    static GColour getLastUsedColour() {
        return lastUsedColour;
    }

    static void setLastUsedColour(GColour lastUsedColour) {
        ColourFunctionsToolItem.lastUsedColour = lastUsedColour;
    }

    private static void createWidgets(ColourFunctionsToolItem colourFunctionsToolItem) {
        final int imgSize = IconSize.getImageSizeFromIconSize();
        NButton btnLastUsedColour = new NButton(colourFunctionsToolItem, Cocoa.getStyle());
        btnLastUsedColourPtr[0] = btnLastUsedColour;
        btnLastUsedColour.setToolTipText(I18n.E3D_COLOUR_16);
        btnLastUsedColour.setImage(ResourceManager.getImage("icon16_fullTransparent.png")); //$NON-NLS-1$

        final GColour col16 = LDConfig.getColour16();
        final Color col = SWTResourceManager.getColor((int) (255f * col16.getR()), (int) (255f * col16.getG()), (int) (255f * col16.getB()));
        final Point size = btnLastUsedColour.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        final int x = Math.round(size.x / 5f);
        final int y = Math.round(size.y / 5f);
        final int w = Math.round(size.x * (3f / 5f));
        final int h = Math.round(size.y * (3f / 5f));
        btnLastUsedColour.addPaintListener(e -> {
            e.gc.setBackground(col);
            e.gc.fillRectangle(x, y, w, h);
            e.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
        });
        widgetUtil(btnLastUsedColour).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                GColour col1 = LDConfig.getColour16();
                Project.getFileToEdit().getVertexManager().colourChangeSelection(16, col1.getR(), col1.getG(), col1.getB(), 1f, true);
            }
            Editor3DWindow.getWindow().regainFocus();
        });

        NButton btnPipette = new NButton(colourFunctionsToolItem, Cocoa.getStyle());
        btnPipettePtr[0] = btnPipette;
        btnPipette.setToolTipText(I18n.E3D_PIPETTE);
        btnPipette.setImage(ResourceManager.getImage("icon16_pipette.png")); //$NON-NLS-1$

        NButton btnDecolour = new NButton(colourFunctionsToolItem, Cocoa.getStyle());
        btnDecolourPtr[0] = btnDecolour;
        btnDecolour.setToolTipText(I18n.E3D_DECOLOUR);
        btnDecolour.setImage(ResourceManager.getImage("icon16_uncolour.png")); //$NON-NLS-1$
    }

    private static void addListeners() {
        widgetUtil(btnPipettePtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                VertexManager vm = Project.getFileToEdit().getVertexManager();
                vm.addSnapshot();
                final GColour gColour2 = pickRandomColourFromSelection(vm);
                btnLastUsedColourPtr[0].clearPaintListeners();
                btnLastUsedColourPtr[0].clearSelectionListeners();
                final int imgSize = IconSize.getImageSizeFromIconSize();
                final Color col = SWTResourceManager.getColor((int) (gColour2.getR() * 255f), (int) (gColour2.getG() * 255f), (int) (gColour2.getB() * 255f));
                final Point size = btnLastUsedColourPtr[0].computeSize(SWT.DEFAULT, SWT.DEFAULT);
                final int x = Math.round(size.x / 5f);
                final int y = Math.round(size.y / 5f);
                final int w = Math.round(size.x * (3f / 5f));
                final int h = Math.round(size.y * (3f / 5f));
                int num = gColour2.getColourNumber();
                btnLastUsedColourPtr[0].addPaintListener(e1 -> {
                    e1.gc.setBackground(col);
                    e1.gc.fillRectangle(x, y, w, h);
                    if (gColour2.getA() >= .99f) {
                        e1.gc.drawImage(ResourceManager.getImage("icon16_transparent.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                    } else {
                        e1.gc.drawImage(ResourceManager.getImage("icon16_halftrans.png"), 0, 0, imgSize, imgSize, x, y, w, h); //$NON-NLS-1$
                    }
                });
                widgetUtil(btnLastUsedColourPtr[0]).addSelectionListener(e1 -> {
                    if (Project.getFileToEdit() != null) {
                        int num1 = gColour2.getColourNumber();
                        if (!LDConfig.hasColour(num1)) {
                            num1 = -1;
                        }
                        Project.getFileToEdit().getVertexManager().addSnapshot();
                        Project.getFileToEdit().getVertexManager().colourChangeSelection(num1, gColour2.getR(), gColour2.getG(), gColour2.getB(), gColour2.getA(), true);
                    }
                    regainFocus();
                });
                if (num != -1) {

                    Object[] messageArguments1 = {num, LDConfig.getColourName(num)};
                    MessageFormat formatter1 = new MessageFormat(""); //$NON-NLS-1$
                    formatter1.setLocale(MyLanguage.getLocale());
                    formatter1.applyPattern(I18n.EDITORTEXT_COLOUR_1);

                    btnLastUsedColourPtr[0].setToolTipText(formatter1.format(messageArguments1));
                } else {
                    StringBuilder colourBuilder = new StringBuilder();
                    colourBuilder.append("0x2"); //$NON-NLS-1$
                    colourBuilder.append(MathHelper.toHex((int) (255f * gColour2.getR())).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * gColour2.getG())).toUpperCase());
                    colourBuilder.append(MathHelper.toHex((int) (255f * gColour2.getB())).toUpperCase());

                    Object[] messageArguments2 = {colourBuilder.toString()};
                    MessageFormat formatter2 = new MessageFormat(""); //$NON-NLS-1$
                    formatter2.setLocale(MyLanguage.getLocale());
                    formatter2.applyPattern(I18n.EDITORTEXT_COLOUR_2);

                    btnLastUsedColourPtr[0].setToolTipText(formatter2.format(messageArguments2));
                }
                btnLastUsedColourPtr[0].redraw();
            }
            regainFocus();
        });

        widgetUtil(btnDecolourPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null && !Project.getFileToEdit().isReadOnly()) {
                VertexManager vm = Project.getFileToEdit().getVertexManager();
                vm.addSnapshot();
                vm.selectAll(new SelectorSettings(), true);
                GDataCSG.clearSelection(Project.getFileToEdit());
                GColour c = LDConfig.getColour16();
                vm.colourChangeSelection(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), false);
                vm.getSelectedData().removeAll(vm.getTriangles().keySet());
                vm.getSelectedData().removeAll(vm.getQuads().keySet());
                vm.getSelectedData().removeAll(vm.getSelectedSubfiles());
                vm.getSelectedSubfiles().clear();
                vm.getSelectedTriangles().removeAll(vm.getTriangles().keySet());
                vm.getSelectedQuads().removeAll(vm.getQuads().keySet());
                c = LDConfig.getColour(24);
                vm.colourChangeSelection(c.getColourNumber(), c.getR(), c.getG(), c.getB(), c.getA(), true);
            }
        });

        ColourToolItem.initPaletteEvent();
    }

    private static GColour pickRandomColourFromSelection(VertexManager vm) {
        final GColour result;
        GColour selectedColour = vm.getRandomSelectedColour(getLastUsedColour());
        if (selectedColour.getColourNumber() == 16) {
            result = LDConfig.getColour16();
        } else {
            result = selectedColour;
        }
        setLastUsedColour(result);
        return result;
    }

    private static void regainFocus() {
        Editor3DWindow.getWindow().regainFocus();
    }
}

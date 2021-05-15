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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.composite.ToolSeparator;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.dialog.colour.ColourDialog;
import org.nschmidt.ldparteditor.enumtype.IconSize;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public class ColourToolItem extends ToolItem {

    private static final String ICON16_HALFTRANS_PNG = "icon16_halftrans.png"; //$NON-NLS-1$
    private static final String ICON16_RANDOM_COLOURS_PNG = "icon16_randomColours.png"; //$NON-NLS-1$
    private static final String ICON16_TRANSPARENT_PNG = "icon16_transparent.png"; //$NON-NLS-1$

    private static final NButton[] btnPalettePtr = new NButton[1];

    private static ToolItem toolItemColourBar;

    public ColourToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createWidgets(this);
    }

    public static GColour getLastUsedColour() {
        return ColourFunctionsToolItem.getLastUsedColour();
    }

    public static void setLastUsedColour(GColour lastUsedColour) {
        ColourFunctionsToolItem.setLastUsedColour(lastUsedColour);
    }

    public static void setLastUsedColour2(GColour lastUsedColour) {
        final int imgSize = IconSize.getImageSizeFromIconSize();
        final GColour[] gColour2 = new GColour[] { lastUsedColour };
        int num = gColour2[0].getColourNumber();
        if (LDConfig.hasColour(num)) {
            gColour2[0] = LDConfig.getColour(num);
        } else {
            num = -1;
        }
        setLastUsedColour(gColour2[0]);
        getLastUsedColourBtn().clearPaintListeners();
        getLastUsedColourBtn().clearSelectionListeners();
        final Color col = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
        final Point size = getLastUsedColourBtn().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        final int x = size.x / 4;
        final int y = size.y / 4;
        final int w = size.x / 2;
        final int h = size.y / 2;
        getLastUsedColourBtn().addPaintListener(e -> {
            e.gc.setBackground(col);
            e.gc.fillRectangle(x, y, w, h);
            if (gColour2[0].getA() >= .99f) {
                e.gc.drawImage(ResourceManager.getImage(ICON16_TRANSPARENT_PNG), 0, 0, imgSize, imgSize, x, y, w, h);
            } else if (gColour2[0].getA() == 0f) {
                e.gc.drawImage(ResourceManager.getImage(ICON16_RANDOM_COLOURS_PNG), 0, 0, imgSize, imgSize, x, y, w, h);
            } else {
                e.gc.drawImage(ResourceManager.getImage(ICON16_HALFTRANS_PNG), 0, 0, imgSize, imgSize, x, y, w, h);
            }
        });
        widgetUtil(getLastUsedColourBtn()).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                setLastUsedColour(gColour2[0]);
                int num1 = gColour2[0].getColourNumber();
                if (!LDConfig.hasColour(num1)) {
                    num1 = -1;
                }
                Project.getFileToEdit().getVertexManager().colourChangeSelection(num1, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA(), true);
            }
        });
        if (num != -1) {

            Object[] messageArguments = {num, LDConfig.getColourName(num)};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.EDITORTEXT_COLOUR_1);

            getLastUsedColourBtn().setToolTipText(formatter.format(messageArguments));
        } else {
            StringBuilder colourBuilder = new StringBuilder();
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

            Object[] messageArguments = {colourBuilder.toString()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.EDITORTEXT_COLOUR_2);

            getLastUsedColourBtn().setToolTipText(formatter.format(messageArguments));
            if (gColour2[0].getA() == 0f) getLastUsedColourBtn().setToolTipText(I18n.COLOURDIALOG_RANDOM_COLOURS);
        }
        getLastUsedColourBtn().redraw();
    }

    public static void reloadAllColours() {
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            w.reloadColours();
        }

        reloadColours();
    }

    private static void createWidgets(ColourToolItem colourToolItem) {
        toolItemColourBar = colourToolItem;
        List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();

        final int size = colours.size();
        for (int i = 0; i < size; i++) {
            addColorButton(colourToolItem, colours.get(i), i);
        }

        NButton btnPalette = new NButton(colourToolItem, Cocoa.getStyle());
        btnPalettePtr[0] = btnPalette;
        btnPalette.setToolTipText(I18n.E3D_MORE);
        btnPalette.setImage(ResourceManager.getImage("icon16_colours.png")); //$NON-NLS-1$
    }

    private static void reloadColours() {
        for (Control ctrl : toolItemColourBar.getChildren()) {
            if (!(ctrl instanceof ToolSeparator)) ctrl.dispose();
        }

        List<GColour> colours = WorkbenchManager.getUserSettingState().getUserPalette();

        final int size = colours.size();
        for (int i = 0; i < size; i++) {
            addColorButton(toolItemColourBar, colours.get(i), i);
        }


        NButton btnPalette = new NButton(toolItemColourBar, SWT.NONE);
        btnPalettePtr[0] = btnPalette;
        btnPalette.setToolTipText(I18n.E3D_MORE);
        btnPalette.setImage(ResourceManager.getImage("icon16_colours.png")); //$NON-NLS-1$
        initPaletteEvent();

        toolItemColourBar.getParent().layout();
        toolItemColourBar.layout();
        toolItemColourBar.redraw();
    }

    static void initPaletteEvent() {
        widgetUtil(btnPalettePtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                final GColour[] gColour2 = new GColour[1];
                new ColourDialog(Editor3DWindow.getWindow().getShell(), gColour2, true).run();
                if (gColour2[0] != null) {
                    setLastUsedColour(gColour2[0]);
                    int num = gColour2[0].getColourNumber();
                    if (!LDConfig.hasColour(num)) {
                        num = -1;
                    }
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().colourChangeSelection(num, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA(), true);

                    getLastUsedColourBtn().clearPaintListeners();
                    getLastUsedColourBtn().clearSelectionListeners();
                    final Color col = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
                    final Point size = getLastUsedColourBtn().computeSize(SWT.DEFAULT, SWT.DEFAULT);
                    final int x = Math.round(size.x / 5f);
                    final int y = Math.round(size.y / 5f);
                    final int w = Math.round(size.x * (3f / 5f));
                    final int h = Math.round(size.y * (3f / 5f));
                    final int imgSize = IconSize.getImageSizeFromIconSize();
                    getLastUsedColourBtn().addPaintListener(e1 -> {
                        e1.gc.setBackground(col);
                        e1.gc.fillRectangle(x, y, w, h);
                        if (gColour2[0].getA() >= .99f) {
                            e1.gc.drawImage(ResourceManager.getImage(ICON16_TRANSPARENT_PNG), 0, 0, imgSize, imgSize, x, y, w, h);
                        } else if (gColour2[0].getA() == 0f) {
                            e1.gc.drawImage(ResourceManager.getImage(ICON16_RANDOM_COLOURS_PNG), 0, 0, imgSize, imgSize, x, y, w, h);
                        } else {
                            e1.gc.drawImage(ResourceManager.getImage(ICON16_HALFTRANS_PNG), 0, 0, imgSize, imgSize, x, y, w, h);
                        }
                    });
                    widgetUtil(getLastUsedColourBtn()).addSelectionListener(e1 -> {
                        if (Project.getFileToEdit() != null) {
                            int num1 = gColour2[0].getColourNumber();
                            if (!LDConfig.hasColour(num1)) {
                                num1 = -1;
                            }
                            Project.getFileToEdit().getVertexManager().addSnapshot();
                            Project.getFileToEdit().getVertexManager().colourChangeSelection(num1, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA(), true);
                        }
                        regainFocus();
                    });
                    if (num != -1) {

                        Object[] messageArguments1 = {num, LDConfig.getColourName(num)};
                        MessageFormat formatter1 = new MessageFormat(""); //$NON-NLS-1$
                        formatter1.setLocale(MyLanguage.getLocale());
                        formatter1.applyPattern(I18n.EDITORTEXT_COLOUR_1);

                        getLastUsedColourBtn().setToolTipText(formatter1.format(messageArguments1));
                    } else {
                        StringBuilder colourBuilder = new StringBuilder();
                        colourBuilder.append("0x2"); //$NON-NLS-1$
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
                        colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

                        Object[] messageArguments2 = {colourBuilder.toString()};
                        MessageFormat formatter2 = new MessageFormat(""); //$NON-NLS-1$
                        formatter2.setLocale(MyLanguage.getLocale());
                        formatter2.applyPattern(I18n.EDITORTEXT_COLOUR_2);

                        getLastUsedColourBtn().setToolTipText(formatter2.format(messageArguments2));
                        if (gColour2[0].getA() == 0f) getLastUsedColourBtn().setToolTipText(I18n.COLOURDIALOG_RANDOM_COLOURS);
                    }
                    getLastUsedColourBtn().redraw();
                }
            }
            regainFocus();
        });
    }

    private static void addColorButton(ToolItem toolItemColours, GColour gColour, final int index) {
        int cn = gColour.getColourNumber();
        if (cn != -1 && LDConfig.hasColour(cn)) {
            gColour = LDConfig.getColour(cn);
        }

        final int imgSize = IconSize.getImageSizeFromIconSize();
        final GColour[] gColour2 = new GColour[] { gColour };
        final Color[] col = new Color[1];
        col[0] = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));

        final NButton btnCol = new NButton(toolItemColours, Cocoa.getStyle());
        btnCol.setData(gColour2);
        int num = gColour2[0].getColourNumber();
        if (!LDConfig.hasColour(num)) {
            num = -1;
        }
        if (num != -1) {

            Object[] messageArguments = {num, LDConfig.getColourName(num)};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.EDITORTEXT_COLOUR_1 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));

            btnCol.setToolTipText(formatter.format(messageArguments));
        } else {
            StringBuilder colourBuilder = new StringBuilder();
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

            Object[] messageArguments = {colourBuilder.toString()};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.EDITORTEXT_COLOUR_2 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));

            btnCol.setToolTipText(formatter.format(messageArguments));
        }

        btnCol.setImage(ResourceManager.getImage("icon16_fullTransparent.png")); //$NON-NLS-1$

        widgetUtil(btnCol).addSelectionListener(e -> {
            if (Cocoa.checkCtrlOrCmdPressed(e.stateMask)) {
                // Choose new colour
                new ColourDialog(Editor3DWindow.getWindow().getShell(), gColour2, false).run();
                WorkbenchManager.getUserSettingState().getUserPalette().set(index, gColour2[0]);
                col[0] = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
                int num2 = gColour2[0].getColourNumber();
                if (LDConfig.hasColour(num2)) {
                    gColour2[0] = LDConfig.getColour(num2);
                } else {
                    num2 = -1;
                }
                if (num2 != -1) {

                    Object[] messageArguments1 = {num2, LDConfig.getColourName(num2)};
                    MessageFormat formatter1 = new MessageFormat(""); //$NON-NLS-1$
                    formatter1.setLocale(MyLanguage.getLocale());
                    formatter1.applyPattern(I18n.EDITORTEXT_COLOUR_1 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));

                    btnCol.setToolTipText(formatter1.format(messageArguments1));
                } else {
                    StringBuilder colourBuilder1 = new StringBuilder();
                    colourBuilder1.append("0x2"); //$NON-NLS-1$
                    colourBuilder1.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
                    colourBuilder1.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
                    colourBuilder1.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

                    Object[] messageArguments2 = {colourBuilder1.toString()};
                    MessageFormat formatter2 = new MessageFormat(""); //$NON-NLS-1$
                    formatter2.setLocale(MyLanguage.getLocale());
                    formatter2.applyPattern(I18n.EDITORTEXT_COLOUR_2 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));

                    btnCol.setToolTipText(formatter2.format(messageArguments2));
                }
                reloadAllColours();
            } else {
                int num3 = gColour2[0].getColourNumber();
                if (LDConfig.hasColour(num3)) {
                    gColour2[0] = LDConfig.getColour(num3);
                } else {
                    num3 = -1;
                }
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().getVertexManager().colourChangeSelection(num3, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA(), true);
                }
                setLastUsedColour(gColour2[0]);
                getLastUsedColourBtn().clearPaintListeners();
                getLastUsedColourBtn().clearSelectionListeners();
                final Color col1 = SWTResourceManager.getColor((int) (gColour2[0].getR() * 255f), (int) (gColour2[0].getG() * 255f), (int) (gColour2[0].getB() * 255f));
                final Point size = getLastUsedColourBtn().computeSize(SWT.DEFAULT, SWT.DEFAULT);
                final int x = Math.round(size.x / 5f);
                final int y = Math.round(size.y / 5f);
                final int w = Math.round(size.x * (3f / 5f));
                final int h = Math.round(size.y * (3f / 5f));
                getLastUsedColourBtn().addPaintListener(e1 -> {
                    e1.gc.setBackground(col1);
                    e1.gc.fillRectangle(x, y, w, h);
                    if (gColour2[0].getA() == 1f) {
                        e1.gc.drawImage(ResourceManager.getImage(ICON16_TRANSPARENT_PNG), 0, 0, imgSize, imgSize, x, y, w, h);
                    } else {
                        e1.gc.drawImage(ResourceManager.getImage(ICON16_HALFTRANS_PNG), 0, 0, imgSize, imgSize, x, y, w, h);
                    }
                });
                widgetUtil(getLastUsedColourBtn()).addSelectionListener(e1 -> {
                    if (Project.getFileToEdit() != null) {
                        setLastUsedColour(gColour2[0]);
                        int num1 = gColour2[0].getColourNumber();
                        if (LDConfig.hasColour(num1)) {
                            gColour2[0] = LDConfig.getColour(num1);
                        } else {
                            num1 = -1;
                        }
                        Project.getFileToEdit().getVertexManager().addSnapshot();
                        Project.getFileToEdit().getVertexManager().colourChangeSelection(num1, gColour2[0].getR(), gColour2[0].getG(), gColour2[0].getB(), gColour2[0].getA(), true);
                    }
                    Editor3DWindow.getWindow().regainFocus();
                });
                if (num3 != -1) {
                    Object[] messageArguments3 = {num3, LDConfig.getColourName(num3)};
                    MessageFormat formatter3 = new MessageFormat(""); //$NON-NLS-1$
                    formatter3.setLocale(MyLanguage.getLocale());
                    formatter3.applyPattern(I18n.EDITORTEXT_COLOUR_1);

                    getLastUsedColourBtn().setToolTipText(formatter3.format(messageArguments3));
                } else {
                    StringBuilder colourBuilder2 = new StringBuilder();
                    colourBuilder2.append("0x2"); //$NON-NLS-1$
                    colourBuilder2.append(MathHelper.toHex((int) (255f * gColour2[0].getR())).toUpperCase());
                    colourBuilder2.append(MathHelper.toHex((int) (255f * gColour2[0].getG())).toUpperCase());
                    colourBuilder2.append(MathHelper.toHex((int) (255f * gColour2[0].getB())).toUpperCase());

                    Object[] messageArguments4 = {colourBuilder2.toString()};
                    MessageFormat formatter4 = new MessageFormat(""); //$NON-NLS-1$
                    formatter4.setLocale(MyLanguage.getLocale());
                    formatter4.applyPattern(I18n.EDITORTEXT_COLOUR_2);

                    getLastUsedColourBtn().setToolTipText(formatter4.format(messageArguments4));
                }
                getLastUsedColourBtn().redraw();
            }
            Editor3DWindow.getWindow().regainFocus();
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
                e.gc.drawImage(ResourceManager.getImage(ICON16_TRANSPARENT_PNG), 0, 0, imgSize, imgSize, x, y, w, h);
            } else {
                e.gc.drawImage(ResourceManager.getImage(ICON16_HALFTRANS_PNG), 0, 0, imgSize, imgSize, x, y, w, h);
            }
        });
    }

    private static NButton getLastUsedColourBtn() {
        return ColourFunctionsToolItem.getLastUsedColourBtn();
    }

    private static void regainFocus() {
        Editor3DWindow.getWindow().regainFocus();
    }
}

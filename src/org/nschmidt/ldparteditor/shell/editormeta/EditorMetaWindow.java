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
package org.nschmidt.ldparteditor.shell.editormeta;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.helper.Version;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.main.LDPartEditor;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.InsertAtCursorPositionToolItem;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * The meta editor window
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic.
 *
 * @author nils
 *
 */
public class EditorMetaWindow extends EditorMetaDesign {

    private boolean opened = false;

    /**
     * Create the application window.
     */
    public EditorMetaWindow() {
        super();
    }

    /**
     * Run a fresh instance of this window
     */
    public void run() {
        setOpened(true);
        // Creating the window to get the shell
        this.create();
        final Shell sh = this.getShell();
        sh.setText(Version.getApplicationName() + " " + Version.getVersion()); //$NON-NLS-1$
        sh.setImage(ResourceManager.getImage("imgDuke2.png")); //$NON-NLS-1$
        sh.setMinimumSize(640, 480);

        // MARK All final listeners will be configured here..

        sh.addShellListener(new ShellAdapter() {
            @Override
            public void shellActivated(ShellEvent e) {
                final UserSettingState userSettings = WorkbenchManager.getUserSettingState();
                if (!evAuthorRealNameTxtPtr[0].getText().equals(userSettings.getRealUserName())) {
                    evAuthorRealNameTxtPtr[0].setText(userSettings.getRealUserName());
                }
                if (!evAuthorUserNameTxtPtr[0].getText().equals(userSettings.getLdrawUserName())) {
                    evAuthorUserNameTxtPtr[0].setText(userSettings.getLdrawUserName());
                }
            }
        });

        widgetUtil(btnCreatePtr[0]).addSelectionListener(e -> {
            String textToCompile = lblLineToInsertPtr[0].getText();
            final DatFile df = Project.getFileToEdit();
            if (df != null) {
                final boolean insertAtCursor = InsertAtCursorPositionToolItem.isInsertingAtCursorPosition();
                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                    if (InsertAtCursorPositionToolItem.isInsertingAtCursorPosition()) {
                        break;
                    }
                    for (CTabItem t : w.getTabFolder().getItems()) {
                        if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                            InsertAtCursorPositionToolItem.setInsertingAtCursorPosition(true);
                            break;
                        }
                    }
                }
                df.getVertexManager().addParsedLine(textToCompile);
                InsertAtCursorPositionToolItem.setInsertingAtCursorPosition(insertAtCursor);
            }
        });

        widgetUtil(evDescriptionBtnPtr[0]).addSelectionListener(e -> updateDescription());
        evDescriptionTxtPtr[0].addModifyListener(e -> updateDescription());
        evDescriptionTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateDescription();
            }
        });

        evNameTxtPtr[0].addModifyListener(e -> updateName());
        evNameTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateName();
            }
        });

        evAuthorRealNameTxtPtr[0].addModifyListener(e -> updateAuthor());
        evAuthorRealNameTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateAuthor();
            }
        });
        evAuthorUserNameTxtPtr[0].addModifyListener(e -> updateAuthor());
        evAuthorUserNameTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateAuthor();
            }
        });

        evTypeUpdateTxtPtr[0].addModifyListener(e -> updateType());
        evTypeUpdateTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateType();
            }
        });
        evTypeTypeCmbPtr[0].addModifyListener(e -> updateType());
        evTypeTypeCmbPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateType();
            }
        });
        widgetUtil(evTypeUnofficialBtnPtr[0]).addSelectionListener(e -> updateType());
        widgetUtil(evTypeUpdateBtnPtr[0]).addSelectionListener(e -> updateType());

        evLicenseCmbPtr[0].addModifyListener(e -> updateLicense());
        evLicenseCmbPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateLicense();
            }
        });

        evHelpTxtPtr[0].addModifyListener(e -> updateHelp());
        evHelpTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHelp();
            }
        });

        evBfcHeaderCmbPtr[0].addModifyListener(e -> updateBfcHeader());
        evBfcHeaderCmbPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateBfcHeader();
            }
        });

        evCategoryCmbPtr[0].addModifyListener(e -> updateCategory());
        evCategoryCmbPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCategory();
            }
        });

        evKeywordsTxtPtr[0].addModifyListener(e -> updateKeywords());
        evKeywordsTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateKeywords();
            }
        });

        evCmdlineTxtPtr[0].addModifyListener(e -> updateCmdline());
        evCmdlineTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCmdline();
            }
        });

        evHistory11TxtPtr[0].addModifyListener(e -> updateHistory1());
        evHistory11TxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHistory1();
            }
        });
        evHistory12TxtPtr[0].addModifyListener(e -> updateHistory1());
        evHistory12TxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHistory1();
            }
        });
        evHistory13TxtPtr[0].addModifyListener(e -> updateHistory1());
        evHistory13TxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHistory1();
            }
        });
        evHistory21TxtPtr[0].addModifyListener(e -> updateHistory2());
        evHistory21TxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHistory2();
            }
        });
        evHistory22TxtPtr[0].addModifyListener(e -> updateHistory2());
        evHistory22TxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHistory2();
            }
        });
        evHistory23TxtPtr[0].addModifyListener(e -> updateHistory2());
        evHistory23TxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHistory2();
            }
        });

        evCommentTxtPtr[0].addModifyListener(e -> updateComment());
        evCommentTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateComment();
            }
        });
        widgetUtil(evCommentBtnPtr[0]).addSelectionListener(e -> updateComment());

        evBfcCmbPtr[0].addModifyListener(e -> updateBfc());
        evBfcCmbPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateBfc();
            }
        });

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateTexmapPlanar();
                }
            };
            final ModifyListener m = e -> updateTexmapPlanar();
            evTexmapPlanarCmbPtr[0].addFocusListener(a);
            evTexmapPlanarCmbPtr[0].addModifyListener(m);
            evTexmapPlanar1TxtPtr[0].addFocusListener(a);
            evTexmapPlanar1TxtPtr[0].addModifyListener(m);
            evTexmapPlanar2TxtPtr[0].addFocusListener(a);
            evTexmapPlanar2TxtPtr[0].addModifyListener(m);
            evTexmapPlanar3TxtPtr[0].addFocusListener(a);
            evTexmapPlanar3TxtPtr[0].addModifyListener(m);
            evTexmapPlanar4TxtPtr[0].addFocusListener(a);
            evTexmapPlanar4TxtPtr[0].addModifyListener(m);
            evTexmapPlanar5TxtPtr[0].addFocusListener(a);
            evTexmapPlanar5TxtPtr[0].addModifyListener(m);
            evTexmapPlanar6TxtPtr[0].addFocusListener(a);
            evTexmapPlanar6TxtPtr[0].addModifyListener(m);
            evTexmapPlanar7TxtPtr[0].addFocusListener(a);
            evTexmapPlanar7TxtPtr[0].addModifyListener(m);
            evTexmapPlanar8TxtPtr[0].addFocusListener(a);
            evTexmapPlanar8TxtPtr[0].addModifyListener(m);
            evTexmapPlanar9TxtPtr[0].addFocusListener(a);
            evTexmapPlanar9TxtPtr[0].addModifyListener(m);
            evTexmapPlanar10TxtPtr[0].addFocusListener(a);
            evTexmapPlanar10TxtPtr[0].addModifyListener(m);

            widgetUtil(evTexmapPlanarBtnPtr[0]).addSelectionListener(e -> {

                FileDialog fd = new FileDialog(sh, SWT.OPEN);
                fd.setText(I18n.META_CHOOSE_PNG);

                if (Project.DEFAULT_PROJECT_PATH.equals(Project.getProjectPath()) && Project.DEFAULT_PROJECT_PATH.equals(Project.LD_PART_EDITOR_PROJECT)) {
                    try {
                        String path = LDPartEditor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8.toString());
                        decodedPath = decodedPath.substring(0, decodedPath.length() - 4);
                        fd.setFilterPath(decodedPath + Project.DEFAULT_PROJECT_PATH);
                    } catch (Exception consumed) {
                        fd.setFilterPath(Project.getProjectPath());
                    }
                } else {
                    fd.setFilterPath(Project.getProjectPath());
                }

                String[] filterExt = { "*.png", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                fd.setFilterExtensions(filterExt);
                String[] filterNames = { I18n.E3D_PORTABLE_NETWORK_GRAPHICS, I18n.E3D_ALL_FILES };
                fd.setFilterNames(filterNames);
                String selected = fd.open();
                if (selected != null) {
                    evTexmapPlanar10TxtPtr[0].setText(selected);
                }
                updateTexmapPlanar();
            });

        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateTexmapCylindrical();
                }
            };
            final ModifyListener m = e -> updateTexmapCylindrical();
            evTexmapCyliCmbPtr[0].addFocusListener(a);
            evTexmapCyliCmbPtr[0].addModifyListener(m);
            evTexmapCyli1TxtPtr[0].addFocusListener(a);
            evTexmapCyli1TxtPtr[0].addModifyListener(m);
            evTexmapCyli2TxtPtr[0].addFocusListener(a);
            evTexmapCyli2TxtPtr[0].addModifyListener(m);
            evTexmapCyli3TxtPtr[0].addFocusListener(a);
            evTexmapCyli3TxtPtr[0].addModifyListener(m);
            evTexmapCyli4TxtPtr[0].addFocusListener(a);
            evTexmapCyli4TxtPtr[0].addModifyListener(m);
            evTexmapCyli5TxtPtr[0].addFocusListener(a);
            evTexmapCyli5TxtPtr[0].addModifyListener(m);
            evTexmapCyli6TxtPtr[0].addFocusListener(a);
            evTexmapCyli6TxtPtr[0].addModifyListener(m);
            evTexmapCyli7TxtPtr[0].addFocusListener(a);
            evTexmapCyli7TxtPtr[0].addModifyListener(m);
            evTexmapCyli8TxtPtr[0].addFocusListener(a);
            evTexmapCyli8TxtPtr[0].addModifyListener(m);
            evTexmapCyli9TxtPtr[0].addFocusListener(a);
            evTexmapCyli9TxtPtr[0].addModifyListener(m);
            evTexmapCyli10TxtPtr[0].addFocusListener(a);
            evTexmapCyli10TxtPtr[0].addModifyListener(m);
            evTexmapCyli11TxtPtr[0].addFocusListener(a);
            evTexmapCyli11TxtPtr[0].addModifyListener(m);

            widgetUtil(evTexmapCyliBtnPtr[0]).addSelectionListener(e -> {

                FileDialog fd = new FileDialog(sh, SWT.OPEN);
                fd.setText(I18n.META_CHOOSE_PNG);

                if (Project.DEFAULT_PROJECT_PATH.equals(Project.getProjectPath()) && Project.DEFAULT_PROJECT_PATH.equals(Project.LD_PART_EDITOR_PROJECT)) {
                    try {
                        String path = LDPartEditor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8.toString());
                        decodedPath = decodedPath.substring(0, decodedPath.length() - 4);
                        fd.setFilterPath(decodedPath + Project.DEFAULT_PROJECT_PATH);
                    } catch (Exception consumed) {
                        fd.setFilterPath(Project.getProjectPath());
                    }
                } else {
                    fd.setFilterPath(Project.getProjectPath());
                }

                String[] filterExt = { "*.png", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                fd.setFilterExtensions(filterExt);
                String[] filterNames = { I18n.E3D_PORTABLE_NETWORK_GRAPHICS, I18n.E3D_ALL_FILES };
                fd.setFilterNames(filterNames);
                String selected = fd.open();
                if (selected != null) {
                    evTexmapCyli11TxtPtr[0].setText(selected);
                }
                updateTexmapCylindrical();
            });

        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateTexmapSpherical();
                }
            };
            final ModifyListener m = e -> updateTexmapSpherical();
            evTexmapSphereCmbPtr[0].addFocusListener(a);
            evTexmapSphereCmbPtr[0].addModifyListener(m);
            evTexmapSphere1TxtPtr[0].addFocusListener(a);
            evTexmapSphere1TxtPtr[0].addModifyListener(m);
            evTexmapSphere2TxtPtr[0].addFocusListener(a);
            evTexmapSphere2TxtPtr[0].addModifyListener(m);
            evTexmapSphere3TxtPtr[0].addFocusListener(a);
            evTexmapSphere3TxtPtr[0].addModifyListener(m);
            evTexmapSphere4TxtPtr[0].addFocusListener(a);
            evTexmapSphere4TxtPtr[0].addModifyListener(m);
            evTexmapSphere5TxtPtr[0].addFocusListener(a);
            evTexmapSphere5TxtPtr[0].addModifyListener(m);
            evTexmapSphere6TxtPtr[0].addFocusListener(a);
            evTexmapSphere6TxtPtr[0].addModifyListener(m);
            evTexmapSphere7TxtPtr[0].addFocusListener(a);
            evTexmapSphere7TxtPtr[0].addModifyListener(m);
            evTexmapSphere8TxtPtr[0].addFocusListener(a);
            evTexmapSphere8TxtPtr[0].addModifyListener(m);
            evTexmapSphere9TxtPtr[0].addFocusListener(a);
            evTexmapSphere9TxtPtr[0].addModifyListener(m);
            evTexmapSphere10TxtPtr[0].addFocusListener(a);
            evTexmapSphere10TxtPtr[0].addModifyListener(m);
            evTexmapSphere11TxtPtr[0].addFocusListener(a);
            evTexmapSphere11TxtPtr[0].addModifyListener(m);
            evTexmapSphere12TxtPtr[0].addFocusListener(a);
            evTexmapSphere12TxtPtr[0].addModifyListener(m);

            widgetUtil(evTexmapSphereBtnPtr[0]).addSelectionListener(e -> {

                FileDialog fd = new FileDialog(sh, SWT.OPEN);
                fd.setText(I18n.META_CHOOSE_PNG);

                if (Project.DEFAULT_PROJECT_PATH.equals(Project.getProjectPath()) && Project.DEFAULT_PROJECT_PATH.equals(Project.LD_PART_EDITOR_PROJECT)) {
                    try {
                        String path = LDPartEditor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8.toString());
                        decodedPath = decodedPath.substring(0, decodedPath.length() - 4);
                        fd.setFilterPath(decodedPath + Project.DEFAULT_PROJECT_PATH);
                    } catch (Exception consumed) {
                        fd.setFilterPath(Project.getProjectPath());
                    }
                } else {
                    fd.setFilterPath(Project.getProjectPath());
                }

                String[] filterExt = { "*.png", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                fd.setFilterExtensions(filterExt);
                String[] filterNames = { I18n.E3D_PORTABLE_NETWORK_GRAPHICS, I18n.E3D_ALL_FILES };
                fd.setFilterNames(filterNames);
                String selected = fd.open();
                if (selected != null) {
                    evTexmapSphere12TxtPtr[0].setText(selected);
                }
                updateTexmapSpherical();
            });

        }

        widgetUtil(evTexmapFallbackBtnPtr[0]).addSelectionListener(e -> {
            lblLineToInsertPtr[0].setText("0 !TEXMAP FALLBACK"); //$NON-NLS-1$
            lblLineToInsertPtr[0].getParent().layout();
        });

        evTexmapMetaTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateTexmapMeta();
            }
        });

        evTexmapMetaTxtPtr[0].addModifyListener(e -> updateTexmapMeta());

        widgetUtil(evTexmapEndBtnPtr[0]).addSelectionListener(e -> {
            lblLineToInsertPtr[0].setText("0 !TEXMAP END"); //$NON-NLS-1$
            lblLineToInsertPtr[0].getParent().layout();
        });

        evTodoTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateLpeTODO();
            }
        });

        evTodoTxtPtr[0].addModifyListener(e -> updateLpeTODO());

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateLpeVertex();
                }
            };

            final ModifyListener m = e -> updateLpeVertex();

            evVertex1TxtPtr[0].addFocusListener(a);
            evVertex1TxtPtr[0].addModifyListener(m);
            evVertex2TxtPtr[0].addFocusListener(a);
            evVertex2TxtPtr[0].addModifyListener(m);
            evVertex3TxtPtr[0].addFocusListener(a);
            evVertex3TxtPtr[0].addModifyListener(m);
        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateCSGdef();
                }
            };

            final ModifyListener m = e -> updateCSGdef();

            evCsgBodyCmbPtr[0].addFocusListener(a);
            evCsgBodyCmbPtr[0].addModifyListener(m);
            evCsgBody1TxtPtr[0].addFocusListener(a);
            evCsgBody1TxtPtr[0].addModifyListener(m);
            evCsgBody2TxtPtr[0].addFocusListener(a);
            evCsgBody2TxtPtr[0].addModifyListener(m);
            evCsgBody3TxtPtr[0].addFocusListener(a);
            evCsgBody3TxtPtr[0].addModifyListener(m);
            evCsgBody4TxtPtr[0].addFocusListener(a);
            evCsgBody4TxtPtr[0].addModifyListener(m);
            evCsgBody5TxtPtr[0].addFocusListener(a);
            evCsgBody5TxtPtr[0].addModifyListener(m);
            evCsgBody6TxtPtr[0].addFocusListener(a);
            evCsgBody6TxtPtr[0].addModifyListener(m);
            evCsgBody7TxtPtr[0].addFocusListener(a);
            evCsgBody7TxtPtr[0].addModifyListener(m);
            evCsgBody8TxtPtr[0].addFocusListener(a);
            evCsgBody8TxtPtr[0].addModifyListener(m);
            evCsgBody9TxtPtr[0].addFocusListener(a);
            evCsgBody9TxtPtr[0].addModifyListener(m);
            evCsgBody10TxtPtr[0].addFocusListener(a);
            evCsgBody10TxtPtr[0].addModifyListener(m);
            evCsgBody11TxtPtr[0].addFocusListener(a);
            evCsgBody11TxtPtr[0].addModifyListener(m);
            evCsgBody12TxtPtr[0].addFocusListener(a);
            evCsgBody12TxtPtr[0].addModifyListener(m);
            evCsgBody13TxtPtr[0].addFocusListener(a);
            evCsgBody13TxtPtr[0].addModifyListener(m);
            evCsgBody14TxtPtr[0].addFocusListener(a);
            evCsgBody14TxtPtr[0].addModifyListener(m);

        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateCSGtrans();
                }
            };

            final ModifyListener m = e -> updateCSGtrans();

            evCsgTrans1TxtPtr[0].addFocusListener(a);
            evCsgTrans1TxtPtr[0].addModifyListener(m);
            evCsgTrans2TxtPtr[0].addFocusListener(a);
            evCsgTrans2TxtPtr[0].addModifyListener(m);
            evCsgTrans3TxtPtr[0].addFocusListener(a);
            evCsgTrans3TxtPtr[0].addModifyListener(m);
            evCsgTrans4TxtPtr[0].addFocusListener(a);
            evCsgTrans4TxtPtr[0].addModifyListener(m);
            evCsgTrans5TxtPtr[0].addFocusListener(a);
            evCsgTrans5TxtPtr[0].addModifyListener(m);
            evCsgTrans6TxtPtr[0].addFocusListener(a);
            evCsgTrans6TxtPtr[0].addModifyListener(m);
            evCsgTrans7TxtPtr[0].addFocusListener(a);
            evCsgTrans7TxtPtr[0].addModifyListener(m);
            evCsgTrans8TxtPtr[0].addFocusListener(a);
            evCsgTrans8TxtPtr[0].addModifyListener(m);
            evCsgTrans9TxtPtr[0].addFocusListener(a);
            evCsgTrans9TxtPtr[0].addModifyListener(m);
            evCsgTrans10TxtPtr[0].addFocusListener(a);
            evCsgTrans10TxtPtr[0].addModifyListener(m);
            evCsgTrans11TxtPtr[0].addFocusListener(a);
            evCsgTrans11TxtPtr[0].addModifyListener(m);
            evCsgTrans12TxtPtr[0].addFocusListener(a);
            evCsgTrans12TxtPtr[0].addModifyListener(m);
            evCsgTrans13TxtPtr[0].addFocusListener(a);
            evCsgTrans13TxtPtr[0].addModifyListener(m);
            evCsgTrans14TxtPtr[0].addFocusListener(a);
            evCsgTrans14TxtPtr[0].addModifyListener(m);
            evCsgTrans15TxtPtr[0].addFocusListener(a);
            evCsgTrans15TxtPtr[0].addModifyListener(m);

        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateCSGextrude();
                }
            };

            final ModifyListener m = e -> updateCSGextrude();

            evCsgEx1TxtPtr[0].addFocusListener(a);
            evCsgEx1TxtPtr[0].addModifyListener(m);
            evCsgEx2TxtPtr[0].addFocusListener(a);
            evCsgEx2TxtPtr[0].addModifyListener(m);
            evCsgEx3TxtPtr[0].addFocusListener(a);
            evCsgEx3TxtPtr[0].addModifyListener(m);
            evCsgEx4TxtPtr[0].addFocusListener(a);
            evCsgEx4TxtPtr[0].addModifyListener(m);
            evCsgEx5TxtPtr[0].addFocusListener(a);
            evCsgEx5TxtPtr[0].addModifyListener(m);
            evCsgEx6TxtPtr[0].addFocusListener(a);
            evCsgEx6TxtPtr[0].addModifyListener(m);
            evCsgEx7TxtPtr[0].addFocusListener(a);
            evCsgEx7TxtPtr[0].addModifyListener(m);
        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateCSGaction();
                }
            };

            final ModifyListener m = e -> updateCSGaction();

            evCsgActionCmbPtr[0].addFocusListener(a);
            evCsgActionCmbPtr[0].addModifyListener(m);
            evCsgAction1TxtPtr[0].addFocusListener(a);
            evCsgAction1TxtPtr[0].addModifyListener(m);
            evCsgAction2TxtPtr[0].addFocusListener(a);
            evCsgAction2TxtPtr[0].addModifyListener(m);
            evCsgAction3TxtPtr[0].addFocusListener(a);
            evCsgAction3TxtPtr[0].addModifyListener(m);

        }

        evCsgQualityTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCSGquality();
            }
        });

        evCsgQualityTxtPtr[0].addModifyListener(e -> updateCSGquality());

        evCsgEpsilonTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCSGepsilon();
            }
        });

        evCsgEpsilonTxtPtr[0].addModifyListener(e -> updateCSGepsilon());

        evCsgTJunctionEpsilonTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCSGtjunctionEpsilon();
            }
        });

        evCsgTJunctionEpsilonTxtPtr[0].addModifyListener(e -> updateCSGtjunctionEpsilon());

        evCsgEdgeCollapseEpsilonTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCSGedgeCollapseEpsilon();
            }
        });

        evCsgEdgeCollapseEpsilonTxtPtr[0].addModifyListener(e -> updateCSGedgeCollapseEpsilon());

        widgetUtil(evCsgDontOptimizeBtnPtr[0]).addSelectionListener(e -> updateCSGdontOptimize());

        evCsgCompileTxtPtr[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCSGcompile();
            }
        });

        evCsgCompileTxtPtr[0].addModifyListener(e -> updateCSGcompile());

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateCSGaction();
                }
            };

            final ModifyListener m = e -> updateCSGaction();

            evCsgActionCmbPtr[0].addFocusListener(a);
            evCsgActionCmbPtr[0].addModifyListener(m);
            evCsgAction1TxtPtr[0].addFocusListener(a);
            evCsgAction1TxtPtr[0].addModifyListener(m);
            evCsgAction2TxtPtr[0].addFocusListener(a);
            evCsgAction2TxtPtr[0].addModifyListener(m);
            evCsgAction3TxtPtr[0].addFocusListener(a);
            evCsgAction3TxtPtr[0].addModifyListener(m);

        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updatePNGdef();
                }
            };
            final ModifyListener m = e -> updatePNGdef();
            widgetUtil(evPngBtnPtr[0]).addSelectionListener(e -> updatePNGdef());
            evPng1TxtPtr[0].addFocusListener(a);
            evPng1TxtPtr[0].addModifyListener(m);
            evPng2TxtPtr[0].addFocusListener(a);
            evPng2TxtPtr[0].addModifyListener(m);
            evPng3TxtPtr[0].addFocusListener(a);
            evPng3TxtPtr[0].addModifyListener(m);
            evPng4TxtPtr[0].addFocusListener(a);
            evPng4TxtPtr[0].addModifyListener(m);
            evPng5TxtPtr[0].addFocusListener(a);
            evPng5TxtPtr[0].addModifyListener(m);
            evPng6TxtPtr[0].addFocusListener(a);
            evPng6TxtPtr[0].addModifyListener(m);
            evPng7TxtPtr[0].addFocusListener(a);
            evPng7TxtPtr[0].addModifyListener(m);
            evPng8TxtPtr[0].addFocusListener(a);
            evPng8TxtPtr[0].addModifyListener(m);
            evPng9TxtPtr[0].addFocusListener(a);
            evPng9TxtPtr[0].addModifyListener(m);

            widgetUtil(evPngBtnPtr[0]).addSelectionListener(e -> {

                FileDialog fd = new FileDialog(sh, SWT.OPEN);
                fd.setText(I18n.META_CHOOSE_PNG);

                if (Project.DEFAULT_PROJECT_PATH.equals(Project.getProjectPath()) && Project.DEFAULT_PROJECT_PATH.equals(Project.LD_PART_EDITOR_PROJECT)) {
                    try {
                        String path = LDPartEditor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8.toString());
                        decodedPath = decodedPath.substring(0, decodedPath.length() - 4);
                        fd.setFilterPath(decodedPath + Project.DEFAULT_PROJECT_PATH);
                    } catch (Exception consumed) {
                        fd.setFilterPath(Project.getProjectPath());
                    }
                } else {
                    fd.setFilterPath(Project.getProjectPath());
                }

                String[] filterExt = { "*.png", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
                fd.setFilterExtensions(filterExt);
                String[] filterNames = { I18n.E3D_PORTABLE_NETWORK_GRAPHICS, I18n.E3D_ALL_FILES };
                fd.setFilterNames(filterNames);
                String selected = fd.open();
                if (selected != null) {
                    evPng9TxtPtr[0].setText(selected);
                }
            });

        }

        this.open();
    }

    private void updatePNGdef() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !LPE PNG "); //$NON-NLS-1$
        sb.append(evPng1TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evPng2TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evPng3TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evPng4TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evPng5TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evPng6TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evPng7TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evPng8TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evPng9TxtPtr[0].getText().trim());
        lblLineToInsertPtr[0].setText(sb.toString());
        lblLineToInsertPtr[0].getParent().layout();
    }


    private void updateCSGcompile() {
        lblLineToInsertPtr[0].setText("0 !LPE CSG_COMPILE " + evCsgCompileTxtPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateCSGepsilon() {
        lblLineToInsertPtr[0].setText("0 !LPE CSG_EPSILON " + evCsgEpsilonTxtPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateCSGtjunctionEpsilon() {
        lblLineToInsertPtr[0].setText("0 !LPE CSG_TJUNCTION_EPSILON " + evCsgTJunctionEpsilonTxtPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateCSGedgeCollapseEpsilon() {
        lblLineToInsertPtr[0].setText("0 !LPE CSG_EDGE_COLLAPSE_EPSILON " + evCsgEdgeCollapseEpsilonTxtPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateCSGquality() {
        lblLineToInsertPtr[0].setText("0 !LPE CSG_QUALITY " + evCsgQualityTxtPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateCSGaction() {
        lblLineToInsertPtr[0].setText("0 !LPE CSG_" + evCsgActionCmbPtr[0].getText().trim() + " " + evCsgAction1TxtPtr[0].getText().trim() + " " + evCsgAction2TxtPtr[0].getText().trim() + " " + evCsgAction3TxtPtr[0].getText().trim()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateCSGdontOptimize() {
        lblLineToInsertPtr[0].setText("0 !LPE CSG_DONT_OPTIMISE"); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateCSGdef() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !LPE CSG_"); //$NON-NLS-1$
        sb.append(evCsgBodyCmbPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody1TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody2TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody3TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody4TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody5TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody6TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody7TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody8TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody9TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody10TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody11TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody12TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody13TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgBody14TxtPtr[0].getText().trim());
        lblLineToInsertPtr[0].setText(sb.toString());
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateCSGtrans() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !LPE CSG_TRANSFORM "); //$NON-NLS-1$
        sb.append(evCsgTrans1TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans2TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans3TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans4TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans5TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans6TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans7TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans8TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans9TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans10TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans11TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans12TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans13TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans14TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evCsgTrans15TxtPtr[0].getText().trim());
        lblLineToInsertPtr[0].setText(sb.toString());
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateCSGextrude() {
        // 0 !LPE CSG_EXT_CFG seg_len= 100000.0 no_of_tr= 1 curve= 10.0 center= 0.5 rot= 0.0 comp= false invert= false
        StringBuilder sb = new StringBuilder();
        String arg;
        sb.append("0 !LPE CSG_EXT_CFG "); //$NON-NLS-1$
        sb.append("seg_len= "); //$NON-NLS-1$
        if ((arg = evCsgEx1TxtPtr[0].getText().trim()).isEmpty()) {
            sb.append("100000.0"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        sb.append(" no_of_tr= "); //$NON-NLS-1$
        if ((arg = evCsgEx2TxtPtr[0].getText().trim()).isEmpty()) {
            sb.append("1"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        sb.append(" curve= "); //$NON-NLS-1$
        if ((arg = evCsgEx3TxtPtr[0].getText().trim()).isEmpty()) {
            sb.append("10.0"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        sb.append(" center= "); //$NON-NLS-1$
        if ((arg = evCsgEx4TxtPtr[0].getText().trim()).isEmpty()) {
            sb.append("0.5"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        sb.append(" rot= "); //$NON-NLS-1$
        if ((arg = evCsgEx5TxtPtr[0].getText().trim()).isEmpty()) {
            sb.append("0.0"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        sb.append(" comp= "); //$NON-NLS-1$
        if ((arg = evCsgEx6TxtPtr[0].getText().trim()).isEmpty()) {
            sb.append("false"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        sb.append(" invert= "); //$NON-NLS-1$
        if ((arg = evCsgEx7TxtPtr[0].getText().trim()).isEmpty()) {
            sb.append("false"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        lblLineToInsertPtr[0].setText(sb.toString());
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateLpeVertex() {
        lblLineToInsertPtr[0].setText("0 !LPE VERTEX " + evVertex1TxtPtr[0].getText().trim() + " " + evVertex2TxtPtr[0].getText().trim() + " " + evVertex3TxtPtr[0].getText().trim()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateLpeTODO() {
        lblLineToInsertPtr[0].setText("0 !LPE TODO " + evTodoTxtPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateTexmapPlanar() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !TEXMAP "); //$NON-NLS-1$
        sb.append(evTexmapPlanarCmbPtr[0].getText().trim());
        sb.append(" PLANAR "); //$NON-NLS-1$
        sb.append(evTexmapPlanar1TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapPlanar2TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapPlanar3TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapPlanar4TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapPlanar5TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapPlanar6TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapPlanar7TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapPlanar8TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapPlanar9TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapPlanar10TxtPtr[0].getText().trim());
        lblLineToInsertPtr[0].setText(sb.toString());
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateTexmapCylindrical() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !TEXMAP "); //$NON-NLS-1$
        sb.append(evTexmapCyliCmbPtr[0].getText().trim());
        sb.append(" CYLINDRICAL "); //$NON-NLS-1$
        sb.append(evTexmapCyli1TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapCyli2TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapCyli3TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapCyli4TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapCyli5TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapCyli6TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapCyli7TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapCyli8TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapCyli9TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapCyli10TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapCyli11TxtPtr[0].getText().trim());
        lblLineToInsertPtr[0].setText(sb.toString());
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateTexmapSpherical() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !TEXMAP "); //$NON-NLS-1$
        sb.append(evTexmapSphereCmbPtr[0].getText().trim());
        sb.append(" SPHERICAL "); //$NON-NLS-1$
        sb.append(evTexmapSphere1TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapSphere2TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapSphere3TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapSphere4TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapSphere5TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapSphere6TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapSphere7TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapSphere8TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapSphere9TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapSphere10TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapSphere11TxtPtr[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(evTexmapSphere12TxtPtr[0].getText().trim());
        lblLineToInsertPtr[0].setText(sb.toString());
        lblLineToInsertPtr[0].getParent().layout();
    }


    private void updateTexmapMeta() {
        lblLineToInsertPtr[0].setText("0 !: " + evTexmapMetaTxtPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateBfc() {
        lblLineToInsertPtr[0].setText("0 BFC " + evBfcCmbPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateComment() {
        lblLineToInsertPtr[0].setText("0 // " + (evCommentBtnPtr[0].getSelection() ? "Needs work: " : "") + evCommentTxtPtr[0].getText().trim()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateHistory1() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !HISTORY "); //$NON-NLS-1$
        sb.append(evHistory11TxtPtr[0].getText().trim());
        if (!evHistory12TxtPtr[0].getText().trim().equals("")) { //$NON-NLS-1$
            sb.append(" ["); //$NON-NLS-1$
            sb.append(evHistory12TxtPtr[0].getText().trim());
            sb.append("] "); //$NON-NLS-1$
        } else {
            sb.append(" "); //$NON-NLS-1$
        }
        sb.append(evHistory13TxtPtr[0].getText().trim());
        lblLineToInsertPtr[0].setText(sb.toString());
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateHistory2() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !HISTORY "); //$NON-NLS-1$
        sb.append(evHistory21TxtPtr[0].getText().trim());
        if (!evHistory22TxtPtr[0].getText().trim().equals("")) { //$NON-NLS-1$
            sb.append(" {"); //$NON-NLS-1$
            sb.append(evHistory22TxtPtr[0].getText().trim());
            sb.append("} "); //$NON-NLS-1$
        } else {
            sb.append(" "); //$NON-NLS-1$
        }
        sb.append(evHistory23TxtPtr[0].getText().trim());
        lblLineToInsertPtr[0].setText(sb.toString());
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateCmdline() {
        lblLineToInsertPtr[0].setText("0 !CMDLINE " + evCmdlineTxtPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateKeywords() {
        lblLineToInsertPtr[0].setText("0 !KEYWORDS " + evKeywordsTxtPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateCategory() {
        lblLineToInsertPtr[0].setText("0 !CATEGORY " + evCategoryCmbPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateBfcHeader() {
        lblLineToInsertPtr[0].setText("0 BFC " + evBfcHeaderCmbPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateHelp() {
        lblLineToInsertPtr[0].setText("0 !HELP " + evHelpTxtPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateLicense() {
        lblLineToInsertPtr[0].setText("0 !LICENSE " + evLicenseCmbPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateType() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !LDRAW_ORG "); //$NON-NLS-1$
        if (evTypeUnofficialBtnPtr[0].getSelection()) {
            sb.append("Unofficial_"); //$NON-NLS-1$
        }
        sb.append(evTypeTypeCmbPtr[0].getText().trim());
        if (evTypeUpdateBtnPtr[0].getSelection()) {
            evTypeUpdateTxtPtr[0].setEnabled(true);
            sb.append(" UPDATE "); //$NON-NLS-1$
            sb.append(evTypeUpdateTxtPtr[0].getText().trim());
        } else {
            evTypeUpdateTxtPtr[0].setEnabled(false);
        }
        lblLineToInsertPtr[0].setText(sb.toString());
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateAuthor() {
        lblLineToInsertPtr[0].setText("0 Author: " + evAuthorRealNameTxtPtr[0].getText().trim() + (evAuthorUserNameTxtPtr[0].getText().trim().equals("") ? "" : " [" + evAuthorUserNameTxtPtr[0].getText().trim() + "]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateName() {
        lblLineToInsertPtr[0].setText("0 Name: " + evNameTxtPtr[0].getText().trim()); //$NON-NLS-1$
        lblLineToInsertPtr[0].getParent().layout();
    }

    private void updateDescription() {
        lblLineToInsertPtr[0].setText("0 " + evDescriptionTxtPtr[0].getText().trim() + (evDescriptionBtnPtr[0].getSelection() ? " (Needs Work)" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        lblLineToInsertPtr[0].getParent().layout();
    }

    @Override
    protected void handleShellCloseEvent() {
        setOpened(false);
        super.handleShellCloseEvent();
    }

    public boolean isOpened() {
        return opened;
    }

    private void setOpened(boolean opened) {
        this.opened = opened;
    }
}

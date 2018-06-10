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
package org.nschmidt.ldparteditor.shells.editormeta;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import java.net.URLDecoder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.helpers.Version;
import org.nschmidt.ldparteditor.helpers.WidgetSelectionListener;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.main.LDPartEditor;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
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
                if (!ev_author_realName_txt[0].getText().equals(userSettings.getRealUserName())) {
                    ev_author_realName_txt[0].setText(userSettings.getRealUserName());
                }
                if (!ev_author_userName_txt[0].getText().equals(userSettings.getLdrawUserName())) {
                    ev_author_userName_txt[0].setText(userSettings.getLdrawUserName());
                }
            }
        });

        WidgetUtil(btn_Create[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String textToCompile = lbl_lineToInsert[0].getText();
                final DatFile df = Project.getFileToEdit();
                if (df != null) {
                    final boolean insertAtCursor = Editor3DWindow.getWindow().isInsertingAtCursorPosition();
                    for (EditorTextWindow w : Project.getOpenTextWindows()) {
                        if (Editor3DWindow.getWindow().isInsertingAtCursorPosition()) {
                            break;
                        }
                        for (CTabItem t : w.getTabFolder().getItems()) {
                            if (df.equals(((CompositeTab) t).getState().getFileNameObj())) {
                                Editor3DWindow.getWindow().setInsertingAtCursorPosition(true);
                                break;
                            }
                        }
                    }
                    df.getVertexManager().addParsedLine(textToCompile);
                    Editor3DWindow.getWindow().setInsertingAtCursorPosition(insertAtCursor);
                }
            }
        });

        WidgetUtil(ev_description_btn[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateDescription();
            }
        });
        ev_description_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateDescription();
            }
        });
        ev_description_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateDescription();
            }
        });

        ev_name_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateName();
            }
        });
        ev_name_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateName();
            }
        });

        ev_author_realName_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateAuthor();
            }
        });
        ev_author_realName_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateAuthor();
            }
        });
        ev_author_userName_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateAuthor();
            }
        });
        ev_author_userName_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateAuthor();
            }
        });

        ev_type_update_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateType();
            }
        });
        ev_type_update_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateType();
            }
        });
        ev_type_type_cmb[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateType();
            }
        });
        ev_type_type_cmb[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateType();
            }
        });
        WidgetUtil(ev_type_unofficial_btn[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateType();
            }
        });
        WidgetUtil(ev_type_update_btn[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateType();
            }
        });

        ev_license_cmb[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateLicense();
            }
        });
        ev_license_cmb[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateLicense();
            }
        });

        ev_help_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateHelp();
            }
        });
        ev_help_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHelp();
            }
        });

        ev_bfcHeader_cmb[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateBfcHeader();
            }
        });
        ev_bfcHeader_cmb[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateBfcHeader();
            }
        });

        ev_category_cmb[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateCategory();
            }
        });
        ev_category_cmb[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCategory();
            }
        });

        ev_keywords_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateKeywords();
            }
        });
        ev_keywords_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateKeywords();
            }
        });

        ev_cmdline_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateCmdline();
            }
        });
        ev_cmdline_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCmdline();
            }
        });

        ev_history11_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateHistory1();
            }
        });
        ev_history11_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHistory1();
            }
        });
        ev_history12_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateHistory1();
            }
        });
        ev_history12_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHistory1();
            }
        });
        ev_history13_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateHistory1();
            }
        });
        ev_history13_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHistory1();
            }
        });
        ev_history21_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateHistory2();
            }
        });
        ev_history21_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHistory2();
            }
        });
        ev_history22_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateHistory2();
            }
        });
        ev_history22_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHistory2();
            }
        });
        ev_history23_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateHistory2();
            }
        });
        ev_history23_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateHistory2();
            }
        });

        ev_comment_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateComment();
            }
        });
        ev_comment_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateComment();
            }
        });
        WidgetUtil(ev_comment_btn[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateComment();
            }
        });

        ev_bfc_cmb[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateBfc();
            }
        });
        ev_bfc_cmb[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
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
            final ModifyListener m = new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateTexmapPlanar();
                }
            };
            ev_texmapPlanar_cmb[0].addFocusListener(a);
            ev_texmapPlanar_cmb[0].addModifyListener(m);
            ev_texmapPlanar1_txt[0].addFocusListener(a);
            ev_texmapPlanar1_txt[0].addModifyListener(m);
            ev_texmapPlanar2_txt[0].addFocusListener(a);
            ev_texmapPlanar2_txt[0].addModifyListener(m);
            ev_texmapPlanar3_txt[0].addFocusListener(a);
            ev_texmapPlanar3_txt[0].addModifyListener(m);
            ev_texmapPlanar4_txt[0].addFocusListener(a);
            ev_texmapPlanar4_txt[0].addModifyListener(m);
            ev_texmapPlanar5_txt[0].addFocusListener(a);
            ev_texmapPlanar5_txt[0].addModifyListener(m);
            ev_texmapPlanar6_txt[0].addFocusListener(a);
            ev_texmapPlanar6_txt[0].addModifyListener(m);
            ev_texmapPlanar7_txt[0].addFocusListener(a);
            ev_texmapPlanar7_txt[0].addModifyListener(m);
            ev_texmapPlanar8_txt[0].addFocusListener(a);
            ev_texmapPlanar8_txt[0].addModifyListener(m);
            ev_texmapPlanar9_txt[0].addFocusListener(a);
            ev_texmapPlanar9_txt[0].addModifyListener(m);
            ev_texmapPlanar10_txt[0].addFocusListener(a);
            ev_texmapPlanar10_txt[0].addModifyListener(m);

            WidgetUtil(ev_texmapPlanar_btn[0]).addXSelectionListener(new WidgetSelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {

                    FileDialog fd = new FileDialog(sh, SWT.OPEN);
                    fd.setText(I18n.META_ChoosePng);

                    if (Project.DEFAULT_PROJECT_PATH.equals(Project.getProjectPath()) && Project.DEFAULT_PROJECT_PATH.equals(Project.PROJECT)) {
                        try {
                            String path = LDPartEditor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                            String decodedPath = URLDecoder.decode(path, "UTF-8"); //$NON-NLS-1$
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
                    String[] filterNames = { I18n.E3D_PortableNetworkGraphics, I18n.E3D_AllFiles };
                    fd.setFilterNames(filterNames);
                    String selected = fd.open();
                    if (selected != null) {
                        ev_texmapPlanar10_txt[0].setText(selected);
                    }
                    updateTexmapPlanar();
                }
            });

        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateTexmapCylindrical();
                }
            };
            final ModifyListener m = new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateTexmapCylindrical();
                }
            };
            ev_texmapCyli_cmb[0].addFocusListener(a);
            ev_texmapCyli_cmb[0].addModifyListener(m);
            ev_texmapCyli1_txt[0].addFocusListener(a);
            ev_texmapCyli1_txt[0].addModifyListener(m);
            ev_texmapCyli2_txt[0].addFocusListener(a);
            ev_texmapCyli2_txt[0].addModifyListener(m);
            ev_texmapCyli3_txt[0].addFocusListener(a);
            ev_texmapCyli3_txt[0].addModifyListener(m);
            ev_texmapCyli4_txt[0].addFocusListener(a);
            ev_texmapCyli4_txt[0].addModifyListener(m);
            ev_texmapCyli5_txt[0].addFocusListener(a);
            ev_texmapCyli5_txt[0].addModifyListener(m);
            ev_texmapCyli6_txt[0].addFocusListener(a);
            ev_texmapCyli6_txt[0].addModifyListener(m);
            ev_texmapCyli7_txt[0].addFocusListener(a);
            ev_texmapCyli7_txt[0].addModifyListener(m);
            ev_texmapCyli8_txt[0].addFocusListener(a);
            ev_texmapCyli8_txt[0].addModifyListener(m);
            ev_texmapCyli9_txt[0].addFocusListener(a);
            ev_texmapCyli9_txt[0].addModifyListener(m);
            ev_texmapCyli10_txt[0].addFocusListener(a);
            ev_texmapCyli10_txt[0].addModifyListener(m);
            ev_texmapCyli11_txt[0].addFocusListener(a);
            ev_texmapCyli11_txt[0].addModifyListener(m);

            WidgetUtil(ev_texmapCyli_btn[0]).addXSelectionListener(new WidgetSelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {

                    FileDialog fd = new FileDialog(sh, SWT.OPEN);
                    fd.setText(I18n.META_ChoosePng);

                    if (Project.DEFAULT_PROJECT_PATH.equals(Project.getProjectPath()) && Project.DEFAULT_PROJECT_PATH.equals(Project.PROJECT)) {
                        try {
                            String path = LDPartEditor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                            String decodedPath = URLDecoder.decode(path, "UTF-8"); //$NON-NLS-1$
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
                    String[] filterNames = { I18n.E3D_PortableNetworkGraphics, I18n.E3D_AllFiles };
                    fd.setFilterNames(filterNames);
                    String selected = fd.open();
                    if (selected != null) {
                        ev_texmapCyli11_txt[0].setText(selected);
                    }
                    updateTexmapCylindrical();
                }
            });

        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateTexmapSpherical();
                }
            };
            final ModifyListener m = new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateTexmapSpherical();
                }
            };
            ev_texmapSphere_cmb[0].addFocusListener(a);
            ev_texmapSphere_cmb[0].addModifyListener(m);
            ev_texmapSphere1_txt[0].addFocusListener(a);
            ev_texmapSphere1_txt[0].addModifyListener(m);
            ev_texmapSphere2_txt[0].addFocusListener(a);
            ev_texmapSphere2_txt[0].addModifyListener(m);
            ev_texmapSphere3_txt[0].addFocusListener(a);
            ev_texmapSphere3_txt[0].addModifyListener(m);
            ev_texmapSphere4_txt[0].addFocusListener(a);
            ev_texmapSphere4_txt[0].addModifyListener(m);
            ev_texmapSphere5_txt[0].addFocusListener(a);
            ev_texmapSphere5_txt[0].addModifyListener(m);
            ev_texmapSphere6_txt[0].addFocusListener(a);
            ev_texmapSphere6_txt[0].addModifyListener(m);
            ev_texmapSphere7_txt[0].addFocusListener(a);
            ev_texmapSphere7_txt[0].addModifyListener(m);
            ev_texmapSphere8_txt[0].addFocusListener(a);
            ev_texmapSphere8_txt[0].addModifyListener(m);
            ev_texmapSphere9_txt[0].addFocusListener(a);
            ev_texmapSphere9_txt[0].addModifyListener(m);
            ev_texmapSphere10_txt[0].addFocusListener(a);
            ev_texmapSphere10_txt[0].addModifyListener(m);
            ev_texmapSphere11_txt[0].addFocusListener(a);
            ev_texmapSphere11_txt[0].addModifyListener(m);
            ev_texmapSphere12_txt[0].addFocusListener(a);
            ev_texmapSphere12_txt[0].addModifyListener(m);

            WidgetUtil(ev_texmapSphere_btn[0]).addXSelectionListener(new WidgetSelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {

                    FileDialog fd = new FileDialog(sh, SWT.OPEN);
                    fd.setText(I18n.META_ChoosePng);

                    if (Project.DEFAULT_PROJECT_PATH.equals(Project.getProjectPath()) && Project.DEFAULT_PROJECT_PATH.equals(Project.PROJECT)) {
                        try {
                            String path = LDPartEditor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                            String decodedPath = URLDecoder.decode(path, "UTF-8"); //$NON-NLS-1$
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
                    String[] filterNames = { I18n.E3D_PortableNetworkGraphics, I18n.E3D_AllFiles };
                    fd.setFilterNames(filterNames);
                    String selected = fd.open();
                    if (selected != null) {
                        ev_texmapSphere12_txt[0].setText(selected);
                    }
                    updateTexmapSpherical();
                }
            });

        }

        WidgetUtil(ev_texmapFallback_btn[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                lbl_lineToInsert[0].setText("0 !TEXMAP FALLBACK"); //$NON-NLS-1$
                lbl_lineToInsert[0].getParent().layout();
            }
        });

        ev_texmapMeta_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateTexmapMeta();
            }
        });

        ev_texmapMeta_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateTexmapMeta();
            }
        });

        WidgetUtil(ev_texmapEnd_btn[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                lbl_lineToInsert[0].setText("0 !TEXMAP END"); //$NON-NLS-1$
                lbl_lineToInsert[0].getParent().layout();
            }
        });

        ev_todo_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateLpeTODO();
            }
        });

        ev_todo_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateLpeTODO();
            }
        });

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateLpeVertex();
                }
            };

            final ModifyListener m = new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateLpeVertex();
                }
            };

            ev_vertex1_txt[0].addFocusListener(a);
            ev_vertex1_txt[0].addModifyListener(m);
            ev_vertex2_txt[0].addFocusListener(a);
            ev_vertex2_txt[0].addModifyListener(m);
            ev_vertex3_txt[0].addFocusListener(a);
            ev_vertex3_txt[0].addModifyListener(m);
        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateCSGdef();
                }
            };

            final ModifyListener m = new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateCSGdef();
                }
            };

            ev_csgBody_cmb[0].addFocusListener(a);
            ev_csgBody_cmb[0].addModifyListener(m);
            ev_csgBody1_txt[0].addFocusListener(a);
            ev_csgBody1_txt[0].addModifyListener(m);
            ev_csgBody2_txt[0].addFocusListener(a);
            ev_csgBody2_txt[0].addModifyListener(m);
            ev_csgBody3_txt[0].addFocusListener(a);
            ev_csgBody3_txt[0].addModifyListener(m);
            ev_csgBody4_txt[0].addFocusListener(a);
            ev_csgBody4_txt[0].addModifyListener(m);
            ev_csgBody5_txt[0].addFocusListener(a);
            ev_csgBody5_txt[0].addModifyListener(m);
            ev_csgBody6_txt[0].addFocusListener(a);
            ev_csgBody6_txt[0].addModifyListener(m);
            ev_csgBody7_txt[0].addFocusListener(a);
            ev_csgBody7_txt[0].addModifyListener(m);
            ev_csgBody8_txt[0].addFocusListener(a);
            ev_csgBody8_txt[0].addModifyListener(m);
            ev_csgBody9_txt[0].addFocusListener(a);
            ev_csgBody9_txt[0].addModifyListener(m);
            ev_csgBody10_txt[0].addFocusListener(a);
            ev_csgBody10_txt[0].addModifyListener(m);
            ev_csgBody11_txt[0].addFocusListener(a);
            ev_csgBody11_txt[0].addModifyListener(m);
            ev_csgBody12_txt[0].addFocusListener(a);
            ev_csgBody12_txt[0].addModifyListener(m);
            ev_csgBody13_txt[0].addFocusListener(a);
            ev_csgBody13_txt[0].addModifyListener(m);
            ev_csgBody14_txt[0].addFocusListener(a);
            ev_csgBody14_txt[0].addModifyListener(m);

        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateCSGtrans();
                }
            };

            final ModifyListener m = new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateCSGtrans();
                }
            };

            ev_csgTrans1_txt[0].addFocusListener(a);
            ev_csgTrans1_txt[0].addModifyListener(m);
            ev_csgTrans2_txt[0].addFocusListener(a);
            ev_csgTrans2_txt[0].addModifyListener(m);
            ev_csgTrans3_txt[0].addFocusListener(a);
            ev_csgTrans3_txt[0].addModifyListener(m);
            ev_csgTrans4_txt[0].addFocusListener(a);
            ev_csgTrans4_txt[0].addModifyListener(m);
            ev_csgTrans5_txt[0].addFocusListener(a);
            ev_csgTrans5_txt[0].addModifyListener(m);
            ev_csgTrans6_txt[0].addFocusListener(a);
            ev_csgTrans6_txt[0].addModifyListener(m);
            ev_csgTrans7_txt[0].addFocusListener(a);
            ev_csgTrans7_txt[0].addModifyListener(m);
            ev_csgTrans8_txt[0].addFocusListener(a);
            ev_csgTrans8_txt[0].addModifyListener(m);
            ev_csgTrans9_txt[0].addFocusListener(a);
            ev_csgTrans9_txt[0].addModifyListener(m);
            ev_csgTrans10_txt[0].addFocusListener(a);
            ev_csgTrans10_txt[0].addModifyListener(m);
            ev_csgTrans11_txt[0].addFocusListener(a);
            ev_csgTrans11_txt[0].addModifyListener(m);
            ev_csgTrans12_txt[0].addFocusListener(a);
            ev_csgTrans12_txt[0].addModifyListener(m);
            ev_csgTrans13_txt[0].addFocusListener(a);
            ev_csgTrans13_txt[0].addModifyListener(m);
            ev_csgTrans14_txt[0].addFocusListener(a);
            ev_csgTrans14_txt[0].addModifyListener(m);
            ev_csgTrans15_txt[0].addFocusListener(a);
            ev_csgTrans15_txt[0].addModifyListener(m);

        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateCSGextrude();
                }
            };

            final ModifyListener m = new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateCSGextrude();
                }
            };

            ev_csgEx1_txt[0].addFocusListener(a);
            ev_csgEx1_txt[0].addModifyListener(m);
            ev_csgEx2_txt[0].addFocusListener(a);
            ev_csgEx2_txt[0].addModifyListener(m);
            ev_csgEx3_txt[0].addFocusListener(a);
            ev_csgEx3_txt[0].addModifyListener(m);
            ev_csgEx4_txt[0].addFocusListener(a);
            ev_csgEx4_txt[0].addModifyListener(m);
            ev_csgEx5_txt[0].addFocusListener(a);
            ev_csgEx5_txt[0].addModifyListener(m);
            ev_csgEx6_txt[0].addFocusListener(a);
            ev_csgEx6_txt[0].addModifyListener(m);
            ev_csgEx7_txt[0].addFocusListener(a);
            ev_csgEx7_txt[0].addModifyListener(m);
        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateCSGaction();
                }
            };

            final ModifyListener m = new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateCSGaction();
                }
            };

            ev_csgAction_cmb[0].addFocusListener(a);
            ev_csgAction_cmb[0].addModifyListener(m);
            ev_csgAction1_txt[0].addFocusListener(a);
            ev_csgAction1_txt[0].addModifyListener(m);
            ev_csgAction2_txt[0].addFocusListener(a);
            ev_csgAction2_txt[0].addModifyListener(m);
            ev_csgAction3_txt[0].addFocusListener(a);
            ev_csgAction3_txt[0].addModifyListener(m);

        }

        ev_csgQuality_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCSGquality();
            }
        });

        ev_csgQuality_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateCSGquality();
            }
        });

        ev_csgEpsilon_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCSGepsilon();
            }
        });

        ev_csgEpsilon_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateCSGepsilon();
            }
        });

        ev_csgTJunctionEpsilon_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCSGtjunctionEpsilon();
            }
        });

        ev_csgTJunctionEpsilon_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateCSGtjunctionEpsilon();
            }
        });

        ev_csgEdgeCollapseEpsilon_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCSGedgeCollapseEpsilon();
            }
        });

        ev_csgEdgeCollapseEpsilon_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateCSGedgeCollapseEpsilon();
            }
        });

        WidgetUtil(ev_csgDontOptimize_btn[0]).addXSelectionListener(new WidgetSelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateCSGdontOptimize();
            }
        });

        ev_csgCompile_txt[0].addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateCSGcompile();
            }
        });

        ev_csgCompile_txt[0].addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                updateCSGcompile();
            }
        });

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updateCSGaction();
                }
            };

            final ModifyListener m = new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateCSGaction();
                }
            };

            ev_csgAction_cmb[0].addFocusListener(a);
            ev_csgAction_cmb[0].addModifyListener(m);
            ev_csgAction1_txt[0].addFocusListener(a);
            ev_csgAction1_txt[0].addModifyListener(m);
            ev_csgAction2_txt[0].addFocusListener(a);
            ev_csgAction2_txt[0].addModifyListener(m);
            ev_csgAction3_txt[0].addFocusListener(a);
            ev_csgAction3_txt[0].addModifyListener(m);

        }

        {
            final org.eclipse.swt.events.FocusAdapter a = new org.eclipse.swt.events.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    updatePNGdef();
                }
            };
            final ModifyListener m = new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updatePNGdef();
                }
            };
            WidgetUtil(ev_png_btn[0]).addXSelectionListener(new WidgetSelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updatePNGdef();
                }
            });
            ev_png1_txt[0].addFocusListener(a);
            ev_png1_txt[0].addModifyListener(m);
            ev_png2_txt[0].addFocusListener(a);
            ev_png2_txt[0].addModifyListener(m);
            ev_png3_txt[0].addFocusListener(a);
            ev_png3_txt[0].addModifyListener(m);
            ev_png4_txt[0].addFocusListener(a);
            ev_png4_txt[0].addModifyListener(m);
            ev_png5_txt[0].addFocusListener(a);
            ev_png5_txt[0].addModifyListener(m);
            ev_png6_txt[0].addFocusListener(a);
            ev_png6_txt[0].addModifyListener(m);
            ev_png7_txt[0].addFocusListener(a);
            ev_png7_txt[0].addModifyListener(m);
            ev_png8_txt[0].addFocusListener(a);
            ev_png8_txt[0].addModifyListener(m);
            ev_png9_txt[0].addFocusListener(a);
            ev_png9_txt[0].addModifyListener(m);

            WidgetUtil(ev_png_btn[0]).addXSelectionListener(new WidgetSelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {

                    FileDialog fd = new FileDialog(sh, SWT.OPEN);
                    fd.setText(I18n.META_ChoosePng);

                    if (Project.DEFAULT_PROJECT_PATH.equals(Project.getProjectPath()) && Project.DEFAULT_PROJECT_PATH.equals(Project.PROJECT)) {
                        try {
                            String path = LDPartEditor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                            String decodedPath = URLDecoder.decode(path, "UTF-8"); //$NON-NLS-1$
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
                    String[] filterNames = { I18n.E3D_PortableNetworkGraphics, I18n.E3D_AllFiles };
                    fd.setFilterNames(filterNames);
                    String selected = fd.open();
                    if (selected != null) {
                        ev_png9_txt[0].setText(selected);
                    }
                }
            });

        }

        this.open();
    }

    private void updatePNGdef() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !LPE PNG "); //$NON-NLS-1$
        sb.append(ev_png1_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_png2_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_png3_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_png4_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_png5_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_png6_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_png7_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_png8_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_png9_txt[0].getText().trim());
        lbl_lineToInsert[0].setText(sb.toString());
        lbl_lineToInsert[0].getParent().layout();
    }


    private void updateCSGcompile() {
        lbl_lineToInsert[0].setText("0 !LPE CSG_COMPILE " + ev_csgCompile_txt[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateCSGepsilon() {
        lbl_lineToInsert[0].setText("0 !LPE CSG_EPSILON " + ev_csgEpsilon_txt[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateCSGtjunctionEpsilon() {
        lbl_lineToInsert[0].setText("0 !LPE CSG_TJUNCTION_EPSILON " + ev_csgTJunctionEpsilon_txt[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateCSGedgeCollapseEpsilon() {
        lbl_lineToInsert[0].setText("0 !LPE CSG_EDGE_COLLAPSE_EPSILON " + ev_csgEdgeCollapseEpsilon_txt[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateCSGquality() {
        lbl_lineToInsert[0].setText("0 !LPE CSG_QUALITY " + ev_csgQuality_txt[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateCSGaction() {
        lbl_lineToInsert[0].setText("0 !LPE CSG_" + ev_csgAction_cmb[0].getText().trim() + " " + ev_csgAction1_txt[0].getText().trim() + " " + ev_csgAction2_txt[0].getText().trim() + " " + ev_csgAction3_txt[0].getText().trim()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateCSGdontOptimize() {
        lbl_lineToInsert[0].setText("0 !LPE CSG_DONT_OPTIMISE"); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateCSGdef() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !LPE CSG_"); //$NON-NLS-1$
        sb.append(ev_csgBody_cmb[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody1_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody2_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody3_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody4_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody5_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody6_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody7_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody8_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody9_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody10_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody11_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody12_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody13_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgBody14_txt[0].getText().trim());
        lbl_lineToInsert[0].setText(sb.toString());
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateCSGtrans() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !LPE CSG_TRANSFORM "); //$NON-NLS-1$
        sb.append(ev_csgTrans1_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans2_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans3_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans4_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans5_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans6_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans7_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans8_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans9_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans10_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans11_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans12_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans13_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans14_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_csgTrans15_txt[0].getText().trim());
        lbl_lineToInsert[0].setText(sb.toString());
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateCSGextrude() {
        // 0 !LPE CSG_EXT_CFG seg_len= 100000.0 no_of_tr= 1 curve= 10.0 center= 0.5 rot= 0.0 comp= false invert= false
        StringBuilder sb = new StringBuilder();
        String arg;
        sb.append("0 !LPE CSG_EXT_CFG "); //$NON-NLS-1$
        sb.append("seg_len= "); //$NON-NLS-1$
        if ((arg = ev_csgEx1_txt[0].getText().trim()).isEmpty()) {
            sb.append("100000.0"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        sb.append(" no_of_tr= "); //$NON-NLS-1$
        if ((arg = ev_csgEx2_txt[0].getText().trim()).isEmpty()) {
            sb.append("1"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        sb.append(" curve= "); //$NON-NLS-1$
        if ((arg = ev_csgEx3_txt[0].getText().trim()).isEmpty()) {
            sb.append("10.0"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        sb.append(" center= "); //$NON-NLS-1$
        if ((arg = ev_csgEx4_txt[0].getText().trim()).isEmpty()) {
            sb.append("0.5"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        sb.append(" rot= "); //$NON-NLS-1$
        if ((arg = ev_csgEx5_txt[0].getText().trim()).isEmpty()) {
            sb.append("0.0"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        sb.append(" comp= "); //$NON-NLS-1$
        if ((arg = ev_csgEx6_txt[0].getText().trim()).isEmpty()) {
            sb.append("false"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        sb.append(" invert= "); //$NON-NLS-1$
        if ((arg = ev_csgEx7_txt[0].getText().trim()).isEmpty()) {
            sb.append("false"); //$NON-NLS-1$
        } else {
            sb.append(arg);
        }
        lbl_lineToInsert[0].setText(sb.toString());
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateLpeVertex() {
        lbl_lineToInsert[0].setText("0 !LPE VERTEX " + ev_vertex1_txt[0].getText().trim() + " " + ev_vertex2_txt[0].getText().trim() + " " + ev_vertex3_txt[0].getText().trim()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateLpeTODO() {
        lbl_lineToInsert[0].setText("0 !LPE TODO " + ev_todo_txt[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateTexmapPlanar() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !TEXMAP "); //$NON-NLS-1$
        sb.append(ev_texmapPlanar_cmb[0].getText().trim());
        sb.append(" PLANAR "); //$NON-NLS-1$
        sb.append(ev_texmapPlanar1_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapPlanar2_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapPlanar3_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapPlanar4_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapPlanar5_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapPlanar6_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapPlanar7_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapPlanar8_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapPlanar9_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapPlanar10_txt[0].getText().trim());
        lbl_lineToInsert[0].setText(sb.toString());
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateTexmapCylindrical() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !TEXMAP "); //$NON-NLS-1$
        sb.append(ev_texmapCyli_cmb[0].getText().trim());
        sb.append(" CYLINDRICAL "); //$NON-NLS-1$
        sb.append(ev_texmapCyli1_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapCyli2_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapCyli3_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapCyli4_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapCyli5_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapCyli6_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapCyli7_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapCyli8_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapCyli9_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapCyli10_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapCyli11_txt[0].getText().trim());
        lbl_lineToInsert[0].setText(sb.toString());
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateTexmapSpherical() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !TEXMAP "); //$NON-NLS-1$
        sb.append(ev_texmapSphere_cmb[0].getText().trim());
        sb.append(" SPHERICAL "); //$NON-NLS-1$
        sb.append(ev_texmapSphere1_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapSphere2_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapSphere3_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapSphere4_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapSphere5_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapSphere6_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapSphere7_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapSphere8_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapSphere9_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapSphere10_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapSphere11_txt[0].getText().trim());
        sb.append(" "); //$NON-NLS-1$
        sb.append(ev_texmapSphere12_txt[0].getText().trim());
        lbl_lineToInsert[0].setText(sb.toString());
        lbl_lineToInsert[0].getParent().layout();
    }


    private void updateTexmapMeta() {
        lbl_lineToInsert[0].setText("0 !: " + ev_texmapMeta_txt[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateBfc() {
        lbl_lineToInsert[0].setText("0 BFC " + ev_bfc_cmb[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateComment() {
        lbl_lineToInsert[0].setText("0 // " + (ev_comment_btn[0].getSelection() ? "Needs work: " : "") + ev_comment_txt[0].getText().trim()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateHistory1() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !HISTORY "); //$NON-NLS-1$
        sb.append(ev_history11_txt[0].getText().trim());
        if (!ev_history12_txt[0].getText().trim().equals("")) { //$NON-NLS-1$
            sb.append(" ["); //$NON-NLS-1$
            sb.append(ev_history12_txt[0].getText().trim());
            sb.append("] "); //$NON-NLS-1$
        } else {
            sb.append(" "); //$NON-NLS-1$
        }
        sb.append(ev_history13_txt[0].getText().trim());
        lbl_lineToInsert[0].setText(sb.toString());
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateHistory2() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !HISTORY "); //$NON-NLS-1$
        sb.append(ev_history21_txt[0].getText().trim());
        if (!ev_history22_txt[0].getText().trim().equals("")) { //$NON-NLS-1$
            sb.append(" {"); //$NON-NLS-1$
            sb.append(ev_history22_txt[0].getText().trim());
            sb.append("} "); //$NON-NLS-1$
        } else {
            sb.append(" "); //$NON-NLS-1$
        }
        sb.append(ev_history23_txt[0].getText().trim());
        lbl_lineToInsert[0].setText(sb.toString());
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateCmdline() {
        lbl_lineToInsert[0].setText("0 !CMDLINE " + ev_cmdline_txt[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateKeywords() {
        lbl_lineToInsert[0].setText("0 !KEYWORDS " + ev_keywords_txt[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateCategory() {
        lbl_lineToInsert[0].setText("0 !CATEGORY " + ev_category_cmb[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateBfcHeader() {
        lbl_lineToInsert[0].setText("0 BFC " + ev_bfcHeader_cmb[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateHelp() {
        lbl_lineToInsert[0].setText("0 !HELP " + ev_help_txt[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateLicense() {
        lbl_lineToInsert[0].setText("0 !LICENSE " + ev_license_cmb[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateType() {
        StringBuilder sb = new StringBuilder();
        sb.append("0 !LDRAW_ORG "); //$NON-NLS-1$
        if (ev_type_unofficial_btn[0].getSelection()) {
            sb.append("Unofficial_"); //$NON-NLS-1$
        }
        sb.append(ev_type_type_cmb[0].getText().trim());
        if (ev_type_update_btn[0].getSelection()) {
            ev_type_update_txt[0].setEnabled(true);
            sb.append(" UPDATE "); //$NON-NLS-1$
            sb.append(ev_type_update_txt[0].getText().trim());
        } else {
            ev_type_update_txt[0].setEnabled(false);
        }
        lbl_lineToInsert[0].setText(sb.toString());
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateAuthor() {
        lbl_lineToInsert[0].setText("0 Author: " + ev_author_realName_txt[0].getText().trim() + (ev_author_userName_txt[0].getText().trim().equals("") ? "" : " [" + ev_author_userName_txt[0].getText().trim() + "]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateName() {
        lbl_lineToInsert[0].setText("0 Name: " + ev_name_txt[0].getText().trim()); //$NON-NLS-1$
        lbl_lineToInsert[0].getParent().layout();
    }

    private void updateDescription() {
        lbl_lineToInsert[0].setText("0 " + ev_description_txt[0].getText().trim() + (ev_description_btn[0].getSelection() ? " (Needs Work)" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        lbl_lineToInsert[0].getParent().layout();
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

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
package org.nschmidt.ldparteditor.shells.searchnreplace;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.WidgetUtil;

import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.StringHelper;

public class SearchWindow extends SearchDesign {

    private StyledText textComposite = null;
    private CompositeTab tab = null;

    private boolean scopeAll = true;

    private int selectionStart = 0;
    private int selectionEnd = 0;

    /**
     * Create the application window.
     */
    public SearchWindow(Shell txtEditorShell) {
        super(txtEditorShell);
    }

    /**
     * Run a fresh instance of this window
     */
    public void run() {
        // Creating the window to get the shell
        this.create();
        final Shell sh = getShell();
        sh.setText("Find/Replace"); //$NON-NLS-1$
        sh.setImage(ResourceManager.getImage("imgDuke2.png")); //$NON-NLS-1$
        sh.setMinimumSize(super.getInitialSize());

        // MARK All final listeners will be configured here..
        WidgetUtil(btn_find[0]).addSelectionListener(e -> find());

        txt_find[0].addModifyListener(e -> {
            setDisabledButtonStatus(textComposite);

            if (btn_find[0].isEnabled() && cb_incremental[0].getSelection()) {

                final String text;
                final String criteria;

                if (cb_caseSensitive[0].getSelection()) {
                    text = textComposite.getText();
                    criteria = txt_find[0].getText();
                } else {
                    text = textComposite.getText().toLowerCase(Locale.ENGLISH);
                    criteria = txt_find[0].getText().toLowerCase(Locale.ENGLISH);
                }

                int len = criteria.length();
                if (len > 0) {

                    if (textComposite.getSelectionRange().x == 0 && text.startsWith(criteria)) {
                        textComposite.setSelectionRange(0, len);
                        textComposite.showSelection();
                    } else {
                        int index = text.indexOf(criteria, textComposite.getSelectionRange().x);

                        if (index != -1) {
                            textComposite.setSelectionRange(index, len);
                            textComposite.showSelection();
                        } else {
                            textComposite.setSelectionRange(0, 0);
                        }

                    }

                } else {
                    textComposite.setSelectionRange(textComposite.getSelectionRange().x, 0);
                }

            }
        });

        txt_find[0].addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                setDisabledButtonStatus(textComposite);
            }
            @Override
            public void focusGained(FocusEvent consumed) {
                setDisabledButtonStatus(textComposite);
            }
        });

        WidgetUtil(btn_replace[0]).addSelectionListener(e -> replace());
        WidgetUtil(btn_replaceAll[0]).addSelectionListener(e -> replaceAll());
        WidgetUtil(btn_findAndReplace[0]).addSelectionListener(e -> {
            replace();
            find();
            replace();
        });

        txt_replace[0].addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                setDisabledButtonStatus(textComposite);
            }
            @Override
            public void focusGained(FocusEvent consumed) {
                setDisabledButtonStatus(textComposite);
            }
        });

        WidgetUtil(rb_selectedLines[0]).addSelectionListener(e -> {
            try {
                if (rb_selectedLines[0].getSelection()) {
                    scopeAll = false;

                    {
                        int offset1 = textComposite.getSelectionRange().x;
                        selectionStart = textComposite.getOffsetAtLine(offset1 > -1 ? textComposite.getLineAtOffset(offset1) : offset1 * -1);
                    }

                    {
                        int offset2 = textComposite.getSelectionRange().x + textComposite.getSelectionRange().y;
                        selectionEnd = textComposite.getOffsetAtLine((offset2 > -1 ? textComposite.getLineAtOffset(offset2) : offset2 * -1) + 1);
                    }

                    textComposite.setSelectionRange(selectionStart, selectionEnd - selectionStart);
                } else {
                    scopeAll = true;
                }
            } catch (IllegalArgumentException iae) {

            }
        });

        Editor3DWindow.getWindow().setSearchWindow(this);
        sh.setLocation(loc);
        this.open();
    }

    /**
     * The Shell-Close-Event
     */
    @Override
    protected void handleShellCloseEvent() {
        Editor3DWindow.getWindow().setSearchWindow(null);
        setReturnCode(CANCEL);
        close();
    }

    public void setTextComposite(CompositeTab tab) {
        if (tab != null) {
            textComposite = tab.getTextComposite();
            this.tab = tab;
        } else {
            textComposite = null;
            this.tab = null;
        }
    }

    private void setDisabledButtonStatus(StyledText txt) {
        boolean b = textComposite == null || textComposite.isDisposed();
        btn_find[0].setEnabled(!b);
        btn_findAndReplace[0].setEnabled(!b);
        btn_replace[0].setEnabled(!b);
        btn_replaceAll[0].setEnabled(!b);
        getShell().update();
    }

    private void find() {
        setDisabledButtonStatus(textComposite);

        if (btn_find[0].isEnabled()) {

            final String text;
            final String criteria;

            if (cb_caseSensitive[0].getSelection()) {
                text = textComposite.getText();
                criteria = txt_find[0].getText();
            } else {
                text = textComposite.getText().toLowerCase(Locale.ENGLISH);
                criteria = txt_find[0].getText().toLowerCase(Locale.ENGLISH);
            }

            int len = criteria.length();
            if (len > 0) {

                if (rb_forward[0].getSelection()) {

                    if (textComposite.getSelectionRange().x == 0  && textComposite.getSelectionRange().y == 0 && text.startsWith(criteria)) {
                        textComposite.setSelectionRange(0, len);
                        textComposite.showSelection();
                    } else {

                        int index = text.indexOf(criteria, textComposite.getSelectionRange().x + len);

                        if (index != -1) {
                            if (!scopeAll && index > selectionEnd) {
                                textComposite.setSelectionRange(selectionStart, 0);
                            } else {
                                textComposite.setSelectionRange(index, len);
                                textComposite.showSelection();
                            }
                        } else {
                            if (scopeAll) {
                                textComposite.setSelectionRange(0, 0);
                            } else {
                                textComposite.setSelectionRange(selectionStart, 0);
                            }
                        }
                    }

                } else {

                    int index = text.lastIndexOf(criteria, textComposite.getSelectionRange().x - 1);

                    if (index != -1) {
                        if (!scopeAll && index < selectionStart) {
                            textComposite.setSelectionRange(selectionEnd, 0);
                        } else {
                            textComposite.setSelectionRange(index, len);
                            textComposite.showSelection();
                        }
                    } else {
                        if (scopeAll) {
                            textComposite.setSelectionRange(text.length(), 0);
                        } else {
                            textComposite.setSelectionRange(selectionEnd, 0);
                        }
                    }
                }
            }
        }
    }

    private void replace() {
        if (textComposite == null || tab != null && !tab.getState().getFileNameObj().getVertexManager().isUpdated()) return;
        setDisabledButtonStatus(textComposite);

        if (btn_find[0].isEnabled()) {

            final String text;
            final String criteria;

            final String replacement = txt_replace[0].getText();

            if (cb_caseSensitive[0].getSelection()) {
                text = textComposite.getSelectionText();
                criteria = txt_find[0].getText();
            } else {
                text = textComposite.getSelectionText().toLowerCase(Locale.ENGLISH);
                criteria = txt_find[0].getText().toLowerCase(Locale.ENGLISH);
            }

            int len = criteria.length();
            if (len > 0 && text.length() > 0 && text.equals(criteria)) {
                final int offset = textComposite.getSelectionRange().x;
                textComposite.insert(replacement);
                if (replacement.length() == 0) {
                    if (rb_forward[0].getSelection()) {
                        try {
                            textComposite.setSelectionRange(offset - 1, 0);
                        } catch (IllegalArgumentException iae) {
                            textComposite.setSelectionRange(offset - 2, 0);
                        }
                    } else {
                        try {
                            textComposite.setSelectionRange(offset + 1, 0);
                        } catch (IllegalArgumentException iae) {
                            textComposite.setSelectionRange(offset + 2, 0);
                        }
                    }
                } else {
                    textComposite.setSelectionRange(offset, replacement.length());
                }

                textComposite.showSelection();
            }
        }
    }

    private void replaceAll() {
        if (textComposite == null || tab != null && !tab.getState().getFileNameObj().getVertexManager().isUpdated()) return;
        setDisabledButtonStatus(textComposite);

        if (btn_find[0].isEnabled()) {

            final String criteria;

            final String replacement = txt_replace[0].getText();

            if (cb_caseSensitive[0].getSelection()) {
                criteria = txt_find[0].getText();
            } else {
                criteria = txt_find[0].getText().toLowerCase(Locale.ENGLISH);
            }
            int len = criteria.length();
            if (len > 0) {

                if (rb_all[0].getSelection()) {
                    try {
                        if (cb_caseSensitive[0].getSelection()) {
                            try {
                                textComposite.setText(textComposite.getText().replaceAll(Pattern.quote(criteria), replacement));
                            } catch (Exception e) {
                                // Workaround for windows OS / JVM BUG
                                textComposite.setText(textComposite.getText().replace(Pattern.quote(criteria), replacement));
                            }

                        } else {
                            try {
                                textComposite.setText(textComposite.getText().replaceAll("(?i)" + Pattern.quote(criteria), replacement)); //$NON-NLS-1$
                            } catch (Exception e) {
                                // Workaround for windows OS / JVM BUG
                                textComposite.setText(textComposite.getText().replace("(?i)" + Pattern.quote(criteria), replacement)); //$NON-NLS-1$
                            }
                        }
                    } catch (Exception consumed) {}
                } else {

                    textComposite.setSelectionRange(selectionStart, selectionEnd - selectionStart);

                    String textToReplace = textComposite.getSelectionText();
                    if (!textToReplace.isEmpty()) {
                        try {
                            if (cb_caseSensitive[0].getSelection()) {
                                try {
                                    textToReplace = textToReplace.replaceAll(Pattern.quote(criteria), replacement);
                                } catch (Exception e) {
                                    // Workaround for windows OS / JVM BUG
                                    textToReplace = textToReplace.replace(Pattern.quote(criteria), replacement);
                                }

                            } else {
                                try {
                                    textToReplace = textToReplace.replaceAll("(?i)" + Pattern.quote(criteria), replacement); //$NON-NLS-1$
                                } catch (Exception e) {
                                    // Workaround for windows OS / JVM BUG
                                    textToReplace = textToReplace.replace("(?i)" + Pattern.quote(criteria), replacement); //$NON-NLS-1$
                                }
                            }

                            textComposite.insert(textToReplace);
                            if (rb_selectedLines[0].getSelection()) {
                                textComposite.setSelectionRange(selectionStart, textToReplace.length() - StringHelper.getLineDelimiter().length());
                            } else {
                                textComposite.setSelectionRange(selectionStart, textToReplace.length());
                            }

                        } catch (Exception consumed) {}
                    }
                }
            }
        }
    }

    public void setScopeToAll() {
        try {
            rb_all[0].setSelection(true);
            rb_selectedLines[0].setSelection(false);
            getShell().update();
        } catch (Exception consumed) {}
    }
}

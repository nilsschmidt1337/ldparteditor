package org.nschmidt.ldparteditor.dialogs.options;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.i18n.I18n;

class OptionsDesign extends ApplicationWindow {

    public OptionsDesign(Shell parentShell) {
        super(parentShell);
        // TODO Auto-generated constructor stub
    }

    /**
     * Create contents of the application window.
     *
     * @param parent
     */
    @Override
    protected Control createContents(Composite parent) {
        setStatus(I18n.E3D_ReadyStatus);
        Composite container = new Composite(parent, SWT.NONE);

        return container;
    }


    // FIXME OptionsDialog needs implementation!
}

package org.nschmidt.ldparteditor.dialogs.options;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.helpers.Version;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

public class OptionsDialog extends OptionsDesign {

    public OptionsDialog(Shell parentShell) {
        super(parentShell);
    }

    public void run() {
        final OptionsDialog me = this;
        this.setBlockOnOpen(true);
        this.setShellStyle(SWT.APPLICATION_MODAL | SWT.SHELL_TRIM ^ SWT.MIN);
        this.create();
        final Shell sh = this.getShell();
        sh.setText(Version.getApplicationName());
        sh.setImage(ResourceManager.getImage("imgDuke2.png")); //$NON-NLS-1$

        btn_OK[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Editor3DWindow.getWindow().compileAll();
                me.close();
            }
        });

        this.open();
    }

    @Override
    protected void handleShellCloseEvent() {
        Editor3DWindow.getWindow().compileAll();
        super.handleShellCloseEvent();
    }

    // FIXME OptionsDialog needs implementation!
}

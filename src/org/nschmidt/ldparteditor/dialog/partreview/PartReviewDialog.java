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
package org.nschmidt.ldparteditor.dialog.partreview;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public class PartReviewDialog extends PartReviewDesign {

    public PartReviewDialog(Shell parentShell, boolean alreadyReviewing) {
        super(parentShell, alreadyReviewing);
    }

    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        txtFilePtr[0].addModifyListener(e -> {
            fileName = txtFilePtr[0].getText();
            String formattedFileName = fileName;
            if (formattedFileName.endsWith(".dat")) formattedFileName = formattedFileName.substring(0, formattedFileName.length() - 4); //$NON-NLS-1$
            if (formattedFileName.contains("/")) formattedFileName = formattedFileName.substring(formattedFileName.lastIndexOf('/') + 1); //$NON-NLS-1$
            if (formattedFileName.contains("\\")) formattedFileName = formattedFileName.substring(formattedFileName.lastIndexOf('\\') + 1); //$NON-NLS-1$
            formattedFileName = WorkbenchManager.getUserSettingState().getAuthoringFolderPath() + File.separator + I18n.PARTREVIEW_REVIEW + File.separator + formattedFileName + File.separator;
            projectPath = formattedFileName;
            Object[] messageArguments = {projectPath};
            MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
            formatter.setLocale(MyLanguage.getLocale());
            formatter.applyPattern(I18n.PARTREVIEW_STORE_LOCATION);
            btnStoreLocallyPtr[0].setText(formatter.format(messageArguments));
            btnStoreLocallyPtr[0].getParent().layout();
        });
        widgetUtil(btnStoreLocallyPtr[0]).addSelectionListener(e -> WorkbenchManager.getUserSettingState().setPartReviewStoreLocalFiles(btnStoreLocallyPtr[0].getSelection()));
        widgetUtil(btnVerbosePtr[0]).addSelectionListener(e -> WorkbenchManager.getUserSettingState().setVerbosePartReview(btnVerbosePtr[0].getSelection()));
        this.spnViewCountPtr[0].addValueChangeListener(spn ->
            WorkbenchManager.getUserSettingState().setPartReview3dViewCount(spnViewCountPtr[0].getValue())
        );
        return super.open();
    }

    public static String getFileName() {
        return fileName;
    }

    public static String getProjectPath() {
        return projectPath;
    }
}

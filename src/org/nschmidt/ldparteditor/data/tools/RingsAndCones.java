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
package org.nschmidt.ldparteditor.data.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.helpers.composite3d.RingsAndConesSettings;

/**
 * @author nils
 *
 */
public enum RingsAndCones {
    INSTANCE;

    public static void solve(Shell sh, final VertexManager vm, final ArrayList<Primitive> allPrimitives, final RingsAndConesSettings rs, boolean syncWithTextEditor) {
        try
        {
            new ProgressMonitorDialog(sh).run(true, false, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException
                {
                    m.beginTask("Solving...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N
                }
            });
        }catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }
    }

}

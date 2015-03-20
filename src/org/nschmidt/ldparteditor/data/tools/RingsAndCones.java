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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.helpers.composite3d.RingsAndConesSettings;

/**
 * @author nils
 *
 */
public enum RingsAndCones {
    INSTANCE;


    private static List<Primitive> prims = null;

    private static Map<Integer, boolean[]> existanceMap = new HashMap<Integer, boolean[]>();

    public static void solve(Shell sh, final VertexManager vm, final ArrayList<Primitive> allPrimitives, final RingsAndConesSettings rs, boolean syncWithTextEditor) {
        if (rs.isUsingExistingPrimitives()) {
            prims = allPrimitives;
            initExistanceMap(rs.isUsingCones());
        }
        try
        {
            new ProgressMonitorDialog(sh).run(true, false, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException
                {
                    m.beginTask("Solving...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N

                    int solutionCount = 0;

                    // TODO We need two different solvers here.

                    // Solver 1: A specialised version, which uses only existing primitives
                    // Solver 2: A general solver, which is not bound to use only existing primitives

                    // MARK Solver 1
                    if (rs.isUsingExistingPrimitives()) {

                    } else {
                        // MARK Solver 1

                    }

                    // TODO The solution needs to be evaluated here
                    if (solutionCount == 0) {
                        if (rs.isCreatingShapeForNoSolution()) {

                        } else {
                            return;
                        }
                    } else {

                    }

                    // TODO The solution should be transformed to the location of a selected 4-4disc.dat if any was selected.
                    GData1 disc44 = null;
                    for (GData gd : vm.getSelectedData()) {
                        if (gd.type() == 1
                                && (gd.toString().toLowerCase(Locale.ENGLISH).endsWith(" 4-4disc.dat") //$NON-NLS-1$
                                        || gd.toString().toLowerCase(Locale.ENGLISH).endsWith(" 48\4-4disc.dat"))) { //$NON-NLS-1$
                            disc44 = (GData1) gd;
                        }
                    }
                    if (disc44 != null) {

                    }

                }
            });
        }catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        }
    }

    private static void initExistanceMap(boolean cones) {
        existanceMap.clear();
        for (Primitive p2 : prims) {
            if (!p2.isCategory()) {
                for (Primitive p : p2.getAllPrimitives()) {

                }
            }
        }
    }

    private static boolean primitiveExists(int radius, int angle) {
        boolean[] lst = null;
        if ((lst = existanceMap.get(radius)) != null) {
            return lst[angle];
        }
        return false;
    }

}

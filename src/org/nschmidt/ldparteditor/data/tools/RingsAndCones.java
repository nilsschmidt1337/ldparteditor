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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
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

        final BigDecimal factor = new BigDecimal(100000000L);

        long rad_min = rs.getRadius1().multiply(factor).longValue();
        long rad_max = rs.getRadius2().multiply(factor).longValue();

        // Throw an arithmetic exception in case the radii were to big.
        if (new BigDecimal(rad_min).compareTo(rs.getRadius1()) < 0 || new BigDecimal(rad_max).compareTo(rs.getRadius2()) < 0) {
            throw new ArithmeticException("The given radius was too big."); //$NON-NLS-1$
        }
        {
            long rad_tmp = rad_min;
            rad_min = Math.min(rad_min, rad_max);
            rad_max = Math.max(rad_tmp, rad_max);
            if (rad_min == rad_max) return;
        }

        final long radi_min = rad_min;
        final long radi_max = rad_max;

        final int angle = rs.getAngle();

        if (rs.isUsingExistingPrimitives()) {
            prims = allPrimitives;
            initExistanceMap(rs.isUsingCones());
        }

        final int[] solutionAmount = new int[]{0};
        final long[] solution = new long[50];
        try
        {
            new ProgressMonitorDialog(sh).run(true, true, new IRunnableWithProgress()
            {
                @Override
                public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException
                {
                    m.beginTask("Solving (you have to press cancel to stop)...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ I18N



                    // TODO We need two different solvers here.

                    // Solver 1: A specialised version, which uses only existing primitives
                    // Solver 2: A general solver, which is not bound to use only existing primitives


                    {
                        int min_amount = Integer.MAX_VALUE;
                        int amount = 0;

                        final long min_r = 1;
                        long max_r = 100;

                        long min_delta = Long.MAX_VALUE;

                        long current = radi_min;

                        Random rnd = new Random(1238426235L);

                        final long[] tsolution = new long[50];

                        final long width = max_r - min_r;

                        final long[] rndSet;
                        final int size = existanceMap.keySet().size();
                        {
                            int i = 0;
                            rndSet  = new long[size];
                            for(int in : existanceMap.keySet())
                            {
                                rndSet[i] = in;
                                i = i + 1;
                            }
                        }

                        while (!m.isCanceled()) {
                            long s, r;
                            // MARK Solver 1
                            if (rs.isUsingExistingPrimitives()) {
                                r = rndSet[(int) (rnd.nextFloat() * size)];
                                if (!primitiveExists((int) r, angle)) {
                                    continue;
                                }
                            } else {
                                // MARK Solver 2
                                r = (long) (width * rnd.nextDouble() + min_r);
                            }

                            s = current / r;
                            if (s < 10000000L) {
                                max_r = r;
                                continue;
                            }

                            amount++;
                            if (amount < 47) {
                                tsolution[amount] = s;
                            } else {
                                current = radi_min;
                                amount = 0;
                                continue;
                            }
                            long sum = s + current;

                            if (sum >= radi_max || amount > min_amount) {
                                if (amount <= min_amount && amount < 47) {
                                    long delta = Math.abs(sum - radi_max);
                                    if (100000L >= delta) {
                                        if (amount != min_amount) {
                                            min_delta = Long.MAX_VALUE;
                                            min_amount = amount;
                                            m.subTask("Best Solution - " + min_amount + " Primitives, with " + View.NUMBER_FORMAT4F.format(new BigDecimal(delta).divide(factor, Threshold.mc).doubleValue()) + " deviation."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                            for(int i = 1; i < amount; i++) {
                                                solution[i] = tsolution[i];
                                            }
                                            solution[amount] = tsolution[amount];
                                            solutionAmount[0] = amount;
                                        } else if (delta < min_delta) {
                                            min_delta = delta;
                                            min_amount = amount;
                                            m.subTask("Best Solution - " + min_amount + " Primitives, with " + View.NUMBER_FORMAT4F.format(new BigDecimal(delta).divide(factor, Threshold.mc).doubleValue()) + " deviation."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                            for(int i = 1; i < amount; i++) {
                                                solution[i] = tsolution[i];
                                            }
                                            solution[amount] = tsolution[amount];
                                            solutionAmount[0] = amount;
                                        }
                                    }
                                }
                                current = radi_min;
                                amount = 0;
                            } else {
                                current = sum;
                            }
                        }
                    }
                }
            });
        } catch (InvocationTargetException consumed) {
        } catch (InterruptedException consumed) {
        } catch (Exception ex) {
        }


        // TODO The solution needs to be evaluated here
        if (solutionAmount[0] == 0) {
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

    private static void initExistanceMap(boolean cones) {
        existanceMap.clear();

        Pattern coneP = Pattern.compile("\\d+\\-\\d+cone{0,1}\\d{1,2}\\.dat"); //$NON-NLS-1$
        Pattern ringP = Pattern.compile("\\d+\\-\\d+ring{0,1}\\d{1,2}\\.dat"); //$NON-NLS-1$

        StringBuilder upper = new StringBuilder();
        StringBuilder lower = new StringBuilder();
        StringBuilder number = new StringBuilder();

        for (Primitive p2 : prims) {
            for (Primitive p : p2.getAllPrimitives()) {
                if (!p.isCategory()) {
                    String name = p.getName();
                    if (cones && coneP.matcher(name).matches() || ringP.matcher(name).matches()) {

                        if (name.startsWith("48\\")) name = name.substring(3); //$NON-NLS-1$
                        if (name.startsWith("48\\")) name = name.substring(3); //$NON-NLS-1$
                        // Special cases: unknown parts numbers "u[Number]" and unknown
                        // stickers "s[Number]"
                        if (name.charAt(0) == 'u' && name.charAt(0) == 'u' || name.charAt(0) == 's' && name.charAt(0) == 's') {
                            name = name.substring(1, name.length());
                            name = name.substring(1, name.length());
                        }

                        if ((name.charAt(1) == '-' || name.charAt(2) == '-')
                                && (name.charAt(1) == '-' || name.charAt(2) == '-')) {

                            upper.setLength(0);
                            lower.setLength(0);

                            boolean readUpper = true;
                            char[] chars_this = name.toCharArray();
                            for (char c : chars_this) {
                                if (Character.isDigit(c)) {
                                    if (readUpper) {
                                        upper.append(c);
                                    } else {
                                        lower.append(c);
                                    }
                                } else {
                                    if (readUpper) {
                                        readUpper = false;
                                    } else {
                                        break;
                                    }
                                }
                            }
                            if (upper.length() > 0 && lower.length() > 0) {
                                number.setLength(0);
                                boolean readDigit = false;
                                for (int i = chars_this.length - 1; i > 0 ; i--) {
                                    char c = chars_this[i];
                                    if (Character.isDigit(c)) {
                                        number = number.insert(0, c);
                                        readDigit = true;
                                    } else if (readDigit) {
                                        break;
                                    } else if (i < chars_this.length - 5) {
                                        break;
                                    }
                                }
                                if (readDigit) {
                                    try {
                                        int index = (int) (48.0 * Double.parseDouble(upper.toString()) / Double.parseDouble(lower.toString())) - 1;
                                        int radius = Integer.parseInt(number.toString());
                                        if (existanceMap.containsKey(radius)) {
                                            existanceMap.get(radius)[index] = true;
                                        } else {
                                            existanceMap.put(radius, new boolean[48]);
                                            existanceMap.get(radius)[index] = true;
                                        }
                                    } catch (NumberFormatException consumed) {}
                                }
                            }
                        }
                    }
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

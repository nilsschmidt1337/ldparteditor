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
package org.nschmidt.ldparteditor.data;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.LDPartEditorException;
import org.nschmidt.ldparteditor.helpers.composite3d.RingsAndConesSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.DatParser;

/**
 * @author nils
 *
 */
public enum RingsAndCones {
    INSTANCE;


    private static List<Primitive> prims = null;

    private static Map<Integer, boolean[]> existanceMap = new HashMap<>();

    public static void solve(Shell sh, final DatFile df, final ArrayList<Primitive> allPrimitives, final RingsAndConesSettings rs, boolean syncWithTextEditor) {

        if (df.isReadOnly()) return;

        final VertexManager vm = df.getVertexManager();
        final GColour col16 = View.getLDConfigColour(16);

        vm.clearSelection2();

        final BigDecimal factor = new BigDecimal(100000000L);

        long radMin = rs.getRadius1().multiply(factor).longValue();
        long radMax = rs.getRadius2().multiply(factor).longValue();

        // Throw an arithmetic exception in case the radii were to big.
        if (new BigDecimal(radMin).compareTo(rs.getRadius1()) < 0 || new BigDecimal(radMax).compareTo(rs.getRadius2()) < 0) {
            throw new ArithmeticException("The given radius was too big."); //$NON-NLS-1$
        }
        {
            long radTmp = radMin;
            radMin = Math.min(radMin, radMax);
            radMax = Math.max(radTmp, radMax);
            if (radMin == radMax) return;
        }

        if (radMin != 0) {

            final long radi_min = radMin;
            final long radi_max = radMax;

            final int angle = rs.getAngle();

            if (rs.isUsingExistingPrimitives()) {
                prims = allPrimitives;
                initExistanceMap(rs.isUsingCones(), rs.isUsingHiRes());
            }

            final int[] solutionAmount = new int[]{0};
            final long[] solution = new long[50];
            final long[] solutionR = new long[50];
            final long[] solutionR2 = new long[50];

            try
            {
                new ProgressMonitorDialog(sh).run(true, true, new IRunnableWithProgress()
                {
                    @Override
                    public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException
                    {
                        m.beginTask(I18n.RCONES_TASK, IProgressMonitor.UNKNOWN);

                        // We need two different solvers here.

                        // Solver 1: A specialised version, which uses only existing primitives
                        // Solver 2: A general solver, which is not bound to use only existing primitives

                        {
                            final AtomicInteger minAmountA = new AtomicInteger(Integer.MAX_VALUE);
                            final AtomicLong minDeltaA = new AtomicLong(Long.MAX_VALUE);
                            final AtomicInteger minDigitsA = new AtomicInteger(Integer.MAX_VALUE);

                            final Lock tlock = new ReentrantLock();

                            final int chunks = View.NUM_CORES;
                            final Thread[] threads = new Thread[chunks];
                            for (int j = 0; j < chunks; ++j) {
                                final long num = j;
                                threads[j] = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        {
                                            final java.text.DecimalFormat numberFormat4f = new java.text.DecimalFormat(View.NUMBER_FORMAT4F, new DecimalFormatSymbols(MyLanguage.locale));
                                            int amount = 0;
                                            final long min_r = 1;
                                            long maxR = 100;

                                            int minAmount;
                                            long minDelta;
                                            int minDigits;

                                            long current = radi_min;

                                            Random rnd = new Random(1238426235L * num);

                                            final long[] tsolution = new long[50];
                                            final long[] tsolutionR = new long[50];
                                            final long[] tsolutionR2 = new long[50];

                                            final long width = maxR - min_r;

                                            final long[] rndSet;
                                            final int size = existanceMap.keySet().size();
                                            if (size == 0 && rs.isUsingExistingPrimitives()) {
                                                return;
                                            }
                                            {
                                                int i = 0;
                                                rndSet  = new long[size];
                                                for(int in : existanceMap.keySet())
                                                {
                                                    rndSet[i] = in;
                                                    i = i + 1;
                                                }
                                            }
                                            long start = System.currentTimeMillis();
                                            while (!m.isCanceled()) {
                                                long s;
                                                long r;
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
                                                    continue;
                                                }

                                                amount++;
                                                if (amount < 47) {
                                                    tsolution[amount] = s;
                                                    tsolutionR[amount] = r;
                                                    tsolutionR2[amount] = current;
                                                } else {
                                                    current = radi_min;
                                                    amount = 0;
                                                    continue;
                                                }
                                                long sum = s + current;

                                                minAmount = minAmountA.get();
                                                minDelta = minDeltaA.get();
                                                minDigits = minDigitsA.get();

                                                if (sum >= radi_max || amount > minAmount) {
                                                    if (amount <= minAmount && amount < 47) {
                                                        long delta = Math.abs(sum - radi_max);
                                                        if (100000L >= delta) {
                                                            if (amount != minAmount) {
                                                                minDelta = Long.MAX_VALUE;
                                                            } else if (delta < minDelta) {
                                                                minDelta = delta;
                                                                minDigits = Integer.MAX_VALUE;
                                                            } else if (delta > minDelta) {
                                                                current = radi_min;
                                                                amount = 0;
                                                                continue;
                                                            } else {
                                                                int digits = getDigits(tsolution[amount]);
                                                                for(int i = 1; i < amount; i++) {
                                                                    digits += getDigits(tsolution[i]);
                                                                }
                                                                if (digits < minDigits) {
                                                                    minDigits = digits;
                                                                } else {
                                                                    current = radi_min;
                                                                    amount = 0;
                                                                    continue;
                                                                }
                                                            }
                                                            minAmount = amount;
                                                            if (minDigits == Integer.MAX_VALUE) {
                                                                m.subTask("Best Solution - " + minAmount + " Primitives, with " + numberFormat4f.format(new BigDecimal(delta).divide(factor, Threshold.MC).doubleValue()) + " deviation."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                                            } else {
                                                                m.subTask("Best Solution - " + minAmount + " Primitives, with " + numberFormat4f.format(new BigDecimal(delta).divide(factor, Threshold.MC).doubleValue()) + " deviation and " + minDigits + " digits."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                                            }
                                                            tlock.lock();
                                                            if (minAmount < minAmountA.get()  || minAmount == minAmountA.get() && minDelta <= minDeltaA.get()) {
                                                                for(int i = 1; i < amount; i++) {
                                                                    solution[i] = tsolution[i];
                                                                    solutionR[i] = tsolutionR[i];
                                                                    solutionR2[i] = tsolutionR2[i];
                                                                }
                                                                solution[amount] = tsolution[amount];
                                                                solutionR[amount] = tsolutionR[amount];
                                                                solutionR2[amount] = tsolutionR2[amount];
                                                                solutionR2[amount + 1] = sum;
                                                                solutionAmount[0] = amount;
                                                                minAmountA.set(minAmount);
                                                                minDeltaA.set(minDelta);
                                                                minDigitsA.set(minDigits);
                                                            }
                                                            tlock.unlock();
                                                            start = System.currentTimeMillis();
                                                        }
                                                    }
                                                    if (num == 0 && System.currentTimeMillis() - start > Math.max(40000 / chunks, 6000)) {
                                                        minAmount--;
                                                        minAmountA.set(minAmount);
                                                        start = System.currentTimeMillis();
                                                    }
                                                    if (minAmount < 1) break;
                                                    current = radi_min;
                                                    amount = 0;
                                                } else {
                                                    current = sum;
                                                }
                                            }
                                        }
                                    }
                                });
                                threads[j].start();
                            }
                            boolean isRunning = true;
                            while (isRunning) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    throw new LDPartEditorException(ie);
                                }
                                isRunning = false;
                                for (Thread thread : threads) {
                                    if (thread.isAlive())
                                        isRunning = true;
                                }
                            }
                        }


                    }

                    private int getDigits(long l) {
                        if (l < 1) return 1;
                        int result = 0;
                        while (100000000L > l) {
                            l *= 10L;
                            result++;
                        }
                        char[] ca = Long.toString(l, 10).toCharArray();
                        final int start = ca.length - 1;
                        for (int i = start; i > 0; i--) {
                            if (ca[i] != '0') {
                                return result + ca.length - (start - i);
                            }
                        }
                        return result + ca.length;
                    }
                });
            } catch (InvocationTargetException consumed) {
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            } catch (Exception ex) {
            }


            // TODO The solution needs to be evaluated here
            if (solutionAmount[0] == 0) {
                if (!rs.isCreatingNothingOnNoSolution()) {

                    // We have to generate the shape here:
                    // Quote: "The algorithm to get proper values for primitives is sine(angle)
                    // rounded to 4 decimal places, multiplied by radius"
                    // This is really important an must be considered!

                    int numFaces = rs.isUsingHiRes() ? 48 : 16;
                    double deltaA = Math.PI * 2.0 / numFaces;
                    double a = deltaA;
                    numFaces = (int) Math.round(numFaces * ((rs.getAngle() + 1.0) / 48.0));
                    final boolean ignoreLastCondline = (rs.isUsingHiRes() ? 48 : 16) != numFaces;
                    BigDecimal r1 = rs.getRadius1().compareTo(rs.getRadius2()) < 0 ? rs.getRadius1() : rs.getRadius2();
                    BigDecimal r2 = rs.getRadius1().compareTo(rs.getRadius2()) < 0 ? rs.getRadius2() : rs.getRadius1();
                    BigDecimal y = BigDecimal.ZERO;
                    if (rs.isUsingCones()) {
                        y = rs.getHeight();
                    }
                    BigDecimal px1 = r1;
                    BigDecimal pz1 = BigDecimal.ZERO;
                    BigDecimal px2 = r2;
                    BigDecimal pz2 = BigDecimal.ZERO;
                    for (int i = 0; i < numFaces; i++) {
                        BigDecimal x1 = new BigDecimal(Math.cos(a) + "", Threshold.MC); //$NON-NLS-1$
                        x1 = x1.setScale(4, RoundingMode.HALF_UP);
                        x1 = x1.multiply(r1);
                        BigDecimal z1 = new BigDecimal(Math.sin(a) + "", Threshold.MC); //$NON-NLS-1$
                        z1 = z1.setScale(4, RoundingMode.HALF_UP);
                        z1 = z1.multiply(r1);
                        BigDecimal x2 = new BigDecimal(Math.cos(a) + "", Threshold.MC); //$NON-NLS-1$
                        x2 = x2.setScale(4, RoundingMode.HALF_UP);
                        x2 = x2.multiply(r2);
                        BigDecimal z2 = new BigDecimal(Math.sin(a) + "", Threshold.MC); //$NON-NLS-1$
                        z2 = z2.setScale(4, RoundingMode.HALF_UP);
                        z2 = z2.multiply(r2);
                        String line3 = "4 16 " + px1 + " 0 " + pz1 + " " + x1 + " 0 " + z1 + " " + x2 + " " + y + " " + z2 + " " + px2 + " " + y + " " + pz2;  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
                        GData quad = DatParser.parseLine(line3
                                , -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, df, false,
                                new HashSet<>()).get(0).getGraphicalData();
                        if (quad != null) {
                            vm.getSelectedData().add(quad);
                            vm.getSelectedQuads().add((GData4) quad);
                            df.addToTailOrInsertAfterCursor(quad);
                        }

                        if (rs.isUsingCones() && ignoreLastCondline && i < numFaces - 1) {
                            BigDecimal nx = new BigDecimal(Math.cos(a + deltaA));
                            nx = nx.setScale(4, RoundingMode.HALF_UP);
                            nx = nx.multiply(r1);
                            BigDecimal nz = new BigDecimal(Math.sin(a + deltaA));
                            nz = nz.setScale(4, RoundingMode.HALF_UP);
                            nz = nz.multiply(r1);

                            String line5 = "5 24 " + x1 + " 0 " + z1 + " " + x2 + " " + y + " " + z2 + " " + nx + " 0 " + nz + " " + px1 + " 0 " + pz1;  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
                            GData condline = DatParser.parseLine(line5
                                    , -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, df, false,
                                    new HashSet<>()).get(0).getGraphicalData();
                            if (condline != null) {
                                vm.getSelectedData().add(condline);
                                vm.getSelectedCondlines().add((GData5) condline);
                                df.addToTailOrInsertAfterCursor(condline);
                            }
                        }

                        px1 = x1;
                        pz1 = z1;
                        px2 = x2;
                        pz2 = z2;
                        a = a + deltaA;
                    }

                } else {

                    MessageBox messageBoxError = new MessageBox(sh, SWT.ICON_INFORMATION | SWT.OK);
                    messageBoxError.setText(I18n.DIALOG_INFO);
                    messageBoxError.setMessage(I18n.RCONES_NO_SOLUTION);
                    messageBoxError.open();

                    return;
                }
            } else {

                String anglePrefix = rs.getAngles().get(angle);
                anglePrefix = anglePrefix.substring(0, anglePrefix.indexOf(" ")); //$NON-NLS-1$

                if (rs.isUsingHiRes()) {
                    anglePrefix = "48\\" + anglePrefix; //$NON-NLS-1$
                }

                BigDecimal height = BigDecimal.ZERO;
                BigDecimal step = BigDecimal.ONE;

                BigDecimal width =  new BigDecimal(solutionR2[solutionAmount[0] + 1] - solutionR2[1]);

                if (rs.isUsingCones()) {
                    height = BigDecimal.ZERO;
                }
                for(int i = 1; i <= solutionAmount[0]; i++) {
                    BigDecimal sf = new BigDecimal(solution[i]).divide(factor, Threshold.MC);
                    String sfs = bigDecimalToString(sf);
                    String radiusSuffix = "" + solutionR[i]; //$NON-NLS-1$

                    String middle;
                    if (rs.isUsingCones()) {
                        step = new BigDecimal(solutionR2[i + 1] - solutionR2[i]).divide(width, Threshold.MC).multiply(rs.getHeight());
                        middle = "con"; //$NON-NLS-1$
                        height = height.add(step);
                    } else {
                        middle = "ring"; //$NON-NLS-1$
                        if (rs.isUsingHiRes()) {
                            if (anglePrefix.length() + radiusSuffix.length() > 7) {
                                middle = middle.substring(0, 3);
                            }
                        } else {
                            if (anglePrefix.length() + radiusSuffix.length() > 4) {
                                middle = middle.substring(0, 3);
                            }
                        }
                    }


                    String line = "1 16 0 " + bigDecimalToString(height) + " 0 " + sfs + " 0 0 0 " + step.negate() + " 0 0 0 " + sfs + " " + anglePrefix + middle + radiusSuffix + ".dat";     //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$
                    GData gd = DatParser
                            .parseLine(line
                                    , -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, df, false,
                                    new HashSet<>()).get(0).getGraphicalData();
                    if (gd == null) {
                        gd = new GData0(line, View.DUMMY_REFERENCE);
                    } else {
                        vm.getSelectedData().add(gd);
                        vm.getSelectedSubfiles().add((GData1) gd);
                    }
                    df.addToTailOrInsertAfterCursor(gd);
                }
            }
        } else {
            String anglePrefix = rs.getAngles().get(rs.getAngle());
            anglePrefix = anglePrefix.substring(0, anglePrefix.indexOf(" ")); //$NON-NLS-1$
            if (rs.isUsingHiRes()) {
                anglePrefix = "48\\" + anglePrefix; //$NON-NLS-1$
            }
            String sfs = bigDecimalToString(rs.getRadius1().compareTo(rs.getRadius2()) < 0 ? rs.getRadius2() : rs.getRadius1());
            final String line;
            if (rs.isUsingCones()) {
                line = "1 16 0 " + bigDecimalToString(rs.getHeight()) + " 0 " + sfs + " 0 0 0 " + bigDecimalToString(rs.getHeight().negate()) + " 0 0 0 " + sfs + " " + anglePrefix + "con0.dat";     //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$
            } else {
                line = "1 16 0 0 0 " + sfs + " 0 0 0 1 0 0 0 " + sfs + " " + anglePrefix + "disc.dat";     //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
            }
            GData gd = DatParser
                    .parseLine(line
                            , -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, df, false,
                            new HashSet<>()).get(0).getGraphicalData();
            if (gd == null) {
                if (!rs.isCreatingNothingOnNoSolution()) {

                    // We have to generate the shape here:
                    // Quote: "The algorithm to get proper values for primitives is sine(angle)
                    // rounded to 4 decimal places, multiplied by radius"
                    // This is really important an must be considered!

                    int numFaces = rs.isUsingHiRes() ? 48 : 16;
                    double deltaA = Math.PI * 2.0 / numFaces;
                    double a = deltaA;
                    numFaces = (int) Math.round(numFaces * ((rs.getAngle() + 1.0) / 48.0));
                    final boolean ignoreLastCondline = (rs.isUsingHiRes() ? 48 : 16) != numFaces;
                    BigDecimal r = rs.getRadius1().compareTo(rs.getRadius2()) < 0 ? rs.getRadius2() : rs.getRadius1();
                    BigDecimal y = BigDecimal.ZERO;
                    if (rs.isUsingCones()) {
                        y = rs.getHeight();
                    }
                    BigDecimal px = r;
                    BigDecimal pz = BigDecimal.ZERO;
                    for (int i = 0; i < numFaces; i++) {
                        BigDecimal x = new BigDecimal(Math.cos(a) + "", Threshold.MC); //$NON-NLS-1$
                        x = x.setScale(4, RoundingMode.HALF_UP);
                        x = x.multiply(r);
                        BigDecimal z = new BigDecimal(Math.sin(a) + "", Threshold.MC); //$NON-NLS-1$
                        z = z.setScale(4, RoundingMode.HALF_UP);
                        z = z.multiply(r);
                        String line3 = "3 16 0 0 0 " + px + " " + y + " " + pz + " " + x + " " + y + " " + z;  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                        GData tri = DatParser.parseLine(line3
                                , -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, df, false,
                                new HashSet<>()).get(0).getGraphicalData();
                        if (tri != null) {
                            vm.getSelectedData().add(tri);
                            vm.getSelectedTriangles().add((GData3) tri);
                            df.addToTailOrInsertAfterCursor(tri);
                        }

                        if (rs.isUsingCones() && ignoreLastCondline && i < numFaces - 1) {
                            BigDecimal nx = new BigDecimal(Math.cos(a + deltaA));
                            nx = nx.setScale(4, RoundingMode.HALF_UP);
                            nx = nx.multiply(r);
                            BigDecimal nz = new BigDecimal(Math.sin(a + deltaA));
                            nz = nz.setScale(4, RoundingMode.HALF_UP);
                            nz = nz.multiply(r);

                            String line5 = "5 24 0 0 0 " + px + " " + y + " " + pz + " " + x + " " + y + " " + z + " " + nx + " " + y + " " + nz;  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
                            GData condline = DatParser.parseLine(line5
                                    , -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, df, false,
                                    new HashSet<>()).get(0).getGraphicalData();
                            if (condline != null) {
                                vm.getSelectedData().add(condline);
                                vm.getSelectedCondlines().add((GData5) condline);
                                df.addToTailOrInsertAfterCursor(condline);
                            }
                        }

                        px = x;
                        pz = z;
                        a = a + deltaA;
                    }


                } else if (!rs.isUsingExistingPrimitives()) {
                    df.addToTailOrInsertAfterCursor(new GData0(line, View.DUMMY_REFERENCE));
                } else {
                    MessageBox messageBoxError = new MessageBox(sh, SWT.ICON_INFORMATION | SWT.OK);
                    messageBoxError.setText(I18n.DIALOG_INFO);
                    messageBoxError.setMessage(I18n.RCONES_NO_SOLUTION);
                    messageBoxError.open();
                    return;
                }
            } else {
                vm.getSelectedData().add(gd);
                vm.getSelectedSubfiles().add((GData1) gd);
                df.addToTailOrInsertAfterCursor(gd);
            }
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

        if (syncWithTextEditor) {
            vm.setModified(true, true);
            vm.validateState();
        } else {
            vm.setModifiedNoSync();
        }

    }

    private static void initExistanceMap(boolean cones, boolean hiRes) {
        existanceMap.clear();

        Pattern coneP = Pattern.compile("\\d+\\-\\d+con\\d{1,2}\\.dat"); //$NON-NLS-1$
        Pattern ringP = Pattern.compile("\\d+\\-\\d+ring{0,1}\\d{1,2}\\.dat"); //$NON-NLS-1$

        StringBuilder upper = new StringBuilder();
        StringBuilder lower = new StringBuilder();
        StringBuilder number = new StringBuilder();

        for (Primitive p2 : prims) {
            for (Primitive p : p2.getAllPrimitives()) {
                if (!p.isCategory()) {
                    String name = p.getName();
                    NLogger.debug(RingsAndCones.class, "Checking content of {0}", name); //$NON-NLS-1$
                    if (name.startsWith("48\\")) {//$NON-NLS-1$
                        if (!hiRes) continue;
                        name = name.substring(3);
                    } else if (hiRes) {
                        continue;
                    }
                    if (cones && coneP.matcher(name).matches() || !cones && ringP.matcher(name).matches()) {

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
                            char[] charsThis = name.toCharArray();
                            for (char c : charsThis) {
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
                                for (int i = charsThis.length - 1; i > 0 ; i--) {
                                    char c = charsThis[i];
                                    if (Character.isDigit(c)) {
                                        number = number.insert(0, c);
                                        readDigit = true;
                                    } else if (readDigit) {
                                        break;
                                    } else if (i < charsThis.length - 5) {
                                        break;
                                    }
                                }
                                if (readDigit) {
                                    try {
                                        int index = (int) (48.0 * Double.parseDouble(upper.toString()) / Double.parseDouble(lower.toString())) - 1;
                                        int radius = Integer.parseInt(number.toString());
                                        if (radius > 0) {
                                            if (existanceMap.containsKey(radius)) {
                                                existanceMap.get(radius)[index] = true;
                                            } else {
                                                existanceMap.put(radius, new boolean[48]);
                                                existanceMap.get(radius)[index] = true;
                                            }
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

    private static String bigDecimalToString(BigDecimal bd) {
        String result;
        if (bd.compareTo(BigDecimal.ZERO) == 0)
            return "0"; //$NON-NLS-1$
        BigDecimal bd2 = bd.stripTrailingZeros();
        result = bd2.toPlainString();
        if (result.startsWith("-0."))return "-" + result.substring(2); //$NON-NLS-1$ //$NON-NLS-2$
        if (result.startsWith("0."))return result.substring(1); //$NON-NLS-1$
        return result;
    }
}

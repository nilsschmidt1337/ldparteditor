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
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.enums.Threshold;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.RingsAndConesSettings;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.DatParser;

/**
 * @author nils
 *
 */
public enum RingsAndCones {
    INSTANCE;


    private static List<Primitive> prims = null;

    private static Map<Integer, boolean[]> existanceMap = new HashMap<Integer, boolean[]>();

    public static void solve(Shell sh, final DatFile df, final ArrayList<Primitive> allPrimitives, final RingsAndConesSettings rs, boolean syncWithTextEditor) {

        final VertexManager vm = df.getVertexManager();

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
            initExistanceMap(rs.isUsingCones(), rs.isUsingHiRes());
        }

        final int[] solutionAmount = new int[]{0};
        final long[] solution = new long[50];
        final long[] solutionR = new long[50];
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
                        final AtomicInteger min_amountA = new AtomicInteger(Integer.MAX_VALUE);
                        final AtomicLong min_deltaA = new AtomicLong(Long.MAX_VALUE);
                        final AtomicInteger min_digitsA = new AtomicInteger(Integer.MAX_VALUE);

                        final Lock tlock = new ReentrantLock();

                        final int chunks = View.NUM_CORES;
                        final Thread[] threads = new Thread[chunks];
                        for (int j = 0; j < chunks; ++j) {
                            final long num = j;
                            threads[j] = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    {
                                        int amount = 0;


                                        final long min_r = 1;
                                        long max_r = 100;

                                        int min_amount = 100;
                                        long min_delta = Long.MAX_VALUE;
                                        int min_digits = Integer.MAX_VALUE;

                                        long current = radi_min;

                                        Random rnd = new Random(1238426235L * num);

                                        final long[] tsolution = new long[50];
                                        final long[] tsolutionR = new long[50];

                                        final long width = max_r - min_r;

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
                                                tsolutionR[amount] = r;
                                            } else {
                                                current = radi_min;
                                                amount = 0;
                                                continue;
                                            }
                                            long sum = s + current;

                                            min_amount = min_amountA.get();
                                            min_delta = min_deltaA.get();
                                            min_digits = min_digitsA.get();

                                            if (sum >= radi_max || amount > min_amount) {
                                                if (amount <= min_amount && amount < 47) {
                                                    long delta = Math.abs(sum - radi_max);
                                                    if (100000L >= delta) {
                                                        if (amount != min_amount) {
                                                            min_delta = Long.MAX_VALUE;
                                                        } else if (delta < min_delta) {
                                                            min_delta = delta;
                                                            min_digits = Integer.MAX_VALUE;
                                                        } else if (delta > min_delta) {
                                                            current = radi_min;
                                                            amount = 0;
                                                            continue;
                                                        } else {
                                                            int digits = getDigits(tsolution[amount]);
                                                            for(int i = 1; i < amount; i++) {
                                                                digits += getDigits(tsolution[i]);
                                                            }
                                                            if (digits < min_digits) {
                                                                min_digits = digits;
                                                            } else {
                                                                current = radi_min;
                                                                amount = 0;
                                                                continue;
                                                            }
                                                        }
                                                        min_amount = amount;
                                                        if (min_digits == Integer.MAX_VALUE) {
                                                            m.subTask("Best Solution - " + min_amount + " Primitives, with " + View.NUMBER_FORMAT4F.format(new BigDecimal(delta).divide(factor, Threshold.mc).doubleValue()) + " deviation."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                                        } else {
                                                            m.subTask("Best Solution - " + min_amount + " Primitives, with " + View.NUMBER_FORMAT4F.format(new BigDecimal(delta).divide(factor, Threshold.mc).doubleValue()) + " deviation and " + min_digits + " digits."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                                        }
                                                        tlock.lock();
                                                        if (min_amount < min_amountA.get()  || min_amount == min_amountA.get() && min_delta <= min_deltaA.get()) {
                                                            for(int i = 1; i < amount; i++) {
                                                                solution[i] = tsolution[i];
                                                                solutionR[i] = tsolutionR[i];
                                                            }
                                                            solution[amount] = tsolution[amount];
                                                            solutionR[amount] = tsolutionR[amount];
                                                            solutionAmount[0] = amount;
                                                            min_amountA.set(min_amount);
                                                            min_deltaA.set(min_delta);
                                                            min_digitsA.set(min_digits);
                                                        }
                                                        tlock.unlock();
                                                        start = System.currentTimeMillis();
                                                    }
                                                }
                                                if (num == 0 && System.currentTimeMillis() - start > Math.max(40000 / chunks, 6000)) {
                                                    min_amount--;
                                                    min_amountA.set(min_amount);
                                                    start = System.currentTimeMillis();
                                                }
                                                if (min_amount < 1) break;
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
                            } catch (InterruptedException e) {
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

            String anglePrefix = rs.getAngles().get(angle);
            anglePrefix = anglePrefix.substring(0, anglePrefix.indexOf(" ")); //$NON-NLS-1$

            if (rs.isUsingHiRes()) {
                anglePrefix = "48\\" + anglePrefix; //$NON-NLS-1$
            }

            BigDecimal height = BigDecimal.ZERO;
            BigDecimal step = BigDecimal.ONE;
            if (rs.isUsingCones()) {
                step = rs.getHeight().divide(new BigDecimal(solutionAmount[0]), Threshold.mc).negate();
                height = BigDecimal.ONE;
            }
            for(int i = 1; i <= solutionAmount[0]; i++) {
                BigDecimal sf = new BigDecimal(solution[i]).divide(factor, Threshold.mc);
                String sfs = bigDecimalToString(sf);
                String radiusSuffix = "" + solutionR[i]; //$NON-NLS-1$

                String middle;
                if (rs.isUsingCones()) {
                    height = height.add(step);
                    middle = "con"; //$NON-NLS-1$
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
                                , -1, 0, 0.5f, 0.5f, 0.5f, 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, df, false,
                                new HashSet<String>(), false).get(0).getGraphicalData();
                if (gd == null) {
                    gd = new GData0(line);
                }
                df.addToTail(gd);
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

        vm.setModified_NoSync();
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
                    NLogger.debug(RingsAndCones.class, name);
                    if (name.startsWith("48\\")) {//$NON-NLS-1$
                        if (!hiRes) continue;
                        name = name.substring(3);
                    } else if (hiRes) {
                        continue;
                    }
                    if (cones && coneP.matcher(name).matches() || ringP.matcher(name).matches()) {

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

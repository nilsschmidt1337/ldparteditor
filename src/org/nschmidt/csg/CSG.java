/**
 * CSG.java
 *
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info@michaelhoffer.de>.
 */
package org.nschmidt.csg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.widgets.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData3;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.GuiStatusManager;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * Constructive Solid Geometry (CSG).
 *
 * This implementation is a Java port of <a
 * href="https://github.com/evanw/csg.js/">https://github.com/evanw/csg.js/</a>
 * with some additional features like polygon extrude, transformations etc.
 * Thanks to the author for creating the CSG.js library.<br>
 * <br>
 *
 * <b>Implementation Details</b>
 *
 * All CSG operations are implemented in terms of two functions,
 * {@link Node#clipTo(org.nschmidt.csg.Node)} and {@link Node#invert()}, which
 * remove parts of a BSP tree inside another BSP tree and swap solid and empty
 * space, respectively. To find the union of {@code a} and {@code b}, we want to
 * remove everything in {@code a} inside {@code b} and everything in {@code b}
 * inside {@code a}, then combine polygons from {@code a} and {@code b} into one
 * solid:
 *
 * <blockquote>
 *
 * <pre>
 * a.clipTo(b);
 * b.clipTo(a);
 * a.build(b.allPolygons());
 * </pre>
 *
 * </blockquote>
 *
 * The only tricky part is handling overlapping coplanar polygons in both trees.
 * The code above keeps both copies, but we need to keep them in one tree and
 * remove them in the other tree. To remove them from {@code b} we can clip the
 * inverse of {@code b} against {@code a}. The code for union now looks like
 * this:
 *
 * <blockquote>
 *
 * <pre>
 * a.clipTo(b);
 * b.clipTo(a);
 * b.invert();
 * b.clipTo(a);
 * b.invert();
 * a.build(b.allPolygons());
 * </pre>
 *
 * </blockquote>
 *
 * Subtraction and intersection naturally follow from set operations. If union
 * is {@code A | B}, difference is {@code A - B = ~(~A | B)} and intersection
 * is {@code A & B =
 * ~(~A | ~B)} where {@code ~} is the complement operator.
 */
public class CSG {

    TreeMap<GData3, IdAndPlane> result = new TreeMap<>();

    private List<Polygon> polygons;
    private Bounds bounds = null;

    private CSG() {
        globalOptimizationRate = 100.0;
    }

    public static final byte UNION = 0;
    public static final byte DIFFERENCE = 1;
    public static final byte INTERSECTION = 2;
    public static final byte CUBOID = 3;
    public static final byte ELLIPSOID = 4;
    public static final byte QUAD = 5;
    public static final byte CYLINDER = 6;
    public static final byte CIRCLE = 7;
    public static final byte COMPILE = 8;
    public static final byte QUALITY = 9;
    public static final byte EPSILON = 10;
    public static final byte CONE = 11;
    public static final byte TRANSFORM = 12;
    public static final byte MESH = 13;
    public static final byte EXTRUDE = 14;
    public static final byte EXTRUDE_CFG = 15;
    public static final byte TJUNCTION = 16;
    public static final byte COLLAPSE = 17;
    public static final byte DONTOPTIMIZE = 18;

    /**
     * Constructs a CSG from a list of {@link Polygon} instances.
     *
     * @param polygons
     *            polygons
     * @return a CSG instance
     */
    public static CSG fromPolygons(List<Polygon> polygons) {

        CSG csg = new CSG();
        csg.polygons = polygons;
        return csg;
    }

    /**
     * Constructs a CSG from the specified {@link Polygon} instances.
     *
     * @param polygons
     *            polygons
     * @return a CSG instance
     */
    public static CSG fromPolygons(Polygon... polygons) {
        return fromPolygons(Arrays.asList(polygons));
    }

    @Override
    public CSG clone() {
        CSG csg = new CSG();

        csg.polygons = new ArrayList<Polygon>();
        for (Polygon polygon : polygons) {
            csg.polygons.add(polygon.clone());
        }

        return csg;
    }

    /**
     *
     * @return the polygons of this CSG
     */
    public List<Polygon> getPolygons() {
        return polygons;
    }

    /**
     * Return a new CSG solid representing the union of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are modified.
     *
     * <blockquote>
     *
     * <pre>
     *    A.union(B)
     *
     *    +-------+            +-------+
     *    |       |            |       |
     *    |   A   |            |       |
     *    |    +--+----+   =   |       +----+
     *    +----+--+    |       +----+       |
     *         |   B   |            |       |
     *         |       |            |       |
     *         +-------+            +-------+
     * </pre>
     *
     * </blockquote>
     *
     *
     * @param csg
     *            other csg
     *
     * @return union of this csg and the specified csg
     */
    public CSG union(CSG csg) {

        final List<Polygon> thisPolys = this.clone().polygons;
        final List<Polygon> otherPolys = csg.clone().polygons;
        final Bounds thisBounds = this.getBounds();
        final Bounds otherBounds = csg.getBounds();

        final List<Polygon> nonIntersectingPolys = new ArrayList<>();

        thisPolys.removeIf((poly) -> {
            final boolean result;
            if (result = !otherBounds.intersects(poly.getBounds())) {
                nonIntersectingPolys.add(poly);
            }
            return result;
        });

        otherPolys.removeIf((poly) -> {
            final boolean result;
            if (result = !thisBounds.intersects(poly.getBounds())) {
                nonIntersectingPolys.add(poly);
            }
            return result;
        });

        CompletableFuture<Node> f1 = CompletableFuture.supplyAsync(() -> new Node(thisPolys));
        CompletableFuture<Node> f2 = CompletableFuture.supplyAsync(() -> new Node(otherPolys));
        CompletableFuture.allOf(f1, f2).join();

        try {
            Node a = f1.get();
            Node b = f2.get();

            a.clipTo(b);
            b.clipTo(a);
            b.invert();
            b.clipTo(a);
            b.invert();

            final List<Node> nodes = new ArrayList<>();
            final Stack<NodePolygon> st = new Stack<>();
            st.push(new NodePolygon(a, b.allPolygons(new ArrayList<>())));
            while (!st.isEmpty()) {
                NodePolygon np = st.pop();
                Node n = np.getNode();
                nodes.add(n);
                List<NodePolygon> npr = n.buildForResult(np.getPolygons());
                for (NodePolygon np2 : npr) {
                    st.push(np2);
                }
            }

            final List<Polygon> resultPolys = a.allPolygons(nonIntersectingPolys);
            return CSG.fromPolygons(resultPolys);
        } catch (ExecutionException | InterruptedException e) {
            // Exceptions sollten (tm) schon im "join" geworfen worden sein.
            NLogger.error(getClass(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Return a new CSG solid representing the difference of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are modified.
     *
     * <blockquote>
     *
     * <pre>
     * A.difference(B)
     *
     * +-------+            +-------+
     * |       |            |       |
     * |   A   |            |       |
     * |    +--+----+   =   |    +--+
     * +----+--+    |       +----+
     *      |   B   |
     *      |       |
     *      +-------+
     * </pre>
     *
     * </blockquote>
     *
     * @param csg
     *            other csg
     * @return difference of this csg and the specified csg
     */
    public CSG difference(CSG csg) {

        final List<Polygon> thisPolys = this.clone().polygons;
        final List<Polygon> otherPolys = csg.clone().polygons;
        final Bounds thisBounds = this.getBounds();
        final Bounds otherBounds = csg.getBounds();

        final List<Polygon> nonIntersectingPolys = new ArrayList<>();

        thisPolys.removeIf((poly) -> {
            final boolean result;
            if (result = !otherBounds.intersects(poly.getBounds())) {
                nonIntersectingPolys.add(poly);
            }
            return result;
        });

        otherPolys.removeIf((poly) -> {
            return !thisBounds.intersects(poly.getBounds());
        });

        CompletableFuture<Node> f1 = CompletableFuture.supplyAsync(() -> new Node(thisPolys));
        CompletableFuture<Node> f2 = CompletableFuture.supplyAsync(() -> new Node(otherPolys));
        CompletableFuture.allOf(f1, f2).join();

        Node a = null;
        Node b = null;

        try {
            a = f1.get();
            b = f2.get();
        } catch (ExecutionException e) {
            NLogger.error(getClass(), e);
        } catch (InterruptedException e) {
            NLogger.error(getClass(), e);
        }

        a.invert();
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();

        Stack<NodePolygon> st = new Stack<>();
        st.push(new NodePolygon(a, b.allPolygons(new ArrayList<>())));
        while (!st.isEmpty()) {
            NodePolygon np = st.pop();
            List<NodePolygon> npr = np.getNode().buildForResult(np.getPolygons());
            for (NodePolygon np2 : npr) {
                st.push(np2);
            }
        }

        a.invert();

        final List<Polygon> resultPolys = a.allPolygons(nonIntersectingPolys);
        return CSG.fromPolygons(resultPolys);
    }

    /**
     * Return a new CSG solid representing the intersection of this csg and the
     * specified csg.
     *
     * <b>Note:</b> Neither this csg nor the specified csg are modified.
     *
     * <blockquote>
     *
     * <pre>
     *     A.intersect(B)
     *
     *     +-------+
     *     |       |
     *     |   A   |
     *     |    +--+----+   =   +--+
     *     +----+--+    |       +--+
     *          |   B   |
     *          |       |
     *          +-------+
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @param csg
     *            other csg
     * @return intersection of this csg and the specified csg
     */
    public CSG intersect(CSG csg) {

        CompletableFuture<Node> f1 = CompletableFuture.supplyAsync(() -> new Node(this.clone().polygons));
        CompletableFuture<Node> f2 = CompletableFuture.supplyAsync(() -> new Node(csg.clone().polygons));
        CompletableFuture.allOf(f1, f2).join();

        Node a = null;
        Node b = null;

        try {
            a = f1.get();
            b = f2.get();
        } catch (ExecutionException e) {
            NLogger.error(getClass(), e);
        } catch (InterruptedException e) {
            NLogger.error(getClass(), e);
        }

        a.invert();
        b.clipTo(a);
        b.invert();
        a.clipTo(b);
        b.clipTo(a);

        Stack<NodePolygon> st = new Stack<>();
        st.push(new NodePolygon(a, b.allPolygons(new ArrayList<>())));
        while (!st.isEmpty()) {
            NodePolygon np = st.pop();
            List<NodePolygon> npr = np.getNode().buildForResult(np.getPolygons());
            for (NodePolygon np2 : npr) {
                st.push(np2);
            }
        }

        a.invert();
        return CSG.fromPolygons(a.allPolygons(new ArrayList<>()));
    }

    /**
     * Returns this csg as list of LDraw triangles
     *
     * @return this csg as list of LDraw triangles
     */
    public TreeMap<GData3, IdAndPlane> toLDrawTriangles(GData1 parent) {
        TreeMap<GData3, IdAndPlane> result = new TreeMap<>();
        for (Polygon p : this.polygons) {
            result.putAll(p.toLDrawTriangles(parent));
        }
        return result;
    }

    public GData1 compile() {
        Matrix4f id = new Matrix4f();
        Matrix4f.setIdentity(id);
        GColour col = View.getLDConfigColour(16);
        GData1 g1 = new GData1(-1, col.getR(), col.getG(), col.getB(), 1f, id, View.ACCURATE_ID, new ArrayList<String>(), null, null, 1, false, id, View.ACCURATE_ID, null, View.DUMMY_REFERENCE, true, false,
                new HashSet<String>(), View.DUMMY_REFERENCE);
        this.result = toLDrawTriangles(g1);
        return g1;
    }

    public void draw(Composite3D c3d, DatFile df) {
        for (GData3 tri : getResult(df).keySet()) {
            tri.drawGL20(c3d);
        }
    }

    public void draw_textured(Composite3D c3d, DatFile df) {
        for (GData3 tri : getResult(df).keySet()) {
            tri.drawGL20_BFC_Textured(c3d);
        }
    }

    private volatile boolean shouldOptimize = true;
    private volatile TreeMap<GData3, IdAndPlane> optimizedResult = null;
    private volatile TreeMap<GData3, IdAndPlane> optimizedTriangles = new TreeMap<>();
    private final Random rnd = new Random(12345678L);
    public static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static volatile long timeOfLastOptimization = -1;
    public static volatile double globalOptimizationRate = 100.0;

    public volatile double optimizationTries = 1.0;
    public volatile double optimizationSuccess = 1.0;
    public volatile double failureStrike = 0;
    private volatile int tjunctionPause = 0;
    private volatile int flipPause = 0;

    private final Map<GData3, Map<GData3, Boolean>> flipCache = new HashMap<>();

    public TreeMap<GData3, IdAndPlane> getResult(DatFile df) {

        if (optimizedTriangles.isEmpty() && df != null && df.isOptimizingCSG()) {
            optimizedTriangles = new TreeMap<>();
            optimizedTriangles.putAll(result);
        }

        if (shouldOptimize && df != null && df.isOptimizingCSG()) {
            final Composite3D lastC3d = DatFile.getLastHoveredComposite();
            if (lastC3d != null) {
                Display.getDefault().asyncExec(() -> {GuiStatusManager.updateStatus(lastC3d);});
            }

            shouldOptimize = false;
            executorService.execute(() -> {

                TreeMap<GData3, IdAndPlane> optimization = new TreeMap<>();
                if (optimizedResult != null) {
                    optimization.putAll(optimizedResult);
                } else {
                    optimization.putAll(optimizedTriangles);
                }

                // Optimize for each plane
                Map<Plane, List<GData3>> trianglesPerPlane = new TreeMap<>();
                List<GData3> obsoleteTriangles = new ArrayList<>();
                for (Entry<GData3, IdAndPlane> entry : optimization.entrySet()) {
                    IdAndPlane id = entry.getValue();
                    if (id == null) {
                        obsoleteTriangles.add(entry.getKey());
                        continue;
                    }
                    final Plane p = id.plane;
                    List<GData3> triangles = trianglesPerPlane.get(p);
                    if (triangles == null) {
                        triangles = new ArrayList<>();
                        triangles.add(entry.getKey());
                        trianglesPerPlane.put(p, triangles);
                    } else {
                        triangles.add(entry.getKey());
                    }
                }
                for (GData3 g : obsoleteTriangles) {
                    optimization.remove(g);
                }

                int action = rnd.nextInt(3);
                boolean foundOptimization = false;

                if (action == 0 || action == 2) {
                    if (tjunctionPause > 0) {
                        tjunctionPause--;
                        action = 2;
                    } else {
                        foundOptimization = CSGOptimizerTJunction.optimize(rnd, trianglesPerPlane, optimization);
                        if (!foundOptimization) {
                            tjunctionPause = 1000;
                        }
                    }
                }

                if (action == 1) {
                    if (flipPause > 0) {
                        flipPause--;
                        action = 2;
                    } else {
                        foundOptimization = CSGOptimizerFlipTriangle.optimize(rnd, trianglesPerPlane, optimization, flipCache);
                        if (!foundOptimization) {
                            flipPause = 1000;
                        }
                    }
                }

                if (action == 2 && tjunctionPause > 0) {
                    foundOptimization = CSGOptimizerEdgeCollapse.optimize(rnd, trianglesPerPlane, optimization);
                    if (!foundOptimization) {
                        flipPause = 0;
                    }
                }

                if (foundOptimization) {
                    optimizationSuccess++;
                    failureStrike = 0;
                } else if (optimizationSuccess > 0) {
                    optimizationSuccess--;
                    if (failureStrike < 100) {
                        failureStrike++;
                    }
                }
                optimizationTries++;

                final double rate = Math.max(1.0 - optimizationSuccess / optimizationTries, failureStrike / 100.0) * 100.0;
                if (rate < 99.0 && failureStrike < 100) {
                    globalOptimizationRate = rate;
                    timeOfLastOptimization = System.currentTimeMillis();
                }

                optimizedResult = optimization;
                shouldOptimize = true;
            });
        }

        if (optimizedResult == null) {
            return result;
        } else {
            return optimizedResult;
        }
    }

    /**
     * Returns a transformed copy of this CSG.
     *
     * @param transform
     *            the transform to apply
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Transform transform) {
        List<Polygon> newpolygons = new ArrayList<Polygon>();
        for (Polygon p : polygons) {
            newpolygons.add(p.transformed(transform));
        }
        CSG result = CSG.fromPolygons(newpolygons);
        return result;
    }

    /**
     * Returns a transformed copy of this CSG.
     *
     * @param transform
     *            the transform to apply
     * @param ID
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Transform transform, GColour c, int ID) {
        List<Polygon> newpolygons = new ArrayList<Polygon>();
        for (Polygon p : polygons) {
            newpolygons.add(p.transformed(transform, c, ID));
        }
        CSG result = CSG.fromPolygons(newpolygons);
        return result;
    }

    /**
     * Returns a transformed copy of this CSG.
     *
     * @param transform
     *            the transform to apply
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Matrix4f transform) {
        return transformed(new Transform().apply(transform));
    }

    /**
     * Returns a transformed coloured, copy of this CSG.
     *
     * @param transform
     *            the transform to apply
     * @param ID
     *
     * @return a transformed copy of this CSG
     */
    public CSG transformed(Matrix4f transform, GColour c, int ID) {
        return transformed(new Transform().apply(transform), c, ID);
    }

    /**
     * Returns the bounds of this csg.
     *
     * @return bouds of this csg
     */
    public Bounds getBounds() {
        Bounds result = bounds;
        if (result == null) {
            if (!polygons.isEmpty()) {
                result = new Bounds();
                for (Polygon t : polygons) {
                    Bounds b = t.getBounds();
                    result.union(b);
                }
            } else {
                result = new Bounds(new VectorCSGd(0, 0, 0), new VectorCSGd(0, 0, 0));
            }
            bounds = result;
        }
        return result;
    }
}

/*
 * Copyright 2014 Gary W. Lucas., modified by Nils Schmidt (removed not required methods)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinfour.constrained.delaunay;

import java.util.ArrayList;
import java.util.List;

/**
 * A synthetic vertex used to handle cases when multiple vertices occupy
 * coincident locations.
 */
class PntMergerGroup extends Pnt {

    private List<Pnt> list = new ArrayList<>();

    /**
     * Constructs a coincident vertex using the specified vertex for initialization.
     *
     * @param firstVertex a valid instance
     */
    PntMergerGroup(Pnt firstVertex) {
        super(firstVertex.x, firstVertex.y);
        status = firstVertex.status;
        list.add(firstVertex);
    }

    /**
     * Add a new vertex to the coincident collection.
     *
     * @param v a valid, unique instance
     * @return true if added to collection; otherwise false
     */
    boolean addVertex(Pnt v) {
        if (v.isConstraintMember()) {
            setConstraintMember(true);
        }
        if (!v.isSynthetic()) {
            setSynthetic(false);
        }
        if (v instanceof PntMergerGroup g) {
            // put the content of the added group into
            // the existing group. it's the only way to
            // ensure that the resolution rules behave properly.
            // note that logic assumes that in general the size
            // of the groups is rather small and so performs the
            // linear search for the contains() method.
            boolean added = false;
            for (Pnt a : g.list) {
                if (!list.contains(a)) {
                    list.add(a);
                    added = true;
                }
            }
            return added;
        }
        if (list.contains(v)) {
            return false;
        }
        return list.add(v);
    }

    /**
     * Indicates whether the group contains the specified vertex
     *
     * @param v a valid vertex
     * @return true if the vertex is a member of the group; otherwise, false.
     */
    boolean contains(Pnt v) {
        return list.contains(v);
    }
}

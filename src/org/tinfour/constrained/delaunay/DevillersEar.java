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

/**
 * A representation of a "ear" from Devillers' algorithm for vertex removal
 */
class DevillersEar {

    DevillersEar prior;
    DevillersEar next;
    QuadEdge c;
    QuadEdge n;
    Pnt v0;
    Pnt v1;
    Pnt v2;

    double score;

    DevillersEar(DevillersEar priorEar, QuadEdge current) {
        this.prior = priorEar;
        if (priorEar != null) {
            priorEar.next = this;
        }
        c = current;
        n = c.getForward();
        v0 = c.getA();
        v1 = c.getB();
        v2 = n.getB();
    }
}

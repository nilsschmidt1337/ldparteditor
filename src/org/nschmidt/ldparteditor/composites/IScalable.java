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
package org.nschmidt.ldparteditor.composites;

import org.eclipse.swt.custom.SashForm;

/**
 * This is the interface which must be implemented by {@link CompositeContainer}
 * , {@link CompositeScale} and {@link Composite3D}. It is helpful for object
 * relations regarding horizontal and vertical scales.
 *
 * @author nils
 *
 */
interface IScalable {

    /**
     * Hierarchy-Note: SashForm <- CompositeContainer <- [CompositeScale] <-
     * Composite3D
     *
     * @return the {@link SashForm} in which this composite is nested.
     */
    public SashForm getSashForm();

    /**
     * Hierarchy-Note: SashForm <- CompositeContainer <- [CompositeScale] <-
     * Composite3D
     *
     * @return the {@link Composite3D} which displays all 3D stuff.
     */
    public Composite3D getComposite3D();

    /**
     * Hierarchy-Note: SashForm <- CompositeContainer <- [CompositeScale] <-
     * Composite3D
     *
     * @return the {@link CompositeContainer} which embeds a {@link Composite3D}
     *         or a {@link CompositeScale}.
     */
    public CompositeContainer getCompositeContainer();
}

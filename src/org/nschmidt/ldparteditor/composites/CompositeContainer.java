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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.enums.Perspective;

/**
 * The container for a {@link Composite3D} or a {@link CompositeScale}.
 *
 * @author nils
 */
// TODO Needs java-doc
public class CompositeContainer extends ScalableComposite {

    /**
     *
     * @param parentSashForm
     * @param compositeScale
     */
    public CompositeContainer(Composite parentSashForm, boolean scaleShown) {
        super(parentSashForm, org.eclipse.swt.SWT.NONE);
        this.setLayout(new FillLayout());
        Composite3D composite3D = new Composite3D(this);
        if (scaleShown) {
            new CompositeScale(this, composite3D, SWT.NONE);
        } else {
            composite3D.setParent(this);
        }
    }

    public CompositeContainer(Composite parentSashForm, boolean scaleShown, boolean syncManipulator, boolean syncTranslation, boolean syncZoom) {
        super(parentSashForm, org.eclipse.swt.SWT.NONE);
        this.setLayout(new FillLayout());
        Composite3D composite3D = new Composite3D(this, syncManipulator, syncTranslation, syncZoom);
        if (scaleShown) {
            new CompositeScale(this, composite3D, SWT.NONE);
        } else {
            composite3D.setParent(this);
        }
    }

    /**
     *
     * @param parentSashForm
     */
    public CompositeContainer(Composite parentSashForm) {
        super(parentSashForm, org.eclipse.swt.SWT.NONE);
        this.setLayout(new FillLayout());
        Composite3D composite3D = new Composite3D(this);
        composite3D.setParent(this);
    }

    public CompositeContainer(Composite parentSashForm, DatFile df) {
        super(parentSashForm, org.eclipse.swt.SWT.NONE);
        this.setLayout(new FillLayout());
        Composite3D composite3D = new Composite3D(this, df);
        composite3D.setParent(this);
        composite3D.getPerspectiveCalculator().setPerspective(Perspective.TOP);
    }

    @Override
    public SashForm getSashForm() {
        return (SashForm) this.getParent();
    }

    @Override
    public Composite3D getComposite3D() {
        return ((ScalableComposite) this.getChildren()[0]).getComposite3D();
    }

    @Override
    public CompositeContainer getCompositeContainer() {
        return this;
    }
}

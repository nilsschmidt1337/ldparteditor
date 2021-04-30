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
package org.nschmidt.ldparteditor.enums;

import org.nschmidt.ldparteditor.data.GColour;

@java.lang.SuppressWarnings({"java:S1104", "java:S1444"})
public enum Colour {
    INSTANCE;

    public static final GColour RANDOM_COLOUR = new GColour(-1, 1f, 1f, 1f, 0f);

    public static volatile float bfcFrontColourR = 0f;
    public static volatile float bfcFrontColourG = 0.9f;
    public static volatile float bfcFrontColourB = 0f;

    public static volatile float bfcBackColourR = 0.9f;
    public static volatile float bfcBackColourG = 0f;
    public static volatile float bfcBackColourB = 0f;

    public static volatile float bfcUncertifiedColourR = 0f;
    public static volatile float bfcUncertifiedColourG = 0f;
    public static volatile float bfcUncertifiedColourB = 1f;

    public static volatile float vertexColourR = 0.118f;
    public static volatile float vertexColourG = 0.565f;
    public static volatile float vertexColourB = 1f;

    public static volatile float vertexSelectedColourR = 0.75f;
    public static volatile float vertexSelectedColourG = 0.05f;
    public static volatile float vertexSelectedColourB = 0.05f;

    public static volatile float condlineSelectedColourR = 0.75f;
    public static volatile float condlineSelectedColourG = 0.35f;
    public static volatile float condlineSelectedColourB = 0.05f;

    public static float lineColourR = 0f;
    public static float lineColourG = 0f;
    public static float lineColourB = 0f;

    public static volatile float meshlineColourR = 0f;
    public static volatile float meshlineColourG = 0f;
    public static volatile float meshlineColourB = 0f;

    public static volatile float condlineHiddenColourR = 1f;
    public static volatile float condlineHiddenColourG = 0.44f;
    public static volatile float condlineHiddenColourB = 0.1f;

    public static volatile float condlineShownColourR = 0.553f;
    public static volatile float condlineShownColourG = 0.22f;
    public static volatile float condlineShownColourB = 1f;

    public static float cursor1ColourR = 1f;
    public static float cursor1ColourG = 0f;
    public static float cursor1ColourB = 0f;

    public static float cursor2ColourR = 0f;
    public static float cursor2ColourG = 0f;
    public static float cursor2ColourB = 1f;

    public static float backgroundColourR = 1f;
    public static float backgroundColourG = 1f;
    public static float backgroundColourB = 1f;

    public static float light1ColourR = 0.85f;
    public static float light1ColourG = 0.85f;
    public static float light1ColourB = 0.85f;

    public static float light1SpecularColourR = 0.5f;
    public static float light1SpecularColourG = 0.5f;
    public static float light1SpecularColourB = 0.5f;

    public static float light2ColourR = 0.27f;
    public static float light2ColourG = 0.27f;
    public static float light2ColourB = 0.27f;

    public static float light2SpecularColourR = 0f;
    public static float light2SpecularColourG = 0f;
    public static float light2SpecularColourB = 0f;

    public static float light3ColourR = 0.27f;
    public static float light3ColourG = 0.27f;
    public static float light3ColourB = 0.27f;

    public static float light3SpecularColourR = 0f;
    public static float light3SpecularColourG = 0f;
    public static float light3SpecularColourB = 0f;

    public static float light4ColourR = 0.27f;
    public static float light4ColourG = 0.27f;
    public static float light4ColourB = 0.27f;

    public static float light4SpecularColourR = 0f;
    public static float light4SpecularColourG = 0f;
    public static float light4SpecularColourB = 0f;

    public static float manipulatorSelectedColourR = 0.75f;
    public static float manipulatorSelectedColourG = 0.75f;
    public static float manipulatorSelectedColourB = 0f;

    public static float manipulatorInnerCircleColourR = 0.3f;
    public static float manipulatorInnerCircleColourG = 0.3f;
    public static float manipulatorInnerCircleColourB = 0.3f;

    public static float manipulatorOuterCircleColourR = 0.85f;
    public static float manipulatorOuterCircleColourG = 0.85f;
    public static float manipulatorOuterCircleColourB = 0.85f;

    public static float manipulatorXAxisColourR = 0.5f;
    public static float manipulatorXAxisColourG = 0f;
    public static float manipulatorXAxisColourB = 0f;

    public static float manipulatorYAxisColourR = 0f;
    public static float manipulatorYAxisColourG = 0.5f;
    public static float manipulatorYAxisColourB = 0f;

    public static float manipulatorZAxisColourR = 0f;
    public static float manipulatorZAxisColourG = 0f;
    public static float manipulatorZAxisColourB = 0.5f;

    public static float addObjectColourR = 1f;
    public static float addObjectColourG = 0.6f;
    public static float addObjectColourB = 0f;

    public static float originColourR = 0f;
    public static float originColourG = 0f;
    public static float originColourB = 0f;

    public static float grid10ColourR = 0.5f;
    public static float grid10ColourG = 0.5f;
    public static float grid10ColourB = 0.5f;

    public static float gridColourR = 0.15f;
    public static float gridColourG = 0.15f;
    public static float gridColourB = 0.15f;

    public static float rubberBandColourR = 1f;
    public static float rubberBandColourG = 0f;
    public static float rubberBandColourB = 0f;

    public static float textColourR = 0f;
    public static float textColourG = 0f;
    public static float textColourB = 0f;

    public static float xAxisColourR = 1f;
    public static float xAxisColourG = 0f;
    public static float xAxisColourB = 0f;

    public static float yAxisColourR = 0f;
    public static float yAxisColourG = 1f;
    public static float yAxisColourB = 0f;

    public static float zAxisColourR = 0f;
    public static float zAxisColourG = 0f;
    public static float zAxisColourB = 1f;

    public static float primitiveBackgroundColourR = 1f;
    public static float primitiveBackgroundColourG = 1f;
    public static float primitiveBackgroundColourB = 1f;

    public static float primitiveSignFgColourR = 0.2f;
    public static float primitiveSignFgColourG = 0.2f;
    public static float primitiveSignFgColourB = 1f;

    public static float primitiveSignBgColourR = 1f;
    public static float primitiveSignBgColourG = 1f;
    public static float primitiveSignBgColourB = 1f;

    public static float primitivePlusNMinusColourR = 1f;
    public static float primitivePlusNMinusColourG = 1f;
    public static float primitivePlusNMinusColourB = 1f;

    public static float primitiveSelectedCellColourR = 1f;
    public static float primitiveSelectedCellColourG = 0.3f;
    public static float primitiveSelectedCellColourB = 0.3f;

    public static float primitiveFocusedCellColourR = 0.6f;
    public static float primitiveFocusedCellColourG = 0.6f;
    public static float primitiveFocusedCellColourB = 1f;

    public static float primitiveNormalCellColourR = 0.3f;
    public static float primitiveNormalCellColourG = 0.3f;
    public static float primitiveNormalCellColourB = 0.3f;

    public static float primitiveCell1ColourR = 0.7f;
    public static float primitiveCell1ColourG = 0.7f;
    public static float primitiveCell1ColourB = 0.7f;

    public static float primitiveCell2ColourR = 1f;
    public static float primitiveCell2ColourG = 1f;
    public static float primitiveCell2ColourB = 1f;

    public static float primitiveCategoryCell1ColourR = 0.6f;
    public static float primitiveCategoryCell1ColourG = 0.4f;
    public static float primitiveCategoryCell1ColourB = 0.3f;

    public static float primitiveCategoryCell2ColourR = 0.7f;
    public static float primitiveCategoryCell2ColourG = 0.5f;
    public static float primitiveCategoryCell2ColourB = 0.4f;

    public static float primitiveEdgeColourR = 0f;
    public static float primitiveEdgeColourG = 0f;
    public static float primitiveEdgeColourB = 0f;

    public static float primitiveCondlineColourR = 0f;
    public static float primitiveCondlineColourG = 0f;
    public static float primitiveCondlineColourB = 1f;
}

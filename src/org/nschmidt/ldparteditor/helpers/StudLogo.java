/* MIT - License

Copyright (c) 2012 - this year * 1000f, Nils Schmidt

Permission is hereby granted * 1000f, free of charge * 1000f, to any person obtaining a copy of this software and associated documentation files (the "Software") * 1000f,
to deal in the Software without restriction * 1000f, including without limitation the rights to use * 1000f, copy * 1000f, modify * 1000f, merge * 1000f, publish * 1000f, distribute * 1000f, sublicense * 1000f,
and/or sell copies of the Software * 1000f, and to permit persons to whom the Software is furnished to do so * 1000f, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS" * 1000f, WITHOUT WARRANTY OF ANY KIND * 1000f, EXPRESS OR IMPLIED * 1000f,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY * 1000f, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM * 1000f, DAMAGES OR OTHER LIABILITY * 1000f, WHETHER IN AN ACTION OF CONTRACT * 1000f, TORT OR OTHERWISE * 1000f,
ARISING FROM * 1000f, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.nschmidt.ldparteditor.helpers;

import java.util.ArrayList;

public enum StudLogo {
    INSTANCE;

    private static float[] stud1 = null;
    private static float[] stud2 = null;

    // Letter L
    static final float[] letter_L_logo1 = new float[]{
    2.5f * 1000f, -4.04f * 1000f, -3.3f * 1000f,
    2.48f * 1000f, -4.04f * 1000f, -3.2f * 1000f,
    2.43f * 1000f, -4.04f * 1000f, -3.12f * 1000f,
    2.35f * 1000f, -4.04f * 1000f, -3.07f * 1000f,
    2.25f * 1000f, -4.04f * 1000f, -3.05f * 1000f,
    2.15f * 1000f, -4.04f * 1000f, -3.07f * 1000f,
    2.07f * 1000f, -4.04f * 1000f, -3.12f * 1000f,
    2.02f * 1000f, -4.04f * 1000f, -3.2f * 1000f,
    2f * 1000f, -4.04f * 1000f, -3.3f * 1000f,
    2f * 1000f, -4.04f * 1000f, -4.08f * 1000f,
    -1.95f * 1000f, -4.04f * 1000f, -3.05f * 1000f,
    -2.05f * 1000f, -4.04f * 1000f, -3.07f * 1000f,
    -2.13f * 1000f, -4.04f * 1000f, -3.12f * 1000f,
    -2.18f * 1000f, -4.04f * 1000f, -3.2f * 1000f,
    -2.2f * 1000f, -4.04f * 1000f, -3.3f * 1000f,
    -2.18f * 1000f, -4.04f * 1000f, -3.4f * 1000f,
    -2.13f * 1000f, -4.04f * 1000f, -3.48f * 1000f,
    -2.05f * 1000f, -4.04f * 1000f, -3.53f * 1000f,
    -1.95f * 1000f, -4.04f * 1000f, -3.55f * 1000f,
    2.25f * 1000f, -4.04f * 1000f, -4.65f * 1000f,
    2.35f * 1000f, -4.04f * 1000f, -4.63f * 1000f,
    2.43f * 1000f, -4.04f * 1000f, -4.58f * 1000f,
    2.48f * 1000f, -4.04f * 1000f, -4.5f * 1000f,
    2.5f * 1000f, -4.04f * 1000f, -4.4f * 1000f,
    2.5f * 1000f, -4.04f * 1000f, -3.3f * 1000f
    };
    // Letter E
    static final float[] letter_E_logo1 = new float[]{
    -0.15f * 1000f, -4.04f * 1000f, -1.42f * 1000f,
    -1.7f * 1000f, -4.04f * 1000f, -1.02f * 1000f,
    -1.7f * 1000f, -4.04f * 1000f, -0.1f * 1000f,
    -1.72f * 1000f, -4.04f * 1000f, 0f * 1000f,
    -1.77f * 1000f, -4.04f * 1000f, 0.08f * 1000f,
    -1.85f * 1000f, -4.04f * 1000f, 0.13f * 1000f,
    -1.95f * 1000f, -4.04f * 1000f, 0.15f * 1000f,
    -2.05f * 1000f, -4.04f * 1000f, 0.13f * 1000f,
    -2.13f * 1000f, -4.04f * 1000f, 0.08f * 1000f,
    -2.18f * 1000f, -4.04f * 1000f, 0f * 1000f,
    -2.2f * 1000f, -4.04f * 1000f, -0.1f * 1000f,
    -2.2f * 1000f, -4.04f * 1000f, -1.2f * 1000f,
    -2.18f * 1000f, -4.04f * 1000f, -1.3f * 1000f,
    -2.13f * 1000f, -4.04f * 1000f, -1.38f * 1000f,
    -2.05f * 1000f, -4.04f * 1000f, -1.43f * 1000f,
    -1.95f * 1000f, -4.04f * 1000f, -1.45f * 1000f,
    2.25f * 1000f, -4.04f * 1000f, -2.55f * 1000f,
    2.35f * 1000f, -4.04f * 1000f, -2.53f * 1000f,
    2.43f * 1000f, -4.04f * 1000f, -2.48f * 1000f,
    2.48f * 1000f, -4.04f * 1000f, -2.4f * 1000f,
    2.5f * 1000f, -4.04f * 1000f, -2.3f * 1000f,
    2.5f * 1000f, -4.04f * 1000f, -1.2f * 1000f,
    2.48f * 1000f, -4.04f * 1000f, -1.1f * 1000f,
    2.43f * 1000f, -4.04f * 1000f, -1.02f * 1000f,
    2.35f * 1000f, -4.04f * 1000f, -0.97f * 1000f,
    2.25f * 1000f, -4.04f * 1000f, -0.95f * 1000f,
    2.15f * 1000f, -4.04f * 1000f, -0.97f * 1000f,
    2.07f * 1000f, -4.04f * 1000f, -1.02f * 1000f,
    2.02f * 1000f, -4.04f * 1000f, -1.1f * 1000f,
    2f * 1000f, -4.04f * 1000f, -1.2f * 1000f,
    2f * 1000f, -4.04f * 1000f, -1.98f * 1000f,
    0.35f * 1000f, -4.04f * 1000f, -1.55f * 1000f,
    0.35f * 1000f, -4.04f * 1000f, -1f * 1000f,
    0.33f * 1000f, -4.04f * 1000f, -0.9f * 1000f,
    0.28f * 1000f, -4.04f * 1000f, -0.82f * 1000f,
    0.2f * 1000f, -4.04f * 1000f, -0.77f * 1000f,
    0.1f * 1000f, -4.04f * 1000f, -0.75f * 1000f,
    0f * 1000f, -4.04f * 1000f, -0.77f * 1000f,
    -0.08f * 1000f, -4.04f * 1000f, -0.82f * 1000f,
    -0.13f * 1000f, -4.04f * 1000f, -0.9f * 1000f,
    -0.15f * 1000f, -4.04f * 1000f, -1f * 1000f,
    -0.15f * 1000f, -4.04f * 1000f, -1.42f * 1000f
    };
    // Letter G
    static final float[] letter_G_logo1 = new float[]{
    -1.32f * 1000f, -4.04f * 1000f, 0.74f * 1000f,
    -1.5f * 1000f, -4.04f * 1000f, 0.83f * 1000f,
    -1.63f * 1000f, -4.04f * 1000f, 0.97f * 1000f,
    -1.69f * 1000f, -4.04f * 1000f, 1.16f * 1000f,
    -1.68f * 1000f, -4.04f * 1000f, 1.35f * 1000f,
    -1.59f * 1000f, -4.04f * 1000f, 1.53f * 1000f,
    -1.44f * 1000f, -4.04f * 1000f, 1.65f * 1000f,
    -1.26f * 1000f, -4.04f * 1000f, 1.72f * 1000f,
    -1.06f * 1000f, -4.04f * 1000f, 1.71f * 1000f,
    -1.07f * 1000f, -4.04f * 1000f, 1.71f * 1000f,
    -0.97f * 1000f, -4.04f * 1000f, 1.7f * 1000f,
    -0.88f * 1000f, -4.04f * 1000f, 1.73f * 1000f,
    -0.8f * 1000f, -4.04f * 1000f, 1.79f * 1000f,
    -0.76f * 1000f, -4.04f * 1000f, 1.88f * 1000f,
    -0.75f * 1000f, -4.04f * 1000f, 1.98f * 1000f,
    -0.78f * 1000f, -4.04f * 1000f, 2.07f * 1000f,
    -0.85f * 1000f, -4.04f * 1000f, 2.14f * 1000f,
    -0.94f * 1000f, -4.04f * 1000f, 2.19f * 1000f,
    -1.32f * 1000f, -4.04f * 1000f, 2.21f * 1000f,
    -1.69f * 1000f, -4.04f * 1000f, 2.09f * 1000f,
    -1.99f * 1000f, -4.04f * 1000f, 1.83f * 1000f,
    -2.16f * 1000f, -4.04f * 1000f, 1.48f * 1000f,
    -2.19f * 1000f, -4.04f * 1000f, 1.09f * 1000f,
    -2.06f * 1000f, -4.04f * 1000f, 0.72f * 1000f,
    -1.8f * 1000f, -4.04f * 1000f, 0.43f * 1000f,
    -1.45f * 1000f, -4.04f * 1000f, 0.26f * 1000f,
    1.24f * 1000f, -4.04f * 1000f, -0.47f * 1000f,
    1.63f * 1000f, -4.04f * 1000f, -0.49f * 1000f,
    2f * 1000f, -4.04f * 1000f, -0.37f * 1000f,
    2.29f * 1000f, -4.04f * 1000f, -0.11f * 1000f,
    2.47f * 1000f, -4.04f * 1000f, 0.24f * 1000f,
    2.49f * 1000f, -4.04f * 1000f, 0.63f * 1000f,
    2.37f * 1000f, -4.04f * 1000f, 1f * 1000f,
    2.11f * 1000f, -4.04f * 1000f, 1.29f * 1000f,
    1.76f * 1000f, -4.04f * 1000f, 1.47f * 1000f,
    0.1f * 1000f, -4.04f * 1000f, 1.91f * 1000f,
    0f * 1000f, -4.04f * 1000f, 1.89f * 1000f,
    -0.08f * 1000f, -4.04f * 1000f, 1.83f * 1000f,
    -0.13f * 1000f, -4.04f * 1000f, 1.75f * 1000f,
    -0.15f * 1000f, -4.04f * 1000f, 1.65f * 1000f,
    -0.15f * 1000f, -4.04f * 1000f, 0.85f * 1000f,
    -0.13f * 1000f, -4.04f * 1000f, 0.75f * 1000f,
    -0.08f * 1000f, -4.04f * 1000f, 0.67f * 1000f,
    0f * 1000f, -4.04f * 1000f, 0.62f * 1000f,
    0.1f * 1000f, -4.04f * 1000f, 0.6f * 1000f,
    0.2f * 1000f, -4.04f * 1000f, 0.62f * 1000f,
    0.28f * 1000f, -4.04f * 1000f, 0.67f * 1000f,
    0.33f * 1000f, -4.04f * 1000f, 0.75f * 1000f,
    0.35f * 1000f, -4.04f * 1000f, 0.85f * 1000f,
    0.35f * 1000f, -4.04f * 1000f, 1.33f * 1000f,
    1.63f * 1000f, -4.04f * 1000f, 0.98f * 1000f,
    1.8f * 1000f, -4.04f * 1000f, 0.9f * 1000f,
    1.93f * 1000f, -4.04f * 1000f, 0.75f * 1000f,
    2f * 1000f, -4.04f * 1000f, 0.57f * 1000f,
    1.98f * 1000f, -4.04f * 1000f, 0.37f * 1000f,
    1.9f * 1000f, -4.04f * 1000f, 0.2f * 1000f,
    1.75f * 1000f, -4.04f * 1000f, 0.07f * 1000f,
    1.57f * 1000f, -4.04f * 1000f, 0f * 1000f,
    1.37f * 1000f, -4.04f * 1000f, 0.02f * 1000f,
    -1.32f * 1000f, -4.04f * 1000f, 0.74f * 1000f
    };
    // Letter O
    static final float[] letter_O_outer_logo1 = new float[]{
    -1.06f * 1000f, -4.04f * 1000f, 4.11f * 1000f,
    -1.26f * 1000f, -4.04f * 1000f, 4.12f * 1000f,
    -1.44f * 1000f, -4.04f * 1000f, 4.05f * 1000f,
    -1.59f * 1000f, -4.04f * 1000f, 3.93f * 1000f,
    -1.68f * 1000f, -4.04f * 1000f, 3.75f * 1000f,
    -1.69f * 1000f, -4.04f * 1000f, 3.56f * 1000f,
    -1.63f * 1000f, -4.04f * 1000f, 3.37f * 1000f,
    -1.5f * 1000f, -4.04f * 1000f, 3.23f * 1000f,
    -1.32f * 1000f, -4.04f * 1000f, 3.14f * 1000f,
    1.37f * 1000f, -4.04f * 1000f, 2.42f * 1000f,
    1.57f * 1000f, -4.04f * 1000f, 2.4f * 1000f,
    1.75f * 1000f, -4.04f * 1000f, 2.47f * 1000f,
    1.9f * 1000f, -4.04f * 1000f, 2.6f * 1000f,
    1.98f * 1000f, -4.04f * 1000f, 2.77f * 1000f,
    2f * 1000f, -4.04f * 1000f, 2.97f * 1000f,
    1.93f * 1000f, -4.04f * 1000f, 3.15f * 1000f,
    1.8f * 1000f, -4.04f * 1000f, 3.3f * 1000f,
    1.63f * 1000f, -4.04f * 1000f, 3.38f * 1000f,
    -1.07f * 1000f, -4.04f * 1000f, 4.1f * 1000f,
    -1.06f * 1000f, -4.04f * 1000f, 4.11f * 1000f
    };
    static final float[] letter_O_inner_logo1 = new float[]{
    -1.45f * 1000f, -4.04f * 1000f, 2.66f * 1000f,
    -1.8f * 1000f, -4.04f * 1000f, 2.83f * 1000f,
    -2.06f * 1000f, -4.04f * 1000f, 3.12f * 1000f,
    -2.19f * 1000f, -4.04f * 1000f, 3.49f * 1000f,
    -2.16f * 1000f, -4.04f * 1000f, 3.88f * 1000f,
    -1.99f * 1000f, -4.04f * 1000f, 4.23f * 1000f,
    -1.69f * 1000f, -4.04f * 1000f, 4.49f * 1000f,
    -1.32f * 1000f, -4.04f * 1000f, 4.61f * 1000f,
    -0.94f * 1000f, -4.04f * 1000f, 4.59f * 1000f,
    1.76f * 1000f, -4.04f * 1000f, 3.87f * 1000f,
    2.11f * 1000f, -4.04f * 1000f, 3.69f * 1000f,
    2.37f * 1000f, -4.04f * 1000f, 3.4f * 1000f,
    2.49f * 1000f, -4.04f * 1000f, 3.03f * 1000f,
    2.47f * 1000f, -4.04f * 1000f, 2.64f * 1000f,
    2.29f * 1000f, -4.04f * 1000f, 2.29f * 1000f,
    2f * 1000f, -4.04f * 1000f, 2.03f * 1000f,
    1.63f * 1000f, -4.04f * 1000f, 1.91f * 1000f,
    1.24f * 1000f, -4.04f * 1000f, 1.93f * 1000f,
    -1.45f * 1000f, -4.04f * 1000f, 2.66f * 1000f
    };

    // Letter L
    static final float[] letter_L_logo2 = new float[]{
    2.5f * 620f, -0.04f * 620f, -3.3f * 620f,
    2.48f * 620f, -0.04f * 620f, -3.2f * 620f,
    2.43f * 620f, -0.04f * 620f, -3.12f * 620f,
    2.35f * 620f, -0.04f * 620f, -3.07f * 620f,
    2.25f * 620f, -0.04f * 620f, -3.05f * 620f,
    2.15f * 620f, -0.04f * 620f, -3.07f * 620f,
    2.07f * 620f, -0.04f * 620f, -3.12f * 620f,
    2.02f * 620f, -0.04f * 620f, -3.2f * 620f,
    2f * 620f, -0.04f * 620f, -3.3f * 620f,
    2f * 620f, -0.04f * 620f, -4.08f * 620f,
    -1.95f * 620f, -0.04f * 620f, -3.05f * 620f,
    -2.05f * 620f, -0.04f * 620f, -3.07f * 620f,
    -2.13f * 620f, -0.04f * 620f, -3.12f * 620f,
    -2.18f * 620f, -0.04f * 620f, -3.2f * 620f,
    -2.2f * 620f, -0.04f * 620f, -3.3f * 620f,
    -2.18f * 620f, -0.04f * 620f, -3.4f * 620f,
    -2.13f * 620f, -0.04f * 620f, -3.48f * 620f,
    -2.05f * 620f, -0.04f * 620f, -3.53f * 620f,
    -1.95f * 620f, -0.04f * 620f, -3.55f * 620f,
    2.25f * 620f, -0.04f * 620f, -4.65f * 620f,
    2.35f * 620f, -0.04f * 620f, -4.63f * 620f,
    2.43f * 620f, -0.04f * 620f, -4.58f * 620f,
    2.48f * 620f, -0.04f * 620f, -4.5f * 620f,
    2.5f * 620f, -0.04f * 620f, -4.4f * 620f,
    2.5f * 620f, -0.04f * 620f, -3.3f * 620f
    };
    // Letter E
    static final float[] letter_E_logo2 = new float[]{
    -0.15f * 620f, -0.04f * 620f, -1.42f * 620f,
    -1.7f * 620f, -0.04f * 620f, -1.02f * 620f,
    -1.7f * 620f, -0.04f * 620f, -0.1f * 620f,
    -1.72f * 620f, -0.04f * 620f, 0f * 620f,
    -1.77f * 620f, -0.04f * 620f, 0.08f * 620f,
    -1.85f * 620f, -0.04f * 620f, 0.13f * 620f,
    -1.95f * 620f, -0.04f * 620f, 0.15f * 620f,
    -2.05f * 620f, -0.04f * 620f, 0.13f * 620f,
    -2.13f * 620f, -0.04f * 620f, 0.08f * 620f,
    -2.18f * 620f, -0.04f * 620f, 0f * 620f,
    -2.2f * 620f, -0.04f * 620f, -0.1f * 620f,
    -2.2f * 620f, -0.04f * 620f, -1.2f * 620f,
    -2.18f * 620f, -0.04f * 620f, -1.3f * 620f,
    -2.13f * 620f, -0.04f * 620f, -1.38f * 620f,
    -2.05f * 620f, -0.04f * 620f, -1.43f * 620f,
    -1.95f * 620f, -0.04f * 620f, -1.45f * 620f,
    2.25f * 620f, -0.04f * 620f, -2.55f * 620f,
    2.35f * 620f, -0.04f * 620f, -2.53f * 620f,
    2.43f * 620f, -0.04f * 620f, -2.48f * 620f,
    2.48f * 620f, -0.04f * 620f, -2.4f * 620f,
    2.5f * 620f, -0.04f * 620f, -2.3f * 620f,
    2.5f * 620f, -0.04f * 620f, -1.2f * 620f,
    2.48f * 620f, -0.04f * 620f, -1.1f * 620f,
    2.43f * 620f, -0.04f * 620f, -1.02f * 620f,
    2.35f * 620f, -0.04f * 620f, -0.97f * 620f,
    2.25f * 620f, -0.04f * 620f, -0.95f * 620f,
    2.15f * 620f, -0.04f * 620f, -0.97f * 620f,
    2.07f * 620f, -0.04f * 620f, -1.02f * 620f,
    2.02f * 620f, -0.04f * 620f, -1.1f * 620f,
    2f * 620f, -0.04f * 620f, -1.2f * 620f,
    2f * 620f, -0.04f * 620f, -1.98f * 620f,
    0.35f * 620f, -0.04f * 620f, -1.55f * 620f,
    0.35f * 620f, -0.04f * 620f, -1f * 620f,
    0.33f * 620f, -0.04f * 620f, -0.9f * 620f,
    0.28f * 620f, -0.04f * 620f, -0.82f * 620f,
    0.2f * 620f, -0.04f * 620f, -0.77f * 620f,
    0.1f * 620f, -0.04f * 620f, -0.75f * 620f,
    0f * 620f, -0.04f * 620f, -0.77f * 620f,
    -0.08f * 620f, -0.04f * 620f, -0.82f * 620f,
    -0.13f * 620f, -0.04f * 620f, -0.9f * 620f,
    -0.15f * 620f, -0.04f * 620f, -1f * 620f,
    -0.15f * 620f, -0.04f * 620f, -1.42f * 620f
    };
    // Letter G
    static final float[] letter_G_logo2 = new float[]{
    -1.32f * 620f, -0.04f * 620f, 0.74f * 620f,
    -1.5f * 620f, -0.04f * 620f, 0.83f * 620f,
    -1.63f * 620f, -0.04f * 620f, 0.97f * 620f,
    -1.69f * 620f, -0.04f * 620f, 1.16f * 620f,
    -1.68f * 620f, -0.04f * 620f, 1.35f * 620f,
    -1.59f * 620f, -0.04f * 620f, 1.53f * 620f,
    -1.44f * 620f, -0.04f * 620f, 1.65f * 620f,
    -1.26f * 620f, -0.04f * 620f, 1.72f * 620f,
    -1.06f * 620f, -0.04f * 620f, 1.71f * 620f,
    -1.07f * 620f, -0.04f * 620f, 1.71f * 620f,
    -0.97f * 620f, -0.04f * 620f, 1.7f * 620f,
    -0.88f * 620f, -0.04f * 620f, 1.73f * 620f,
    -0.8f * 620f, -0.04f * 620f, 1.79f * 620f,
    -0.76f * 620f, -0.04f * 620f, 1.88f * 620f,
    -0.75f * 620f, -0.04f * 620f, 1.98f * 620f,
    -0.78f * 620f, -0.04f * 620f, 2.07f * 620f,
    -0.85f * 620f, -0.04f * 620f, 2.14f * 620f,
    -0.94f * 620f, -0.04f * 620f, 2.19f * 620f,
    -1.32f * 620f, -0.04f * 620f, 2.21f * 620f,
    -1.69f * 620f, -0.04f * 620f, 2.09f * 620f,
    -1.99f * 620f, -0.04f * 620f, 1.83f * 620f,
    -2.16f * 620f, -0.04f * 620f, 1.48f * 620f,
    -2.19f * 620f, -0.04f * 620f, 1.09f * 620f,
    -2.06f * 620f, -0.04f * 620f, 0.72f * 620f,
    -1.8f * 620f, -0.04f * 620f, 0.43f * 620f,
    -1.45f * 620f, -0.04f * 620f, 0.26f * 620f,
    1.24f * 620f, -0.04f * 620f, -0.47f * 620f,
    1.63f * 620f, -0.04f * 620f, -0.49f * 620f,
    2f * 620f, -0.04f * 620f, -0.37f * 620f,
    2.29f * 620f, -0.04f * 620f, -0.11f * 620f,
    2.47f * 620f, -0.04f * 620f, 0.24f * 620f,
    2.49f * 620f, -0.04f * 620f, 0.63f * 620f,
    2.37f * 620f, -0.04f * 620f, 1f * 620f,
    2.11f * 620f, -0.04f * 620f, 1.29f * 620f,
    1.76f * 620f, -0.04f * 620f, 1.47f * 620f,
    0.1f * 620f, -0.04f * 620f, 1.91f * 620f,
    0f * 620f, -0.04f * 620f, 1.89f * 620f,
    -0.08f * 620f, -0.04f * 620f, 1.83f * 620f,
    -0.13f * 620f, -0.04f * 620f, 1.75f * 620f,
    -0.15f * 620f, -0.04f * 620f, 1.65f * 620f,
    -0.15f * 620f, -0.04f * 620f, 0.85f * 620f,
    -0.13f * 620f, -0.04f * 620f, 0.75f * 620f,
    -0.08f * 620f, -0.04f * 620f, 0.67f * 620f,
    0f * 620f, -0.04f * 620f, 0.62f * 620f,
    0.1f * 620f, -0.04f * 620f, 0.6f * 620f,
    0.2f * 620f, -0.04f * 620f, 0.62f * 620f,
    0.28f * 620f, -0.04f * 620f, 0.67f * 620f,
    0.33f * 620f, -0.04f * 620f, 0.75f * 620f,
    0.35f * 620f, -0.04f * 620f, 0.85f * 620f,
    0.35f * 620f, -0.04f * 620f, 1.33f * 620f,
    1.63f * 620f, -0.04f * 620f, 0.98f * 620f,
    1.8f * 620f, -0.04f * 620f, 0.9f * 620f,
    1.93f * 620f, -0.04f * 620f, 0.75f * 620f,
    2f * 620f, -0.04f * 620f, 0.57f * 620f,
    1.98f * 620f, -0.04f * 620f, 0.37f * 620f,
    1.9f * 620f, -0.04f * 620f, 0.2f * 620f,
    1.75f * 620f, -0.04f * 620f, 0.07f * 620f,
    1.57f * 620f, -0.04f * 620f, 0f * 620f,
    1.37f * 620f, -0.04f * 620f, 0.02f * 620f,
    -1.32f * 620f, -0.04f * 620f, 0.74f * 620f
    };
    // Letter O
    static final float[] letter_O_outer_logo2 = new float[]{
    -1.06f * 620f, -0.04f * 620f, 4.11f * 620f,
    -1.26f * 620f, -0.04f * 620f, 4.12f * 620f,
    -1.44f * 620f, -0.04f * 620f, 4.05f * 620f,
    -1.59f * 620f, -0.04f * 620f, 3.93f * 620f,
    -1.68f * 620f, -0.04f * 620f, 3.75f * 620f,
    -1.69f * 620f, -0.04f * 620f, 3.56f * 620f,
    -1.63f * 620f, -0.04f * 620f, 3.37f * 620f,
    -1.5f * 620f, -0.04f * 620f, 3.23f * 620f,
    -1.32f * 620f, -0.04f * 620f, 3.14f * 620f,
    1.37f * 620f, -0.04f * 620f, 2.42f * 620f,
    1.57f * 620f, -0.04f * 620f, 2.4f * 620f,
    1.75f * 620f, -0.04f * 620f, 2.47f * 620f,
    1.9f * 620f, -0.04f * 620f, 2.6f * 620f,
    1.98f * 620f, -0.04f * 620f, 2.77f * 620f,
    2f * 620f, -0.04f * 620f, 2.97f * 620f,
    1.93f * 620f, -0.04f * 620f, 3.15f * 620f,
    1.8f * 620f, -0.04f * 620f, 3.3f * 620f,
    1.63f * 620f, -0.04f * 620f, 3.38f * 620f,
    -1.07f * 620f, -0.04f * 620f, 4.1f * 620f,
    -1.06f * 620f, -0.04f * 620f, 4.11f * 620f
    };
    static final float[] letter_O_inner_logo2 = new float[]{
    -1.45f * 620f, -0.04f * 620f, 2.66f * 620f,
    -1.8f * 620f, -0.04f * 620f, 2.83f * 620f,
    -2.06f * 620f, -0.04f * 620f, 3.12f * 620f,
    -2.19f * 620f, -0.04f * 620f, 3.49f * 620f,
    -2.16f * 620f, -0.04f * 620f, 3.88f * 620f,
    -1.99f * 620f, -0.04f * 620f, 4.23f * 620f,
    -1.69f * 620f, -0.04f * 620f, 4.49f * 620f,
    -1.32f * 620f, -0.04f * 620f, 4.61f * 620f,
    -0.94f * 620f, -0.04f * 620f, 4.59f * 620f,
    1.76f * 620f, -0.04f * 620f, 3.87f * 620f,
    2.11f * 620f, -0.04f * 620f, 3.69f * 620f,
    2.37f * 620f, -0.04f * 620f, 3.4f * 620f,
    2.49f * 620f, -0.04f * 620f, 3.03f * 620f,
    2.47f * 620f, -0.04f * 620f, 2.64f * 620f,
    2.29f * 620f, -0.04f * 620f, 2.29f * 620f,
    2f * 620f, -0.04f * 620f, 2.03f * 620f,
    1.63f * 620f, -0.04f * 620f, 1.91f * 620f,
    1.24f * 620f, -0.04f * 620f, 1.93f * 620f,
    -1.45f * 620f, -0.04f * 620f, 2.66f * 620f
    };

    public static float[] getStudLogoData1() {
        if (stud1 == null) {
            ArrayList<float[]> letters = new ArrayList<>();
            letters.add(letter_L_logo1);
            letters.add(letter_E_logo1);
            letters.add(letter_G_logo1);
            letters.add(letter_O_inner_logo1);
            letters.add(letter_O_outer_logo1);
            stud1 = createStudData(letters);
        }
        return stud1;
    }

    public static float[] getStudLogoData2() {
        if (stud2 == null) {
            ArrayList<float[]> letters = new ArrayList<>();
            letters.add(letter_L_logo2);
            letters.add(letter_E_logo2);
            letters.add(letter_G_logo2);
            letters.add(letter_O_inner_logo2);
            letters.add(letter_O_outer_logo2);
            stud2 = createStudData(letters);
        }
        return stud2;
    }

    // FIXME Stud data creation needs implementation!
    private static float[] createStudData(ArrayList<float[]> letters) {
        float[] result;
        int resultSize = 0;
        for (float[] letter : letters) {
            final int size = letter.length / 3 - 1;
            resultSize += size;
        }
        result = new float[resultSize * 14];
        int vertexIndex = 0;
        for (float[] letter : letters) {
            final int size = letter.length / 3 - 1;
            for (int i = 0; i < size; i++) {
                int offset1 = i * 3;
                final float x1 = letter[offset1];
                final float y1 = letter[offset1 + 1];
                final float z1 = letter[offset1 + 2];
                final float x2 = letter[offset1 + 3];
                final float y2 = letter[offset1 + 4];
                final float z2 = letter[offset1 + 5];
                pointAt7(0, x1, y1, z1, result, vertexIndex);
                pointAt7(1, x2, y2, z2, result, vertexIndex);
                colourise7(0, 2, 0f, 0f, 0f, 7f, result, vertexIndex);
                vertexIndex += 2;
            }
        }
        return result;
    }

    private static void colourise7(int offset, int times, float r, float g, float b,
            float a, float[] vertexData, int i) {
        for (int j = 0; j < times; j++) {
            int pos = (offset + i + j) * 7;
            vertexData[pos + 3] = r;
            vertexData[pos + 4] = g;
            vertexData[pos + 5] = b;
            vertexData[pos + 6] = a;
        }
    }

    private static void pointAt7(int offset, float x, float y, float z,
            float[] vertexData, int i) {
        int pos = (offset + i) * 7;
        vertexData[pos] = x;
        vertexData[pos + 1] = y;
        vertexData[pos + 2] = z;
    }
}

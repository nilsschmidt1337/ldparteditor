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

import java.math.BigDecimal;

import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;

public enum ProtractorHelper {
    INSTANCE;
    
    public static BigDecimal[] changeAngle(double angle1, GData3 tri) {
        angle1 = angle1 / 180d * Math.PI;
        double angle2 = -angle1;
        
        BigDecimal[] result = new BigDecimal[3];
        result[0] = tri.X3;
        result[1] = tri.Y3;
        result[2] = tri.Z3;
        
        Vector3d old = new Vector3d(tri.X3, tri.Y3, tri.Z3); 
        
        Vector3d AtoB = Vector3d.sub(new Vector3d(tri.X2, tri.Y2, tri.Z2), new Vector3d(tri.X1, tri.Y1, tri.Z1));        
        BigDecimal r = Vector3d.sub(new Vector3d(tri.X2, tri.Y2, tri.Z2), new Vector3d(tri.X1, tri.Y1, tri.Z1)).length();
        if (BigDecimal.ZERO.compareTo(AtoB.length()) == 0 || BigDecimal.ZERO.compareTo(r) == 0) {
            return result;
        }
        Vector3d u = new Vector3d();
        AtoB.normalise(u);
        Vector3d C = new Vector3d(tri.X1, tri.Y1, tri.Z1);
        Vector3d n = new Vector3d(new BigDecimal(tri.xn), new BigDecimal(tri.yn), new BigDecimal(tri.zn));
        n.normalise(n);
        BigDecimal sin_t;
        BigDecimal cos_t;
        
        // Calculate new X, Y, Z values
        
        // Parametric Equation of a Circle in 3D
        // P(t) = r * cos(t) * u + r * sin(t) * n x u + C 
        
        // r = radius
        // n = normal vector
        // u = any unit vector perpendicular to the normal vector  
        // t = angle
        // C = center
   
        sin_t = MathHelper.sin(new BigDecimal(angle1));
        cos_t = MathHelper.cos(new BigDecimal(angle1));
        
        Vector3d res1 = 
                Vector3d.add(
                Vector3d.add(
                        u.scale(r.multiply(cos_t)), 
                        Vector3d.cross(n, u).scale(r.multiply(sin_t))), C);
        
        sin_t = MathHelper.sin(new BigDecimal(angle2));
        cos_t = MathHelper.cos(new BigDecimal(angle2));
        
        Vector3d res2 = 
                Vector3d.add(
                Vector3d.add(
                        u.scale(r.multiply(cos_t)), 
                        Vector3d.cross(n, u).scale(r.multiply(sin_t))), C);
   
        if (true || Vector3d.distSquare(old, res1).compareTo(Vector3d.distSquare(old, res2)) < 0) {
            // result[0] = res1.X;
            // result[1] = res1.Y;
            // result[2] = res1.Z;
        } else {
            result[0] = res2.X;
            result[1] = res2.Y;
            result[2] = res2.Z;
        }
        return result;
    }
}

/*
 * The MIT License
 *
 * Copyright 2023 akava.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ptjava;

import java.util.SplittableRandom;

class Camera {

    public Vector p, u, v, w;
    public double m;
    public double focalDistance;
    public double apertureRadius;
    public double fovy;
    public double theta;
    public static Vector Position;

    public Camera() {
    }

    public static Camera LookAt(Vector eye, Vector center, Vector up, double fovy) {
        Camera c = new Camera();
        c.p = eye;
	    c.w = center.Sub(eye).Normalize();
	    c.u = up.Cross(c.w).Normalize();
	    c.v = c.w.Cross(c.u).Normalize();
	    c.m = 1 / Math.tan(fovy * Math.PI/360);
        return c;
    }

    public void SetFocus(Vector focalPoint, double apertureRadius) {
        focalDistance = focalPoint.Sub(p).Length();
        this.apertureRadius = apertureRadius;
    }

    public Ray CastRay(int x, int y, Integer w, int h, Double u, Double v, SplittableRandom  rand) {
        double aspect = w / (double)h;
        var px = (((double)x + u - 0.5) / ((double)w - 1.0)) * 2 - 1;
        var py = (((double)y + v - 0.5) / ((double)h - 1.0)) * 2 - 1;

        Vector d = new Vector();
        d = d.Add(this.u.MulScalar(-px * aspect));
        d = d.Add(this.v.MulScalar(-py));
        d = d.Add(this.w.MulScalar(this.m));
        d = d.Normalize();

        var p = this.p;

        if (this.apertureRadius > 0)
        {
            var focalPoint = this.p.Add(d.MulScalar(focalDistance));
            var angle = rand.nextDouble() * 2.0 * Math.PI;
            var radius = rand.nextDouble() * apertureRadius;
            p = p.Add(this.u.MulScalar(Math.cos(angle) * radius)).Add(this.v.MulScalar(Math.sin(angle) * radius));
            d = focalPoint.Sub(p).Normalize();
        }

        return new Ray(p, d);
    }
}

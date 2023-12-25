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

class Sphere extends TransformedShape {

    public Vector Center;
    public double Radius;
    Material Material;
    Box Box;

    static final double EPS = 1e-9;

    Sphere(Vector center, double radius, Material material, Box box) {
        Center = center;
        Radius = radius;
        Material = material;
        Box = box;
    }

    static Sphere NewSphere(Vector center, double radius, Material material) {
        Vector min = new Vector(center.getX() - radius, center.getY() - radius, center.getZ() - radius);
        Vector max = new Vector(center.getX() + radius, center.getY() + radius, center.getZ() + radius);
        Box box = new Box(min, max);
        return new Sphere(center, radius, material, box);
    }

    @Override
    public Box BoundingBox() {
        return Box;
    }

    @Override
    public Hit Intersect(Ray r) {
        Vector to = r.Origin.Sub(Center);
        double b = to.Dot(r.Direction);
        double c = to.Dot(to) - Radius * Radius;
        double d = b * b - c;

        if (d > 0) {
            d = Math.sqrt(d);
            double t1 = -b - d;
            if (t1 > EPS) {
                return new Hit(this, t1, null); 
            }

            double t2 = -b + d;
            if (t2 > EPS) {
                return new Hit(this, t2, null);
            }
        }

        return Hit.NoHit; 
    }

    @Override
    public Vector UV(Vector p) {
        p = p.Sub(Center);
        double u = Math.atan2(p.getZ(), p.getX());
        double v = Math.atan2(p.getY(), new Vector(p.getX(), 0, p.getZ()).Length());
        u = 1 - (u + Math.PI) / (2 * Math.PI);
        v = (v + Math.PI / 2) / Math.PI;
        return new Vector(u, v, 0);
    }

    @Override
    public void Compile() {
    }

    @Override
    public Material MaterialAt(Vector p) {
        return Material;
    }

    @Override
    public Vector NormalAt(Vector p) {
        return p.Sub(Center).Normalize();
    }
}
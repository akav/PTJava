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

class Plane extends TransformedShape {

    Vector Point;
    Vector Normal;
    Material Material;
    Box box;

    Plane() {
    }

    Plane(Vector point, Vector normal, Material mat) {
        Point = point;
        Normal = normal;
        Material = mat;
        box = new Box(new Vector(-Util.INF, -Util.INF, -Util.INF), new Vector(Util.INF, Util.INF, Util.INF));
    }

    public static Plane NewPlane(Vector point, Vector normal, Material material) {
        return new Plane(point, normal.Normalize(), material);
    }

    @Override
    public Box BoundingBox() {
        return new Box(new Vector(-Util.INF, -Util.INF, -Util.INF), new Vector(Util.INF, Util.INF, Util.INF));
    }

    @Override
    public Hit Intersect(Ray ray) {
        double d = this.Normal.Dot(ray.Direction);

        if (Math.abs(d) < Util.EPS) {
            return Hit.NoHit;
        }

        Vector a = this.Point.Sub(ray.Origin);
        double t = a.Dot(this.Normal) / d;

        if (t < Util.EPS) {
            return Hit.NoHit;
        }

        return new Hit(this, t, null);
    }

    @Override
    public Vector NormalAt(Vector a) {
        return Normal;
    }

    @Override
    public Vector UV(Vector a) {
        return new Vector();
    }

    @Override
    public Material MaterialAt(Vector a) {
        return Material;
    }

    @Override
    public void Compile() {

    }
}

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

import java.util.Objects;

class Triangle implements IShape {

    public Material Material;
    public Vector V1, V2, V3;
    public Vector N1, N2, N3;
    public Vector T1, T2, T3;

    public Triangle() { }

    Triangle(Vector v1, Vector v2, Vector v3) {
        V1 = v1;
        V2 = v2;
        V3 = v3;
    }

    Triangle(Vector v1, Vector v2, Vector v3, Material material) {
        V1 = v1;
        V2 = v2;
        V3 = v3;
        this.Material = material;
    }

    Triangle(Vector v1, Vector v2, Vector v3, Vector n1, Vector n2, Vector n3, Material material) {
        V1 = v1;
        V2 = v2;
        V3 = v3;
        N1 = n1;
        N2 = n2;
        N3 = n3;
        this.Material = material;
    }

    static Triangle NewTriangle(Vector v1, Vector v2, Vector v3, Vector t1, Vector t2, Vector t3, Material material) {
        Triangle t = new Triangle();
        t.V1 = v1;
        t.V2 = v2;
        t.V3 = v3;
        t.T1 = t1;
        t.T2 = t2;
        t.T3 = t3;
        t.Material = material;
        t.FixNormals();
        return t;
    }

    static Triangle NewTriangle(Vector v1, Vector v2, Vector v3, Vector n1, Vector n2, Vector n3, Vector t1, Vector t2, Vector t3, Material material) {
        Triangle t = new Triangle();
        t.V1 = v1;
        t.V2 = v2;
        t.V3 = v3;
        t.N1 = n1;
        t.N2 = n2;
        t.N3 = n3;
        t.T1 = t1;
        t.T2 = t2;
        t.T3 = t3;
        t.Material = material;
        return t;
    }

    public Vector[] Vertices() {
        return new Vector[]{V1, V2, V3};
    }

    @Override
    public void Compile() { }

    @Override
    public Box BoundingBox() {
        Vector min = V1.Min(V2).Min(V3);
        Vector max = V1.Max(V2).Max(V3);
        return new Box(min, max);
    }

    @Override
    public Hit Intersect(Ray r) {
        Vector e1 = V2.Sub(V1);
        Vector e2 = V3.Sub(V1);
        Vector h = r.Direction.Cross(e2);
        double det = e1.Dot(h);

        if (det > -Util.EPS && det < Util.EPS) {
            return Hit.NoHit;
        }

        double invDet = 1.0 / det;
        Vector s = r.Origin.Sub(V1);
        double u = s.Dot(h) * invDet;

        if (u < 0 || u > 1) {
            return Hit.NoHit;
        }

        Vector q = s.Cross(e1);
        double v = r.Direction.Dot(q) * invDet;

        if (v < 0 || (u + v) > 1) {
            return Hit.NoHit;
        }

        double t = e2.Dot(q) * invDet;

        if (t < Util.EPS) {
            return Hit.NoHit;
        }

        return new Hit(this, t, null);
    }

    @Override
    public Vector UV(Vector p) {
        double[] barycentric = Barycentric(p);
        double u = barycentric[0];
        double v = barycentric[1];
        double w = barycentric[2];
        Vector n = new Vector();
        n = n.Add(T1.MulScalar(u));
        n = n.Add(T2.MulScalar(v));
        n = n.Add(T3.MulScalar(w));
        return new Vector(n.getX(), n.getY(), 0);
    }

    @Override
    public Material MaterialAt(Vector p) {
        return this.Material;
    }

    @Override
    public Vector NormalAt(Vector p) {
        double[] barycentric = Barycentric(p);
        double u = barycentric[0];
        double v = barycentric[1];
        double w = barycentric[2];
        Vector n = N1.MulScalar(u).Add(N2.MulScalar(v)).Add(N3.MulScalar(w));

        if (Material.NormalTexture != null) {
            Vector b = T1.MulScalar(u).Add(T2.MulScalar(v)).Add(T3.MulScalar(w));
            Vector ns = Material.NormalTexture.NormalSample(b.getX(), b.getY());

            if (!ns.equals(Vector.ZERO)) {
                Vector dv1 = V2.Sub(V1);
                Vector dv2 = V3.Sub(V1);
                Vector dt1 = T2.Sub(T1);
                Vector dt2 = T3.Sub(T1);
                Vector T = dv1.MulScalar(dt2.getY()).Sub(dv2.MulScalar(dt1.getY())).Normalize();
                Vector B = dv2.MulScalar(dt1.getX()).Sub(dv1.MulScalar(dt2.getX())).Normalize();
                Vector N = T.Cross(B);

                Matrix matrix = new Matrix(T.getX(), B.getX(), N.getX(), 0,
                                           T.getY(), B.getY(), N.getY(), 0,
                                           T.getZ(), B.getZ(), N.getZ(), 0,
                                           0, 0, 0, 1);
                n = matrix.MulDirection(ns);
            }
        }

        if (Material.BumpTexture != null) {
            Vector b = T1.MulScalar(u).Add(T2.MulScalar(v)).Add(T3.MulScalar(w));
            Vector bump = Material.BumpTexture.BumpSample(b.getX(), b.getY());

            if (!bump.equals(Vector.ZERO)) {
                Vector dv1 = V2.Sub(V1);
                Vector dv2 = V3.Sub(V1);
                Vector dt1 = T2.Sub(T1);
                Vector dt2 = T3.Sub(T1);
                Vector tangent = dv1.MulScalar(dt2.getY()).Sub(dv2.MulScalar(dt1.getY())).Normalize();
                Vector bitangent = dv2.MulScalar(dt1.getX()).Sub(dv1.MulScalar(dt2.getX())).Normalize();
                n = n.Add(tangent.MulScalar(bump.getX() * Material.BumpMultiplier));
                n = n.Add(bitangent.MulScalar(bump.getY() * Material.BumpMultiplier));
            }
        }

        return n.Normalize();
    }

    double Area() {
        Vector e1 = V2.Sub(V1);
        Vector e2 = V3.Sub(V1);
        Vector n = e1.Cross(e2);
        return n.Length() / 2;
    }

    Vector Normal() {
        Vector e1 = V2.Sub(V1);
        Vector e2 = V3.Sub(V1);
        return e1.Cross(e2).Normalize();
    }

    double[] Barycentric(Vector p) {
        Vector v0 = V2.Sub(V1);
        Vector v1 = V3.Sub(V1);
        Vector v2 = p.Sub(V1);
        double d00 = v0.Dot(v0);
        double d01 = v0.Dot(v1);
        double d11 = v1.Dot(v1);
        double d20 = v2.Dot(v0);
        double d21 = v2.Dot(v1);
        double d = d00 * d11 - d01 * d01;
        double v = (d11 * d20 - d01 * d21) / d;
        double w = (d00 * d21 - d01 * d20) / d;
        double u = 1 - v - w;
        return new double[]{u, v, w};
    }

    public void FixNormals() {
        Vector n = Normal();
        Vector zero = new Vector();

        if (N1.equals(zero)) {
            N1 = n;
        }

        if (N2.equals(zero)) {
            N2 = n;
        }

        if (N3.equals(zero)) {
            N3 = n;
        }
    }

    public Vector SamplePoint(java.util.Random rand) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

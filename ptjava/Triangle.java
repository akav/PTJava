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

class Triangle extends TransformedShape {

    double INF = 1e9;
    double EPS = 1e-9;
    Material Material;
    Vector V1, V2, V3;
    Vector N1, N2, N3;
    Vector T1, T2, T3;

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

    void FixNormals() {
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

    Vector Normal() {
        Vector e1 = V2.Sub(V1);
        Vector e2 = V3.Sub(V1);
        return e1.Cross(e2).Normalize();
    }

    Vector[] Vertices() {
        Vector[] ve = {V1, V2, V3};
        return ve;
    }

    @Override
    public Box BoundingBox() {
        Vector min = this.V1.Min(this.V2).Min(this.V3);
        Vector max = this.V1.Max(this.V2).Max(this.V3);
        return new Box(min, max);
    }

    @Override
    public Vector UV(Vector p) {
        double[] centric = this.Barycentric(p);
        Vector n = new Vector();
        n = n.Add(T1.MulScalar(centric[0]));
        n = n.Add(T2.MulScalar(centric[1]));
        n = n.Add(T3.MulScalar(centric[2]));
        return new Vector(n.getX(), n.getY(), 0);
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

        double[] uvw = {u, v, w};

        return uvw;

    }

    @Override
    public Material MaterialAt(Vector p) {
        return this.Material;
    }

    double Area(Triangle t) {
        Vector e1 = t.V2.Sub(t.V1);
        Vector e2 = t.V3.Sub(t.V1);
        Vector n = e1.Cross(e2);
        return n.Length() / 2;
    }

    @Override
    public Hit Intersect(Ray r) {
        //var e1x = V2.X - V1.X;
        //var e1y = V2.Y - V1.Y;
        //var e1z = V2.Z - V1.Z;
        //var e2x = V3.X - V1.X;
        //var e2y = V3.Y - V1.Y;
        //var e2z = V3.Z - V1.Z;
        //var px = r.Direction.Y * e2z - r.Direction.Z * e2y;
        //var py = r.Direction.Z * e2x - r.Direction.X * e2z;
        //var pz = r.Direction.X * e2y - r.Direction.Y * e2x;
        var e1x = V2.getX() - V1.getX();
        var e1y = V2.getY() - V1.getY();
        var e1z = V2.getZ() - V1.getZ();
        var e2x = V3.getX() - V1.getX();
        var e2y = V3.getY() - V1.getY();
        var e2z = V3.getZ() - V1.getZ();
        var px = r.Direction.getY() * e2z - r.Direction.getZ() * e2y;
        var py = r.Direction.getZ() * e2x - r.Direction.getX() * e2z;
        var pz = r.Direction.getX() * e2y - r.Direction.getY() * e2x;
        var det = e1x * px + e1y * py + e1z * pz;
        
        if (det > -Util.EPS && det < Util.EPS) {
            return Hit.NoHit;
        }

        var inv = 1 / det;
        //var tx = r.Origin.X - V1.X;
        //var ty = r.Origin.Y - V1.Y;
        //var tz = r.Origin.Z - V1.Z;
        var tx = r.Origin.getX() - V1.getX();
        var ty = r.Origin.getY() - V1.getY();
        var tz = r.Origin.getZ() - V1.getZ();

        var u = (tx * px + ty * py + tz * pz) * inv;

        if (u < 0 || u > 1) {
            return Hit.NoHit;
        }

        var qx = ty * e1z - tz * e1y;
        var qy = tz * e1x - tx * e1z;
        var qz = tx * e1y - ty * e1x;
        //var v = (r.Direction.X * qx + r.Direction.Y * qy + r.Direction.Z * qz) * inv;
        var v = (r.Direction.getX() * qx + r.Direction.getY() * qy + r.Direction.getZ() * qz) * inv;

        if ((v < 0) || ((u + v) > 1)) {
            return Hit.NoHit;

        }

        var d = (e2x * qx + e2y * qy + e2z * qz) * inv;

        if (d < Util.EPS) {
            return Hit.NoHit;
        }

        return new Hit(this, d, null);
    }

    @Override
    public Vector NormalAt(Vector p) {
        double[] bcentric = this.Barycentric(p);
        double u = bcentric[0];
        double v = bcentric[1];
        double w = bcentric[2];
        Vector n = new Vector();
        n = n.Add(N1.MulScalar(u));
        n = n.Add(N2.MulScalar(v));
        n = n.Add(N3.MulScalar(w));
        n = n.Normalize();

        if (Material.NormalTexture != null) {
            Vector b = new Vector();
            b = b.Add(T1.MulScalar(u));
            b = b.Add(T2.MulScalar(v));
            b = b.Add(T3.MulScalar(w));
            //Vector ns = this.Material.NormalTexture.NormalSample(b.X, b.Y);
            Vector ns = this.Material.NormalTexture.NormalSample(b.getX(), b.getY());
            Vector dv1 = V2.Sub(V1);
            Vector dv2 = V3.Sub(V1);
            Vector dt1 = T2.Sub(T1);
            Vector dt2 = T3.Sub(T1);
            //Vector T = dv1.MulScalar(dt2.Y).Sub(dv2.MulScalar(dt1.Y)).Normalize();
            //Vector B = dv2.MulScalar(dt1.X).Sub(dv1.MulScalar(dt2.X)).Normalize();
            Vector T = dv1.MulScalar(dt2.getY()).Sub(dv2.MulScalar(dt1.getY())).Normalize();
            Vector B = dv2.MulScalar(dt1.getX()).Sub(dv1.MulScalar(dt2.getX())).Normalize();

            Vector N = T.Cross(B);

            //Matrix matrix = new Matrix(T.X, B.X, N.X, 0,
            //        T.Y, B.Y, N.Y, 0,
            //        T.Z, B.Z, N.Z, 0,
            //        0, 0, 0, 1);
            Matrix matrix = new Matrix(T.getX(), B.getX(), N.getX(), 0,
                    T.getY(), B.getY(), N.getY(), 0,
                    T.getZ(), B.getZ(), N.getZ(), 0,
                    0, 0, 0, 1);
            matrix.MulDirection(ns);
        }

        if (this.Material.BumpTexture != null) {
            Vector b = new Vector();
            b = b.Add(T1.MulScalar(u));
            b = b.Add(T2.MulScalar(v));
            b = b.Add(T3.MulScalar(w));
            //Vector bump = Material.BumpTexture.BumpSample(b.X, b.Y);
            Vector bump = Material.BumpTexture.BumpSample(b.getX(), b.getY());
            Vector dv1 = V2.Sub(V1);
            Vector dv2 = V3.Sub(V1);
            Vector dt1 = T2.Sub(T1);
            Vector dt2 = T3.Sub(T1);
            //Vector tangent = dv1.MulScalar(dt2.Y).Sub(dv2.MulScalar(dt1.Y)).Normalize();
            //Vector bitangent = dv2.MulScalar(dt1.X).Sub(dv1.MulScalar(dt2.X)).Normalize();
            //n = n.Add(tangent.MulScalar(bump.X * Material.BumpMultiplier));
            //n = n.Add(bitangent.MulScalar(bump.Y * Material.BumpMultiplier));
            Vector tangent = dv1.MulScalar(dt2.getY()).Sub(dv2.MulScalar(dt1.getY())).Normalize();
            Vector bitangent = dv2.MulScalar(dt1.getX()).Sub(dv1.MulScalar(dt2.getX())).Normalize();
            n = n.Add(tangent.MulScalar(bump.getX() * Material.BumpMultiplier));
            n = n.Add(bitangent.MulScalar(bump.getY() * Material.BumpMultiplier));
        }
        n = n.Normalize();
        return n;
    }
}

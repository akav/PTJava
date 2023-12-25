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

public class Matrix {

    double M11, M12, M13, M14;
    double M21, M22, M23, M24;
    double M31, M32, M33, M34;
    double M41, M42, M43, M44;

    Matrix() {
    }

    Matrix(double x00, double x01, double x02, double x03,
           double x10, double x11, double x12, double x13,
           double x20, double x21, double x22, double x23,
           double x30, double x31, double x32, double x33) {
        this.M11 = x00;
        this.M12 = x01;
        this.M13 = x02;
        this.M14 = x03;
        this.M21 = x10;
        this.M22 = x11;
        this.M23 = x12;
        this.M24 = x13;
        this.M31 = x20;
        this.M32 = x21;
        this.M33 = x22;
        this.M34 = x23;
        this.M41 = x30;
        this.M42 = x31;
        this.M43 = x32;
        this.M44 = x33;
    }

    public static final Matrix Identity = new Matrix(
        1, 0, 0, 0, 
        0, 1, 0, 0, 
        0, 0, 1, 0,
        0, 0, 0, 1);

    Matrix Translate(Vector v) {
        return new Matrix(1, 0, 0, v.getX(),
                          0, 1, 0, v.getY(),
                          0, 0, 1, v.getZ(),
                          0, 0, 0, 1).Mul(this);
    }

    Matrix Scale(Vector v) {
        return new Matrix(v.getX(),   0,   0, 0,
                        0,     v.getY(),   0, 0,
                        0,   0,     v.getZ(), 0,
                        0,   0,   0, 1).Mul(this);
    }

    Matrix Rotate(Vector v, double a) {
        v = v.Normalize();
        var s = Math.sin(a);
        var c = Math.cos(a);
        var m = 1 - c;
        return new Matrix(m * v.getX() * v.getX() + c, m * v.getX() * v.getY() + v.getZ() * s, m * v.getZ() * v.getX() - v.getY() * s, 0,
                  m * v.getX() * v.getY() - v.getZ() * s, m * v.getY() * v.getY() + c, m * v.getY() * v.getZ() + v.getX() * s, 0,
                  m * v.getZ() * v.getX() + v.getY() * s, m * v.getY() * v.getZ() - v.getX() * s, m * v.getZ() * v.getZ() + c, 0,
                  0, 0, 0, 1).Mul(this);
    }

    Matrix Frustum(double l, double r, double b, double t, double n, double f) {
        double t1 = 2 * n;
        double t2 = r - l;
        double t3 = t - b;
        double t4 = f - n;
        return new Matrix(t1 / t2, 0, (r + l) / t2, 0,
                  0, t1 / t3, (t + b) / t3, 0,
                  0, 0, (-f - n) / t4, (-t1 * f) / t4,
                  0, 0, -1, 0);
    }

    Matrix Orthographic(double l, double r, double b, double t, double n, double f) {
        return new Matrix(2 / (r - l), 0, 0, -(r + l) / (r - l),
                0, 2 / (t - b), 0, -(t + b) / (t - b),
                0, 0, -2 / (f - n), -(f + n) / (f - n),
                0, 0, 0, 1);
    }

    Matrix Perspective(double fovy, double aspect, double near, double far) {
        double ymax = near * Math.tan(fovy * Math.PI / 360);
        double xmax = ymax * aspect;
        return Frustum(-xmax, xmax, -ymax, ymax, near, far);
    }

    static Matrix LookAtMatrix(Vector eye, Vector center, Vector up) {
        up = up.Normalize();
        var f = center.Sub(eye).Normalize();
        var s = f.Cross(up).Normalize();
        var u = s.Cross(f);
        var m = new Matrix(s.getX(), u.getX(), f.getX(), 0,
                s.getY(), u.getY(), f.getY(), 0,
                s.getZ(), u.getZ(), f.getZ(), 0,
                0, 0, 0, 1);

        return m.Transpose().Inverse().Translate(m, eye);
    }

    Matrix Translate(Matrix m, Vector v) {
        return new Matrix().Translate(v).Mul(m);
    }

    Matrix Mul(Matrix b) {
        Matrix m = new Matrix();
        m.M11 = M11 * b.M11 + M12 * b.M21 + M13 * b.M31 + M14 * b.M41;
        m.M21 = M21 * b.M11 + M22 * b.M21 + M23 * b.M31 + M24 * b.M41;
        m.M31 = M31 * b.M11 + M32 * b.M21 + M33 * b.M31 + M34 * b.M41;
        m.M41 = M41 * b.M11 + M42 * b.M21 + M43 * b.M31 + M44 * b.M41;
        m.M12 = M11 * b.M12 + M12 * b.M22 + M13 * b.M32 + M14 * b.M42;
        m.M22 = M21 * b.M12 + M22 * b.M22 + M23 * b.M32 + M24 * b.M42;
        m.M32 = M31 * b.M12 + M32 * b.M22 + M33 * b.M32 + M34 * b.M42;
        m.M42 = M41 * b.M12 + M42 * b.M22 + M43 * b.M32 + M44 * b.M42;
        m.M13 = M11 * b.M13 + M12 * b.M23 + M13 * b.M33 + M14 * b.M43;
        m.M23 = M21 * b.M13 + M22 * b.M23 + M23 * b.M33 + M24 * b.M43;
        m.M33 = M31 * b.M13 + M32 * b.M23 + M33 * b.M33 + M34 * b.M43;
        m.M43 = M41 * b.M13 + M42 * b.M23 + M43 * b.M33 + M44 * b.M43;
        m.M14 = M11 * b.M14 + M12 * b.M24 + M13 * b.M34 + M14 * b.M44;
        m.M24 = M21 * b.M14 + M22 * b.M24 + M23 * b.M34 + M24 * b.M44;
        m.M34 = M31 * b.M14 + M32 * b.M24 + M33 * b.M34 + M34 * b.M44;
        m.M44 = M41 * b.M14 + M42 * b.M24 + M43 * b.M34 + M44 * b.M44;
        return m;
    }

    Vector MulPosition(Vector b) {
        var x = M11 * b.getX() + M12 * b.getY() + M13 * b.getZ() + M14;
        var y = M21 * b.getX() + M22 * b.getY() + M23 * b.getZ() + M24;
        var z = M31 * b.getX() + M32 * b.getY() + M33 * b.getZ() + M34;
        return new Vector(x, y, z);
    }

    Vector MulDirection(Vector b) {
        double x = M11 * b.getX() + M12 * b.getY() + M13 * b.getZ();
        double y = M21 * b.getX() + M22 * b.getY() + M23 * b.getZ();
        double z = M31 * b.getX() + M32 * b.getY() + M33 * b.getZ();
        return new Vector(x, y, z).Normalize();
    }

    Ray MulRay(Ray b) {
        return new Ray(MulPosition(b.Origin), MulDirection(b.Direction));
    }

    Box MulBox(Box box) {
        Vector r = new Vector(M11, M21, M31);
        Vector u = new Vector(M12, M22, M32);
        Vector b = new Vector(M13, M23, M33);
        Vector t = new Vector(M14, M24, M34);
        Vector xa = r.MulScalar(box.Min.getX());
        Vector xb = r.MulScalar(box.Max.getX());
        Vector ya = u.MulScalar(box.Min.getY());
        Vector yb = u.MulScalar(box.Max.getY());
        Vector za = b.MulScalar(box.Min.getZ());
        Vector zb = b.MulScalar(box.Max.getZ());
        xa = xa.Min(xb);
        ya = ya.Min(yb);
        za = za.Min(zb);
        Vector min = xa.Add(ya).Add(za).Add(t);
        Vector max = xb.Add(yb).Add(zb).Add(t);
        return new Box(min, max);
    }

    Matrix Transpose() {
        return new Matrix(M11, M21, M31, M41, M12, M22, M32, M42, M13, M23, M33, M43, M14, M24, M34, M44);
    }

    double Determinant() {
        return (M11 * M22 * M33 * M44 - M11 * M22 * M34 * M43 +
        M11 * M23 * M34 * M42 - M11 * M23 * M32 * M44 +
        M11 * M24 * M32 * M43 - M11 * M24 * M33 * M42 -
        M12 * M23 * M34 * M41 + M12 * M23 * M31 * M44 -
        M12 * M24 * M31 * M43 + M12 * M24 * M33 * M41 -
        M12 * M21 * M33 * M44 + M12 * M21 * M34 * M43 +
        M13 * M24 * M31 * M42 - M13 * M24 * M32 * M41 +
        M13 * M21 * M32 * M44 - M13 * M21 * M34 * M42 +
        M13 * M22 * M34 * M41 - M13 * M22 * M31 * M44 -
        M14 * M21 * M32 * M43 + M14 * M21 * M33 * M42 -
        M14 * M22 * M33 * M41 + M14 * M22 * M31 * M43 -
        M14 * M23 * M31 * M42 + M14 * M23 * M32 * M41);
    }

    Matrix Inverse() {
        Matrix m = new Matrix();
        double d = Determinant();
        m.M11 = (M23 * M34 * M42 - M24 * M33 * M42 + M24 * M32 * M43 - M22 * M34 * M43 - M23 * M32 * M44 + M22 * M33 * M44) / d;
        m.M12 = (M14 * M33 * M42 - M13 * M34 * M42 - M14 * M32 * M43 + M12 * M34 * M43 + M13 * M32 * M44 - M12 * M33 * M44) / d;
        m.M13 = (M13 * M24 * M42 - M14 * M23 * M42 + M14 * M22 * M43 - M12 * M24 * M43 - M13 * M22 * M44 + M12 * M23 * M44) / d;
        m.M14 = (M14 * M23 * M32 - M13 * M24 * M32 - M14 * M22 * M33 + M12 * M24 * M33 + M13 * M22 * M34 - M12 * M23 * M34) / d;
        m.M21 = (M24 * M33 * M41 - M23 * M34 * M41 - M24 * M31 * M43 + M21 * M34 * M43 + M23 * M31 * M44 - M21 * M33 * M44) / d;
        m.M22 = (M13 * M34 * M41 - M14 * M33 * M41 + M14 * M31 * M43 - M11 * M34 * M43 - M13 * M31 * M44 + M11 * M33 * M44) / d;
        m.M23 = (M14 * M23 * M41 - M13 * M24 * M41 - M14 * M21 * M43 + M11 * M24 * M43 + M13 * M21 * M44 - M11 * M23 * M44) / d;
        m.M24 = (M13 * M24 * M31 - M14 * M23 * M31 + M14 * M21 * M33 - M11 * M24 * M33 - M13 * M21 * M34 + M11 * M23 * M34) / d;
        m.M31 = (M22 * M34 * M41 - M24 * M32 * M41 + M24 * M31 * M42 - M21 * M34 * M42 - M22 * M31 * M44 + M21 * M32 * M44) / d;
        m.M32 = (M14 * M32 * M41 - M12 * M34 * M41 - M14 * M31 * M42 + M11 * M34 * M42 + M12 * M31 * M44 - M11 * M32 * M44) / d;
        m.M33 = (M12 * M24 * M41 - M14 * M22 * M41 + M14 * M21 * M42 - M11 * M24 * M42 - M12 * M21 * M44 + M11 * M22 * M44) / d;
        m.M34 = (M14 * M22 * M31 - M12 * M24 * M31 - M14 * M21 * M32 + M11 * M24 * M32 + M12 * M21 * M34 - M11 * M22 * M34) / d;
        m.M41 = (M23 * M32 * M41 - M22 * M33 * M41 - M23 * M31 * M42 + M21 * M33 * M42 + M22 * M31 * M43 - M21 * M32 * M43) / d;
        m.M42 = (M12 * M33 * M41 - M13 * M32 * M41 + M13 * M31 * M42 - M11 * M33 * M42 - M12 * M31 * M43 + M11 * M32 * M43) / d;
        m.M43 = (M13 * M22 * M41 - M12 * M23 * M41 - M13 * M21 * M42 + M11 * M23 * M42 + M12 * M21 * M43 - M11 * M22 * M43) / d;
        m.M44 = (M12 * M23 * M31 - M13 * M22 * M31 + M13 * M21 * M32 - M11 * M23 * M32 - M12 * M21 * M33 + M11 * M22 * M33) / d;
        return m;
    }
}
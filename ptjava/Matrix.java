package ptjava;

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

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorOperators;

public class Matrix {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_256;
    private final DoubleVector[] rows;

    Matrix() {
        this.rows = new DoubleVector[4];
        for (int i = 0; i < 4; i++) {
            this.rows[i] = DoubleVector.zero(SPECIES);
        }
    }

    Matrix(DoubleVector row0, DoubleVector row1, DoubleVector row2, DoubleVector row3) {
        this.rows = new DoubleVector[]{row0, row1, row2, row3};
    }

    Matrix(double x00, double x01, double x02, double x03,
           double x10, double x11, double x12, double x13,
           double x20, double x21, double x22, double x23,
           double x30, double x31, double x32, double x33) {
        this.rows = new DoubleVector[4];
        this.rows[0] = DoubleVector.fromArray(SPECIES, new double[]{x00, x01, x02, x03}, 0);
        this.rows[1] = DoubleVector.fromArray(SPECIES, new double[]{x10, x11, x12, x13}, 0);
        this.rows[2] = DoubleVector.fromArray(SPECIES, new double[]{x20, x21, x22, x23}, 0);
        this.rows[3] = DoubleVector.fromArray(SPECIES, new double[]{x30, x31, x32, x33}, 0);
    }

    Matrix(double[] values) {
        this.rows = new DoubleVector[4];
        for (int i = 0; i < 4; i++) {
            this.rows[i] = DoubleVector.fromArray(SPECIES, values, i * 4);
        }
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
        return new Matrix(v.getX(), 0, 0, 0,
                          0, v.getY(), 0, 0,
                          0, 0, v.getZ(), 0,
                          0, 0, 0, 1).Mul(this);
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
        DoubleVector[] resultRows = new DoubleVector[4];
        for (int i = 0; i < 4; i++) {
            DoubleVector row = this.rows[i];
            double[] rowValues = new double[4];
            row.intoArray(rowValues, 0);
            DoubleVector resultRow = DoubleVector.zero(SPECIES);
            for (int j = 0; j < 4; j++) {
                DoubleVector col = DoubleVector.fromArray(SPECIES, new double[]{
                    b.rows[0].lane(j),
                    b.rows[1].lane(j),
                    b.rows[2].lane(j),
                    b.rows[3].lane(j)
                }, 0);
                resultRow = resultRow.add(row.broadcast(rowValues[j]).mul(col));
            }
            resultRows[i] = resultRow;
        }
        return new Matrix(resultRows[0], resultRows[1], resultRows[2], resultRows[3]);
    }

    Vector MulPosition(Vector b) {
        double[] result = new double[4];
        for (int i = 0; i < 4; i++) {
            result[i] = this.rows[i].mul(b.vec).reduceLanes(VectorOperators.ADD);
        }
        return new Vector(result[0], result[1], result[2]);
    }

    Vector MulDirection(Vector b) {
        double[] result = new double[4];
        for (int i = 0; i < 4; i++) {
            result[i] = this.rows[i].mul(b.vec).reduceLanes(VectorOperators.ADD);
        }
        return new Vector(result[0], result[1], result[2]).Normalize();
    }

    Ray MulRay(Ray b) {
        return new Ray(MulPosition(b.Origin), MulDirection(b.Direction));
    }

    Box MulBox(Box box) {
        double M11 = rows[0].lane(0);
        double M21 = rows[1].lane(0);
        double M31 = rows[2].lane(0);
        double M12 = rows[0].lane(1);
        double M22 = rows[1].lane(1);
        double M32 = rows[2].lane(1);
        double M13 = rows[0].lane(2);
        double M23 = rows[1].lane(2);
        double M33 = rows[2].lane(2);
        double M14 = rows[0].lane(3);
        double M24 = rows[1].lane(3);
        double M34 = rows[2].lane(3);

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
        double[] transposedValues = new double[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                transposedValues[i * 4 + j] = this.rows[j].lane(i);
            }
        }
        return new Matrix(transposedValues);
    }

    double Determinant() {
        double[] m = new double[16];
        for (int i = 0; i < 4; i++) {
            this.rows[i].intoArray(m, i * 4);
        }
        return (m[0] * m[5] * m[10] * m[15] - m[0] * m[5] * m[11] * m[14] +
                m[0] * m[6] * m[11] * m[13] - m[0] * m[6] * m[9] * m[15] +
                m[0] * m[7] * m[9] * m[14] - m[0] * m[7] * m[10] * m[13] -
                m[1] * m[6] * m[11] * m[12] + m[1] * m[6] * m[8] * m[15] -
                m[1] * m[7] * m[8] * m[14] + m[1] * m[7] * m[10] * m[12] -
                m[1] * m[4] * m[10] * m[15] + m[1] * m[4] * m[11] * m[14] +
                m[2] * m[7] * m[8] * m[13] - m[2] * m[7] * m[9] * m[12] +
                m[2] * m[4] * m[9] * m[15] - m[2] * m[4] * m[11] * m[13] +
                m[2] * m[5] * m[11] * m[12] - m[2] * m[5] * m[8] * m[15] -
                m[3] * m[4] * m[9] * m[14] + m[3] * m[4] * m[10] * m[13] -
                m[3] * m[5] * m[10] * m[12] + m[3] * m[5] * m[8] * m[14] -
                m[3] * m[6] * m[8] * m[13] + m[3] * m[6] * m[9] * m[12]);
    }

    Matrix Inverse() {
        Matrix m = new Matrix();
        double d = Determinant();
        double M11 = rows[0].lane(0);
        double M12 = rows[0].lane(1);
        double M13 = rows[0].lane(2);
        double M14 = rows[0].lane(3);
        double M21 = rows[1].lane(0);
        double M22 = rows[1].lane(1);
        double M23 = rows[1].lane(2);
        double M24 = rows[1].lane(3);
        double M31 = rows[2].lane(0);
        double M32 = rows[2].lane(1);
        double M33 = rows[2].lane(2);
        double M34 = rows[2].lane(3);
        double M41 = rows[3].lane(0);
        double M42 = rows[3].lane(1);
        double M43 = rows[3].lane(2);
        double M44 = rows[3].lane(3);
        m.rows[0] = DoubleVector.fromArray(SPECIES, new double[]{
            (M23 * M34 * M42 - M24 * M33 * M42 + M24 * M32 * M43 - M22 * M34 * M43 - M23 * M32 * M44 + M22 * M33 * M44) / d,
            (M14 * M33 * M42 - M13 * M34 * M42 - M14 * M32 * M43 + M12 * M34 * M43 + M13 * M32 * M44 - M12 * M33 * M44) / d,
            (M13 * M24 * M42 - M14 * M23 * M42 + M14 * M22 * M43 - M12 * M24 * M43 - M13 * M22 * M44 + M12 * M23 * M44) / d,
            (M14 * M23 * M32 - M13 * M24 * M32 - M14 * M22 * M33 + M12 * M24 * M33 + M13 * M22 * M34 - M12 * M23 * M34) / d
        }, 0);
        m.rows[1] = DoubleVector.fromArray(SPECIES, new double[]{
            (M24 * M33 * M41 - M23 * M34 * M41 - M24 * M31 * M43 + M21 * M34 * M43 + M23 * M31 * M44 - M21 * M33 * M44) / d,
            (M13 * M34 * M41 - M14 * M33 * M41 + M14 * M31 * M43 - M11 * M34 * M43 - M13 * M31 * M44 + M11 * M33 * M44) / d,
            (M14 * M23 * M41 - M13 * M24 * M41 - M14 * M21 * M43 + M11 * M24 * M43 + M13 * M21 * M44 - M11 * M23 * M44) / d,
            (M13 * M24 * M31 - M14 * M23 * M31 + M14 * M21 * M33 - M11 * M24 * M33 - M13 * M21 * M34 + M11 * M23 * M34) / d
        }, 0);
        m.rows[2] = DoubleVector.fromArray(SPECIES, new double[]{
            (M22 * M34 * M41 - M24 * M32 * M41 + M24 * M31 * M42 - M21 * M34 * M42 - M22 * M31 * M44 + M21 * M32 * M44) / d,
            (M14 * M32 * M41 - M12 * M34 * M41 - M14 * M31 * M42 + M11 * M34 * M42 + M12 * M31 * M44 - M11 * M32 * M44) / d,
            (M12 * M24 * M41 - M14 * M22 * M41 + M14 * M21 * M42 - M11 * M24 * M42 - M12 * M21 * M44 + M11 * M22 * M44) / d,
            (M14 * M22 * M31 - M12 * M24 * M31 - M14 * M21 * M32 + M11 * M24 * M32 + M12 * M21 * M34 - M11 * M22 * M34) / d
        }, 0);
        m.rows[3] = DoubleVector.fromArray(SPECIES, new double[]{
            (M23 * M32 * M41 - M22 * M33 * M41 - M23 * M31 * M42 + M21 * M33 * M42 + M22 * M31 * M43 - M21 * M32 * M43) / d,
            (M12 * M33 * M41 - M13 * M32 * M41 + M13 * M31 * M42 - M11 * M33 * M42 - M12 * M31 * M43 + M11 * M32 * M43) / d,
            (M13 * M22 * M41 - M12 * M23 * M41 - M13 * M21 * M42 + M11 * M23 * M42 + M12 * M21 * M43 - M11 * M22 * M43) / d,
            (M12 * M23 * M31 - M13 * M22 * M31 + M13 * M21 * M32 - M11 * M23 * M32 - M12 * M21 * M33 + M11 * M22 * M33) / d
        }, 0);
        return m;
    }
}
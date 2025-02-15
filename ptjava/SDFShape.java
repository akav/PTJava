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

interface SDF {
    double Evaluate(Vector p);
    Box BoundingBox();
}

class SDFShape extends TransformedShape implements SDF {
    SDF SDF;
    Material SDFMaterial;

    SDFShape(SDF sdf, Material material) {
        this.SDF = sdf;
        this.SDFMaterial = material;

    }

    SDFShape NewSDFShape(SDF sdf, Material material) {
        return new SDFShape(sdf, material);
    }

    @Override
    public void Compile() {
    }

    @Override
    public Material MaterialAt(Vector p) {
        return this.SDFMaterial;
    }

    @Override
    public Hit Intersect(Ray ray) {
        double epsilon = 0.0001;
        double start = 0.0001;
        double jumpSize = 0.001;
        Box box = this.BoundingBox();
        var ts = box.Intersect(ray);
        Double t1 = ts[0];
        Double t2 = ts[1];

        if (t2 < t1 || t2 < 0) {
            return Hit.NoHit;
        }

        double t = Math.max(start, t1);
        boolean jump = true;

        for (int i = 0; i < 1000; i++) {
            //this.Evaluate(ray.Position(t));
        }

        return Hit.NoHit;
    }

    @Override
    public Vector UV(Vector p) {
        return new Vector();
    }

    @Override
    public Vector NormalAt(Vector p) {
        double e = 0.0001;
        double x = p.getX();
        double y = p.getY();
        double z = p.getZ();

        return new Vector(this.Evaluate(new Vector(x - e, y, z)) - this.Evaluate(new Vector(x + e, y, z)),
                this.Evaluate(new Vector(x, y - e, z)) - this.Evaluate(new Vector(x, y + e, z)),
                this.Evaluate(new Vector(x, y, z - e)) - this.Evaluate(new Vector(x, y, z + e)));
    }

    @Override
    public double Evaluate(Vector p) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    class SphereSDF implements SDF {

        double Radius;
        double Exponent;

        SphereSDF(double Radius, double Exponent) {
            this.Radius = Radius;
            this.Exponent = Exponent;
        }

        @Override
        public double Evaluate(Vector p) {
            return p.LengthN(Exponent) - Radius;
        }

        SDF NewSphereSDF(double radius) {
            return new SphereSDF(radius, 2);
        }

        @Override
        public Box BoundingBox() {
            double r = Radius;
            return new Box(new Vector(-r, -r, -r), new Vector(r, r, r));
        }
    }

    class CubeSDF implements SDF {

        Vector Size;

        CubeSDF(Vector size) {
            Size = size;
        }

        SDF NewCubeSDF(Vector size) {
            return new CubeSDF(size);
        }

        @Override
        public Box BoundingBox() {
            double x, y, z;
            x = Size.getX() / 2;
            y = Size.getY() / 2;
            z = Size.getZ() / 2;

            return new Box(new Vector(-y, -y, -z), new Vector(x, y, z));
        }

        @Override
        public double Evaluate(Vector p) {
            double x = p.getX();
            double y = p.getY();
            double z = p.getZ();
            
            if (x < 0) {
                x = -x;
            }

            if (y < 0) {
                y = -y;
            }

            if (z < 0) {
                z = -z;
            }

            x -= this.Size.getX() / 2;
            y -= this.Size.getY() / 2;
            z -= this.Size.getZ() / 2;

            double a = x;

            if (y > a) {
                a = y;
            }

            if (z > a) {
                a = z;
            }

            if (a > 0) {
                a = 0;
            }

            if (x < 0) {
                x = 0;
            }

            if (y < 0) {
                y = 0;
            }

            if (z < 0) {
                z = 0;
            }

            double b = Math.sqrt(x * x + y * y + z * z);
            return a + b;
        }

    }

    class CylinderSDF implements SDF {

        double Radius;
        double Height;

        CylinderSDF(double Radius, double Height) {
            this.Radius = Radius;
            this.Height = Height;
        }

        SDF NewCylinderSDF(double radius, double height) {
            return new CylinderSDF(radius, height);
        }

        @Override
        public Box BoundingBox() {
            double r = Radius;
            double h = Height / 2;
            return new Box(new Vector(-r, -h, -r), new Vector(r, h, r));
        }

        @Override
        public double Evaluate(Vector p) {
            double x = Math.sqrt(p.getX() * p.getX() + p.getZ() * p.getZ());
            double y = p.getY();

            if (x < 0) {
                x = -x;
            }

            if (y < 0) {
                y = -y;
            }

            x -= this.Radius;
            y -= this.Height / 2;
            double a = x;

            if (y > a) {
                a = y;
            }

            if (a > 0) {
                a = 0;
            }

            if (x < 0) {
                x = 0;
            }

            if (y < 0) {
                y = 0;
            }

            double b = Math.sqrt(x * x + y * y);
            return a + b;
        }
    }

    class CapsuleSDF implements SDF {

        Vector A, B;
        double Radius;
        double Exponent;

        CapsuleSDF(Vector A, Vector B, double Radius, double Exponent) {
            this.A = A;
            this.B = B;
            this.Radius = Radius;
            this.Exponent = Exponent;
        }

        SDF NewCapsuleSDF(Vector a, Vector b, double radius) {
            return new CapsuleSDF(a, b, radius, 2);
        }

        @Override
        public double Evaluate(Vector p) {
            Vector pa = p.Sub(this.A);
            Vector ba = this.B.Sub(this.A);
            double h = Math.max(0, Math.min(1, pa.Dot(ba) / ba.Dot(ba)));
            return pa.Sub(ba.MulScalar(h)).LengthN(this.Exponent) - this.Radius;
        }

        @Override
        public Box BoundingBox() {
            Vector a = this.A.Min(this.B);
            Vector b = this.A.Max(this.B);
            return new Box(a.SubScalar(this.Radius), b.AddScalar(this.Radius));
        }
    }

    class TorusSDF implements SDF {

        double MajorRadius;
        double MinRadius;
        double MajorExponent;
        double MinorExponent;

        TorusSDF(double MajorRadius, double MinRadius, double MajorExponent, double MinorExponent) {
            this.MajorRadius = MajorRadius;
            this.MinRadius = MinRadius;
            this.MajorExponent = MajorExponent;
            this.MinorExponent = MinorExponent;
        }

        SDF NewTorusSDF(double major, double minor) {
            return new TorusSDF(major, minor, 2, 2);
        }

        @Override
        public double Evaluate(Vector p) {
            //Vector q = new Vector(new Vector(p.X, p.Y, 0).LengthN(this.MajorExponent) - this.MajorRadius, p.Z, 0);
            Vector q = new Vector(new Vector(p.getX(), p.getY(), 0).LengthN(this.MajorExponent) - this.MajorRadius, p.getZ(), 0);
            return q.LengthN(this.MinorExponent) - this.MinRadius;
        }

        @Override
        public Box BoundingBox() {
            double a = this.MinRadius;
            double b = this.MinRadius + this.MajorRadius;
            return new Box(new Vector(-b, -b, a), new Vector(b, b, a));
        }

    }

    class TransformSDF implements SDF {

        SDF SDF;
        Matrix Matrix;
        Matrix Inverse;

        TransformSDF(SDF SDF, Matrix Matrix, Matrix Inverse) {
            this.SDF = SDF;
            this.Matrix = Matrix;
            this.Inverse = Inverse;
        }

        SDF NewTransformSDF(SDF sdf, Matrix matrix) {
            return new TransformSDF(sdf, matrix, matrix.Inverse());
        }

        @Override
        public double Evaluate(Vector p) {
            Vector q = this.Inverse.MulPosition(p);
            return this.SDF.Evaluate(q);
        }

        @Override
        public Box BoundingBox() {
            return this.Matrix.MulBox(this.SDF.BoundingBox());
        }

    }

    class ScaleSDF implements SDF {

        SDF SDF;
        double Factor;

        ScaleSDF(SDF sdf, double Factor) {
            this.SDF = sdf;
            this.Factor = Factor;
        }

        SDF NewScaleSDF(SDF sdf, double factor) {
            return new ScaleSDF(sdf, factor);
        }

        @Override
        public double Evaluate(Vector p) {
            return this.SDF.Evaluate(p.DivScalar(this.Factor)) * this.Factor;
        }

        @Override
        public Box BoundingBox() {
            double f = this.Factor;
            Matrix m = new Matrix().Scale(new Vector(f, f, f));
            return m.MulBox(this.SDF.BoundingBox());
        }
    }

    class UnionSDF implements SDF {

        SDF[] Items;

        UnionSDF(SDF[] Items) {
            this.Items = Items;
        }

        SDF NewUnionSDF(SDF[] items) {
            return new UnionSDF(items);
        }

        @Override
        public double Evaluate(Vector p) {
            double result = 0;
            int i = 0;
            for (SDF item : this.Items) {
                double d = item.Evaluate(p);
                if (i == 0 || d < result) {
                    result = d;
                }
                i++;
            }
            return result;
        }

        @Override
        public Box BoundingBox() {
            Box result = null;
            Box box;
            int i = 0;
            for (SDF item : this.Items) {
                box = item.BoundingBox();
                if (i == 0) {
                    result = box;

                } else {
                    result = result.Extend(box);
                }
                i++;
            }
            return result;
        }
    }

    class DifferenceSDF implements SDF {

        SDF[] Items;

        DifferenceSDF(SDF[] Items) {
            this.Items = Items;
        }

        SDF NewDifferenceSDF(SDF[] items) {
            return new DifferenceSDF(items);
        }

        @Override
        public double Evaluate(Vector p) {
            double result = 0;
            int i = 0;

            for (SDF item : this.Items) {
                double d = item.Evaluate(p);
                if (i == 0) {
                    result = d;
                } else if (-d > result) {
                    result = -d;
                }
                i++;
            }

            return result;
        }

        @Override
        public Box BoundingBox() {
            return this.Items[0].BoundingBox();
        }
    }

    class IntersectionSDF implements SDF {

        SDF[] Items;

        IntersectionSDF(SDF[] Items) {
            this.Items = Items;
        }

        @Override
        public double Evaluate(Vector p) {
            double result = 0;
            int i = 0;

            for (SDF item : this.Items) {
                double d = item.Evaluate(p);
                if (i == 0 || d > result) {
                    result = d;
                }
                i++;
            }

            return result;
        }

        @Override
        public Box BoundingBox() {
            Box result = null;
            int i = 0;

            for (SDF item : this.Items) {
                Box box = item.BoundingBox();
                if (i == 0) {
                    result = box;
                } else {
                    result = result.Extend(box);
                }
                i++;
            }

            return result;
        }
    }

    class RepeatSDF implements SDF {

        SDF SDF;
        Vector Step;

        RepeatSDF(SDF sdf, Vector step) {
            SDF = sdf;
            Step = step;
        }

        @Override
        public double Evaluate(Vector p) {
            Vector q = p.Mod(Step).Sub(Step.DivScalar(2));
            return SDF.Evaluate(q);
        }

        @Override
        public Box BoundingBox() {
            return new Box();
        }
    }
}
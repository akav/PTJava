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

class Mesh extends TransformedShape {

    Triangle[] Triangles;
    Box box;
    Tree tree;

    Mesh() {
    }

    Mesh(Triangle[] triangles_, Box box_, Tree tree_) {
        Triangles = triangles_;
        box = box_;
        tree = tree_;
    }

    static Mesh NewMesh(Triangle[] triangle) {
        return new Mesh(triangle, null, null);
    }

    void dirty() {
        box = null;
        tree = null;
    }

    Mesh Copy() {
        Triangle[] triangle = new Triangle[Triangles.length];
        System.arraycopy(Triangles, 0, triangle, 0, Triangles.length);
        return NewMesh(triangle);
    }

    @Override
    public void Compile() {
        if (tree == null) {
            IShape[] shapes = new IShape[this.Triangles.length];
            int i = 0;
            for (Triangle triangle : this.Triangles) {
                shapes[i] = triangle;
                i++;
            }
            this.tree = new Tree(shapes);
        }
    }

    void Add(Mesh b) {
        int aLength = this.Triangles.length;
        int bLength = b.Triangles.length;
        Triangle[] newTriangles = new Triangle[aLength + bLength];
        System.arraycopy(this.Triangles, 0, newTriangles, 0, aLength);
        System.arraycopy(b.Triangles, 0, newTriangles, aLength, bLength);
        this.Triangles = newTriangles;
        this.dirty();
    }

    @Override
    public Box BoundingBox() {
        if (this.box == null) {
            Vector min = this.Triangles[0].V1;
            Vector max = this.Triangles[0].V1;
            for (Triangle t : this.Triangles) {
                if (t != null) {
                    min = min.Min(t.V1).Min(t.V2).Min(t.V3);
                    max = max.Max(t.V1).Max(t.V2).Max(t.V3);
                }
            }
            this.box = new Box(min, max);
        }
        return this.box;
    }

    @Override
    public Hit Intersect(Ray r) {
        return this.tree.Intersect(r);
    }

    @Override
    public Vector UV(Vector p) {
        return new Vector();
    }

    @Override
    public Material MaterialAt(Vector p) {
        return new Material();
    }

    @Override
    public Vector NormalAt(Vector p) {
        return new Vector();
    }

    Vector smoothNormalsThreshold(Vector normal, Vector[] normals, double threshold) {
        Vector result = new Vector();
        for (Vector x : normals) {
            if (x.Dot(normal) >= threshold) {
                result = result.Add(x);
            }
        }
        return result.Normalize();
    }

    void SmoothNormalsThreshold(double radians) {
        // TODO
    }

    void SmoothNormals() {
        // TODO
    }

    void UnitCube() {
        this.FitInside(new Box(new Vector(0, 0, 0), new Vector(1, 1, 1)), new Vector(0, 0, 0));
        this.MoveTo(new Vector(), new Vector(0.5, 0.5, 0.5));
    }

    void MoveTo(Vector position, Vector anchor) {
        Matrix matrix = Matrix.Translate(position.Sub(this.BoundingBox().Anchor(anchor)));
        this.Transform(matrix);
    }

    void Transform(Matrix matrix) {
        for (Triangle t : this.Triangles) {
            t.V1 = matrix.MulPosition(t.V1);
            t.V2 = matrix.MulPosition(t.V2);
            t.V3 = matrix.MulPosition(t.V3);
            t.N1 = matrix.MulDirection(t.N1);
            t.N2 = matrix.MulDirection(t.N2);
            t.N3 = matrix.MulDirection(t.N3);
        }
        this.dirty();
    }

    void FitInside(Box box, Vector anchor) {
        double scale = box.Size().Div(BoundingBox().Size()).MinComponent();
        Vector extra = box.Size().Sub(BoundingBox().Size().MulScalar(scale));
        Matrix matrix = ptjava.Matrix.Identity;
        matrix = matrix.Translate(BoundingBox().Min.Negate()).Mul(matrix);
        matrix = matrix.Scale(new Vector(scale, scale, scale)).Mul(matrix);
        matrix = matrix.Translate(box.Min.Add(extra.Mul(anchor))).Mul(matrix);
        this.Transform(matrix);
    }

    void SetMaterial(Material material) {
        for (Triangle t : this.Triangles) {
            t.Material = material;
        }
    }

    void SaveSTL(String path) {
        // TODO
    }
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

class Mesh implements IShape {

    Triangle[] triangles;
    Box box;
    Tree tree;
    Colour color;

    Mesh() {
    }

    Mesh(Triangle[] triangles, Box box, Tree tree) {
        this.triangles = triangles;
        this.box = box;
        this.tree = tree;
        System.out.println("Mesh initialized with " + triangles.length + " triangles.");

    }

    public Mesh(List<IShape> shapes) {
        this.triangles = shapes.stream()
                               .filter(shape -> shape instanceof Triangle)
                               .toArray(Triangle[]::new);
        this.box = null;
        this.tree = null;
    }

    public static Mesh NewMesh(Triangle[] triangles) {
        return new Mesh(triangles, null, null);
    }

    void dirty() {
        box = null;
        tree = null;
    }

    Mesh Copy() {
        Triangle[] trianglesCopy = new Triangle[triangles.length];
        System.arraycopy(triangles, 0, trianglesCopy, 0, triangles.length);
        return NewMesh(trianglesCopy);
    }

    @Override
    public void Compile() {
        if (tree == null) {
            IShape[] shapes = new IShape[triangles.length];
            System.arraycopy(triangles, 0, shapes, 0, triangles.length);
            tree = Tree.NewTree(shapes);
            System.out.println("Tree compiled with " + shapes.length + " shapes.");
        }
    }

    void Add(Mesh b) {
        Triangle[] all = new Triangle[triangles.length + b.triangles.length];
        System.arraycopy(triangles, 0, all, 0, triangles.length);
        System.arraycopy(b.triangles, 0, all, triangles.length, b.triangles.length);
        triangles = all;
        dirty();
    }

    @Override
    public Hit Intersect(Ray r) {
        return tree.Intersect(r);
    }

    @Override
    public Box BoundingBox() {
        if (triangles.length == 0) {
            return new Box(new Vector(), new Vector());
        }

        if (box == null) {
            Vector min = triangles[0].V1;
            Vector max = triangles[0].V1;

            for (Triangle t : triangles) {
                min = min.Min(t.V1).Min(t.V2).Min(t.V3);
                max = max.Max(t.V1).Max(t.V2).Max(t.V3);
            }
            box = new Box(min, max);
        }
        return box;
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
        double threshold = Math.cos(radians);

        List<Vector> NL1 = new ArrayList<>();
        List<Vector> NL2 = new ArrayList<>();
        List<Vector> NL3 = new ArrayList<>();

        Map<Vector, List<Vector>> lookup = new HashMap<>();

        for (Triangle t : triangles) {
            NL1.add(t.N1);
            NL2.add(t.N2);
            NL3.add(t.N3);

            lookup.put(t.V1, new ArrayList<>(NL1));
            lookup.put(t.V2, new ArrayList<>(NL2));
            lookup.put(t.V3, new ArrayList<>(NL3));
        }

        for (Triangle t : triangles) {
            t.N1 = smoothNormalsThreshold(t.N1, lookup.get(t.V1).toArray(new Vector[0]), threshold);
            t.N2 = smoothNormalsThreshold(t.N2, lookup.get(t.V2).toArray(new Vector[0]), threshold);
            t.N3 = smoothNormalsThreshold(t.N3, lookup.get(t.V3).toArray(new Vector[0]), threshold);
        }
    }

    void SmoothNormals() {
        Map<Vector, Vector> lookup = new HashMap<>();

        for (Triangle t : triangles) {
            lookup.put(t.V1, new Vector());
            lookup.put(t.V2, new Vector());
            lookup.put(t.V3, new Vector());
        }

        for (Triangle t : triangles) {
            lookup.put(t.V1, lookup.get(t.V1).Add(t.N1));
            lookup.put(t.V2, lookup.get(t.V2).Add(t.N2));
            lookup.put(t.V3, lookup.get(t.V3).Add(t.N3));
        }

        for (Map.Entry<Vector, Vector> entry : lookup.entrySet()) {
            lookup.put(entry.getKey(), entry.getValue().Normalize());
        }

        for (Triangle t : triangles) {
            t.N1 = lookup.get(t.V1);
            t.N2 = lookup.get(t.V2);
            t.N3 = lookup.get(t.V3);
        }
    }

    void UnitCube() {
        FitInside(new Box(new Vector(0, 0, 0), new Vector(1, 1, 1)), new Vector(0, 0, 0));
        MoveTo(new Vector(0, 0, 0), new Vector(0.5, 0.5, 0.5));
    }

    public void MoveTo(Vector position, Vector anchor) {
        Matrix matrix = new Matrix().Translate(position.Sub(BoundingBox().Anchor(anchor)));
        Transform(matrix);
    }

    void FitInside(Box box, Vector anchor) {
        double scale = box.Size().Div(BoundingBox().Size()).MinComponent();
        Vector extra = box.Size().Sub(BoundingBox().Size().MulScalar(scale));
        Matrix matrix = Matrix.Identity;
        matrix = matrix.Translate(BoundingBox().Min.Negate());
        matrix = matrix.Scale(new Vector(scale, scale, scale));
        matrix = matrix.Translate(box.Min.Add(extra.Mul(anchor)));
        Transform(matrix);
    }

    void Transform(Matrix matrix) {
        // Create a copy of triangles array to update vertices and normals
        Triangle[] trianglesCopy = new Triangle[triangles.length];
        System.arraycopy(triangles, 0, trianglesCopy, 0, triangles.length);

        for (int i = 0; i < trianglesCopy.length; i++) {
            trianglesCopy[i].V1 = matrix.MulPosition(trianglesCopy[i].V1);
            trianglesCopy[i].V2 = matrix.MulPosition(trianglesCopy[i].V2);
            trianglesCopy[i].V3 = matrix.MulPosition(trianglesCopy[i].V3);
            trianglesCopy[i].N1 = (trianglesCopy[i].N1 != null) ? matrix.MulDirection(trianglesCopy[i].N1) : new Vector();
            trianglesCopy[i].N2 = (trianglesCopy[i].N2 != null) ? matrix.MulDirection(trianglesCopy[i].N2) : new Vector();
            trianglesCopy[i].N3 = (trianglesCopy[i].N3 != null) ? matrix.MulDirection(trianglesCopy[i].N3) : new Vector();
        }

        // Replace triangles array with the updated copy
        triangles = trianglesCopy;

        dirty();
    }

    void SetMaterial(Material material) {
        for (Triangle t : triangles) {
            t.Material = material;
        }
    }
}

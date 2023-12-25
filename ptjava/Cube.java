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

class Cube extends TransformedShape {

    Vector Min;
    Vector Max;
    Material Material;
    Box Box;

    static final double EPS = 1e-9;

    Cube(Vector min, Vector max, Material material, Box box) {
        this.Min = min;
        this.Max = max;
        this.Material = material;
        this.Box = box;
    }

    static Cube NewCube(Vector min, Vector max, Material material) {
        Box box = new Box(min, max);
        return new Cube(min, max, material, box);
    }

    @Override
    public void Compile() {

    }

    @Override
    public Box BoundingBox() {
        return Box;
    }

    @Override
    public Hit Intersect(Ray r) {
        Vector n = Min.Sub(r.Origin).Div(r.Direction);
        Vector f = Max.Sub(r.Origin).Div(r.Direction);
                
        Vector min = n.Min(f);
        Vector max = n.Max(f);

        //double t0 = Math.max(Math.max(min.X, min.Y), min.Z);
        //double t1 = Math.min(Math.min(max.X, max.Y), max.Z);
        double t0 = Math.max(Math.max(min.getX(), min.getY()), min.getZ());
        double t1 = Math.min(Math.min(max.getX(), max.getY()), max.getZ());

        if (t0 > 0 && t0 < t1) {
            return new Hit(this, t0, null); // Assuming this constructor for Hit
        }

        return Hit.NoHit;
    }
    
    @Override
    public Vector UV(Vector p) {
        p = p.Sub(Min).Div(Max.Sub(Min));
        //return new Vector(p.X, p.Z, 0);
        return new Vector(p.getX(), p.getZ(), 0);
    }

    @Override
    public Material MaterialAt(Vector p) {
        return Material;
    }

    @Override
    public Vector NormalAt(Vector p) {
                
       /* if (p.X < this.Min.X + EPS) {
            return new Vector(-1, 0, 0);
        } else if (p.X > this.Max.X - EPS) {
            return new Vector(1, 0, 0);
        } else if (p.Y < this.Min.Y + EPS) {
            return new Vector(0, -1, 0);
        } else if (p.Y > this.Max.Y - EPS) {
            return new Vector(0, 1, 0);
        } else if (p.Z < this.Min.Z + EPS) {
            return new Vector(0, 0, -1);
        } else if (p.Z > this.Max.Z - EPS) {
            return new Vector(0, 0, 1);
        }
        return new Vector(0, 1, 0);       */
        if (p.getX() < this.Min.getX() + EPS) {
            return new Vector(-1, 0, 0);
        } else if (p.getX() > this.Max.getX() - EPS) {
            return new Vector(1, 0, 0);
        } else if (p.getY() < this.Min.getY() + EPS) {
            return new Vector(0, -1, 0);
        } else if (p.getY() > this.Max.getY() - EPS) {
            return new Vector(0, 1, 0);
        } else if (p.getZ() < this.Min.getZ() + EPS) {
            return new Vector(0, 0, -1);
        } else if (p.getZ() > this.Max.getZ() - EPS) {
            return new Vector(0, 0, 1);
        }
        return new Vector(0, 1, 0);       
    }

    Mesh Mesh() {
        Vector a = Min;
        Vector b = Max;
        Vector z = new Vector();
        Material m = Material;
        //var v000 = new Vector(a.X, a.Y, a.Z);
        //var v001 = new Vector(a.X, a.Y, b.Z);
        //var v010 = new Vector(a.X, b.Y, a.Z);
        //var v011 = new Vector(a.X, b.Y, b.Z);
        //var v100 = new Vector(b.X, a.Y, a.Z);
        //var v101 = new Vector(b.X, a.Y, b.Z);
        //var v110 = new Vector(b.X, b.Y, a.Z);
        //var v111 = new Vector(b.X, b.Y, b.Z);
        var v000 = new Vector(a.getX(), a.getY(), a.getZ());
        var v001 = new Vector(a.getX(), a.getY(), b.getZ());
        var v010 = new Vector(a.getX(), b.getY(), a.getZ());
        var v011 = new Vector(a.getX(), b.getY(), b.getZ());
        var v100 = new Vector(b.getX(), a.getY(), a.getZ());
        var v101 = new Vector(b.getX(), a.getY(), b.getZ());
        var v110 = new Vector(b.getX(), b.getY(), a.getZ());
        var v111 = new Vector(b.getX(), b.getY(), b.getZ());

        Triangle[] triangles = {
            Triangle.NewTriangle(v000, v100, v110, z, z, z, m),
            Triangle.NewTriangle(v000, v110, v010, z, z, z, m),
            Triangle.NewTriangle(v001, v101, v111, z, z, z, m),
            Triangle.NewTriangle(v001, v111, v011, z, z, z, m),
            Triangle.NewTriangle(v000, v100, v101, z, z, z, m),
            Triangle.NewTriangle(v000, v101, v001, z, z, z, m),
            Triangle.NewTriangle(v010, v110, v111, z, z, z, m),
            Triangle.NewTriangle(v010, v111, v011, z, z, z, m),
            Triangle.NewTriangle(v000, v010, v011, z, z, z, m),
            Triangle.NewTriangle(v000, v011, v001, z, z, z, m),
            Triangle.NewTriangle(v100, v110, v111, z, z, z, m),
            Triangle.NewTriangle(v100, v111, v101, z, z, z, m)
        };
        return new Mesh(triangles, null, null);
    }
}

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

interface Func extends IShape {

    double func(double x, double y);
}

class Function implements Func {

    Func Function;
    Box Box;
    Material Material;

    Function() {
    }

    Function(Func Function, Box Box, Material Material) {
        this.Function = Function;
        this.Box = Box;
        this.Material = Material;
    }

    IShape NewFunction(Func function, Box box, Material material) {
        return new Function(function, box, material);
    }

    @Override
    public void Compile() {
    }

    @Override
    public Box BoundingBox() {
        return this.Box;
    }

    boolean Contains(Vector v) {
        //return v.Z < Function.func(v.X, v.Y);
        return v.getZ() < Function.func(v.getX(), v.getY());
    }

    @Override
    public Hit Intersect(Ray ray) {
        double step = 1.0 / 32;
        boolean sign = Contains(ray.Position(step));
        for (double t = step; t < 12; t += step) {
            Vector v = ray.Position(t);
            if (Contains(v) != sign && Box.Contains(v)) {
                return new Hit(this, t - step, null);
            }
        }
        return Hit.NoHit;
    }

    @Override
    public Vector UV(Vector p) {
        //double x1 = Box.Min.X;
        //double x2 = Box.Max.X;
        //double y1 = Box.Min.Y;
        //double y2 = Box.Max.Y;
        //double u = p.X - x1 / x2 - x1;
        //double v = p.Y - y1 / y2 - y1;
        double x1 = Box.Min.getX();
        double x2 = Box.Max.getX();
        double y1 = Box.Min.getY();
        double y2 = Box.Max.getY();
        double u = p.getX() - x1 / x2 - x1;
        double v = p.getY() - y1 / y2 - y1;
        return new Vector(u, v, 0);
    }

    @Override
    public Material MaterialAt(Vector p) {
        return this.Material;
    }

    @Override
    public Vector NormalAt(Vector p) {
        double eps = 1e-3;
        //double x = Function.func(p.X - eps, p.Y) - Function.func(p.X + eps, p.Y);
        //double y = Function.func(p.X, p.Y - eps) - Function.func(p.X, p.Y + eps);
        double x = Function.func(p.getX() - eps, p.getY()) - Function.func(p.getX() + eps, p.getY());
        double y = Function.func(p.getX(), p.getY() - eps) - Function.func(p.getX(), p.getY() + eps);
        double z = 2 * eps;
        Vector v = new Vector(x, y, z);
        return v.Normalize();
    }

    @Override
    public double func(double x, double y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

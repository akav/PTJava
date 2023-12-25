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
import ptjava.Hit.HitInfo;

class TransformedShape implements IShape {

    IShape Shape;
    Matrix Matrix;
    Matrix Inverse;

    TransformedShape() {}
    
    TransformedShape(IShape s, Matrix m) {
        Shape = s;        
        Matrix = m;
        Inverse = m.Inverse();
    }

    @Override
    public void Compile() {
        Shape.Compile();
    }
    
    static IShape NewTransformedShape(IShape s, Matrix m) {
        return new TransformedShape(s, m);
    }
    
    @Override
    public Box BoundingBox() {
        return Matrix.MulBox(Shape.BoundingBox());
    }

    @Override
    public Hit Intersect(Ray r) {
        var shapeRay = Inverse.MulRay(r);
        var hit = Shape.Intersect(shapeRay);

        if(!hit.Ok())
        {
            return hit;
        }

        var shape = hit.Shape;
        var shapePosition = shapeRay.Position(hit.T);
        var shapeNormal = shape.NormalAt(shapePosition);
        var position = Matrix.MulPosition(shapePosition);
        var normal = Inverse.Transpose().MulDirection(shapeNormal);
        var material = Material.MaterialAt(shape, shapePosition);
        var inside = false;

        if(shapeNormal.Dot(shapeRay.Direction) > 0)
        {
            normal = normal.Negate();
            inside = true;
        }

        var ray = new Ray(position, normal);
        var info = new HitInfo(shape, position, normal, ray, material, inside);
        hit.T = position.Sub(r.Origin).Length();
        hit.HitInfo = info;
        return hit;
    }

    @Override
    public Vector UV(Vector uv) {
        return Shape.UV(uv);
    }

    @Override
    public Vector NormalAt(Vector normal) {
        return Shape.NormalAt(normal);
    }

    @Override
    public Material MaterialAt(Vector v) {
        return Shape.MaterialAt(v);
    }
}
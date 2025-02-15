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


class Box {

    public Vector Min;
    public Vector Max;
    boolean left;
    boolean right;

    Box() {
    }

    Box(Vector min, Vector max) {
        Min = min;
        Max = max;
    }

    static Box BoxForShapes(IShape[] shapes) {
        if (shapes.length == 0) {
            return new Box();
        }
        Box box = shapes[0].BoundingBox();

        for (IShape shape : shapes) {
            box = box.Extend(shape.BoundingBox());
        }
        return box;
    }

    static Box BoxForTriangles(Triangle[] shapes) {
        if (shapes.length == 0) {
            return new Box();
        }
        Box box = shapes[0].BoundingBox();
        for (Triangle shape : shapes) {
            box = box.Extend(shape.BoundingBox());
        }
        return box;
    }

    public Vector Anchor(Vector anchor) {
        return Min.Add(Size().Mul(anchor));
    }

    public Vector Center() {
        return Anchor(new Vector(0.5, 0.5, 0.5));
    }

    public double OuterRadius() {
        return Min.Sub(Center()).Length();
    }

    public double InnerRadius() {
        return Center().Sub(Min).MaxComponent();
    }

    public Vector Size() {
        return Max.Sub(Min);
    }

    public Box Extend(Box b) {
        return new Box(Min.Min(b.Min), Max.Max(b.Max));
    }

    public boolean Contains(Vector b) {
        return Min.getX() <= b.getX() && Max.getX()>= b.getX() &&
        Min.getY() <= b.getY() && Max.getY() >= b.getY() &&
        Min.getZ() <= b.getZ() && Max.getZ() >= b.getZ();
    }

    public boolean Intersects(Box b) {
        return ! (Min.getX() > b.Max.getX() 
        || Max.getX() < b.Min.getX() 
        || Min.getY() > b.Max.getY() 
        || Max.getY() < b.Min.getY() 
        || Min.getZ() > b.Max.getZ() 
        || Max.getZ() < b.Min.getZ());
        }

        public double[] Intersect(Ray r) {
        double x1 = (Min.getX() - r.Origin.getX()) / r.Direction.getX();
        double y1 = (Min.getY() - r.Origin.getY()) / r.Direction.getY();
        double z1 = (Min.getZ() - r.Origin.getZ()) / r.Direction.getZ();
        double x2 = (Max.getX() - r.Origin.getX()) / r.Direction.getX();
        double y2 = (Max.getY() - r.Origin.getY()) / r.Direction.getY();
        double z2 = (Max.getZ() - r.Origin.getZ()) / r.Direction.getZ();
        
        if(x1 > x2) {
            double temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if(y1 > y2) {
            double temp = y1;
            y1 = y2;
            y2 = temp;
        }
        if(z1 > z2) {
            double temp = z1;
            z1 = z2;
            z2 = temp;
        }
        
        double t1 = Math.max(Math.max(x1, y1), z1);
        double t2 = Math.min(Math.min(x2, y2), z2);
        return new double[]{t1, t2};
        }

        public boolean[] Partition(Axis axis, double point) {
        switch (axis) {
            case AxisX:
                left = Min.getX() <= point;
                right = Max.getX() >= point;
                break;
            case AxisY:
                left = Min.getY() <= point;
                right = Max.getY() >= point;
                break;
            case AxisZ:
                left = Min.getZ() <= point;
                right = Max.getZ() >= point;
                break;
            case AxisNone:
                left = false;
                right = false;
                break;
        }
    
        boolean partition[] = {left, right};
        return partition;
    }
}

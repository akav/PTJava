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

class Cylinder extends TransformedShape {
    
    double INF = 1e9;
    double EPS = 1e-9;
    double Radius;
    double Z0, Z1;
    Material CylinderMaterial;
    
    Cylinder(double radius, double z0, double z1, Material material) {
        this.Radius = radius;
        this.Z0 = z0;
        this.Z1 = z1;
        this.CylinderMaterial = material;
    }
    
    Cylinder NewCylinder(double radius, double z0, double z1, Material material) {
        return new Cylinder(radius, z0, z1, material);
    }
    
    IShape NewTransformedCylinder(Vector v0, Vector v1, double radius, Material material) {
        Vector up = new Vector(0,0,1);
        Vector d = v1.Sub(v0);
        double z = d.Length();
        double a = Math.acos(d.Normalize().Dot(up));
        Matrix m = new Matrix().Translate(v0);
        if (a!=0)
        {
            Vector u = d.Cross(up).Normalize();
            m = m.Rotate(u, a).Translate(v0);
        }
        Cylinder c = NewCylinder(radius, 0, z, material);
        return NewTransformedShape(c,m);
        
    } 
    
    @Override
    public Box BoundingBox() {
        double radius = this.Radius;
        return new Box(new Vector(-radius, -radius, this.Z0), new Vector(radius, radius, this.Z1));
    }
           
    @Override
    public Vector UV(Vector p) {
        return p;
    }
    
    @Override
    public Material MaterialAt(Vector p) {
        return this.CylinderMaterial;
    }

    @Override
    public Vector NormalAt(Vector p) {
        //p.Z = 0;
        p = new Vector(p.getX(), p.getY(), 0);
        return p.Normalize();
    }
    
    @Override
    public Hit Intersect(Ray ray) {
        double r = this.Radius;
        Vector o = ray.Origin;
        Vector d = ray.Direction;
        double a = d.getX() * d.getX() + d.getY() * d.getY();
        double b = 2 * o.getX() * d.getX() + 2 * o.getY() * d.getY();
        double c = o.getX() * o.getX() + o.getY() * o.getY() - r * r;
        double q = b*b - 4*a*c;
        if(q < EPS )
        {
            return Hit.NoHit;
        }
        double s = Math.sqrt(q);
        double t0 = (-b + s) / (2 * a);
        double t1 = (-b - s) / (2 * a);
        if (t0 > t1)
        {
            // swap values
            double temp = t0;
            t0 = t1;
            t1 = temp;
        }
        double z0 = o.getZ() + t0 * d.getZ();
        double z1 = o.getZ() + t1 * d.getZ();

        if (t0 > EPS && this.Z0 < z0 && z0 < this.Z1)
        {
            return new Hit(this, t0, null);
        }

        if (t1 > EPS && this.Z0 < z1 && z1 < this.Z1)
        {
            return new Hit(this, t1, null);
        }
        return Hit.NoHit;
    }
}

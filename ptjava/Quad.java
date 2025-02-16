package ptjava;

public class Quad implements IShape {
    Vector v0, v1, v2, v3;
    Material material;

    public Quad(Vector v0, Vector v1, Vector v2, Vector v3, Material material) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.material = material;
    }

    @Override
    public void Compile() {
        
    }

    @Override
    public Box BoundingBox() {
        Vector min = v0.Min(v1).Min(v2).Min(v3);
        Vector max = v0.Max(v1).Max(v2).Max(v3);
        return new Box(min, max);
    }

    @Override
    public Vector UV(Vector p) {
        return p;
    }

    @Override
    public Material MaterialAt(Vector p) {
        return this.material;
    }

    @Override
    public Vector NormalAt(Vector p) {
        Vector edge1 = v1.Sub(v0);
        Vector edge2 = v2.Sub(v0);
        return edge1.Cross(edge2).Normalize();
    }

    @Override
    public Hit Intersect(Ray ray) {
        Triangle t1 = new Triangle(v0, v1, v2, material);
        Triangle t2 = new Triangle(v0, v2, v3, material);
        Hit hit1 = t1.Intersect(ray);
        if (hit1.Ok()) return hit1;
        return t2.Intersect(ray);
    }
}
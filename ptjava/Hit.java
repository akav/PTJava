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

public class Hit {

    static final double INF = 1e9;
    IShape Shape;
    public double T;
    public HitInfo HitInfo;

    public static Hit NoHit = new Hit(null, INF, null);

    Hit(IShape shape, double t, HitInfo hinfo) {
        this.Shape = shape;
        this.T = t;
        this.HitInfo = hinfo;
    }

    HitInfo Info(Ray r) {

        if (HitInfo != null) {
            return HitInfo;
        }

        var shape = Shape;
        var position = r.Position(T);
        var normal = shape.NormalAt(position);
        var material = Material.MaterialAt(shape, position);
        var inside = false;

        if (normal.Dot(r.Direction) > 0) {
            normal = normal.Negate();
            inside = true;
    
            if (shape instanceof SDFShape || shape instanceof SphericalHarmonic) {
                inside = false;
            }
        }

        Ray ray = new Ray(position, normal);
        return new HitInfo(shape, position, normal, ray, material, inside);        
    }

    boolean Ok() {
        return T < INF;
    }

    public static class HitInfo {

        public IShape Shape;
        public Vector Position;
        public Vector Normal;
        public Ray Ray;
        public Material material;
        public boolean Inside;

        HitInfo(IShape shape, Vector position, Vector normal, Ray r, Material mat, boolean inside)
        {
            this.Shape = shape;
            this.Position = position;
            this.Normal = normal;
            this.Ray = r;
            this.material = mat;
            this.Inside = inside;
        }
    }
}

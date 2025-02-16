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

import java.util.concurrent.ThreadLocalRandom;
import ptjava.Hit.HitInfo;

public class Ray {

    public Vector Origin, Direction;
    public boolean reflected;
    boolean reflect;
    
    public Ray(Vector Origin, Vector Direction) {
        this.Origin = Origin;
        this.Direction = Direction;
    }

    public Vector Position(double t) {
        return Origin.Add(Direction.MulScalar(t));
    }

    public Ray Reflect(Ray i) {
        return new Ray(Origin, Direction.Reflect(i.Direction));
    }

    public Ray Refract(Ray i, double n1, double n2) {
        return new Ray(Origin, Direction.Refract(i.Direction, n1, n2));
    }

    public double Reflectance(Ray i, double n1, double n2) {
        return this.Direction.Reflectance(i.Direction, n1, n2);
    }

    public Ray WeightedBounce(double u, double v, ThreadLocalRandom rand) {
        var radius = Math.sqrt(u);
        var theta = 2 * Math.PI * v;
        var s = Direction.Cross(Vector.RandomUnitVector(rand)).Normalize();
        var t = Direction.Cross(s);
        var d = new Vector();
        d = d.Add(s.MulScalar(radius * Math.cos(theta)));
        d = d.Add(t.MulScalar(radius * Math.sin(theta)));
        d = d.Add(Direction.MulScalar(Math.sqrt(1 - u)));
        return new Ray(Origin, d);
    }

    public Ray ConeBounce(double theta, double u, double v, ThreadLocalRandom rand) {
        return new Ray(this.Origin, Util.Cone(Direction, theta, u, v, rand));
    }

    public BounceResult Bounce(HitInfo info, double u, double v, BounceType bounceType, ThreadLocalRandom rand) {
        Ray n = info.Ray;
        Material material = info.material;

        double n1 = 1.0;
        double n2 = material.Index;

        if (info.Inside) {
            
            double swap = n1;
            n1 = n2;
            n2 = swap;           
        }

        double p;
        
        if(material.Reflectivity >= 0) {
            p = material.Reflectivity;
         } else {
            p = n.Reflectance(this, n1, n2);
         } 
        
        boolean reflect = false;
        
        switch (bounceType) {
            case BounceTypeAny:
                reflect = rand.nextDouble() < p;
                break;
            case BounceTypeDiffuse:
                reflect = false;
                break;
            case BounceTypeSpecular:
                reflect = true;
                break;
        }

        if (reflect) {
            var reflected = n.Reflect(this);
            return new BounceResult(reflected.ConeBounce(material.Gloss, u, v, rand), true, p);
        } else if (material.Transparent) {
            var refracted = n.Refract(this, n1, n2);
            return new BounceResult(refracted.ConeBounce(material.Gloss, u, v, rand), true, 1-p);
        } else {
            return new BounceResult(n.WeightedBounce(u, v, rand), false, 1 - p);
        }
    }

    public static class BounceResult {
        private Ray ray;
        private boolean reflected;
        private double probability;
    
        public BounceResult(Ray ray, boolean reflected, double probability) {
            this.ray = ray;
            this.reflected = reflected;
            this.probability = probability;
        }
    
        public Ray getRay() {
            return ray;
        }
    
        public boolean isReflected() {
            return reflected;
        }
    
        public double getProbability() {
            return probability;
        }
    }
}
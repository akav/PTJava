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

interface Sampler {
    Colour Sample(Scene scene, Ray ray, ThreadLocalRandom rand);
}

class DefaultSampler implements Sampler {

    int FirstHitSamples;
    int MaxBounces;
    boolean DirectLighting;
    boolean SoftShadows;
    LightMode lightMode;
    SpecularMode specularMode;

    DefaultSampler() {

    }

    public DefaultSampler(int firstHitSamples, int maxBounces) {
        this.FirstHitSamples = firstHitSamples;
        this.MaxBounces = maxBounces;
        this.DirectLighting = true;
        this.SoftShadows = true;
        this.lightMode = LightMode.LightModeRandom;
        this.specularMode = SpecularMode.SpecularModeNaive;
    }

    public static DefaultSampler NewSampler(int firstHitSamples, int maxBounces) {
        return new DefaultSampler(firstHitSamples, maxBounces);
    }

    public static DefaultSampler NewDirectSampler() {
        return new DefaultSampler(1, 0);
    }

    @Override
    public Colour Sample(Scene scene, Ray ray, ThreadLocalRandom rand) {

        return sample(scene, ray, true, FirstHitSamples, 0, rand);
    }

    Colour sample(Scene scene, Ray ray, boolean emission, int samples, int depth, ThreadLocalRandom rand) {
        if (depth > MaxBounces) {
            return Colour.Black;
        }

        Hit hit = scene.Intersect(ray);

        if (!hit.Ok()) {
            return sampleEnvironment(scene, ray);
        }

        var info = hit.Info(ray);
        var material = info.material;
        var result = Colour.Black;

        if (material.Emittance > 0) {
            if (this.DirectLighting && !emission) {
                return Colour.Black;
            }
            result = result.Add(material.Color.MulScalar(material.Emittance * (double) samples));
        }

        int n = (int) Math.sqrt(samples);
        BounceType ma, mb;

        if (this.specularMode == SpecularMode.SpecularModeAll || depth == 0 && this.specularMode == SpecularMode.SpecularModeFirst) {
            ma = BounceType.BounceTypeDiffuse;
            mb = BounceType.BounceTypeSpecular;
        } else {
            ma = BounceType.BounceTypeAny;
            mb = BounceType.BounceTypeAny;
        }

        try {
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    for (BounceType mode = ma; mode.compareTo(mb) <= 0; mode = mode.next()) {
                        var fu = ((double)u + rand.nextDouble()) / (double)n;
                        var fv = ((double)v + rand.nextDouble()) / (double)n;                    
                        var bounceResult = ray.Bounce(info, fu, fv, mode, rand);
                        Ray newRay = (Ray)bounceResult._0;
                        boolean reflected = (boolean)bounceResult._1;
                        double p = (double)bounceResult._2;
                        
                        if (mode == BounceType.BounceTypeAny) {
                            p = 1.0;
                        }

                        if (p > 0 && reflected) {
                            // specular
                            var indirect = sample(scene, newRay, reflected, 1, depth + 1, rand);
                            Colour tinted = indirect.Mix(material.Color.Mul(indirect), material.Tint);
                            result = result.Add(tinted.MulScalar(p));
                        }

                        if (p > 0 && !reflected) {
                            // diffuse
                            Colour indirect = sample(scene, newRay, reflected, 1, depth + 1, rand);
                            Colour direct = Colour.Black;
                            
                            if (DirectLighting) {
                                direct = sampleLights(scene, info.Ray, rand);
                            }
                            result = result.Add(material.Color.Mul(direct.Add(indirect)).MulScalar(p));
                        }

                        if (mode == mb) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return result.DivScalar((double) (n * n));
    }    

    Colour sampleEnvironment(Scene scene, Ray ray) {
        if (scene.Texture != null) {
            Vector d = ray.Direction;
            double u = Math.atan2(d.getZ(), d.getX()) + scene.TextureAngle;
            double v = Math.atan2(d.getY(), new Vector(d.getX(), 0, d.getZ()).Length());
            u = (u + Math.PI) / (2 * Math.PI);
            v = (v + Math.PI / 2) / Math.PI;
            return scene.Texture.Sample(u, v);
        }
        return scene.Color;
    }

    Colour sampleLights(Scene scene, Ray n, ThreadLocalRandom rand) {
        int nLights = scene.Lights.length;
        if (nLights == 0) {
            return Colour.Black;
        }

        if (lightMode == LightMode.LightModeAll) {
            Colour result = new Colour();
            for (IShape light : scene.Lights) {
                if (light != null) {
                    result = result.Add(sampleLight(scene, n, rand, light));
                }
            }
            return result;
        } else {
            // pick a random light
            IShape light = scene.Lights[rand.nextInt(nLights)];
            return sampleLight(scene, n, rand, light).MulScalar((double)nLights);
        }
    }

    Colour sampleLight(Scene scene, Ray n, ThreadLocalRandom rand, IShape light) {
        
        Vector center = new Vector();
        double radius = 0;

        if (light != null) {
            if (light instanceof Sphere) {
                radius = ((Sphere) light).Radius;
                center = ((Sphere) light).Center;
            } else {
                Box box = light.BoundingBox();
                radius = box.OuterRadius();
                center = box.Center();
            }
        }

        var point = center;

        if (this.SoftShadows)
        {
            for (;;)
            {
                var x = rand.nextDouble() * 2 - 1;
                var y = rand.nextDouble() * 2 - 1;
                if (x * x + y * y <= 1)
                {
                    var l = center.Sub(n.Origin).Normalize();
                    var u = l.Cross(Vector.RandomUnitVector(rand)).Normalize();
                    var v = l.Cross(u);
                    point = new Vector();
                    point = point.Add(u.MulScalar(x * radius));
                    point = point.Add(v.MulScalar(y * radius));
                    point = point.Add(center);
                    break;
                }
            }
        }

        // construct ray toward light point
        Ray ray = new Ray(n.Origin, point.Sub(n.Origin).Normalize());

        // get cosine term
        var diffuse = ray.Direction.Dot(n.Direction);

        if (diffuse <= 0)
        {
            return Colour.Black;
        }

        // check for light visibility
        Hit hit = scene.Intersect(ray);

        if (!hit.Ok() || hit.Shape != light)
        {
            return Colour.Black;
        }

        // compute solid angle (hemisphere coverage)
        var hyp = center.Sub(n.Origin).Length();
        var opp = radius;
        var theta = Math.asin(opp / hyp);
        var adj = opp / Math.tan(theta);
        var d = Math.cos(theta) * adj;
        var r = Math.sin(theta) * adj;
        var coverage = (r * r) / (d * d);

        if (hyp < opp)
        {
            coverage = 1;
        }

        coverage = Math.min(coverage, 1);

        // get material properties from light
        Material material = Material.MaterialAt(light, point);

        // combine factors
        var m = material.Emittance * diffuse * coverage;
        return material.Color.MulScalar(m);
    }
}

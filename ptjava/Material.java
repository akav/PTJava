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

public class Material {

    Colour Color;
    ITexture Texture;
    ITexture NormalTexture;
    ITexture BumpTexture;
    ITexture GlossTexture;
    double BumpMultiplier;
    double Emittance;
    double Index;
    double Gloss;
    double Tint;
    double Reflectivity;
    Boolean Transparent;

    Material() {
    }

    Material(Colour color, ITexture texture, ITexture normaltexture, ITexture bumptexture, ITexture glosstexture, double b, double e, double i, double g, double tint, double r, Boolean t) {
        Color = color;
        Texture = texture;
        NormalTexture = normaltexture;
        BumpTexture = bumptexture;
        GlossTexture = glosstexture;
        BumpMultiplier = b;
        Emittance = e;
        Index = i;
        Gloss = g;
        Tint = tint;
        Reflectivity = r;
        Transparent = t;
    }

    public static Material DiffuseMaterial(Colour color)
    {
        return new Material(color, null, null, null, null, 1, 0, 1, 0, 0, -1, false);
    }

    public static Material SpecularMaterial(Colour color, double index)
    {
        return new Material(color, null, null, null, null, 1, 0, index, 0, 0, -1, false);
    }

    public static Material GlossyMaterial(Colour color, double index, double gloss)
    {
        return new Material(color, null, null, null, null, 1, 0, index, gloss, 0, -1, false);
    }

    public static Material ClearMaterial(double index, double gloss)
    {
        return new Material(new Colour(0, 0, 0), null, null, null, null, 1, 0, index, gloss, 0, -1, true);
    }

    public static Material TransparentMaterial(Colour color, double index, double gloss, double tint)
    {
        return new Material(color, null, null, null, null, 1, 0, index, gloss, tint, -1, true);
    }

    public static Material MetallicMaterial(Colour color, double gloss, double tint)
    {
        return new Material(color, null, null, null, null, 1, 0, 1, gloss, tint, 1, false);
    }

    public static Material LightMaterial(Colour color, double emittance)
    {
        return new Material(color, null, null, null, null, 1, emittance, 1, 0, 0, -1, false);
    }

    static Material MaterialAt(IShape shape, Vector point) {
        Material material = shape.MaterialAt(point);
        Vector uv = shape.UV(point);

        if (material.Texture != null) {
            material.Color = material.Texture.Sample(uv.getX(), uv.getY());
        }

        if (material.GlossTexture != null) {
            var c = material.GlossTexture.Sample(uv.getX(), uv.getY());
            material.Gloss = (c.r + c.g + c.b) / 3;
        }

        return material;
    }
}

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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

interface Texture {
    Colour Sample(double u, double v);
    Vector NormalSample(double u, double v);
    Vector BumpSample(double u, double v);
    Texture Pow(double a);
    Texture MulScalar(double a);
}

class ColorTexture implements Texture {

    int Width;
    int Height;
    Colour[] Data;

    double INF = 1e9;
    double EPS = 1e-9;

    // Create a Dictionary for texture maps
    static Map<String, ITexture> TextureMap = new HashMap<String, ITexture>();

    ColorTexture() {
        this.Width = 0;
        this.Height = 0;
        this.Data = new Colour[Height * Width + Width];
        Arrays.fill(this.Data, new Colour(0, 0, 0));
    }

    ColorTexture(int width, int height, Colour[] data) {
        this.Width = width;
        this.Height = height;
        this.Data = data;
    }
   
    public static ITexture GetTexture(String path) {
        if (TextureMap.containsKey(path)) {
            System.out.println("Texture: " + path + " ... OK");
            return TextureMap.get(path);
        } else {
            System.out.println("Adding texture to list...");
            ITexture img = LoadTexture(path);
            TextureMap.put(path, img);
            return img;
        }
    }

    private static ITexture LoadTexture(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            int width = image.getWidth();
            int height = image.getHeight();
            Colour[] data = new Colour[width * height];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    data[y * width + x] = new Colour(r / 255.0, g / 255.0, b / 255.0);
                }
            }

            return (ITexture) new ColorTexture(width, height, data);
        } catch (IOException e) {
            e.printStackTrace();
            return (ITexture) new ColorTexture();
        }
    }

    @Override
    public Colour Sample(double u, double v) {
        u = Util.Fract((Util.Fract(u)) + 1);
        v = Util.Fract((Util.Fract(v)) + 1);
        return this.bilinearSample(u, 1 - v);
    }

    Colour bilinearSample(double u, double v) {
        if (u == 1) {
            u -= EPS;
        }

        if (v == 1) {
            v -= EPS;
        }

        double w = this.Width - 1;
        double h = this.Height - 1;
        int X, Y, x0, y0, x1, y1;
        double x, y;
        X = (int) (u * w);
        Y = (int) (v * h);
        x = Util.Fract(u * w);
        y = Util.Fract(v * h);
        x0 = (int) (X);
        y0 = (int) (Y);
        x1 = x0 + 1;
        y1 = y0 + 1;
        Colour c00 = this.Data[y0 * this.Width + x0];
        Colour c01 = this.Data[y1 * this.Width + x0];
        Colour c10 = this.Data[y0 * this.Width + x1];
        Colour c11 = this.Data[y1 * this.Width + x1];
        Colour c = new Colour(0, 0, 0);
        c = c.Add(c00.MulScalar((1 - x) * (1 - y)));
        c = c.Add(c10.MulScalar(x * (1 - y)));
        c = c.Add(c01.MulScalar((1 - x) * y));
        c = c.Add(c11.MulScalar(x * y));
        return c;
    }

    @Override
    public Vector BumpSample(double u, double v) {
        u = Util.Fract(Util.Fract(u) + 1);
        v = Util.Fract(Util.Fract(v) + 1);
        v = 1 - v;
        int x = (int) (u * this.Width);
        int y = (int) (v * this.Height);
        int x1 = Util.ClampInt(x - 1, 0, this.Width - 1);
        int x2 = Util.ClampInt(x + 1, 0, this.Height - 1);
        int y1 = Util.ClampInt(y - 1, 0, this.Height - 1);
        int y2 = Util.ClampInt(y + 1, 0, this.Height - 1);
        Colour cx = this.Data[y * this.Width + x].Sub(this.Data[y * this.Width + 2]);
        Colour cy = this.Data[y1 * this.Width + x].Sub(this.Data[y2 * this.Width + x]);
        return new Vector(cx.r, cy.r, 0);
    }

    @Override
    public Texture Pow(double a) {
        for (int i = 0; i < this.Data.length; i++) {
            this.Data[i] = this.Data[i].Pow(a);
        }
        return this;
    }

    @Override
    public Texture MulScalar(double a) {
        for (int i = 0; i < this.Data.length; i++) {
            this.Data[i] = this.Data[i].MulScalar(a);
        }
        return this;
    }

    @Override
    public Vector NormalSample(double u, double v) {
        Colour c = this.Sample(u, v);
        return new Vector(c.r * 2 - 1, c.g * 2 - 1, c.b * 2 - 1).Normalize();
    }

   
}

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

public class Colour {

    double r;
    double g;
    double b;

    public Colour(Colour c) {
        r = c.r;
        g = c.g;
        b = c.b;
    }

    public Colour(double R, double G, double B) {
        r = R;
        g = G;
        b = B;
    }

    public static Colour Black = new Colour(0, 0, 0);
    public static Colour White = new Colour(1, 1, 1);

    public Colour() {
    }

    public static Colour NewColor(int r, int g, int b) {
        return new Colour((double) r / 65535, (double) g / 65535, (double) b / 65535);
    }

    public static Colour HexColor(int x) {
        double r = ((x >> 16) & 0xff) / 255.0;
        double g = ((x >> 8) & 0xff) / 255.0;
        double b = ((x >> 0) & 0xff) / 255.0;
        return new Colour(r, g, b).Pow(2.2);
    }

    public Colour Pow(double b) {
        return new Colour(Math.pow(r, b), Math.pow(g, b), Math.pow(this.b, b));
    }

    public static <T extends Comparable<T>> T clamp(T val, T min, T max) {
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }   

    public static int getIntFromColor(double red, double green, double blue) {
        
        if (Double.isNaN(red))
            red = 0.0;
        if (Double.isNaN(green))
            green = 0.0;
        if (Double.isNaN(blue))
            blue = 0.0;
        
        var r = (int)(256 * clamp(red, 0.0, 0.999)); 
        var g = (int)(256 * clamp(green, 0.0, 0.999));
        var b = (int)(256 * clamp(blue, 0.0, 0.999));
        
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }

    public static Colour Kelvin(double K) {
        double red, green, blue;
        double a, b, c, x;
        // red
        if (K >= 6600) {
            a = 351.97690566805693;
            b = 0.114206453784165;
            c = -40.25366309332127;
            x = K / 100 - 55;

            red = a + b * x + c * Math.log(x);
        } else {
            red = 255;
        }
        if (K >= 6600) {
            a = 325.4494125711974;
            b = 0.07943456536662342;
            c = -28.0852963507957;
            x = K / 100 - 50;
            green = a + b * x + c * Math.log(x);
        } else if (K >= 1000) {
            a = -155.25485562709179;
            b = -0.44596950469579133;
            c = 104.49216199393888;
            x = K / 100 - 2;
            green = a + b * x + c * Math.log(x);
        } else {
            green = 0;
        }
        if (K >= 6600) {
            blue = 255;
        } else if (K >= 2000) {
            a = -254.76935184120902;
            b = 0.8274096064007395;
            c = 115.67994401066147;
            x = K / 100 - 10;
            blue = a + b * x + c * Math.log(x);
        } else {
            blue = 0;
        }
        red = Math.min(1, red / 255);
        green = Math.min(1, green / 255);
        blue = Math.min(1, blue / 255);
        return new Colour(red, green, blue);
    }

    public Colour Mix(Colour b, double pct) {
        Colour a = MulScalar(1 - pct);
        b = b.MulScalar(pct);
        return a.Add(b);
    }

    public Colour MulScalar(double b) {
        return new Colour(r * b, g * b, this.b * b);
    }

    public Colour Add(Colour b) {
        return new Colour(r + b.r, g + b.g, this.b + b.b);
    }

    public Colour Sub(Colour b) {
        return new Colour(r - b.r, g - b.g, this.b - b.b);
    }

    public Colour Mul(Colour b) {
        return new Colour(r * b.r, g * b.g, this.b * b.b);
    }

    public Colour Div(Colour b) {
        return new Colour(r / b.r, g / b.g, this.b / b.b);
    }

    public Colour DivScalar(double b) {
        return new Colour(r / b, g / b, this.b / b);
    }

    public Colour Min(Colour b) {
        return new Colour(Math.min(r, b.r), Math.min(g, b.g), Math.min(this.b, b.b));
    }

    public Colour Max(Colour b) {
        return new Colour(Math.max(r, b.r), Math.max(g, b.g), Math.max(this.b, b.b));
    }

    public double MinComponent() {
        return Math.min(Math.min(r, g), b);
    }

    public double MaxComponent() {
        return Math.max(Math.max(r, g), b);
    }
}

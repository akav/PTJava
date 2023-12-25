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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class Buffer {

    
    public int W, H;
    public Pixel[] Pixels;
    public List<Pixel> PixelList = new ArrayList<>();
    byte[] imageBuffer;

    public enum Channel {
        ColorChannel, 
        VarianceChannel, 
        StandardDeviationChannel, 
        SamplesChannel
    }

    Buffer() {
    }

    Buffer(int width, int height) {
        this.W = width;
        this.H = height;
        imageBuffer = new byte[256 * 4 * height];
        this.Pixels = new Pixel[width * height];
        PixelList = new ArrayList<>(width * height);

        for (int i = 0; i < Pixels.length; i++) {
            Pixels[i] = new Pixel(0, new Colour(0, 0, 0), new Colour(0, 0, 0));
        }
    }

    Buffer(int width, int height, Pixel[] pbuffer) {
        this.W = width;
        this.H = height;
        this.Pixels = pbuffer;
    }

    Buffer NewBuffer(int w, int h) {
        Pixel[] pixbuffer = new Pixel[w * h];

        for (int i = 0; i < pixbuffer.length; i++) {
            pixbuffer[i] = new Pixel(0, new Colour(0, 0, 0), new Colour(0, 0, 0));
        }
        return new Buffer(w, h, pixbuffer);
    }

    Buffer Copy() {
        Pixel[] pixcopy = new Pixel[this.W * this.H];
        System.arraycopy(this.Pixels, 0, pixcopy, 0, this.Pixels.length);
        return new Buffer(this.W, this.H, pixcopy);
    }

    synchronized void AddSample(int x, int y, Colour sample) {
        Pixels[y * W + x].AddSample(sample);
    }

    int Samples(int x, int y) {
        return Pixels[y * W + x].Samples.get();
    }

    Colour Color(int x, int y) {
        return Pixels[y * W + x].Color();
    }

    Colour Variance(int x, int y) {
        return Pixels[y * W + x].Variance();
    }

    Colour StandardDeviation(int x, int y) {
        return Pixels[y * W + x].StandardDeviation();
    }

    BufferedImage Image(Buffer buf, Channel channel) {

        BufferedImage renderedImage = new BufferedImage(this.W, this.H, BufferedImage.TYPE_INT_RGB);
        
        double maxSamples = 0;
        if (channel == Channel.SamplesChannel) {
            for (Pixel pix : Pixels) {
                maxSamples = Math.max(maxSamples, (double) pix.Samples.get());
            }
        }

        Colour pixelColor = new Colour();

        for (int y = 0; y < this.H; y++) {
            for (int x = 0; x < this.W; x++) {
                switch (channel) {
                    case ColorChannel:
                        pixelColor = Pixels[y * W + x].Color().Pow(1 / 2.2);
                        break;
                    case VarianceChannel:
                        pixelColor = Pixels[y * W + x].Variance();
                        break;
                    case StandardDeviationChannel:
                        pixelColor = Pixels[y * W + x].StandardDeviation();
                        break;
                    case SamplesChannel:
                        double p = Pixels[y * W + x].Samples.get() / maxSamples;
                        pixelColor = new Colour(p, p, p);
                        break;
                }
                renderedImage.setRGB(x, y, Colour.getIntFromColor(pixelColor.r, pixelColor.g, pixelColor.b));
            }
        }
        return renderedImage;
    }

    class Pixel {

        public AtomicInteger Samples = new AtomicInteger();
      
        public Colour M;
        public Colour V;

        public Pixel() { }

        public Pixel(int Samples, Colour M, Colour V) {
            this();
            this.M = M;
            this.V = V;
        }

        public void AddSample(Colour sample) {
            int sampleCount = Samples.incrementAndGet();

            if (sampleCount == 1) {
                M = sample;
                return;
            }

            Colour m = M;
            M = M.Add(sample.Sub(M).DivScalar(Samples.get()));
            V = V.Add(sample.Sub(m).Mul(sample.Sub(M)));
        }

        public Colour Color() {
            return M;
        }

        public Colour Variance() {
            if (Samples.get() < 2) {
                return new Colour(0, 0, 0);
            }
            return V.DivScalar((double)(Samples.get() - 1));
        }

        public Colour StandardDeviation() {
            return Variance().Pow(0.5);
        }
    }
}
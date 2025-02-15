package ptjava;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import java.awt.Point;

public class Buffer {
    
    public final int W, H;
    public final Pixel[] Pixels;
    private final byte[] imageBuffer;

    public enum Channel {
        ColorChannel, 
        VarianceChannel, 
        StandardDeviationChannel, 
        SamplesChannel
    }

    public Buffer(int width, int height, Pixel[] pixels) {
        this.W = width;
        this.H = height;
        this.imageBuffer = new byte[256 * 4 * height];
        this.Pixels = pixels;
    }

    public Buffer(int width, int height) {
        this.W = width;
        this.H = height;
        this.imageBuffer = new byte[256 * 4 * height];
        this.Pixels = new Pixel[width * height];

        for (int i = 0; i < Pixels.length; i++) {
            Pixels[i] = new Pixel();
        }
    }

    public Buffer Copy() {
        Pixel[] pixCopy = java.util.Arrays.stream(Pixels)
            .parallel() // Parallel processing for large buffers
            .map(Pixel::new) // Calls Pixel(Pixel other) constructor
            .toArray(Pixel[]::new);
    
        return new Buffer(this.W, this.H, pixCopy);
    }
    

    public synchronized void AddSample(int x, int y, Colour sample) {
        Pixels[y * W + x].AddSample(sample);
    }

    public void AddSample(Point p, Colour sample) {
        AddSample(p.x, p.y, sample);
    }

    public int Samples(int x, int y) {
        return Pixels[y * W + x].Samples.get();
    }

    public Colour Color(int x, int y) {
        return Pixels[y * W + x].Color();
    }

    public Colour Variance(int x, int y) {
        return Pixels[y * W + x].Variance();
    }

    public Colour StandardDeviation(int x, int y) {
        return Pixels[y * W + x].StandardDeviation();
    }

    public BufferedImage Image(Channel channel) {
        BufferedImage renderedImage = new BufferedImage(this.W, this.H, BufferedImage.TYPE_INT_RGB);
        double maxSamples = (channel == Channel.SamplesChannel) ? findMaxSamples() : 0;

        for (int i = 0; i < Pixels.length; i++) {
            int x = i % W;
            int y = i / W;
            Colour pixelColor = switch (channel) {
                case ColorChannel -> Pixels[i].Color().Pow(1 / 2.2);
                case VarianceChannel -> Pixels[i].Variance();
                case StandardDeviationChannel -> Pixels[i].StandardDeviation();
                case SamplesChannel -> new Colour(Pixels[i].Samples.get() / maxSamples, Pixels[i].Samples.get() / maxSamples, Pixels[i].Samples.get() / maxSamples);
            };
            renderedImage.setRGB(x, y, Colour.getIntFromColor(pixelColor.r, pixelColor.g, pixelColor.b));
        }

        return renderedImage;
    }

    private double findMaxSamples() {
        return java.util.Arrays.stream(Pixels)
            .mapToDouble(p -> p.Samples.get())
            .max()
            .orElse(1);
    }

    public static class Pixel {
        public final AtomicInteger Samples = new AtomicInteger();
        private Colour M = new Colour(0, 0, 0);
        private Colour V = new Colour(0, 0, 0);

        public Pixel() {}

        public Pixel(Pixel other) {
            this.Samples.set(other.Samples.get());
            this.M = other.M;
            this.V = other.V;
        }

        public void AddSample(Colour sample) {
            int sampleCount = Samples.incrementAndGet();
            if (sampleCount == 1) {
                M = sample;
                return;
            }

            Colour oldM = M;
            M = M.Add(sample.Sub(M).DivScalar(sampleCount));
            V = V.Add(sample.Sub(oldM).Mul(sample.Sub(M)));
        }

        public Colour Color() {
            return M;
        }

        public Colour Variance() {
            return (Samples.get() < 2) ? new Colour(0, 0, 0) : V.DivScalar(Samples.get() - 1);
        }

        public Colour StandardDeviation() {
            return Variance().Pow(0.5);
        }
    }
}

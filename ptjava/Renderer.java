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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import ptjava.Buffer.Channel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.util.concurrent.CountDownLatch;

final class Renderer {

    Scene Scene;
    Camera Camera;
    Sampler Sampler;
    Buffer PBuffer;
    int SamplesPerPixel;
    public boolean StratifiedSampling;
    public int AdaptiveSamples;
    double AdaptiveThreshold;
    double AdaptiveExponent;
    public int FireflySamples;
    double FireflyThreshold;
    int NumCPU;
    int iterations;
    String pathTemplate;

    Renderer() {
    }

    static Renderer NewRenderer(Scene scene, Camera camera, Sampler sampler, int w, int h) {
        Renderer r = new Renderer();
        r.Scene = scene;
        r.Camera = camera;
        r.Sampler = sampler;
        r.PBuffer = new Buffer(w, h);
        r.SamplesPerPixel = 1;
        r.StratifiedSampling = false;
        r.AdaptiveSamples = 0;
        r.AdaptiveExponent = 0;
        r.AdaptiveThreshold = 1;
        r.FireflySamples = 0;
        r.FireflyThreshold = 1;
        r.NumCPU = Runtime.getRuntime().availableProcessors();

        return r;
    }

    public void RenderParallel(BufferedImage renderedImage, JPanel renderPanel) {
        Scene scene = Scene;
        Camera camera = Camera;
        Sampler sampler = Sampler;
        Buffer buf = PBuffer;
        int w = buf.W;
        int h = buf.H;
        int spp = SamplesPerPixel;
        int sppRoot = (int) (Math.sqrt((double)(SamplesPerPixel)));
        scene.Compile();
        scene.rays = new AtomicInteger(0);
        ThreadLocalRandom rand = ThreadLocalRandom.current();
         
        int numberOfThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        int blockSize = Math.max(1, h / numberOfThreads);
        int numBlocks = (h + blockSize - 1) / blockSize;
        CountDownLatch renderingLatch = new CountDownLatch(numBlocks);

        for (int blockStartY = 0; blockStartY < h; blockStartY += blockSize) {
            final int finalBlockStartY = blockStartY;
            int blockEndY = Math.min(finalBlockStartY + blockSize, h);
            executor.submit(() -> {
                try {
                    for (int y = finalBlockStartY; y < blockEndY; y++) {
                        int finalY = y;
                        for (int x = 0; x < w; x++) {
                            if (StratifiedSampling) {
                                for (int u = 0; u < sppRoot; u++) {
                                    for (int v = 0; v < sppRoot; v++) {
                                        var fu = ((double) u + 0.5) / (double) sppRoot;
                                        var fv = ((double) v + 0.5) / (double) sppRoot;
                                        Ray ray = camera.CastRay(x, finalY, w, h, fu, fv, rand);
                                        Colour sample = sampler.Sample(scene, ray, rand);
                                        buf.AddSample(x, finalY, sample);
                                    }
                                }
                            } else {
                                Colour sample = Colour.Black;
                                // Random subsampling
                                for (int r = 0; r < spp; r++) {
                                    var fu = (x + rand.nextDouble() * 0.5) / w;
                                    var fv = (y + rand.nextDouble() * 0.5) / h;
                                    Ray ray = camera.CastRay(x, y, w, h, fu, fv, rand);
                                    sample = sample.Add(sampler.Sample(scene, ray, rand));
                                }
                                sample.DivScalar(spp);
                                buf.AddSample(x, y, sample);                      
                            }                         
                        
                            // Adaptive Sampling
                            if (AdaptiveSamples > 0) {
                                double v = buf.StandardDeviation(x, finalY).MaxComponent();
                                v = Util.Clamp(v / AdaptiveThreshold, 0, 1);
                                v = Math.pow(v, AdaptiveExponent);
                                int samples = (int) (v * AdaptiveSamples);
                                for (int d = 0; d < samples; d++) {
                                    var fu_ = rand.nextDouble();
                                    var fv_ = rand.nextDouble();
                                    Ray ray = camera.CastRay(x, finalY, w, h, fu_, fv_, rand);
                                    Colour sample = sampler.Sample(scene, ray, rand);
                                    buf.AddSample(x, finalY, sample);
                                }
                            }

                            if (FireflySamples > 0) {
                                if (PBuffer.StandardDeviation(x, finalY).MaxComponent() > FireflyThreshold) {
                                    for (int e = 0; e < FireflySamples; e++) {
                                        var fu_ = rand.nextDouble();
                                        var fv_ = rand.nextDouble();
                                        Ray ray = camera.CastRay(x, finalY, w, h, fu_, fv_, rand);
                                        Colour sample = sampler.Sample(scene, ray, rand);
                                        PBuffer.AddSample(x, finalY, sample);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Handle the exception
                    System.out.println(e);
                    } finally {
                    renderingLatch.countDown();
                }
            });
        }
        executor.shutdown();

        try {
            if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt(); // Preserve interrupt status
        }        
    }

    public void Render(BufferedImage renderedImage, JPanel renderPanel) {

        Scene scene = Scene;
        Camera camera = Camera;
        Sampler sampler = Sampler;
        Buffer buf = PBuffer;
        int w = buf.W;
        int h = buf.H;
        int spp = SamplesPerPixel;
        int sppRoot = (int) (Math.sqrt((double) (SamplesPerPixel)));
        scene.Compile();
        scene.rays = new AtomicInteger(0);
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        double fu, fv;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (StratifiedSampling) {
                    for (int u = 0; u < sppRoot; u++) {
                        for (int v = 0; v < sppRoot; v++) {
                            fu = ((double) u + 0.5) / (double) sppRoot;
                            fv = ((double) v + 0.5) / (double) sppRoot;
                            Ray ray = camera.CastRay(x, y, w, h, fu, fv, rand);
                            Colour sample = sampler.Sample(scene, ray, rand);
                            buf.AddSample(x, y, sample);
                        }
                    }
                } else {
                    // Random subsampling
                    for (int ii = 0; ii < spp; ii++) {
                        var fu_ = rand.nextDouble();
                        var fv_ = rand.nextDouble();
                        Ray ray = camera.CastRay(x, y, w, h, fu_, fv_, rand);
                        Colour sample = sampler.Sample(scene, ray, rand);
                        buf.AddSample(x, y, sample);
                    }
                }
                // Adaptive Sampling
                if (AdaptiveSamples > 0) {
                    double v = buf.StandardDeviation(x, y).MaxComponent();
                    v = Util.Clamp(v / AdaptiveThreshold, 0, 1);
                    v = Math.pow(v, AdaptiveExponent);
                    int samples = (int) (v * AdaptiveSamples);
                    for (int d = 0; d < samples; d++) {

                        fu = rand.nextDouble();
                        fv = rand.nextDouble();
                        Ray ray = camera.CastRay(x, y, w, h, fu, fv, rand);
                        Colour sample = sampler.Sample(scene, ray, rand);
                        buf.AddSample(x, y, sample);
                    }
                }

                if (FireflySamples > 0) {
                    if (PBuffer.StandardDeviation(x, y).MaxComponent() > FireflyThreshold) {
                        for (int e = 0; e < FireflySamples; e++) {
                            fu = rand.nextDouble();
                            fv = rand.nextDouble();
                            Ray ray = camera.CastRay(x, y, w, h, fu, fv, rand);
                            Colour sample = sampler.Sample(scene, ray, rand);
                            PBuffer.AddSample(x, y, sample);
                        }
                    }
                }
            }
        }
    }


    public void IterativeRender(String pathTemplate, int iterations, BufferedImage renderedImage, JPanel renderPanel) throws InterruptedException, IOException {
        this.iterations = iterations;
        this.pathTemplate = pathTemplate;

        for (int iter = 1; iter <= this.iterations; iter++) {
            //Render(renderedImage, renderPanel);            
            RenderParallel(renderedImage, renderPanel);
            
            for(int y = 0; y < PBuffer.H; y++)
            {
                for(int x = 0; x < PBuffer.W; x++)
                {
                    var r = PBuffer.Pixels[y * PBuffer.W + x].Color().Pow(1.0/2.2).r;
                    var g = PBuffer.Pixels[y * PBuffer.W + x].Color().Pow(1.0/2.2).g;
                    var b = PBuffer.Pixels[y * PBuffer.W + x].Color().Pow(1.0/2.2).b;
                    int pixelColor = Colour.getIntFromColor(r,g,b);
                    renderedImage.setRGB(x, y, pixelColor);
                }
            }
            renderPanel.repaint();
            System.out.print("\r[Iteration: " + iter + " of " + iterations + "]");
        }
       
        System.out.println("\n Iteration Completed. Writing image...");
        File outputfile = new File(pathTemplate);
        boolean write = ImageIO.write(PBuffer.Image(PBuffer, Channel.ColorChannel), "png", outputfile);
    }
}

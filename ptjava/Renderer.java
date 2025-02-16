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
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;
import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.io.File;

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
        scene.Compile();
        scene.rays = new AtomicInteger(0);
        
        long seed = System.nanoTime();
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        /*
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, h).parallel().forEach(y -> {

                Map<Point, Colour> localBuffer = new HashMap<>();

                for (int x = 0; x < w; x++) {
                    Colour sample = Colour.Black;

                    for (int r = 0; r < spp; r++) {
                        double fu = rand.nextDouble();
                        double fv = rand.nextDouble();
                        Ray ray = camera.CastRay(x, y, w, h, fu, fv, rand);
                        sample = sample.Add(sampler.Sample(scene, ray, rand));
                    }

                    sample.DivScalar(spp);
                    localBuffer.put(new Point(x, y), sample);
                }

                synchronized (buf) {
                    localBuffer.forEach(buf::AddSample);
                }
            });
        }*/

        int tileSize = 128;
        int numTilesX = (w + tileSize - 1) / tileSize;
        int numTilesY = (h + tileSize - 1) / tileSize;
        int subTileSize = 16; // Increased sub-tile size for better granularity

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int tileIndex = 0; tileIndex < numTilesX * numTilesY; tileIndex++) {
            int tileX = tileIndex % numTilesX;
            int tileY = tileIndex / numTilesX;
            int xStart = tileX * tileSize;
            int yStart = tileY * tileSize;
            int xEnd = Math.min(xStart + tileSize, w);
            int yEnd = Math.min(yStart + tileSize, h);

            for (int subTileY = 0; subTileY < tileSize; subTileY += subTileSize) {
                for (int subTileX = 0; subTileX < tileSize; subTileX += subTileSize) {
                    int subXStart = xStart + subTileX;
                    int subYStart = yStart + subTileY;
                    int subXEnd = Math.min(subXStart + subTileSize, xEnd);
                    int subYEnd = Math.min(subYStart + subTileSize, yEnd);

                    executor.submit(() -> {
                        
                        Map<Point, Colour> localBuffer = new ConcurrentHashMap<>();

                        for (int y = subYStart; y < subYEnd; y++) {
                            for (int x = subXStart; x < subXEnd; x++) {
                                Colour sample = Colour.Black;

                                for (int r = 0; r < spp; r++) {
                                    double xOffset = rand.nextDouble();
                                    double yOffset = rand.nextDouble();

                                    double fu = (x + xOffset) / w;
                                    double fv = (y + yOffset) / h;
                                    Ray ray = camera.CastRay(x, y, w, h, fu, fv, rand);
                                    sample = sample.Add(sampler.Sample(scene, ray, rand));
                                }

                                sample.DivScalar(spp);
                                localBuffer.put(new Point(x, y), sample);
                            }
                        }

                        synchronized (buf) {
                            localBuffer.forEach((point, color) -> buf.AddSample(point, color));
                        }

                        synchronized (renderedImage) {
                            for (int y = subYStart; y < subYEnd; y++) {
                                for (int x = subXStart; x < subXEnd; x++) {
                                    Colour pixelColor = buf.Pixels[y * w + x].Color().Pow(1.0 / 2.2);
                                    int colorInt = Colour.getIntFromColor(pixelColor.r, pixelColor.g, pixelColor.b);
                                    renderedImage.setRGB(x, y, colorInt);
                                }
                            }
                            renderPanel.repaint();
                        }
                    });
                }
            }
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void IterativeRender(String pathTemplate, int iterations, BufferedImage renderedImage, JPanel renderPanel)
            throws InterruptedException, IOException {
        this.iterations = iterations;
        this.pathTemplate = pathTemplate;

        for (int iter = 1; iter <= this.iterations; iter++) {
            RenderParallel(renderedImage, renderPanel);

            for (int y = 0; y < PBuffer.H; y++) {
                for (int x = 0; x < PBuffer.W; x++) {
                    var pixelColor = PBuffer.Pixels[y * PBuffer.W + x].Color().Pow(1.0 / 2.2);
                    int colorInt = Colour.getIntFromColor(pixelColor.r, pixelColor.g, pixelColor.b);
                    renderedImage.setRGB(x, y, colorInt);
                }
            }

            renderPanel.repaint();
            System.out.print("\r[Iteration: " + iter + " of " + iterations + "]");

        }

        System.out.println("\nIteration Completed. Writing image...");
        ImageIO.write(PBuffer.Image(Buffer.Channel.ColorChannel), "png", new File(pathTemplate));
    }
}

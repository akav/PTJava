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
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JPanel;

public class Example {

    Example() {
    }

    static void MaterialSpheres(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws IOException, InterruptedException {
        Scene scene = new Scene();
        double r = 0.4;
        Material mat1 = Material.DiffuseMaterial(Colour.HexColor(0x334D5C));
        scene.Add(Sphere.NewSphere(new Vector(-2, r, 0), r, mat1));
        Material mat2 = Material.SpecularMaterial(Colour.HexColor(0x334D5C), 2);
        scene.Add(Sphere.NewSphere(new Vector(-1, r, 0), r, mat2));
        Material mat3 = Material.GlossyMaterial(Colour.HexColor(0x334D5C), 2, Util.Radians(50));
        scene.Add(Sphere.NewSphere(new Vector(0, r, 0), r, mat3));
        Material mat4 = Material.TransparentMaterial(Colour.HexColor(0x334D5C), 2, Util.Radians(20), 1);
        scene.Add(Sphere.NewSphere(new Vector(1, r, 0), r, mat4));
        Material mat5 = Material.ClearMaterial(2, 0);
        scene.Add(Sphere.NewSphere(new Vector(2, r, 0), r, mat5));
        Material mat6 = Material.MetallicMaterial(Colour.HexColor(0xFFFFFF), 0, 1);
        scene.Add(Sphere.NewSphere(new Vector(0, 1.5, -4), 1.5, mat6));
        scene.Add(Cube.NewCube(new Vector(-1000, -1, -1000), new Vector(1000, 0, 1000),
                Material.GlossyMaterial(Colour.HexColor(0xFFFFFF), 1.4, Util.Radians(20))));
        scene.Add(Sphere.NewSphere(new Vector(0, 5, 0), 1, Material.LightMaterial(Colour.White, 25)));
        Camera camera = Camera.LookAt(new Vector(0, 3, 6), new Vector(0, 1, 0), new Vector(0, 1, 0), 30);
        DefaultSampler sampler = DefaultSampler.NewSampler(64, 6);
        Renderer renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.FireflySamples = 128;
        renderer.IterativeRender("materialspheres.png", 500, renderedImage, renderPanel);
    }

    static void ellipsoid(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws IOException, InterruptedException {
        Scene scene = new Scene();
        var wall = Material.GlossyMaterial(Colour.HexColor(0xFCFAE1), 1.333, Util.Radians(30));
        scene.Add(Sphere.NewSphere(new Vector(10, 10, 10), 2, Material.LightMaterial(Colour.White, 50)));
        scene.Add(Cube.NewCube(new Vector(-100, -100, -100), new Vector(-12, 100, 100), wall));
        scene.Add(Cube.NewCube(new Vector(-100, -100, -100), new Vector(100, -1, 100), wall));
        var material = Material.GlossyMaterial(Colour.HexColor(0x167F39), 1.333, Util.Radians(30));
        var sphere = Sphere.NewSphere(new Vector(), 1, material);

        for (int i = 0; i < 180; i += 30) {
            var m = Matrix.Identity;
            m = m.Scale(new Vector(0.3, 1, 5));
            m = m.Rotate(new Vector(0, 1, 0), Util.Radians((double) i));
            var shape = TransformedShape.NewTransformedShape(sphere, m);
            scene.Add(shape);
        }

        var camera = Camera.LookAt(new Vector(8, 8, 0), new Vector(1, 0, 0), new Vector(0, 1, 0), 45);
        var sampler = DefaultSampler.NewSampler(4, 4);
        Renderer renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.FireflySamples = 128;
        renderer.IterativeRender("ellipsoid.png", 50, renderedImage, renderPanel);
    }

    static void example1(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws IOException, InterruptedException {
        Scene scene = new Scene();
        scene.Add(Sphere.NewSphere(new Vector(1.5, 1.25, 0), 1.25,
                Material.SpecularMaterial(Colour.HexColor(0x004358), 1.3)));
        scene.Add(Sphere.NewSphere(new Vector(-1, 1, 2), 1, Material.SpecularMaterial(Colour.HexColor(0xFFE11A), 1.3)));
        scene.Add(Sphere.NewSphere(new Vector(-2.5, 0.75, 0), 0.75,
                Material.SpecularMaterial(Colour.HexColor(0xFD7400), 1.3)));
        scene.Add(Sphere.NewSphere(new Vector(-0.75, 0.5, -1), 0.5, Material.ClearMaterial(1.5, 0)));
        scene.Add(Cube.NewCube(new Vector(-10, -1, -10), new Vector(10, 0, 10),
                Material.GlossyMaterial(Colour.White, 1.1, Util.Radians(10))));
        scene.Add(Sphere.NewSphere(new Vector(-1.5, 4, 0), 0.5, Material.LightMaterial(Colour.White, 30)));
        var camera = Camera.LookAt(new Vector(0, 2, -5), new Vector(0, 0.25, 3), new Vector(0, 1, 0), 45);
        camera.SetFocus(new Vector(-0.75, 1, -1), 0.1);
        var sampler = DefaultSampler.NewSampler(4, 8);
        sampler.specularMode = SpecularMode.SpecularModeFirst;
        Renderer renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.AdaptiveSamples = 32;
        renderer.FireflySamples = 256;
        renderer.IterativeRender("example1.png", 1000, renderedImage, renderPanel);
    }

    public static void example2(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws IOException, InterruptedException {

        var scene = new Scene();
        var material = Material.GlossyMaterial(Colour.HexColor(0xEFC94C), 3, Util.Radians(30));
        var whiteMat = Material.GlossyMaterial(Colour.White, 3, Util.Radians(30));

        for (int x = 0; x < 40; x++) {
            for (int z = 0; z < 40; z++) {
                var center = new Vector((double) x - 19.5, 0, (double) z - 19.5);
                scene.Add(Sphere.NewSphere(center, 0.4, material));
            }
        }
        scene.Add(Cube.NewCube(new Vector(-100, -1, -100), new Vector(100, 0, 100), whiteMat));
        scene.Add(Sphere.NewSphere(new Vector(-1, 4, -1), 1, Material.LightMaterial(Colour.White, 20)));
        var camera = Camera.LookAt(new Vector(0, 4, -8), new Vector(0, 0, -2), new Vector(0, 1, 0), 45);
        var sampler = DefaultSampler.NewSampler(32, 4);
        Renderer renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.IterativeRender("example2.png", 100, renderedImage, renderPanel);
    }

    public static void example3(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws IOException, InterruptedException {
        var scene = new Scene();
        var material = Material.DiffuseMaterial(Colour.HexColor(0xFCFAE1));
        scene.Add(Cube.NewCube(new Vector(-1000, -1, -1000), new Vector(1000, 0, 1000), material));

        for (int x = -20; x <= 20; x++) {
            for (int z = -20; z <= 20; z++) {
                if ((x + z) % 2 == 0) {
                    continue;
                }
                var s = 0.1;
                var min = new Vector((double) x - s, 0, (double) z - s);
                var max = new Vector((double) x + s, 2, (double) z + s);
                scene.Add(Cube.NewCube(min, max, material));
            }
        }
        scene.Add(Cube.NewCube(new Vector(-5, 10, -5), new Vector(5, 11, 5), Material.LightMaterial(Colour.White, 5)));
        var camera = Camera.LookAt(new Vector(20, 10, 0), new Vector(8, 0, 0), new Vector(0, 1, 0), 45);
        var sampler = DefaultSampler.NewSampler(8, 8);
        var renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.IterativeRender("example3.png", 1000, renderedImage, renderPanel);
    }

    static void SimpleSphere(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws IOException, InterruptedException {
        Scene scene = new Scene();
        Material material = Material.DiffuseMaterial(Colour.White);
        Plane plane = Plane.NewPlane(new Vector(0, 0, 0), new Vector(0, 0, 1), material);
        scene.Add(plane);
        Material material2 = Material.GlossyMaterial(new Colour(0.4, 0.4, 1.0), 1.1, Util.Radians(10));
        Sphere sphere = Sphere.NewSphere(new Vector(0, 0, 1), 1, material2);
        scene.Add(sphere);
        Sphere light = Sphere.NewSphere(new Vector(0, 0, 5), 1, Material.LightMaterial(Colour.White, 8));
        scene.Add(light);
        Camera camera = Camera.LookAt(new Vector(3, 3, 3), new Vector(0, 0, 0.5), new Vector(0, 0, 1), 50);
        DefaultSampler sampler = DefaultSampler.NewSampler(4, 4);
        Renderer renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.AdaptiveSamples = 32;
        renderer.FireflySamples = 16;
        renderer.IterativeRender("simplesphere.png", 1000, renderedImage, renderPanel);
    }

    public static void simplecylinder(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws InterruptedException, IOException {
        var scene = new Scene();
        // create a material
        var material = Material.DiffuseMaterial(Colour.White);

        // add the floor (a plane)
        var plane = Plane.NewPlane(new Vector(0, 0, 0), new Vector(0, 0, 1), material);
        scene.Add(plane);

        // Calculate the translation matrix to place the bottom of the cylinder at the
        // center
        double cylinderHeight = 2.0; // Height of the cylinder
        var translation = new Matrix().Translate(new Vector(0, 0, -cylinderHeight / 2.0));

        // add the cylinder with the adjusted translation
        var cylinder = TransformedShape.NewTransformedShape(Cylinder.NewCylinder(1, 0, cylinderHeight, material),
                translation);
        scene.Add(cylinder);

        // add a spherical light source
        var light = Sphere.NewSphere(new Vector(0, 0, 5), 1, Material.LightMaterial(Colour.White, 8));
        scene.Add(light);

        // position the camera
        var camera = Camera.LookAt(new Vector(3, 3, 3), new Vector(0, 0, 0.5), new Vector(0, 0, 1), 50);

        // render the scene with progressive refinement
        var sampler = DefaultSampler.NewSampler(16, 4);
        var renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.AdaptiveSamples = 128;
        renderer.FireflySamples = 32;
        renderer.IterativeRender("simplecylinder.png", 100, renderedImage, renderPanel);
    }

    static void qbert(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws IOException, InterruptedException {
        Scene scene = new Scene();
        var floor = Material.GlossyMaterial(Colour.HexColor(0xFCFFF5), 1.2, Util.Radians(30));
        var cube = Material.GlossyMaterial(Colour.HexColor(0xFF8C00), 1.3, Util.Radians(20));
        var ball = Material.GlossyMaterial(Colour.HexColor(0xD90000), 1.4, Util.Radians(10));
        int n = 7;
        double fn = (double) n;
        var rand = new Random(0);

        for (int z = 0; z < n; z++) {
            for (int x = 0; x < n - z; x++) {
                for (int y = 0; y < n - z - x; y++) {
                    var fx = (double) x;
                    var fy = (double) y;
                    var fz = (double) z;
                    scene.Add(Cube.NewCube(new Vector(fx, fy, fz), new Vector(fx + 1, fy + 1, fz + 1), cube));

                    if (x + y == n - z - 1) {
                        if (rand.nextDouble() > 0.75) {
                            scene.Add(Sphere.NewSphere(new Vector(fx + 0.5F, fy + 0.5F, fz + 1.5F), 0.35F, ball));
                        }
                    }
                }
            }
        }
        scene.Add(Cube.NewCube(new Vector(-1000, -1000, -1), new Vector(1000, 1000, 0), floor));
        scene.Add(Sphere.NewSphere(new Vector(fn, fn / 3, fn * 2), 1, Material.LightMaterial(Colour.White, 100)));
        Camera camera = Camera.LookAt(new Vector(fn * 2, fn * 2, fn * 2), new Vector(0, 0, fn / 4), new Vector(0, 0, 1),
                35);
        DefaultSampler sampler = DefaultSampler.NewSampler(4, 4);
        Renderer renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.IterativeRender("qbert.png", 1000, renderedImage, renderPanel);
    }

    public static void runway(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws InterruptedException, IOException {
        int radius = 2;
        int height = 3;
        int emission = 3;
        Scene scene = new Scene();
        var white = Material.DiffuseMaterial(Colour.White);
        var floor = Cube.NewCube(new Vector(-250, -1500, -1), new Vector(250, 6200, 0), white);
        scene.Add(floor);
        var light = Material.LightMaterial(Colour.Kelvin(2700), emission);
        for (int y = 0; y <= 6000; y += 40) {
            scene.Add(Sphere.NewSphere(new Vector(-100, (double) y, height), radius, light));
            scene.Add(Sphere.NewSphere(new Vector(0, (double) y, height), radius, light));
            scene.Add(Sphere.NewSphere(new Vector(100, (double) y, height), radius, light));

        }
        for (int y = -40; y >= -750; y -= 20) {
            scene.Add(Sphere.NewSphere(new Vector(-10, (double) y, height), radius, light));
            scene.Add(Sphere.NewSphere(new Vector(0, (double) y, height), radius, light));
            scene.Add(Sphere.NewSphere(new Vector(10, (double) y, height), radius, light));
        }
        var green = Material.LightMaterial(Colour.HexColor(0x0BDB46), emission);
        var red = Material.LightMaterial(Colour.HexColor(0xDC4522), emission);

        for (int x = -160; x <= 160; x += 10) {
            scene.Add(Sphere.NewSphere(new Vector((double) x, -20, height), radius, green));
            scene.Add(Sphere.NewSphere(new Vector((double) x, 6100, height), radius, red));
        }
        scene.Add(Sphere.NewSphere(new Vector(-160, 250, height), radius, red));
        scene.Add(Sphere.NewSphere(new Vector(-180, 250, height), radius, red));
        scene.Add(Sphere.NewSphere(new Vector(-200, 250, height), radius, light));
        scene.Add(Sphere.NewSphere(new Vector(-220, 250, height), radius, light));
        for (int i = 0; i < 5; i++) {
            var y = (double) ((i + 1) * -120);

            for (int j = 1; j <= 4; j++) {
                var x = (double) (j + 4) * 7.5F;
                scene.Add(Sphere.NewSphere(new Vector(x, y, height), radius, red));
                scene.Add(Sphere.NewSphere(new Vector(-x, y, height), radius, red));
                scene.Add(Sphere.NewSphere(new Vector(x, -y, height), radius, light));
                scene.Add(Sphere.NewSphere(new Vector(-x, -y, height), radius, light));
            }
        }
        var camera = Camera.LookAt(new Vector(0, -1500, 200), new Vector(0, -100, 0), new Vector(0, 0, 1), 20);
        camera.SetFocus(new Vector(0, 20000, 0), 1);
        var sampler = DefaultSampler.NewSampler(4, 4);
        var renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.IterativeRender("runway.png", 1000, renderedImage, renderPanel);
    }

    public static void maze(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws InterruptedException, IOException {

        var rand = new Random(0);
        var scene = new Scene();
        var floor = Material.GlossyMaterial(Colour.HexColor(0x7E827A), 1.1F, Util.Radians(30));
        var material = Material.GlossyMaterial(Colour.HexColor(0xE3CDA4), 1.1F, Util.Radians(30));
        scene.Add(Cube.NewCube(new Vector(-10000, -10000, -10000), new Vector(10000, 10000, 0), floor));
        var n = 24;

        for (int x = -n; x <= n; x++) {
            for (int y = -n; y <= n; y++) {

                if (rand.nextDouble() > 0.8) {
                    var min = new Vector((double) x - 0.5F, (double) y - 0.5F, 0);
                    var max = new Vector((double) x + 0.5F, (double) y + 0.5F, 1);
                    var cube = Cube.NewCube(min, max, material);
                    scene.Add(cube);
                }
            }
        }
        scene.Add(Sphere.NewSphere(new Vector(0, 0, 2.25F), 0.25F, Material.LightMaterial(Colour.White, 500)));
        var camera = Camera.LookAt(new Vector(1, 0, 30), new Vector(0, 0, 0), new Vector(0, 0, 1), 35);
        var sampler = DefaultSampler.NewSampler(4, 4);
        var renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.FireflySamples = 128;
        renderer.IterativeRender("maze.png", 1000, renderedImage, renderPanel);

    }

    public static void gopher(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws InterruptedException, IOException {

        var scene = new Scene();
        var gopher = Material.GlossyMaterial(Colour.Black, 1.2, Util.Radians(30));
        var wall = Material.GlossyMaterial(Colour.HexColor(0xFCFAE1), 1.5, Util.Radians(10));
        var light = Material.LightMaterial(Colour.White, 80);

        scene.Add(Cube.NewCube(new Vector(-10, -1, -10), new Vector(-2, 10, 10), wall));
        scene.Add(Cube.NewCube(new Vector(-10, -1, -10), new Vector(10, 0, 10), wall));
        scene.Add(Sphere.NewSphere(new Vector(4, 10, 1), 1, light));

        var mesh = OBJ.Load("models/gopher.obj", gopher);
        scene.Add(mesh);
        mesh.Transform(new Matrix().Rotate(new Vector(0, 1, 0), Util.Radians(-10)));
        mesh.SmoothNormals();
        mesh.FitInside(new Box(new Vector(-1, 0, -1), new Vector(1, 2, 1)), new Vector(0.5, 0, 0.5));
        scene.Add(mesh);

        var camera = Camera.LookAt(new Vector(4, 1, 0), new Vector(0, 0.9, 0), new Vector(0, 1, 0), 40);
        var sampler = DefaultSampler.NewSampler(16, 16);
        var renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.IterativeRender("gopher.png", 1000, renderedImage, renderPanel);
    }

    public static void refraction(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws InterruptedException, IOException {
        var scene = new Scene();
        var glass = Material.ClearMaterial(1.5, 0);
        // add a sphere primitive
        scene.Add(Sphere.NewSphere(new Vector(-1.5, 0, 0.5), 1, glass));
        // add a mesh sphere
        var mesh = STL.Load("models/sphere.stl", glass);
        mesh.SmoothNormals();
        mesh.Transform(new Matrix().Translate(new Vector(1.5, 0, 0.5)));
        scene.Add(mesh);
        // add the floor
        scene.Add(Plane.NewPlane(new Vector(0, 0, -1), new Vector(0, 0, 1), Material.DiffuseMaterial(Colour.White)));
        // add the light
        scene.Add(Sphere.NewSphere(new Vector(0, 0, 5), 1, Material.LightMaterial(Colour.White, 15)));
        var camera = Camera.LookAt(new Vector(0, -5, 5), new Vector(0, 0, 0), new Vector(0, 0, 1), 50);
        var sampler = DefaultSampler.NewSampler(16, 8);
        sampler.SetSpecularMode(SpecularMode.SpecularModeAll);
        var renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.IterativeRender("refraction.png", 1000, renderedImage, renderPanel);
    }

    public static void love(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws InterruptedException, IOException {
        var scene = new Scene();
        var material = Material.GlossyMaterial(Colour.HexColor(0xF2F2F2), 1.5, Util.Radians(20));
        scene.Add(Cube.NewCube(new Vector(-100, -1, -100), new Vector(100, 0, 100), material));
        var heart = Material.GlossyMaterial(Colour.HexColor(0xF60A20), 1.5, Util.Radians(20));
        var mesh = STL.Load("models/love.stl", heart);
        mesh.FitInside(new Box(new Vector(-0.5, 0, -0.5), new Vector(0.5, 1, 0.5)), new Vector(0, 0, 0));
        scene.Add(mesh);
        scene.Add(Sphere.NewSphere(new Vector(-2, 10, 2), 1, Material.LightMaterial(Colour.White, 30)));
        scene.Add(Sphere.NewSphere(new Vector(0, 10, 2), 1, Material.LightMaterial(Colour.White, 30)));
        scene.Add(Sphere.NewSphere(new Vector(2, 10, 2), 1, Material.LightMaterial(Colour.White, 30)));
        var camera = Camera.LookAt(new Vector(0, 1.5F, 2), new Vector(0, 0.5F, 0), new Vector(0, 1, 0), 35);
        var sampler = DefaultSampler.NewSampler(4, 4);
        var renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.IterativeRender("love.png", 1000, renderedImage, renderPanel);
    }

    public static void hits(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws InterruptedException, IOException {
        var scene = new Scene();
        var material = Material.DiffuseMaterial(new Colour(0.95F, 0.95F, 1));
        var light = Material.LightMaterial(Colour.White, 300);
        scene.Add(Sphere.NewSphere(new Vector(-0.75F, -0.75F, 5), 0.25F, light));
        scene.Add(Cube.NewCube(new Vector(-1000, -1000, -1000), new Vector(1000, 1000, 0), material));
        var mesh = STL.Load("models/hits.stl", material);
        mesh.SmoothNormalsThreshold(Util.Radians(10));
        mesh.FitInside(new Box(new Vector(-1, -1, 0), new Vector(1, 1, 2)), new Vector(0.5F, 0.5F, 0));
        scene.Add(mesh);
        var camera = Camera.LookAt(new Vector(1.6F, -3, 2), new Vector(-0.25F, 0.5F, 0.5F), new Vector(0, 0, 1), 50);
        var sampler = DefaultSampler.NewSampler(4, 4);
        var renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.FireflySamples = 128;
        renderer.IterativeRender("hits.png", 1000, renderedImage, renderPanel);
    }

    public static void toybrick(int WIDTH, int HEIGHT, BufferedImage renderedImage, JPanel renderPanel)
            throws InterruptedException, IOException {
        double H = 1.46875F;
        var scene = new Scene();
        scene.Color = Colour.White;
        var whiteBrick = Util.CreateBrick(0xF2F3F2);
        var brightRedBrick = Util.CreateBrick(0xC4281B);
        var brightBlueBrick = Util.CreateBrick(0x0D69AB);
        var brightYellowBrick = Util.CreateBrick(0xF5CD2F);
        var blackBrick = Util.CreateBrick(0x1B2A34);
        var darkGreenBrick = Util.CreateBrick(0x287F46);

        var meshes = new Mesh[] { whiteBrick, brightRedBrick, brightBlueBrick, brightYellowBrick, blackBrick,
                darkGreenBrick };

        // Generate bricks and add them to the list
        var brickList = new ArrayList<Mesh>();
        for (int i = 0; i < 6; i++) {
            int h = new Random().nextInt(5) + 1;
            for (int j = 0; j < h; j++) {
                brickList.add(meshes[i]);
            }
        }

        var random = new Random(); // Instantiate outside the loop

        for (int x = -30; x <= 50; x += 2) {
            for (int y = -50; y <= 20; y += 4) {
                int h = random.nextInt(5) + 1;
                for (int i = 0; i < h; i++) {
                    var dy = 0;

                    if (((x / 2 + i) % 2) == 0) {
                        dy = 2;
                    }
                    var z = i * H;
                    int mnum = random.nextInt(brickList.size()); // Randomly select from the brick list
                    Mesh mesh = brickList.get(mnum); // Selecting a mesh from the list
                    var m = new Matrix().Translate(new Vector((double) x, (double) (y + dy), (double) z));
                    scene.Add(TransformedShape.NewTransformedShape(mesh, m));

                    // Debug information
                    System.out.println("Brick position: x=" + x + ", y=" + (y + dy) + ", z=" + z);
                }
            }
        }

        var camera = Camera.LookAt(new Vector(-23, 13, 20), new Vector(0, 0, 0), new Vector(0, 0, 1), 45);
        var sampler = DefaultSampler.NewSampler(4, 4);
        var renderer = Renderer.NewRenderer(scene, camera, sampler, WIDTH, HEIGHT);
        renderer.FireflySamples = 64;
        renderer.IterativeRender("toybrick.png", 1000, renderedImage, renderPanel);
    }
}

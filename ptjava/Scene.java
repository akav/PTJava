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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class Scene {

    Colour Color = new Colour();
    ITexture Texture;
    double TextureAngle = 0;
    List<IShape> shapeList = new ArrayList<>();
    List<IShape> lightList = new ArrayList<>();
    IShape[] Lights = new IShape[]{};
    IShape[] Shapes = new IShape[]{};
    Tree tree;
    AtomicInteger rays = new AtomicInteger(0);

    Scene() {
    }

    public void Compile() {
        for (IShape shape : Shapes) {
            if (shape != null) {
                shape.Compile();
            }
        }
        if (tree == null) {
            tree = new Tree(Shapes);
        }
    }

    void Add(IShape shape) {
        shapeList.add(shape);
        if (shape.MaterialAt(new Vector()).Emittance > 0) {
            lightList.add(shape);
            Lights = lightList.toArray(Shapes);
        }
        Shapes = shapeList.toArray(Shapes);
    }

    void Add(List<IShape> shapes) {
        for (IShape shape : shapes) {
            Add(shape);
        }
    }

    int RayCount() {
        return rays.incrementAndGet();
    }

    Hit Intersect(Ray r) {
        rays.incrementAndGet();
        return tree.Intersect(r);
    }
}

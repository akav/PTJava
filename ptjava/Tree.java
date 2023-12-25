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

import static ptjava.Box.BoxForShapes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;

class Tree {

    public Box Box;
    public Node Root;

    public Tree() {
    }

    public Tree(Box box, Node root) {
        this.Box = box;
        this.Root = root;
    }

    public Tree(IShape[] shapes) {
        System.out.println("Building k-d tree: " + shapes.length + "\n");
        Box box = BoxForShapes(shapes);
        Node node = new Node(Axis.AxisNone, 0, shapes, null, null);
        node.Split(0);
        Box = box;
        Root = node;
    }

    public Hit Intersect(Ray r) {
        var tm = Box.Intersect(r);
        double tmin = (double)tm._0;
        double tmax = (double)tm._1;

        if (tmax < tmin || tmax <= 0) {
            return Hit.NoHit;
        }

        return this.Root.Intersect(r, tmin, tmax);
    }

    public class Node {

        Axis Axis;
        double Point;
        IShape[] Shapes;
        Node Left;
        Node Right;       

        public Node(){}

        Node(Axis axis, double point, IShape[] shapes, Node left, Node right) {
            this.Axis = axis;
            this.Point = point;
            this.Shapes = shapes;
            this.Left = left;
            this.Right = right;
        }

        public Node(List<IShape> shapes) {
            this.Axis = ptjava.Axis.AxisNone;
            this.Point = 0;
            this.Shapes = shapes.toArray(new IShape[shapes.size()]);
            this.Left = null;
            this.Right = null;
        }

        Node NewNode(IShape[] shapes) {
            Node n = new Node(Axis.AxisNone, 0, shapes, null, null);
            return n;
        }

        IShape[][] Partition(AtomicInteger size, Axis axis, double point) {
            ArrayList<IShape> left = new ArrayList<>();
            ArrayList<IShape> right = new ArrayList<>();
            
            for (IShape shape : Shapes) {
                if (shape != null) {
                    Box box = shape.BoundingBox();
                    boolean[] lr = box.Partition(axis, point);

                    if (lr[0]) {
                        left.add(shape);
                    }

                    if (lr[1]) {
                        right.add(shape);
                    }
                }
            }
            
            IShape[] lp = new IShape[left.size()];
            IShape[] rp = new IShape[right.size()];
            IShape[] leftp = left.toArray(lp);
            IShape[] rightp = right.toArray(rp);
            return new IShape[][]{leftp,rightp}; 
        }

        public Hit Intersect(Ray r, double tmin, double tmax) {

            double tsplit = 0;
            boolean leftFirst = false;

            switch (Axis) {
                case AxisNone:
                    return IntersectShapes(r);
                case AxisX:
                    tsplit = (Point - r.Origin.getX()) / r.Direction.getX();
                    leftFirst = (r.Origin.getX() < Point) || (r.Origin.getX() == Point && r.Direction.getX() <= 0);
                    break;
                case AxisY:
                    tsplit = (Point - r.Origin.getY()) / r.Direction.getY();
                    leftFirst = (r.Origin.getY() < Point) || (r.Origin.getY() == Point && r.Direction.getY() <= 0);
                    break;
                case AxisZ:
                    tsplit = (Point - r.Origin.getZ()) / r.Direction.getZ();
                    leftFirst = (r.Origin.getZ() < Point) || (r.Origin.getZ() == Point && r.Direction.getZ() <= 0);
                    break;
                default:
                    break;
            }

            Node first = leftFirst ? Left : Right;
            Node second = leftFirst ? Right : Left;

            if (tsplit > tmax || tsplit <= 0)
            {
                return first.Intersect(r, tmin, tmax);
            }
            else if (tsplit < tmin)
            {
                return second.Intersect(r, tmin, tmax);
            }
            else
            {
                Hit h1 = first.Intersect(r, tmin, tsplit);
                if (h1.T <= tsplit)
                {
                    return h1;
                }
                Hit h2 = second.Intersect(r, tsplit, Math.min(tmax, h1.T));
                return h1.T <= h2.T ? h1 : h2;
            }       
        }

        public Hit IntersectShapes(Ray r) {
            Hit hit = Hit.NoHit;
            for (IShape shapes : Shapes) {
                Hit h = shapes.Intersect(r);
                if (h.T < hit.T) {
                    hit = h;
                }
            }
            return hit;
        }

        public double Median(List<Double> list) {

            int middle = list.size() / 2;

            if(list.size() == 0)
            {
                return 0;
            }
            else if (list.size() % 2 == 1) {
                return list.get(middle);
            } else {
                return (list.get(list.size() / 2 - 1) + list.get(list.size() / 2)) / 2.0;
            }
        }

        public AtomicInteger PartitionScore(Axis axis, double point) {
            AtomicInteger left = new AtomicInteger(0);
            AtomicInteger right = new AtomicInteger(0);
            for (IShape shape : this.Shapes) {
                
                if(shape != null)
                {
                    Box box = shape.BoundingBox();
                    boolean[] lr = box.Partition(axis, point);
                    if (lr[0]) {
                        left.incrementAndGet();
                    }
                    if (lr[1]) {
                        right.incrementAndGet();
                    }
                }
                
            }
            if (left.get() >= right.get()) {
                return left;
            } else {
                return right;
            }
        }  

        public void Split(int depth) {

        
            if (this.Shapes.length < 8) {
                return;
            }

            List<Double> xs = new ArrayList<>();
            List<Double> ys = new ArrayList<>();
            List<Double> zs = new ArrayList<>();

            for (IShape shape : this.Shapes) {
                
                if(shape!= null)
                {
                    Box box = shape.BoundingBox();
                    xs.add(box.Min.getX());
                    xs.add(box.Max.getX());
                    ys.add(box.Min.getY());
                    ys.add(box.Max.getY());
                    zs.add(box.Min.getZ());
                    zs.add(box.Max.getZ());
                }
            }

            Collections.sort(xs);
            Collections.sort(ys);
            Collections.sort(zs);
           
            double mx = Median(xs);
            double my = Median(ys);
            double mz = Median(zs);

            AtomicInteger best = new AtomicInteger((int)(Shapes.length * 0.85));
            ptjava.Axis bestAxis = Axis.AxisNone;
            double bestPoint = 0.0;

            var sx = PartitionScore(Axis.AxisX, mx);
            if (sx.get() < best.get()) {
                best = sx;
                bestAxis = Axis.AxisX;
                bestPoint = mx;
            }

            var sy = PartitionScore(Axis.AxisY, my);
            if (sy.get() < best.get()) {
                best = sy;
                bestAxis = Axis.AxisY;
                bestPoint = my;
            }

            var sz = PartitionScore(Axis.AxisZ, mz);
            if (sz.get() < best.get()) {
                best = sz;
                bestAxis = Axis.AxisZ;
                bestPoint = mz;
            }

            if (bestAxis == Axis.AxisNone) {
                return;
            }
        
            var partitions = Partition(best, bestAxis, bestPoint);
            Axis = bestAxis;
            Point = bestPoint;
            Left = NewNode(partitions[0]);
            Right = NewNode(partitions[1]);
            Left.Split(depth + 1);
            Right.Split(depth + 1);
            Shapes = null; 
        }
    }    
}

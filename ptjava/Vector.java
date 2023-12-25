package ptjava;

import java.util.concurrent.ThreadLocalRandom;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorOperators;


public class Vector {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_256;
    public DoubleVector vec;

    public static Vector ORIGIN = new Vector(0, 0, 0);

    public Vector() {
        vec = DoubleVector.fromArray(SPECIES, new double[]{0, 0, 0, 0.0}, 0);
    }

    public double getX() {
        return vec.lane(0);
    }

    public double getY() {
        return vec.lane(1);
    }

    public double getZ() {
        return vec.lane(2);
    }

    // Method to set X and return a new Vector3D
    public Vector withX(double x) {
        return new Vector(vec.withLane(0, x));
    }

    // Method to set Y and return a new Vector3D
    public Vector withY(double y) {
        return new Vector(vec.withLane(1, y));
    }

    // Method to set Z and return a new Vector3D
    public Vector withZ(double z) {
        return new Vector(vec.withLane(2, z));
    }

    private Vector(DoubleVector vec) {
        this.vec = vec;
    }

    public Vector(double x, double y, double z) {
        vec = DoubleVector.fromArray(SPECIES, new double[]{x, y, z, 0.0}, 0);
    }

    public static Vector RandomUnitVector(ThreadLocalRandom rnd) {    
        var z = rnd.nextDouble() * 2.0 - 1.0;
        var a = rnd.nextDouble() * 2.0 * Math.PI;
        var r = Math.sqrt(1.0 - z * z);
        var x = Math.sin(a);
        var y = Math.cos(a);
        return new Vector(r * x, r * y, z);
    } 

    public double Length() {
        double sumOfSquares = this.vec.mul(this.vec).reduceLanes(VectorOperators.ADD);
        return Math.sqrt(sumOfSquares);
    }

    public double LengthN(double n) {
        if (n == 2) {
            return this.Length();
        }

        double sumOfPowers = this.vec.pow(n).reduceLanes(VectorOperators.ADD);
        return Math.pow(sumOfPowers, 1.0 / n);
    }

    public double Dot(Vector b) {

        DoubleVector product = this.vec.mul(b.vec);
        return product.reduceLanes(VectorOperators.ADD);
    }

    public Vector Cross(Vector b) {
        double[] aComponents = new double[4];
        double[] bComponents = new double[4];
        this.vec.intoArray(aComponents, 0);
        b.vec.intoArray(bComponents, 0);
        double x = aComponents[1] * bComponents[2] - aComponents[2] * bComponents[1];
        double y = aComponents[2] * bComponents[0] - aComponents[0] * bComponents[2];
        double z = aComponents[0] * bComponents[1] - aComponents[1] * bComponents[0];
        return new Vector(x, y, z);
    }

    public Vector Normalize() {
        double length = this.Length();
        return new Vector(this.vec.div(length));
    }

    public Vector Negate() {
        return new Vector(this.vec.neg());
    }

    public Vector Abs() {
        return new Vector(this.vec.abs());
    }

    public Vector Add(Vector b) {
        return new Vector(this.vec.add(b.vec));
    }

    public Vector Sub(Vector b) {
        return new Vector(this.vec.sub(b.vec));
    }

    public Vector Mul(Vector b) {
        return new Vector(this.vec.mul(b.vec));
    }

    public Vector Div(Vector b) {
        return new Vector(this.vec.div(b.vec));
    }

    public Vector Mod(Vector b) {
        double[] aComponents = new double[4];
        double[] bComponents = new double[4];
        this.vec.intoArray(aComponents, 0);
        b.vec.intoArray(bComponents, 0);

        double x = aComponents[0] % bComponents[0];
        double y = aComponents[1] % bComponents[1];
        double z = aComponents[2] % bComponents[2];

        return new Vector(x, y, z);
    }

    public Vector AddScalar(double b) {
        return new Vector(this.vec.add(b));
    }

    public Vector SubScalar(double b) {
        return new Vector(this.vec.sub(b));
    }

    public Vector MulScalar(double b) {
        return new Vector(this.vec.mul(b));
    }

    public Vector DivScalar(double b) {
        return new Vector(this.vec.div(b));
    }

    public Vector Min(Vector b) {
        return new Vector(this.vec.min(b.vec));
    }

    public static Vector Min(Vector a, Vector b) {
        return new Vector(a.vec.min(b.vec));
    }

    public Vector Max(Vector b) {
        return new Vector(this.vec.max(b.vec));
    }

    public static Vector Max(Vector a, Vector b) {
        return new Vector(a.vec.max(b.vec));
    }

    public Vector MinAxis() {
        double[] components = new double[4];
        this.vec.abs().intoArray(components, 0);
        double x = components[0];
        double y = components[1];
        double z = components[2];

        if (x <= y && x <= z) {
            return new Vector(1, 0, 0);
        } else if (y <= x && y <= z) {
            return new Vector(0, 1, 0);
        }
        return new Vector(0, 0, 1);
    }

    public double MinComponent() {
        double[] components = new double[4];
        this.vec.intoArray(components, 0);
        double minVal = components[0]; 
        for (int i = 1; i < 3; i++) { 
            if (components[i] < minVal) {
                minVal = components[i];
            }
        }
        return minVal;
    }

    public double MaxComponent() {
        double[] components = new double[4];
        this.vec.intoArray(components, 0);
        double maxVal = components[0]; 
        for (int i = 1; i < 3; i++) { 
            if (components[i] > maxVal) {
                maxVal = components[i];
            }
        }
        return maxVal;
    }

    public Vector Reflect(Vector i) {
        return i.Sub(MulScalar(2 * Dot(i)));
    }

    public Vector Refract(Vector i, double n1, double n2) {
        var nr = n1 / n2;
        var cosI = -Dot(i);
        var sinT2 = nr * nr * (1 - cosI * cosI);

        if (sinT2 > 1)
        {
            return new Vector();
        }

        var cosT = Math.sqrt(1 - sinT2);

        return i.MulScalar(nr).Add(MulScalar(nr * cosI - cosT));
    }

    public double Reflectance(Vector i, double n1, double n2) {
        var nr = n1 / n2;
        var cosI = -Dot(i);
        var sinT2 = nr * nr * (1 - cosI * cosI);

        if (sinT2 > 1)
        {
            return 1;
        }

        var cosT = Math.sqrt(1 - sinT2);
        var rOrth = (n1 * cosI - n2 * cosT) / (n1 * cosI + n2 * cosT);
        var rPar = (n2 * cosI - n1 * cosT) / (n2 * cosI + n1 * cosT);
        return (rOrth * rOrth + rPar * rPar) / 2;
    }
}
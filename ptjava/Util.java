
package ptjava;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class Util {

    public static double INF = 1e9;
    public static double EPS = 1e-9;

    public static int swapInt(int... args) {
        return args[0];
    }

    public static double swapDouble(double... args) {
        return args[0];
    }

    public static double Radians(double degrees) {
        return degrees * Math.PI / 180;
    }

    public static double Degrees(double radians) {
        return radians * 180 / Math.PI;
    }

    public static Vector Cone(Vector direction, double theta, double u, double v, ThreadLocalRandom rand) {
        if (theta < EPS) {
            return direction;
        }
        theta = theta * (1 - (2 * Math.acos(u) / Math.PI));
        var m1 = Math.sin(theta);
        var m2 = Math.cos(theta);
        var a = v * 2 * Math.PI;
        var q = Vector.RandomUnitVector(rand);
        var s = direction.Cross(q);
        var t = direction.Cross(s);
        var d = new Vector();
        d = d.Add(s.MulScalar(m1 * Math.cos(a)));
        d = d.Add(t.MulScalar(m1 * Math.sin(a)));
        d = d.Add(direction.MulScalar(m2));
        d = d.Normalize();
        return d;
    }

    static double Median(List<Double> a) {
        int middle = a.size() / 2;
        if (a.size() % 2 == 1) {
            return a.get(middle);
        } else {
            return (a.get(middle - 1) + a.get(middle)) / 2.0;
        }
    }

    double Median(double[] items) {
        var n = items.length;
        if (n == 0) {
            return 0;
        } else if (n % 2 == 1) {
            return items[items.length / 2];
        } else {
            var a = items[items.length / 2 - 1];
            var b = items[items.length / 2];
            return (a + b) / 2;
        }
    }

    public static double Fract(double x) {
        double ret = x - (int) x;
        return ret;
    }

    public static double Clamp(double x, double lo, double hi) {
        if (x < lo) {
            return lo;
        }
        if (x > hi) {
            return hi;
        }
        return x;
    }

    public static int ClampInt(int x, int lo, int hi) {
        if (x < lo) {
            return lo;
        }
        if (x > hi) {
            return hi;
        }
        return x;
    }

    static String NumberString(Double x) {
        return x.toString();
    }

    double[] ParseFloats(String[] items) {
        double[] result = new double[items.length];
        for (int i = 0; i < items.length; i++) {
            result[i] = Double.parseDouble(items[i]);
        }
        return result;
    }

    int[] ParseInts(String[] items) {
        int[] result = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            result[i] = Integer.parseInt(items[i]);
        }
        return result;
    }

    static String DurationString(Duration time) {
        return "Current Duration" + time.toString();
    }

    static String RelativePath(String path1) {
        Path filepath = Paths.get(path1);
        return filepath.relativize(filepath).toString();
    }

    static String NumberString(double x) {
        return Double.toString(x);
    }

    static double[] ParseFloats_(String[] items) {
        double[] doublearray = Arrays.stream(items).mapToDouble(Double::parseDouble).toArray();
        return doublearray;
    }

    static int[] ParseInts_(String[] items) {
        int[] intarray = Arrays.stream(items).mapToInt(Integer::parseInt).toArray();
        return intarray;
    }

    public static Mesh CreateBrick(int color) throws IOException
    {
        var material = Material.GlossyMaterial(Colour.HexColor(color), 1.3F, Radians(20));
        var mesh = STL.Load("models/toybrick.stl", material);
        mesh.SmoothNormalsThreshold(Radians(20));
        mesh.FitInside(new Box(new Vector(), new Vector(2, 4, 10)), new Vector( 0, 0, 0 ));
        return mesh;
    }
}

package ptjava;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class STL {

    static class STLTriangle {
        Vector Normal;
        Vector A;
        Vector B;
        Vector C;
        short AttributeByteCount;

        public STLTriangle(Vector normal, Vector a, Vector b, Vector c, short attributeByteCount) {
            this.Normal = normal;
            this.A = a;
            this.B = b;
            this.C = c;
            this.AttributeByteCount = attributeByteCount;
        }
    }

    public static Mesh Load(String filePath, Material material) throws IOException {
        try (FileInputStream stream = new FileInputStream(filePath)) {
            if (IsBinaryStl(stream)) {
                return ReadBinaryStl(filePath, material);
            } else {
                return ReadTextStl(filePath, material);
            }
        }
    }

    static boolean IsBinaryStl(FileInputStream stream) throws IOException {
        final int HeaderSize = 80;
        if (stream.available() < HeaderSize + 4) return false;

        byte[] header = new byte[HeaderSize];
        stream.read(header, 0, HeaderSize);
        stream.getChannel().position(0);

        String headerString = new String(header);
        if (headerString.trim().startsWith("solid")) {
            byte[] content = new byte[256];
            stream.read(content);
            stream.getChannel().position(0);
            String contentString = new String(content);
            return !contentString.contains("facet");
        }

        return true;
    }

    public static Mesh ReadTextStl(String filename, Material material) {
        final String regex = "\\s*(facet normal|vertex)\\s+(?<X>[^\\s]+)\\s+(?<Y>[^\\s]+)\\s+(?<Z>[^\\s]+)";
        final int numberStyle = Pattern.CASE_INSENSITIVE;

        List<Vector> facetNormals = new ArrayList<>();
        List<Vector> vertices = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();

        try (BufferedReader file = new BufferedReader(new FileReader(filename))) {
            String line = file.readLine();
            if (line == null || !line.contains("solid")) {
                throw new IOException("Invalid STL file: Missing 'solid' header.");
            }

            while ((line = file.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("facet normal")) {
                    facetNormals.add(ParseVector(line, regex, numberStyle));
                } else if (line.startsWith("vertex")) {
                    vertices.add(ParseVector(line, regex, numberStyle));
                } else if (line.startsWith("endfacet")) {
                    if (vertices.size() >= 3) {
                        Triangle triangle = new Triangle(vertices.get(0), vertices.get(1), vertices.get(2), material);
                        triangle.T1 = vertices.get(0);
                        triangle.T2 = vertices.get(1);
                        triangle.T3 = vertices.get(2);
                        triangle.N1 = facetNormals.get(facetNormals.size() - 1);
                        triangle.N2 = facetNormals.get(facetNormals.size() - 1);
                        triangle.N3 = facetNormals.get(facetNormals.size() - 1);
                        triangle.FixNormals();
                        triangles.add(triangle);
                        vertices.clear();
                    }
                } else if (line.startsWith("endsolid")) {
                    break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Failed to read STL file: " + ex.getMessage());
            return new Mesh();
        }

        return Mesh.NewMesh(triangles.toArray(new Triangle[0]));
    }

    private static Vector ParseVector(String line, String regex, int numberStyle) throws IOException {
        Pattern pattern = Pattern.compile(regex, numberStyle);
        Matcher match = pattern.matcher(line);
        if (!match.find()) {
            throw new IOException("Invalid line format: " + line);
        }

        double x = Double.parseDouble(match.group("X"));
        double y = Double.parseDouble(match.group("Y"));
        double z = Double.parseDouble(match.group("Z"));

        return new Vector(x, y, z);
    }

    public static Mesh ReadBinaryStl(String filePath, Material material) {
        List<Triangle> tList = new ArrayList<>();

        try (FileInputStream stream = new FileInputStream(filePath)) {
            final int HeaderSize = 80;
            final int FacetSize = 50;

            stream.getChannel().position(HeaderSize);

            byte[] facetCountBytes = new byte[4];
            stream.read(facetCountBytes, 0, 4);
            int facetCount = ByteBuffer.wrap(facetCountBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

            for (int i = 0; i < facetCount; i++) {
                byte[] facetBytes = new byte[FacetSize];
                stream.read(facetBytes, 0, FacetSize);

                Vector a = new Vector(
                        ByteBuffer.wrap(facetBytes, 12, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat(),
                        ByteBuffer.wrap(facetBytes, 16, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat(),
                        ByteBuffer.wrap(facetBytes, 20, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat()
                );

                Vector b = new Vector(
                        ByteBuffer.wrap(facetBytes, 24, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat(),
                        ByteBuffer.wrap(facetBytes, 28, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat(),
                        ByteBuffer.wrap(facetBytes, 32, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat()
                );

                Vector c = new Vector(
                        ByteBuffer.wrap(facetBytes, 36, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat(),
                        ByteBuffer.wrap(facetBytes, 40, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat(),
                        ByteBuffer.wrap(facetBytes, 44, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat()
                );

                Vector normal = new Vector(
                        ByteBuffer.wrap(facetBytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat(),
                        ByteBuffer.wrap(facetBytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat(),
                        ByteBuffer.wrap(facetBytes, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat()
                );

                short attributeByteCount = ByteBuffer.wrap(facetBytes, 48, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();

                Triangle t = new Triangle(a, b, c, material);
                t.T1 = a;
                t.T2 = b;
                t.T3 = c;
                t.N1 = normal; // Ensure normal is set
                t.N2 = normal; // Ensure normal is set
                t.N3 = normal; // Ensure normal is set
                t.FixNormals();

                tList.add(t);
            }
        } catch (Exception ex) {
            System.out.println("Error reading binary STL file: " + ex.getMessage());
        }

        return Mesh.NewMesh(tList.toArray(new Triangle[0]));
    }
}

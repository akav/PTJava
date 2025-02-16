package ptjava;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OBJ {

    private static Map<String, Material> matList = new HashMap<>();

    public static Mesh Load(String filePath, Material parent) throws IOException {
        List<Vector> vertices = new ArrayList<>();
        List<Vector> textureCoords = new ArrayList<>();
        List<Vector> normals = new ArrayList<>();
        normals.add(new Vector(0, 0, 0)); // Add a default normal
        List<IShape> shapes = new ArrayList<>();
        Material material = parent;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                if (tokens.length == 0 || tokens[0].isEmpty()) continue;

                switch (tokens[0]) {
                    case "mtllib":
                        String mtlPath = filePath.substring(0, filePath.lastIndexOf('/') + 1) + tokens[1];
                        LoadMTL(mtlPath, parent);
                        break;
                    case "usemtl":
                        material = matList.getOrDefault(tokens[1], parent);
                        break;
                    case "v":
                        vertices.add(new Vector(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3])));
                        break;
                    case "vt":
                        textureCoords.add(new Vector(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), 0));
                        break;
                    case "vn":
                        normals.add(new Vector(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3])));
                        break;
                    case "f":
                        List<Vector> faceVertices = new ArrayList<>();
                        List<Vector> faceTextureCoords = new ArrayList<>();
                        List<Vector> faceNormals = new ArrayList<>();

                        for (int i = 1; i < tokens.length; i++) {
                            String[] indices = tokens[i].split("/");
                            int vertexIndex = Integer.parseInt(indices[0]) - 1;
                            faceVertices.add(vertices.get(vertexIndex));

                            if (indices.length > 1 && !indices[1].isEmpty()) {
                                int textureIndex = Integer.parseInt(indices[1]) - 1;
                                if (textureIndex >= 0 && textureIndex < textureCoords.size()) {
                                    faceTextureCoords.add(textureCoords.get(textureIndex));
                                }
                            }

                            if (indices.length > 2 && !indices[2].isEmpty()) {
                                int normalIndex = Integer.parseInt(indices[2]) - 1;
                                if (normalIndex >= 0 && normalIndex < normals.size()) {
                                    faceNormals.add(normals.get(normalIndex));
                                }
                            }
                        }

                        if (faceVertices.size() == 3) {
                            shapes.add(new Triangle(faceVertices.get(0), faceVertices.get(1), faceVertices.get(2), material));
                        } else if (faceVertices.size() == 4) {
                            shapes.add(new Quad(faceVertices.get(0), faceVertices.get(1), faceVertices.get(2), faceVertices.get(3), material));
                        }
                        break;
                }
            }
        }

        Mesh mesh = new Mesh(shapes);

        // Debug info
        System.out.println("Mesh loaded from: " + filePath);
        System.out.println("Number of vertices: " + vertices.size());
        System.out.println("Number of texture coordinates: " + textureCoords.size());
        System.out.println("Number of normals: " + normals.size());
        System.out.println("Number of shapes: " + shapes.size());

        return mesh;        
    }

    public static void LoadMTL(String path, Material parent) throws IOException {
        Material material = parent;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                if (tokens.length == 0) continue;

                switch (tokens[0]) {
                    case "newmtl":
                        material = new Material();
                        matList.put(tokens[1], material);
                        break;
                    case "Ke":
                        double max = Math.max(Math.max(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2])), Double.parseDouble(tokens[3]));
                        if (max > 0) {
                            material.Color = new Colour(Double.parseDouble(tokens[1]) / max, Double.parseDouble(tokens[2]) / max, Double.parseDouble(tokens[3]) / max);
                            material.Emittance = max;
                        }
                        break;
                    case "Kd":
                        material.Color = new Colour(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3]));
                        break;
                    case "map_Kd":
                        material.Texture = ColorTexture.GetTexture(path.substring(0, path.lastIndexOf('/') + 1) + tokens[1]);
                        break;
                    case "map_bump":
                        material.NormalTexture = ColorTexture.GetTexture(path.substring(0, path.lastIndexOf('/') + 1) + tokens[1]).Pow(1 / 2.2);
                        break;
                }
            }
        }
    }

    public static List<IShape> LoadList(String filePath, Material defaultMaterial) throws IOException {
        List<Vector> vertices = new ArrayList<>();
        List<Vector> normals = new ArrayList<>();
        List<Vector> textureCoords = new ArrayList<>();
        List<IShape> shapes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                if (tokens.length == 0) continue;

                switch (tokens[0]) {
                    case "v":
                        // Vertex position
                        double x = Double.parseDouble(tokens[1]);
                        double y = Double.parseDouble(tokens[2]);
                        double z = Double.parseDouble(tokens[3]);
                        vertices.add(new Vector(x, y, z));
                        break;
                    case "vt":
                        // Texture coordinate
                        double u = Double.parseDouble(tokens[1]);
                        double v = Double.parseDouble(tokens[2]);
                        textureCoords.add(new Vector(u, v, 0));
                        break;
                    case "vn":
                        // Vertex normal
                        double nx = Double.parseDouble(tokens[1]);
                        double ny = Double.parseDouble(tokens[2]);
                        double nz = Double.parseDouble(tokens[3]);
                        normals.add(new Vector(nx, ny, nz));
                        break;
                    case "f":
                        // Face
                        List<Vector> faceVertices = new ArrayList<>();
                        List<Vector> faceNormals = new ArrayList<>();
                        List<Vector> faceTextureCoords = new ArrayList<>();

                        for (int i = 1; i < tokens.length; i++) {
                            String[] indices = tokens[i].split("/");
                            int vertexIndex = Integer.parseInt(indices[0]) - 1;
                            faceVertices.add(vertices.get(vertexIndex));

                            if (indices.length > 1 && !indices[1].isEmpty()) {
                                int textureIndex = Integer.parseInt(indices[1]) - 1;
                                faceTextureCoords.add(textureCoords.get(textureIndex));
                            }

                            if (indices.length > 2 && !indices[2].isEmpty()) {
                                int normalIndex = Integer.parseInt(indices[2]) - 1;
                                faceNormals.add(normals.get(normalIndex));
                            }
                        }

                        // Create a shape from the face vertices
                        if (faceVertices.size() == 3) {
                            shapes.add(new Triangle(faceVertices.get(0), faceVertices.get(1), faceVertices.get(2), defaultMaterial));
                        } else if (faceVertices.size() == 4) {
                            shapes.add(new Quad(faceVertices.get(0), faceVertices.get(1), faceVertices.get(2), faceVertices.get(3), defaultMaterial));
                        }
                        break;
                }
            }
        }

        return shapes;
    }
}


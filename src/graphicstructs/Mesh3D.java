package graphicstructs;

import datakit.SinglyLinkedList;
import mathkit.Vector;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * Structure to store triangles in a 3D space as a mesh
 */
public class Mesh3D implements Iterable<Triangle3D> {
    /**
     * Container for all triangles in mesh
     */
    private final SinglyLinkedList<Triangle3D> con;

    // Constructors

    /**
     * Create a mesh from any amount of triangles
     * @param triangles triangles that create this mesh
     */
    public Mesh3D(Triangle3D ... triangles) {
        if (triangles == null) {
            throw new IllegalArgumentException("Cannot create a mesh from null triangles");
        }

        con = new SinglyLinkedList<>(triangles);
    }

    public Mesh3D(SinglyLinkedList<Triangle3D> triList) {
        if (triList == null) {
            throw new IllegalArgumentException("Cannot create a mesh from a null list");
        }

        con = triList;
    }

    // Statics Constructors

    /**
     * Create a mesh of a rectangular prism from its bottom left corner
     * @param x x value of bottom left corner
     * @param y y value of bottom left corner
     * @param z z value of bottom left corner
     * @param width width (in x dimension), must be greater than 0
     * @param height height (in y dimension), must be greater than 0
     * @param depth depth (in z dimension), must be greater than 0
     * @return a mesh of a rectangular prism
     */
    public static Mesh3D rectangularPrism(double x, double y, double z, double width, double height, double depth) {
        if (width <= 0.0 || height <= 0.0 || depth <= 0.0) {
            throw new IllegalArgumentException("Invalid dimensions");
        }

        double xFar = x + width;
        double yFar = y + height;
        double zFar = z + depth;

        Vector bottomLeft = new Vector(x, y, z);
        Vector topLeft = new Vector(x, yFar, z);
        Vector topRight = new Vector(xFar, yFar, z);
        Vector bottomRight = new Vector(xFar, y, z);

        Vector bottomLeftFar = new Vector(x, y, zFar);
        Vector topLeftFar = new Vector(x, yFar, zFar);
        Vector topRightFar = new Vector(xFar, yFar, zFar);
        Vector bottomRightFar = new Vector(xFar, y, zFar);

        return new Mesh3D(
                // front face
                new Triangle3D(bottomLeft, topLeft, topRight),
                new Triangle3D(topRight, bottomRight, bottomLeft),

                // back face
                new Triangle3D(bottomRightFar, topRightFar, topLeftFar),
                new Triangle3D(topLeftFar, bottomLeftFar, bottomRightFar),

                // top face
                new Triangle3D(topLeft, topLeftFar, topRightFar),
                new Triangle3D(topRightFar, topRight, topLeft),

                // bottom face
                new Triangle3D(bottomRight, bottomRightFar, bottomLeftFar),
                new Triangle3D(bottomLeftFar, bottomLeft, bottomRight),

                // right face
                new Triangle3D(bottomRight, topRight, topRightFar),
                new Triangle3D(topRightFar, bottomRightFar, bottomRight),

                // left face
                new Triangle3D(bottomLeftFar, topLeftFar, topLeft),
                new Triangle3D(topLeft, bottomLeft, bottomLeftFar)
        );
    }

    /**
     * Create a mesh of a cube from its bottom left corner
     * @param x x value of bottom left corner
     * @param y y value of bottom left corner
     * @param z z value of bottom left corner
     * @param length length of each side
     * @return a mesh of a cube
     */
    public static Mesh3D cube(double x, double y, double z, double length) {
        return rectangularPrism(x, y, z, length, length, length);
    }

    public static Mesh3D createMeshFromFile(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("Cannot make a mesh from a null file");
        }

        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            List<Vector> vecList = new ArrayList<>();
            SinglyLinkedList<Triangle3D> triList = new SinglyLinkedList<>();

            String line = fileReader.readLine();

            while (line != null) {
                if (!line.isEmpty()) {
                    Scanner lineScanner = new Scanner(line);

                    switch (lineScanner.next()) {
                        case "v":
                            double xCoord = lineScanner.nextDouble();
                            double yCoord = lineScanner.nextDouble();
                            double zCoord = lineScanner.nextDouble();

                            Vector newVec = new Vector(xCoord, yCoord, zCoord);
                            vecList.add(newVec);
                            break;
                        case "f":
                            Vector firstVec = vecList.get(lineScanner.nextInt() - 1);
                            Vector secondVec = vecList.get(lineScanner.nextInt() - 1);
                            Vector thirdVec = vecList.get(lineScanner.nextInt() - 1);

                            Triangle3D newTri = new Triangle3D(firstVec, secondVec, thirdVec);
                            triList.add(newTri);
                            break;
                    }
                }

                line = fileReader.readLine();
            }

            return new Mesh3D(triList);
        }
    }

    // Getters

    /**
     * @param index index of triangle to retrieve, must be within the size of the mesh
     * @return a triangle from this mesh at a specified index
     */
    public Triangle3D get(int index) {
        if (index < 0 || index >= con.size()) {
            throw new IllegalArgumentException("Index " + index + " is out of bounds");
        }

        return con.get(index);
    }

    // Overrides

    /**
     * @return the triangles that create this mesh
     */
    @Override
    public String toString() {
        return con.toString();
    }

    /**
     * @return an iterator to traverse the triangles that compose this mesh
     */
    @Override
    public Iterator<Triangle3D> iterator() {
        return con.iterator();
    }

}

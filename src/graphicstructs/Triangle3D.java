package graphicstructs;

import mathkit.Vector;

import java.awt.Color;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Structure to store three 3D vectors as a triangle
 * <br>The triangles are wound clockwise
 */
public class Triangle3D implements Iterable<Vector> {
    public static final int SIZE = 3;

    /**
     * Container for 3 vertices of triangle
     */
    private final Vector[] con;

    /**
     * Color of this triangle
     */
    private Color color;

    // Constructors

    /**
     * Initialize this triangle with three 3D points as arrays
     * @param vec1 first point as vector, must be 3D
     * @param vec2 second point as vector, must be 3D
     * @param vec3 third point as vector, must be 3D
     */
    public Triangle3D(double[] vec1, double[] vec2, double[] vec3) {
        if (vec1.length != 3 || vec2.length != 3 || vec3.length != 3) {
            throw new IllegalArgumentException("All points must be 3 dimensional");
        }

        con = new Vector[3];

        con[0] = new Vector(vec1);
        con[1] = new Vector(vec2);
        con[2] = new Vector(vec3);

        color = Color.WHITE;
    }

    /**
     * Initialize this triangle with 3 points as vectors
     * @param vec1 first vector, must be 3D
     * @param vec2 second vector, must be 3D
     * @param vec3 third vector, must be 3D
     */
    public Triangle3D(Vector vec1, Vector vec2, Vector vec3) {
        if (vec1.size() != 3 || vec2.size() != 3 || vec3.size() != 3) {
            throw new IllegalArgumentException("All points must be 3 dimensional");
        }

        con = new Vector[SIZE];

        con[0] = vec1;
        con[1] = vec2;
        con[2] = vec3;

        color = Color.WHITE;
    }

    /**
     * Initialize this triangle with three 3D points as arrays and with a color
     * @param vec1 first point as vector
     * @param vec2 second point as vector
     * @param vec3 third point as vector
     * @param color color to set for this triangle
     */
    public Triangle3D(double[] vec1, double[] vec2, double[] vec3, Color color) {
        this(vec1, vec2, vec3);

        if (color == null) {
            throw new IllegalArgumentException("Cannot set color to null");
        }

        this.color = color;
    }

    /**
     * Initialize this triangle with 3 points as vectors
     * @param vec1 first vector, must be 3D
     * @param vec2 second vector, must be 3D
     * @param vec3 third vector, must be 3D
     * @param color color to set for this triangle
     */
    public Triangle3D(Vector vec1, Vector vec2, Vector vec3, Color color) {
        this(vec1, vec2, vec3);

        if (color == null) {
            throw new IllegalArgumentException("Cannot set color to null");
        }

        this.color = color;
    }

    // Setters

    /**
     * Set specified vector of triangle to a new point from a 2D array
     * @param index index of vector from triangle to change, must be within the size of a triangle
     * @param vector new vector as 2D array to replace old vector, cannot be null and must be 3D
     */
    public void set(int index, double[] vector) {
        if (index < 0 || index > 2) {
            throw new IllegalArgumentException("Index " + index + " is out of bounds");
        } else if (vector == null) {
            throw new IllegalArgumentException("Cannot replace this vector with a null vector");
        } else if (vector.length != 3) {
            throw new IllegalArgumentException("Cannot replace this vector with a non 3D vector");
        }

        con[index] = new Vector(vector);
    }

    /**
     * Set specified vector of triangle to a new point from a vector
     * @param index index of vector from triangle to change
     * @param vector new vector as vector to replace old vector
     */
    public void set(int index, Vector vector) {
        if (index < 0 || index > 2) {
            throw new IllegalArgumentException("Index " + index + " is out of bounds");
        } else if (vector == null) {
            throw new IllegalArgumentException("Cannot replace this vector with a null vector");
        } else if (vector.size() != 3) {
            throw new IllegalArgumentException("Cannot replace this vector with a non 3D vector");
        }

        con[index] = vector;
    }

    /**
     * @param color color to set for this triangle
     */
    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Cannot set color to null");
        }

        this.color = color;
    }

    /**
     * @param shadingVal value to scale colors up and down in hue, must be between 0 and 1
     */
    public void setShading(double shadingVal) {
        if (shadingVal < 0.0 || shadingVal > 1.0) {
            throw new IllegalArgumentException("Shading value must be between 0 and 1");
        }

        color = new Color(
                (int) (shadingVal * color.getRed()),
                (int) (shadingVal * color.getGreen()),
                (int) (shadingVal * color.getBlue())
        );
    }

    // Getters

    /**
     * @param index of vector from triangle to return, must be within the size of a triangle
     * @return the vector at the specified index of this triangle
     */
    public Vector get(int index) {
        if (index < 0 || index > 2) {
            throw new IllegalArgumentException("Index " + index + " is out of bounds");
        }

        return con[index];
    }

    /**
     * @return the color of this triangle
     */
    public Color getColor() {
        return color;
    }

    /**
     * @return the surface normal of this triangle
     */
    public Vector getSurfaceNormal() {
        Vector vec1 = con[1].subtract(con[0]);
        Vector vec2 = con[2].subtract(con[0]);
        return vec1.crossProduct(vec2).normalized();
    }

    // Overrides

    /**
     * @return the vectors that create this triangle in order
     */
    @Override
    public String toString() {
        return Arrays.toString(con);
    }

    /**
     * @return an iterator to traverse the vectors that compose this triangle
     */
    @Override
    public Iterator<Vector> iterator() {
        return Arrays.stream(con).iterator();
    }
}

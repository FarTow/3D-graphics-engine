package graphicstructs;

import mathkit.Matrix;
import mathkit.Vector;

/**
 * Utility class containing various methods to perform calculations necessary for 3D spaces
 */
public final class Math3D {
    private static final Vector RIGHT = new Vector(1, 0, 0);

    private static final Vector UP = new Vector(0, 1, 0);

    private static final Vector FORWARD = new Vector(0, 0, 1);

    private Math3D() {

    }

    // Math

    /**
     * @param angleRadians angle to rotate by
     * @return a rotation matrix about the x axis
     */
    public static Matrix xRotationMatrix(double angleRadians) {
        Matrix rotationMatrix = new Matrix(3, 3);

        double cosOfAngle = Math.cos(angleRadians);
        double sinOfAngle = Math.sin(angleRadians);

        rotationMatrix.set(0, 0, 1.0);
        rotationMatrix.set(1, 1, cosOfAngle);
        rotationMatrix.set(1, 2, sinOfAngle);
        rotationMatrix.set(2, 1, -sinOfAngle);
        rotationMatrix.set(2, 2, cosOfAngle);

        return rotationMatrix;
    }

    /**
     * @param angleRadians angle to rotate by
     * @return a rotation matrix about the y axis
     */
    public static Matrix yRotationMatrix(double angleRadians) {
        Matrix rotationMatrix = new Matrix(3, 3);

        double cosOfAngle = Math.cos(angleRadians);
        double sinOfAngle = Math.sin(angleRadians);

        rotationMatrix.set(0, 0, cosOfAngle);
        rotationMatrix.set(0, 2, sinOfAngle);
        rotationMatrix.set(2, 0, -sinOfAngle);
        rotationMatrix.set(1, 1, 1.0);
        rotationMatrix.set(2, 2, cosOfAngle);

        return rotationMatrix;
    }

    /**
     * @param angleRadians angle to rotate by
     * @return a rotation matrix about the z axis
     */
    public static Matrix zRotationMatrix(double angleRadians) {
        Matrix rotationMatrix = new Matrix(3, 3);

        double cosOfAngle = Math.cos(angleRadians);
        double sinOfAngle = Math.sin(angleRadians);

        rotationMatrix.set(0, 0, cosOfAngle);
        rotationMatrix.set(0, 1, sinOfAngle);
        rotationMatrix.set(1, 0, -sinOfAngle);
        rotationMatrix.set(1, 1, cosOfAngle);
        rotationMatrix.set(2, 2, 1.0);

        return rotationMatrix;
    }

    /**
     * @param yaw degree of rotation about the y axis
     * @param pitch degree of rotation about the x axis
     * @param roll degree of rotation about the z axis
     * @return a rotation matrix to rotate a vector by a desired amount of degrees about three axis
     */
    public static Matrix rotationMatrix(double yaw, double pitch, double roll) {
        Matrix zRotMat = Math3D.zRotationMatrix(roll);
        Matrix yRotMat = Math3D.yRotationMatrix(yaw);
        Matrix xRotMat = Math3D.xRotationMatrix(pitch);
        return xRotMat.multiplyMatrix(yRotMat).multiplyMatrix(zRotMat);
    }

    /**
     * @param rotationMatrix matrix to rotate the right vector by
     * @return the right direction vector rotated
     */
    public static Vector rightRotated(Matrix rotationMatrix) {
        return RIGHT.multiplyMatrix(rotationMatrix);
    }

    /**
     * @param rotationMatrix matrix to rotate the right vector by
     * @return the up direction vector rotated
     */
    public static Vector upRotated(Matrix rotationMatrix) {
        return UP.multiplyMatrix(rotationMatrix);
    }

    /**
     * @param rotationMatrix matrix to rotate the right vector by
     * @return the forward direction vector rotated
     */
    public static Vector forwardRotated(Matrix rotationMatrix) {
        return FORWARD.multiplyMatrix(rotationMatrix);
    }

}

package graphicstructs;

import mathkit.Constants;
import mathkit.Vector;

import java.util.Objects;

public class Plane {
    /**
     * A point on this plane
     */
    private final Vector point;

    /**
     * The normal vector to this plane
     */
    private final Vector normal;

    // Constructors

    public Plane(Vector point, Vector normal) {
        this.point = new Vector(point);
        this.normal = new Vector(normal).normalized();
    }

    // Math

    /**
     * @param otherPoint another point in the 3D space
     * @return the distance of otherPoint from the plane
     */
    public double distanceFromPoint(Vector otherPoint) {
        if (otherPoint == null) {
            throw new IllegalArgumentException("Cannot get the distance between the plane and a null point");
        }

        Vector pointToPointVec = otherPoint.subtract(point);
        return normal.dotProduct(pointToPointVec);
    }

    /**
     * @param pointA a point in the 3D space
     * @param pointB a point in the 3D space
     * @return whether or not the line created by pointA and pointB intersects the plane
     */
    public boolean lineIntersectsPlane(Vector pointA, Vector pointB) {
        if (pointA == null || pointB == null) {
            throw new IllegalArgumentException("Cannot generate a line from null point(s)");
        }

        double distA = distanceFromPoint(pointA);
        double distB = distanceFromPoint(pointB);

        return distA < 0 && distB > 0;
    }

    /**
     * @param pointA starting point of the line segment
     * @param pointB ending point of the line segment
     * @return the point of intersection between the line segment formed from points A and B and the plane
     */
    public Vector lineIntersectPlanePoint(Vector pointA, Vector pointB) {
//        if (!lineIntersectsPlane(pointA, pointB)) {
//            throw new IllegalArgumentException("Cannot get the point of intersection from a line that does not " +
//                    "intersect the plane");
//        }

        double planeDotProd = point.dotProduct(normal);
        double aPlaneDotProd = pointA.dotProduct(normal);
        double bPlaneDotProd = pointB.dotProduct(normal);
        double t = (planeDotProd - aPlaneDotProd) / (bPlaneDotProd - aPlaneDotProd);

        Vector lineStartToEnd = pointB.subtract(pointA);
        Vector lineToIntersect = lineStartToEnd.multiplyByScalar(t);

        return pointA.add(lineToIntersect);
    }

    // Overrides

    /**
     * @return a string containing the point and normal of this plane
     */
    @Override
    public String toString() {
        return "Point: " + point + " | Normal: " + normal;
    }

    /**
     * Two planes are considered equal if they have the same normal and a point along the same plane
     * @param obj object to compare to
     * @return if an object is equal to this plane
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Plane)) {
            return false;
        }

        Plane objAsPlane = (Plane) obj;

        if (!objAsPlane.normal.equals(normal)) {
            return false;
        }

        double dotProduct = objAsPlane.point.dotProduct(point);

        return Constants.doubleEqualsZero(dotProduct);
    }

    /**
     * @return the hash code of this plane
     */
    @Override
    public int hashCode() {
        return Objects.hash(point, normal);
    }

}

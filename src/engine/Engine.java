package engine;

import datakit.SinglyLinkedList;
import graphicstructs.Mesh3D;
import graphicstructs.Plane;
import graphicstructs.Triangle3D;
import mathkit.Matrix;
import mathkit.Vector;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * JPanel to calculate and render 3D Graphics
 */
public class Engine extends JPanel {
    // Vector Indices

    /**
     * Index of where the x value of a vector will be
     */
    private final int X_INDEX = 0;

    /**
     * Index of where the y value of a vector will be
     */
    private final int Y_INDEX = 1;

    /**
     * Index of where the z value of a vector will be
     */
    private final int Z_INDEX = 2;

    // Plane Defaults

    /**
     * Highest value visible in the z-plane of the 3D environment
     */
    private final double Z_FAR = 1000.0;

    /**
     * Lowest value visible in the z-plane of the 3D environment
     */
    private final double Z_NEAR = 0.1;

    // Mesh and Triangle

    /**
     * List of all meshes that exist
     */
    private final ArrayList<Mesh3D> meshes;

    /**
     * List of triangles to convert to projected values to be rendered
     */
    private SinglyLinkedList<Triangle3D> trianglesToRender;

    /**
     * List of all triangles that are currently being rendered onto the canvas
     */
    private SinglyLinkedList<Triangle3D> trianglesBeingRendered;

    /**
     * Matrix to convert a 3D coordinate into a 2D point on the screen
     */
    private Matrix projMat;

    // Rendering

    /**
     * Image taking up the whole screen
     */
    private BufferedImage canvas;

    /**
     * Container for colors for each pixel in the canvas
     */
    private int[] canvasRaster;

    /**
     * Container for the z-value of each pixel in the canvas
     */
    private double[] depthBuffer;

    // Engine

    /**
     * Engine loop thread
     */
    private Timer timer;

    /**
     * Engine loop task performer
     */
    private TimerTask timerTask;

    private Camera camera;

    /**
     * Amount of frames to run per second
     */
    private double frameRate;

    // Constructors

    /**
     * Create a default engine
     * @param frameRate frames per second the engine should run at
     */
    public Engine(double frameRate) {
        this.frameRate = frameRate;

        // create a timer with a timer task that runs the engine
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                update();
            }
        };

        camera = new Camera(.25, .25, .25, Math.toRadians(1));
        trianglesToRender = new SinglyLinkedList<>();
        trianglesBeingRendered = new SinglyLinkedList<>();
        meshes = new ArrayList<>();

        initMeshes();
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);

        // keyboard communication
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { // exit application
                    System.exit(0);
                }
            }
        });
        addKeyListener(camera);
    }


    // Initialization

    // Create all desired meshes here
    private void initMeshes() {
        try {
            meshes.add(Mesh3D.createMeshFromFile(new File("res/models/Mountains.obj")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the engine by starting the timer to run at a set interval
     */
    public void start() {
        projMat = projectionMatrix(Math.PI / 2);
        canvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        canvasRaster = ((DataBufferInt) canvas.getRaster().getDataBuffer()).getData();
        depthBuffer = new double[canvasRaster.length];
        timer.scheduleAtFixedRate(timerTask, 0, (long) (1000 / frameRate));
        requestFocus();
    }

    /**
     * Stop the engine by stopping the timer
     */
    public void stop() {
        timer.cancel();
    }

    // Render

    /**
     * All rendering occurs here
     * @param g graphics object to draw onto screen with
     */
    @Override
    protected void paintComponent(Graphics g) {
        Arrays.fill(canvasRaster, 0);
        Arrays.fill(depthBuffer, Z_FAR);

        // fill all triangles (triangles are already in projected form)
        for (Triangle3D tri : trianglesBeingRendered) {
            fillTriangle(tri);
        }

        g.drawImage(canvas, 0, 0, null);
    }

    private void drawTriangle(Triangle3D projTri, Graphics g) {
        Vector point1 = projTri.get(0);
        Vector point2 = projTri.get(1);
        Vector point3 = projTri.get(2);

        g.drawLine((int) point1.get(X_INDEX), (int) point1.get(Y_INDEX),
                (int) point2.get(X_INDEX), (int) point2.get(Y_INDEX));

        g.drawLine((int) point2.get(X_INDEX), (int) point2.get(Y_INDEX),
                (int) point3.get(X_INDEX), (int) point3.get(Y_INDEX));

        g.drawLine((int) point3.get(X_INDEX), (int) point3.get(Y_INDEX),
                (int) point1.get(X_INDEX), (int) point1.get(Y_INDEX));
    }

    // Fill a projected triangle with a scanline algorithm
    // Accounts for depth buffering
    private void fillTriangle(Triangle3D projTri) {
        // sort the projected triangle by y values
        Triangle3D sortedProjTri = getSortedTriByY(projTri);
        Vector point1 = sortedProjTri.get(0);
        Vector point2 = sortedProjTri.get(1);
        Vector point3 = sortedProjTri.get(2);

        int rgb = sortedProjTri.getColor().getRGB();
        double avgZ = (point1.get(Z_INDEX) + point2.get(Z_INDEX) + point3.get(Z_INDEX)) / 3;

        // scanline fill the upper sub triangle
        int currY = (int) point1.get(Y_INDEX) + 1;
        int point1X = (int) point1.get(X_INDEX);
        int startRow = 0;
        int endRow = 0;

        double slopeA = (point2.get(X_INDEX) - point1.get(X_INDEX)) / (point2.get(Y_INDEX) - point1.get(Y_INDEX));
        double slopeB = (point3.get(X_INDEX) - point1.get(X_INDEX)) / (point3.get(Y_INDEX) - point1.get(Y_INDEX));

        while (currY <= point2.get(Y_INDEX)) {
            int xStart = (int) (point1X + (startRow * slopeA));
            int xEnd = (int) (point1X + (endRow * slopeB));

            for (int currX = Math.min(xStart, xEnd); currX <= Math.max(xStart, xEnd); currX++) {
                int screenIndex = currY * getWidth() + currX;

                if (avgZ < depthBuffer[screenIndex]) {
                    depthBuffer[screenIndex] = avgZ;
                    canvasRaster[screenIndex] = rgb;
                }
            }

            currY++;
            startRow++;
            endRow++;
        }

        // scanline fill the lower sub triangle
        currY = (int) point2.get(Y_INDEX) + 1;
        startRow = 0;
        slopeA = (point3.get(X_INDEX) - point2.get(X_INDEX)) / (point3.get(Y_INDEX) - point2.get(Y_INDEX));
        int point2X = (int) point2.get(X_INDEX);

        while (currY <= point3.get(Y_INDEX)) {
            int xStart = (int) (point2X + (startRow * slopeA));
            int xEnd = (int) (point1X + (endRow * slopeB));

            for (int currX = Math.min(xStart, xEnd); currX <= Math.max(xStart, xEnd); currX++) {
                int screenIndex = currY * getWidth() + currX;

                if (avgZ < depthBuffer[screenIndex]) {
                    depthBuffer[screenIndex] = avgZ;
                    canvasRaster[screenIndex] = rgb;
                }
            }

            currY++;
            startRow++;
            endRow++;
        }
    }

    // Given a triangle, return a new triangle with the vectors sorted by ascending y value
    private Triangle3D getSortedTriByY(Triangle3D triangle) {
        int[] sortedTriVecIndices = new int[] {0, 1, 2};

        for (int i = 0; i < sortedTriVecIndices.length; i++) {
            int j = i;
            int temp = sortedTriVecIndices[j];

            while (j > 0 && triangle.get(temp).get(Y_INDEX) < triangle.get(sortedTriVecIndices[j - 1]).get(Y_INDEX)) {
                sortedTriVecIndices[j] = sortedTriVecIndices[j - 1];
                sortedTriVecIndices[j - 1] = temp;
                j--;
            }
        }

        return new Triangle3D(
                triangle.get(sortedTriVecIndices[0]),
                triangle.get(sortedTriVecIndices[1]),
                triangle.get(sortedTriVecIndices[2]),
                triangle.getColor()
        );
    }

    // Update

    // Perform all actions for the engine each frame
    private void update() {
        camera.update();

        trianglesToRender = new SinglyLinkedList<>();
        cullTrianglesFromMeshes();
        projectAndScaleTriangles();
        clipTrianglesToRender();

        trianglesBeingRendered = trianglesToRender;
        repaint();
    }

    // Add transformed triangles to the list of triangles to be rendered if valid
    private void cullTrianglesFromMeshes() {
        Vector lightDirection = new Vector(1, 1, -1).normalized(); // magic
        Matrix worldMat = Matrix.identityMatrix(3);
        Vector translationVec = new Vector(0, 0, 20); // magic

        // transform world relative to camera movement
        Matrix viewMatrix = camera.getPointAtMat().getTransposed();
        Vector viewVec = camera.getWorldPos().multiplyMatrix(viewMatrix).multiplyByScalar(-1.0);

        Plane nearPlane = new Plane(new Vector(0, 0, Z_NEAR), new Vector(0, 0, -1));

        for (Mesh3D mesh : meshes) {
            for (Triangle3D tri : mesh) {
                // transform triangle in world space
                Triangle3D transformedTri = transformTriangle(tri, worldMat, translationVec);

                // apply lighting to triangle
                // cull based on if the triangle won't be seen
                Vector triSurfNorm = transformedTri.getSurfaceNormal();
                Vector triCamLine = transformedTri.get(0).subtract(camera.getWorldPos());
                double surfNormCamDiff = triSurfNorm.dotProduct(triCamLine);

                if (surfNormCamDiff < 0.0) {
                    double shadingValue = triSurfNorm.dotProduct(lightDirection);

                    if (shadingValue < 0) {
                        shadingValue = 0;
                    } else if (shadingValue > 1) {
                        shadingValue = 1;
                    }

                    transformedTri.setShading(shadingValue);
                    transformedTri = transformTriangle(transformedTri, viewMatrix, viewVec);
                    trianglesToRender.addAll(clipTriangleAgainstPlane(transformedTri, nearPlane));
                }
            }
        }
    }

    // Transform a triangle given a rotation and scaling matrix and a translation vector
    private Triangle3D transformTriangle(Triangle3D tri, Matrix rotAndScaleMat, Vector translationVec) {
        Vector[] transformedVertices = new Vector[Triangle3D.SIZE];
        for (int i = 0; i < transformedVertices.length; i++) {
            Vector currVec = tri.get(i);
            transformedVertices[i] = currVec.multiplyMatrix(rotAndScaleMat).add(translationVec);
        }

        return new Triangle3D(transformedVertices[0], transformedVertices[1], transformedVertices[2], tri.getColor());
    }

    // Convert all vertices of a triangle from world space to screen space
    private void projectAndScaleTriangles() {
        for (Triangle3D tri : trianglesToRender) { // iterate through all triangles
            for (int i = 0; i < Triangle3D.SIZE; i++) { // iterate through each vertex of the triangle
                // project the 3D coordinate to 2D
                Vector currVector = tri.get(i);
                double z = currVector.get(Z_INDEX);
                double newZ = (z - Z_NEAR) * projMat.get(2, 2);

                currVector = currVector.multiplyMatrix(projMat);
                currVector.set(Z_INDEX, newZ);
                Vector normalizedVector = currVector.divideByScalar(z);

                // scale the normalized coordinates to pixel values on the screen
                double newX = (-normalizedVector.get(X_INDEX) + 1.0) * getWidth() / 2.0;
                double newY = (-normalizedVector.get(Y_INDEX) + 1.0) * getHeight() / 2.0;

                normalizedVector.set(X_INDEX, newX);
                normalizedVector.set(Y_INDEX, newY);

                tri.set(i, normalizedVector);
            }
        }
    }

    private void clipTrianglesToRender() {
        Plane[] planes = {
                new Plane(new Vector(0, 0, 0), new Vector(0, -1, 0)), // top plane
                new Plane(new Vector(0, getHeight() - 1, 0), new Vector(0, 1, 0)), // bottom plane

                new Plane(new Vector(0, 0, 0), new Vector(-1, 0, 0)), // left plane
                new Plane(new Vector(getWidth() - 1, 0, 0), new Vector(1, 0, 0)) // right plane
        };

        SinglyLinkedList<Triangle3D> clippedTrisToRender = new SinglyLinkedList<>();

        for (Triangle3D triToRender : trianglesToRender) {
            SinglyLinkedList<Triangle3D> triQueue = new SinglyLinkedList<>();
            triQueue.add(triToRender);

            for (Plane plane : planes) {
                int queueSize = triQueue.size();

                while (queueSize > 0) {
                    Triangle3D triToClip = triQueue.removeFirst();
                    triQueue.addAll(clipTriangleAgainstPlane(triToClip, plane));
                    queueSize--;
                }
            }

            clippedTrisToRender.addAll(triQueue);
        }

        trianglesToRender = clippedTrisToRender;
    }

    // Generate a list of new clipped triangles given an original triangle and a plane to clip against
    private SinglyLinkedList<Triangle3D> clipTriangleAgainstPlane(Triangle3D tri, Plane plane) {
        SinglyLinkedList<Triangle3D> clippedTris = new SinglyLinkedList<>();
        SinglyLinkedList<Vector> insideVertices = new SinglyLinkedList<>();
        SinglyLinkedList<Vector> outsideVertices = new SinglyLinkedList<>();

        for (Vector vertex : tri) {
            double distFromPlane = plane.distanceFromPoint(vertex);

            if (distFromPlane <= 0.0) {
                insideVertices.add(vertex);
            } else {
                outsideVertices.add(vertex);
            }
        }

        if (insideVertices.size() == 1) {
            Vector insideVec = insideVertices.getFirst();
            Vector outsideVec1 = outsideVertices.getFirst();
            Vector outsideVec2 = outsideVertices.getLast();

            Vector intersectPoint1 = plane.lineIntersectPlanePoint(insideVec, outsideVec1);
            Vector intersectPoint2 = plane.lineIntersectPlanePoint(insideVec, outsideVec2);

            Vector[] vertices = new Vector[3];
            vertices[0] = insideVec;
            vertices[1] = intersectPoint1;
            vertices[2] = intersectPoint2;

            Triangle3D clippedTri = new Triangle3D(vertices[0], vertices[1], vertices[2], tri.getColor());
            clippedTris.add(clippedTri);
        } else if (insideVertices.size() == 2) {
            Vector insideVec1 = insideVertices.getFirst();
            Vector insideVec2 = insideVertices.getLast();
            Vector outsideVec = outsideVertices.getFirst();

            Vector intersectPoint1 = plane.lineIntersectPlanePoint(insideVec1, outsideVec);
            Vector intersectPoint2 = plane.lineIntersectPlanePoint(insideVec2, outsideVec);

            Vector[] vertices1 = new Vector[3];
            vertices1[0] = insideVec1;
            vertices1[1] = insideVec2;
            vertices1[2] = intersectPoint1;

            Vector[] vertices2 = new Vector[3];
            vertices2[0] = intersectPoint1;
            vertices2[1] = insideVec2;
            vertices2[2] = intersectPoint2;

            Triangle3D clippedTri1 = new Triangle3D(vertices1[0], vertices1[1], vertices1[2], tri.getColor());
            Triangle3D clippedTri2 = new Triangle3D(vertices2[0], vertices2[1], vertices2[2], tri.getColor());

            clippedTris.add(clippedTri1);
            clippedTris.add(clippedTri2);
        } else if (insideVertices.size() == 3) {
            clippedTris.add(tri);
        }

        return clippedTris;
    }

    // Generate a projection matrix based on a field of view
    private Matrix projectionMatrix(double fovInRadians) {
        Matrix newProjectionMatrix = new Matrix(3, 3);

        double aspectRatio = (double) getHeight() / getWidth();
        double fovRatio = 1.0 / Math.tan(fovInRadians / 2.0);
        double zNormalization = Z_FAR / (Z_FAR - Z_NEAR);

        newProjectionMatrix.set(0, 0, aspectRatio * fovRatio);
        newProjectionMatrix.set(1, 1, fovRatio);
        newProjectionMatrix.set(2, 2, zNormalization);

        return newProjectionMatrix;
    }

    // Setters

    /**
     * Set the frame rate to a desired value
     * @param frameRate new frame rate, must be greater than 0
     */
    public void setFrameRate(double frameRate) {
        if (frameRate <= 0.0) {
            throw new IllegalArgumentException("Frame rate must be greater than 0");
        }

        this.frameRate = frameRate;
        timer.scheduleAtFixedRate(timerTask, 0, (long) (1000 / frameRate));
    }

    // Getters

    /**
     * @return the current frame rate
     */
    public double getFrameRate() {
        return frameRate;
    }

}

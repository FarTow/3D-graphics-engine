package engine;

import datakit.SinglyLinkedList;
import graphicstructs.Mesh3D;
import graphicstructs.Triangle3D;
import mathkit.Matrix;
import mathkit.Vector;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * JPanel to calculate and render 3D Graphics
 */
public class Engine extends JPanel {
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

    /**
     * Highest value visible in the z-plane of the 3D environment
     */
    private final double Z_FAR = 1000.0;

    /**
     * Lowest value visible in the z-plane of the 3D environment
     */
    private final double Z_NEAR = 0.1;

    /**
     * Amount of frames to run per second
     */
    private double frameRate;

    /**
     * Engine loop thread
     */
    private Timer timer;

    /**
     * Engine loop task performer
     */
    private TimerTask timerTask;

    /**
     * Matrix to convert a 3D coordinate into a 2D point on the screen
     */
    private Matrix projMat;

    /**
     * List of all triangles that should be rendered onto screen
     */
    private final SinglyLinkedList<Triangle3D> trianglesToRender;

    /**
     * List of all meshes that exist
     */
    private final ArrayList<Mesh3D> meshes;

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

        trianglesToRender = new SinglyLinkedList<>();
        meshes = new ArrayList<>();

        initMeshes();
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);

        // keyboard communications
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { // exit application
                    System.exit(0);
                }
            }
        });
    }

    // Initialization

    // Create all desired meshes here
    private void initMeshes() {
        meshes.add(Mesh3D.cube(0, 0, 0,1.0));
    }

    /**
     * Start the engine by starting the timer to run at a set interval
     */
    public void start() {
        projMat = projectionMatrix(Math.PI / 2);
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
        super.paintComponent(g);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // draw all triangles (triangles are already in projected form)
        for (Triangle3D tri : trianglesToRender) {
            fillTriangle(g, tri);
        }
    }

    // Draw a projected triangle (a 3D triangle that has its coordinates converted into 2D)

    private void drawTriangle(Graphics g, Triangle3D projTri) {
        Vector point1 = projTri.get(0);
        Vector point2 = projTri.get(1);
        Vector point3 = projTri.get(2);

        int x1 = (int) point1.get(X_INDEX);
        int y1 = (int) point1.get(Y_INDEX);

        int x2 = (int) point2.get(X_INDEX);
        int y2 = (int) point2.get(Y_INDEX);

        int x3 = (int) point3.get(X_INDEX);
        int y3 = (int) point3.get(Y_INDEX);

        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x2, y2, x3, y3);
        g.drawLine(x3, y3, x1, y1);
    }

    // Fill a projected triangle (a 3D triangle that has its coordinates converted into 2D)
    private void fillTriangle(Graphics g, Triangle3D projTri) {
        g.setColor(projTri.getColor());
        drawTriangle(g, projTri);

        Vector point1 = projTri.get(0);
        Vector point2 = projTri.get(1);
        Vector point3 = projTri.get(2);

        int[] xPoints = new int[] {(int) point1.get(X_INDEX), (int) point2.get(X_INDEX), (int) point3.get(X_INDEX)};
        int[] yPoints = new int[] {(int) point1.get(Y_INDEX), (int) point2.get(Y_INDEX), (int) point3.get(Y_INDEX)};

        g.fillPolygon(xPoints, yPoints, 3);
    }

    // Update

    // Perform all actions for the engine each frame
    private void update() {
        trianglesToRender.clear();

        cullTrianglesFromMeshes();
        projectAndScaleTriangles();
        repaint();
    }

    // Add copies of the triangles from our meshes to the triangles to be rendered
    private void cullTrianglesFromMeshes() {
        double rotationAngle = Math.toRadians(System.currentTimeMillis() / frameRate);

        Vector camera = new Vector(0, 0, 0);
        Vector lightDirection = new Vector(0, 0, -1).normalized();
        Matrix worldMatrix = zRotationMatrix(rotationAngle).multiplyMatrix(xRotationMatrix(rotationAngle / 2));
        Vector translationVector = new Vector(0, 0, 3);

        for (Mesh3D mesh : meshes) {
            for (Triangle3D tri : mesh) {
                // transform the mesh's triangle
                Vector[] transformedVertices = new Vector[Triangle3D.SIZE];
                for (int i = 0; i < transformedVertices.length; i++) {
                    transformedVertices[i] = tri.get(i).multiplyMatrix(worldMatrix).add(translationVector);
                }

                Triangle3D transformedTri = new Triangle3D(
                        transformedVertices[0],
                        transformedVertices[1],
                        transformedVertices[2],
                        tri.getColor()
                );

                // cull the triangle
                Vector triSurfNorm = transformedTri.getSurfaceNormal();
                Vector triCamLine = transformedTri.get(0).subtract(camera);
                double surfNormCamDiff = triSurfNorm.dotProduct(triCamLine);

                if (surfNormCamDiff < 0.0) {
                    double shadingValue = triSurfNorm.dotProduct(lightDirection);
                    transformedTri.setShading(shadingValue);
                    trianglesToRender.add(transformedTri);
                }
            }
        }
    }

    // Convert all 3D points into 2D points to be rendered onto the screen
    private void projectAndScaleTriangles() {
        for (Triangle3D tri : trianglesToRender) {
            for (int i = 0; i < Triangle3D.SIZE; i++) {
                // project the 3D coordinate to 2D
                // normalized x and y are now between -1 and 1
                Vector currVector = tri.get(i);
                double z = currVector.get(Z_INDEX);
                double newZ = (z - Z_NEAR) * projMat.get(2, 2);

                currVector = currVector.multiplyMatrix(projMat);
                currVector.set(Z_INDEX, newZ);
                Vector normalizedVector = currVector.divideByScalar(z);

                // scale the normalized coordinates to pixel values on the screen
                double newX = (normalizedVector.get(X_INDEX) + 1.0) * getWidth() / 2.0;
                double newY = (normalizedVector.get(Y_INDEX) + 1.0) * getHeight() / 2.0;

                normalizedVector.set(X_INDEX, newX);
                normalizedVector.set(Y_INDEX, newY);

                tri.set(i, normalizedVector);
            }
        }
    }

    // Calculations

    // Generate a matrix to rotate the x coordinate of a 3D point
    private Matrix xRotationMatrix(double angleRadians) {
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

    // Generate a matrix to rotate the y coordinate of a 3D point
    private Matrix yRotationMatrix(double angleRadians) {
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

    // Generate a matrix to rotate the z coordinate of a 3D point
    private Matrix zRotationMatrix(double angleRadians) {
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

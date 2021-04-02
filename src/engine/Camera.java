package engine;

import graphicstructs.Math3D;
import mathkit.Matrix;
import mathkit.Vector;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Camera implements KeyListener {
    // Position

    /**
     * Position in the 3D environment
     */
    private Vector worldPos;

    private Matrix rotMat;

     // Speed

    private double moveSpeed;

    private double strafeSpeed;

    private double flySpeed;

    /**
     * The speed at which the camera rotates in radians
     */
    private double rotSpeed;

    // Angles

    /**
     * Rotation angle around the y-axis
     */
    private double yaw;

    /**
     * Rotation angle around the x-axis
     */
    private double pitch;

    /**
     * Rotation angle around the z-axis
     */
    private double roll;

    // Movement

    private boolean moveLeft;

    private boolean moveRight;

    private boolean moveUp;

    private boolean moveDown;

    private boolean moveForward;

    private boolean moveBackward;

    // Panning

    private boolean panLeft;

    private boolean panRight;

    private boolean panUp;

    private boolean panDown;

    private boolean tiltLeft;

    private boolean tiltRight;

    // Constructors

    public Camera(double moveSpeed, double strafeSpeed, double flySpeed, double rotSpeed) {
        this.moveSpeed = moveSpeed;
        this.strafeSpeed = strafeSpeed;
        this.flySpeed = flySpeed;
        this.rotSpeed = rotSpeed;

        worldPos = new Vector(3);

        rotMat = Matrix.identityMatrix(3);
    }

    public void update() {
        if (panLeft || panRight || panUp || panDown || tiltLeft || tiltRight) {
            rotate();
            rotMat = Math3D.rotationMatrix(yaw, pitch, roll);
        }


        move();
    }

    private void rotate() {
        if (panLeft) {
            yaw -= rotSpeed;
        }

        if (panRight) {
            yaw += rotSpeed;
        }

        if (panUp) {
            pitch -= rotSpeed;
        }

        if (panDown) {
            pitch += rotSpeed;
        }

        if (tiltLeft) {
            roll -= rotSpeed;
        }

        if (tiltRight) {
            roll += rotSpeed;
        }
    }

    private void move() {
        if (moveForward || moveBackward) {
            Vector zTranspose = Math3D.forwardRotated(rotMat).multiplyByScalar(moveSpeed);

            if (moveForward) {
                worldPos = worldPos.add(zTranspose);
            }

            if (moveBackward) {
                worldPos = worldPos.subtract(zTranspose);
            }
        }

        if (moveUp || moveDown) {
            Vector yTranspose = Math3D.upRotated(rotMat).multiplyByScalar(flySpeed);

            if (moveUp) {
                worldPos = worldPos.add(yTranspose);
            }

            if (moveDown) {
                worldPos = worldPos.subtract(yTranspose);
            }
        }

        if (moveLeft || moveRight) {
            Vector xTranspose = Math3D.rightRotated(rotMat).multiplyByScalar(strafeSpeed);

            if (moveLeft) {
                worldPos = worldPos.add(xTranspose);
            }

            if (moveRight) {
                worldPos = worldPos.subtract(xTranspose);
            }
        }
    }

    // Getters

    /**
     * @return the vector position of the camera in the 3D environment
     */
    public Vector getWorldPos() {
        return worldPos;
    }

    /**
     * @return the rotation matrix of the camera
     */
    public Matrix getRotMat() {
        return rotMat;
    }

    /**
     * @return a matrix representing where the camera is pointing towards in world space
     */
    public Matrix getPointAtMat() {
        Vector right = Math3D.rightRotated(rotMat);
        Vector up = Math3D.upRotated(rotMat);
        Vector forward = right.crossProduct(up);

        Matrix pointAtMat = new Matrix(right.size(), right.size());
        for (int i = 0; i < right.size(); i++) {
            pointAtMat.set(0, i, right.get(i));
            pointAtMat.set(1, i, up.get(i));
            pointAtMat.set(2, i, forward.get(i));
        }

        return pointAtMat;
    }

    // Overrides

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // movement
        if (code == KeyEvent.VK_A) {
            moveLeft = true;
        }

        if (code == KeyEvent.VK_D) {
            moveRight = true;
        }

        if (code == KeyEvent.VK_W) {
            moveForward = true;
        }

        if (code == KeyEvent.VK_S) {
            moveBackward = true;
        }

        if (code == KeyEvent.VK_SPACE) {
            moveUp = true;
        }

        if (code == KeyEvent.VK_CONTROL) {
            moveDown = true;
        }

        // rotation
        if (code == KeyEvent.VK_LEFT) {
            panLeft = true;
        }

        if (code == KeyEvent.VK_RIGHT) {
            panRight = true;
        }

        if (code == KeyEvent.VK_UP) {
            panUp = true;
        }

        if (code == KeyEvent.VK_DOWN) {
            panDown = true;
        }

        if (code == KeyEvent.VK_Q) {
            tiltLeft = true;
        }

        if (code == KeyEvent.VK_E) {
            tiltRight = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        // movement
        if (code == KeyEvent.VK_A) {
            moveLeft = false;
        }

        if (code == KeyEvent.VK_D) {
            moveRight = false;
        }

        if (code == KeyEvent.VK_W) {
            moveForward = false;
        }

        if (code == KeyEvent.VK_S) {
            moveBackward = false;
        }

        if (code == KeyEvent.VK_SPACE) {
            moveUp = false;
        }

        if (code == KeyEvent.VK_CONTROL) {
            moveDown = false;
        }

        // rotation
        if (code == KeyEvent.VK_LEFT) {
            panLeft = false;
        }

        if (code == KeyEvent.VK_RIGHT) {
            panRight = false;
        }

        if (code == KeyEvent.VK_UP) {
            panUp = false;
        }

        if (code == KeyEvent.VK_DOWN) {
            panDown = false;
        }

        if (code == KeyEvent.VK_Q) {
            tiltLeft = false;
        }

        if (code == KeyEvent.VK_E) {
            tiltRight = false;
        }
    }
}

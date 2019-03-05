package viewer3D.GraphicsEngine;

import java.awt.Point;
import viewer3D.Math.Matrix;
import viewer3D.Math.Plane;
import viewer3D.Math.Vector;

/**
 * Responsible for observing a given set of polygons and projecting them into a 
 * screen coordinate space usable by drawer classes
 * @author Arik Dicks
 */
public class Camera {
    private Plane projectionPlane;
    private Polygon[] polygons;
    private Polygon[] polygons2D;
    private Vector[][] projectionPoints;
    private Vector[][] projectedPolygons;
    private final Vector projectionPlanePosition;
    private final Vector originVector;
    private final Vector normalVector;
    private Vector cameraPositionVector;
    private Vector cameraRotationVector;
    private Vector cameraPlaneNormalVector;
    private final Vector xUnitVector;
    private final Vector yUnitVector;
    private final Vector zUnitVector;
    private int defaultRotationAngle;
    private int pitchAngle;
    private int yawAngle;
    private int rollAngle;
    private int theta;
    private int phi;
    private double translationScalar;    
    private double xFOV;
    private double yFOV;
    private int width;
    private int height;
    private Matrix rollMatrix;
    private Matrix pitchMatrix;
    private Matrix yawMatrix;
    /**
     * Camera is constructed with a speed of 10 and an initial position of {0, 150, -450}
     * @param polygons
     */
    public Camera(Polygon[] polygons) {
        this.polygons = polygons;
        
        // Rotation fields
        defaultRotationAngle = 1;
        yawAngle = 0;
        pitchAngle = 0;
        rollAngle = 0;
        theta = 0;
        phi = 0;
        
        rollMatrix = Matrix.get3DZRotationMatrix(Math.toRadians(rollAngle));
        pitchMatrix = Matrix.get3DXRotationMatrix(Math.toRadians(pitchAngle));
        yawMatrix = Matrix.get3DYRotationMatrix(Math.toRadians(yawAngle));
         
        
        // Speed
        translationScalar = 1;

        // Starting position
        int initialCameraPositionX = 0;
        int initialCameraPositionY = 0;
        int initialCameraPositionZ = 0;
        
        double[] originVectorArray = {initialCameraPositionX, initialCameraPositionY, initialCameraPositionZ};
        double[] projectionPlanePositionArray = {initialCameraPositionX, initialCameraPositionY, initialCameraPositionZ+1};
        double[] normalVectorArray = {0, 0, 1};
        double[] cameraPositionVectorArray = {0, 0, 0};
        double[] cameraRotationVectorArray = {0, 0, 1};
        double[] cameraPlaneNormalVectorArray = {0, 1, 0};
        double[] xUnitVectorArray = {1, 0, 0};
        double[] yUnitVectorArray = {0, 1, 0};
        double[] zUnitVectorArray = {0, 0, 1};
        
        cameraPositionVector = new Vector(cameraPositionVectorArray);
        cameraRotationVector = new Vector(cameraRotationVectorArray);
        cameraPlaneNormalVector = (new Vector(cameraPlaneNormalVectorArray));
        
        originVector = new Vector(originVectorArray);
        projectionPlanePosition = new Vector(projectionPlanePositionArray);
        normalVector = (new Vector(normalVectorArray));
        
        xUnitVector = new Vector(xUnitVectorArray);
        yUnitVector = new Vector(yUnitVectorArray);
        zUnitVector = new Vector(zUnitVectorArray);

        projectionPlane = new Plane(projectionPlanePosition, normalVector);
    }

    /**
     * Projects the polygons provided at construction into a screenspace co-ordinate space
     * usable by drawer classes
     * @return
     */
    public Polygon[] observe() {
        //projectionPlane = new Plane(normalVector, normalVector);
        projectPolygons();
        mapPolygonsToScreenSpace();
        // Create Buffered Image and return
        return polygons2D;
    }
    
    private void projectPolygons() {
        // Project polygons onto camera plane
        projectedPolygons = new Vector[polygons.length][3];
        for (int i = 0; i < polygons.length; i++) {
            Vector[] vertices = {polygons[i].getVertex(0), polygons[i].getVertex(1), polygons[i].getVertex(2)};
            Vector[] projectedVertices = new Vector[vertices.length];
            for (int j = 0; j < projectedVertices.length; j++) {
                // rotate and translate the point before getting its intersection
                Vector fromCameraVector =  vertices[j].subtract(cameraPositionVector);
                Vector newPosition = (fromCameraVector)
                        .multiply(yawMatrix)
                        .multiply(pitchMatrix);
                projectedVertices[j] = projectionPlane.getIntersectingVector(newPosition);
            }
            projectedPolygons[i] = projectedVertices;
        }
    }
    
    private void mapPolygonsToScreenSpace() {
        Vector[][] projectionVertices2D = new Vector[polygons.length][3];
        // Map camera plane to screen space
        double[] vectorArray = {1, 1, 0};
        Vector vector = new Vector(vectorArray);

        polygons2D = new ProjectedPolygon[polygons.length];
        for (int i = 0; i < projectedPolygons.length; i++) {
            for (int j = 0; j < projectedPolygons[0].length; j++) {
                projectionVertices2D[i][j] = (projectedPolygons[i][j]).add(vector).multiply(0.5);
            }
            polygons2D[i] = new ProjectedPolygon(projectionVertices2D[i], polygons[i]);
        }
    }
    public boolean isInTriangle(Polygon polygon, Vector pointVector) {
        Vector[] vertices = polygon.getVertices();
        double x = pointVector.getComponent(0);
        double y = pointVector.getComponent(1);
        double x1 = vertices[0].getComponent(0);
        double y1 = vertices[0].getComponent(1);
        double x2 = vertices[1].getComponent(0);
        double y2 = vertices[1].getComponent(1);
        double x3 = vertices[2].getComponent(0);
        double y3 = vertices[2].getComponent(1);
        
        double denominator = ((y2 - y3)*(x1 - x3) + (x3 - x2)*(y1 - y3));
        double a = ((y2 - y3)*(x - x3) + (x3 - x2)*(y - y3))/denominator;
        double b = ((y3 - y1)*(x - x3) + (x1 - x3)*(y - y3))/denominator;
        double c = 1 - a - b;
        return 0 <= a && a <= 1 && 0 <= b && b <= 1 && 0 <= c && c <= 1;
    }
    private void setProjectionPoints() {
        projectionPlane = new Plane(projectionPlanePosition, normalVector);
        Vector[] projectionVertices = new Vector[4];
        /* 
        1. Get the vectors whos direction from the observer, point at the corners of the projection rectangle
        2. Give those vectors to the projection plane to get the set of vectors corresponding to the screens pixels
        */ 
        Matrix xNegativeRotationMatrix = Matrix.get3DXRotationMatrix(-1*xFOV/2);
        Matrix xPositiveRotationMatrix = Matrix.get3DXRotationMatrix(xFOV/2);
        Matrix zNegativeRotationMatrix = Matrix.get3DZRotationMatrix(-1*yFOV/2);
        Matrix zPositiveRotationMatrix = Matrix.get3DZRotationMatrix(yFOV/2);
        Vector vertexDirection1 = originVector.add(normalVector.multiply(xNegativeRotationMatrix).multiply(zPositiveRotationMatrix));
        Vector vertexDirection2 = originVector.add(normalVector.multiply(xNegativeRotationMatrix).multiply(zNegativeRotationMatrix));
        Vector vertexDirection3 = originVector.add(normalVector.multiply(xPositiveRotationMatrix).multiply(zNegativeRotationMatrix));
        Vector vertexDirection4 = originVector.add(normalVector.multiply(xPositiveRotationMatrix).multiply(zPositiveRotationMatrix));
        projectionVertices[0] = projectionPlane.getIntersectingVector(originVector, vertexDirection1);
        projectionVertices[1] = projectionPlane.getIntersectingVector(originVector, vertexDirection2);
        projectionVertices[2] = projectionPlane.getIntersectingVector(originVector, vertexDirection3);
        projectionVertices[3] = projectionPlane.getIntersectingVector(originVector, vertexDirection4);
        projectionPoints = projectionPlane.getGridOfVectors(height, width, projectionVertices);
    }
    /**
     * Moves the camera in the given direction by an amount adjusted by the translationScalar (speed)
     * @param direction The direction in which the camera is to move
     */
    public void move(Direction direction) {
        double x = cameraPositionVector.getComponent(0);
        double y = cameraPositionVector.getComponent(1);
        double z = cameraPositionVector.getComponent(2);

        double xr = cameraRotationVector.getComponent(0)*translationScalar;
        double yr = cameraRotationVector.getComponent(1)*translationScalar;
        double zr = cameraRotationVector.getComponent(2)*translationScalar;
        switch(direction) {
            case FORWARD:
                cameraPositionVector = new Vector(new double[]{x + -1*xr, y + yr, z + zr});
                //cameraPositionVector = cameraPositionVector.add(cameraRotationVector.multiply(-translationScalar));
                break;
            case BACKWARD:
                //cameraPositionVector = new Vector(new double[]{x + xr, y + -1*yr, z + -zr});
                cameraPositionVector = cameraPositionVector.add(cameraRotationVector.multiply(translationScalar));
                break;
            case LEFT:
                //cameraPositionVector = new Vector(new double[]{x + -zr, y + -1*yr, z + -xr});
                Vector translationLeftVector = cameraRotationVector.cross(cameraPlaneNormalVector);
                cameraPositionVector = cameraPositionVector.add(translationLeftVector.multiply(translationScalar));
                break;
            case RIGHT:
                //cameraPositionVector = new Vector(new double[]{x + zr, y + -1*yr, z + xr});
                Vector translationRightVector = cameraPlaneNormalVector.cross(cameraRotationVector);
                cameraPositionVector = cameraPositionVector.add(translationRightVector.multiply(translationScalar));
                break;
            case UP:
                cameraPositionVector = cameraPositionVector.add(cameraPlaneNormalVector.multiply(translationScalar));
                break;
            case DOWN:
                cameraPositionVector = cameraPositionVector.add(cameraPlaneNormalVector.multiply(-1*translationScalar));
                break;
        }
    }

/**
     * Rotates the camera one degree by the given rotation type
     * @param rotation The type of rotation
     * @param rotationDelta
     */
    public void rotate(Rotation rotation, int rotationDelta) {
        // Adjust rotation angles
        switch(rotation) {
            case YAW_LEFT:
                yawAngle = (yawAngle + defaultRotationAngle*rotationDelta)%360;
                break;
            case YAW_RIGHT:
                yawAngle = (yawAngle + -1*defaultRotationAngle*rotationDelta)%360;
                break;
            case PITCH_FORWARD:
                pitchAngle = (pitchAngle + defaultRotationAngle*rotationDelta)%360;
                break;
            case PITCH_BACKWARD:
                pitchAngle = (pitchAngle + -1*defaultRotationAngle*rotationDelta)%360;
                break;
            case ROLL_COUNTER_CLOCKWISE:
                rollAngle = (rollAngle + -1*defaultRotationAngle*rotationDelta)%360;
                break;
            case ROLL_CLOCKWISE:
                rollAngle = (rollAngle + defaultRotationAngle*rotationDelta)%360;
                break;
        }
        yawAngle = (yawAngle < 0)? 360+yawAngle : yawAngle;
        pitchAngle = (pitchAngle < 0)? 360+pitchAngle : pitchAngle;
        rollAngle = (rollAngle < 0)? 360+rollAngle : rollAngle;

        // Apply rotations to camera direction
        rollMatrix = Matrix.get3DZRotationMatrix(Math.toRadians(rollAngle));
        pitchMatrix = Matrix.get3DXRotationMatrix(Math.toRadians(pitchAngle));
        yawMatrix = Matrix.get3DYRotationMatrix(Math.toRadians(yawAngle));
        cameraRotationVector = ((zUnitVector.multiply(yawMatrix).multiply(pitchMatrix).getUnitVector()));
    }
    public void rotate(int deltaTheta, int deltaPhi) {
        // Adjust rotation angles
        theta = (theta+deltaTheta)%360;
        theta = (theta < 0)? 360+theta : theta;
        
        phi = (phi+deltaPhi)%360;
        phi = (phi < 0)? 360+phi : phi;

        // Apply rotations to camera direction
        pitchMatrix = Matrix.get3DXRotationMatrix(Math.toRadians(phi));
        yawMatrix = Matrix.get3DYRotationMatrix(Math.toRadians(theta));
        cameraRotationVector = (((zUnitVector.multiply(yawMatrix)).multiply(pitchMatrix)).getUnitVector());
    }
    
    
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////    
////////////////////////////////////////////////////////////////////////////////
    /**
     * Gives parameter states of this camera as an array of Strings, including: 
     * Camera Position, Camera Direction, Projection Plane Position, Projection Plane Direction and Projection Vector Length
     * @return An array of strings of parameter states of this camera
     */
    public String[] getData() {
        String[] data = {
                        "Camera Position: " + cameraPositionVector.copy(),
                        "Camera Direction: " + cameraRotationVector.copy(),
//                        "Projection Position: " + (cameraPositionVector.add(cameraRotationVector)).copy(),
//                        "Projection Direction: " + ((cameraPositionVector.add(cameraRotationVector)).subtract(cameraPositionVector)).copy(),
//                        "Projection Length: " + ((cameraPositionVector.add(cameraRotationVector)).subtract(cameraPositionVector)).getLength(),
                        "Yaw: " + theta + "°",
                        "Pitch: " + phi + "°"
//                        "Yaw Angle: " + yawAngle,
//                        "Pitch Angle: " + pitchAngle,
//                        "Roll Angle: " + rollAngle 
                };
        return data;
    }

    /**
     * Returns the current speed of the camera
     * @return the current speed of the camera
     */
    public double getSpeed() {
        return translationScalar;
    }

    /**
     * Returns the current x position of the camera
     * @return the current x position of the camera
     */
    public double getXPosition() {
        return cameraPositionVector.getComponent(0);
    }

    /**
     * Returns the current y position of the camera
     * @return the current y position of the camera
     */
    public double getYPosition() {
        return cameraPositionVector.getComponent(1);
    }

    /**
     * Returns the current z position of the camera
     * @return the current z position of the camera
     */
    public double getZPosition() {
        return cameraPositionVector.getComponent(2);
    }

    /**
     * Returns the current x direction of the camera
     * @return the current x direction of the camera
     */
    public double getXDirection() {
        return cameraRotationVector.getComponent(0);
    }

    /**
     * Returns the current y direction of the camera
     * @return the current y direction of the camera
     */
    public double getYDirection() {
        return cameraRotationVector.getComponent(1);
    }

    /**
     * Returns the current z direction of the camera
     * @return the current z direction of the camera
     */
    public double getZDirection() {
        return cameraRotationVector.getComponent(2);
    }

    /**
     * Returns the current speed of the camera
     * @param speed the current speed of the camera
     */
    public void setSpeed(double speed) {
        translationScalar = speed;
    }

    /**
     * Sets the position of the camera to be at the given coordinates
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    public void setPosition(double x, double y, double z) {
        cameraPositionVector = new Vector(new double[]{x, y, z});
    }

    /**
     * Sets the direction of the camera to be along the given vector coordinates
     * @param x The x direction
     * @param y The y direction
     * @param z The z direction
     */
    public void setDirection(double x, double y, double z) {
        cameraRotationVector = (new Vector(new double[]{x, y, z})).getUnitVector();
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < projectionPoints.length; i++) {
            for (int j = 0; j < projectionPoints[0].length; j++) {
                str += (projectionPoints[i][j] + " ");
            }
            str += "\n";
        }
        return str;
    }
    private void printVectorArray2D(Vector[][] vectorArray2D, String message) {
        System.out.println(message);
        for (int i = 0; i < vectorArray2D.length; i++) {
            for (int j = 0; j < vectorArray2D[0].length; j++) {
                System.out.print(vectorArray2D[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
    private void printVectorArray(Vector[] vectorArray, String message) {
        System.out.println(message);
        for (int i = 0; i < vectorArray.length; i++) {
            System.out.print(vectorArray[i] + " ");
        }
        System.out.println();
    }
    private void printPolygons(Polygon[] polygons, String message) {
        System.out.println(message);
        for (int i = 0; i < polygons.length; i++) {
            System.out.print(polygons[i].getVertex(0) + " ");
            System.out.print(polygons[i].getVertex(1) + " ");
            System.out.print(polygons[i].getVertex(2));
            System.out.println();
        }
        System.out.println();
    }
}
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
    private Vector[][] projectionPoints;
    private Vector[][] projectedPolygons;
    private Vector projectionPlanePosition;
    private Vector cameraPlaneNormalVector;
    private Vector observerVector;
    private Vector normalVector;
    private Point[] screenSpacePixelPoints;
    private final Vector zUnitVector;
    private double translationScalar;
    private double rotationScalar;
    private double rotationAngle;
    private double xFOV;
    private double yFOV;
    private int width;
    private int height;

    /**
     * Camera is constructed with a speed of 10 and an initial position of {0, 150, -450}
     * @param polygons
     */
    public Camera(Polygon[] polygons) {
        this.polygons = polygons;
        translationScalar = 10;
        rotationScalar = 0.0001;
        rotationAngle = Math.PI/180;
        xFOV = Math.PI/2;
        yFOV = Math.PI/2;
        int initialCameraPositionX = 0;
        int initialCameraPositionY = 150;
        int initialCameraPositionZ = -450;
        
        double[] observerVectorArray = {initialCameraPositionX, initialCameraPositionY, initialCameraPositionZ};
        double[] projectionPlanePositionArray = {initialCameraPositionX, initialCameraPositionY, initialCameraPositionZ+1};
        double[] normalVectorArray = {0, 0, 1};
        double[] cameraPlaneNormalVectorArray = {0, 1, 0};
        double[] zUnitVectorArray = {0, 0, 1};
        
        observerVector = new Vector(observerVectorArray);
        projectionPlanePosition = new Vector(projectionPlanePositionArray);
        normalVector = (new Vector(normalVectorArray)).getUnitVector();
        cameraPlaneNormalVector = (new Vector(cameraPlaneNormalVectorArray).getUnitVector());
        zUnitVector = new Vector(zUnitVectorArray);

        projectionPlane = new Plane(projectionPlanePosition, normalVector);
    }

    /**
     * Projects the polygons provided at construction into a screenspace co-ordinate space
     * usable by drawer classes
     * @return
     */
    public Polygon[] observe() {
        // Project polygons onto camera plane
        projectedPolygons = new Vector[polygons.length][3];
        for (int i = 0; i < polygons.length; i++) {
            if (calcPolygonIsVisible(polygons[i])) {
                //System.out.println("test");
                polygons[i].setIsVisible(true);
            }
            Vector[] vertices = {polygons[i].getVertex(0), polygons[i].getVertex(1), polygons[i].getVertex(2)};
            Vector[] projectedVertices = new Vector[vertices.length];
            for (int j = 0; j < vertices.length; j++) {
                projectedVertices[j] = projectionPlane.getIntersectingVector(observerVector, vertices[j].subtract(observerVector));
            }
            projectedPolygons[i] = projectedVertices;
        }
        Vector[][] projectionVertices2D = new Vector[polygons.length][3];
//------------------------------------------------------------------------------
        // Map camera plane to screen space
        Vector axisOfRotation = normalVector.cross(zUnitVector);
        double angleOfRotation = Math.acos(normalVector.dot(zUnitVector));
        double[] subtractionVectorArray = {1, 1, 0};
        Vector subtractionVector = new Vector(subtractionVectorArray);

        Polygon[] polygons2D = new ProjectedPolygon[polygons.length];
        for (int i = 0; i < projectedPolygons.length; i++) {
            for (int j = 0; j < projectedPolygons[0].length; j++) {
                projectionVertices2D[i][j] = (projectedPolygons[i][j].rotate(axisOfRotation, angleOfRotation)
                        .subtract(projectionPlanePosition.subtract(subtractionVector))).multiply(0.5);
            }
            polygons2D[i] = new ProjectedPolygon(projectionVertices2D[i], polygons[i]);
        }
        return polygons2D;
    }

    /**
     * Moves the camera in the given direction by an amount adjusted by the translationScalar (speed)
     * @param direction The direction in which the camera is to move
     */
    public void move(Direction direction) {
        switch(direction) {
            case FORWARD:
                observerVector = observerVector.add(normalVector.multiply(translationScalar));
                projectionPlanePosition = projectionPlanePosition.add(normalVector.multiply(translationScalar));
                break;
            case LEFT:
                Vector translationLeftVector = normalVector.cross(cameraPlaneNormalVector);
                observerVector = observerVector.add(translationLeftVector.multiply(translationScalar));
                projectionPlanePosition = projectionPlanePosition.add(translationLeftVector.multiply(translationScalar));
                break;
            case BACKWARD:
                observerVector = observerVector.add(normalVector.multiply(-1*translationScalar));
                projectionPlanePosition = projectionPlanePosition.add(normalVector.multiply(-1*translationScalar));
                break;
            case RIGHT:
                Vector translationRightVector = cameraPlaneNormalVector.cross(normalVector);
                observerVector = observerVector.add(translationRightVector.multiply(translationScalar));
                projectionPlanePosition = projectionPlanePosition.add(translationRightVector.multiply(translationScalar));
                break;
            case UP:
                observerVector = observerVector.add(cameraPlaneNormalVector.multiply(translationScalar));
                projectionPlanePosition = projectionPlanePosition.add(cameraPlaneNormalVector.multiply(translationScalar));
                break;
            case DOWN:
                observerVector = observerVector.add(cameraPlaneNormalVector.multiply(-1*translationScalar));
                projectionPlanePosition = projectionPlanePosition.add(cameraPlaneNormalVector.multiply(-1*translationScalar));
                break;
        }
        projectionPlane = new Plane(projectionPlanePosition, normalVector);
    }

    /**
     * Rotates the camera around the camera normal in the given direction
     * @param direction The direction in which the camera is to rotate
     */
    public void rotate(Direction direction) {
        double angle = 0;
        switch(direction) {
            case LEFT:
                angle = -1*rotationAngle;
                break;
            case RIGHT:
                angle = rotationAngle;
                break;
        }
        Matrix rotationMatrix = Matrix.get3DYRotationMatrix(angle);
        normalVector = normalVector.multiply(rotationMatrix).getUnitVector();
        projectionPlanePosition = observerVector.add(normalVector);
        projectionPlane = new Plane(projectionPlanePosition, normalVector);
    }

    public boolean calcPolygonIsVisible(Polygon polygon) {
        Vector polygonNormal = polygon.getNormal();
//        System.out.println("normal: " +polygonNormal);
//        System.out.println("dot pr: " +polygonNormal.dot(normalVector));
//        System.out.println("length: " +polygonNormal.getLength());
        if (polygonNormal.dot(normalVector) > 0)
            return false;
        return true;
    }
        
    /**
     * Gives parameter states of this camera as an array of Strings, including: 
     * Camera Position, Camera Direction, Projection Plane Position, Projection Plane Direction and Projection Vector Length
     * @return An array of strings of parameter states of this camera
     */
    public String[] getData() {
        String[] data = {
                        "Camera Position: " + observerVector.copy(),
                        "Camera Direction: " + normalVector.copy(),
                        "Projection Position: " + projectionPlanePosition.copy(),
                        "Projection Direction: " + (projectionPlanePosition.subtract(observerVector)).copy(),
                        "Projection Length: " + (projectionPlanePosition.subtract(observerVector)).getLength()
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
        return observerVector.getComponent(0);
    }

    /**
     * Returns the current y position of the camera
     * @return the current y position of the camera
     */
    public double getYPosition() {
        return observerVector.getComponent(1);
    }

    /**
     * Returns the current z position of the camera
     * @return the current z position of the camera
     */
    public double getZPosition() {
        return observerVector.getComponent(2);
    }

    /**
     * Returns the current x direction of the camera
     * @return the current x direction of the camera
     */
    public double getXDirection() {
        return normalVector.getComponent(0);
    }

    /**
     * Returns the current y direction of the camera
     * @return the current y direction of the camera
     */
    public double getYDirection() {
        return normalVector.getComponent(1);
    }

    /**
     * Returns the current z direction of the camera
     * @return the current z direction of the camera
     */
    public double getZDirection() {
        return normalVector.getComponent(2);
    }

    /**
     * Returns the current speed of the camera
     * @param speed the current speed of the camera
     */
    public void setSpeed(int speed) {
        translationScalar = speed;
    }

    /**
     * Sets the position of the camera to be at the given coordinates
     * @param x The x position
     * @param y The y position
     * @param z The z position
     */
    public void setPosition(double x, double y, double z) {
        double[] newPositionArray = {x, y, z};
        observerVector = new Vector(newPositionArray);
        projectionPlanePosition = observerVector.add(normalVector);
    }

    /**
     * Sets the direction of the camera to be along the given vector coordinates
     * @param x The x direction
     * @param y The y direction
     * @param z The z direction
     */
    public void setDirection(double x, double y, double z) {
        double[] newDirectionArray = {x, y, z};
        normalVector = (new Vector(newDirectionArray)).getUnitVector();
        projectionPlanePosition = observerVector.add(normalVector);
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public void setHeight(int height) {
        this.height = height;
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
        Vector vertexDirection1 = observerVector.add(normalVector.multiply(xNegativeRotationMatrix).multiply(zPositiveRotationMatrix));
        Vector vertexDirection2 = observerVector.add(normalVector.multiply(xNegativeRotationMatrix).multiply(zNegativeRotationMatrix));
        Vector vertexDirection3 = observerVector.add(normalVector.multiply(xPositiveRotationMatrix).multiply(zNegativeRotationMatrix));
        Vector vertexDirection4 = observerVector.add(normalVector.multiply(xPositiveRotationMatrix).multiply(zPositiveRotationMatrix));
        projectionVertices[0] = projectionPlane.getIntersectingVector(observerVector, vertexDirection1);
        projectionVertices[1] = projectionPlane.getIntersectingVector(observerVector, vertexDirection2);
        projectionVertices[2] = projectionPlane.getIntersectingVector(observerVector, vertexDirection3);
        projectionVertices[3] = projectionPlane.getIntersectingVector(observerVector, vertexDirection4);
        projectionPoints = projectionPlane.getGridOfVectors(height, width, projectionVertices);
        System.out.println("v0: " + projectionVertices[0]);
        System.out.println("v1: " + projectionVertices[1]);
        System.out.println("v2: " + projectionVertices[2]);
        System.out.println("v3: " + projectionVertices[3]);
    }
}
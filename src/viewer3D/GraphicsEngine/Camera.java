package viewer3D.GraphicsEngine;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
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
    private final Polygon[] polygons;
    private ProjectedPolygon[] projectedPolygons;
    private ProjectedPolygon[] translatedPolygons;
    private Vector[][] projectionPoints;
    private final Vector projectionPlanePosition;
    private final Vector originVector;
    private final Vector normalVector;
    private final Vector xUnitVector;
    private final Vector yUnitVector;
    private final Vector zUnitVector;
    private final Vector cameraPlaneNormalVector;
    private Vector cameraPositionVector;
    private Vector cameraRotationVector;
    private Matrix rollMatrix;
    private Matrix pitchMatrix;
    private Matrix yawMatrix;
    private final int defaultRotationAngle;
    private int pitchAngle;
    private int yawAngle;
    private int rollAngle;
    private int theta;
    private int phi;
    private int width;
    private int height;
    private final double EPSILON = Math.pow(10, -1);
    private double translationScalar;    
    private double xFOV;
    private double yFOV;
    private double[][] zBuffer;
    private double[] polygonBounds;
    private BufferedImage image;
    private GraphicsConfiguration gc;
    /**
     * Camera is constructed with a speed of 10 and an initial position of {0, 150, -450}
     * @param polygons
     * @param width
     * @param height
     * @param gc
     */
    public Camera(Polygon[] polygons, int width, int height, GraphicsConfiguration gc) {
        this.polygons = polygons;
        this.width = width;
        this.height = height;
        this.gc = gc;
        
        // Rotation fields
        defaultRotationAngle = 1;
        yawAngle = 0;
        pitchAngle = 0;
        rollAngle = 0;
        theta = 0;
        phi = 0;
        xFOV = Math.PI/4;
        yFOV = Math.PI/4;
        
        rollMatrix = Matrix.get3DZRotationMatrix(Math.toRadians(rollAngle));
        pitchMatrix = Matrix.get3DXRotationMatrix(Math.toRadians(pitchAngle));
        yawMatrix = Matrix.get3DYRotationMatrix(Math.toRadians(yawAngle));
         
        // Speed
        translationScalar = 100;

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
        
        setProjectionPoints();
        zBuffer = new double[height][width];
        
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Projects the polygons provided at construction into a screenspace coordinate space
     * usable by drawer classes
     * @return
     */
    public BufferedImage observe() {
        projectPolygons();
        image = gc.createCompatibleImage(width, height);
        image.setAccelerationPriority(1);
        zBuffer = new double[height][width];
        rasterizePolygons();
        return image;
    }
    private void projectPolygons() {
        // Project polygons onto camera plane
        translatedPolygons = new ProjectedPolygon[polygons.length];
        projectedPolygons = new ProjectedPolygon[polygons.length];
        for (int i = 0; i < polygons.length; i++) {
            Vector[] vertices = {polygons[i].getVertex(0), polygons[i].getVertex(1), polygons[i].getVertex(2)};
            Vector[] translatedVertices = new Vector[vertices.length];
            Vector[] projectedVertices = new Vector[vertices.length];
            for (int j = 0; j < translatedVertices.length; j++) {
                // rotate and translate the point before getting its intersection
                Vector fromCameraVector =  vertices[j].subtract(cameraPositionVector);
                Vector newPosition = (fromCameraVector)
                        .multiply(Matrix.get3DYRotationMatrix(Math.toRadians(-theta)))
                        .multiply(pitchMatrix);
                translatedVertices[j] = newPosition;
            }
            translatedPolygons[i] = new ProjectedPolygon(translatedVertices, polygons[i]);
            double dotProduct = (translatedPolygons[i].getOriginalPolygon().getNormal())
                    .dot(translatedPolygons[i].getOriginalPolygon().getVertex(0).subtract(cameraPositionVector));
            if (dotProduct < 0) {
                for (int j = 0; j < translatedVertices.length; j++) {
                    projectedVertices[j] = projectionPlane.getIntersectingVector(translatedVertices[j]);
                }
                projectedPolygons[i] = new ProjectedPolygon(projectedVertices, polygons[i]);
            }
        }
    }
    private void rasterizePolygons() {
        for (int i = 0; i < translatedPolygons.length; i++) {
            if (projectedPolygons[i] != null) {
                rasterizePolygon(projectedPolygons[i], translatedPolygons[i]);
            }
        }
    }
    private void rasterizePolygon(ProjectedPolygon polygon, ProjectedPolygon translatedPolygon) {
        // Convert polygon bounds to indices
        polygonBounds = polygon.getXYBounds();
        int colLo = (int)(((polygonBounds[0]+1)/2)*height);
        int colHi = (int)(((polygonBounds[1]+1)/2)*height+1);
        int rowLo = (int)(((polygonBounds[2]+1)/2)*width);
        int rowHi = (int)(((polygonBounds[3]+1)/2)*width+1);
        
        rowLo = (rowLo < 0)? 0 : rowLo;
        rowLo = (rowLo >= height)? height - 1 : rowLo;

        rowHi = (rowHi < 0)? 0 : rowHi;
        rowHi = (rowHi >= height)? height - 1 : rowHi;

        colLo = (colLo < 0)? 0 : colLo;
        colLo = (colLo >= width)? width - 1 : colLo;

        colHi = (colHi < 0)? 0 : colHi;
        colHi = (colHi >= width)? width - 1 : colHi;
        for (int i = rowLo; i <= rowHi; i++) {
            for (int j = colLo; j <= colHi; j++) {
                
                // Check if point is in triangle
                if (isInTriangle(polygon, projectionPoints[i][j])) {

                    // Check if point is not parallel
                    Vector intersectionVector = translatedPolygon.lineIntersection(projectionPoints[i][j]);
                    if (intersectionVector != null) {
                        
                        // Check against zBuffer, and for proximity
                        double z = intersectionVector.getLength();
                        if (zBuffer[i][j] == 0 || z > EPSILON && z < zBuffer[i][j]) {  
                            zBuffer[i][j] = z;
                            Color color = polygon.getFaceColor();
                            int[] colorArray = {color.getRed(), color.getGreen(), color.getBlue()};
                            image.getRaster().setPixel(j, height-1-i, colorArray);
                        }
                    }
                }
            }
        }
        image.flush();
    }
    private boolean isInTriangle(Polygon polygon, Vector pointVector) {
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
        projectionVertices[0] = new Vector(new double[]{1, 1, 1});        
        projectionVertices[1] = new Vector(new double[]{-1, -1, 1});
        projectionVertices[2] = new Vector(new double[]{1, -1, 1});
        projectionVertices[3] = new Vector(new double[]{-1, 1, 1});
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

        double xr = cameraRotationVector.getComponent(0)*translationScalar*0.1;
        double yr = cameraRotationVector.getComponent(1)*translationScalar*0.1;
        double zr = cameraRotationVector.getComponent(2)*translationScalar*0.1;
        switch(direction) {
            case FORWARD:
                cameraPositionVector = new Vector(new double[]{x + xr, y + yr, z + zr});
                break;
            case BACKWARD:
                cameraPositionVector = new Vector(new double[]{x + -xr, y + -yr, z + -zr});
                break;
            case LEFT:
                cameraPositionVector = new Vector(new double[]{x + -zr, y, z + xr});
                break;
            case RIGHT:
                cameraPositionVector = new Vector(new double[]{x + zr, y, z + -xr});
                break;
            case UP:
                cameraPositionVector = cameraPositionVector.add(cameraPlaneNormalVector.multiply(translationScalar*0.1));
                break;
            case DOWN:
                cameraPositionVector = cameraPositionVector.add(cameraPlaneNormalVector.multiply(-1*translationScalar*0.1));
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
        yawMatrix = Matrix.get3DYRotationMatrix(Math.toRadians(theta));
        pitchMatrix = Matrix.get3DXRotationMatrix(Math.toRadians(phi));
        cameraRotationVector = (((normalVector.multiply(pitchMatrix)).getUnitVector()).multiply(yawMatrix)).getUnitVector();
        cameraRotationVector.setComponent(-cameraRotationVector.getComponent(1), 1);
    }
      
////////////////////////////////////////////////////////////////////////////////
    /**
     * Gives parameter states of this camera as an array of Strings, including: 
     * Camera Position, Camera Direction, Projection Plane Position, Projection Plane Direction and Projection Vector Length
     * @return An array of strings of parameter states of this camera
     */
    public String[] getData() {
        String[] data = {
                        "Camera Position: " + cameraPositionVector,
                        "Camera Direction: " + cameraRotationVector,
                        "Yaw: " + theta + "°",
                        "Pitch: " + phi + "°"
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
     * @param x The x position of the camera
     * @param y The y position of the camera
     * @param z The z position of the camera
     */
    public void setPosition(double x, double y, double z) {
        cameraPositionVector = new Vector(new double[]{x, y, z});
    }

    /**
     * Sets the direction of the camera to point along the vector described by the given coordinates
     * @param x The x direction of the camera
     * @param y The y direction of the camera
     * @param z The z direction of the camera
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
                System.out.print("(" + i + " : " + j + ") " +vectorArray2D[i][j] + " ");
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
    public BufferedImage getBufferedImage() {
        return image;
    }
}
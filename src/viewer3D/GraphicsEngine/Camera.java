package viewer3D.GraphicsEngine;

import static java.awt.AlphaComposite.CLEAR;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import viewer3D.Math.Matrix;
import viewer3D.Math.Plane;
import viewer3D.Math.Vector;
import viewer3D.ProjectionTester;

/**
 * Responsible for observing a given set of polygons and projecting them into a 
 * screen coordinate space usable by drawer classes
 * @author Arik Dicks
 */
public class Camera {
    private Plane projectionPlane;
    private Polygon[] polygons;
    private ProjectedPolygon[] projectedPolygons;
    private ProjectedPolygon[] screenspacePolygons;
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
    private int defaultRotationAngle;
    private int pitchAngle;
    private int yawAngle;
    private int rollAngle;
    private int theta;
    private int phi;
    private int width;
    private int height;
    private double translationScalar;    
    private double xFOV;
    private double yFOV;
    private double[][] zBuffer;
    private double[] polygonBounds;
    private WritableRaster raster;
    private ColorModel colorModel;
    private BufferedImage image;
    private BufferedImage clearedImage;
    private VolatileImage vImage;
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
        
        zBuffer = new double[height][width];
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        clearedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setAccelerationPriority(1);
        vImage = gc.createCompatibleVolatileImage(width, height);
        vImage.setAccelerationPriority(1);
        //raster = image.getRaster();
        
        setProjectionPoints();
//        System.out.println(image.getColorModel());
//        System.out.println(image.getRaster().setSample(phi, phi, phi, phi));
//        System.out.println(image.getSampleModel());
        
    }

    /**
     * Projects the polygons provided at construction into a screenspace coordinate space
     * usable by drawer classes
     * @return
     */
    public Polygon[] observe() {
        //projectionPlane = new Plane(normalVector, normalVector);
        projectPolygons();
        mapPolygonsToScreenSpace();
        Graphics2D g2 = image.createGraphics();
//        g2.setBackground(Color.BLACK);
        //image.setData(clearedImage.getRaster());
        
//        g2.setColor(Color.BLACK);
//        g2.fillRect(0, 0, width, height);
        image = gc.createCompatibleImage(width, height);//new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //vImage.createGraphics();
        rasterizePolygons();
        //printVectorArray2D(projectionPoints, "Projection Points");
        //ProjectionTester drawer = new ProjectionTester(projectionPoints, polygons2D[0]);
        // Create Buffered Image and return
        return screenspacePolygons;
    }
    
    private void projectPolygons() {
        // Project polygons onto camera plane
        projectedPolygons = new ProjectedPolygon[polygons.length];
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
            projectedPolygons[i] = new ProjectedPolygon(projectedVertices, polygons[i]);
        }
    }
    
    private void mapPolygonsToScreenSpace() {
        // Map camera plane to screen space
        double[] vectorArray = {1, 1, 0};
        Vector vector = new Vector(vectorArray);

        screenspacePolygons = new ProjectedPolygon[polygons.length];
        for (int i = 0; i < projectedPolygons.length; i++) {
            Vector[] vertices = projectedPolygons[i].getVertices();
            Vector[] screenSpaceVertices = new Vector[3];
            for (int j = 0; j < vertices.length; j++) {
                screenSpaceVertices[j] = (vertices[j]).add(vector).multiply(0.5);
            }
            screenspacePolygons[i] = new ProjectedPolygon(screenSpaceVertices, polygons[i]);
        }
    }
    private void rasterizePolygons() {
        
        for (int i = 0; i < projectedPolygons.length; i++) {
            calcPointsInPolygon(projectedPolygons[i]);
            
        }
        

    }
    private void calcPointsInPolygon(ProjectedPolygon polygon) {
            // Find points in polygon Algorithm
        // Going to implement single edge algorithm, as its simpler, for now

        // Get the highest and lowest vertex
        // Describe the line
        // Round the vertex to nearest interior point
        // Get the next highest point

        // Actually, lets implement the bounding box algorithm, its even simpler

        // Get minX, minY, maxX, maxY

        polygonBounds = polygon.getXYBounds();
        
        // Projection points range from -1 to 1 in both axises 
        // The polygons however, have been already mapped to the unit square (0 to 1 in both axises)
        // I need the projection points to range from -1 to 1 for getting the intersections between the origin and the polygon points
        // I need to compare the projection points to the polygons pre-mapping OR convert the projection points
        // 
        
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

//        System.out.println("RowHi: " + rowHi);
//        System.out.println("RowLo: " + rowLo);
//        System.out.println("ColHi: " + colHi);
//        System.out.println("ColLo: " + colLo);
//
//        System.out.println(polygon);
//        System.out.print("Polygon Hi \t{" + String.format("%.2f", polygonBounds[0]) + ", " + String.format("%.2f", polygonBounds[3]) + "} ");
//        System.out.println("\t\t{" + String.format("%.2f", polygonBounds[1]) + ", " + String.format("%.2f", polygonBounds[3]) + "}");
//        System.out.print("Polygon Lo \t{" + String.format("%.2f", polygonBounds[0]) + ", " + String.format("%.2f", polygonBounds[2]) + "} ");
//        System.out.println("\t\t{" + String.format("%.2f", polygonBounds[1]) + ", " + String.format("%.2f", polygonBounds[2]) + "}");
//        System.out.println("Projection Hi " + "\t" + projectionPoints[rowHi][colLo] + "\t" + projectionPoints[rowHi][colHi]);
//        System.out.println("Projection Lo " + "\t" + projectionPoints[rowLo][colLo] + "\t" + projectionPoints[rowLo][colHi]);
//        System.out.println();
        //Loop through points
        for (int i = rowLo; i <= rowHi; i++) {
            for (int j = colLo; j <= colHi; j++) {
                boolean b = isInTriangle(polygon, projectionPoints[i][j]);
                if (b) {
                    Color color = polygon.getFaceColor();
                    int[] colorArray = {color.getRed(), color.getGreen(), color.getBlue()};
                    image.getRaster().setPixel(j, height-1-i, colorArray);
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
        /* 
        1. Get the vectors whos direction from the observer, point at the corners of the projection rectangle
        2. Give those vectors to the projection plane to get the set of vectors corresponding to the screens pixels
        */ 
//        Matrix xNegativeRotationMatrix = Matrix.get3DXRotationMatrix(-1*xFOV/2);
//        Matrix xPositiveRotationMatrix = Matrix.get3DXRotationMatrix(xFOV/2);
//        Matrix zNegativeRotationMatrix = Matrix.get3DZRotationMatrix(-1*yFOV/2);
//        Matrix zPositiveRotationMatrix = Matrix.get3DZRotationMatrix(yFOV/2);
//        Vector vertexDirection1 = originVector.add(normalVector.multiply(xNegativeRotationMatrix).multiply(zPositiveRotationMatrix));
//        Vector vertexDirection2 = originVector.add(normalVector.multiply(xNegativeRotationMatrix).multiply(zNegativeRotationMatrix));
//        Vector vertexDirection3 = originVector.add(normalVector.multiply(xPositiveRotationMatrix).multiply(zNegativeRotationMatrix));
//        Vector vertexDirection4 = originVector.add(normalVector.multiply(xPositiveRotationMatrix).multiply(zPositiveRotationMatrix));
//        projectionVertices[0] = projectionPlane.getIntersectingVector(originVector, vertexDirection1);
//        projectionVertices[1] = projectionPlane.getIntersectingVector(originVector, vertexDirection2);
//        projectionVertices[2] = projectionPlane.getIntersectingVector(originVector, vertexDirection3);
//        projectionVertices[3] = projectionPlane.getIntersectingVector(originVector, vertexDirection4);
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

        double xr = cameraRotationVector.getComponent(0)*translationScalar*0.01;
        double yr = cameraRotationVector.getComponent(1)*translationScalar*0.01;
        double zr = cameraRotationVector.getComponent(2)*translationScalar*0.01;
        switch(direction) {
            case FORWARD:
                cameraPositionVector = new Vector(new double[]{x + -xr, y + yr, z + zr});
                //cameraPositionVector = cameraPositionVector.add(cameraRotationVector.multiply(translationScalar));
                break;
            case BACKWARD:
                cameraPositionVector = new Vector(new double[]{x + xr, y + -yr, z + -zr});
                //cameraPositionVector = cameraPositionVector.add(cameraRotationVector.multiply(-translationScalar));
                break;
            case LEFT:
                cameraPositionVector = new Vector(new double[]{x + -zr, y, z + -xr});
                //Vector translationLeftVector = cameraRotationVector.cross(cameraPlaneNormalVector);
                //cameraPositionVector = cameraPositionVector.add(translationLeftVector.multiply(translationScalar));
                break;
            case RIGHT:
                cameraPositionVector = new Vector(new double[]{x + zr, y, z + xr});
                //Vector translationRightVector = cameraPlaneNormalVector.cross(cameraRotationVector);
                //cameraPositionVector = cameraPositionVector.add(translationRightVector.multiply(translationScalar));
                break;
            case UP:
                cameraPositionVector = cameraPositionVector.add(cameraPlaneNormalVector.multiply(translationScalar*0.01));
                break;
            case DOWN:
                cameraPositionVector = cameraPositionVector.add(cameraPlaneNormalVector.multiply(-1*translationScalar*0.01));
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
     * Sets the direction of the camera to point along the vector described by the given coordinates
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
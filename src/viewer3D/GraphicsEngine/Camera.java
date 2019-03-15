package viewer3D.GraphicsEngine;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
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
    private final double EPSILON = Math.pow(10, -14);
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
                //rasterizePolygon(projectedPolygons[i], translatedPolygons[i]);
                //rasterizePolygon(projectedPolygons[i], translatedPolygons[i], 0);
                rasterizePolygon(projectedPolygons[i], translatedPolygons[i], 0, 0);
            }
        }
    }
    private void rasterizePolygon(ProjectedPolygon polygon, ProjectedPolygon translatedPolygon, int dummy, int dummy2) {
        polygonBounds = polygon.getXYBounds();
        Vector[] vertices = polygon.getVertices();
        
        // Vertices converted from normalspace to screenspace
        double v1X_D = ((vertices[0].getComponent(0)+1)/2)*width;
        double v1Y_D = ((vertices[0].getComponent(1)+1)/2)*height;
        double v2X_D = ((vertices[1].getComponent(0)+1)/2)*width;
        double v2Y_D = ((vertices[1].getComponent(1)+1)/2)*height;
        double v3X_D = ((vertices[2].getComponent(0)+1)/2)*width;
        double v3Y_D = ((vertices[2].getComponent(1)+1)/2)*height;
        
        // Vertices quantized from screenspace to pixelspace
        int v1X, v1Y, v2X, v2Y, v3X, v3Y;
        v1X = (int)Math.round(v1X_D);
        v1Y = (int)Math.round(v1Y_D);
        v2X = (int)Math.round(v2X_D);
        v2Y = (int)Math.round(v2Y_D);
        v3X = (int)Math.round(v3X_D);
        v3Y = (int)Math.round(v3Y_D);
        
        Sector v1Sector, v2Sector, v3Sector;
        v1Sector = getSector(v1X, v1Y);
        v2Sector = getSector(v2X, v2Y);
        v3Sector = getSector(v3X, v3Y);
        
        int[] line1Points = new int[4];
        int[] line2Points = new int[4];
        int[] line3Points = new int[4];
        
        // Project first line
        restrictLine(v1X, v1Y, v2X, v2Y, v1Sector, v2Sector, line1Points); 
        restrictLine(v2X, v2Y, v3X, v3Y, v2Sector, v3Sector, line2Points); 
        restrictLine(v1X, v1Y, v3X, v3Y, v1Sector, v3Sector, line3Points); 
        
        int rowHi = Math.max(Math.max(v1Y, v2Y), v3Y);
        int rowLo = Math.min(Math.min(v1Y, v2Y), v3Y);
        int colHi = Math.max(Math.max(v1X, v2X), v3X);
        int colLo = Math.min(Math.min(v1X, v2X), v3X);
        rowHi = (rowHi < 0)? 0 : rowHi;
        rowHi = (rowHi >= height)? height - 1 : rowHi;
        rowLo = (rowLo < 0)? 0 : rowLo;
        rowLo = (rowLo >= height)? height - 1 : rowLo;
        colHi = (colHi < 0)? 0 : colHi;
        colHi = (colHi >= width)? width - 1 : colHi;
        colLo = (colLo < 0)? 0 : colLo;
        colLo = (colLo >= width)? width - 1 : colLo;
        int[] leftBounds = new int[rowHi-rowLo+1];
        int[] rightBounds = new int[rowHi-rowLo+1];
        
        for (int i = 0; i < leftBounds.length; i++) {
            leftBounds[i] = width;
        }
        System.out.println();
        System.out.println("Projected Vertices");
        System.out.println(
                "Vertex1, Line1 {" + line1Points[0] + " : " + line1Points[1] + "}\n" +
                "Vertex2, Line1 {" + line1Points[2] + " : " + line1Points[3] + "}"
        );
        System.out.println(
                "Vertex2, Line2 {" + line2Points[0] + " : " + line2Points[1] + "}\n" +
                "Vertex3, Line2 {" + line2Points[2] + " : " + line2Points[3] + "}"
        );
        System.out.println(
                "Vertex1, Line3 {" + line3Points[0] + " : " + line3Points[1] + "}\n" +
                "Vertex3, Line3 {" + line3Points[2] + " : " + line3Points[3] + "}"
        );
        System.out.println("RowBounds: " + rowLo + " : " + rowHi);
        System.out.println("ColBounds: " + colLo + " : " + colHi);
        System.out.println("Num Of Rows: " + leftBounds.length);
        
        getLinePoints(
                line1Points[0]-colLo, line1Points[1]-rowLo, 
                line1Points[2]-colLo, line1Points[3]-rowLo,
                leftBounds, rightBounds, colLo);
        getLinePoints(
                line2Points[0]-colLo, line2Points[1]-rowLo, 
                line2Points[2]-colLo, line2Points[3]-rowLo,
                leftBounds, rightBounds, colLo);
        getLinePoints(
                line3Points[0]-colLo, line3Points[1]-rowLo, 
                line3Points[2]-colLo, line3Points[3]-rowLo,
                leftBounds, rightBounds, colLo);
        
        for (int i = 0; i < leftBounds.length; i++) {
            leftBounds[i] = (leftBounds[i] < 0)? 0 : leftBounds[i];
            leftBounds[i] = (leftBounds[i] > width-1)? width-1 : leftBounds[i];
            rightBounds[i] = (rightBounds[i] < 0)? 0 : rightBounds[i];
            rightBounds[i] = (rightBounds[i] > width-1)? width-1 : rightBounds[i];
        }
        System.out.println();
        
        // RASTERIZE //
        for (int i = rowLo; i < rowHi ; i++) {
            for (int j = leftBounds[i-rowLo]; j < rightBounds[i-rowLo] ; j++) {   
                // Check if point is not parallel
                Vector intersectionVector = translatedPolygon.lineIntersection(projectionPoints[i][j]);
                if (intersectionVector != null) {

                    // Check against zBuffer, and for proximity
                    double z = intersectionVector.getLength();
                    if (zBuffer[i][j] == 0 || z < zBuffer[i][j]) {
                        zBuffer[i][j] = z;
                        Color color = polygon.getFaceColor();
                        int[] colorArray = {color.getRed(), color.getGreen(), color.getBlue()};
                        image.getRaster().setPixel(j, height-1-i, colorArray);
                    }
                }
            }
        }
    }
    private void restrictLine(double v1X, double v1Y, double v2X, double v2Y, Sector v1Sector, Sector v2Sector, int[] linePoints) {
        // y = (^y)/(^x)(x-x1) + y1
        // x = (^y)/(^x)(y-y1) + x1
        
        if (v1Sector == Sector.CENTER_MIDDLE) {
            linePoints[0] = (int)Math.round(v1X);
            linePoints[1] = (int)Math.round(v1Y);
            System.out.println("IN CENTER: " + v1X + " : " + v1Y + " --> " + linePoints[0] + " : " + linePoints[1]);
        } else {
            if (           // Vertex is in the LEFT trapezoid
                v1Sector == Sector.TOP_LEFT_LO ||
                v1Sector == Sector.CENTER_LEFT ||
                v1Sector == Sector.BOT_LEFT_HI) {
                linePoints[0] = 0;
                linePoints[1] = (int)Math.round(((v2Y-v1Y)/(v2X-v1X)*(-v1X)+v1Y));
                System.out.println("LEFT TRAPEZOID (" + v1Sector + "): " + v1X + " : " + v1Y + " --> " + linePoints[0] + " : " + linePoints[1]);
            } else if (    // Vertex is in the RIGHT trapezoid
                v1Sector == Sector.TOP_RIGHT_LO ||
                v1Sector == Sector.CENTER_RIGHT ||
                v1Sector == Sector.BOT_RIGHT_HI) {
                linePoints[0] = width-1;
                linePoints[1] = (int)Math.round(((v2Y-v1Y)/(v2X-v1X)*(width-1-v1X)+v1Y));
                System.out.println("RIGHT TRAPEZOID (" + v1Sector + "): " + v1X + " : " + v1Y + " --> " + linePoints[0] + " : " + linePoints[1]);
            } else if (    // Vertex is in the TOP trapezoid
                v1Sector == Sector.TOP_LEFT_HI ||
                v1Sector == Sector.TOP_MIDDLE ||
                v1Sector == Sector.TOP_RIGHT_HI) {
                linePoints[0] = (int)Math.round((height-1-v1Y)*((v2X-v1X)/(v2Y-v1Y))+v1X);
                System.out.println("TOP TRAPEZOID (" + v1Sector + "): " + v1X + " : " + v1Y + " --> " + linePoints[0] + " : " + linePoints[1]);
                linePoints[1] = height-1;
            } else if (    // Vertex is in the BOTTOM trapezoid
                v1Sector == Sector.BOT_LEFT_LO ||
                v1Sector == Sector.BOT_MIDDLE ||
                v1Sector == Sector.BOT_RIGHT_LO) {
                linePoints[0] = (int)Math.round((-v1Y)*((v2X-v1X)/(v2Y-v1Y))+v1X);
                System.out.println("BOT TRAPEZOID (" + v1Sector + "): " + v1X + " : " + v1Y + " --> " + linePoints[0] + " : " + linePoints[1]);
                linePoints[1] = 0;
            }
        }
        if (v2Sector == Sector.CENTER_MIDDLE) {
            linePoints[2] = (int)Math.round(v2X);
            linePoints[3] = (int)Math.round(v2Y);
            System.out.println("IN CENTER: " + v2X + " : " + v2Y + " --> " + linePoints[2] + " : " + linePoints[3]);
        } else {
            if (           // Vertex is in the LEFT trapezoid
                v2Sector == Sector.TOP_LEFT_LO ||
                v2Sector == Sector.CENTER_LEFT ||
                v2Sector == Sector.BOT_LEFT_HI) {
                linePoints[2] = 0;
                linePoints[3] = (int)Math.round(((v2Y-v1Y)/(v2X-v1X)*(-v1X)+v1Y));
                System.out.println("LEFT TRAPEZOID (" + v2Sector + "): " + v2X + " : " + v2Y + " --> " + linePoints[2] + " : " + linePoints[3]);
            } else if (    // Vertex is in the RIGHT trapezoid
                v2Sector == Sector.TOP_RIGHT_LO ||
                v2Sector == Sector.CENTER_RIGHT ||
                v2Sector == Sector.BOT_RIGHT_HI) {
                linePoints[2] = width-1;
                linePoints[3] = (int)Math.round(((v2Y-v1Y)/(v2X-v1X)*(width-1-v1X)+v1Y));
                System.out.println("RIGHT TRAPEZOID (" + v2Sector + "): " + v2X + " : " + v2Y + " --> " + linePoints[2] + " : " + linePoints[3]);
            } else if (    // Vertex is in the TOP trapezoid
                v2Sector == Sector.TOP_LEFT_HI ||
                v2Sector == Sector.TOP_MIDDLE ||
                v2Sector == Sector.TOP_RIGHT_HI) {
                linePoints[2] = (int)Math.round((height-1-v1Y)*((v2X-v1X)/(v2Y-v1Y))+v1X);
                linePoints[3] = height-1;
                System.out.println("TOP TRAPEZOID (" + v2Sector + "): " + v2X + " : " + v2Y + " --> " + linePoints[2] + " : " + linePoints[3]);
            } else if (    // Vertex is in the BOTTOM trapezoid
                v2Sector == Sector.BOT_LEFT_LO ||
                v2Sector == Sector.BOT_MIDDLE ||
                v2Sector == Sector.BOT_RIGHT_LO) {
                linePoints[2] = (int)Math.round((-v1Y)*((v2X-v1X)/(v2Y-v1Y))+v1X);
                linePoints[3] = 0;
                System.out.println("BOT TRAPEZOID (" + v2Sector + "): " + v2X + " : " + v2Y + " --> " + linePoints[2] + " : " + linePoints[3]);
            }
        }
    }
    private int numOfVerticesInCenter(Sector sector1, Sector sector2) {
        int numOfVerticesInCenter = 0;
        if (sector1 == Sector.CENTER_MIDDLE)
            numOfVerticesInCenter++;
        if (sector2 == Sector.CENTER_MIDDLE)
            numOfVerticesInCenter++;
        return numOfVerticesInCenter;
    }
    private boolean verticesOccupySameSectorTrapezoid(Sector sector1, Sector sector2) {
//        TOP_LEFT_LO, TOP_LEFT_HI, TOP_MIDDLE, TOP_RIGHT_LO, TOP_RIGHT_HI,
//        CENTER_LEFT, CENTER_MIDDLE, CENTER_RIGHT,
//        BOT_LEFT_LO, BOT_LEFT_HI, BOT_MIDDLE, BOT_RIGHT_LO, BOT_RIGHT_HI,
//        ERROR

        // Eliminate vertices that occupy the same trapezoid of sectors
        // Y bounds covered by the screen rectangle intersections will be calculated
        // by the other lines
        if (    // Vertices are in the LEFT trapezoid
                sector1 == Sector.TOP_LEFT_LO ||
                sector1 == Sector.CENTER_LEFT ||
                sector1 == Sector.BOT_LEFT_HI &&
                sector2 == Sector.TOP_LEFT_LO ||
                sector2 == Sector.CENTER_LEFT ||
                sector2 == Sector.BOT_LEFT_HI
                ) {
            return false;
        }
        if (    // Vertices are in the RIGHT trapezoid
                sector1 == Sector.TOP_RIGHT_LO ||
                sector1 == Sector.CENTER_RIGHT ||
                sector1 == Sector.BOT_RIGHT_HI &&
                sector2 == Sector.TOP_RIGHT_LO ||
                sector2 == Sector.CENTER_RIGHT ||
                sector2 == Sector.BOT_RIGHT_HI
                ) {
            return false;
        }
        if (    // Vertices are in the TOP trapezoid
                sector1 == Sector.TOP_LEFT_HI ||
                sector1 == Sector.TOP_MIDDLE ||
                sector1 == Sector.TOP_RIGHT_HI &&
                sector2 == Sector.TOP_LEFT_HI ||
                sector2 == Sector.TOP_MIDDLE ||
                sector2 == Sector.TOP_RIGHT_HI
                ) {
            return false;
        }
        if (    // Vertices are in the BOTTOM trapezoid
                sector1 == Sector.BOT_LEFT_HI ||
                sector1 == Sector.BOT_MIDDLE ||
                sector1 == Sector.BOT_RIGHT_HI &&
                sector2 == Sector.BOT_LEFT_HI ||
                sector2 == Sector.BOT_MIDDLE ||
                sector2 == Sector.BOT_RIGHT_HI
                ) {
            return false;
        }
        return true;
    }
    private Row getRow(int v1Y) {
        if (v1Y < 0) {
            return Row.BOTTOM;
        } else if (v1Y >= height) {
            return Row.TOP;
        } else {
            return Row.CENTER;
        }
    }
    private Column getColumn(int v1X) {
        if (v1X < 0) {
            return Column.LEFT;
        } else if (v1X >= width) {
            return Column.RIGHT;
        } else {
            return Column.MIDDLE;
        }
    }
    private Sector getSector(int v1X, int v1Y) { 
        Row row;
        Column col;
        // Get column
        if (v1X < 0) {
            col = Column.LEFT;
        } else if (v1X >= width) {
            col = Column.RIGHT;
        } else {
            col = Column.MIDDLE;
        }
        
        // Get row
        if (v1Y < 0) {
            row = Row.BOTTOM;
        } else if (v1Y >= height) {
            row = Row.TOP;
        } else {
            row = Row.CENTER;
        }
//        System.out.println(v1X + " : " + v1Y);
//        System.out.println(row);
//        System.out.println(col);
//        System.out.println();
        switch(row) {
            case TOP:
                switch (col) {
                    case LEFT:     // top left
                        if ((v1X-width)*(height)-(v1Y)*(-width) >= 0)
                            return Sector.TOP_LEFT_HI;
                        else 
                            return Sector.TOP_LEFT_LO;
                    case MIDDLE:     // top middle
                        return Sector.TOP_MIDDLE;
                    case RIGHT:     // top right
                        if ((v1X)*(height)-(v1Y)*(width) >= 0)
                            return Sector.TOP_RIGHT_LO;
                        else 
                            return Sector.TOP_RIGHT_HI;
                }
                break;
            case CENTER:
                switch (col) {
                    case LEFT:     // center left
                        return Sector.CENTER_LEFT;
                    case MIDDLE:     // center middle
                        return Sector.CENTER_MIDDLE;
                    case RIGHT:     // center right
                        return Sector.CENTER_RIGHT;
                }
                break;
            case BOTTOM:
                switch (col) {
                    case LEFT:     // bottom left
                        if ((v1X)*(height)-(v1Y)*(width) >= 0)
                            return Sector.BOT_RIGHT_HI;
                        else 
                            return Sector.BOT_RIGHT_LO;
                    case MIDDLE:     // bottom middle
                        return Sector.BOT_MIDDLE;
                    case RIGHT:     // bottom right
                        if ((v1X-width)*(height)-(v1Y)*(-width) >= 0)
                            return Sector.BOT_LEFT_HI;
                        else 
                            return Sector.BOT_LEFT_LO;
                }
            }
        return Sector.ERROR;
        }
    private boolean isVertexOnScreen(int v1X, int v1Y) {
        return(v1X >= width || v1X < 0 || v1Y >= height || v1Y < 0);
    }
    private void getLinePoints(int x1, int y1, int x2, int y2, int[] leftBounds, int[] rightBounds, int colLo) {
        // delta of exact value and rounded value of the dependent variable
        int d = 0;
 
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
 
        int dx2 = 2 * dx; // slope scaling factors to
        int dy2 = 2 * dy; // avoid floating point
 
        int ix = x1 < x2 ? 1 : -1; // increment direction
        int iy = y1 < y2 ? 1 : -1;
 
        int x = x1;
        int y = y1;
 
        if (dx >= dy) {     // The slope is less than 1
            int i = 0;
            while (true) {
                //System.out.println(y + " : " + y2);
                int xNew = x+colLo;
                if (xNew < leftBounds[y]) {
                    leftBounds[y] = xNew;
                }
                if (xNew > rightBounds[y]) {
                    rightBounds[y] = xNew;
                }
                if (x == x2)        // The end of the line has been reached, exit
                    break;
                x += ix;            // increment x to the next pixel column
                d += dy2;           
                if (d > dx) {
                    y += iy;
                    d -= dx2;
                }
            }
        } else {            // The slope is greater than 1
            while (true) {
                //System.out.println(y + " : " + y2);
                int xNew = x+colLo;
                if (xNew < leftBounds[y]) {
                    leftBounds[y] = xNew;
                }
                if (xNew > rightBounds[y]) {
                    rightBounds[y] = xNew;
                }
                if (y == y2)        // The end of the line has been reached, exit
                    break;
                y += iy;            // increment y to the next pixel row
                d += dx2;
                if (d > dy) {
                    x += ix;
                    d -= dy2;
                }
            }
        }
    }
    private void rasterizePolygon(ProjectedPolygon polygon, ProjectedPolygon translatedPolygon, int dummy) {
        /*
        Algorithm:
            Get each pair of vertices (3 pairs)
            Find the equation of the line that passes through each pair of vertices
            Calculate the polygon interior integer points along the line segment
            Store the x-bounds information
            Walk through each row between the x-bounds, rasterizing each corresponding pixel
        */
        polygonBounds = polygon.getXYBounds();
        Vector[] vertices = polygon.getVertices();
        
        // Vertex co-ordinates in projection space (-1 to 1)
        double v1X = (vertices[0].getComponent(0)+1)/2;
        double v1Y = (vertices[0].getComponent(1)+1)/2;
        double v2X = (vertices[1].getComponent(0)+1)/2;
        double v2Y = (vertices[1].getComponent(1)+1)/2;
        double v3X = (vertices[2].getComponent(0)+1)/2;
        double v3Y = (vertices[2].getComponent(1)+1)/2;
        
        // Vertex y's in pixel space (indexes)
        int v1YIndex = (int)((v1Y)*height);
        v1YIndex = (v1YIndex < 0)? 0 : v1YIndex;
        v1YIndex = (v1YIndex >= height)? height - 1 : v1YIndex;
        int v2YIndex = (int)((v2Y)*height);
        v2YIndex = (v2YIndex < 0)? 0 : v2YIndex;
        v2YIndex = (v2YIndex >= height)? height - 1 : v2YIndex;
        int v3YIndex = (int)((v3Y)*height);
        v3YIndex = (v3YIndex < 0)? 0 : v3YIndex;
        v3YIndex = (v3YIndex >= height)? height - 1 : v3YIndex;
        
        // Upper and lower y index bounds
        int rowHi = Math.max(Math.max(v1YIndex, v2YIndex), v3YIndex);
        int rowLo = Math.min(Math.min(v1YIndex, v2YIndex), v3YIndex);
        int[] loXBounds = new int[rowHi+1-rowLo];
        int[] hiXBounds = new int[rowHi+1-rowLo];
        
        // Initialize bounds to illegal pixel index values for error catching
        for (int i = 0; i < loXBounds.length; i++) {
            loXBounds[i] = -1;
            hiXBounds[i] = -1;
        }
        updateBoundsFromLine(loXBounds, hiXBounds, rowLo, v1YIndex, v2YIndex, v1X, v2X, v1Y, v2Y, polygon);
        updateBoundsFromLine(loXBounds, hiXBounds, rowLo, v2YIndex, v3YIndex, v2X, v3X, v2Y, v3Y, polygon);
        updateBoundsFromLine(loXBounds, hiXBounds, rowLo, v1YIndex, v3YIndex, v1X, v3X, v1Y, v3Y, polygon);
        
        for (int i = rowLo; i < rowHi; i++) {
            for (int j = loXBounds[i-rowLo]; j <= hiXBounds[i-rowLo]; j++) {
                if (loXBounds[i-rowLo] >= 0 && hiXBounds[i-rowLo] >= 0) {
                    
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
    }
    private void updateBoundsFromLine(
            int[] loXBounds, 
            int[] hiXBounds, 
            int rowLo, 
            int v1YIndex, 
            int v2YIndex, 
            double v1X, 
            double v2X, 
            double v1Y, 
            double v2Y,
            ProjectedPolygon polygon) {
        double m = (v1Y-v2Y)/(v1X-v2X);
        for (int i = Math.min(v1YIndex, v2YIndex); i <= Math.max(v1YIndex, v2YIndex); i++) {
            double x;
            double y = i/((double)height);
            
            // Find the X at the given y for this line
            if (Math.abs(v1X - v2X) < EPSILON || Math.abs(m) < EPSILON) {
                x = v1X;
            } else {
                x = getXAtY(v2X, v1X, m, y);
            }

            // Find the points on either side of the line with integer x's
            int xIntLeft = (int)(x*width);
            int xIntRight = (int)(x*width+1);
            double xLeft = xIntLeft/((double)width);
            double xRight = xIntRight/((double)width);
            double xFinal = -1;
            
            // Determine which integer x is in the triangle
            if (isInTriangle(polygon, xLeft*2-1, y*2-1)) {
                xFinal = xLeft;
            } else if (isInTriangle(polygon, xRight*2-1, y*2-1)) {
                xFinal = xRight;
            }
            
            // Find the lower or bound this point rests on and set it
            if (loXBounds[i-rowLo] < 0) {
                loXBounds[i-rowLo] = (int)(xFinal*width);
                loXBounds[i-rowLo] = (loXBounds[i-rowLo] >= width)? width - 1 : loXBounds[i-rowLo];
            } else if (xFinal != -1 && (int)(xFinal*width) < loXBounds[i-rowLo]) {
                loXBounds[i-rowLo] = (int)(xFinal*width);
                loXBounds[i-rowLo] = (loXBounds[i-rowLo] >= width)? width - 1 : loXBounds[i-rowLo];
            }
            if (hiXBounds[i-rowLo] < 0) {
                hiXBounds[i-rowLo] = (int)(xFinal*width)-1;
                hiXBounds[i-rowLo] = (hiXBounds[i-rowLo] >= width)? width - 1 : hiXBounds[i-rowLo];
            } else if (xFinal != -1 && hiXBounds[i-rowLo] == -1 || (int)(xFinal*width) > hiXBounds[i-rowLo]) {
                hiXBounds[i-rowLo] = (int)(xFinal*width)-1;
                hiXBounds[i-rowLo] = (hiXBounds[i-rowLo] >= width)? width - 1 : hiXBounds[i-rowLo];
            }
        }
    }
    private double getXAtY(double y1, double x1, double m, double y) {
        return ((y-y1)/m) + x1;
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
    private boolean isInTriangle(Polygon polygon, double x, double y) {
        Vector[] vertices = polygon.getVertices();
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
    public enum Sector {
        TOP_LEFT_LO, TOP_LEFT_HI, TOP_MIDDLE, TOP_RIGHT_LO, TOP_RIGHT_HI,
        CENTER_LEFT, CENTER_MIDDLE, CENTER_RIGHT,
        BOT_LEFT_LO, BOT_LEFT_HI, BOT_MIDDLE, BOT_RIGHT_LO, BOT_RIGHT_HI,
        ERROR
    }
    public enum Row {TOP, CENTER, BOTTOM}
    public enum Column {LEFT, MIDDLE, RIGHT}
}

//
//    /**
//     * Rotates the camera one degree by the given rotation type
//     * @param rotation The type of rotation
//     * @param rotationDelta
//     */
//    public void rotate(Rotation rotation, int rotationDelta) {
//        // Adjust rotation angles
//        switch(rotation) {
//            case YAW_LEFT:
//                yawAngle = (yawAngle + defaultRotationAngle*rotationDelta)%360;
//                break;
//            case YAW_RIGHT:
//                yawAngle = (yawAngle + -1*defaultRotationAngle*rotationDelta)%360;
//                break;
//            case PITCH_FORWARD:
//                pitchAngle = (pitchAngle + defaultRotationAngle*rotationDelta)%360;
//                break;
//            case PITCH_BACKWARD:
//                pitchAngle = (pitchAngle + -1*defaultRotationAngle*rotationDelta)%360;
//                break;
//            case ROLL_COUNTER_CLOCKWISE:
//                rollAngle = (rollAngle + -1*defaultRotationAngle*rotationDelta)%360;
//                break;
//            case ROLL_CLOCKWISE:
//                rollAngle = (rollAngle + defaultRotationAngle*rotationDelta)%360;
//                break;
//        }
//        yawAngle = (yawAngle < 0)? 360+yawAngle : yawAngle;
//        pitchAngle = (pitchAngle < 0)? 360+pitchAngle : pitchAngle;
//        rollAngle = (rollAngle < 0)? 360+rollAngle : rollAngle;
//
//        // Apply rotations to camera direction
//        rollMatrix = Matrix.get3DZRotationMatrix(Math.toRadians(rollAngle));
//        pitchMatrix = Matrix.get3DXRotationMatrix(Math.toRadians(pitchAngle));
//        yawMatrix = Matrix.get3DYRotationMatrix(Math.toRadians(yawAngle));
//        cameraRotationVector = ((zUnitVector.multiply(yawMatrix).multiply(pitchMatrix).getUnitVector()));
//    }





//    private void updateBoundsFromLine(
//            double[] loXBounds, 
//            double[] hiXBounds, 
//            int rowLo, 
//            int v1YIndex, 
//            int v2YIndex, 
//            double v1X, 
//            double v2X, 
//            double v1Y, 
//            double v2Y,
//            ProjectedPolygon polygon) {
        // calculate x's along first line, set lo and x bounds to what we find
//        double m1 = (v1Y-v2Y)/(v1X-v2X);
//        for (int i = Math.min(v1YIndex, v2YIndex); i <= Math.max(v1YIndex, v2YIndex); i++) {
//            if (Math.abs(v1X - v2X) < EPSILON || Math.abs(m1) < EPSILON) {
//                int min = (int)(((v1X+1)/2)*width);
//                int max = (int)(((v2X+1)/2)*width);
//                int temp = min;
//                if (min > max) {
//                    min = max;
//                    max = temp;
//                }
//                loXBounds[i-rowLo] = min;
//                hiXBounds[i-rowLo] = max;
//            } else {
//                loXBounds[i-rowLo] = (int)getXAtY(v1Y, v1X, m1, i);
//                hiXBounds[i-rowLo] = (int)(getXAtY(v1Y, v1X, m1, i));
//            }
//        }
//        // calculate x's along second line, check which side of the line is in the 
//        // triangle, then add it to the appropriate bound
//        double m2 = (v2Y-v3Y)/(v2X-v3X);
//        for (int i = Math.min(v2YIndex, v3YIndex); i <= Math.max(v2YIndex, v3YIndex); i++) {
//            double x;
//            double y = i/((double)height);
//            
//            // Find the X at the given y for this line
//            if (Math.abs(v2X - v3X) < EPSILON || Math.abs(m2) < EPSILON) {
//                x = v2X;
//            } else {
//                x = getXAtY(v2Y, v2X, m2, y);
//            }
//
//            // Find the points on either side of the line with integer x's
//            int xIntLeft = (int)(x*width);
//            int xIntRight = (int)(x*width+1);
//            double xLeft = xIntLeft/((double)width);
//            double xRight = xIntRight/((double)width);
//            double xFinal = -1;
//            
//            // Determine which integer x is in the triangle
//            if (isInTriangle(polygon, xLeft*2-1, y*2-1)) {
//                xFinal = xLeft;
//            } else if (isInTriangle(polygon, xRight*2-1, y*2-1)) {
//                xFinal = xRight;
//            }
//            
//            // Find the lower or bound this point rests on and set it
//            if (loXBounds[i-rowLo] < 0) {
//                loXBounds[i-rowLo] = (int)(xFinal*width);
//            } else if (xFinal != -1 && (int)(xFinal*width) < loXBounds[i-rowLo]) {
//                loXBounds[i-rowLo] = (int)(xFinal*width);
//            }
//            if (hiXBounds[i-rowLo] < 0) {
//                hiXBounds[i-rowLo] = (int)(xFinal*width);
//            } else if (xFinal != -1 && hiXBounds[i-rowLo] == -1 || (int)(xFinal*width) > hiXBounds[i-rowLo]) {
//                hiXBounds[i-rowLo] = (int)(xFinal*width);
//            }
//        }
        
package viewer3D.Polyhedrons;

import java.awt.Color;
import viewer3D.Math.Vector;
import viewer3D.GraphicsEngine.Polygon;
//import viewer3D.PolygonObject;

/**
 *
 * @author Arik Dicks
 */
public class Floor implements Polyhedron {
    private Vector normalVector;
    private static String shapeName = "Floor";
    private static int numID = 1;
    private Vector[][] vertices;
    private Polygon[] polygons;
    private int xGridWidth;
    private int zGridLength;
    private int xNumOfSquares;
    private int zNumOfSquares;
    private int xStart;
    private int zStart;
    private Color color1;
    private Color color2;
        
    /**
     *
     * @param xNumOfSquares
     * @param zNumOfSquares
     * @param xGridWidth
     * @param zGridLength
     * @param xStart
     * @param zStart
     */
    public Floor(int xNumOfSquares, int zNumOfSquares, int xGridWidth, int zGridLength, int xStart, int zStart) {
        this.xNumOfSquares = xNumOfSquares;
        this.zNumOfSquares = zNumOfSquares;
        this.xGridWidth = xGridWidth;
        this.zGridLength = zGridLength;
        this.xStart = xStart;
        this.zStart = zStart;
        color1 = Color.GREEN;
        color2 = Color.BLUE;
        initVertices();
        initPolygons();
    }
    private void initVertices() {
        vertices = new Vector[xNumOfSquares+1][zNumOfSquares+1];
        for (int i = 0; i < vertices.length; i++) {
            for (int j = 0; j < vertices[0].length; j++) {
                double[] vertexArray = {(i*xGridWidth + xStart), 0, j*zGridLength + zStart};
                vertices[i][j] = new Vector(vertexArray);
            }
        }
    }
    private void initPolygons() {
        int numOfPolygons = xNumOfSquares*zNumOfSquares*2;
        polygons = new Polygon[numOfPolygons];
        int polygonIndex = 0;
        for (int i = 1; i < vertices.length; i++) {
            for (int j = 1; j < vertices[0].length; j++) {
                polygons[polygonIndex] = new Polygon(new Vector[]{
                        vertices[i-1][j-1],
                        vertices[i][j-1],
                        vertices[i-1][j]});
                polygons[polygonIndex].setShapeID(shapeName +"_"+ numID);
                polygons[polygonIndex].setPolygonID("Square_p1: " + "x"+i + "," + "y"+j);
                switch ((i+j)%2) {
                    case 0:
                        polygons[polygonIndex].setFaceColor(color1);
                        break;
                    case 1:
                        polygons[polygonIndex].setFaceColor(color2);
                        break;
                }
                polygons[++polygonIndex] = new Polygon(new Vector[]{
                        vertices[i][j],
                        vertices[i][j-1],
                        vertices[i-1][j]});
                polygons[polygonIndex].setShapeID(shapeName +"_"+ numID);
                polygons[polygonIndex].setPolygonID("Square_p2: " + "x"+i + "," + "y"+j);
                switch ((i+j)%2) {
                    case 0:
                        polygons[polygonIndex].setFaceColor(color1);
                        break;
                    case 1:
                        polygons[polygonIndex].setFaceColor(color2);
                        break;
                }
                polygonIndex++;
            }
        }
    }

    /**
     *
     * @return
     */
    @Override
    public Polygon[] getPolygons() {
        return polygons;
    }

    /**
     *
     * @return
     */
    @Override
    public String getID() {
        return "";
    }

    /**
     *
     * @return
     */
    @Override
    public Vector getNorma() {
        return normalVector;
    }
}
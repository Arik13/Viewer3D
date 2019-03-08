package viewer3D.Polyhedrons;

import java.awt.Color;
import viewer3D.Math.Vector;
import viewer3D.GraphicsEngine.Polygon;
//import viewer3D.PolygonObject;

/**
 *
 * @author Arik Dicks
 */
public class Axes3D implements Polyhedron {
    private Vector normalVector;
    private static String shapeName = "Axes3D";
    private static int numID = 1;
    private Polygon[] polygons;
    private Color edgeColor = Color.RED;

    /**
     *
     * @param xHi
     * @param xLo
     * @param yHi
     * @param yLo
     * @param zHi
     * @param zLo
     */
    public Axes3D(double xHi, double xLo, double yHi, double yLo, double zHi, double zLo) {
        polygons = new Polygon[3];
        double[] xAxisHiArray = {xHi, 0, 0};
        double[] xAxisLoArray = {xLo, 0, 0};
        Vector[] xAxisVectors = {};
        polygons[0] = new Polygon(new Vector[]{
            new Vector(xAxisHiArray), 
            new Vector(xAxisLoArray), 
            new Vector(xAxisLoArray)});
        polygons[0].setEdgeColor(edgeColor);
        polygons[0].setShapeID(shapeName +"_"+ numID);
        polygons[0].setPolygonID("X-Axis");
        
        double[] yAxisHiArray = {0, yHi, 0};
        double[] yAxisLoArray = {0, yLo, 0};
        polygons[1] = new Polygon(new Vector[]{
            new Vector(yAxisHiArray), 
            new Vector(yAxisLoArray), 
            new Vector(yAxisLoArray)});
        polygons[1].setEdgeColor(edgeColor);
        polygons[1].setShapeID(shapeName +"_"+ numID);
        polygons[1].setPolygonID("Y-Axis");
        
        double[] zAxisHiArray = {0, 0, zHi};
        double[] zAxisLoArray = {0, 0, zLo};
        polygons[2] = new Polygon(new Vector[]{
            new Vector(zAxisHiArray), 
            new Vector(zAxisLoArray), 
            new Vector(zAxisLoArray)});
        polygons[2].setEdgeColor(edgeColor);
        polygons[2].setShapeID(shapeName +"_"+ numID);
        polygons[2].setPolygonID("Z-Axis");
        
        numID++;
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
    public Vector getNormal() {
        return normalVector;
    }
}

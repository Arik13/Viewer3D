package viewer3D.GraphicsEngine;

import viewer3D.Math.Vector;
import java.awt.Color;
import viewer3D.Math.Plane;

/**
 * A collection of vectors specifying a 3 dimensional polygon, as well as a shape 
 * and polygon ID, and face/edge/selection colors
 * @author Arik Dicks
 */
public class Polygon {
    private Vector[] vertices;
    private Vector normal;
    private Plane plane;
    private boolean selected;
    private boolean isVisible;
    private static Color selectionColor = Color.RED;
    private Color faceColor;
    private Color edgeColor;
    private String shapeID;
    private String polygonID;
    //private double zDistance;
    /**
     * Constructs a polygon with vertices corresponding to the given vector array
     * @param vectorArray An array of vertices
     */
    public Polygon(Vector[] vectorArray) {
        isVisible = false;
        vertices = vectorArray;
        this.polygonID = polygonID;
        plane = new Plane(vertices[0], calcNormal());
    }

    /**
     * Constructs a polygon with vertices corresponding to the given vector array, 
     * and with the given polygon ID
     * @param vectorArray
     * @param polygonID
     */
    public Polygon(Vector[] vectorArray, String polygonID) {
        isVisible = false;
        vertices = vectorArray;  
        this.polygonID = polygonID;
        plane = new Plane(vertices[0], calcNormal());
    }

    /**
     * Sets the vertex at the given index to be the given vector
     * @param vertex A vector
     * @param index The index of the vertex to set
     */
    public void setVertex(Vector vertex, int index) {
        vertices[index] = vertex;
    }

    /**
     * Sets the vertices of the polygon to be the vectors in the given vector array
     * @param vectorArray An array of vertices
     */
    public void setVertices(Vector[] vectorArray) {
        vertices = vectorArray;
    }

    /**
     * Sets the vertices of the polygon to be the coordinates in the given double arrays
     * @param vectorArray1 The coordinates of the first vertex
     * @param vectorArray2 The coordinates of the second vertex
     * @param vectorArray3 The coordinates of the third vertex
     */
    public void setVertices(double[] vectorArray1, double[] vectorArray2, double[] vectorArray3) {
        vertices[0] = new Vector(vectorArray1);
        vertices[1] = new Vector(vectorArray2);
        vertices[2] = new Vector(vectorArray3);
    }

    /**
     * Sets the vertices of the polygon to be the give vectors
     * @param vertex1 The first vertex
     * @param vertex2 The second vertex
     * @param vertex3 The third vertex
     */
    public void setVertices(Vector vertex1, Vector vertex2, Vector vertex3) {
        vertices[0] = vertex1;
        vertices[1] = vertex2;
        vertices[2] = vertex3;
    }

    /**
     * Sets the color of this polygons face
     * @param color The color of this polygons face
     */
    public void setFaceColor(Color color) {
        this.faceColor = color;
    }

    /**
     * Sets the color of this polygons edges
     * @param color The color of this polygons edges
     */
    public void setEdgeColor(Color color) {
        this.edgeColor = color;
    }

    /**
     * Sets the ID of this polygon
     * @param polygonID The ID of this polygon
     */
    public void setPolygonID(String polygonID) {
        this.polygonID = polygonID;
    }

    /**
     * Sets the ID of the shape of which this polygon is a piece of
     * @param shapeID The ID of the shape of which this polygon is a piece of
     */
    public void setShapeID(String shapeID) {
        this.shapeID = shapeID;
    }

    public void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }
    /**
     * Returns the vertex at the given index
     * @param index The index of the desired vertex
     * @return the vertex at the given index
     */
    public Vector getVertex(int index) {
        return vertices[index];
    }

    /**
     * Returns the vertices of this polygon
     * @return the vertices of this polygon
     */
    public Vector[] getVertices() {
        return vertices;
    }

    /**
     * Returns the color of this polygons face (if the polygon is selected, returns the selection color)
     * @return the color of this polygons face (if the polygon is selected, returns the selection color)
     */
    public Color getFaceColor() {
        if (selected) {
            return selectionColor;
        }
        return this.faceColor;
    }

    /**
     * Returns the color of this polygons edges
     * @return the color of this polygons edges
     */
    public Color getEdgeColor() {
        return this.edgeColor;
    }

    /**
     * Returns the polygonID of this polygon
     * @return the polygonID of this polygon
     */
    public String getPolygonID() {
        return polygonID;
    }

    /**
     * Returns the shapeID of the shape of which this polygon is a part of
     * @return the shapeID of the shape of which this polygon is a part of
     */
    public String getShapeID() {
        return shapeID;
    }

    public boolean getIsVisible() {
        return isVisible;
    }
    public Vector getNormal() {
        return plane.getNormalVector();
    }
    /**
     * Copies the colors, ID's and selection status of this polygon to the given polygon
     * @param otherPolygon Another polygon
     */
    public void copyAttributes(Polygon otherPolygon) {
        this.selected = otherPolygon.selected;
        this.edgeColor = otherPolygon.edgeColor;
        this.faceColor = otherPolygon.faceColor;
        this.shapeID = otherPolygon.shapeID;
        this.polygonID = otherPolygon.polygonID;
        this.isVisible = otherPolygon.isVisible;
    }

    /**
     * Sets the selection status of this polygon to true
     */ 
    public void select() {
        selected = true;
    }

    /**
     * Sets the selection status of this polygon to false
     */
    public void deselect() {
        selected = false;
    }
    @Override
    public String toString() {
        return "Selected: " + selected + "     " +
                "Face Color: " + faceColor + "     " +
                "Edge Color: " + edgeColor + "     " +
                "Shape ID: " + shapeID + "     " +
                "Polygon ID: " + polygonID;
    }
    
    private Vector calcNormal() {
        Vector v = (vertices[1].subtract(vertices[0])).cross((vertices[2].subtract(vertices[0])));
        return v.multiply(1/v.getLength());
    }
}

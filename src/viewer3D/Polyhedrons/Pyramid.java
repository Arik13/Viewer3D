package viewer3D.Polyhedrons;

import java.awt.Color;
import viewer3D.Math.Vector;
import viewer3D.GraphicsEngine.Polygon;
//import viewer3D.PolygonObject;

/**
 *
 * @author Arik Dicks
 */
public class Pyramid implements Polyhedron {
    private Vector normalVector;
    private static String shapeName = "Pyramid";
    private static int numID = 1;
    private Polygon[] polygons;
    private Color faceColor = Color.WHITE;
    private Color edgeColor = Color.BLACK;

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param scale
     */
    public Pyramid(int x, int y, int z, int scale) {
        polygons = new Polygon[4];

        double[] vertexArray1 = {x,y,z};                        // Front Bottom Left
        double[] vertexArray2 = {x+scale,y,z+scale*2};          // Back Bottom Center
        double[] vertexArray3 = {x+2*scale,y,z};                // Front Bottom Right
        double[] vertexArray4 = {x+scale,y+scale*4,z+scale};    // Middle Top Center 
        
        // Bottom face
        Polygon p1 = new Polygon(new Vector[]{
            new Vector(vertexArray1),
            new Vector(vertexArray2),
            new Vector(vertexArray3) 
        });
        p1.setEdgeColor(edgeColor);
        p1.setFaceColor(faceColor);
        p1.setShapeID(shapeName + "_" + numID);
        p1.setPolygonID("BottomFace");
        
        // Forward face
        Polygon p2 = new Polygon(new Vector[]{
            new Vector(vertexArray1),
            new Vector(vertexArray4),
            new Vector(vertexArray3) 
        });
        p2.setEdgeColor(edgeColor);
        p2.setFaceColor(faceColor);
        p2.setShapeID(shapeName + "_" + numID);
        p2.setPolygonID("ForwardFace");
        
        // Back left face
        Polygon p3 = new Polygon(new Vector[]{
            new Vector(vertexArray1),
            new Vector(vertexArray2),
            new Vector(vertexArray4) 
        });
        p3.setEdgeColor(edgeColor);
        p3.setFaceColor(faceColor);
        p3.setShapeID(shapeName + "_" + numID);
        p3.setPolygonID("BackLeftFace");
        
        // Back right face
        Polygon p4 = new Polygon(new Vector[]{
            new Vector(vertexArray2),
            new Vector(vertexArray3),
            new Vector(vertexArray4) 
        });
        p4.setEdgeColor(edgeColor);
        p4.setFaceColor(faceColor);
        p4.setShapeID(shapeName + "_" + numID);
        p4.setPolygonID("BackRightFace");
        
        polygons[0] = p1;
        polygons[1] = p2;
        polygons[2] = p3;
        polygons[3] = p4;
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
    public Vector getNorma() {
        return normalVector;
    }
}

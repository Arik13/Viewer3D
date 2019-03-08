package viewer3D.Polyhedrons;

import java.awt.Color;
import viewer3D.GraphicsEngine.Polygon;
import viewer3D.Math.Vector;

public class BigAssPolygon implements Polyhedron {
    Polygon[] polygons;
    public BigAssPolygon() {
        polygons = new Polygon[]{new Polygon(new Vector[]{
            new Vector(new double[]{2, 0, 2}),
            new Vector(new double[]{-2, 0, 2}),
            new Vector(new double[]{0, 2, 2})
            })};
        polygons[0].setFaceColor(Color.WHITE);
    }
    @Override
    public Polygon[] getPolygons() {
        return polygons;
    }

    @Override
    public Vector getNormal() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getID() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

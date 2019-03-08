package viewer3D.Polyhedrons;

import viewer3D.Math.Vector;
import viewer3D.GraphicsEngine.Polygon;

/**
 *
 * @author Arik Dicks
 */
public interface Polyhedron {

    /**
     *
     * @return
     */
    public Polygon[] getPolygons();

    /**
     *
     * @return
     */
    public Vector getNormal();

    /**
     *
     * @return
     */
    public String getID();
}

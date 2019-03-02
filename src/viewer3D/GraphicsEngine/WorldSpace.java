package viewer3D.GraphicsEngine;

import java.util.ArrayList;
import viewer3D.Polyhedrons.Polyhedron;

/**
 *
 * @author Arik Dicks
 */
public class WorldSpace {
    ArrayList<Polyhedron> polyhedrons;

    /**
     * Constructs a world space, which consists of a set of polyhedrons (shapes made of polygons)
     */
    public WorldSpace() {
        polyhedrons = new ArrayList<>();
    }

    /**
     * Adds a polyhedron to this world space
     * @param polyhedron A polyhedron
     */
    public void add(Polyhedron polyhedron) {
        polyhedrons.add(polyhedron);
    }

    /**
     * Returns the full set of polygons contained in all this world spaces polyhedrons
     * @return
     */
    public Polygon[] getPolygons() {
        ArrayList<Polygon> polygonsList = new ArrayList<>();
        for (int i = 0; i < polyhedrons.size(); i++) {
            Polygon[] shapePolygons = polyhedrons.get(i).getPolygons();
            for (int j = 0; j < shapePolygons.length; j++) {
                polygonsList.add(shapePolygons[j]);
            }
        }
        Polygon[] polygons = new Polygon[polygonsList.size()];
        return polygonsList.toArray(polygons);
    }
}

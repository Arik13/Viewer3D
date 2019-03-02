package viewer3D.GraphicsEngine;

import viewer3D.Math.Vector;

/**
 * A polygon that has been projected in some way
 * @author Arik Dicks
 */
public class ProjectedPolygon extends Polygon {
    Polygon originalPolygon;

    /**
     * Constructs this polygon with vertices and a reference to the original polygon
     * @param vectorArray The vertices of this polygon
     * @param originalPolygon The original polygon
     */
    public ProjectedPolygon(Vector[] vectorArray, Polygon originalPolygon) {
        super(vectorArray);
        super.copyAttributes(originalPolygon);
        this.originalPolygon = originalPolygon;
    }

    /**
     * Sets the selection status of this polygon to true
     */
    @Override
    public void select() {
        originalPolygon.select();
        super.select();
    }

    /**
     * Sets the selection status of this polygon to false
     */
    @Override
    public void deselect() {
        originalPolygon.deselect();
        super.deselect();
    }

    /**
     * Returns the pre-projection polygon this polygon was derived from
     * @return
     */
    public Polygon getOriginalPolygon() {
        return originalPolygon;
    }
    @Override
    public Vector getNormal() {
        return originalPolygon.getNormal();
    }
}

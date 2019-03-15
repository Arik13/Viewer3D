package viewer3D.GraphicsEngine;

import viewer3D.Polyhedrons.BigAssPolygon;

public class BigAssPolygonSpace extends WorldSpace {
    public BigAssPolygonSpace() {
        super.add(new BigAssPolygon());
    }
}
